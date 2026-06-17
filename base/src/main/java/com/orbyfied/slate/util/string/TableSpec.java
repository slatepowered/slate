package com.orbyfied.slate.util.string;

import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Builder(toBuilder = true)
@Getter
public class TableSpec<V> {
    final List<ColumnSpec<V>> columns;

    /* Implemented by Lombok */
    public static class TableSpecBuilder<V> {
        {
            if (this.columns == null) this.columns = new ArrayList<>();
        }

        public TableSpecBuilder<V> withColumn(ColumnSpec<V> column) {
            columns.add(column);
            return this;
        }

        public <R> TableSpecBuilder<V> withColumn(String displayName, Function<V, R> valueExtractor, Function<R, String> formatter, Padding padding) {
            columns.add(new ColumnSpec<>(displayName, valueExtractor, (Function<Object, String>) formatter, padding));
            return this;
        }
    }
}
