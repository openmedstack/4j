package org.openmedstack.events

import java.time.OffsetDateTime

abstract class DomainEvent protected constructor(
    private val _source: String,
    val version: Int,
    private val _timeStamp: OffsetDateTime,
    private val _correlationId: String? = null
) : BaseEvent(_source, _timeStamp, _correlationId)