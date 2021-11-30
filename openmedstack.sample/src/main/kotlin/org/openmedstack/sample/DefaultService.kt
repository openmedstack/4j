package org.openmedstack.sample

import com.google.inject.Guice
import com.google.inject.Injector
import com.google.inject.Module
import org.openmedstack.Service
import org.openmedstack.commands.CommandResponse
import org.openmedstack.commands.DomainCommand
import org.openmedstack.commands.IRouteCommands
import org.openmedstack.events.BaseEvent
import org.openmedstack.events.IPublishEvents
import java.util.concurrent.CompletableFuture

class DefaultService(vararg modules: Module?) : Service {
    private val injector: Injector
    override fun start(): CompletableFuture<*> {
        return CompletableFuture.completedFuture(true)
    }

    override fun <T : DomainCommand> send(command: T): CompletableFuture<CommandResponse> {
        val sender = injector.getInstance(IRouteCommands::class.java)
        return sender.send(command, HashMap())
    }

    override fun <T : BaseEvent> publish(msg: T): CompletableFuture<*> {
        val sender = injector.getInstance(IPublishEvents::class.java)
        return sender.publish(msg, HashMap())
    }

    override fun <T : Any> resolve(type: Class<T>): T {
        return injector.getInstance(type)
    }

    @Throws(Exception::class)
    override fun close() {
    }

    init {
        injector = Guice.createInjector(*modules)
    }
}
