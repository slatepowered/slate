package com.orbyfied.slate.util.string;

public interface ValueFormatter<T> {

    void format(FormatterContext context, StringBuilder b, T value);

}
