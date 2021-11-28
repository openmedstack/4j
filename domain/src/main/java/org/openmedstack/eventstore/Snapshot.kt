package org.openmedstack.eventstore

import org.openmedstack.domain.Memento

class Snapshot<T : Memento> constructor(private val _bucketId: String?, private val _streamId: String, private val _streamRevision: Int, private val _payload: T) {
    val bucketId: String?
        get() = _bucketId
    val streamId: String
        get() = _streamId
    val streamRevision: Int
        get() = _streamRevision
    val payload: T
        get() = _payload
}
