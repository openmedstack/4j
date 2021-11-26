package org.openmedstack;

import java.util.concurrent.CompletableFuture;

public interface IPersist {
    <T extends Object> CompletableFuture<Boolean> save(T item);
}

