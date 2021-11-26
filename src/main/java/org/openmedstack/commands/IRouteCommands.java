package org.openmedstack.commands;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public interface IRouteCommands {
    <T extends DomainCommand> CompletableFuture<CommandResponse> send(T command, HashMap<String, Object> headers);
}

