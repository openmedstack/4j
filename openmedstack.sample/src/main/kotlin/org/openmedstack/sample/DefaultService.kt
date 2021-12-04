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
    private val _injector: Injector

    override fun start(): CompletableFuture<*> {
        return CompletableFuture.completedFuture(true)
    }

    override fun <T> send(command: T): CompletableFuture<CommandResponse> where T : DomainCommand {
        val sender = _injector.getInstance(IRouteCommands::class.java)
        return sender.send(command, HashMap())
    }

    override fun <T> publish(msg: T): CompletableFuture<*> where T : BaseEvent {
        val sender = _injector.getInstance(IPublishEvents::class.java)
        return sender.publish(msg, HashMap())
    }

    override fun <T> resolve(type: Class<T>): T where T : Any {
        return _injector.getInstance(type)
    }

    @Throws(Exception::class)
    override fun close() {
    }

    init {
        _injector = Guice.createInjector(*modules)
    }
}
