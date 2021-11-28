package org.openmedstack.domain

import org.openmedstack.events.DomainEvent

interface Aggregate {
    val id: String
    val version: Int
    @Throws(HandlerForDomainEventNotFoundException::class)
    fun applyEvent(event: Any)
    val uncommittedEvents: MutableList<DomainEvent>
    fun clearUncommittedEvents()
    fun getSnapshot(): Memento
}
