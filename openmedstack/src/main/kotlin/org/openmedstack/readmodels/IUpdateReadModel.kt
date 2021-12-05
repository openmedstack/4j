package org.openmedstack.readmodels

import org.openmedstack.MessageHeaders
import org.openmedstack.events.BaseEvent
import java.util.concurrent.CompletableFuture

interface IUpdateReadModel<T> where T: BaseEvent {
    fun canUpdate(eventType: Class<*>): Boolean
    fun <T> update(domainEvent: T, headers: MessageHeaders?): CompletableFuture<*>
}