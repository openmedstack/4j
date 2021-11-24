package openmedstack.commands;

import java.util.concurrent.CompletableFuture;

public interface ISubscribeCommands<T extends DomainCommand> {
    CompletableFuture<AutoCloseable> Subscribe(IHandleCommands<T> handler);
}
