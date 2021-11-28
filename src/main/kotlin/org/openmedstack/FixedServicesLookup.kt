package org.openmedstack

import java.net.URI
import java.util.concurrent.CompletableFuture
import java.util.regex.Pattern

class FixedServicesLookup(private val _serviceAddresses: HashMap<Pattern, URI>) : ILookupServices {
    override fun lookup(type: Class<*>): CompletableFuture<URI?> {
        for (p in _serviceAddresses.keys) {
            if (p.matcher(type.name).find()) {
                return CompletableFuture.supplyAsync { _serviceAddresses[p] }
            }
        }
        throw IllegalArgumentException(type.name)
    }
}
