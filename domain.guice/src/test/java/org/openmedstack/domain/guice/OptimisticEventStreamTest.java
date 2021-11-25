package org.openmedstack.domain.guice;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmedstack.eventstore.CommitAttempt;
import org.openmedstack.eventstore.Commit;
import org.openmedstack.eventstore.ICommitEvents;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class OptimisticEventStreamTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void canCreateInstance() {
        var instance = OptimisticEventStream.Create("bucket", "stream", new ICommitEvents() {
            @Override
            public CompletableFuture<Iterable<Commit>> getFrom(String bucketId, String streamId, int minRevision, int maxRevision) {
                return null;
            }

            @Override
            public CompletableFuture<Commit> commit(CommitAttempt attempt) {
                return null;
            }
        });
        Assert.assertNotNull(instance);
    }

    @Test
    public void canCreateFutureInstance() {
        var future = OptimisticEventStream.Create("bucket", "stream", new ICommitEvents() {
            @Override
            public CompletableFuture<Iterable<Commit>> getFrom(String bucketId, String streamId, int minRevision, int maxRevision) {
                return CompletableFuture.completedFuture(List.of(new Commit[0]));
            }

            @Override
            public CompletableFuture<Commit> commit(CommitAttempt attempt) {
                return null;
            }
        }, 0, Integer.MAX_VALUE);
        try {
            var instance = future.get();

            Assert.assertNotNull(instance);
        } catch (Exception e) {
            Assert.fail();
        }
    }

    public void testTestCreate1() {
    }

    public void testBucketId() {
    }

    public void testGetStreamId() {
    }

    public void testGetStreamRevision() {
    }

    public void testGetCommitSequence() {
    }

    public void testGetCommittedEvents() {
    }

    public void testGetCommittedHeaders() {
    }

    public void testGetUncommittedEvents() {
    }

    public void testGetUncommittedHeaders() {
    }

    public void testAdd() {
    }

    public void testCommitChanges() {
    }

    public void testClearChanges() {
    }

    public void testClose() {
    }
}