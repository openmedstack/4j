package org.openmedstack.events

import java.time.Instant

abstract class DomainEvent protected constructor(source: String, val version: Int, timeStamp: Instant, correlationId: String? = null) : BaseEvent(source, timeStamp, correlationId)