package org.openmedstack.events

import org.openmedstack.ICorrelate
import java.time.OffsetDateTime

abstract class BaseEvent protected constructor(
    source: String,
    timeStamp: OffsetDateTime,
    correlationId: String? = null
) : ICorrelate {
    val source: String
    val timestamp: OffsetDateTime
    override val correlationId: String?

    init {
        require(!(timeStamp === OffsetDateTime.MIN))
        this.source = source
        timestamp = timeStamp
        this.correlationId = correlationId
    }
}