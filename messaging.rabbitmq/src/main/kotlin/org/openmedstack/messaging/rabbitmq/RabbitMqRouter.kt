package org.openmedstack.messaging.rabbitmq

import com.fasterxml.jackson.databind.ObjectMapper
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import io.cloudevents.core.builder.CloudEventBuilder
import io.cloudevents.core.provider.EventFormatProvider
import io.cloudevents.jackson.JsonFormat
import org.openmedstack.ILookupServices
import org.openmedstack.IProvideTopic
import org.openmedstack.commands.CommandResponse
import org.openmedstack.commands.DomainCommand
import org.openmedstack.commands.IRouteCommands
import java.net.URI
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.CompletableFuture

class RabbitMqRouter(
    private val _connection: Connection,
    private val serviceLookup: ILookupServices,
    private val _topicProvider: IProvideTopic,
    private val _mapper: ObjectMapper) : IRouteCommands, AutoCloseable {
    private val _waitBuffer: HashMap<String, ManualResetEvent> = HashMap()
    private val _responseBuffer: HashMap<String, CommandResponse> = HashMap()
    private val _channel: Channel = _connection.createChannel()
    private val _queue: AMQP.Queue.DeclareOk = _channel.queueDeclare()

    override fun <T : DomainCommand> send(
        command: T,
        headers: HashMap<String, Any>
    ): CompletableFuture<CommandResponse> {
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
            _channel.basicPublish("", uri!!.path, true, true, properties, getMessageBytes(messageId, command, properties))
            waitHandle.waitOne()
            val result = _responseBuffer.remove(messageId)
            if (result != null) {
                CompletableFuture.completedFuture(result)
            } else {
                CompletableFuture.failedFuture(RuntimeException("Could not find result"))
            }
        }
    }

    private fun <T> getMessageBytes(id:String, command: T, properties: AMQP.BasicProperties): ByteArray where T : DomainCommand {
        val topic = _topicProvider.get(command::class.java)
        val event = CloudEventBuilder.v1()
            .withId(id)
            .withType(JsonFormat.CONTENT_TYPE)
            .withSource(URI.create("http://localhost"))
            .withData("application/json+${topic}") {
                _mapper.writeValueAsString(command).toByteArray(StandardCharsets.UTF_8)
            }
            .withExtension("reply_to", properties.replyTo)
            .withExtension("correlation_id", properties.correlationId)
            .build()

        return EventFormatProvider
            .getInstance()
            .resolveFormat(JsonFormat.CONTENT_TYPE)!!
            .serialize(event)
    }

    override fun close() {
        _channel.close()
        _connection.close()
    }

    init {
        _channel.basicConsume(_queue.queue, true, { _, delivery ->
            val messageId = delivery.properties.messageId
            val response = _mapper.readValue(delivery.body, CommandResponse::class.java)
            val waitHandle = _waitBuffer.remove(messageId)
            if (waitHandle != null) {
                _responseBuffer[messageId] = response
                waitHandle.set()
            }
        }) { _: String -> }
    }
}
