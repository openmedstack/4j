package org.openmedstack.sample

import org.openmedstack.MessageHeaders
import org.openmedstack.events.BaseEvent
import org.openmedstack.events.IHandleEvents
import java.util.concurrent.CompletableFuture

class TestEventHandler : IHandleEvents {
    override fun <T : BaseEvent> canHandle(type: Class<T>): Boolean {
        return true
    }

    override fun handle(evt: BaseEvent, headers: MessageHeaders): CompletableFuture<*> {
        return CompletableFuture.completedFuture(true)
    }
}