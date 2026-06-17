package com.orbyfied.slate.util.concurrent;

import com.orbyfied.slate.util.functional.ThrowingRunnable;
import com.orbyfied.slate.util.functional.ThrowingSupplier;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * An object which can manage scheduled tasks, note that it does not
 * provide execution or scheduling services.
 */
public interface TaskManager {

    default void startedTask(Object taskReference) {

    }

    default void finishedTask(Object taskReference) {

    }

    default void onError(Object taskReference, Throwable error) {

    }

    default CompletableFuture<Void> dispatchAsync(ThrowingRunnable runnable, TaskExecutor executor) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        startedTask(future);
        executor.schedule(this, () -> {
            try {
                runnable.run();
                future.complete(null);
                finishedTask(future);
            } catch (Exception ex) {
                onError(future, ex);
            }
        });

        return future;
    }

    default CompletableFuture<Void> dispatchAsync(ThrowingRunnable runnable, long delay, TimeUnit unit) {
        return dispatchAsync(runnable, TaskExecutor.delayed(Async.EXECUTOR, delay, unit));
    }

    default CompletableFuture<Void> dispatchAsync(ThrowingRunnable runnable) {
        return dispatchAsync(runnable, TaskExecutor.SUBMIT_DEFAULT);
    }

    default <T> CompletableFuture<T> dispatchAsync(ThrowingSupplier<T> supplier, TaskExecutor executor) {
        CompletableFuture<T> future = new CompletableFuture<>();
        startedTask(future);
        executor.schedule(this, () -> {
            try {
                future.complete(supplier.get());
                finishedTask(future);
            } catch (Exception ex) {
                future.completeExceptionally(ex);
                onError(future, ex);
            }
        });

        return future;
    }

    default <T> CompletableFuture<T> dispatchAsync(ThrowingSupplier<T> supplier, long delay, TimeUnit unit) {
        return dispatchAsync(supplier, TaskExecutor.delayed(Async.EXECUTOR, delay, unit));
    }

    default <T> CompletableFuture<T> dispatchAsync(ThrowingSupplier<T> supplier) {
        return dispatchAsync(supplier, TaskExecutor.SUBMIT_DEFAULT);
    }

    default <T> CompletableFuture<T> dispatchAsyncComposed(ThrowingSupplier<CompletableFuture<T>> supplier, TaskExecutor executor) {
        CompletableFuture<T> future = new CompletableFuture<>();
        startedTask(future);
        executor.schedule(this, () -> {
            try {
                var ret = supplier.get();
                if (ret == null) {
                    future.complete(null);
                    finishedTask(future);
                    return;
                }

                ret.whenComplete((v, t) -> {
                    if (t != null) {
                        onError(future, t);
                        return;
                    }

                    future.complete(v);
                    finishedTask(future);
                });
            } catch (Exception ex) {
                onError(future, ex);
            }
        });

        return future;
    }

    default <T> CompletableFuture<T> dispatchAsyncComposed(ThrowingSupplier<CompletableFuture<T>> supplier, long delay, TimeUnit unit) {
        return dispatchAsyncComposed(supplier, TaskExecutor.delayed(Async.EXECUTOR, delay, unit));
    }

    default <T> CompletableFuture<T> dispatchAsyncComposed(ThrowingSupplier<CompletableFuture<T>> supplier) {
        return dispatchAsyncComposed(supplier, TaskExecutor.SUBMIT_DEFAULT);
    }

}
