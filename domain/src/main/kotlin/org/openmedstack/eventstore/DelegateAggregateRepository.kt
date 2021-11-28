package org.openmedstack.eventstore

import org.openmedstack.IProvideTenant
import org.openmedstack.domain.Aggregate
import org.openmedstack.domain.HandlerForDomainEventNotFoundException
import org.openmedstack.domain.Memento
import java.lang.reflect.ParameterizedType
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer
import kotlin.reflect.KClass

class DelegateAggregateRepository(
        private val _tenantId: IProvideTenant,
        private val _eventStore: IStoreEvents,
        private val _snapshotStore: IAccessSnapshots,
        private val _factory: IConstructAggregates) : Repository {
    override fun <TAggregate : Aggregate, TMemento : Memento> getById(type: Class<TAggregate>, mementoType: Class<Snapshot<TMemento>>, id: String): CompletableFuture<TAggregate> {
        return getById<TAggregate, TMemento>(type, mementoType, id, Int.MAX_VALUE)
    }

    override fun <TAggregate : Aggregate, TMemento : Memento> getById(
        type: Class<TAggregate>,
        mementoType: Class<Snapshot<TMemento>>,
        id: String,
        version: Int
    ): CompletableFuture<TAggregate> {
        return getSnapshot(mementoType,_tenantId.tenantName, id, version)
            .thenComposeAsync { snapshot: Snapshot<TMemento>? ->
                openStream(_tenantId.tenantName, id, version, snapshot)
                    .thenApplyAsync { stream: IEventStream ->
                        val a = getAggregate<TAggregate, TMemento>(type, snapshot, stream)
                        applyEventsToAggregate(version, stream, a)
                        a as TAggregate
                    }
            }
    }

    @Throws(DuplicateCommitException::class)
    override fun <TAggregate : Aggregate, TMemento : Memento> save(
        aggregate: TAggregate,
        updateHeaders: Consumer<HashMap<String, Any>>
    ): CompletableFuture<Boolean> {

        val headers = prepareHeaders(aggregate, updateHeaders)
        return prepareStream<TMemento>(_tenantId.tenantName, aggregate, headers)
            .thenComposeAsync { stream: IEventStream ->
                val commitId = UUID.randomUUID()
                stream.commitChanges(commitId)
            }
            .thenApplyAsync { _ ->
                aggregate.clearUncommittedEvents()
                true
            }
    }

    private fun <TAggregate : Aggregate, TMemento : Memento> getAggregate(
        type: Class<TAggregate>,
        snapshot: Snapshot<TMemento>?,
        stream: IEventStream
    ): Aggregate {
        val snapshot1 = snapshot?.payload as Memento
        return _factory.build(type, stream.streamId, snapshot1)
    }

    private fun <T : Memento> getSnapshot(type: Class<Snapshot<T>>, bucketId: String?, id: String, version: Int): CompletableFuture<Snapshot<T>?> {
        return _snapshotStore.getSnapshot(type, bucketId, id, version)
    }

    private fun <T : Memento> openStream(
        bucketId: String?,
        id: String,
        version: Int,
        snapshot: Snapshot<T>?
    ): CompletableFuture<IEventStream> {
        return if (snapshot == null) _eventStore.openStream(bucketId, id, 0, version) else _eventStore.openStream(
            snapshot,
            version
        )
    }

    private fun <T : Memento> prepareStream(
        bucketId: String?,
        aggregate: Aggregate,
        headers: HashMap<String, Any>
    ): CompletableFuture<IEventStream> {
        return openStream<T>(bucketId, aggregate.id, aggregate.version, null)
            .thenApply { stream: IEventStream ->
                for ((key, value) in headers) {
                    stream.uncommittedHeaders[key] = value
                }
                for (uncommittedEvent in aggregate.uncommittedEvents) {
                    stream.add(EventMessage(uncommittedEvent, headers))
                }
                stream
            }
    }
//
//    private fun throwOnConflict(stream: IEventStream, skip: Int): Boolean {
//        val committedEvents =
//            Arrays.stream(stream.committedEvents.toTypedArray()).skip(skip.toLong()).map { x: EventMessage -> x.body }
//                .toArray()
//        val uncommittedEvents =
//            Arrays.stream(stream.uncommittedEvents.toTypedArray()).map { x: EventMessage -> x.body }.toArray()
//        return _conflictDetector.conflictsWith(uncommittedEvents, committedEvents)
//    }

    companion object {
        private const val AggregateTypeHeader: String = "AggregateType"

        @Throws(HandlerForDomainEventNotFoundException::class)
        private fun applyEventsToAggregate(versionToLoad: Int, stream: IEventStream, aggregate: Aggregate) {
            if (versionToLoad != 0 && aggregate.version >= versionToLoad) {
                return
            }
            Arrays.stream(stream.committedEvents.toTypedArray()).map { x: EventMessage -> x.body }
                .forEachOrdered { x: Any ->
                    try {
                        aggregate.applyEvent(x)
                    } catch (e: HandlerForDomainEventNotFoundException) {
                        throw RuntimeException(e)
                    }
                }
        }

        private fun prepareHeaders(
            aggregate: Aggregate,
            updateHeaders: Consumer<HashMap<String, Any>>
        ): HashMap<String, Any> {
            val dictionary = HashMap<String, Any>()
            dictionary[AggregateTypeHeader] = aggregate.javaClass.typeName
            updateHeaders.accept(dictionary)
            return dictionary
        }
    }
}
