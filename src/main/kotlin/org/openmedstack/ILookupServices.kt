package org.openmedstack

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
import java.net.URI

interface ILookupServices {
    fun lookup(type: Class<*>): CompletableFuture<URI?>
}