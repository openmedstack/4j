package org.openmedstack.messaging.aws

import org.openmedstack.MessageHeadersImpl
import org.openmedstack.commands.DomainCommand
import org.openmedstack.commands.IHandleCommands
import org.openmedstack.messaging.CloudEventFactory
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.Message
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue
import java.net.URI
import java.util.*

internal class SendHandler constructor(
    loader: () -> List<Message>,
    private val _mapper: CloudEventFactory,
    private val _types: Set<Class<*>>,
    private val _handlers: Set<IHandleCommands>,
    private val _channel: SqsClient,
    private val _source: URI
) : MessageHandler(_mapper.mapper), AutoCloseable {
    private val timer: Timer = Timer()

    init {
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                loader.invoke().forEach { d -> handle(d) }
            }
        }, 1000L, 1000L)
    }

    fun handle(d: Message) {
        try {
            val (inputEvent, headers) = readDelivery(d)

            for (type in _types) {
                var handled = false
                val value = _mapper.mapper.readValue(inputEvent, type)
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
        headers: Map<String, MessageAttributeValue>
    ): Boolean {
        val response =
            handler.handle(value, MessageHeadersImpl(headers)).join()
        val cloudEvent = _mapper.toCloudEvent(UUID.randomUUID().toString(), response, _source)
        val body = _mapper.asString(cloudEvent)
        _channel.sendMessage { b ->
            b.queueUrl(headers["reply-to"].toString()).messageBody(body).messageAttributes(headers)
        }
        return true
    }

    override fun close() {
        timer.cancel()
    }
}