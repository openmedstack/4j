package org.openmedstack.eventstore

import org.openmedstack.domain.Aggregate
import org.openmedstack.domain.Memento

interface IConstructAggregates {
    fun <T : Aggregate> build(type: Class<T>, id: String, snapshot: Memento?): T
}
