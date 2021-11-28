package org.openmedstack.domain

import org.junit.Assert
import org.junit.Test
import org.openmedstack.events.BaseEvent
import java.time.Instant

class ConventionEventRouterTest {
    @Test
    fun generatesHandlersFromHandlerSource() {
        val handler = TestHandler()
        val router = ConventionEventRouter(true, handler)
        try {
            router.dispatch(TestMessage("test", Instant.now()))
        } catch (e: HandlerForDomainEventNotFoundException) {
            Assert.fail()
        }
    }
}

internal class TestHandler {
    fun apply(msg: TestMessage) {
        println(msg.source)
    }
}

internal class TestMessage(source: String, timeStamp: Instant) : BaseEvent(source, timeStamp)