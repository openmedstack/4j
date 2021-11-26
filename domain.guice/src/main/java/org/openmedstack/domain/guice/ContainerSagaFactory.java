package org.openmedstack.domain.guice;

import com.google.inject.Inject;
import com.google.inject.Injector;
import org.openmedstack.domain.Saga;
import org.openmedstack.eventstore.IConstructSagas;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

class ContainerSagaFactory implements IConstructSagas {
    private final Injector _container;

    @Inject
    public ContainerSagaFactory(Injector container) {
        _container = container;
    }

    @Override
    public <T extends Saga> T build(Class<T> type, String id) {
        T result = null;
        try {
            var optional = Arrays.stream(type.getConstructors()).findFirst();
            if (optional.isPresent()) {
                var constructor = optional.get();
                var parameters = Arrays.stream(constructor.getParameterTypes()).map(t -> {
                    if (String.class.isAssignableFrom(t)) {
                        return id;
                    }
                    return _container.getInstance(t);
                });
                result = (T) optional.get().newInstance(parameters.toArray());
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}
