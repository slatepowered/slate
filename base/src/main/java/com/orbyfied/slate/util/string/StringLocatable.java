package com.orbyfied.slate.util.string;

/**
 * An object which can have a location in a string.
 */
public interface StringLocatable {

    StringLocation location();
    StringLocatable located(StringLocation location);

}