package org.openmedstack.eventstore

import java.util.concurrent.CompletableFuture

interface ITrackCheckpoints {
    fun getLatest(): CompletableFuture<Long>
    fun setLatest(value: Long): CompletableFuture<Void>
}
