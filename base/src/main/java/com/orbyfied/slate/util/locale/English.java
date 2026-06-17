package com.orbyfied.slate.util.locale;

import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * English language specification (as instance) and utilities (static methods and classes).
 */
@Builder
@Getter
public class English {

    public static final English DEFAULT = English.builder()
            .build();

    public static String ordinal(int i) {
        final String[] suffixes = new String[] { "th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th" };
        return switch (i % 100) {
            case 11, 12, 13 -> i + "th";
            default -> i + suffixes[i % 10];
        };
    }

    public static class Summation {
        private final English english;
        private final @Getter List<String> items = new ArrayList<>();
        private boolean useOr;

        public Summation(English english) {
            this.english = english;
        }

        public Summation() {
            this.english = DEFAULT;
        }

        public boolean isEmpty() {
            return items.isEmpty();
        }

        public Summation add(Object item) {
            if (item != null) {
                items.add(item.toString());
            }

            return this;
        }

        public Summation add(Collection<Object> collection) {
            for (Object o : collection) {
                items.add(String.valueOf(o));
            }

            return this;
        }

        public <T> Summation add(Collection<T> collection, Function<T, String> function) {
            for (T o : collection) {
                items.add(function.apply(o));
            }

            return this;
        }

        public Summation useOr(boolean useOr) {
            this.useOr = useOr;
            return this;
        }

        public void build(StringBuilder builder) {
            for (int i = 0; i < items.size(); i++) {
                if (i == items.size() - 1 && items.size() > 1) {
                    builder.append(useOr ? " or " : " and ");
                } else if (i > 0) {
                    builder.append(", ");
                }

                builder.append(items.get(i));
            }
        }

        public String build() {
            StringBuilder b = new StringBuilder();
            build(b);
            return b.toString();
        }
    }

}
