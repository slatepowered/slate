package com.orbyfied.slate.util.concurrent;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import com.orbyfied.slate.util.None;
import com.orbyfied.slate.util.concurrent.TaskManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A basic dynamic lock implementation keyed by the given context type.
 *
 * @param <C> The context type.
 */
@SuppressWarnings("unchecked")
@RequiredArgsConstructor
public class Lock<C> {

    final Class<C> contextClass;

    /**
     * The global lock key for a lock instance.
     */
    public static final Object GLOBAL_LOCK_KEY = new Object();

    /**
     * Marker which locks a specific key.
     */
    public static final Object LOCKED = new Object();

    final ConcurrentHashMap<C, Object> map = new ConcurrentHashMap<>();

    public boolean isLockedExact(C context) {
        return map.get(context) == LOCKED;
    }

    public Session lockExact(C context) {
        if (map.get(context) == LOCKED) {
            throw new IllegalStateException("Context `" + context + "` is already locked");
        }

        map.put(context, LOCKED);
        return new Session(context);
    }

    public C toContext(Object contextProvider) {
        if (contextProvider == null) {
            return (C) GLOBAL_LOCK_KEY;
        }

        if (contextClass.isInstance(contextProvider)) {
            return (C) contextProvider;
        }

        C ctx = retrieveContext(contextProvider);
        if (ctx == null) {
            throw new IllegalArgumentException("Object of type " + contextProvider.getClass().getName() + " is not a valid context provider for lock with context " + contextClass);
        }

        return ctx;
    }

    protected C retrieveContext(Object contextProvider) {
        return null;
    }

    public boolean isLocked(Object contextProvider) {
        return isLockedExact(toContext(contextProvider));
    }

    public Session lock(Object contextProvider) {
        return lockExact(toContext(contextProvider));
    }

    @RequiredArgsConstructor
    public class Session implements TaskManager {
        final @Getter C context;
        final AtomicBoolean completedDispatch = new AtomicBoolean(true);
        final AtomicInteger dispatchCounter = new AtomicInteger(0);
        final CompletableFuture<Session> completionFuture = new CompletableFuture<>();
        final @Getter List<Throwable> errors = new ArrayList<>();

        public boolean isGlobal() {
            return context == GLOBAL_LOCK_KEY;
        }

        /**
         * Get the completion future invoked **before** release of the lock for this context.
         */
        public CompletableFuture<Session> future() {
            return completionFuture;
        }

        public synchronized void completed() {
            completionFuture.complete(this);
            unlock();
        }

        public void unlock() {
            map.remove(context);
        }

        @Override
        public void startedTask(Object taskRef) {
            dispatchCounter.incrementAndGet();
        }

        @Override
        public void finishedTask(Object taskRef) {
            if (dispatchCounter.decrementAndGet() <= 0) {
                if (completedDispatch.get()) {
                    completed();
                }
            }
        }

        @Override
        public void onError(Object taskRef, Throwable thr) {
            synchronized (errors) {
                errors.add(thr);
            }

            finishedTask(taskRef);
        }

        public void beginDispatch() {
            completedDispatch.set(false);
        }

        public void completedDispatch() {
            completedDispatch.set(true);
            if (dispatchCounter.get() <= 0) {
                completed();
            }
        }
    }

    public static Lock<None> global() {
        return new Lock<>(None.class) {
            @Override
            public None toContext(Object contextProvider) {
                return null;
            }
        };
    }

}
