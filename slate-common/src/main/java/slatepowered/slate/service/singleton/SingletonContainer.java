package slatepowered.slate.service.singleton;

import slatepowered.slate.service.Service;

/**
 * Stores a singleton object.
 */
public class SingletonContainer<T> implements Service {

    /**
     * The value.
     */
    protected T value;

    public T getValue() {
        return value;
    }

    public SingletonContainer<T> value(T value) {
        this.value = value;
        return this;
    }

}
