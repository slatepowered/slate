package com.orbyfied.slate.util.string;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.function.Function;

@RequiredArgsConstructor
@Getter
public class ColumnSpec<V> {
    final String displayName;
    final Function<V, ?> valueExtractor;
    final Function<?, String> formatter;
    final Padding padding;
}
