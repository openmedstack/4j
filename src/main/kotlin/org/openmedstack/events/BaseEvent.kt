package org.openmedstack.events

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