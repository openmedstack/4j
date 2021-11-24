package org.openmedstack.eventstore;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class CommitAttempt {
    private String _bucketId;
    private String _streamId;
    private int _streamRevision;
    private UUID _commitId;
    private int _commitSequence;
    private Instant _commitStamp;
    private HashMap<String, Object> _headers;
    private List<EventMessage> _events;

    public CommitAttempt(
            String bucketId,
            String streamId,
            int streamRevision,
            UUID commitId,
            int commitSequence,
            Instant commitStamp,
            HashMap<String, Object> headers,
            List<EventMessage> events) {
        _bucketId = bucketId;
        _streamId = streamId;
        _streamRevision = streamRevision;
        _commitId = commitId;
        _commitSequence = commitSequence;
        _commitStamp = commitStamp;
        _headers = headers == null ? new HashMap<>() : headers;
        _events = events == null ?
                new ArrayList<>() :
                events;
    }

    public String getBucketId() {
        return _bucketId;
    }

    public String getStreamId() {
        return _streamId;
    }

    public int getStreamRevision() {
        return _streamRevision;
    }

    public UUID getCommitId() {
        return _commitId;
    }

    public int getCommitSequence() {
        return _commitSequence;
    }

    public Instant getCommitStamp() {
        return _commitStamp;
    }

    public HashMap<String, Object> getHeaders() {
        return _headers;
    }

    public List<EventMessage> getEvents() {
        return _events;
    }
}
