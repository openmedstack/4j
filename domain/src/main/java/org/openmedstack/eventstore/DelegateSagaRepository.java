package org.openmedstack.eventstore;

import openmedstack.IProvideTenant;
import org.openmedstack.domain.HandlerForDomainEventNotFoundException;
import org.openmedstack.domain.Saga;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class DelegateSagaRepository implements SagaRepository {
    private final IProvideTenant _tenantId;
    private final IStoreEvents _eventStore;
    private final IConstructSagas _factory;

    public DelegateSagaRepository(IProvideTenant tenantId, IStoreEvents eventStore, IConstructSagas factory)
    {
        _tenantId = tenantId;
        _eventStore = eventStore;
        _factory = factory;
    }

    @Override
    public <TSaga extends Saga> CompletableFuture<TSaga> getById(Class<TSaga> type,String sagaId) {
        return openStream(_tenantId.getTenantName(), sagaId, Integer.MAX_VALUE).thenApplyAsync(stream -> {
            try {
                return buildSaga(type, sagaId, stream);
            } catch (HandlerForDomainEventNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> save(Saga saga, Consumer<HashMap<String, Object>> updateHeaders) {
        if (saga == null) {
            throw new IllegalArgumentException("Saga cannot be null");
        }
        var commitId = UUID.randomUUID();
        var headers = PrepareHeaders(saga, updateHeaders);
        return prepareStream(_tenantId.getTenantName(), saga, headers).thenApplyAsync(eventStream -> {
            try {
                persist(eventStream, commitId);
                saga.clearUncommittedEvents();
                saga.clearUndispatchedMessages();
                return true;
            } catch (ConcurrencyException e) {
                return false;
            }
        });
    }

    private CompletableFuture<IEventStream> openStream(String bucketId, String sagaId, int maxVersion)
    {
        CompletableFuture<IEventStream> eventStream;
        try
        {
            eventStream = _eventStore.openStream(bucketId, sagaId, 0, maxVersion);
        }
        catch (StreamNotFoundException)
        {
            eventStream =  _eventStore.createStream(bucketId, sagaId);
        }
        return eventStream;
    }

    private <TSaga extends Saga> TSaga buildSaga(Class<TSaga> type, String sagaId, IEventStream stream) throws HandlerForDomainEventNotFoundException
    {
        var saga = (TSaga)_factory.build(type, sagaId);
        for (var x : stream.getCommittedEvents()){
            saga.transition(x.getBody());
    }

        saga.clearUncommittedEvents();
        saga.clearUndispatchedMessages();
        return saga;
    }

    private static HashMap<String, Object> PrepareHeaders(Saga saga, Consumer<HashMap<String, Object>> updateHeaders)
    {
        var dictionary = new HashMap<String, Object>();
        dictionary.put("SagaType", saga.getClass().getTypeName());
    if(updateHeaders!= null){
        updateHeaders.accept(dictionary);
    }
        int num = 0;
        for (var undispatchedMessage : saga.getUndispatchedMessages())
        {
            dictionary.put("UndispatchedMessage." + num++, undispatchedMessage);
        }
        return dictionary;
    }

    private CompletableFuture<IEventStream> prepareStream(String bucketId, Saga saga, HashMap<String, Object> headers) {
        return openStream(bucketId, saga.getId(), saga.getVersion())
                .thenApply(stream -> {

                    for (var header : headers.entrySet()) {
                        stream.getUncommittedHeaders().put(header.getKey(), header.getValue());
                    }

                    saga.getUncommittedEvents().forEach(o -> {
                        stream.add(new EventMessage(o, null));
                    });

                    return stream;
                });
    }

    private CompletableFuture persist(IEventStream stream, UUID commitId) throws ConcurrencyException
    {
        try
        {
            return stream.commitChanges(commitId);
        }
        catch (DuplicateCommitException ex)
        {
            stream.clearChanges();
        }
        return CompletableFuture.completedFuture(true);
    }
}
