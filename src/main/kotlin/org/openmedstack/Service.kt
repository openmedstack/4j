package org.openmedstack

import org.openmedstack.commands.CommandResponse
import org.openmedstack.commands.DomainCommand
import org.openmedstack.events.BaseEvent
import java.util.concurrent.CompletableFuture

interface Service : AutoCloseable {
    fun start(): CompletableFuture<*>
    fun <T> send(command: T): CompletableFuture<CommandResponse> where T : DomainCommand
    fun <T> publish(msg: T): CompletableFuture<*> where T : BaseEvent
    fun <T> resolve(type: Class<T>): T where T : Any
}
