package org.openmedstack.domain

import org.junit.Assert
import org.junit.Test
import org.openmedstack.events.BaseEvent
import java.time.OffsetDateTime

class ConventionEventRouterTest {
    @Test
    fun generatesHandlersFromHandlerSource() {
        val handler = TestHandler()
        val router = ConventionEventRouter(true, handler)
        try {
            router.dispatch(TestMessage("test", OffsetDateTime.now()), TestMessage::class.java)
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

internal class TestMessage(source: String, timeStamp: OffsetDateTime) : BaseEvent(source, timeStamp)