package org.openmedstack.messaging.rabbitmq

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.rabbitmq.client.*
import org.openmedstack.DeploymentConfiguration
import org.openmedstack.IProvideTopic
import org.openmedstack.MessageHeadersImpl
import org.openmedstack.ReflectionTool
import org.openmedstack.events.BaseEvent
import org.openmedstack.events.IHandleEvents
import java.util.concurrent.CompletableFuture
import java.util.stream.Collectors

class RabbitMqListener constructor(
    connection: Connection,
    configuration: DeploymentConfiguration,
    private val _topicProvider: IProvideTopic,
    private val _handlers: Set<IHandleEvents>,
    mapper: ObjectMapper,
    vararg packages: Package
) {
    private val _mapper: ObjectMapper
    private val _configuration: DeploymentConfiguration
    private val _channel: Channel

    init {
        _mapper = mapper
        _configuration = configuration
        val graph = ReflectionTool.findAllClasses(*packages)
            .filter { c -> BaseEvent::class.java.isAssignableFrom(c) }
            .filter { c -> _handlers.any { h -> h.canHandle(c) } }
            .collect(Collectors.toSet())
            .groupBy { c -> _topicProvider.get(c) }

        _channel = connection.createChannel()
        for (entry in graph) {
            _channel.exchangeDeclare(entry.key, BuiltinExchangeType.TOPIC)
            val queue = _channel.queueDeclare()
            _channel.queueBind(queue.queue, entry.key, "#")
            val handlers =
                _handlers.stream().filter { h -> entry.value.stream().anyMatch { c -> h.canHandle(c) } }.toList()
            val deliverCallback = DeliverHandler(_mapper, entry.value.toSet(), handlers.toSet())
            _channel.basicConsume(queue.queue, true, deliverCallback) { _: String -> }
        }
    }

    private class DeliverHandler constructor(
        private val _mapper: ObjectMapper,
        private val _types: Set<Class<*>>,
        private val _handlers: Set<IHandleEvents>
    ) : DeliverCallback {
        override fun handle(tag: String?, d: Delivery) {
            try {
                val p = _mapper.createParser(d.body)
                val node = p.readValueAsTree() as ObjectNode
                val inputEvent = node.remove("data")
                val headers = generateHeaders(d.properties, node)

                val list = ArrayList<CompletableFuture<*>>()
                for (type in _types) {
                    val value = _mapper.readValue(inputEvent.traverse(p.codec), type)
                    for (handler in _handlers) {
                        try {
                            if (handler.canHandle(type)) {
                                var task = handler.handle(value as BaseEvent, MessageHeadersImpl(headers))
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

        private fun generateHeaders(
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
    }
}
