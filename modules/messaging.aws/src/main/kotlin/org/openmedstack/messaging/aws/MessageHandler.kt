package org.openmedstack.messaging.aws

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import software.amazon.awssdk.services.sqs.model.Message
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue

internal abstract class MessageHandler constructor(private val _mapper: ObjectMapper) {
    fun readDelivery(d: Message): Pair<JsonParser, Map<String, MessageAttributeValue>> {
        val p = _mapper.createParser(d.body())
        val node = p.readValueAsTree() as ObjectNode
        val inputEvent = node.remove("data")
        val headers = d.messageAttributes()
        return Pair(inputEvent.traverse(p.codec), headers)
    }
}
