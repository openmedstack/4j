package org.openmedstack.messaging.activemq

import org.openmedstack.ILookupServices
import org.openmedstack.ManualResetEvent
import org.openmedstack.commands.CommandResponse
import org.openmedstack.commands.DomainCommand
import org.openmedstack.commands.IRouteCommands
import org.openmedstack.messaging.CloudEventFactory
import java.net.URI
import java.util.*
import java.util.concurrent.CompletableFuture
import javax.jms.*
import javax.jms.Queue

class ActiveMqRouter(
    connection: Connection,
    private val _serviceLookup: ILookupServices,
    private val _mapper: CloudEventFactory
) : IRouteCommands, AutoCloseable {
    private val _source: URI
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
        val event = _mapper.toCloudEvent(id, command, _source)

        val msg = _channel.createTextMessage(
            _mapper.asString(event)
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
        val tmp = URI.create(connection.toString())
        _source = URI(tmp.scheme, null, tmp.host, tmp.port, tmp.path, null, null)
        _consumer.messageListener = MessageListener { msg ->
            val messageId = msg.jmsMessageID
            val waitHandle = _waitBuffer.remove(messageId)
            if (waitHandle != null) {
                _responseBuffer[messageId] = when (msg::class.java) {
                    TextMessage::class.java -> _mapper.read(
                        (msg as TextMessage).text.toByteArray(),
                        CommandResponse::class.java
                    )
                    BytesMessage::class.java -> {
                        val m = (msg as BytesMessage)
                        val buffer = ByteArray(m.bodyLength.toInt())
                        m.readBytes(buffer)
                        _mapper.read(buffer, CommandResponse::class.java)
                    }
                    else -> CommandResponse("", 0, "Unsupported message type", msg.jmsCorrelationID)
                }
                waitHandle.set()
            }
        }
    }
}
