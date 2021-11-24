package org.openmedstack.domain.guice;

import com.google.inject.Injector;
import org.openmedstack.domain.Aggregate;
import org.openmedstack.domain.Memento;
import org.openmedstack.eventstore.IConstructAggregates;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

class ContainerAggregateFactory implements IConstructAggregates {
    private final Injector _container;

    public ContainerAggregateFactory(Injector container){
        _container = container;
    }

    @Override
    public <T extends Aggregate> T build(Class<T> type, String id, Memento snapshot) {
        T result = null;
        try {
            var optional = Arrays.stream(type.getConstructors()).findFirst();
            if(optional.isPresent()){
                var constructor = optional.get();
                var parameters = Arrays.stream(constructor.getParameterTypes()).map(t->{
                    if (String.class.isAssignableFrom(t)) {
                        return id;
                    }
                    if(Memento.class.isAssignableFrom(t)){
                        return snapshot;
                    }
                    return _container.getInstance(t);
                });
                result = (T)optional.get().newInstance(parameters.toArray());
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}
