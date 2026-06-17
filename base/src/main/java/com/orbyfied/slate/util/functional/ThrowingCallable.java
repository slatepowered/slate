package com.orbyfied.slate.util.functional;

import com.orbyfied.slate.util.exception.Throwables;

/**
 * A function callable with or
 * without a value, which is likely
 * to throw an error, hence the {@code throws}.
 *
 * This extends {@link Callable} for inter-op.
 * {@link Callable#call(Object)} will silently
 * re-throw any errors caught.
 *
 * @param <V> The value type.
 */
public interface ThrowingCallable<V> extends Callable<V> {

    public static <V> ThrowingCallable<V> of(ThrowingCallable<V> callable) {
        return callable;
    }

    /**
     * Call this callable with the
     * provided value.
     *
     * @param value The value.
     * @throws Throwable Any error may be thrown by the handler code.
     */
    void callThrowing(V value) throws Throwable;

    /**
     * Call this callable with no value (null).
     *
     * @throws Throwable Any error may be thrown by the handler code.
     * @see ThrowingCallable#callThrowing(Object)
     */
    default void callThrowing() throws Throwable {
        callThrowing(null);
    }

    /**
     * For inter-op with {@link Callable}.
     *
     * Will call {@link ThrowingCallable#callThrowing(Object)}
     * with the provided value. It will silently rethrow
     * any errors that may be caught.
     *
     * @param value The value to call with.
     */
    @Override
    default void call(V value) {
        try {
            // call normally
            callThrowing(value);
        } catch (Throwable t) {
            // rethrow error
            Throwables.sneakyThrow(t);
        }
    }

}
