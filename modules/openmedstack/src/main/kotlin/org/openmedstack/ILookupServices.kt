package org.openmedstack

import java.net.URI
import java.util.concurrent.CompletableFuture

interface ILookupServices {
    fun <T> lookup(type: Class<T>): CompletableFuture<URI?> where T : Any
}
