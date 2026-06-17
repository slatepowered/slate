package com.orbyfied.slate.util.string;

import lombok.Getter;

/**
 * Represents a location in a string.
 *
 * It contains the (virtual) file name, the string
 * and the start and end indices.
 */
public class StringLocation implements StringLocatable {

    /**
     * The file this location appears in.
     */
    @Getter
    String file;

    /*
     * The analyzed string object as source of this location.
     */
    @Getter
    AnalyzedString string;

    /* Both the start and end indices are inclusive. */
    @Getter
    int startIndex;
    @Getter
    int endIndex;

    /* The start and end line numbers. */
    int startLine;
    int endLine = -1;

    public StringLocation(String file, AnalyzedString str, int startIndex, int endIndex, int startLine) {
        this.file = file;
        this.string = str;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.startLine = startLine;
    }

    public StringLocation(String file, AnalyzedString str, int startIndex, int endIndex, int startLine, int endLine) {
        this.file = file;
        this.string = str;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.startLine = startLine;
        this.endLine = endLine;
    }

    public int getStartLineNumber() {
        return startLine;
    }

    public int getEndLineNumber() {
        if (endLine == -1) {
            // calculate
            endLine = startLine + Strings.countLines(string.getString(), startIndex, endIndex);
        }

        return endLine;
    }

    public int getStartColumn() {
        return startIndex - string.getLine(startLine).getStartIndex();
    }

    public int getEndColumn() {
        return endIndex - string.getLine(getEndLineNumber()).getEndIndex();
    }

    @Override
    public String toString() {
        return "StringLocation(" +
                "file='" + file + '\'' +
                ", string='" + string + '\'' +
                ", startIndex=" + startIndex +
                ", endIndex=" + endIndex +
                ')';
    }

    // creates an ansi code from the given
    // format and then returns it escaped if the
    // boolean is true
    private static String ac(String c, boolean fmt) {
        if (!fmt)
            return "";
        return "\u001B[" + c + "m";
    }

    public String toStringFormatted(boolean fmt, int indent) {
        final String str = string.getString();
        final int surround = 10;

        final String indentStr = " ".repeat(indent);
        StringBuilder b = new StringBuilder(ac("31", fmt) + indentStr);

        AnalyzedString.Line startLine = string.getLine(getStartLineNumber());
        AnalyzedString.Line endLine   = string.getLine(getEndLineNumber());

        int startLineIdx = startLine.getColumn(startIndex);
        int endLineIdx   = endLine.getColumn(endIndex);

        // append info
        if (file != null)
            b.append(ac("1", fmt)).append("in file ").append(ac("0", fmt)).append(file).append(", ");
        b.append(startLine.getNumber()).append(":").append(startLineIdx);
        b.append(ac("1", fmt)).append(ac("31", fmt)).append(" to ").append(ac("0", fmt));
        b.append(endLine.getNumber()).append(":").append(endLineIdx).append(ac("0", fmt)).append("\n");

        // append lines
        if (endLine == startLine) {
            b.append(indentStr);
            b.append(ac("90", fmt)).append(ac("1", fmt))
                    .append(startLine.getNumber()).append(" | ").append(ac("0", fmt));

            String lStr = startLine.getLine();

            int sIdx = startLineIdx - surround;
            b.append(ac("90", fmt));
            b.append(sIdx <= 0 ? "" : "... ");
            b.append(lStr, Math.max(0, sIdx), startLineIdx);

            b.append(ac("31", fmt)).append(ac("4", fmt));
            b.append(lStr, startLineIdx, endLineIdx + 1);
            b.append(ac("0", fmt)).append(ac("90", fmt));

            int eIdx = endLineIdx + surround;
            b.append(ac("90", fmt));
            b.append(lStr, endLineIdx + 1, Math.min(lStr.length() - 1, eIdx));
            b.append(eIdx >= lStr.length() ? "" : " ...");
        } else {
            // todo: cant be fucking bothered fuck this shit
        }

        return b.toString();
    }

    @Override
    public StringLocation location() {
        return this;
    }

    @Override
    public StringLocation located(StringLocation location) {
        this.startIndex = location.startIndex;
        this.endIndex = location.endIndex;
        this.file = location.file;
        this.string = location.string;
        return this;
    }

}