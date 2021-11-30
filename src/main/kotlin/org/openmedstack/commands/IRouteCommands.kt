package org.openmedstack.commands

import java.util.concurrent.CompletableFuture

interface IRouteCommands {
    fun <T : DomainCommand> send(command: T, headers: HashMap<String, Any>): CompletableFuture<CommandResponse>
}
