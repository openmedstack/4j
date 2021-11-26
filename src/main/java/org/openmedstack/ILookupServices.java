package org.openmedstack;

import java.net.URI;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

public interface ILookupServices {
    CompletableFuture<URI> lookup(Class type);
}

