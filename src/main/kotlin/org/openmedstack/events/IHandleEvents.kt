package org.openmedstack.events

import org.openmedstack.MessageHeaders
import java.util.concurrent.CompletableFuture

interface IHandleEvents<T : BaseEvent?> {
    fun canHandle(type: Class<*>): Boolean
    fun handle(evt: T, headers: MessageHeaders): CompletableFuture<*>
}
