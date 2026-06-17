package com.orbyfied.slate.util.data;

import com.orbyfied.slate.util.exception.Throwables;
import com.orbyfied.slate.util.functional.Callback;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * A class responsible for holding a
 * value and providing access to that
 * value under certain conditions.
 *
 * @param <V> The value type.
 */
public interface Container<V> {

    /**
     * Create a new immutable container
     * with the value already set.
     *
     * @param value The final value.
     * @param <V> The value type.
     * @return The container instance.
     */
    static <V> Container<V> finalImmutable(final V value) {
        // return new container
        return new Container<V>() {
            @Override
            public V get() {
                return value;
            }

            @Override
            public boolean isSet() {
                return true;
            }

            @Override
            public Container<V> set(V val) {
                throw new UnsupportedOperationException("Container is immutable");
            }

            @Override
            public Mutability mutability() {
                return Mutability.UNSUPPORTED;
            }

        };
    }

    /**
     * Create a new container whose value can
     * only be set once.
     *
     * @param <V> The value type.
     * @return The container instance.
     */
    static <V> Container<V> futureImmutable() {
        // return new container
        return new Container<V>() {
            // the current value
            V value;
            // if it has been set
            boolean set;

            @Override
            public V get() {
                return value;
            }

            @Override
            public boolean isSet() {
                return set;
            }

            @Override
            public Container<V> set(V val) {
                if (set)
                    throw new UnsupportedOperationException("This container already has" +
                            " a value set");
                this.value = val;
                this.set = true;
                return this;
            }

            @Override
            public Mutability mutability() {
                return set ? Mutability.UNSUPPORTED : Mutability.MODIFY;
            }
        };
    }

    /**
     * Create a new mutable container instance
     * with a value optionally pre-set. Providing
     * null to the value argument is essentially
     * the same as providing nothing at all.
     *
     * @param val The value to pre-set.
     * @param <V> The value type.
     * @return The container instance.
     */
    static <V> Container<V> mutable(final V val) {
        // return new container
        return new Container<V>() {
            // the value currently stored
            V value = val;

            @Override
            public V get() {
                return value;
            }

            @Override
            public boolean isSet() {
                return true;
            }

            @Override
            public Container<V> set(V val) {
                value = val;
                return this;
            }

            @Override
            public Mutability mutability() {
                return Mutability.MODIFY;
            }
        };
    }

    /**
     * Create a new mutable container instance
     * without a value pre-set. Just calls
     * {@link Container#mutable(Object)} with null
     * provided as the pre-set value.
     *
     * @param <V> The value type.
     * @return The container instance.
     */
    static <V> Container<V> mutable() {
        return mutable(null);
    }

    /**
     * Wraps the given container in an
     * access layer protecting it against
     * denied callers according to
     * the provided stack frame predicate.
     *
     * @param container The container to wrap.
     * @param predicate The predicate to test the frames with.
     * @param protectNew If new instances created by any of the
     *                   method calls of the wrapped instance
     *                   should automatically be protected under
     *                   the same conditions.
     * @param <V> The value type.
     * @return The new wrapped container.
     */
    static <V> Container<V> protect(final Container<V> container,
                                    final Predicate<StackTraceElement> predicate,
                                    final boolean protectNew) {
        // return new container
        return new Container<V>() {
            /**
             * Check the access for the caller of
             * the function which called this.
             */
            private void checkAccess() {
                throw new UnsupportedOperationException("TODO");
//                StackTraceElement element = TODO;
//                if (!predicate.test(element))
//                    throw new SecurityException("Access to container denied");
            }

            @Override
            public V get() {
                checkAccess();
                return container.get();
            }

            @Override
            public boolean isSet() {
                checkAccess();
                return container.isSet();
            }

            @Override
            public Container<V> set(V val) {
                checkAccess();

                // get return value
                Container<V> ret = container.set(val);

                if (ret != container)
                    if (protectNew)
                        // protect new instances
                        return Container.protect(ret, predicate, true);
                    else
                        // dont protect new instance
                        return ret;
                else
                    // return already protected this
                    return this;
            }

            @Override
            public Mutability mutability() {
                checkAccess();
                return container.mutability();
            }
        };
    }

