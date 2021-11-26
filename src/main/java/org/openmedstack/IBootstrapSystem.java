package org.openmedstack;

import java.util.concurrent.CompletableFuture;

public interface IBootstrapSystem {
    CompletableFuture<Boolean> start();

    CompletableFuture stop();
}
