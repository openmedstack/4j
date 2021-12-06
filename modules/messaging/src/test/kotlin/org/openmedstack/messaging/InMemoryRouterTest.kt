package org.openmedstack.messaging

import junit.framework.TestCase
import org.junit.Assert
import org.junit.Test
import org.openmedstack.commands.IHandleCommands

class InMemoryRouterTest : TestCase() {

    public override fun setUp() {
        super.setUp()
    }

    @Test
    fun testSend() {
        var router = InMemoryRouter(setOf(TestCommandHandler() as IHandleCommands))
        var result = router.send(TestCommand("test", 1), HashMap()).get()

        Assert.assertNull(result.faultMessage)
    }
}
