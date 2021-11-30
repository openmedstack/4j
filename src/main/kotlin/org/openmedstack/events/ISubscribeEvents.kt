package org.openmedstack.events

import java.util.concurrent.CompletableFuture

interface ISubscribeEvents {
    fun subscribe(handle: IHandleEvents): CompletableFuture<AutoCloseable>
}