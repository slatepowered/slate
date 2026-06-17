package com.orbyfied.slate.util.string;

import lombok.Builder;

@Builder
public class StandardBuilderFormatting extends BuilderFormatting {

    // The line prefix to use
    final String linePrefix;
    // The amount of characters per line to target, if this is 0 or less word wrap will be disabled
    final int targetCharsPerLine;

    @Override
    public StringBuilder check(StringBuilder builder) {
        /* process word wrap */
        if (targetCharsPerLine > 0) {
            // find last new line
            int i = builder.length() - 1;
            for (; i >= 0; i--) {
                if (builder.charAt(i) == '\n') {
                    break;
                }
            }

            int count = builder.length() - i - 1;
            if (count > targetCharsPerLine) {
                // find first wrap boundary
                for (i = Math.max(1, i + targetCharsPerLine); i < builder.length(); i++) {
                    if (isWrapUnitBoundary(builder.charAt(i - 1), builder.charAt(i))) {
                        break;
                    }
                }

                // insert new line
                endLine(builder, i);
            }
        }

        return builder;
    }

    @Override
    public StringBuilder startLine(StringBuilder builder, int index) {
        return builder.insert(index, linePrefix);
    }

    // Only applies word wrap when this function returns true
    protected boolean isWrapUnitBoundary(char a, char b) {
        return a == ' ' || b == ' ' || !Character.isLetterOrDigit(a) || !Character.isLetterOrDigit(b); // todo
    }

}
