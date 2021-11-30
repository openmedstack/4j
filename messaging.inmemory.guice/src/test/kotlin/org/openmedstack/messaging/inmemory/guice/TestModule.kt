package org.openmedstack.messaging.inmemory.guice

import com.google.inject.AbstractModule
import com.google.inject.multibindings.Multibinder
import org.openmedstack.commands.IHandleCommands
import org.openmedstack.events.IHandleEvents

class TestModule : AbstractModule() {
    override fun configure() {
        super.configure()

        val commandSetBinder = Multibinder.newSetBinder(binder(), IHandleCommands::class.java)
        commandSetBinder.addBinding().to(TestCommandHandler::class.java)
        val eventSetBinder = Multibinder.newSetBinder(binder(), IHandleEvents::class.java)
        eventSetBinder.addBinding().to(TestEventHandler::class.java)
    }
}
