package org.openmedstack.messaging.rabbitmq

import org.openmedstack.ManualResetEvent
import org.openmedstack.MessageHeaders
import org.openmedstack.events.BaseEvent
import org.openmedstack.events.IHandleEvents
import java.util.concurrent.CompletableFuture

class TestEventHandler constructor(private val _waitHandle: ManualResetEvent) : IHandleEvents {
    override fun canHandle(type: Class<*>): Boolean {
        return TestEvent::class.java.isAssignableFrom(type)
    }

    override fun handle(evt: BaseEvent, headers: MessageHeaders): CompletableFuture<*> {
        _waitHandle.set()
        return CompletableFuture.completedFuture(true)
    }
}
