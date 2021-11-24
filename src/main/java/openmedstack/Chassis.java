package openmedstack;

import com.google.inject.Module;
import openmedstack.commands.CommandResponse;
import openmedstack.commands.DomainCommand;
import openmedstack.events.BaseEvent;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class Chassis implements AutoCloseable {
    private final DeploymentConfiguration _configuration;
    private final HashMap<String, Object> _metadata;
    private Function<DeploymentConfiguration, Service> _builder = Chassis::buildService;
    private Service _service;

    private Chassis(DeploymentConfiguration configuration) {
        _configuration = configuration;
        _metadata = new HashMap<>();
    }

    public DeploymentConfiguration getConfiguration() {
        return _configuration;
    }

    public static Chassis from(DeploymentConfiguration manifest) {
        return new Chassis(manifest);
    }

    public Chassis usingCustomBuilder(Function<DeploymentConfiguration, Service> builder) {
        _builder = builder;
        return this;
    }

    public CompletableFuture start() {
        _service = _builder.apply(_configuration);
        return _service.start();
    }

    public <T extends DomainCommand> CompletableFuture<CommandResponse> send(T command) throws NullPointerException {
        if (_service == null) {
            throw new NullPointerException("Chassis not started");
        }
        return _service.send(command);
    }

    public <T extends BaseEvent> CompletableFuture publish(T msg) throws NullPointerException {
        if (_service == null) {
            throw new NullPointerException("Chassis not started");
        }
        return _service.publish(msg);
    }

    public <T extends Object> T resolve(Class<T> type) throws NullPointerException {
        if (_service == null) {
            throw new NullPointerException("Chassis not started");
        }
        return _service.resolve(type);
    }

    private static Service buildService(DeploymentConfiguration manifest, Module... modules) throws IllegalStateException {
        return new DefaultService(modules);
    }

    @Override
    public void close() throws Exception {
        if(_service != null){
            _service.close();
        }
        _metadata.clear();
    }
}
