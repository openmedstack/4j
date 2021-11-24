package openmedstack;

import openmedstack.commands.CommandResponse;
import openmedstack.commands.DomainCommand;
import openmedstack.events.BaseEvent;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public interface Service extends AutoCloseable {
    CompletableFuture start();

    <T extends DomainCommand> CompletableFuture<CommandResponse> send(T command);

    <T extends BaseEvent> CompletableFuture publish(T msg);

    <T extends Object> T resolve(Class<T> type);
}

