package org.openmedstack.domain.guice

import com.fasterxml.jackson.databind.ObjectMapper
import org.openmedstack.eventstore.DelegateAggregateRepository
import org.openmedstack.IProvideTenant
import com.google.inject.Guice
import org.junit.Assert
import org.junit.Test

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