package org.openmedstack.sample

import org.openmedstack.events.BaseEvent
import java.time.Instant

class TestEvent : BaseEvent("test", Instant.now()) {}