    /**
     * Create a new container wrapping the
     * given container instance, which will,
     * when modified, fork the instance,
     * apply modification to the fork, and
     * return the forked instance from the method.
     *
     * @param container The container to wrap.
     * @param forkConstructor The fork function.
     * @param <V> The value type.
     * @return The wrapper container.
     */
    static <V> Container<V> forking(final Container<V> container,
                                    final BiFunction<Container<V>, V, Container<V>> forkConstructor) {
        // return new container
        return new Container<V>() {
            @Override
            public V get() {
                return container.get();
            }

            @Override
            public boolean isSet() {
                return container.isSet();
            }

            @Override
            public Container<V> set(V val) {
                return forkConstructor.apply(container, val);
            }

            @Override
            public Mutability mutability() {
                return Mutability.FORK;
            }
        };
    }

    /**
     * Create a new container wrapping the
     * provided, already existent container,
     * to introduce awaiting functionality.
     * It supports multiple futures awaiting
     * an event at once.
     *
     * If an error is thrown, all futures will
     * be completed exceptionally, after which
     * the error will be rethrown using
     * {@link Throwables#sneakyThrow(Throwable)}.
     *
     * @param container The container to wrap.
     * @param <V> The value type.
     * @return The new, wrapper container.
     */
    static <V> Container<V> awaitable(final Container<V> container) {
        // return new container
        return new Container<V>() {
            // the callback
            Callback<V> callback;
            // the futures awaiting
            final List<CompletableFuture<V>> futures = new ArrayList<>();

            @Override
            public V get() {
                return container.get();
            }

            @Override
            public boolean isSet() {
                return container.isSet();
            }

            @Override
            public Container<V> set(V val) {
                try {
                    container.set(val);
                    if (callback != null)
                        callback.call(val);
                    for (CompletableFuture<V> future : futures)
                        future.complete(val);
                } catch (Throwable t) {
                    for (CompletableFuture<V> future : futures)
                        future.completeExceptionally(t);
                    Throwables.sneakyThrow(t);
                }

                return this;
            }

            @Override
            public Mutability mutability() {
                return container.mutability();
            }

            @Override
            public boolean canAwait() {
                return true;
            }

            @Override
            public Callback<V> callback() {
                if (callback == null) {
                    callback = Callback.multi();
                }

                return callback;
            }

            @Override
            public CompletableFuture<V> await(boolean listen) {
                CompletableFuture<V> future = new CompletableFuture<>();
                if (!listen && isSet())
                    future.complete(get());
                else
                    futures.add(future);
                return future;
            }
        };
    }

    /**
     * Wraps the provided container to make it
     * immutable. This does not affect the original
     * instance, so you can still modify the value
     * using the original instance.
     *
     * @param container The container to wrap.
     * @param <V> The value type.
     * @return The immutable container wrapper.
     */
    static <V> Container<V> immutable(Container<V> container) {
        return new Container<V>() {
            @Override
            public V get() {
                return container.get();
            }

            @Override
            public boolean isSet() {
                return container.isSet();
            }

            @Override
            public Container<V> set(V val) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Mutability mutability() {
                return Mutability.UNSUPPORTED;
            }
        };
    }

    /**
     * Create a new container which lazy loads
     * the value using the provided supplier when
     * it is first queried.
     *
     * This container is immutable.
     *
     * @param supplier The lazy loader.
     * @param <V> The value type.
     * @return The lazy loaded container instance.
     */
    static <V> Container<V> lazy(Supplier<V> supplier) {
        return new Container<V>() {
            // if we have a cached value
            boolean hasCached;
            // the cached value
            V cached;

            @Override
            public V get() {
                if (!hasCached) {
                    cached = supplier.get();
                    hasCached = true;
                }

                return cached;
            }

            @Override
            public boolean isSet() {
                return true;
            }

            @Override
            public Container<V> set(V val) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Mutability mutability() {
                return Mutability.UNSUPPORTED;
            }
        };
    }

