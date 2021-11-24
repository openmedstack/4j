package openmedstack;

import java.util.concurrent.CompletableFuture;

public class DefaultTokenValidator implements IValidateTokens {

    @Override
    public CompletableFuture<String> validate(String token) {
        return CompletableFuture.supplyAsync(() -> null);
    }
}
