package org.openmedstack.events

import java.time.Instant
import org.openmedstack.ICorrelate

abstract class BaseEvent protected constructor(source: String, timeStamp: Instant, correlationId: String? = null) : ICorrelate {
    val source: String
    val timestamp: Instant
    override val correlationId: String?

    init {
        require(!(timeStamp === Instant.MIN))
        this.source = source
        timestamp = timeStamp
        this.correlationId = correlationId
    }
}