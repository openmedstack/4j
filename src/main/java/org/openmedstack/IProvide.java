package org.openmedstack;

import java.util.concurrent.CompletableFuture;

public interface IProvide<TKey, TItem> {
    CompletableFuture<TItem> Fetch(TKey key);
}
