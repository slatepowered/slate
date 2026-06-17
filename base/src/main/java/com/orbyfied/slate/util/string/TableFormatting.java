package com.orbyfied.slate.util.string;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.function.Function;

@Builder(toBuilder = true)
@Getter
public class TableFormatting {
    final boolean showHeader;
    final boolean showRowCount;

    /**
     * Format a table with rows and columns according to the given
     * table specification, formatting and data.
     *
     * @param b The builder.
     * @param builderFormatting The formatting spec to use for the string builder.
     * @param spec The table spec.
     * @param formatting The table formatting settings.
     * @param rows The row data.
     */
    public static <V> void formatTable(StringBuilder b, BuilderFormatting builderFormatting, TableSpec<V> spec, TableFormatting formatting, List<? extends V> rows) {
        final List<ColumnSpec<V>> columns = spec.getColumns();
        final int columnCount = columns.size();
        final int rowCount = rows.size();

        // pre-render all values
        String[][] renderedValues = new String[rows.size()][columns.size()];
        int[] maxLength = new int[columns.size()];

        for (int i = 0; i < rowCount; i++) {
            V elem = rows.get(i);
            String[] renderedColumns = renderedValues[i];

            for (int j = 0; j < columnCount; j++) {
                ColumnSpec<V> col = columns.get(j);
                Object value = col.getValueExtractor().apply(elem);
                String str = ((Function<Object, String>)col.getFormatter()).apply(value);

                renderedColumns[j] = str;
                if (str.length() > maxLength[j]) {
                    maxLength[j] = str.length();
                }
            }
        }

        if (formatting.isShowHeader()) {
            // draw table header
            builderFormatting.startLine(b);
            for (int j = 0; j < columnCount; j++) {
                String columnHeader = columns.get(j).displayName;
                if (j != 0) {
                    b.append(" | ");
                } else if (formatting.isShowRowCount()) {
                    columnHeader = "(" + rowCount + ") " + columnHeader;
                }

                if (columnHeader.length() > maxLength[j]) maxLength[j] = columnHeader.length();
                b.append(Strings.pad(columnHeader, maxLength[j], ' ', columns.get(j).padding));
            }

            b.append("\n");
            builderFormatting.startLine(b);
            for (int j = 0; j < columnCount; j++) {
                if (j != 0) b.append("-+-");
                b.append("-".repeat(maxLength[j]));
            }

            b.append("\n");
        }

        // draw table rows
        for (int i = 0; i < rowCount; i++) {
            if (i != 0) b.append("\n");
            builderFormatting.startLine(b);

            V elem = rows.get(i);
            String[] renderedColumns = renderedValues[i];

            for (int j = 0; j < columnCount; j++) {
                ColumnSpec<V> col = columns.get(j);
                if (j != 0) b.append(" | ");
                b.append(Strings.pad(renderedColumns[j], maxLength[j], ' ', col.padding));
            }
        }
    }
}
