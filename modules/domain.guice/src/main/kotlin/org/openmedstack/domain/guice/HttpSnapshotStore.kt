package org.openmedstack.domain.guice

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.openmedstack.domain.Memento
import org.openmedstack.eventstore.IAccessSnapshots
import org.openmedstack.eventstore.Snapshot
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.util.concurrent.CompletableFuture

class HttpSnapshotStore constructor(mapper: ObjectMapper) : IAccessSnapshots {
    private val _persistence: HttpClient
    private val _mapper: ObjectMapper

    override fun <TMemento: Memento> getSnapshot(type: Class<Snapshot<TMemento>>, bucketId: String?, streamId: String, maxRevision: Int): CompletableFuture<Snapshot<TMemento>?> {
        val request = HttpRequest.newBuilder(URI.create("")).GET().build()
        return _persistence.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApplyAsync { obj: HttpResponse<String?> -> obj.body() }
            .thenApplyAsync { c: String? -> _mapper.readValue(c, type) }
    }

    override fun <TMemento : Memento> addSnapshot(snapshot: Snapshot<TMemento>): CompletableFuture<Boolean> {
        return try {
            val json = _mapper.writeValueAsString(snapshot)
            val request = HttpRequest.newBuilder(URI.create("")).POST(HttpRequest.BodyPublishers.ofString(json)).build()
            _persistence.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApplyAsync { obj: HttpResponse<String?> -> obj.statusCode() }
                    .thenApplyAsync { c: Int -> c < 300 }
        } catch (j: JsonProcessingException) {
            CompletableFuture.completedFuture(false)
        }
    }

    init {
        _mapper = mapper
        _persistence = HttpClient.newHttpClient()
    }
}