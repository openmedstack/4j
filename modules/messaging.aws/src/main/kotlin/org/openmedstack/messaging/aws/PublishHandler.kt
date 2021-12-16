package org.openmedstack.messaging.aws

import com.fasterxml.jackson.databind.ObjectMapper
import org.openmedstack.MessageHeadersImpl
import org.openmedstack.events.BaseEvent
import org.openmedstack.events.IHandleEvents
import software.amazon.awssdk.services.sqs.model.Message
import java.util.*
import java.util.concurrent.CompletableFuture

internal class PublishHandler constructor(
    loader: () -> List<Message>,
    private val _mapper: ObjectMapper,
    private val _types: Set<Class<*>>,
    private val _handlers: Set<IHandleEvents>
) : MessageHandler(_mapper), AutoCloseable {
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

            val list = ArrayList<CompletableFuture<*>>()
            for (type in _types) {
                val value = _mapper.readValue(inputEvent, type)
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

    override fun close() {
        timer.cancel()
    }
}

