package com.orbyfied.slate.util.concurrent;

import com.orbyfied.slate.util.exception.CallTrace;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class Futures {

    /**
     * Adds a generic uncaught error handler to the given future and returns it. The error handler will
     * log/report any error to `System.err`. Additionally, in the formatting the stack trace of the call to this
     * method will be logged, to improve caller tracing.
     * <br>
     * This method is only meant to be used semi-exclusively,
     * 1. on futures without intended or present exception handling, as the only purpose
     *    of this method is to report errors exclusively to the developer through the console.
     * 2. by the initial creator of the future to best fulfill its purpose of caller tracing.
     * <br>
     * Additionally, the initialization of the caller stack trace has to happen on every call
     * to this method and is relatively expensive, therefor it is not recommended to use this method in
     * performance sensitive applications.
     *
     * @param future The future without.
     * @return The future including the reporting mechanic.
     * @param <T> The completion value type.
     */
    public static <T> CompletableFuture<T> trace(CompletableFuture<T> future) {
        final CallTrace callTrace = new CallTrace();
        return future.whenComplete((__, thr) -> {
            if (thr == null) {
                return;
            }

            try {
                StringBuilder b = new StringBuilder();
                b.append("An uncaught ").append(thr.getClass().getSimpleName()).append(" occurred in a future invoked using Futures.report");

                StackTraceElement[] callerTrace = callTrace.getStackTrace();
                int firstCallerFrame = 0;
                for (; firstCallerFrame < callerTrace.length; firstCallerFrame++) {
                    StackTraceElement element = callerTrace[firstCallerFrame];
                    if (!element.getMethodName().equalsIgnoreCase("report")) {
                        break;
                    }
                }

                CallTrace.formatStackTrace(callTrace, b, CALL_TRACE_FORMATTING, firstCallerFrame);

                if (thr instanceof CompletionException completionException) {
                    thr = completionException.getCause();
                }

                if (!b.toString().trim().endsWith("\n")) {
                    b.append("\n");
                }

                b.append("Cause: ");
                CallTrace.formatStackTrace(thr, b, EXC_FORMATTING);
                System.err.println(b);
            } catch (Throwable t) {
                System.err.println("An error occurred while reporting exceptional completion of future lol ?");
                t.printStackTrace();

                thr.printStackTrace();
                callTrace.printStackTrace();
            }
        });
    }

    /**
     * Adds a generic uncaught error handler to the given future and returns it. The error handler will
     * log/report any error to `System.err`.
     * <br>
     * This method is only meant to be used semi-exclusively,
     * 1. on futures without intended or present exception handling, as the only purpose
     *    of this method is to report errors exclusively to the developer through the console.
     *
     * @param future The future without.
     * @return The future including the reporting mechanic.
     * @param <T> The completion value type.
     */
    public static <T> CompletableFuture<T> report(CompletableFuture<T> future) {
        return future.whenComplete((__, thr) -> {
            if (thr == null) {
                return;
            }

            try {
                StringBuilder b = new StringBuilder();
                b.append("An uncaught ").append(thr.getClass().getSimpleName()).append(" occurred in a future invoked using Futures.report");

                if (thr instanceof CompletionException completionException) {
                    thr = completionException.getCause();
                }

                if (!b.toString().trim().endsWith("\n")) {
                    b.append("\n");
                }

                b.append("Cause: ");
                CallTrace.formatStackTrace(thr, b, EXC_FORMATTING);
                System.err.println(b);
            } catch (Throwable t) {
                System.err.println("An error occurred while reporting exceptional completion of future lol ?");
                t.printStackTrace();

                thr.printStackTrace();
            }
        });
    }

    /**
     * Waits for all given futures to complete, either successfully or exceptionally, then invokes
     * the appropriate action on the returned future. If no futures are provided, a completed future
     * with an empty list is returned.
     * <br>
     * Upon successful completion of all futures, the compound future is invoked with a list of
     * the completion value of every future.
     * <br>
     * If a future completes exceptionally, the returned future is immediately invoked exceptionally
     * and any further completions will be logged, but not propagated.
     * <br>
     * The returned future is always invoked synchronously on the same thread of the finalizing future,
     * meaning either the last future to complete or the first future to throw an exception.
     *
     * @param futures The futures to combine.
     * @return The compound future.
     * @param <T> The value type.
     */
    public static <T> CompletableFuture<List<? extends T>> allOf(List<? extends CompletableFuture<? extends T>> futures) {
        final int size = futures.size();
        if (size == 0) {
            return CompletableFuture.completedFuture(new ArrayList<>());
        }

        CompletableFuture<List<? extends T>> compoundFuture = new CompletableFuture<>();
        ArrayList<T> list = new ArrayList<>();
        Object errorLock = new Object();

        for (CompletableFuture<? extends T> future : futures) {
            future.whenComplete((t, throwable) -> {
                if (throwable != null) {
                    synchronized (errorLock) {
                        if (!compoundFuture.isDone()) {
                            compoundFuture.completeExceptionally(throwable);
                        }

//                        throwable.printStackTrace();
                    }

                    return;
                }

                synchronized (list) {
                    list.add(t);
                    if (list.size() >= size) {
                        compoundFuture.complete(list);
                    }
                }
            });
        }

        return compoundFuture;
    }

    private static final CallTrace.CallTraceFormatting EXC_FORMATTING = CallTrace.CallTraceFormatting.builder()
            .linePrefix("  ")
            .trimJavaConcurrent(CallTrace.TrimStyle.REMOVE)
            .minTraceLength(5)
            .maxTraceLength(999)
            .build();
    private static final CallTrace.CallTraceFormatting CALL_TRACE_FORMATTING = CallTrace.CallTraceFormatting.builder()
            .linePrefix("  ")
            .printExceptionMessage(false)
            .maxTraceLength(2)
            .addDots(false)
            .build();

}
