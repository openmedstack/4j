package org.openmedstack.domain.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import org.openmedstack.IProvideTenant;
import org.openmedstack.domain.Memento;
import org.openmedstack.domain.Saga;
import org.openmedstack.eventstore.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class EventStoreModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(IConstructAggregates.class).to(ContainerAggregateFactory.class).asEagerSingleton();
        bind(IConstructSagas.class).to(ContainerSagaFactory.class).asEagerSingleton();
        bind(IStoreEvents.class).to(HttpEventStore.class).asEagerSingleton();
        bind(IAccessSnapshots.class).to(HttpSnapshotStore.class).asEagerSingleton();
        bind(Repository.class).toConstructor((Constructor<DelegateAggregateRepository>)DelegateAggregateRepository.class.getConstructors()[0]).asEagerSingleton();
        bind(SagaRepository.class).toConstructor((Constructor<DelegateSagaRepository>)DelegateSagaRepository.class.getConstructors()[0]).asEagerSingleton();
        bind(IProvideTenant.class).toInstance(new IProvideTenant() {
            @Override
            public String getTenantName() {
                return "test";
            }
        });
        bind(IDetectConflicts.class).to(ConflictDetector.class);
    }
}

