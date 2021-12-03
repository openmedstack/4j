package org.openmedstack.messaging

import org.openmedstack.MessageHeadersImpl
import org.openmedstack.commands.CommandResponse
import org.openmedstack.commands.DomainCommand
import org.openmedstack.commands.IHandleCommands
import org.openmedstack.commands.IRouteCommands
import java.util.concurrent.CompletableFuture

class InMemoryRouter constructor(private val _commandHandlers: Set<IHandleCommands>) : IRouteCommands {
    override fun <T : DomainCommand> send(
        command: T,
        headers: HashMap<String, Any>
    ): CompletableFuture<CommandResponse> {
        var finalError = ""
        val results = _commandHandlers.filter { h -> h.canHandle(command::class.java) }.map { h: IHandleCommands ->
            h.handle(
                command,
                MessageHeadersImpl(headers)
            )
        }
        val result = CompletableFuture.allOf(*results.toTypedArray()).thenComposeAsync {
            results.map { f: CompletableFuture<CommandResponse> -> f.get() }.map { c -> c.faultMessage }
                .forEach { s ->
                    if (!s.isNullOrBlank()) {
                        finalError += s
                    }
                }
            if (finalError.isNullOrBlank()) {
                CompletableFuture.completedFuture(CommandResponse.success(command))
            } else {
                CompletableFuture.completedFuture(CommandResponse.error(command, finalError))
            }
        }

        return result
    }
}

