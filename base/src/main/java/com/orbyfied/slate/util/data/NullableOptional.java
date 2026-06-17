package com.orbyfied.slate.util.data;

import lombok.RequiredArgsConstructor;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

@RequiredArgsConstructor
@SuppressWarnings({ "unchecked", "rawtypes" })
public class NullableOptional<T> {

    public static final Object NONE_VALUE = new Object();
    public static final NullableOptional EMPTY = new NullableOptional(NONE_VALUE);

    public static <T> NullableOptional<T> ofOptional(Optional<T> optional) {
        return optional.isPresent() ? present(optional.get()) : EMPTY;
    }

    public static <T> NullableOptional<T> present(T value) {
        return new NullableOptional<>(value);
    }

    public static <T> NullableOptional<T> empty() {
        return (NullableOptional<T>) EMPTY;
    }

    /**
     * The value.
     */
    private final Object value;

    /**
     * Get the raw value, which may be either {@link #NONE_VALUE} to indicate an empty optional,
     * null for a present null value or an object.
     */
    public Object rawValue() {
        return value;
    }

    private NoSuchElementException emptyError() {
        return new NoSuchElementException("Optional is empty");
    }

    public boolean isEmpty() {
        return value == NONE_VALUE;
    }

    public boolean isPresent() {
        return value != NONE_VALUE;
    }

    public T get() {
        if (value == NONE_VALUE) {
            throw emptyError();
        }

        return (T) value;
    }

    public T orElse(T def) {
        return value == NONE_VALUE ? def : (T) value;
    }

    public T orSupply(Supplier<T> def) {
        return value == NONE_VALUE ? def.get() : (T) value;
    }

    public NullableOptional<T> ifPresent(Consumer<T> consumer) {
        if (isPresent()) {
            consumer.accept(get());
        }

        return this;
    }

}
