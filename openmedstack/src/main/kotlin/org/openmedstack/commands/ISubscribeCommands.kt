package org.openmedstack.commands

import java.util.concurrent.CompletableFuture

interface ISubscribeCommands {
    fun Subscribe(handler: IHandleCommands): CompletableFuture<AutoCloseable>
}
