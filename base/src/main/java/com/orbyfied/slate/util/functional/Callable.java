package com.orbyfied.slate.util.functional;

import java.util.function.Consumer;

/**
 * A function callable with or without
 * a value.
 *
 * Overrides {@link Consumer#accept(Object)} and
 * {@link Runnable#run()} for inter-op.
 *
 * @param <V> The value parameter type.
 */
@FunctionalInterface
public interface Callable<V> extends Consumer<V>, Runnable {

    /**
     * Call this callable with the
     * provided value, this may throw
     * an error.
     *
     * @param value The value to call with.
     */
    void call(V value);

    /**
     * Call this callable with no
     * value (null), this may throw
     * an error.
     *
     * @see Callable#call(Object)
     */
    default void call() {
        call(null);
    }

    @Override
    default void accept(V v) {
        call(v);
    }

    @Override
    default void run() {
        call();
    }

}