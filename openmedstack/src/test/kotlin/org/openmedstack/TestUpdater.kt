package org.openmedstack

import org.openmedstack.events.BaseEvent
import org.openmedstack.readmodels.IUpdateReadModel
import java.util.concurrent.CompletableFuture

class TestUpdater: IUpdateReadModel<BaseEvent> {
    override fun canUpdate(eventType: Class<*>): Boolean {
        return true
    }
    override fun <T> update(domainEvent: T, headers: MessageHeaders?): CompletableFuture<*> {
        return CompletableFuture.completedFuture(true)
    }
}