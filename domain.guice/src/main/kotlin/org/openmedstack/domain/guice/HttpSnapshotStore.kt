package org.openmedstack.domain.guice

import com.fasterxml.jackson.databind.ObjectMapper
import java.util.concurrent.CompletableFuture
import org.openmedstack.domain.guice.OptimisticEventStream
import java.lang.NullPointerException
import kotlin.Throws
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.type.TypeReference
import com.google.inject.AbstractModule
import com.google.inject.Inject
import org.openmedstack.domain.guice.ContainerAggregateFactory
import org.openmedstack.domain.guice.ContainerSagaFactory
import org.openmedstack.domain.guice.HttpEventStore
import org.openmedstack.domain.guice.HttpSnapshotStore
import org.openmedstack.IProvideTenant
import com.google.inject.Injector
import org.openmedstack.domain.Saga
import java.util.Arrays
import java.lang.InstantiationException
import java.lang.RuntimeException
import java.lang.IllegalAccessException
import java.lang.reflect.InvocationTargetException
import java.util.HashSet
import java.util.UUID
import java.util.HashMap
import java.lang.IllegalArgumentException
import java.lang.Void
import java.time.Instant
import org.openmedstack.domain.Memento
import org.openmedstack.eventstore.*
import java.net.URI
import java.net.http.HttpClient

class HttpSnapshotStore @Inject constructor(private val _mapper: ObjectMapper) : IAccessSnapshots {
    private val _persistence: HttpClient = HttpClient.newHttpClient()
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

}