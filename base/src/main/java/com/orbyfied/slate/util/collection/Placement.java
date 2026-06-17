package com.orbyfied.slate.util.collection;

import java.util.List;
import java.util.function.Predicate;

/**
 * Determines the placement of an element in an
 * ordered collection.
 */
@SuppressWarnings("rawtypes")
public interface Placement {

    static Placement at(int offset) {
        return (list, value) -> {
            if (offset < 0) {
                return list.size() - 1 + offset;
            } else {
                return offset;
            }
        };
    }

    static Placement last() {
        return (list, value) -> list.size();
    }

    static Placement last(int offset) {
        return (list, value) -> list.size() + offset;
    }

    // single instance first
    Placement FIRST = (list, value) -> 0;

    @SuppressWarnings("unchecked")
    static Placement first() {
        return FIRST;
    }

    @SuppressWarnings("unchecked")
    static <T> Placement before(Predicate<T> predicate) {
        return (list, value) -> {
            final int l = list.size();
            for (int i = 0; i < l; i++) {
                Object t = list.get(i);

                if (predicate.test((T) t)) {
                    return i;
                }
            }

            return -1;
        };
    }

    ////////////////////////////////

    /**
     * Finds the index to position the
     * given value in a given list.
     *
     * @param list The list to find it in.
     * @param value The value to position.
     * @return The index or -1.
     */
    int find(List list, Object value);

    /** Find a position and throw an exception if no position was found. */
    default int findChecked(List list, Object value) {
        int index = find(list, value);
        if (index == -1)
            throw new IllegalArgumentException("Could not find an index for the given element");
        return index;
    }

    default void insert(List list, Object value) {
        list.add(findChecked(list, value), value);
    }

}