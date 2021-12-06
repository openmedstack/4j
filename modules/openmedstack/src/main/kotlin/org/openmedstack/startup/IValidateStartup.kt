package org.openmedstack.startup

import java.util.concurrent.CompletableFuture
import java.lang.Exception

interface IValidateStartup {
    fun validate(): CompletableFuture<Exception?>?
}