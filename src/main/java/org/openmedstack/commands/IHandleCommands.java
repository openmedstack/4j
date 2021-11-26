package org.openmedstack.commands;

import org.openmedstack.MessageHeaders;

import java.util.concurrent.CompletableFuture;

public interface IHandleCommands<T extends DomainCommand> {
    CompletableFuture<CommandResponse> handle(T command, MessageHeaders messageHeaders);
}

