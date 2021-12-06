package org.openmedstack.messaging

import org.openmedstack.MessageHeaders
import org.openmedstack.events.BaseEvent
import org.openmedstack.events.IHandleEvents
import java.util.concurrent.CompletableFuture

class TestEventHandler : IHandleEvents {
    override fun canHandle(type: Class<*>): Boolean {
        return true
    }

    override fun handle(evt: BaseEvent, headers: MessageHeaders): CompletableFuture<*> {
        return CompletableFuture.completedFuture(true)
    }
}
