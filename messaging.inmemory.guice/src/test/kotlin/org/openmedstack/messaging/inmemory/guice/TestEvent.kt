package org.openmedstack.messaging.inmemory.guice

import org.openmedstack.events.BaseEvent
import java.time.Instant

class TestEvent : BaseEvent("test", Instant.now()) {}