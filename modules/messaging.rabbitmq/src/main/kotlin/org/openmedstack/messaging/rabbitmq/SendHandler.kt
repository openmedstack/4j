package org.openmedstack.messaging.rabbitmq

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.Channel
import com.rabbitmq.client.DeliverCallback
import com.rabbitmq.client.Delivery
import io.cloudevents.jackson.JsonFormat
import org.openmedstack.IProvideTopic
import org.openmedstack.MessageHeadersImpl
import org.openmedstack.commands.DomainCommand
import org.openmedstack.commands.IHandleCommands
import org.openmedstack.messaging.CloudEventFactory
import java.net.URI
import java.util.*

internal class SendHandler constructor(
    private val _mapper: CloudEventFactory,
    private val _types: Set<Class<*>>,
    private val _handlers: Set<IHandleCommands>,
    private val _channel: Channel,
    private val _topicProvider: IProvideTopic,
    private val _source: URI
) : MessageHandler(_mapper), DeliverCallback {

    override fun handle(tag: String?, d: Delivery) {
        try {
            val (codec, inputEvent, headers) = readDelivery(d)

            for (type in _types) {
                var handled = false
                val value = _mapper.mapper.readValue(inputEvent.traverse(codec), type)
                if (value is DomainCommand) {
                    for (handler in _handlers) {
                        try {
                            if (handler.canHandle(type)) {
                                handled = sendMessage(handler, value, headers)
                                break
                            }
                        } catch (e: Exception) {
                        }
                    }
                }
                if (handled) {
                    break
                }
            }
        } catch (e: Exception) {
            println(e.message)
        }
    }

    private fun sendMessage(
        handler: IHandleCommands,
        value: DomainCommand,
        headers: HashMap<String, Any>
    ): Boolean {
        val response =
            handler.handle(value, MessageHeadersImpl(headers)).join()
        val props = AMQP.BasicProperties.Builder()
            .headers(headers)
            .messageId()
            .correlationId(value.correlationId ?: "")
            .contentEncoding("application/json+${_topicProvider.get(value::class.java)}")
            .type(JsonFormat.CONTENT_TYPE)
            .build()
        val cloudEvent = _mapper.toCloudEvent(
            UUID.randomUUID().toString(),
            response,
            _source
        )

        _channel.basicPublish(
            "",
            headers["reply-to"].toString(),
            props,
            _mapper.toBytes(cloudEvent)
        )
        return true
    }
}
