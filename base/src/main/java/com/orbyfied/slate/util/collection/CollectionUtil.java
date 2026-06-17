package com.orbyfied.slate.util.collection;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public class CollectionUtil {

    public static <E> E findFirst(List<E> list, Predicate<E> predicate) {
        for (E e : list) {
            if (predicate.test(e)) {
                return e;
            }
        }

        return null;
    }

    /**
     * Combine the given lists into one returned.
     */
    public static <E> List<E> concat(List<E> a, List<E> b) {
        List<E> list = new ArrayList<>(a.size() + b.size());
        list.addAll(a);
        list.addAll(b);
        return list;
    }

    public static <E> List<E> concat(List<E> a, E... b) {
        return concat(a, Arrays.asList(b));
    }

    /* Mapping and filtering */

    public static <E, T> List<T> map(List<E> list, Function<E, T> function) {
        List<T> ls = new ArrayList<>(list.size());
        int l = list.size();
        for (int i = 0; i < l; i++) {
            ls.add(function.apply(list.get(i)));
        }

        return ls;
    }

    public static <E, T> List<T> map(List<E> list, BiFunction<Integer, E, T> function) {
        List<T> ls = new ArrayList<>(list.size());
        int l = list.size();
        for (int i = 0; i < l; i++) {
            ls.add(function.apply(i, list.get(i)));
        }

        return ls;
    }

    public static <E, T> List<T> filterAndMap(List<E> list, Predicate<E> predicate, Function<E, T> function) {
        List<T> ls = new ArrayList<>(list.size());
        for (E e : list) {
            if (predicate.test(e)) {
                ls.add(function.apply(e));
            }
        }

        return ls;
    }

    public static <E> List<E> flatten(List<List<E>> list) {
        List<E> list1 = new ArrayList<>();
        for (List<E> list2 : list) {
            list1.addAll(list2);
        }

        return list1;
    }

    public static <T> List<T> takeFirst(List<T> list, int count) {
        if (list.size() < count) return list;
        return list.subList(0, count);
    }

    public static <T> List<T> takeFirstAndCount(List<T> list, int count, int[] outCount) {
        if (list.size() < count) {
            outCount[0] = list.size();
            return list;
        }

        outCount[0] = count;
        return list.subList(0, count);
    }

    public static <T> boolean anyMatch(Collection<T> collection, Predicate<T> predicate) {
        for (T elem : collection) {
            if (predicate.test(elem)) {
                return true;
            }
        }

        return false;
    }

    public static <T> Optional<T> firstMatch(List<T> list, Predicate<T> predicate) {
        for (T elem : list) {
            if (predicate.test(elem)) {
                return Optional.of(elem);
            }
        }

        return Optional.empty();
    }

    public static <T> Optional<Integer> firstMatchIndex(List<T> list, Predicate<T> predicate) {
        for (int i = 0; i < list.size(); i++) {
            if (predicate.test(list.get(i))) {
                return Optional.of(i);
            }
        }

        return Optional.empty();
    }

    public static <T> Optional<Integer> lastMatchIndex(List<T> list, Predicate<T> predicate) {
        for (int i = list.size() - 1; i >= 0; i--) {
            if (predicate.test(list.get(i))) {
                return Optional.of(i);
            }
        }

        return Optional.empty();
    }

    public static <T> List<T> intersection(Collection<T> a, Collection<T> b) {
        if (b instanceof Set<T> set) {
            var t = a;
            a = b;
            b = t;
        }

        List<T> result = new ArrayList<>();
        if (a instanceof Set<T> set) {
            for (T item : b) {
                if (set.contains(item)) {
                    result.add(item);
                }
            }
        } else if (a.size() < 10) {
            for (T item : b) {
                if (a.contains(item)) {
                    result.add(item);
                }
            }
        } else {
            Set<T> set = new HashSet<>(a);
            for (T item : b) {
                if (set.contains(item)) {
                    result.add(item);
                }
            }
        }

        return result;
    }

    public static <T> Optional<T> first(List<T> collection) {
        return collection.isEmpty() ? Optional.empty() : Optional.of(collection.get(0));
    }

}
