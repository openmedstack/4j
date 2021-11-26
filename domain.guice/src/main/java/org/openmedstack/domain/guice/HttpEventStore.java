package org.openmedstack.domain.guice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openmedstack.eventstore.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class HttpEventStore implements IStoreEvents, ICommitEvents {
    private final HttpClient _persistence;
    private final ObjectMapper _mapper;
    private final URI _baseAddress;

    public HttpEventStore(ObjectMapper mapper, URI baseAddress) {
        _baseAddress = baseAddress;
        _persistence = HttpClient.newHttpClient();
        _mapper = mapper;
    }

    @Override
    public CompletableFuture<IEventStream> createStream(String bucketId, String streamId) {
        return CompletableFuture.completedFuture(OptimisticEventStream.Create(bucketId, streamId, this));
    }

    @Override
    public CompletableFuture<IEventStream> openStream(String bucketId, String streamId, int minRevision, int maxRevision) {
        if (streamId == null) {
            throw new NullPointerException();
        }
        maxRevision = maxRevision <= 0 ? Integer.MAX_VALUE : maxRevision;

        return OptimisticEventStream.Create(bucketId, streamId, this, minRevision, maxRevision);
    }

    @Override
    public CompletableFuture<IEventStream> openStream(Snapshot snapshot, int maxRevision) {
        return openStream(snapshot.getBucketId(), snapshot.getStreamId(), 0, maxRevision <= 0 ? Integer.MAX_VALUE : maxRevision);
    }

    @Override
    public void close() throws Exception {
    }

    @Override
    public CompletableFuture<Iterable<Commit>> getFrom(String bucketId, String streamId, int minRevision, int maxRevision) {
        var request = HttpRequest.newBuilder(
                        URI.create(String.format("/%s/%s/%d/%d", bucketId, streamId, minRevision, maxRevision))
                                .relativize(_baseAddress))
                .GET()
                .build();
        return _persistence.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApplyAsync(HttpResponse::body)
                .thenApplyAsync(c -> {
                    try {
                        return _mapper.readValue(c, Commit[].class);
                    } catch (JsonProcessingException e) {
                    }
                    return new Commit[0];
                }).thenApplyAsync(c -> List.of(c));
    }

    @Override
    public CompletableFuture<Commit> commit(CommitAttempt attempt) {
        try{
            var json = _mapper.writeValueAsString(attempt);
        var request = HttpRequest.newBuilder(_baseAddress).POST(HttpRequest.BodyPublishers.ofString(json)).build();
        return _persistence.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApplyAsync(HttpResponse::body)
                .thenApplyAsync(c -> {
                    try {
                        return _mapper.readValue(c, Commit.class);
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    return null;
                });}
        catch (JsonProcessingException j){
            return CompletableFuture.completedFuture(null);
        }
    }
}
