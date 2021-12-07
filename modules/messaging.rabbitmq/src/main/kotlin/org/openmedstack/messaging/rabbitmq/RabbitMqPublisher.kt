package org.openmedstack.messaging.rabbitmq

import com.fasterxml.jackson.databind.ObjectMapper
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import io.cloudevents.core.builder.CloudEventBuilder
import io.cloudevents.core.provider.EventFormatProvider
import io.cloudevents.jackson.JsonFormat
import org.openmedstack.IProvideTopic
import org.openmedstack.events.BaseEvent
import org.openmedstack.events.IPublishEvents
import java.net.URI
import java.nio.charset.StandardCharsets
import java.time.Clock
import java.time.OffsetDateTime
import java.util.*
import java.util.concurrent.CompletableFuture

class RabbitMqPublisher constructor(
    private val _connection: Connection,
    private val _topicProvider: IProvideTopic,
    private val _mapper: ObjectMapper
) : IPublishEvents, AutoCloseable {
    private val _channel: Channel = _connection.createChannel()
    override fun <T : BaseEvent> publish(evt: T, headers: HashMap<String, Any>): CompletableFuture<*> {
        return CompletableFuture.supplyAsync {
            val topic = _topicProvider.get(evt::class.java)
            _channel.basicPublish(
                topic,
                "",
                AMQP.BasicProperties.Builder().build(),
                getMessageBytes(UUID.randomUUID().toString(), evt)
            )
            topic
        }
    }

    private fun <T> getMessageBytes(id: String, evt: T): ByteArray where T : BaseEvent {
        val topic = _topicProvider.getCanonical(evt::class.java)
        val event = CloudEventBuilder.v1()
            .withId(id)
            .withTime(OffsetDateTime.now(Clock.systemUTC()))
            .withType(JsonFormat.CONTENT_TYPE)
            .withSubject(topic)
            .withSource(URI.create("http://localhost"))
            .withData("application/json+${topic}") {
                _mapper.writeValueAsString(evt).toByteArray(StandardCharsets.UTF_8)
            }
        if (!evt.correlationId.isNullOrBlank()) {
            event.withExtension("correlation_id", evt.correlationId!!)
        }

        return EventFormatProvider
            .getInstance()
            .resolveFormat(JsonFormat.CONTENT_TYPE)!!
            .serialize(event.build())
    }

    override fun close() {
        _channel.close()
        _connection.close()
    }
}
