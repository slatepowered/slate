package com.orbyfied.slate.util.exception;

import lombok.SneakyThrows;
import com.orbyfied.slate.util.reflect.UnsafeUtil;
import sun.misc.Unsafe;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

/**
 * Any exception designated to manage complex control flow instead of representing
 * an actual error.
 */
@SuppressWarnings("unchecked")
public class Interrupt extends RuntimeException {

    static final VarHandle Throwable_stackTrace;
    static final Unsafe UNSAFE = UnsafeUtil.getUnsafe();

    public static final StackTraceElement[] INTERRUPT_STACK_TRACE = new StackTraceElement[1];

    static {
        INTERRUPT_STACK_TRACE[0] = new StackTraceElement("Interrupt", "", "", 0);

        try {
            final MethodHandles.Lookup lookup = UnsafeUtil.getInternalLookup();
            Throwable_stackTrace = lookup.findVarHandle(Throwable.class, "stackTrace", StackTraceElement[].class);
        } catch (Exception ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    protected Interrupt() {
        System.err.println("Usual instantiation of an Interrupt is inefficient, use Interrupt#create(Class) instead");
    }

    public static Interrupt interrupt() {
        return create(Interrupt.class);
    }

    @SneakyThrows
    public static <I extends Interrupt> I create(Class<? super I> klass) {
        Interrupt interrupt = (Interrupt) UNSAFE.allocateInstance(klass);
        Throwable_stackTrace.set(interrupt, INTERRUPT_STACK_TRACE);
        return (I) interrupt;
    }

}
