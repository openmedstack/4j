package org.openmedstack.sample

import com.google.inject.AbstractModule
import com.google.inject.multibindings.Multibinder
import org.openmedstack.commands.IHandleCommands
import org.openmedstack.events.IHandleEvents

class TestModule : AbstractModule() {
    override fun configure() {
        super.configure()
        bind(String::class.java).toInstance("test")
        val commandHandlerSet = Multibinder.newSetBinder(binder(), IHandleCommands::class.java)
        commandHandlerSet.addBinding().to(TestCommandHandler::class.java)
        val eventHandlerSet = Multibinder.newSetBinder(binder(), IHandleEvents::class.java)
        eventHandlerSet.addBinding().to(TestEventHandler::class.java)
    }
}
