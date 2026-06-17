package com.orbyfied.slate.util.functional;

/**
 * A three parameters to one result function.
 */
@FunctionalInterface
public interface TriFunction<A, B, C, R> {

    R apply(A a, B b, C c);

}
