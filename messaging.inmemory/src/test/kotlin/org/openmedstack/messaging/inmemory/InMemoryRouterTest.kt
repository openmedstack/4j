package org.openmedstack.messaging.inmemory

import junit.framework.TestCase
import org.junit.Assert
import org.junit.Test
import org.openmedstack.commands.DomainCommand
import java.time.Instant

class InMemoryRouterTest : TestCase() {

    public override fun setUp() {
        super.setUp()
    }

    @Test
    fun testSend() {
        var router = InMemoryRouter()
        var result = router.send(TestCommand("test", 1), null).get()

        Assert.assertNull(result.faultMessage)
    }
}

