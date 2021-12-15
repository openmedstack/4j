package org.openmedstack.messaging.rabbitmq

import com.fasterxml.jackson.core.ObjectCodec
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Delivery

internal abstract class MessageHandler constructor(private val _mapper: ObjectMapper) {
    fun generateHeaders(
        prop: AMQP.BasicProperties
    ): HashMap<String, Any> {
        val headers = HashMap<String, Any>()
        if (!prop.replyTo.isNullOrBlank()) {
            headers["reply-to"] = prop.correlationId
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

        return headers
    }

    fun readDelivery(d: Delivery): Triple<ObjectCodec, JsonNode, HashMap<String, Any>> {
        val p = _mapper.createParser(d.body)
        val node = p.readValueAsTree() as ObjectNode
        val inputEvent = node.remove("data")
        val headers = generateHeaders(d.properties)
        return Triple(p.codec, inputEvent, headers)
    }
}
