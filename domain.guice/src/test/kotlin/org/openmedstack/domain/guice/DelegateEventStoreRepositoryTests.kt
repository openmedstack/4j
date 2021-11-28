package org.openmedstack.domain.guice

import com.fasterxml.jackson.databind.ObjectMapper
import org.openmedstack.eventstore.DelegateAggregateRepository
import org.openmedstack.IProvideTenant
import org.openmedstack.domain.guice.HttpEventStore
import org.openmedstack.domain.guice.HttpSnapshotStore
import org.openmedstack.domain.guice.ContainerAggregateFactory
import com.google.inject.Guice
import org.junit.Assert
import org.junit.Test
import org.openmedstack.eventstore.ConflictDetector

class DelegateEventStoreRepositoryTests {
    @Test
    fun canCreateRepository() {
        val mapper = ObjectMapper()
        val repository = DelegateAggregateRepository(
            object : IProvideTenant {
                override val tenantName: String
                    get() = ""
            },
            HttpEventStore(mapper),
            HttpSnapshotStore(mapper),
            ContainerAggregateFactory(Guice.createInjector())
        )
        Assert.assertNotNull(repository)
    }
}