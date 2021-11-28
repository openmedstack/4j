package org.openmedstack.domain

import org.openmedstack.events.BaseEvent
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

interface IRouteEvents {
    fun <T : BaseEvent> register(handler: Consumer<T>)
    @Throws(HandlerForDomainEventNotFoundException::class)
    fun dispatch(eventMessage: Any): CompletableFuture<*>
}
