package com.orbyfied.slate.util.data;

import java.util.function.Supplier;

public class LazyValue<T> {

    T value;

    public T get(Supplier<T> supplier) {
        if (value == null) {
            return value = supplier.get();
        }

        return value;
    }

}
