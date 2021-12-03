package org.openmedstack.messaging.guice

import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.Inject
import org.junit.Assert
import org.junit.Test
import org.openmedstack.commands.IHandleCommands
import org.openmedstack.events.IHandleEvents

class DomainModuleTest{
    @Test
    fun canLoadDomainTypes() {
        val module = DomainModule(TestEvent::class.java.`package`)
        val injecter = Guice.createInjector(module, TestDomainModule())
        val listener = injecter.getInstance(TestListener::class.java)
        Assert.assertEquals(2, listener.hasHandlers())
    }
}

class TestDomainModule: AbstractModule() {
    override fun configure() {
        super.configure()
        bind(TestListener::class.java).toConstructor(TestListener::class.java.getConstructor(Set::class.java, Set::class.java))
    }
}

class TestListener @Inject constructor(private val eventHandlers: Set<IHandleEvents>, private val commandHandlers: Set<IHandleCommands>) {
    fun hasHandlers(): Int{
        return eventHandlers.size + commandHandlers.size
    }
}