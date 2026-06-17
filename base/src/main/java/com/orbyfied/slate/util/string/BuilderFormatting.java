package com.orbyfied.slate.util.string;

public abstract class BuilderFormatting {
    public StringBuilder check(StringBuilder builder) {
        return null;
    }

    public StringBuilder startLine(StringBuilder builder) {
        return startLine(builder, Strings.tailIndex(builder));
    }

    public StringBuilder endLine(StringBuilder builder) {
        return endLine(builder, Strings.tailIndex(builder));
    }

    public StringBuilder startLine(StringBuilder builder, int index) {
        return builder;
    }

    public StringBuilder endLine(StringBuilder builder, int index) {
        return startLine(builder.insert(index, "\n"));
    }
}
