package org.openmedstack.readmodels

import org.openmedstack.MessageHeaders
import org.openmedstack.events.BaseEvent
import java.util.concurrent.CompletableFuture

interface IUpdateReadModel {
    fun canUpdate(eventType: Class<*>): Boolean
    fun update(domainEvent: BaseEvent, headers: MessageHeaders?): CompletableFuture<*>
}