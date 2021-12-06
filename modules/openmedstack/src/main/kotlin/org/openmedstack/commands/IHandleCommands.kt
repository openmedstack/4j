package org.openmedstack.commands

import org.openmedstack.MessageHeaders
import java.util.concurrent.CompletableFuture

interface IHandleCommands {
    fun canHandle(type: Class<*>): Boolean
    fun handle(command: DomainCommand, messageHeaders: MessageHeaders): CompletableFuture<CommandResponse>
}
