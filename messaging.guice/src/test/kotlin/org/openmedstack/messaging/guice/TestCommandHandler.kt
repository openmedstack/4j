package org.openmedstack.messaging.guice

import org.openmedstack.MessageHeaders
import org.openmedstack.commands.CommandResponse
import org.openmedstack.commands.DomainCommand
import org.openmedstack.commands.IHandleCommands
import java.util.concurrent.CompletableFuture

class TestCommandHandler: IHandleCommands {
    override fun <T> canHandle(type: Class<T>): Boolean where T: DomainCommand {
        return TestCommand::class.java.isAssignableFrom(type)
    }

    override fun handle(command: DomainCommand, messageHeaders: MessageHeaders): CompletableFuture<CommandResponse> {
        return CompletableFuture.completedFuture(CommandResponse.success(command))
    }
}