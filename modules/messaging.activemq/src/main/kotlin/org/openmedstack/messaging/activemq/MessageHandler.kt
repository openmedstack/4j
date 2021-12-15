package org.openmedstack.messaging.activemq

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.ObjectCodec
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.apache.activemq.BlobMessage
import javax.jms.BytesMessage
import javax.jms.Message
import javax.jms.TextMessage

internal abstract class MessageHandler constructor(private val _mapper: ObjectMapper) {
    private fun generateHeaders(msg: Message): HashMap<String, Any> {
        val headers = HashMap<String, Any>()

        if (msg.jmsExpiration >= 0) {
            headers["expiration"] = msg.jmsExpiration
        }
        for (name in msg.propertyNames.toList().map { n -> n.toString() }) {
            headers[name] = msg.getObjectProperty(name)
        }

        return headers
    }

    fun readDelivery(msg: Message): Triple<ObjectCodec, JsonNode, HashMap<String, Any>> {
        val p: JsonParser = when (msg::class.java) {
            TextMessage::class.java -> {
                _mapper.createParser((msg as TextMessage).text)
            }
            BlobMessage::class.java -> {
                _mapper.createParser((msg as BlobMessage).url)
            }
            BytesMessage::class.java -> {
                val m = (msg as BytesMessage)
                val buffer = ByteArray(m.bodyLength.toInt())
                m.readBytes(buffer)
                _mapper.createParser(buffer)
            }
//                ObjectMessage::class.java -> {
//                    _mapper.createParser((msg as ObjectMessage).`object`)
//                }
//                StreamMessage::class.java -> {
//                    val buffer: ByteArray = byteArrayOf()
//                    (msg as StreamMessage).readBytes()
//                    _mapper.createParser()
//                }
            else -> {
                throw RuntimeException("${msg::class.java.name} is not supported")
            }
        }

        val node = p.readValueAsTree() as ObjectNode
        val inputEvent = node.remove("data")
        val headers = generateHeaders(msg)
        return Triple(p.codec, inputEvent, headers)
    }
}