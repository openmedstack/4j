package org.openmedstack.domain

import org.openmedstack.events.BaseEvent

interface Saga {
    val id: String
    val version: Int
    @Throws(HandlerForDomainEventNotFoundException::class)
    fun transition(message: BaseEvent)
    val uncommittedEvents: Iterable<BaseEvent>
    fun clearUncommittedEvents()
    val undispatchedMessages: Iterable<Any>
    fun clearUndispatchedMessages()
}
