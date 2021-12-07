package org.openmedstack.sample

import org.openmedstack.events.BaseEvent
import java.time.OffsetDateTime

class TestEvent : BaseEvent("test", OffsetDateTime.now()) {}