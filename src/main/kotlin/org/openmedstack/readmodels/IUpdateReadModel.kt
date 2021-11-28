package org.openmedstack.readmodels

import org.openmedstack.MessageHeaders
import org.openmedstack.events.BaseEvent
import java.util.concurrent.CompletableFuture

interface IUpdateReadModel<T : BaseEvent?> {
    fun update(domainEvent: T, headers: MessageHeaders?): CompletableFuture<*>?
}