    /**
     * A mutable container which stores the current
     * value in an atomic reference making it thread
     * safe.
     *
     * @param <V> The value type.
     * @return The atomic container instance.
     */
    static <V> Container<V> atomic() {
        return new Container<V>() {
            // the reference
            final AtomicReference<V> reference = new AtomicReference<>();
            // if a value is set
            boolean set;

            @Override
            public V get() {
                return reference.get();
            }

            @Override
            public boolean isSet() {
                return set;
            }

            @Override
            public Container<V> set(V val) {
                set = true;
                reference.set(val);
                return this;
            }

            @Override
            public Mutability mutability() {
                return Mutability.MODIFY;
            }
        };
    }

    /**
     * Wraps the given container with the provided
     * mapping function to return an immutable container
     * which maps the queried value of type {@code V} to
     * a value of type {@code T}.
     *
     * @param container The container to wrap.
     * @param function The V -> R mapper.
     * @param <V> The original value type.
     * @param <R> The mapped value type.
     * @return The mapping container.
     */
    static <V, R> Container<R> mapped(final Container<V> container,
                                      final Function<V, R> function) {
        return new Container<R>() {
            @Override
            public R get() {
                return function.apply(container.get());
            }

            @Override
            public boolean isSet() {
                return container.isSet();
            }

            @Override
            public Container<R> set(R val) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Mutability mutability() {
                return Mutability.UNSUPPORTED;
            }
        };
    }

    /**
     * Wraps the given container with a two-way mapping system
     * returning a bi-mapped container, which converts
     * between {@code V} and {@code R} for setting and getting.
     * This container retains the mutability of the wrapped container,
     * meaning it can be mutable.
     *
     * @param container The container to wrap.
     * @param toFunction The V -> R function.
     * @param fromFunction The R -> V function.
     * @param <V> The original value type.
     * @param <R> The mapped value type.
     * @return The mapping container.
     */
    static <V, R> Container<R> biMapped(final Container<V> container,
                                        final Function<V, R> toFunction,
                                        final Function<R, V> fromFunction) {
        return new Container<R>() {
            @Override
            public R get() {
                return toFunction.apply(container.get());
            }

            @Override
            public boolean isSet() {
                return container.isSet();
            }

            @Override
            public Container<R> set(R val) {
                container.set(fromFunction.apply(val));
                return this;
            }

            @Override
            public Mutability mutability() {
                return container.mutability();
            }
        };
    }

    /**
     * Specifies the way
     */
    enum Mutability {
        /**
         * Trying to mutate the container will throw an error.
         * It will not have any effect.
         */
        UNSUPPORTED(true, false),

        /**
         * Trying to modify the container
         * will fork it into a new instance with the
         * modifications applied. No error will be thrown
         * but the original instance won't be modified.
         */
        FORK(false, false),

        /**
         * Modifications will not throw an error
         * and go through to affect the original
         * instance it was called on.
         */
        MODIFY(false, true)
        ;

        // properties
        final boolean throwsError;
        final boolean modifiesInstance;

        Mutability(boolean throwsError, boolean modifiesInstance) {
            this.throwsError = throwsError;
            this.modifiesInstance = modifiesInstance;
        }

        /**
         * Will the call throw an error
         * on a container with this mutability.
         * @return If it will throw.
         */
        public boolean throwsError() {
            return throwsError;
        }

        /**
         * If a container with this mutability
         * will modify the instance it was called
         * on or fork/silently fail.
         * @return If it will modify the instance.
         */
        public boolean modifiesInstance() {
            return modifiesInstance;
        }

    }

    ////////////////////////////////////////////

    /**
     * Issue the value currently stored.
     *
     * This operation doesn't necessarily return
     * the exact currently stored value. Wrapper
     * containers may (often do) override this
     * method to lazy processing when called.
     *
     * @return The value of type {@code V}
     */
    V get();

    /**
     * Try to issue the value currently stored,
     * return a successful result containing the value
     * if successful, or failed when an error occurs or
     * the operation simply failed.
     *
     * This operation doesn't necessarily return
     * the exact currently stored value. Wrapper
     * containers may (often do) override this
     * method to lazy processing when called.
     *
     * The default implementation checks if the container
     * has a value set, if not, it returns {@link Result#unset()}.
     * Otherwise it calls {@link Container#get()} and catches any
     * errors it might throw to produce a result.
     *
     * @return The result.
     */
    default Result<V> issue() {
        // check if set
        if (!isSet())
            return Result.unset();

        try {
            // attempt to call Container#get(...)
            // and return the result as success
            return Result.success(get());
        } catch (Throwable err) {
            // catch error and return failed result
            return Result.failed(err);
        }
    }

