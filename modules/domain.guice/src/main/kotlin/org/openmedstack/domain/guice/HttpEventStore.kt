package org.openmedstack.domain.guice

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.inject.Inject
import org.openmedstack.domain.Memento
import org.openmedstack.eventstore.*
import java.net.*
import java.net.http.*
import java.util.concurrent.CompletableFuture

class HttpEventStore @Inject constructor(mapper: ObjectMapper) : IStoreEvents, ICommitEvents {
    private val _persistence: HttpClient
    private val _mapper: ObjectMapper
    override fun createStream(bucketId: String?, streamId: String): CompletableFuture<IEventStream> {
        return CompletableFuture.completedFuture(OptimisticEventStream.create(bucketId, streamId, this))
    }

    override fun openStream(bucketId: String?, streamId: String, minRevision: Int, maxRevision: Int): CompletableFuture<IEventStream> {
        val mr = if (maxRevision <= 0) Int.MAX_VALUE else maxRevision
        return OptimisticEventStream.create(bucketId, streamId, this, minRevision.coerceAtLeast(0), mr)
    }

    override fun <TMemento : Memento> openStream(snapshot: Snapshot<TMemento>, maxRevision: Int): CompletableFuture<IEventStream> {
        return openStream(snapshot.bucketId, snapshot.streamId, 0, if (maxRevision <= 0) Int.MAX_VALUE else maxRevision)
    }

    @Throws(Exception::class)
    override fun close() {
    }

    override fun getFrom(bucketId: String?, streamId: String, minRevision: Int, maxRevision: Int): CompletableFuture<Iterable<Commit>> {
        val request = HttpRequest.newBuilder(URI.create("")).GET().build()
        return _persistence.sendAsync(request, HttpResponse.BodyHandlers.ofString())
            .thenApplyAsync { obj: HttpResponse<String> -> obj.body() }
            .thenApplyAsync { c: String -> _mapper.readValue(c, Array<Commit>::class.java).asList() }
    }

    override fun commit(attempt: CommitAttempt): CompletableFuture<Commit> {
        return try {
            val json = _mapper.writeValueAsString(attempt)
            val request = HttpRequest.newBuilder(URI.create("")).POST(HttpRequest.BodyPublishers.ofString(json)).build()
            _persistence.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApplyAsync { obj: HttpResponse<String?> -> obj.body() }
                    .thenApplyAsync { c: String? ->
                        try {
                            return@thenApplyAsync _mapper.readValue(c, Commit::class.java)
                        } catch (e: JsonProcessingException) {
                            e.printStackTrace()
                        }
                        null
                    }
        } catch (j: JsonProcessingException) {
            CompletableFuture.completedFuture(null)
        }
    }

    init {
        _mapper = mapper
        _persistence = HttpClient.newHttpClient()
    }
}