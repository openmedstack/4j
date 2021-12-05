package org.openmedstack

import java.util.concurrent.CompletableFuture

interface IPersist {
    fun save(item: Any): CompletableFuture<Boolean>
}
