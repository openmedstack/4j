package openmedstack.events;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public interface IPublishEvents {
    <T extends BaseEvent> CompletableFuture publish(T evt, HashMap<String, Object> headers);
}

