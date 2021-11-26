package org.openmedstack.eventstore;

import org.openmedstack.IProvideTenant;
import org.openmedstack.domain.Aggregate;
import org.openmedstack.domain.HandlerForDomainEventNotFoundException;
import org.openmedstack.domain.Memento;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class DelegateAggregateRepository implements Repository {
    private static final String AggregateTypeHeader = "AggregateType";
    private final IProvideTenant _tenantId;
    private final IDetectConflicts _conflictDetector;
    private final IStoreEvents _eventStore;
    private final IAccessSnapshots _snapshotStore;
    private final IConstructAggregates _factory;

    public DelegateAggregateRepository(
            IProvideTenant tenantId,
            IStoreEvents eventStore,
            IAccessSnapshots snapshotStore,
            IConstructAggregates factory,
            IDetectConflicts conflictDetector) {
        _tenantId = tenantId;
        _eventStore = eventStore;
        _snapshotStore = snapshotStore;
        _factory = factory;
        _conflictDetector = conflictDetector;
    }

    public <TAggregate extends Aggregate> CompletableFuture<TAggregate> getById(Class<TAggregate> type, String id) {
        return getById(type, id, Integer.MAX_VALUE);
    }

    public <TAggregate extends Aggregate> CompletableFuture<TAggregate> getById(Class<TAggregate> type, String id, int version) {
        return getSnapshot(_tenantId.getTenantName(), id, version)
                .thenApplyAsync(snapshot -> {
                    try {
                        var stream = openStream(_tenantId.getTenantName(), id, version, snapshot).get();
                        var a =(TAggregate) getAggregate(type, snapshot, stream);
                        applyEventsToAggregate(version, stream, a);
                        return a;
                    } catch (HandlerForDomainEventNotFoundException | ExecutionException|InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    public CompletableFuture save(
            Aggregate aggregate,
            Consumer<HashMap<String, Object>> updateHeaders) throws DuplicateCommitException {
        var commitId = UUID.randomUUID();
        var headers = prepareHeaders(aggregate, updateHeaders == null ? (d -> {
        }) : updateHeaders);

        return prepareStream(_tenantId.getTenantName(), aggregate, headers).thenComposeAsync(
                stream -> {
                    var count = stream.getCommittedEvents().size();
                    try {
                        return stream.commitChanges(commitId);

                        //_logger.LogDebug($"Saved aggregate of type {aggregate.GetType()} with id {aggregate.Id} at version {aggregate.Version}");

                    } catch (DuplicateCommitException ex) {
                        stream.clearChanges();
                    } catch (ConcurrencyException ex) {
                        //_logger.LogError(ex, ex.Message);
                        var flag = throwOnConflict(stream, count);
                        stream.clearChanges();
                        if (flag) {
                            throw new RuntimeException(new ConflictingCommandException(ex.getMessage(), ex));
                        }
//                } catch (StorageException ex) {
//                        //_logger.LogError(ex, ex.Message);
//                        throw new PersistenceException(ex.Message, ex);
                    } catch (Exception ex) {
                        //_logger.LogError(ex, ex.Message);
                        throw new RuntimeException(ex);
                    }
                    return null;
                })
                .thenAcceptAsync(f -> aggregate.clearUncommittedEvents());
    }

    private void commitStreamChanges(IEventStream stream, UUID commitId) throws DuplicateCommitException, ConcurrencyException{
        stream.commitChanges(commitId);
    }

    private static void applyEventsToAggregate(int versionToLoad, IEventStream stream, Aggregate aggregate) throws HandlerForDomainEventNotFoundException {
        if (versionToLoad != 0 && aggregate.getVersion() >= versionToLoad) {
            return;
        }

        Arrays.stream(stream.getCommittedEvents().toArray(EventMessage[]::new)).map(x -> x.getBody()).forEachOrdered(x -> {
            try {
                aggregate.applyEvent(x);
            } catch (HandlerForDomainEventNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private <TAggregate extends Aggregate> Aggregate getAggregate(Class<TAggregate> type, Snapshot snapshot, IEventStream stream) {
        var snapshot1 = (Memento) snapshot.getPayload();
        return _factory.build(type, stream.getStreamId(), snapshot1);
    }

    private CompletableFuture<Snapshot> getSnapshot(String bucketId, String id, int version) {
        return _snapshotStore.GetSnapshot(bucketId, id, version);
    }

    private CompletableFuture<IEventStream> openStream(String bucketId, String id, int version, Snapshot snapshot) {
        var eventStream = snapshot == null
                ? _eventStore.openStream(bucketId, id, 0, version)
                : _eventStore.openStream(snapshot, version);
        return eventStream;
    }

    private CompletableFuture<IEventStream> prepareStream(
            String bucketId,
            Aggregate aggregate,
            HashMap<String, Object> headers) {
        return openStream(bucketId, aggregate.getId(), aggregate.getVersion(), null)
                .thenApply(stream -> {
                    for (var entry : headers.entrySet()) {
                        stream.getUncommittedHeaders().put(entry.getKey(), entry.getValue());
                    }

                    for (var uncommittedEvent : aggregate.getUncommittedEvents()) {
                        stream.add(new EventMessage(uncommittedEvent, null));
                    }

                    return stream;
                });
    }

    private static HashMap<String, Object> prepareHeaders(
            Aggregate aggregate,
            Consumer<HashMap<String, Object>> updateHeaders) {
        var dictionary = new HashMap<String, Object>();
        dictionary.put(AggregateTypeHeader, aggregate.getClass().getTypeName());

        updateHeaders.accept(dictionary);
        return dictionary;
    }

    private Boolean throwOnConflict(IEventStream stream, int skip) {
        var committedEvents = Arrays.stream(stream.getCommittedEvents().toArray(EventMessage[]::new)).skip(skip).map(x -> x.getBody()).toArray();
        var uncommittedEvents = Arrays.stream(stream.getUncommittedEvents().toArray(EventMessage[]::new)).map(x -> x.getBody()).toArray();
        return _conflictDetector.conflictsWith(uncommittedEvents, committedEvents);
    }
}

