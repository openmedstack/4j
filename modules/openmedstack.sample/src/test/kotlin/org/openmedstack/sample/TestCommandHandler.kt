package org.openmedstack.sample

import org.openmedstack.MessageHeaders
import org.openmedstack.commands.CommandResponse
import org.openmedstack.commands.DomainCommand
import org.openmedstack.commands.IHandleCommands
import java.util.concurrent.CompletableFuture

class TestCommandHandler : IHandleCommands {
    override fun canHandle(type: Class<*>): Boolean {
        return true
    }

    override fun handle(command: DomainCommand, messageHeaders: MessageHeaders): CompletableFuture<CommandResponse> {
        return CompletableFuture.completedFuture(CommandResponse.success(command))
    }
}