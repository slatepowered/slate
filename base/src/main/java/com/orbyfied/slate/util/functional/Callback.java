package com.orbyfied.slate.util.functional;

import com.orbyfied.slate.util.collection.Placement;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A callable function that can be
 * externally handled. This can be
 * invoked both with or without a value.
 *
 * @param <V> The value type.
 */
public interface Callback<V> extends Callable<V> {

    /**
     * Allows one handler and the same
     * future to be in use at a time.
     *
     * @param <V> The value type.
     * @return The callback.
     */
    static <V> Callback<V> mono() {
        return new Callback<V>() {
            Function<V, HandlerResult> consumer;
            CompletableFuture<V> future;

            @Override
            public Callback<V> thenApply(Function<V, HandlerResult> consumer) {
                this.consumer = consumer;
                return this;
            }

            @Override
            public Callback<V> thenApply(Function<V, HandlerResult> handler, Placement placement) {
                this.consumer = handler;
                return this;
            }

            @Override
            public CompletableFuture<V> await() {
                return future != null ? future : (future = new CompletableFuture<>());
            }

            @Override
            public void call(V value) {
                if (consumer != null)
                    if (consumer.apply(value) == HandlerResult.REMOVE)
                        consumer = null;
                if (future != null)
                    future.complete(value);
            }
        };
    }

    /**
     * Allows multiple handlers and futures
     * to be in use at the same time.
     *
     * @param <V> The value type.
     * @return The callback.
     */
    static <V> Callback<V> multi() {
        return new Callback<V>() {
            // the handlers
            List<Function<V, HandlerResult>> consumers = new ArrayList<>();
            // the futures
            List<CompletableFuture<V>> futures = new ArrayList<>();

            @Override
            public Callback<V> thenApply(Function<V, HandlerResult> handler) {
                consumers.add(handler);
                return this;
            }

            @Override
            public Callback<V> thenApply(Function<V, HandlerResult> handler, Placement placement) {
                placement.insert(consumers, handler);
                return this;
            }

            @Override
            public CompletableFuture<V> await() {
                CompletableFuture<V> future = new CompletableFuture<>();
                futures.add(future);
                return future;
            }

            @Override
            public void call(V value) {
                if (!consumers.isEmpty()) {
                    Function<V, HandlerResult> handler = consumers.get(0);
                    for (Iterator<Function<V, HandlerResult>> it = consumers.listIterator(); it.hasNext(); handler = it.next())
                        if (handler.apply(value) == HandlerResult.REMOVE)
                            it.remove();
                }

                {
                    final int l = futures.size();
                    for (int i = 0; i < l; i++)
                        futures.get(i).complete(value);
                    futures.clear();
                }
            }
        };
    }

    /////////////////////////////////////////

    /**
     * Register a handler for the value
     * when called. This may replace an
     * existing handler or append a new
     * one to the end of the pipeline
     * depending on the implementation.
     *
     * @param consumer The handler.
     * @return This.
     */
    default Callback<V> then(Consumer<V> consumer) {
        return thenApply(v -> {
            consumer.accept(v);
            return HandlerResult.KEEP;
        });
    }

    /**
     * Register a handler for the value
     * when called. This may replace an
     * existing handler or append a new
     * one to the end of the pipeline
     * depending on the implementation.
     *
     * @param consumer The handler.
     * @param placement Determines the position to register the handler at.
     * @return This.
     */
    default Callback<V> then(Consumer<V> consumer, Placement placement) {
        return thenApply(v -> {
            consumer.accept(v);
            return HandlerResult.KEEP;
        }, placement);
    }

    /**
     * Register a handler for the value
     * when called. This may replace an
     * existing handler or append a new
     * one to the end of the pipeline
     * depending on the implementation.
     *
     * @param handler The handler.
     * @return This.
     */
    Callback<V> thenApply(Function<V, HandlerResult> handler);

    /**
     * Register a handler for the value
     * when called. This may replace an
     * existing handler or append a new
     * one to the end of the pipeline
     * depending on the implementation.
     *
     * @param handler The handler.
     * @param placement Determines the position to register the handler at.
     * @return This.
     */
    Callback<V> thenApply(Function<V, HandlerResult> handler, Placement placement);

    /**
     * Await a call by accepting an
     * {@link CompletableFuture}.
     *
     * @return The future.
     */
    CompletableFuture<V> await();

}