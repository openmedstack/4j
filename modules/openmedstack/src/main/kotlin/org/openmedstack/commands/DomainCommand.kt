package org.openmedstack.commands

import org.openmedstack.ICorrelate
import java.time.OffsetDateTime

abstract class DomainCommand protected constructor(
    aggregateId: String,
    version: Int,
    timeStamp: OffsetDateTime,
    correlationId: String?
) : ICorrelate {
    private val _timestamp: OffsetDateTime
    private val _aggregateId: String
    private val _version: Int
    private val _correlationId: String?
    override val correlationId: String?
        get() = _correlationId
    val timestamp: OffsetDateTime
        get() = _timestamp
    val aggregateId: String
        get() = _aggregateId
    val version: Int
        get() = _version

    init {
        require(timeStamp != OffsetDateTime.MIN) { "Cannot use min time" }
        _aggregateId = aggregateId
        _version = version
        _timestamp = timeStamp
        _correlationId = correlationId
    }
}
