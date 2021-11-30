package org.openmedstack.domain.guice

import com.google.inject.AbstractModule
import com.google.inject.Scope
import com.google.inject.multibindings.Multibinder
import org.openmedstack.IProvideTenant
import org.openmedstack.MessageHeaders
import org.openmedstack.commands.CommandResponse
import org.openmedstack.commands.DomainCommand
import org.openmedstack.commands.IHandleCommands
import org.openmedstack.eventstore.*
import java.lang.reflect.Constructor
import java.util.concurrent.CompletableFuture

class EventStoreModule : AbstractModule() {
    override fun configure() {
        bind(IConstructAggregates::class.java).to(ContainerAggregateFactory::class.java)
        bind(IConstructSagas::class.java).to(ContainerSagaFactory::class.java)
        bind(IStoreEvents::class.java).to(HttpEventStore::class.java)
        bind(IAccessSnapshots::class.java).to(HttpSnapshotStore::class.java)
        bind(Repository::class.java).toConstructor(
            DelegateAggregateRepository::class.java.getConstructor(
                IStoreEvents::class.java,
                IAccessSnapshots::class.java,
                IConstructAggregates::class.java
            )
        )
        bind(SagaRepository::class.java).toConstructor(
            DelegateSagaRepository::class.java.getConstructor(
                IProvideTenant::class.java,
                IStoreEvents::class.java,
                IConstructSagas::class.java
            )
        )
        bind(IProvideTenant::class.java).toInstance(object : IProvideTenant {
            override val tenantName: String
                get() = "test"
        })
        bind(IDetectConflicts::class.java).to(ConflictDetector::class.java)
    }
}
