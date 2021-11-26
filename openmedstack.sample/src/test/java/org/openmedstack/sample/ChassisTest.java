package org.openmedstack.sample;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openmedstack.Chassis;
import org.openmedstack.DeploymentConfiguration;
import org.openmedstack.Service;
import org.openmedstack.commands.CommandResponse;
import org.openmedstack.commands.DomainCommand;
import org.openmedstack.commands.IRouteCommands;
import org.openmedstack.domain.guice.EventStoreModule;
import org.openmedstack.events.BaseEvent;
import org.openmedstack.events.IPublishEvents;
import org.openmedstack.messaging.rabbitmq.guice.*;
import java.net.URI;
import java.util.concurrent.CompletableFuture;

public class ChassisTest {
    Chassis chassis;

    @Before
    public void beforeTest() {
        var config = new DeploymentConfiguration();
        config.setServiceBus(URI.create("amqp://localhost"));
        chassis = Chassis.from(config);
    }

    @Test
    public void getConfiguration() {
        Assert.assertNotNull(chassis.getConfiguration());
    }

    @Test
    public void from() {
        Assert.assertNotNull(chassis);
    }

    @Test
    public void start() {
        chassis.usingCustomBuilder(this::getService);
        chassis.start();
    }

    @Test
    public void send() {
        chassis.usingCustomBuilder(this::getService);
        chassis.start();
        var s = chassis.send(new TestCommand("test"));

        Assert.assertTrue(s instanceof CompletableFuture);
    }

    @Test
    public void publish() {
    }

    @Test
    public void resolve() {
        chassis.usingCustomBuilder(this::getService);
        chassis.start();
        String s = chassis.resolve(String.class);

        Assert.assertTrue(s instanceof String);
    }

    @Test
    public void close() {
        try {
            chassis.close();
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    private Service getService(DeploymentConfiguration c) {
        var module = new Module() {
            @Override
            public void configure(Binder binder) {
                binder.bind(String.class).toInstance("test");
            }
        };
        return new Service() {
            Injector injector = Guice.createInjector(module, new EventStoreModule(), new RabbitMqModule(c));

            @Override
            public CompletableFuture start() {
                return CompletableFuture.completedFuture(true);
            }

            @Override
            public <T extends DomainCommand> CompletableFuture<CommandResponse> send(T command) {
                var router = injector.getInstance(IRouteCommands.class);
                return router.send(command, null);
            }

            @Override
            public <T extends BaseEvent> CompletableFuture publish(T msg) {
                var publisher = injector.getInstance(IPublishEvents.class);
                return publisher.publish(msg, null);
            }

            @Override
            public <T> T resolve(Class<T> type) {
                return (T) injector.getInstance(type);
            }

            @Override
            public void close() throws Exception {

            }
        };
    }
}

