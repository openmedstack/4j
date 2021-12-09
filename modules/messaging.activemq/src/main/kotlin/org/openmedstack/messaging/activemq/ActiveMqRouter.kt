package org.openmedstack.messaging.activemq

import com.fasterxml.jackson.databind.ObjectMapper
import io.cloudevents.core.builder.CloudEventBuilder
import io.cloudevents.core.provider.EventFormatProvider
import io.cloudevents.jackson.JsonFormat
import org.openmedstack.ILookupServices
import org.openmedstack.IProvideTopic
import org.openmedstack.ManualResetEvent
import org.openmedstack.commands.CommandResponse
import org.openmedstack.commands.DomainCommand
import org.openmedstack.commands.IRouteCommands
import java.net.URI
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.CompletableFuture
import javax.jms.*
import javax.jms.Queue

class ActiveMqRouter(
    connection: Connection,
    private val _serviceLookup: ILookupServices,
    private val _topicProvider: IProvideTopic,
    private val _mapper: ObjectMapper
) : IRouteCommands, AutoCloseable {
    private val _waitBuffer: HashMap<String, ManualResetEvent> = HashMap()
    private val _responseBuffer: HashMap<String, CommandResponse> = HashMap()
    private val _channel: Session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)
    private val _queue = _channel.createTemporaryQueue()
    private val _consumer: MessageConsumer = _channel.createConsumer(_queue)

    override fun <T> send(
        command: T,
        headers: HashMap<String, Any>
    ): CompletableFuture<CommandResponse> where T : DomainCommand {
        if (command.correlationId.isNullOrBlank()) {
            CompletableFuture.failedFuture<CommandResponse>(RuntimeException("Missing correlation id"))
        }
        return _serviceLookup.lookup(command::class.java).thenComposeAsync { uri ->
            if (uri == null) {
                CompletableFuture.failedFuture<CommandResponse>(RuntimeException("No service endpoint found"))
            }

            val messageId = UUID.randomUUID().toString()
            val waitHandle = ManualResetEvent(false)
            _waitBuffer[messageId] = waitHandle
            val msg = getMessage(messageId, command, headers)
            val producer = _channel.createProducer(Queue { uri!!.path })
            producer.deliveryMode = DeliveryMode.PERSISTENT
            producer.send(msg)
            producer.close()
            waitHandle.waitOne()
            val result = _responseBuffer.remove(messageId)
            if (result != null) {
                CompletableFuture.completedFuture(result)
            } else {
                CompletableFuture.failedFuture(RuntimeException("Could not find result"))
            }
        }
    }

    private fun <T> getMessage(
        id: String,
        command: T,
        headers: Map<String, Any>
    ): TextMessage where T : DomainCommand {
        val topic = _topicProvider.get(command::class.java)
        val event = CloudEventBuilder.v1()
            .withId(id)
            .withType(JsonFormat.CONTENT_TYPE)
            .withSource(URI.create("http://localhost"))
            .withData("application/json+${topic}") {
                _mapper.writeValueAsString(command).toByteArray(StandardCharsets.UTF_8)
            }
            .withSubject(topic)
            .withTime(command.timestamp)
            .withExtension("reply_to", _queue.queueName)
            .withExtension("correlation_id", command.correlationId!!)
            .build()

        val msg = _channel.createTextMessage(
            String(
                EventFormatProvider
                    .getInstance()
                    .resolveFormat(JsonFormat.CONTENT_TYPE)!!
                    .serialize(event)
            )
        )
        msg.jmsCorrelationID = command.correlationId
        msg.jmsReplyTo = _queue
        msg.jmsMessageID = id
        msg.jmsTimestamp = event.time!!.toEpochSecond()
        for (entry in headers) {
            msg.setObjectProperty(entry.key, entry.value)
        }
        return msg
    }

    override fun close() {
        _queue.delete()
        _consumer.close()
        _channel.close()
    }

    init {
        _consumer.messageListener = MessageListener { msg ->
            val messageId = msg.jmsMessageID
            val waitHandle = _waitBuffer.remove(messageId)
            if (waitHandle != null) {
                _responseBuffer[messageId] = when (msg::class.java) {
                    TextMessage::class.java -> _mapper.readValue((msg as TextMessage).text, CommandResponse::class.java)
                    BytesMessage::class.java -> {
                        val m = (msg as BytesMessage)
                        val buffer = ByteArray(m.bodyLength.toInt())
                        m.readBytes(buffer)
                        _mapper.readValue(buffer, CommandResponse::class.java)
                    }
                    else -> CommandResponse("", 0, "Unsupported message type", msg.jmsCorrelationID)
                }
                waitHandle.set()
            }
        }
    }
}
