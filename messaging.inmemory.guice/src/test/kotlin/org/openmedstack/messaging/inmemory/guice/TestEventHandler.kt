package org.openmedstack.messaging.inmemory.guice

import org.openmedstack.MessageHeaders
import org.openmedstack.events.EventHandlerBase
import java.util.concurrent.CompletableFuture

class TestEventHandler : EventHandlerBase<TestEvent>() {
    override fun handleInternal(evt: TestEvent, headers: MessageHeaders?): CompletableFuture<*>? {
        return CompletableFuture.completedFuture(true)
    }
}