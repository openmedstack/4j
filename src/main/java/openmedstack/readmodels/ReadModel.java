package openmedstack.readmodels;

import openmedstack.MessageHeaders;
import openmedstack.events.BaseEvent;

import java.util.concurrent.CompletableFuture;

public interface ReadModel {
    int getVersion();
}

