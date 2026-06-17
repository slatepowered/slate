package com.orbyfied.slate.util.data;


public record Triple<X, Y, Z>(X first, Y second, Z third) {
    public static <X, Y, Z> Triple<X, Y, Z> of(X x, Y y, Z z) {
        return new Triple<>(x, y, z);
    }

    public Pair<Y, Z> yz() {
        return Pair.of(second, third);
    }

    public Pair<X, Y> xy() {
        return Pair.of(first, second);
    }

    public Pair<X, Z> xz() {
        return Pair.of(first, third);
    }

    public Triple<Z, Y, X> reverse() {
        return new Triple<>(third, second, first);
    }

    public X x() {
        return first;
    }

    public Y y() {
        return second;
    }

    public Z z() {
        return third;
    }
}
