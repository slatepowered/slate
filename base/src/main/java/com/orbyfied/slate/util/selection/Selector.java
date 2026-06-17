package com.orbyfied.slate.util.selection;

import lombok.RequiredArgsConstructor;
import com.orbyfied.slate.util.collection.ArrayUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@SuppressWarnings({ "rawtypes", "unchecked" })
public interface Selector<T> extends Predicate<T> {

    byte TRUE      =  1;
    byte UNCERTAIN =  0;
    byte FALSE     = -1;

    /**
     * Filter the given stream using this selection predicate.
     */
    default Stream<T> filter(Stream<T> stream) {
        return stream.filter(e -> e != null && test(e));
    }

    /**
     * Create a filtered stream for the given iterable using this selection predicate.
     */
    default Stream<T> filter(Iterable<T> iterable) {
        return filter(StreamSupport.stream(iterable.spliterator(), false));
    }

    /**
     * Create a filtered stream for the given collection using this selection predicate.
     */
    default Stream<T> filter(Collection<T> collection) {
        return filter(collection.stream());
    }

    /**
     * Create a parallel, filtered stream for the given iterable using this selection predicate.
     */
    default Stream<T> filterParallel(Iterable<T> iterable) {
        return filter(StreamSupport.stream(iterable.spliterator(), true));
    }

    /**
     * Create a parallel, filtered stream for the given collection using this selection predicate.
     */
    default Stream<T> filterParallel(Collection<T> collection) {
        return filter(collection.parallelStream());
    }

    /**
     * Filter the given collection directly into a list.
     */
    default List<T> filtered(Collection<T> collection) {
        List<T> list = new ArrayList<>();
        for (T elem : collection) {
            if (elem != null && test(elem)) {
                list.add(elem);
            }
        }

        return list;
    }

    /**
     * Create a new selection following this selector from the given source collection.
     *
     * @param collection The collection.
     * @return The selection.
     */
    default Selection<T> select(Collection<T> collection) {
        return new Selection.FilteredCollectionSelection<>(collection, this);
    }

    /**
     * Test the given element and return a tri-state.
     * {@link #TRUE} for true, {@link #FALSE} for false and {@link #UNCERTAIN} for uncertain.
     * <p>
     * Uncertainty might occur
     */
    default byte testTriState(T elem) {
        return test(elem) ? TRUE : FALSE;
    }

    /**
     * Invert this selection, this may invert this instance or replicate
     * the behavior of {@link #inverted()} to create a new instance which is returned.
     */
    default Selector<T> invert() {
        return inverted();
    }

    default <R> Selector<R> reverseMap(Function<R, T> function) {
        return elem -> test(function.apply(elem));
    }

    /**
     * Create an inverted instance of this selection.
     * @see Selector#inverted(Selector)
     */
    default Selector<T> inverted() {
        return inverted(this);
    }

    /**
     * Add the given selection as a logic and condition and return the new selection.
     */
    default Selector<T> andImmutable(Selector<? super T> condition) {
        return new AndSelector<>(new Selector[] { this, condition });
    }

    /**
     * Add the given selection as a logic and condition and return the new selection,
     * or this (modified) instance, based on the underlying implementation.
     */
    default Selector<T> and(Selector<? super T> condition) {
        return andImmutable(condition);
    }

    /**
     * Add the given selections as logic and conditions and return the new selection.
     */
    default Selector<T> andImmutable(Selector<? super T>... conditions) {
        return new AndSelector<>(new Selector[] { this }).and((Selector[]) conditions);
    }

    /**
     * Add the given selections as logic and conditions and return the new selection,
     * or this (modified) instance, based on the underlying implementation.
     */
    default Selector<T> and(Selector<? super T>... conditions) {
        return andImmutable(conditions);
    }

    /**
     * Add the given selections as logic and conditions and return the new selection.
     */
    default Selector<T> andImmutable(Collection<? extends Selector<? super T>> conditions) {
        return new AndSelector<>(new Selector[] { this }).and((Collection) conditions);
    }

    /**
     * Add the given selections as logic and conditions and return the new selection,
     * or this (modified) instance, based on the underlying implementation.
     */
    default Selector<T> and(Collection<? extends Selector<? super T>> conditions) {
        return andImmutable(conditions);
    }

    /**
     * Add the given selection as a logic inverted-and condition and return the new selection,
     * or this (modified) instance, based on the underlying implementation.
     */
    default Selector<T> except(Selector<? super T> condition) {
        return and(condition.inverted());
    }

    /**
     * Add the given selection as a logic inverted-and condition and return the new selection.
     */
    default Selector<T> exceptImmutable(Selector<? super T> condition) {
        return andImmutable(condition.inverted());
    }

    /**
     * Add the given selections as logic inverted-and conditions and return the new selection,
     * or this (modified) instance, based on the underlying implementation.
     */
    default Selector<T> except(Collection<? extends Selector<? super T>> conditions) {
        return and(new AndSelector<T>((Collection) conditions).invert());
    }

    /**
     * Add the given selections as logic inverted-and conditions and return the new selection.
     */
    default Selector<T> exceptImmutable(Collection<? extends Selector<? super T>> conditions) {
        return andImmutable(new AndSelector<T>((Collection) conditions).invert());
    }

    /**
     * Add the given selections as logic inverted-and conditions and return the new selection,
     * or this (modified) instance, based on the underlying implementation.
     */
    default Selector<T> except(Selector<? super T>... conditions) {
        return and(new AndSelector<T>(conditions).invert());
    }

