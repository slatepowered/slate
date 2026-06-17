package com.orbyfied.slate.util.string;

import com.orbyfied.slate.util.data.ValueContext;

public class Placeholders {

    public static Object evaluateExpression(StringReader reader, ValueContext context) {
        return context.get(reader.collect(Strings::isIdentifierCharacter));
    }

    /**
     * Evaluate all placeholders in the given string, formatted
     * using { }.
     *
     * @param format The format string.
     * @param context The value context.
     * @return The result string.
     */
    public static String evaluatePlaceholders(String format, ValueContext context) {
        StringReader reader = new StringReader(format);
        StringBuilder builder = new StringBuilder();

        while (!reader.ended()) {
            // check for backslash
            if (reader.curr() == '\\') {
                builder.append(reader.advance());
                continue;
            }

            // check current char
            if (reader.curr() == '{') {
                int startIndex = reader.index();
                reader.advance();
                Object v = evaluateExpression(reader, context);
                if (reader.curr() != '}') {
                    throw new IllegalArgumentException("Invalid expression at position " + startIndex);
                }

                reader.advance();
                builder.append(v);
            }

            // append character
            builder.append(reader.curr());
            reader.advance();
        }

        return builder.toString();
    }

}