    /**
     * Get the value if set, or return the
     * provided fallback if absent. Utilizes
     * {@link Container#isSet()} for checks.
     *
     * @param def The fallback value.
     * @return The value.
     */
    default V orElse(V def) {
        return isSet() ? get() : def;
    }

    /**
     * Get the value if set, or return the result
     * of provided fallback if absent. Utilizes
     * {@link Container#isSet()} for checks.
     *
     * @param def The fallback value supplier.
     * @return The value.
     */
    default V orElseGet(Supplier<V> def) {
        return isSet() ? get() : def.get();
    }

    /**
     * Get the value currently stored
     * casted to type {@code T}.
     *
     * @throws ClassCastException If V can not be used as T.
     * @param tClass The target type class.
     * @param <T> The target type.
     * @return The casted value.
     */
    @SuppressWarnings("unchecked")
    default <T> T getAs(Class<T> tClass) {
        V val = get();
        return (T) val;
    }

    /**
     * Get if a value is currently set.
     *
     * @return If a value is currently set.
     */
    boolean isSet();

    /**
     * Set the value stored to {@code val}.
     * This might not affect the current
     * instance and instead return a new
     * {@link Container} based on the implementation.
     *
     * @param val The value to set.
     * @return This or a new instance with the new value.
     * @throws UnsupportedOperationException If setting is unsupported.
     */
    Container<V> set(V val);

    /**
     * Get this containers mutability.
     * This may depends on the current state
     * of the container, so using an old
     * value may have unexpected effects.
     *
     * @return The active mutability settings.
     */
    Mutability mutability();

    /**
     * Get if you can await/listen a value.
     *
     * @return True/false.
     */
    default boolean canAwait() {
        return false;
    }

    /**
     * Awaits a value in this container if supported. If listen is set, it won't
     * complete early if a value is already set. Otherwise, you might get a completed future
     * if a value was already set.
     *
     * Check if it is supported using {@link Container#canAwait()}.
     * By default, it will return a completed future if possible,
     * otherwise it will throw an {@link UnsupportedOperationException}.
     *
     * @throws UnsupportedOperationException If awaiting is unsupported.
     * @param listen If it should listen for a value,
     *               or instead already complete if a
     *               value is set.
     * @return The future.
     */
    default CompletableFuture<V> await(boolean listen) {
        // check if it can be completed
        if (!listen && isSet())
            return CompletableFuture.completedFuture(get());
        // otherwise throw error
        throw new UnsupportedOperationException();
    }

    /**
     * @see Container#await(boolean)
     * {@code listen} is defaulted to false.
     */
    default CompletableFuture<V> await() {
        return await(false);
    }

    /**
     * Returns a callback which is invoked every time this container is
     * mutated.
     *
     * This only works if the container is awaitable, otherwise it will
     * throw an {@link UnsupportedOperationException} when invoking this
     * method.
     *
     * @throws UnsupportedOperationException If the container is not awaitable.
     * @return The callback.
     */
    default Callback<V> callback() {
        throw new UnsupportedOperationException();
    }

    /**
     * Clones this container (copies the value reference)
     * into a new mutable container instance.
     *
     * @return The new instance.
     */
    default Container<V> cloneMutable() {
        return mutable(get());
    }

    /* QOL Methods */

    default Container<V> asProtected(Predicate<StackTraceElement> predicate,
                                     boolean newProtected) {
        return protect(this, predicate, newProtected);
    }

    default Container<V> asForking(BiFunction<Container<V>, V, Container<V>> forkConstructor) {
        return forking(this, forkConstructor);
    }

    default Container<V> immutable() {
        return immutable(this);
    }

    default <R> Container<R> map(Function<V, R> function) {
        return Container.mapped(this, function);
    }

    default <R> Container<R> biMap(Function<V, R> toFunction,
                                   Function<R, V> fromFunction) {
        return Container.biMapped(this, toFunction, fromFunction);
    }

    default Container<V> awaitable() {
        return awaitable(this);
    }

}