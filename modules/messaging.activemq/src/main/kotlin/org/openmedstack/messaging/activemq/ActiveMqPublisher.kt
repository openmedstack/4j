package org.openmedstack.messaging.activemq

import org.openmedstack.IProvideTopic
import org.openmedstack.events.BaseEvent
import org.openmedstack.events.IPublishEvents
import org.openmedstack.messaging.CloudEventFactory
import java.net.URI
import java.util.*
import java.util.concurrent.CompletableFuture
import javax.jms.Connection
import javax.jms.Session
import javax.jms.TextMessage
import javax.jms.Topic

class ActiveMqPublisher constructor(
    connection: Connection,
    private val _topicProvider: IProvideTopic,
    private val _mapper: CloudEventFactory
) : IPublishEvents, AutoCloseable {
    private val _source: URI
    private val _channel: Session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE)

    override fun <T : BaseEvent> publish(evt: T, headers: HashMap<String, Any>): CompletableFuture<*> {
        return CompletableFuture.supplyAsync {
            val topic = _topicProvider.get(evt::class.java)
            val producer = _channel.createProducer(Topic { topic })
            val msg = getMessage(UUID.randomUUID().toString(), evt)
            producer.send(msg)
            producer.close()
            CompletableFuture.completedFuture(true)
        }
    }

    private fun <T> getMessage(id: String, evt: T): TextMessage where T : BaseEvent {
        val event = _mapper.toCloudEvent(id, evt, _source)
        val msg = _channel.createTextMessage(
            _mapper.asString(event)
        )
        msg.jmsMessageID = id
        msg.jmsTimestamp = event.time!!.toEpochSecond()
        msg.jmsCorrelationID = evt.correlationId
        return msg
    }

    override fun close() {
        _channel.close()
    }

    init {
        val tmp = URI.create(connection.toString())
        _source = URI(tmp.scheme, null, tmp.host, tmp.port, tmp.path, null, null)
    }
}
