package openmedstack;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class MessageHeadersImpl implements MessageHeaders {
    private final Map<String, Object> _headers;

    public MessageHeadersImpl(Entry<String, Object>... items) {
        _headers = Map.ofEntries(items);
    }

    @Override
    public String getUserToken() {
        return (String) _headers.get("token");
    }

    @Override
    public int size() {
        return _headers.size();
    }

    @Override
    public boolean isEmpty() {
        return _headers.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return _headers.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return _headers.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        return _headers.get(key);
    }

    @Override
    public Object put(String key, Object value) {
        return _headers.put(key, value);
    }

    @Override
    public Object remove(Object key) {
        return _headers.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ?> m) {
        _headers.putAll(m);
    }

    @Override
    public void clear() {
        _headers.clear();
    }

    @Override
    public Set<String> keySet() {
        return _headers.keySet();
    }

    @Override
    public Collection<Object> values() {
        return _headers.values();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return _headers.entrySet();
    }
}

