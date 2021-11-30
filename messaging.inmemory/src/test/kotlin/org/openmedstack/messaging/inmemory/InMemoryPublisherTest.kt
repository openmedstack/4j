package org.openmedstack.messaging.inmemory

import junit.framework.TestCase
import org.junit.Assert
import org.junit.Test
import org.openmedstack.events.IHandleEvents

class InMemoryPublisherTest : TestCase() {
    public override fun setUp() {
        super.setUp()
    }

    @Test
    fun testPublish() {
        val publisher = InMemoryPublisher(setOf(TestEventHandler() as IHandleEvents))
        val task = publisher.publish(TestEvent(), HashMap())

        Assert.assertNotNull(task)
    }
}
