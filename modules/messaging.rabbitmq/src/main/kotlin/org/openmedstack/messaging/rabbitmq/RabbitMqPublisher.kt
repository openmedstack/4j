package org.openmedstack.messaging.rabbitmq

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import com.rabbitmq.client.Connection
import io.cloudevents.core.provider.EventFormatProvider
import io.cloudevents.jackson.JsonFormat
import org.openmedstack.IProvideTopic
import org.openmedstack.events.BaseEvent
import org.openmedstack.events.IPublishEvents
import org.openmedstack.messaging.CloudEventFactory
import java.net.URI
import java.util.*
import java.util.concurrent.CompletableFuture

class RabbitMqPublisher constructor(
    connection: Connection,
    private val _topicProvider: IProvideTopic,
    private val _mapper: CloudEventFactory
) : IPublishEvents, AutoCloseable {
    private val _channel: Channel = connection.createChannel()
    private val _source: URI

    override fun <T : BaseEvent> publish(evt: T, headers: HashMap<String, Any>): CompletableFuture<*> {
        return CompletableFuture.supplyAsync {
            val topic = _topicProvider.getTenantSpecific(evt::class.java)
            val (bytes, props) = getMessageBytes(UUID.randomUUID().toString(), evt)
            _channel.basicPublish(
                topic,
                "",
                props,
                bytes
            )
            topic
        }
    }

    private fun <T> getMessageBytes(id: String, evt: T): Pair<ByteArray, AMQP.BasicProperties> where T : BaseEvent {
        val event = _mapper.toCloudEvent(id, evt, _source)
        val properties = AMQP.BasicProperties.Builder()
            .messageId(id)
            .correlationId(evt.correlationId ?: "")
            .contentEncoding("application/json+${event.type}").type(JsonFormat.CONTENT_TYPE).build()
        return try {
            Pair(
                EventFormatProvider
                    .getInstance()
                    .resolveFormat(JsonFormat.CONTENT_TYPE)!!
                    .serialize(event), properties
            )
        } catch (e: Exception) {
            println(e.message)
            Pair(byteArrayOf(), properties)
        }
    }

    override fun close() {
        _channel.close()
    }

    init {
        val tmp = URI.create(connection.toString())
        _source = URI(tmp.scheme, null, tmp.host, tmp.port, tmp.path, null, null)
    }
}
