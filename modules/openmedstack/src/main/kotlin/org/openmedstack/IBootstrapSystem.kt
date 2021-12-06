package org.openmedstack

import java.util.concurrent.CompletableFuture

interface IBootstrapSystem {
    fun start(): CompletableFuture<Boolean>
    fun stop(): CompletableFuture<*>
}
