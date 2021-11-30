package org.openmedstack

import java.util.concurrent.CompletableFuture

interface IFactory<T : Any?, TParam : Any?> {
    fun create(param: TParam): CompletableFuture<T>?
}

