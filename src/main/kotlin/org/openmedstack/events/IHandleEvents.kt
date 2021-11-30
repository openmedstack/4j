package org.openmedstack.events

import org.openmedstack.MessageHeaders
import java.util.concurrent.CompletableFuture

interface IHandleEvents {
    fun <T> canHandle(type: Class<T>): Boolean where T: BaseEvent
    fun handle(evt: BaseEvent, headers: MessageHeaders): CompletableFuture<*>
}
