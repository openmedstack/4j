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
import java.time.Period
import java.util.ArrayList
import java.util.regex.Pattern

class DeploymentConfiguration {
    var name: String? = null
    var environment: String? = null
    var services = HashMap<Pattern, URI>()
    var serviceBus: URI? = null
    var serviceBusUsername: String? = null
    var serviceBusPassword: String? = null
    var queueName: String? = null
    var connectionString: String? = null
    var timeout: Period? = null
    var tokenService: String? = null
    var validIssuers: List<String> = ArrayList()
    var clientId: String? = null
    var secret: String? = null
    var scope = ""
}