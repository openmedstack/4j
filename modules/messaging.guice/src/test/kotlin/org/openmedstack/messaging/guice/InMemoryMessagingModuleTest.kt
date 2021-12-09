package org.openmedstack.messaging.guice

import com.google.inject.Guice
import junit.framework.TestCase
import org.junit.Assert
import org.junit.Test
import org.openmedstack.commands.IRouteCommands
import java.time.Clock
import java.time.OffsetDateTime

class InMemoryMessagingModuleTest : TestCase() {

    public override fun setUp() {
        super.setUp()
    }

    @Test
    fun testCanRouteCommandsToHandler() {
        val container = Guice.createInjector(InMemoryMessagingModule(), TestModule())
        val router = container.getInstance(IRouteCommands::class.java)
        val response = router.send(TestCommand("test", 1, OffsetDateTime.now(Clock.systemUTC())), HashMap()).get()

        Assert.assertTrue(response.faultMessage.isNullOrBlank())
    }
}
