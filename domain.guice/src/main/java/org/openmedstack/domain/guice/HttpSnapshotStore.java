package org.openmedstack.domain.guice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openmedstack.eventstore.IAccessSnapshots;
import org.openmedstack.eventstore.Snapshot;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class HttpSnapshotStore implements IAccessSnapshots {
    private final ObjectMapper _mapper;
    private final HttpClient _persistence;

    public HttpSnapshotStore(ObjectMapper mapper) {
        _mapper = mapper;
        _persistence = HttpClient.newHttpClient();
    }

    @Override
    public CompletableFuture<Snapshot> GetSnapshot(String bucketId, String streamId, int maxRevision) {
        var request = HttpRequest.newBuilder(URI.create("")).GET().build();
        return _persistence.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApplyAsync(HttpResponse::body)
                .thenApplyAsync(c -> {
                    try {
                        return _mapper.readValue(c, Snapshot.class);
                    } catch (JsonProcessingException e) {
                    }
                    return null;
                });
    }

    @Override
    public CompletableFuture<Boolean> AddSnapshot(Snapshot snapshot) {
        try {
            var json = _mapper.writeValueAsString(snapshot);
            var request = HttpRequest.newBuilder(URI.create("")).POST(HttpRequest.BodyPublishers.ofString(json)).build();
            return _persistence.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApplyAsync(HttpResponse::statusCode)
                    .thenApplyAsync(c -> c < 300);
        } catch (JsonProcessingException j) {
            return CompletableFuture.completedFuture(false);
        }
    }
}
