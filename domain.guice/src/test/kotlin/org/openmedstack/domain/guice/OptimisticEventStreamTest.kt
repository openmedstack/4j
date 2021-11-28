package org.openmedstack.domain.guice

import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.openmedstack.domain.guice.OptimisticEventStream.Companion.create
import org.openmedstack.eventstore.Commit
import org.openmedstack.eventstore.CommitAttempt
import org.openmedstack.eventstore.CommitImpl
import org.openmedstack.eventstore.ICommitEvents
import java.util.concurrent.CompletableFuture

class OptimisticEventStreamTest {
    @Before
    @Throws(Exception::class)
    fun setUp() {
    }

    @After
    @Throws(Exception::class)
    fun tearDown() {
    }

    @Test
    fun canCreateInstance() {
        val instance = create("bucket", "stream", object : ICommitEvents {
            override fun getFrom(
                bucketId: String?,
                streamId: String,
                minRevision: Int,
                maxRevision: Int
            ): CompletableFuture<Iterable<Commit>> {
                return CompletableFuture.completedFuture(listOf<Commit>())
            }

            override fun commit(attempt: CommitAttempt): CompletableFuture<Commit> {
                return CompletableFuture.completedFuture(
                    CommitImpl.create(attempt)
                )
            }
        })
        Assert.assertNotNull(instance)
    }

    @Test
    fun canCreateFutureInstance() {
        val future = create("bucket", "stream", object : ICommitEvents {
            override fun getFrom(
                bucketId: String?,
                streamId: String,
                minRevision: Int,
                maxRevision: Int
            ): CompletableFuture<Iterable<Commit>> {
                return CompletableFuture.completedFuture(listOf())
            }

            override fun commit(attempt: CommitAttempt): CompletableFuture<Commit> {
                return CompletableFuture.completedFuture(CommitImpl.create(attempt))
            }
        }, 0, Int.MAX_VALUE)
        try {
            val instance = future.get()
            Assert.assertNotNull(instance)
        } catch (e: Exception) {
            Assert.fail()
        }
    }
}
