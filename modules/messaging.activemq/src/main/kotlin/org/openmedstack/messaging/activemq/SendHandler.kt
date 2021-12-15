package org.openmedstack.messaging.activemq

import org.apache.activemq.command.ActiveMQTextMessage
import org.openmedstack.MessageHeadersImpl
import org.openmedstack.commands.DomainCommand
import org.openmedstack.commands.IHandleCommands
import org.openmedstack.messaging.CloudEventFactory
import java.net.URI
import javax.jms.Message
import javax.jms.MessageListener
import javax.jms.Session

internal class SendHandler constructor(
    private val _mapper: CloudEventFactory,
    private val _types: Set<Class<*>>,
    private val _handlers: Set<IHandleCommands>,
    private val _channel: Session
) : MessageHandler(_mapper.mapper), MessageListener {
    override fun onMessage(msg: Message) {
        try {
            val (codec, inputEvent, headers) = readDelivery(msg)

            for (type in _types) {
                var handled = false
                val value = _mapper.read(inputEvent.traverse(codec), type)
                for (handler in _handlers) {
                    try {
                        if (handler.canHandle(type)) {
                            val response =
                                handler.handle(value as DomainCommand, MessageHeadersImpl(headers)).join()
                            handled = true
                            val producer = _channel.createProducer(msg.jmsReplyTo)
                            val send = ActiveMQTextMessage()
                            send.text = _mapper.asString(
                                _mapper.toCloudEvent(
                                    response = response,
                                    source = URI(msg.jmsDestination.toString())
                                )
                            )
                            producer.send(send)
                            producer.close()
                            break
                        }
                    } catch (e: Exception) {
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
}