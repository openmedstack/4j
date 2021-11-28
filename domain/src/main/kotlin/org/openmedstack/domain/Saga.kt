package org.openmedstack.domain

interface Saga {
    val id: String
    val version: Int
    @Throws(HandlerForDomainEventNotFoundException::class)
    fun transition(message: Any)
    val uncommittedEvents: Iterable<Any>
    fun clearUncommittedEvents()
    val undispatchedMessages: Iterable<Any>
    fun clearUndispatchedMessages()
}