    /**
     * Add the given selections as logic inverted-and conditions and return the new selection.
     */
    default Selector<T> exceptImmutable(Selector<? super T>... conditions) {
        return andImmutable(new AndSelector<>(conditions).invert());
    }

    /**
     * Add the given selection as a logic or condition and return the new selection.
     */
    default Selector<T> orImmutable(Selector<? super T> condition) {
        return new OrSelector<>(new Selector[] { this, condition });
    }

    /**
     * Add the given selection as a logic or condition and return the new selection,
     * or this (modified) instance, based on the underlying implementation.
     */
    default Selector<T> or(Selector<? super T> condition) {
        return orImmutable(condition);
    }

    /**
     * Add the given selections as logic or conditions and return the new selection.
     */
    default Selector<T> orImmutable(Selector<? super T>... conditions) {
        return new OrSelector<>(new Selector[] { this }).and(conditions);
    }

    /**
     * Add the given selections as logic or conditions and return the new selection,
     * or this (modified) instance, based on the underlying implementation.
     */
    default Selector<T> or(Selector<? super T>... conditions) {
        return orImmutable(conditions);
    }

    /**
     * Add the given selections as logic or conditions and return the new selection.
     */
    default Selector<T> orImmutable(Collection<? extends Selector<? super T>> conditions) {
        return new OrSelector<>(new Selector[] { this }).and(conditions);
    }

    /**
     * Add the given selections as logic or conditions and return the new selection,
     * or this (modified) instance, based on the underlying implementation.
     */
    default Selector<T> or(Collection<? extends Selector<? super T>> conditions) {
        return orImmutable(conditions);
    }

    ////////////////////////////////////////////////////////////////////

    class AllSelector<T> implements Selector<T> {
        public static final AllSelector INSTANCE = new AllSelector();

        @Override
        public boolean test(T t) {
            return true;
        }

        @Override
        public byte testTriState(T elem) {
            return TRUE;
        }
    }

    static <T> Selector<T> all() {
        return (Selector<T>) AllSelector.INSTANCE;
    }

    static <T> Selector<T> inverted(Selector<T> selection) {
        return new InvertedSelector<>(selection);
    }

    static <T> Selector<T> andOf(Selector<T>... selections) {
        return new AndSelector<>(selections);
    }

    static <T> Selector<T> andOf(Collection<? extends Selector<T>> collection) {
        return new AndSelector<>(collection.toArray(new Selector[0]));
    }

    static <T> Selector<T> orOf(Selector<T>... selections) {
        return new OrSelector<>(selections);
    }

    static <T> Selector<T> orOf(Collection<? extends Selector<T>> collection) {
        return new OrSelector<>(collection.toArray(new Selector[0]));
    }

    class AndSelector<T> implements Selector<T> {
        boolean inverted = false;
        Selector<T>[] conditions;

        public AndSelector(Selector[] conditions) {
            this.conditions = conditions;
        }

        public AndSelector(Collection<Selector> conditions) {
            this.conditions = conditions.toArray(new Selector[0]);
        }

        public AndSelector() {
            this.conditions = new Selector[0];
        }

        @Override
        public boolean test(T t) {
            for (Selector<T> elem : conditions) {
                if (!elem.test(t)) {
                    return inverted;
                }
            }

            return !inverted;
        }

        @Override
        public byte testTriState(T elem) {
            if (conditions.length == 0) return UNCERTAIN;
            return test(elem) ? TRUE : FALSE;
        }

        @Override
        public Selector<T> and(Selector<? super T> condition) {
            conditions = (Selector<T>[]) ArrayUtil.append(conditions, condition);
            return this;
        }

        @Override
        public Selector<T> and(Collection<? extends Selector<? super T>> conditions) {
            this.conditions = (Selector<T>[]) ArrayUtil.append(this.conditions, conditions);
            return this;
        }

        @SafeVarargs
        @Override
        public final Selector<T> and(Selector<? super T>... conditions) {
            this.conditions = (Selector<T>[]) ArrayUtil.append(this.conditions, conditions);
            return this;
        }

        @Override
        public Selector<T> invert() {
            this.inverted = !inverted;
            return this;
        }
    }

    class OrSelector<T> implements Selector<T> {
        boolean inverted = false;
        Selector<T>[] conditions;

        public OrSelector(Selector<T>[] conditions) {
            this.conditions = conditions;
        }

        public OrSelector() {
            this.conditions = new Selector[0];
        }

        @Override
        public boolean test(T t) {
            if (conditions.length == 0) return !inverted;

            for (Selector<T> elem : conditions) {
                if (elem.test(t)) {
                    return !inverted;
                }
            }

            return inverted;
        }

        @Override
        public byte testTriState(T elem) {
            if (conditions.length == 0) return UNCERTAIN;
            return test(elem) ? TRUE : FALSE;
        }

        @Override
        public Selector<T> or(Selector<? super T> condition) {
            conditions = (Selector<T>[]) ArrayUtil.append(conditions, condition);
            return this;
        }

        @Override
        public Selector<T> or(Collection<? extends Selector<? super T>> conditions) {
            this.conditions = (Selector<T>[]) ArrayUtil.append(this.conditions, conditions);
            return this;
        }

        @SafeVarargs
        @Override
        public final Selector<T> or(Selector<? super T>... conditions) {
            this.conditions = (Selector<T>[]) ArrayUtil.append(this.conditions, conditions);
            return this;
        }

        @Override
        public Selector<T> invert() {
            this.inverted = !inverted;
            return this;
        }
    }

    @RequiredArgsConstructor
    class InvertedSelector<T> implements Selector<T> {
        final Selector<T> origin;

        @Override
        public boolean test(T t) {
            return !origin.test(t);
        }
    }

}
