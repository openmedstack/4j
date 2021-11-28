package org.openmedstack

import org.openmedstack.commands.CommandResponse
import org.openmedstack.commands.DomainCommand
import org.openmedstack.events.BaseEvent
import java.util.concurrent.CompletableFuture

interface Service : AutoCloseable {
    fun start(): CompletableFuture<*>
    fun <T : DomainCommand> send(command: T): CompletableFuture<CommandResponse>
    fun <T : BaseEvent> publish(msg: T): CompletableFuture<*>
    fun <T : Any> resolve(type: Class<T>): T
}
