package org.openmedstack.commands

import org.openmedstack.MessageHeaders
import java.util.concurrent.CompletableFuture

interface IHandleCommands<T : DomainCommand?> {
    fun handle(command: T, messageHeaders: MessageHeaders): CompletableFuture<CommandResponse>
}
