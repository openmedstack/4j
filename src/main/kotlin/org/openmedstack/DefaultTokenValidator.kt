package org.openmedstack

import java.util.concurrent.CompletableFuture

class DefaultTokenValidator : IValidateTokens {
    override fun validate(token: String?): CompletableFuture<String> {
        return CompletableFuture.completedFuture("")
    }
}
