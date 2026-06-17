package com.orbyfied.slate.util.data;

import java.lang.ref.Reference;
import java.util.Optional;

public class Optionals {

    public static <T> Optional<T> ofNullableReference(Reference<T> ref) {
        return ref != null ? Optional.ofNullable(ref.get()) : Optional.empty();
    }

}
