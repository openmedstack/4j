package org.openmedstack

import org.openmedstack.commands.CommandResponse
import org.openmedstack.commands.DomainCommand
import org.openmedstack.commands.IHandleCommands
import java.util.concurrent.CompletableFuture

class TestHandler : IHandleCommands {
    override fun <T> canHandle(type: Class<T>): Boolean where T: DomainCommand {
        return true
    }

    override fun handle(command: DomainCommand, messageHeaders: MessageHeaders): CompletableFuture<CommandResponse> {
        return CompletableFuture.completedFuture(CommandResponse.success(command))
    }
}