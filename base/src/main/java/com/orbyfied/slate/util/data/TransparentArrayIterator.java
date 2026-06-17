package com.orbyfied.slate.util.data;

import lombok.Getter;

@Getter
public class TransparentArrayIterator<T> implements MutableIterator<T> {
    public TransparentArrayIterator(T[] array) {
        this.array = array;
        this.index = 0;
        this.length = array.length;
    }

    public TransparentArrayIterator(T[] array, int offset, int len) {
        this.array = array;
        this.index = offset;
        this.length = offset + len;
    }

    final T[] array;
    int index;
    int length;

    @Override
    public boolean hasNext() {
        return index < length;
    }

    @Override
    public T next() {
        return array[index++];
    }

    @Override
    public void replace(T value) {
        array[index - /* next advances the index */ 1] = value;
    }
}
