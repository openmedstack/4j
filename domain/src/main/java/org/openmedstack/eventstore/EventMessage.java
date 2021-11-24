package org.openmedstack.eventstore;

import java.util.HashMap;

public final class EventMessage {
    private Object _body;
    private HashMap<String, Object> _headers;

    public EventMessage(Object body, HashMap<String, Object> headers) {
        _headers = headers == null ? new HashMap<>() : headers;
        _body = body;
    }

    public HashMap<String, Object> getHeaders() {
        return _headers;
    }

    public Object getBody() {
        return _body;
    }
}
