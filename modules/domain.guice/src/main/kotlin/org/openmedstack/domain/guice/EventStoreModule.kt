package org.openmedstack.domain.guice

import com.google.inject.AbstractModule
import org.openmedstack.IProvideTenant
import org.openmedstack.eventstore.*
import java.lang.reflect.Constructor
import java.net.http.HttpClient

class EventStoreModule : AbstractModule() {
    override fun configure() {
        bind(HttpClient::class.java).toInstance(
            HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build()
        )
        bind(IConstructAggregates::class.java).to(ContainerAggregateFactory::class.java)
        bind(IConstructSagas::class.java).to(ContainerSagaFactory::class.java)
        bind(IStoreEvents::class.java).to(HttpEventStore::class.java)
        bind(IAccessSnapshots::class.java).toConstructor(HttpSnapshotStore::class.java.declaredConstructors[0] as Constructor<HttpSnapshotStore>)
        bind(Repository::class.java).toConstructor(
            DelegateAggregateRepository::class.java.getConstructor(
                IProvideTenant::class.java,
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
        bind(IDetectConflicts::class.java).to(ConflictDetector::class.java)
    }
}
