package org.openmedstack.messaging.rabbitmq

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import io.cloudevents.CloudEvent
import io.cloudevents.core.builder.CloudEventBuilder
import java.net.URI
import java.time.OffsetDateTime

class CloudEventDeserializer : JsonDeserializer<CloudEvent>() {
    override fun deserialize(p: JsonParser, ctx: DeserializationContext): CloudEvent {
        val node: JsonNode = p.readValueAsTree()
        if (node.get("specversion").asText() == "1.0") {
            return CloudEventBuilder.v1()
                .withId(node.get("id").asText())
                .withSource(URI.create(node.get("source").asText()))
                .withType(node.get("type").asText())
                .withDataContentType(node.get("datacontenttype").asText())
                .withSubject(node.get("subject").asText())
                .withTime(OffsetDateTime.parse(node.get("time").asText()))
                .withoutData()
                .withDataSchema(if (node.has("dataschema")) URI.create(node.get("dataschema").asText()) else null)
                .build()
        }
        return CloudEventBuilder.v03()
            .withId(node.get("id").asText())
            .withSource(URI.create(node.get("source").asText()))
            .withType(node.get("type").asText())
            .withDataContentType(node.get("datacontenttype").asText())
            .withSubject(node.get("subject").asText())
            .withTime(OffsetDateTime.parse(node.get("time").asText()))
            .withoutData()
            .withDataSchema(if (node.has("dataschema")) URI.create(node.get("dataschema").asText()) else null)
            .build()
    }
}
