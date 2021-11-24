package org.openmedstack.domain;

import openmedstack.events.BaseEvent;
import org.junit.Assert;
import org.junit.Test;

import java.time.Instant;

public class ConventionEventRouterTest {
    @Test
    public void generatesHandlersFromHandlerSource() {
        var handler = new TestHandler();
        var router = new ConventionEventRouter(true, handler);
        try {
            router.dispatch(new TestMessage("test", Instant.now()));
        } catch (HandlerForDomainEventNotFoundException e) {
            Assert.fail();
        }
    }
}

class TestHandler{
    public void apply(TestMessage msg){
    }
}

class TestMessage extends BaseEvent{
    protected TestMessage(String source, Instant timeStamp) {
        super(source, timeStamp);
    }
}
