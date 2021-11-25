package openmedstack;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import openmedstack.commands.CommandResponse;
import openmedstack.commands.DomainCommand;
import openmedstack.events.BaseEvent;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;

public class ChassisTest {
    Chassis chassis;

    @Before
    public void beforeTest() {
        chassis = Chassis.from(new DeploymentConfiguration());
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
    public void usingCustomBuilder() {
        chassis.usingCustomBuilder(c->null);
    }

    @Test
    public void start() {
        chassis.usingCustomBuilder(this::getService);
        chassis.start();
    }

    @Test
    public void send() {
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
        return new Service() {
            Injector injector = Guice.createInjector(new Module() {
                @Override
                public void configure(Binder binder) {
                    binder.bind(String.class).toInstance("test");
                }
            });

            @Override
            public CompletableFuture start() {
                return null;
            }

            @Override
            public <T extends DomainCommand> CompletableFuture<CommandResponse> send(T command) {
                return null;
            }

            @Override
            public <T extends BaseEvent> CompletableFuture publish(T msg) {
                return null;
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
