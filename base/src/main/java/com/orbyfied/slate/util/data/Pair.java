package com.orbyfied.slate.util.data;

public record Pair<X, Y>(X first, Y second) {

    public static <X, Y> Pair<X, Y> of(X x, Y y) {
        return new Pair<>(x, y);
    }

    public Pair<Y, X> reverse() {
        return new Pair<>(second, first);
    }

    public <Z> Triple<X, Y, Z> then(Z elem) {
        return new Triple<>(first, second, elem);
    }

    public X x() {
        return first;
    }

    public Y y() {
        return second;
    }

}
