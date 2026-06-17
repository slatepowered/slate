package com.orbyfied.slate.util.functional;

import com.orbyfied.slate.util.data.Container;
import com.orbyfied.slate.util.data.Result;
import com.orbyfied.slate.util.exception.Throwables;

import java.util.function.Supplier;

/**
 * A supplier of a value, but declaring
 * any throwables that might be thrown.
 *
 * This class extends {@link Supplier} for inter-op.
 * When {@link Supplier#get()} is called, it calls {@link ThrowingSupplier#issue()}
 * and rethrows any caught errors that may occur.
 *
 * @param <T> The value type.
 */
@FunctionalInterface
public interface ThrowingSupplier<T> extends Supplier<T> {

    /**
     * Helper method to avoid casting when trying to provide
     * a throwing supplier to a method taking in a {@link Supplier}.
     *
     * Example: {@code lazy(ThrowingSupplier.of(() -> ...))}
     * instead of {@code lazy((ThrowingSupplier)() -> }
     *
     * @param supplier The supplier.
     * @param <T> The value type.
     * @return The supplier.
     */
    static <T> ThrowingSupplier<T> of(ThrowingSupplier<T> supplier) {
        return supplier;
    }

    /**
     * Create a new throwing supplier, which
     * just always returns the provided value.
     * This supplier will never throw an error.
     *
     * @param value The constant value to supply.
     * @param <T> The value type.
     * @return The supplier instance.
     */
    static <T> ThrowingSupplier<T> constant(final T value) {
        return () -> value;
    }

    /////////////////////////////////////////////////

    /**
     * The throwing supplier method.
     *
     * @return The value.
     * @throws Throwable Any errors may be thrown.
     */
    T issue() throws Throwable;

    /**
     * Get the value by calling the {@link ThrowingSupplier#issue()}
     * method, and sneakily rethrow any errors to eliminate
     * a {@code throws ...} declaration.
     *
     * This method is overridden from {@link Supplier#get()}, which means
     * this class can be used in exchange with {@link Supplier}.
     *
     * @return The value.
     */
    @Override
    default T get() {
        try {
            // issue value
            return issue();
        } catch (Throwable t) {
            // rethrow error
            Throwables.sneakyThrow(t);
            return null;
        }
    }

    /**
     * Try to issue the value using {@link ThrowingSupplier#issue()},
     * but catch any errors and return null instead of rethrowing them.
     *
     * @return The value or null if an error occurred.
     */
    default T tryOrNull() {
        try {
            // issue value
            return issue();
        } catch (Throwable t) {
            return null;
        }
    }

    /**
     * Try to issue the value using {@link ThrowingSupplier#issue()}
     * and return a successful result when successful, but catch any
     * errors and return a failed result capturing them instead of
     * rethrowing them.
     *
     * @return The result.
     */
    default Result<T> attempt() {
        try {
            // issue value and return success
            return Result.success(issue());
        } catch (Throwable t) {
            // fail with error
            return Result.failed(t);
        }
    }

    /**
     * Creates a new lazily loaded {@link Container}
     * using {@link Container#lazy(Supplier)} with this
     * supplier as the loader function.
     *
     * @return The container instance.
     */
    default Container<T> asContainer() {
        return Container.lazy(this);
    }

}