package org.openmedstack.commands

import java.time.Instant
import org.openmedstack.ICorrelate
import java.lang.IllegalArgumentException
import org.openmedstack.events.BaseEvent
import org.openmedstack.MessageHeaders
import java.util.concurrent.CompletableFuture
import java.util.HashMap
import org.openmedstack.events.IHandleEvents
import java.lang.AutoCloseable
import org.openmedstack.commands.DomainCommand
import org.openmedstack.commands.CommandResponse
import org.openmedstack.commands.IHandleCommands
import org.openmedstack.DeploymentConfiguration
import org.openmedstack.Chassis
import kotlin.Throws
import java.lang.NullPointerException
import org.openmedstack.ILookupServices
import org.openmedstack.IValidateTokens

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