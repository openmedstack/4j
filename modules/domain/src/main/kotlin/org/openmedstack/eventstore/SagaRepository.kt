package org.openmedstack.eventstore

import org.openmedstack.domain.Saga
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

interface SagaRepository {
    fun <TSaga : Saga> getById(type: Class<TSaga>, sagaId: String): CompletableFuture<TSaga>
    fun save(saga: Saga, updateHeaders: Consumer<HashMap<String, Any>>): CompletableFuture<Boolean>
}
