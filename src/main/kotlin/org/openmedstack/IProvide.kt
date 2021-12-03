package org.openmedstack

import java.util.concurrent.CompletableFuture

interface IProvide<TKey, TItem> {
    fun fetch(key: TKey): CompletableFuture<TItem?>
}

