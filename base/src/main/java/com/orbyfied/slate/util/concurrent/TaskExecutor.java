package com.orbyfied.slate.util.concurrent;

import com.orbyfied.slate.util.concurrent.TaskManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@FunctionalInterface
public interface TaskExecutor {

    boolean schedule(TaskManager manager, Runnable runnable);

    static TaskExecutor of(Consumer<Runnable> consumer) {
        return (m, r) -> {
            consumer.accept(r);
            return true;
        };
    }

    static TaskExecutor submit(ExecutorService executorService) {
        return (m, r) -> {
            executorService.submit(r);
            return true;
        };
    }

    static TaskExecutor delayed(ScheduledExecutorService executorService, long delay, TimeUnit unit) {
        return (m, r) -> {
            executorService.schedule(r, delay, unit);
            return true;
        };
    }

    TaskExecutor SUBMIT_DEFAULT = submit(Async.EXECUTOR);
    TaskExecutor SYNC = (manager, runnable) -> { runnable.run(); return true; };

}
