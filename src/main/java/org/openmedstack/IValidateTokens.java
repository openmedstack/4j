package org.openmedstack;

import java.util.concurrent.CompletableFuture;

public interface IValidateTokens {
    CompletableFuture<String> validate(String token);
}

