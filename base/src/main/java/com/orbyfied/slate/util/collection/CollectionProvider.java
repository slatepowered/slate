package com.orbyfied.slate.util.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@SuppressWarnings({ "unchecked", "rawtypes" })
@FunctionalInterface
public interface CollectionProvider<T, C extends Collection<T>> {

    CollectionProvider<Object, List<Object>> GENERIC_LIST_PREFERS_ARRAYLIST = optionalInitialCollection -> {
        if (optionalInitialCollection == null) {
            return new ArrayList<>();
        }

        if (optionalInitialCollection instanceof List<Object> list) {
            return list;
        }

        return new ArrayList<>(optionalInitialCollection);
    };

    static <T> CollectionProvider<T, List<T>> listPrefersArrayList() {
        return (CollectionProvider<T, List<T>>) (Object) GENERIC_LIST_PREFERS_ARRAYLIST;
    }

    /**
     * Create a new collection of the appropriate type, with the given initial elements
     * if specified.
     *
     * @param optionalInitialCollection The initial collection.
     * @return The collection.
     */
    C provide(Collection<T> optionalInitialCollection);

}
