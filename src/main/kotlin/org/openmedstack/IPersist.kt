package org.openmedstack

import java.util.concurrent.CompletableFuture

interface IPersist {
    fun <T : Any?> save(item: T): CompletableFuture<Boolean?>?
}

