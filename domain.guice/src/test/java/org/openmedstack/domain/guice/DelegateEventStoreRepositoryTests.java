package org.openmedstack.domain.guice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;
import org.openmedstack.IProvideTenant;
import org.junit.Assert;
import org.junit.Test;
import org.openmedstack.eventstore.ConflictDetector;
import org.openmedstack.eventstore.DelegateAggregateRepository;

public class DelegateEventStoreRepositoryTests {
    @Test
    public void canCreateRepository() {
        var mapper = new ObjectMapper();
        var repository = new DelegateAggregateRepository(new IProvideTenant() {
            @Override
            public String getTenantName() {
                return "";
            }
        },
                new HttpEventStore(mapper),
                new HttpSnapshotStore(mapper),
                new ContainerAggregateFactory(Guice.createInjector()),
                new ConflictDetector());

        Assert.assertNotNull(repository);
    }
}
