package org.openmedstack.messaging.activemq

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.ObjectCodec
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.activemq.BlobMessage
import org.apache.activemq.command.ActiveMQTextMessage
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
import javax.jms.*

class ActiveMqListener constructor(
    connection: Connection,
    configuration: DeploymentConfiguration,
    private val _topicProvider: IProvideTopic,
    eventHandlers: Set<IHandleEvents>,
    commandHandlers: Set<IHandleCommands>,
    mapper: ObjectMapper,
    vararg packages: Package
) : AutoCloseable {
    private val _mapper: ObjectMapper
    private val _configuration: DeploymentConfiguration
    private val _channel: Session

    init {
        _channel = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)
        _mapper = mapper
        _configuration = configuration
        val classes = ReflectionTool.findAllClasses(*packages).toList()
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
            val topic = createTopic(entry)
            val effective: List<IHandleEvents> =
                handlers.stream().filter { h -> entry.value.stream().anyMatch { c -> h.canHandle(c) } }.toList()
            val consumer = _channel.createConsumer(topic)
            consumer.messageListener = PublishHandler(_mapper, entry.value.toSet(), effective.toSet())
        }
    }

    private fun subscribeCommandHandlers(classes: List<Class<*>>, handlers: Set<IHandleCommands>) {
        val graph = classes.stream()
            .filter { c -> DomainCommand::class.java.isAssignableFrom(c) }
            .filter { c -> handlers.any { h -> h.canHandle(c) } }
            .collect(Collectors.toSet())
            .groupBy { c -> _topicProvider.get(c) }

        for (entry in graph) {
            val queue = createTopic(entry)
            val effective =
                handlers.stream().filter { h -> entry.value.stream().anyMatch { c -> h.canHandle(c) } }.toList()
            val consumer = _channel.createConsumer(queue)
            consumer.messageListener = SendHandler(_mapper, entry.value.toSet(), effective.toSet(), _channel)
        }
    }

    private fun createTopic(entry: Map.Entry<String, List<Class<*>>>): Topic {
        return _channel.createTopic(entry.key)
    }

    private class PublishHandler constructor(
        private val _mapper: ObjectMapper,
        private val _types: Set<Class<*>>,
        private val _handlers: Set<IHandleEvents>
    ) : MessageHandler(_mapper), MessageListener {
        override fun onMessage(msg: Message) {
            try {
                val (codec, inputEvent, headers) = readDelivery(msg)

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
        private val _channel: Session
    ) : MessageHandler(_mapper), MessageListener {
        override fun onMessage(msg: Message) {
            try {
                val (codec, inputEvent, headers) = readDelivery(msg)

                for (type in _types) {
                    var handled = false
                    val value = _mapper.readValue(inputEvent.traverse(codec), type)
                    for (handler in _handlers) {
                        try {
                            if (handler.canHandle(type)) {
                                val response =
                                    handler.handle(value as DomainCommand, MessageHeadersImpl(headers)).join()
                                handled = true
                                val producer = _channel.createProducer(msg.jmsReplyTo)
                                val send = ActiveMQTextMessage()
                                send.text = _mapper.writeValueAsString(response)
                                producer.send(send)
                                producer.close()
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
        fun generateHeaders(msg: Message, node: ObjectNode): HashMap<String, Any> {
            val headers = HashMap<String, Any>()

            if (!msg.jmsCorrelationID.isNullOrBlank()) {
                headers["correlation_id"] = msg.jmsCorrelationID
            }
            if (!msg.jmsMessageID.isNullOrBlank()) {
                headers["message_id"] = msg.jmsMessageID
            }
            if (msg.jmsExpiration >= 0) {
                headers["expiration"] = msg.jmsExpiration
            }
            for (name in msg.propertyNames.toList().map { n -> n.toString() }) {
                headers[name] = msg.getObjectProperty(name)
            }

            headers["id"] = node.get("id").asText()
            headers["source"] = node.get("source").asText()
            headers["type"] = node.get("type").asText()
            headers["datacontenttype"] = node.get("datacontenttype").asText()
            headers["subject"] = node.get("subject").asText()
            headers["time"] = node.get("time").asText()
            return headers
        }

        fun readDelivery(msg: Message): Triple<ObjectCodec, JsonNode, HashMap<String, Any>> {
            val p: JsonParser = when (msg::class.java) {
                TextMessage::class.java -> {
                    _mapper.createParser((msg as TextMessage).text)
                }
                BlobMessage::class.java -> {
                    _mapper.createParser((msg as BlobMessage).url)
                }
                BytesMessage::class.java -> {
                    val m = (msg as BytesMessage)
                    val buffer = ByteArray(m.bodyLength.toInt())
                    m.readBytes(buffer)
                    _mapper.createParser(buffer)
                }
//                ObjectMessage::class.java -> {
//                    _mapper.createParser((msg as ObjectMessage).`object`)
//                }
//                StreamMessage::class.java -> {
//                    val buffer: ByteArray = byteArrayOf()
//                    (msg as StreamMessage).readBytes()
//                    _mapper.createParser()
//                }
                else -> {
                    throw RuntimeException("${msg::class.java.name} is not supported")
                }
            }

            val node = p.readValueAsTree() as ObjectNode
            val inputEvent = node.remove("data")
            val headers = generateHeaders(msg, node)
            return Triple(p.codec, inputEvent, headers)
        }
    }

    override fun close() {
        _channel.close()
    }
}
