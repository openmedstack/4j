package org.openmedstack.domain

import org.openmedstack.events.BaseEvent
import java.util.function.Consumer

interface IRouteEvents {
    fun <T> register(handler: Consumer<T>) where T : BaseEvent

    @Throws(HandlerForDomainEventNotFoundException::class)
    fun <T : BaseEvent> dispatch(eventMessage: T, type: Class<T>)
}
