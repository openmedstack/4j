package org.openmedstack;

import java.util.concurrent.CompletableFuture;

public interface IFactory<T extends Object, TParam extends Object> {
    CompletableFuture<T> create(TParam param);
}
