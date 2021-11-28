package org.openmedstack.eventstore

import org.openmedstack.IProvideTenant
import org.openmedstack.domain.HandlerForDomainEventNotFoundException
import org.openmedstack.domain.Saga
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer
import kotlin.collections.HashMap

class DelegateSagaRepository(private val _tenantId: IProvideTenant, private val _eventStore: IStoreEvents, private val _factory: IConstructSagas) : SagaRepository {
    override fun <TSaga : Saga> getById(type: Class<TSaga>, sagaId: String): CompletableFuture<TSaga> {
        return openStream(_tenantId.tenantName, sagaId, Int.MAX_VALUE).thenApplyAsync { stream: IEventStream ->
            try {
                return@thenApplyAsync buildSaga(type, sagaId, stream)
            } catch (e: HandlerForDomainEventNotFoundException) {
                throw RuntimeException(e)
            }
        }
    }

    override fun save(saga: Saga, updateHeaders: Consumer<HashMap<String, Any>>): CompletableFuture<Boolean> {
        val commitId = UUID.randomUUID()
        val headers = prepareHeaders(saga, updateHeaders)
        return prepareStream(_tenantId.tenantName, saga, headers).thenApplyAsync { eventStream: IEventStream ->
            try {
                persist(eventStream, commitId)
                saga.clearUncommittedEvents()
                saga.clearUndispatchedMessages()
                return@thenApplyAsync true
            } catch (e: ConcurrencyException) {
                return@thenApplyAsync false
            }
        }
    }

    private fun openStream(bucketId: String?, sagaId: String, maxVersion: Int): CompletableFuture<IEventStream> {
        return _eventStore.openStream(bucketId, sagaId, 0, maxVersion)
    }

    @Throws(HandlerForDomainEventNotFoundException::class)
    private fun <TSaga : Saga> buildSaga(type: Class<TSaga>, sagaId: String, stream: IEventStream): TSaga {
        val saga: TSaga = _factory.build(type, sagaId)
        for (x in stream.committedEvents) {
            saga.transition(x.body)
        }
        saga.clearUncommittedEvents()
        saga.clearUndispatchedMessages()
        return saga
    }

    private fun prepareStream(bucketId: String?, saga: Saga, headers: HashMap<String, Any>): CompletableFuture<IEventStream> {
        return openStream(bucketId, saga.id, saga.version)
                .thenApplyAsync { stream: IEventStream ->
                    for ((key, value) in headers) {
                        stream.uncommittedHeaders[key] = value
                    }
                    saga.uncommittedEvents.forEach(Consumer { o: Any -> stream.add(EventMessage(o, HashMap())) })
                    stream
                }
    }

    @Throws(ConcurrencyException::class)
    private fun persist(stream: IEventStream, commitId: UUID): CompletableFuture<*>? {
        try {
            return stream.commitChanges(commitId)
        } catch (ex: DuplicateCommitException) {
            stream.clearChanges()
        }
        return CompletableFuture.completedFuture(true)
    }

    companion object {
        private fun prepareHeaders(saga: Saga, updateHeaders: Consumer<HashMap<String, Any>>): HashMap<String, Any> {
            val dictionary = HashMap<String, Any>()
            dictionary["SagaType"] = saga.javaClass.typeName
            updateHeaders.accept(dictionary)
            var num = 0
            for (undispatchedMessage in saga.undispatchedMessages) {
                dictionary["UndispatchedMessage." + num++] = undispatchedMessage
            }
            return dictionary
        }
    }
}