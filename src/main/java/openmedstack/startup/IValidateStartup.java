package openmedstack.startup;

import java.util.concurrent.CompletableFuture;

public interface IValidateStartup {
    CompletableFuture<Exception> validate();
}
