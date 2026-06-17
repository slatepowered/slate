package com.orbyfied.slate.util.selection;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A concrete selection of data including a data source.
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public interface Selection<T> extends Selector<T> {

    /**
     * Create a filtered stream for the given collection using this selection predicate.
     */
    default Stream<T> filteredStream() {
        return filter(sourceStream());
    }

    /**
     * Create a parallel, filtered stream for the given collection using this selection predicate.
     */
    default Stream<T> filteredParallel() {
        return filter(sourceCollection().parallelStream());
    }

    /**
     * Filter the given collection directly into a list.
     */
    default List<T> filteredList() {
        Collection<T> collection = sourceCollection();
        List<T> list = new ArrayList<>();
        for (T elem : collection) {
            if (elem != null && test(elem)) {
                list.add(elem);
            }
        }

        return list;
    }

    /**
     * Create a filtered stream for the given collection using this selection predicate.
     */
    default Stream<T> filteredStream(Object context) {
        return filter(sourceStream(context));
    }

    /**
     * Create a parallel, filtered stream for the given collection using this selection predicate.
     */
    default Stream<T> filteredParallel(Object context) {
        return filter(sourceCollection(context).parallelStream());
    }

    /**
     * Filter the given collection directly into a list.
     */
    default List<T> filteredList(Object context) {
        Collection<T> collection = sourceCollection(context);
        List<T> list = new ArrayList<>();
        for (T elem : collection) {
            if (elem != null && test(elem)) {
                list.add(elem);
            }
        }

        return list;
    }

    default List<T> toMutableList() {
        return new ArrayList<>(filteredList());
    }

    /**
     * Return a filtered version of this selection.
     */
    default Selection<T> filter(Predicate<T> filter) {
        return new FilteredSelection<>(this, filter);
    }

    /**
     * Whether the selection requires a context.
     */
    default boolean isContextual() {
        return false;
    }

    /**
     * Check whether the given element is included in this this selection, if the element is
     * null false is returned.
     */
    default boolean contains(T element) {
        return testTriState(element) == TRUE;
    }

    /**
     * Check whether the given element is included in this selection, if the element is
     * null false is returned.
     */
    byte containsTriState(T element);

    /**
     * Get all unfiltered input data as a stream.
     */
    Stream<T> sourceStream(Object context);

    /**
     * Get all unfiltered input data as a collection.
     */
    Collection<T> sourceCollection(Object context);

    /**
     * Get all unfiltered input data as a stream.
     */
    default Stream<T> sourceStream() {
        return sourceStream(null);
    }

    /**
     * Get all unfiltered input data as a collection.
     */
    default Collection<T> sourceCollection() {
        return sourceCollection(null);
    }

    static <T> EmptySelection<T> empty() {
        return EmptySelection.INSTANCE;
    }

    class EmptySelection<T> implements Selection<T> {
        static final EmptySelection INSTANCE = new EmptySelection();

        @Override
        public Stream<T> sourceStream(Object context) {
            return Stream.empty();
        }

        @Override
        public Collection<T> sourceCollection(Object context) {
            return List.of();
        }

        @Override
        public byte containsTriState(T element) {
            return FALSE;
        }

        @Override
        public boolean test(T t) {
            return false;
        }

        @Override
        public byte testTriState(T elem) {
            return FALSE;
        }
    }

    static <T> FilteredSelection<T> filtered(Selection<T> selection, Predicate<T> predicate) {
        return new FilteredSelection<>(selection, predicate);
    }

    @RequiredArgsConstructor
    class FilteredSelection<T> implements Selection<T> {
        final Selection<T> base;
        final Predicate<T> predicate;

        @Override
        public Stream<T> sourceStream(Object context) {
            return base.sourceStream();
        }

        @Override
        public Collection<T> sourceCollection(Object context) {
            return base.sourceCollection();
        }

        @Override
        public byte containsTriState(T element) {
            if (!predicate.test(element)) return FALSE;
            return base.containsTriState(element);
        }

        @Override
        public boolean test(T t) {
            return predicate.test(t) && base.test(t);
        }

        @Override
        public byte testTriState(T elem) {
            if (!predicate.test(elem)) return FALSE;
            return base.testTriState(elem);
        }
    }

    static <T> AllOfSourceCollectionSelection<T> allOf(Collection<T> collection) {
        return new AllOfSourceCollectionSelection<>(collection);
    }

    interface AllOfSelection<T> extends Selection<T> {

    }

    @RequiredArgsConstructor
    class AllOfSourceCollectionSelection<T> implements AllOfSelection<T> {
        final Collection<T> collection;

        @Override
        public Stream<T> sourceStream(Object context) {
            return collection.stream();
        }

        @Override
        public Collection<T> sourceCollection(Object context) {
            return collection;
        }

        @Override
        public byte containsTriState(T element) {
            return collection.contains(element) ? TRUE : FALSE;
        }

        @Override
        public boolean test(T t) {
            return true;
        }

        @Override
        public byte testTriState(T elem) {
            return TRUE;
        }
    }

    static <T> NoneOfSourceCollectionSelection<T> noneOf(Collection<T> collection) {
        return new NoneOfSourceCollectionSelection<>(collection);
    }

    interface NoneOfSelection<T> extends Selection<T> {
        NoneOfSelection<T> except(Selection<T> selection);
    }

    @RequiredArgsConstructor
    class NoneOfSourceCollectionSelection<T> implements NoneOfSelection<T> {
        final OrSelector<T> exceptSelector = new OrSelector<>();
        final Collection<T> collection;

        public NoneOfSourceCollectionSelection<T> except(Selection<T> selection) {
            exceptSelector.or(selection);
            return this;
        }

        @Override
        public Stream<T> sourceStream(Object context) {
            return collection.stream();
        }

        @Override
        public Collection<T> sourceCollection(Object context) {
            return collection;
        }

        @Override
        public byte containsTriState(T element) {
            return collection.contains(element) ? testTriState(element) : FALSE;
        }

        @Override
        public boolean test(T t) {
            return exceptSelector.test(t);
        }

        @Override
        public byte testTriState(T elem) {
            return exceptSelector.testTriState(elem);
        }
    }

    @RequiredArgsConstructor
    class FilteredCollectionSelection<T> implements Selection<T> {
        final Collection<T> collection;
        final Selector<T> selector;

        @Override
        public byte containsTriState(T element) {
            return collection.contains(element) ? testTriState(element) : FALSE;
        }

        @Override
        public Stream<T> sourceStream(Object context) {
            return collection.stream();
        }

        @Override
        public Collection<T> sourceCollection(Object context) {
            return collection;
        }

        @Override
        public byte testTriState(T elem) {
            return selector.testTriState(elem);
        }

        @Override
        public boolean test(T t) {
            return selector.test(t);
        }
    }

    static <T> SingleElementSelection<T> of(T element) {
        return new SingleElementSelection<>(element);
    }

    @RequiredArgsConstructor
    class SingleElementSelection<T> implements Selection<T> {
        final T element;

        @Override
        public List<T> filteredList() {
            return List.of(element);
        }

        @Override
        public List<T> filteredList(Object context) {
            return List.of(element);
        }

        @Override
        public Stream<T> sourceStream(Object context) {
            return Stream.of(element);
        }

        @Override
        public Collection<T> sourceCollection(Object context) {
            return Collections.singletonList(element);
        }

        @Override
        public byte containsTriState(T element) {
            return testTriState(element);
        }

        @Override
        public boolean test(T t) {
            return element.equals(t);
        }

        @Override
        public byte testTriState(T elem) {
            return element.equals(elem) ? TRUE : FALSE;
        }
    }

}
