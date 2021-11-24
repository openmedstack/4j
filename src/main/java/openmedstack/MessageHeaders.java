package openmedstack;

import java.util.Map;

public interface MessageHeaders extends Map<String, Object> {
    String getUserToken();
}
