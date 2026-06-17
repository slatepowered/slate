package com.orbyfied.slate.util.data;

import java.util.Iterator;
import java.util.function.Function;

@SuppressWarnings("unchecked")
public interface MutableIterator<T> extends Iterator<T> {

    static <E> TransparentArrayIterator<E> array(E[] arr) {
        return new TransparentArrayIterator<>(arr);
    }

    static <E> TransparentArrayIterator<E> array(E[] arr, int off, int len) {
        return new TransparentArrayIterator<>(arr, off, len);
    }

    default <U> U nextAs() {
       return (U) next();
    }

    default  <U> U convertAndReplaceNext(Class<U> uClass, Function<T, U> function) {
        T elem = next();
        if (uClass.isInstance(elem)) {
            return (U) elem;
        }

        U val = function.apply(elem);
        replace((T) val);
        return val;
    }

    /**
     * Replace the current element with the given value.
     */
    void replace(T value);

}
