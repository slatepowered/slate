package com.orbyfied.slate.util.data;

import com.orbyfied.slate.util.exception.Throwables;

import java.util.function.Supplier;

/**
 * Construct to hold the result of an
 * operation which could have thrown
 * an error.
 *
 * @param <V> The value type on success.
 */
public class Result<V> {

    /**
     * Create a new success result, with a nullable
     * value. The returned instance will always
     * have a value set and marked as set.
     *
     * @param value The value (nullable).
     * @param <V> The value type.
     * @return The result instance.
     */
    public static <V> Result<V> success(final V value) {
        return new Result<>(value, null);
    }

    /**
     * Create a new success result, but without
     * a value set or marked as set. The returned
     * instance will never have a value set or
     * marked as set.
     *
     * @param <V> The value type.
     * @return The result instance.
     */
    public static <V> Result<V> unset() {
        return new Result<>(null, NO_VALUE);
    }

    /**
     * Create a new result signaling that the operation
     * failed with the given error, and no value is
     * present. The returned instance will never have
     * a value set or marked as set.
     *
     * @param throwable The cause.
     * @param <V> The value type (for compatibility).
     * @return The failed result instance.
     */
    public static <V> Result<V> failed(Throwable throwable) {
        if (throwable == null)
            throwable = new FailedException();
        return new Result<>(null, throwable);
    }

    /**
     * For absent errors.
     * Simply describes that the operation failed.
     */
    public static class FailedException extends RuntimeException {
        @Override public String getMessage() { return "No further information"; }
    }

    /** No value descriptor instance. */
    static final NoValue NO_VALUE = new NoValue();
    static class NoValue extends Throwable { private NoValue() { } }

    /////////////////////////////////////////////

    /**
     * Internal constructor.
     *
     * Protected because it directly
     * sets the fields, including the
     * 'special' throwable field.
     */
    Result(final V value,
           final Throwable throwable) {
        this.value     = value;
        this.throwable = throwable;
    }

    /**
     * The optional value provided as
     * a result of the operation. This can
     * be set to null, which is why it is not
     * used to check for presence.
     */
    final V value;

    /**
     * The optional throwable thrown by the result.
     *
     * If this is null no error was thrown and
     * a value is present.
     * If this is {@link Result#NO_VALUE} no error was thrown,
     * but no value is present either.
     * If this is any other instance, an error was thrown.
     */
    final Throwable throwable;

    /**
     * Check if a SUCCESSFUL value is present.
     * You can assign a value and a thrown error,
     * which means in some rare cases, entirely
     * based on the source of this result, it can
     * still return false while containing a value.
     *
     * @return If a value was successfully assigned and is present.
     */
    public boolean isPresent() {
        return throwable == null;
    }

    /**
     * Check if an error occurred.
     * This just checks to see if a throwable
     * was captured by this result.
     *
     * @return If a throwable was captured.
     */
    public boolean isSuccess() {
        return throwable != null && throwable != NO_VALUE;
    }

    /**
     * Returns the captured error if present,
     * or null if absent. The field being set
     * to {@link Result#NO_VALUE} is considered
     * absent, so null will be returned.
     *
     * @return The error or null if absent.
     */
    public Throwable error() {
        return throwable != NO_VALUE ? throwable : null;
    }

    /**
     * Get the value if present or null
     * if absent. This ignores if an and
     * what error was thrown.
     *
     * @return The value or null if absent.
     */
    public V orNull() {
        return value;
    }

    /**
     * Get the value if present or the provided
     * default/fallback value if absent. This uses
     * {@link Result#isPresent()} to check presence.
     *
     * @param def The fallback value.
     * @return The value.
     */
    public V orElse(V def) {
        if (isPresent())
            return value;
        return def;
    }

    /**
     * Get the value if present or result of the provided
     * default/fallback if absent. This uses
     * {@link Result#isPresent()} to check presence.
     *
     * @param def The fallback value supplier.
     * @return The value.
     */
    public V orElseGet(Supplier<V> def) {
        if (isPresent())
            return value;
        return def.get();
    }

    /**
     * Rethrow the error if present, or
     * exit and return this instance back if absent.
     * This uses {@link Throwables#sneakyThrow(Throwable)}
     * to rethrow the error.
     *
     * @return This.
     */
    public Result<V> rethrowFailed() {
        Throwable t = error();
        if (t != null)
            Throwables.sneakyThrow(t);
        return this;
    }

    /**
     * Get the captured value if successful, or
     * rethrow the captured error if failed.
     * This uses {@link Throwables#sneakyThrow(Throwable)}
     * to rethrow the error.
     *
     * @return The value.
     */
    public V orRethrow() {
        Throwable t = error();
        if (t != null)
            Throwables.sneakyThrow(t);

        return value;
    }

    /**
     * Get and return the value if present,
     * or throw with a description if absent.
     *
     * If the value is simply absent, with no cause ({@link Result#error()}),
     * a blank {@link AbsentValueException} will be thrown. If there
     * is a cause, an error captured, it will be set as the cause for
     * the {@link AbsentValueException} thrown.
     *
     * @return The value.
     * @throws AbsentValueException If no value is present.
     */
    public V unwrap() {
        if (throwable == NO_VALUE)
            throw new AbsentValueException();
        if (throwable != null)
            throw new AbsentValueException("Operation failed with an exception", throwable);
        return value;
    }

    /**
     * An exception used by {@link Result} when unwrapping
     * the result to signal the absence of a value.
     *
     * When no value is present due to an error,
     * this exception will usually have the cause set to that
     * error to signal the reason.
     */
    public static class AbsentValueException extends RuntimeException {

        public AbsentValueException() { }

        public AbsentValueException(String message) {
            super(message);
        }

        public AbsentValueException(String message, Throwable cause) {
            super(message, cause);
        }

        public AbsentValueException(Throwable cause) {
            super(cause);
        }

    }

}