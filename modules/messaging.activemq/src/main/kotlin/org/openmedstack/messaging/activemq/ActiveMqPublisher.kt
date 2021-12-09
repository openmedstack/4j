package org.openmedstack.messaging.activemq

import com.fasterxml.jackson.databind.ObjectMapper
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
import javax.jms.Connection
import javax.jms.Session
import javax.jms.TextMessage
import javax.jms.Topic

class ActiveMqPublisher constructor(
    connection: Connection,
    private val _topicProvider: IProvideTopic,
    private val _mapper: ObjectMapper
) : IPublishEvents, AutoCloseable {
    private val _channel: Session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)

    override fun <T : BaseEvent> publish(evt: T, headers: HashMap<String, Any>): CompletableFuture<*> {
        return CompletableFuture.supplyAsync {
            val topic = _topicProvider.get(evt::class.java)
            var producer = _channel.createProducer(Topic { topic })
            val msg = getMessage(UUID.randomUUID().toString(), evt)
            producer.send(msg)
            producer.close()
            CompletableFuture.completedFuture(true)
        }
    }

    private fun <T> getMessage(id: String, evt: T): TextMessage where T : BaseEvent {
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
        val ce = event.build()
        val msg = _channel.createTextMessage(
            String(
                EventFormatProvider
                    .getInstance()
                    .resolveFormat(JsonFormat.CONTENT_TYPE)!!
                    .serialize(ce)
            )
        )
        msg.jmsMessageID = id
        msg.jmsTimestamp = ce.time.toEpochSecond()
        msg.jmsCorrelationID = evt.correlationId
        return msg
    }

    override fun close() {
        _channel.close()
    }
}
