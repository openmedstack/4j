package org.openmedstack

import java.net.URI
import java.util.concurrent.CompletableFuture
import java.util.regex.Pattern

class FixedServicesLookup(private val _serviceAddresses: Map<Pattern, URI>) : ILookupServices {
    override fun <T> lookup(type: Class<T>): CompletableFuture<URI?> where T: Any {
        for (p in _serviceAddresses.keys) {
            if (p.matcher(type.name).find()) {
                return CompletableFuture.supplyAsync { _serviceAddresses[p] }
            }
        }
        return CompletableFuture.completedFuture(null)
    }
}
