package org.openmedstack.messaging.rabbitmq

import com.fasterxml.jackson.databind.ObjectMapper
import com.rabbitmq.client.DeliverCallback
import com.rabbitmq.client.Delivery
import org.openmedstack.MessageHeadersImpl
import org.openmedstack.events.BaseEvent
import org.openmedstack.events.IHandleEvents
import java.util.concurrent.CompletableFuture

internal class PublishHandler constructor(
    private val _mapper: ObjectMapper,
    private val _types: Set<Class<*>>,
    private val _handlers: Set<IHandleEvents>
) : MessageHandler(_mapper), DeliverCallback {
    override fun handle(tag: String?, d: Delivery) {
        try {
            val (codec, inputEvent, headers) = readDelivery(d)

            val list = ArrayList<CompletableFuture<*>>()
            for (type in _types) {
                val value = _mapper.readValue(inputEvent.traverse(codec), type)
                for (handler in _handlers) {
                    try {
                        if (handler.canHandle(type)) {
                            val task = handler.handle(value as BaseEvent, MessageHeadersImpl(headers))
                            list.add(task)
                        }
                    } catch (e: Exception) {
                    }
                }
            }
            CompletableFuture.allOf(*list.toTypedArray()).join()
        } catch (e: Exception) {
            println(e.message)
        }
    }
}
