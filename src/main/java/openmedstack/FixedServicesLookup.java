package openmedstack;

import java.net.URI;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

public class FixedServicesLookup implements ILookupServices {
    private final HashMap<Pattern, URI> _serviceAddresses;

    public FixedServicesLookup(HashMap<Pattern, URI> serviceAddresses) {
        _serviceAddresses = serviceAddresses;
    }

    @Override
    public CompletableFuture<URI> lookup(Class type) {
        for (final Pattern p : _serviceAddresses.keySet()) {
            if (p.matcher(type.getName()).find()) {
                return CompletableFuture.supplyAsync(() -> _serviceAddresses.get(p));
            }
        }
        throw new IllegalArgumentException(type.getName());
    }
}
