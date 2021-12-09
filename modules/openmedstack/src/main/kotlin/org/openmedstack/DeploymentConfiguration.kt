package org.openmedstack

import java.net.URI
import java.time.Duration
import java.util.regex.Pattern

class DeploymentConfiguration {
    var name: String? = null
    var tenantId: String? = null
    var services = HashMap<Pattern, URI>()
    var serviceBus: URI? = null
    var serviceBusUsername: String? = null
    var serviceBusPassword: String? = null
    var queueName: String? = null
    var connectionString: String? = null
    var timeout: Duration = Duration.ofMinutes(5)
    var tokenService: String? = null
    var validIssuers: List<String> = ArrayList()
    var clientId: String? = null
    var secret: String? = null
    var scope = ""
}