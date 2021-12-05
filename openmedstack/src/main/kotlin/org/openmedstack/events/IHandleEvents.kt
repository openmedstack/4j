package org.openmedstack.events

import org.openmedstack.MessageHeaders
import java.util.concurrent.CompletableFuture

interface IHandleEvents {
    fun canHandle(type: Class<*>): Boolean
    fun handle(evt: BaseEvent, headers: MessageHeaders): CompletableFuture<*>
}
