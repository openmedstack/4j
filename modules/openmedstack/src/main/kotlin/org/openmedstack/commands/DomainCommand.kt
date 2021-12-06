package org.openmedstack.commands

import java.time.Instant
import org.openmedstack.ICorrelate

abstract class DomainCommand protected constructor(aggregateId: String, version: Int, timeStamp: Instant, correlationId: String?) : ICorrelate {
    private val _timestamp: Instant
    private val _aggregateId: String
    private val _version: Int
    private val _correlationId: String?
    override val correlationId: String?
        get() = _correlationId
    val timestamp:Instant
        get() = _timestamp
    val aggregateId:String
        get() = _aggregateId
    val version : Int
        get() = _version

    init {
        require(!(timeStamp === Instant.MIN)) { "Cannot use min time" }
        _aggregateId = aggregateId
        _version = version
        _timestamp = timeStamp
        _correlationId = correlationId
    }
}