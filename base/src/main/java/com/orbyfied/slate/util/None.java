package com.orbyfied.slate.util;

/**
 * Denotes no kind.
 */
public interface None {
    Class<None> TYPE = None.class;

    None NONE = new None() {
        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public String toString() {
            return "None";
        }
    };
}
