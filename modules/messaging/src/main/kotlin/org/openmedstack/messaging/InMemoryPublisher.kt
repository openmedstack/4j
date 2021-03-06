package org.openmedstack.messaging

import org.openmedstack.MessageHeadersImpl
import org.openmedstack.events.BaseEvent
import org.openmedstack.events.IHandleEvents
import org.openmedstack.events.IPublishEvents
import java.util.concurrent.CompletableFuture

class InMemoryPublisher constructor(private val _eventHandlers: Set<IHandleEvents>) : IPublishEvents {
    override fun <T> publish(
        evt: T,
        headers: HashMap<String, Any>
    ): CompletableFuture<*> where T : BaseEvent {
        val results = _eventHandlers.filter { h -> h.canHandle(evt::class.java) }.map { h: IHandleEvents ->
            h.handle(
                evt,
                MessageHeadersImpl(headers)
            )
        }
        return CompletableFuture.allOf(*results.toTypedArray())
    }
}

