package org.openmedstack.eventstore

import org.openmedstack.domain.Saga

interface IConstructSagas {
    fun <T : Saga> build(type: Class<T>, id: String): T
}
