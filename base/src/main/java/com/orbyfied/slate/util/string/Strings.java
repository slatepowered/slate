package com.orbyfied.slate.util.string;

import com.orbyfied.slate.util.data.Pair;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.function.Function;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class Strings {

    public static String repeat(String str, int times) {
        StringBuilder b = new StringBuilder(str.length() * times);

        for(int i = 0; i < times; ++i) {
            b.append(str);
        }

        return b.toString();
    }

    public static <T> String join(List<T> list, String delimiter) {
        StringBuilder b = new StringBuilder();
        int l = list.size();

        for(int i = 0; i < l; ++i) {
            Object o = list.get(i);
            if (i != 0) {
                b.append(delimiter);
            }

            b.append(o);
        }

        return b.toString();
    }

    public static int countLines(String str, int start, int end) {
        if (end == -1) {
            end = str.length() - 1;
        }

        int end2 = Math.min(str.length() - 1, end);
        int count = 0;

        int i;
        for(i = start; i <= end2; ++i) {
            char c = str.charAt(i);
            if (c == '\n') {
                ++count;
            }
        }

        if (i == str.length()) {
            ++count;
        }

        return count;
    }

    public static String stringifyRange(Pair<Integer, Integer> range) {

        if (range.first() == Integer.MIN_VALUE) return range.second() + "-";
        if (range.second() == Integer.MAX_VALUE) return range.first() + "+";
        return range.first() + " - " + range.second();
    }

    public static <V> String join(List<V> list, Function<V, String> function, String separator) {
        if (list.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        builder.append(function.apply(list.get(0)));
        for (int i = 1; i < list.size(); i++) {
            builder.append(separator).append(function.apply(list.get(i)));
        }

        return builder.toString();
    }

    public static <V> String joinListEnglish(List<V> list, Function<V, String> function) {
        if (list.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder();
        builder.append(function.apply(list.get(0)));
        for (int i = 1; i < list.size(); i++) {
            if (i == list.size() - 1) builder.append(" or ");
            else builder.append(", ");
            builder.append(function.apply(list.get(i)));
        }

        return builder.toString();
    }

    public static String padEnd(Object val, int targetLength, char c) {
        String str = String.valueOf(val);
        if (str.length() < targetLength) {
            str = str + String.valueOf(c).repeat(targetLength - str.length());
        }

        return str;
    }

    public static String padStart(Object val, int targetLength, char c) {
        String str = String.valueOf(val);
        if (str.length() < targetLength) {
            str = String.valueOf(c).repeat(targetLength - str.length()) + str;
        }

        return str;
    }

    public static String padMid(Object val, int targetLength, char c) {
        String str = String.valueOf(val);
        if (str.length() < targetLength) {
            str = String.valueOf(c).repeat((targetLength - str.length()) / 2) + str + String.valueOf(c).repeat((targetLength - str.length()) / 2);
        }

        return str;
    }

    public static String pad(Object val, int targetLength, char c, Padding padding) {
        return switch (padding) {
            case LEFT -> padEnd(val, targetLength, c);
            case RIGHT -> padEnd(val, targetLength, c);
            case MIDDLE -> padStart(val, targetLength, c);
        };
    }

    public static String editAllLines(String block, Function<String, String> lineConsumer) {
        StringBuilder b = new StringBuilder();
        String[] lines = block.split("\n");
        for (int i = 0; i < lines.length; i++) {
            if (i != 0) b.append("\n");
            b.append(lineConsumer.apply(lines[i]));
        }

        return b.toString();
    }

    public static Writer stringBuilderWriter(StringBuilder b) {
        return new Writer() {
            @Override
            public void write(char[] cbuf, int off, int len) throws IOException {
                b.append(cbuf, off, len);
            }

            @Override public void flush() throws IOException { }
            @Override public void close() throws IOException { }
        };
    }

    public static Writer stringBuilderWriter(BuilderFormatting formatting, StringBuilder b) {
        if (formatting == null) {
            return stringBuilderWriter(b);
        }

        return new Writer() {
            @Override
            public void write(char[] cbuf, int off, int len) throws IOException {
                int lastIndex = off + len;
                int j = off;
                for (int i = off; i <= lastIndex; i++) {
                    if (cbuf[i] == '\n') {
                        b.append(cbuf, j, i);
                        formatting.endLine(b);
                        i++;
                        j = i;
                    }
                }

                if (j != lastIndex) {
                    b.append(cbuf, j, lastIndex);
                }

                formatting.check(b);
            }

            @Override public void flush() throws IOException { }
            @Override public void close() throws IOException { }
        };
    }

    public static boolean isIdentifierCharacter(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '_' || c == '-';
    }

    public static boolean isIdentifierCharacterLenient(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9') || c == '_' || c == '-' || c == '.' || c == '/';
    }

    public static void consumeWhitespaceExceptNewLine(StringReader reader) {
        reader.consume(c -> Character.isWhitespace(c) && c != '\n');
    }

    public static int tailIndex(StringBuilder builder) {
        return builder.length();
    }

    public static <V> Function<V, String> orEmptyIfNull(Function<V, String> formatter) {
        return o -> o != null ? formatter.apply(o) : "";
    }

    public static <V> Function<V, String> orDefaultIfNull(Function<V, String> formatter, String def) {
        return o -> o != null ? formatter.apply(o) : def;
    }

}
