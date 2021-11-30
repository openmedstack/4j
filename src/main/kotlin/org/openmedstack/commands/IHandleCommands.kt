package org.openmedstack.commands

import org.openmedstack.MessageHeaders
import java.util.concurrent.CompletableFuture

interface IHandleCommands {
    fun <T> canHandle(type: Class<T>): Boolean where T: DomainCommand
    fun handle(command: DomainCommand, messageHeaders: MessageHeaders): CompletableFuture<CommandResponse>
}
