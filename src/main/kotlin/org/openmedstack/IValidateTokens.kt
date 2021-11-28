package org.openmedstack

import java.util.concurrent.CompletableFuture

interface IValidateTokens {
    fun validate(token: String?): CompletableFuture<String?>
}