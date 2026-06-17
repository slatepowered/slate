package com.orbyfied.slate.util.exception;

/**
 * Utilities for dealing with throwables.
 */
public class Throwables {

    // static utility class
    // no instantiating
    private Throwables() { }

    /**
     * Allows sneakily throwing an exception: This
     * means any exception can be thrown without
     * having to declare it in the method declaration.
     * Useful for rethrowing exceptions.
     *
     * @param t The throwable to throw.
     * @param <T> The throwable type.
     * @throws T The throwable.
     */
    @SuppressWarnings("unchecked")
    public static <T extends Throwable> void sneakyThrow(Throwable t) throws T {
        throw (T) t;
    }

}