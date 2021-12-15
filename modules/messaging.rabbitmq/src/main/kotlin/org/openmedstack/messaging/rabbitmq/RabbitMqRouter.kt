package org.openmedstack.messaging.rabbitmq

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import org.openmedstack.ILookupServices
import org.openmedstack.ManualResetEvent
import org.openmedstack.commands.CommandResponse
import org.openmedstack.commands.DomainCommand
import org.openmedstack.commands.IRouteCommands
import org.openmedstack.messaging.CloudEventFactory
import java.net.URI
import java.util.*
import java.util.concurrent.CompletableFuture

class RabbitMqRouter(
    connection: Connection,
    private val serviceLookup: ILookupServices,
    private val _mapper: CloudEventFactory
) : IRouteCommands, AutoCloseable {
    private val _waitBuffer: HashMap<String, ManualResetEvent> = HashMap()
    private val _responseBuffer: HashMap<String, CommandResponse> = HashMap()
    private val _channel: Channel = connection.createChannel()
    private val _queue: AMQP.Queue.DeclareOk = _channel.queueDeclare()
    private val _source: URI

    override fun <T> send(
        command: T,
        headers: HashMap<String, Any>
    ): CompletableFuture<CommandResponse> where T : DomainCommand {
        if (command.correlationId.isNullOrBlank()) {
            CompletableFuture.failedFuture<CommandResponse>(RuntimeException("Missing correlation id"))
        }

        return serviceLookup.lookup(command::class.java).thenComposeAsync { uri ->
            if (uri == null) {
                CompletableFuture.failedFuture<CommandResponse>(RuntimeException("No service endpoint found"))
            }

            val messageId = UUID.randomUUID().toString()
            val waitHandle = ManualResetEvent(false)
            _waitBuffer[messageId] = waitHandle
            val properties =
                AMQP.BasicProperties.Builder()
                    .deliveryMode(2)
                    .replyTo(_queue.queue)
                    .correlationId(command.correlationId)
                    .messageId(messageId)
                    .headers(headers)
                    .build()
            _channel.basicPublish(
                "",
                uri!!.path.substring(1),
                true,
                true,
                properties,
                _mapper.toBytes(_mapper.toCloudEvent(messageId, command, _source))
            )
            waitHandle.waitOne()
            val result = _responseBuffer.remove(messageId)
            if (result != null) {
                CompletableFuture.completedFuture(result)
            } else {
                CompletableFuture.failedFuture(RuntimeException("Could not find result"))
            }
        }
    }

    override fun close() {
        _channel.close()
    }

    init {
        val tmp = URI.create(connection.toString())
        _source = URI(tmp.scheme, null, tmp.host, tmp.port, tmp.path, null, null)
        _channel.basicConsume(_queue.queue, true, { _, delivery ->
            val messageId = delivery.properties.messageId
            val response = _mapper.read(delivery.body, CommandResponse::class.java)
            val waitHandle = _waitBuffer.remove(messageId)
            if (waitHandle != null) {
                _responseBuffer[messageId] = response
                waitHandle.set()
            }
        }) { _: String -> }
    }
}
