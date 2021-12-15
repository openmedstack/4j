package org.openmedstack.messaging

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.ObjectCodec
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.cloudevents.CloudEvent
import io.cloudevents.core.builder.CloudEventBuilder
import io.cloudevents.core.provider.EventFormatProvider
import io.cloudevents.jackson.JsonFormat
import org.openmedstack.IProvideTopic
import org.openmedstack.commands.CommandResponse
import org.openmedstack.commands.DomainCommand
import org.openmedstack.events.BaseEvent
import java.net.URI
import java.nio.charset.StandardCharsets
import java.time.Clock
import java.time.OffsetDateTime
import java.util.*

class CloudEventFactory(private val _topicProvider: IProvideTopic, public val mapper: ObjectMapper) {
    private val _formatProvider = EventFormatProvider.getInstance().resolveFormat(JsonFormat.CONTENT_TYPE)!!

    fun read(node: JsonNode, codec: ObjectCodec, type: Class<*>): Any {
        return mapper.readValue(node.traverse(codec), type)
    }

    fun read(parser: JsonParser, type: Class<*>): Any {
        return mapper.readValue(parser, type)
    }

    fun <T> read(bytes: ByteArray, type: Class<T>): T {
        return mapper.readValue(bytes, type)
    }

    fun <T> toCloudEvent(
        id: String,
        evt: T,
        source: URI
    ): CloudEvent where T : BaseEvent {
        return toCloudEvent(id, evt, evt.correlationId, source)
    }

    fun <T> toCloudEvent(
        id: String,
        command: T,
        source: URI
    ): CloudEvent where T : DomainCommand {
        return toCloudEvent(id, command, command.correlationId, source)
    }

    fun toCloudEvent(
        id: String? = null,
        response: CommandResponse,
        source: URI
    ): CloudEvent {
        return toCloudEvent(id ?: UUID.randomUUID().toString(), response, response.correlationId, source)
    }

    private fun toCloudEvent(
        id: String,
        item: Any,
        correlationId: String?,
        source: URI
    ): CloudEvent {
        val topic = _topicProvider.get(item::class.java)
        return CloudEventBuilder.v1()
            .withId(id)
            .withSource(source)
            .withTime(OffsetDateTime.now(Clock.systemUTC()))
            .withType(topic)
            .withSubject(correlationId)
            .withData("application/json+${topic}") {
                mapper.writeValueAsString(item).toByteArray(StandardCharsets.UTF_8)
            }.build()
    }

    fun toBytes(cloudEvent: CloudEvent): ByteArray {
        return _formatProvider.serialize(cloudEvent)
    }

    fun asString(cloudEvent: CloudEvent): String {
        return String(toBytes(cloudEvent))
    }
}

