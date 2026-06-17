package com.orbyfied.slate.util.exception;

import lombok.Builder;
import lombok.Getter;

public class CallTrace extends RuntimeException {

    public static final CallTraceFormatting STANDARD_FORMATTING = CallTraceFormatting.builder()
            .build();

    public enum TrimStyle {
        NONE,
        REMOVE,
        INDICATE
        ;
    }

    @Getter
    @Builder(toBuilder = true, builderClassName = "Builder")
    public static class CallTraceFormatting {
        final boolean ansiFormatted;

        final boolean printExceptionMessage;

        final String linePrefix;
        final String stackTraceElementPrefix;
        final String classNamePrefix;
        final String exceptionMessagePrefix;
        final String directivePrefix;
        final String continueSuffix;

        final int minTraceLength;
        final int maxTraceLength;
        final int topLevelExceptionTraceLength;
        final TrimStyle trimJavaConcurrent;
        final boolean addDots;

        public String ansiReset() {
            return ansiFormatted ? "" /* todo */ : "";
        }

        public static class Builder {
            {
                this.printExceptionMessage = true;
                this.linePrefix = "";
                this.stackTraceElementPrefix = "";
                this.classNamePrefix = "";
                this.exceptionMessagePrefix = "";
                this.directivePrefix = "";
                this.continueSuffix = "";
                this.maxTraceLength = Integer.MAX_VALUE;
                this.topLevelExceptionTraceLength = 0;
                this.trimJavaConcurrent = TrimStyle.NONE;
                this.minTraceLength = 0;
                this.addDots = true;
            }
        }
    }

    public static void formatStackTrace(Throwable t, StringBuilder builder, CallTraceFormatting formatting) {
        formatStackTrace(t, builder, formatting, 0);
    }

    /**
     * Format the stack trace of the given throwable.
     */
    public static void formatStackTrace(Throwable t, StringBuilder builder, CallTraceFormatting formatting, int startFrame) {
        Throwable cause = t.getCause();

        if (formatting.isPrintExceptionMessage()) {
            builder.append(formatting.classNamePrefix).append(t.getClass().getName());
            if (t.getMessage() != null) {
                builder.append(": ").append(formatting.exceptionMessagePrefix).append(t.getMessage());
            }
        }

        builder.append(formatting.ansiReset());
        StackTraceElement[] trace = t.getStackTrace();
        int maxCount = (cause != null && formatting.topLevelExceptionTraceLength != 0) ?
                formatting.topLevelExceptionTraceLength :
                formatting.getMaxTraceLength();
        int endExcl = Math.min(maxCount, trace.length) + startFrame;
        for (int i = startFrame; i < endExcl; i++) {
            StackTraceElement element = trace[i];
            TrimStyle trimStyle;

            if (i < formatting.minTraceLength) {
                // determine trimming
                if (element.getClassName().startsWith("java.util.concurrent") && (trimStyle = formatting.trimJavaConcurrent) != TrimStyle.NONE) {
                    // todo: trim styles? idk im over engineering
                    //  simply exclude it for now
                    continue;
                }
            }

            // append element
            builder.append("\n").append(formatting.linePrefix).append("   ").append(formatting.directivePrefix).append("at ")
                    .append(formatting.stackTraceElementPrefix).append(element.toString()).append(formatting.ansiReset());
        }

        if (endExcl < trace.length && formatting.addDots) {
            if (cause == null) {
                builder.append("\n").append(formatting.linePrefix).append("   ").append(formatting.directivePrefix);
            } else {
                builder.append(formatting.continueSuffix);
            }

            builder.append(" ").append("...").append("\n");
        }

        builder.append(formatting.ansiReset());
        if (cause != null) {
            builder.append("\n");
            builder.append(formatting.linePrefix).append(formatting.directivePrefix).append("Caused By: ");
            formatStackTrace(cause, builder, formatting, 0);
        }
    }

}
