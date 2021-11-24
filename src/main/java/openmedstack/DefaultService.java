package openmedstack;

import com.google.inject.Guice;
import com.google.inject.Injector;
import openmedstack.commands.CommandResponse;
import openmedstack.commands.DomainCommand;
import openmedstack.commands.IRouteCommands;
import openmedstack.events.BaseEvent;
import openmedstack.events.IPublishEvents;

import java.util.concurrent.CompletableFuture;

public class DefaultService implements Service {
    final Injector injector;

    public DefaultService(com.google.inject.Module... modules) {
        injector = Guice.createInjector(modules);
    }

    @Override
    public CompletableFuture start() {
        return null;
    }

    @Override
    public <T extends DomainCommand> CompletableFuture<CommandResponse> send(T command) {
        var sender = injector.getInstance(IRouteCommands.class);
        return sender.send(command, null);
    }

    @Override
    public <T extends BaseEvent> CompletableFuture publish(T msg) {

        var sender = injector.getInstance(IPublishEvents.class);
        return sender.publish(msg, null);
    }

    @Override
    public <T> T resolve(Class<T> type) {
        return injector.getInstance(type);
    }

    @Override
    public void close() throws Exception {
    }
}
