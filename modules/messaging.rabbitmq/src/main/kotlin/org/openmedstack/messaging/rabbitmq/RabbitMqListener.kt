package org.openmedstack.messaging.rabbitmq

import com.fasterxml.jackson.core.ObjectCodec
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.rabbitmq.client.*
import org.openmedstack.DeploymentConfiguration
import org.openmedstack.IProvideTopic
import org.openmedstack.MessageHeadersImpl
import org.openmedstack.ReflectionTool
import org.openmedstack.commands.DomainCommand
import org.openmedstack.commands.IHandleCommands
import org.openmedstack.events.BaseEvent
import org.openmedstack.events.IHandleEvents
import java.util.concurrent.CompletableFuture
import java.util.stream.Collectors

class RabbitMqListener constructor(
    connection: Connection,
    configuration: DeploymentConfiguration,
    private val _topicProvider: IProvideTopic,
    eventHandlers: Set<IHandleEvents>,
    commandHandlers: Set<IHandleCommands>,
    mapper: ObjectMapper,
    packages: Set<Package>
) : AutoCloseable {
    private val _mapper: ObjectMapper
    private val _configuration: DeploymentConfiguration
    private val _channel: Channel

    init {
        _channel = connection.createChannel()
        _mapper = mapper
        _configuration = configuration
        val classes: List<Class<*>> = ReflectionTool.findAllClasses(*packages.toTypedArray()).toList()
        subscribeEventHandlers(classes, eventHandlers)
        subscribeCommandHandlers(classes, commandHandlers)
    }

    private fun subscribeEventHandlers(classes: List<Class<*>>, handlers: Set<IHandleEvents>) {
        val graph = classes.stream()
            .filter { c -> BaseEvent::class.java.isAssignableFrom(c) }
            .filter { c -> handlers.any { h -> h.canHandle(c) } }
            .collect(Collectors.toSet())
            .groupBy { c -> _topicProvider.get(c) }

        for (entry in graph) {
            val queue = createQueue(entry)
            val effective: List<IHandleEvents> =
                handlers.stream().filter { h -> entry.value.stream().anyMatch { c -> h.canHandle(c) } }.toList()
            val deliverCallback = PublishHandler(_mapper, entry.value.toSet(), effective.toSet())
            _channel.basicConsume(queue.queue, true, deliverCallback) { _: String -> }
        }
    }

    private fun subscribeCommandHandlers(classes: List<Class<*>>, handlers: Set<IHandleCommands>) {
        val graph = classes.stream()
            .filter { c -> DomainCommand::class.java.isAssignableFrom(c) }
            .filter { c -> handlers.any { h -> h.canHandle(c) } }
            .collect(Collectors.toSet())
            .groupBy { c -> _topicProvider.get(c) }

        for (entry in graph) {
            val queue = createQueue(entry)
            val effective =
                handlers.stream().filter { h -> entry.value.stream().anyMatch { c -> h.canHandle(c) } }.toList()
            val deliverCallback = SendHandler(_mapper, entry.value.toSet(), effective.toSet(), _channel)
            _channel.basicConsume(queue.queue, true, deliverCallback) { _: String -> }
        }
    }

    private fun createQueue(entry: Map.Entry<String, List<Class<*>>>): AMQP.Queue.DeclareOk {
        _channel.exchangeDeclare(entry.key, BuiltinExchangeType.TOPIC)
        val queue = _channel.queueDeclare()
        _channel.queueBind(queue.queue, entry.key, "#")
        return queue
    }

    private class PublishHandler constructor(
        private val _mapper: ObjectMapper,
        private val _types: Set<Class<*>>,
        private val _handlers: Set<IHandleEvents>
    ) : MessageHandler(_mapper), DeliverCallback {
        override fun handle(tag: String?, d: Delivery) {
            try {
                val (codec, inputEvent, headers) = readDelivery(d)

                val list = ArrayList<CompletableFuture<*>>()
                for (type in _types) {
                    val value = _mapper.readValue(inputEvent.traverse(codec), type)
                    for (handler in _handlers) {
                        try {
                            if (handler.canHandle(type)) {
                                val task = handler.handle(value as BaseEvent, MessageHeadersImpl(headers))
                                list.add(task)
                            }
                        } catch (e: Exception) {
                        }
                    }
                }
                CompletableFuture.allOf(*list.toTypedArray()).join()
            } catch (e: Exception) {
                println(e.message)
            }
        }
    }

    private class SendHandler constructor(
        private val _mapper: ObjectMapper,
        private val _types: Set<Class<*>>,
        private val _handlers: Set<IHandleCommands>,
        private val _channel: Channel
    ) : MessageHandler(_mapper), DeliverCallback {
        override fun handle(tag: String?, d: Delivery) {
            try {
                val (codec, inputEvent, headers) = readDelivery(d)

                for (type in _types) {
                    var handled = false
                    val value = _mapper.readValue(inputEvent.traverse(codec), type)
                    for (handler in _handlers) {
                        try {
                            if (handler.canHandle(type)) {
                                val response =
                                    handler.handle(value as DomainCommand, MessageHeadersImpl(headers)).join()
                                handled = true
                                _channel.basicPublish(
                                    d.properties.replyTo,
                                    "",
                                    null,
                                    _mapper.writeValueAsBytes(response)
                                )
                                break
                            }
                        } catch (e: Exception) {
                        }
                    }
                    if (handled) {
                        break
                    }
                }
            } catch (e: Exception) {
                println(e.message)
            }
        }
    }

    private abstract class MessageHandler constructor(private val _mapper: ObjectMapper) {
        fun generateHeaders(
            prop: AMQP.BasicProperties,
            node: ObjectNode
        ): HashMap<String, Any> {
            val headers = HashMap<String, Any>()
            if (!prop.correlationId.isNullOrBlank()) {
                headers["correlation_id"] = prop.correlationId
            }
            if (!prop.messageId.isNullOrBlank()) {
                headers["message_id"] = prop.messageId
            }
            if (!prop.clusterId.isNullOrBlank()) {
                headers["cluster_id"] = prop.clusterId
            }
            if (!prop.appId.isNullOrBlank()) {
                headers["app_id"] = prop.appId
            }
            if (!prop.contentEncoding.isNullOrBlank()) {
                headers["content_encoding"] = prop.contentEncoding
            }
            if (!prop.expiration.isNullOrBlank()) {
                headers["expiration"] = prop.expiration
            }
            if (prop.headers != null) {
                prop.headers.forEach { e -> headers[e.key] = e.value }
            }

            headers["id"] = node.get("id").asText()
            headers["source"] = node.get("source").asText()
            headers["type"] = node.get("type").asText()
            headers["datacontenttype"] = node.get("datacontenttype").asText()
            headers["subject"] = node.get("subject").asText()
            headers["time"] = node.get("time").asText()
            return headers
        }

        fun readDelivery(d: Delivery): Triple<ObjectCodec, JsonNode, HashMap<String, Any>> {
            val p = _mapper.createParser(d.body)
            val node = p.readValueAsTree() as ObjectNode
            val inputEvent = node.remove("data")
            val headers = generateHeaders(d.properties, node)
            return Triple(p.codec, inputEvent, headers)
        }
    }

    override fun close() {
        _channel.close()
    }
}
