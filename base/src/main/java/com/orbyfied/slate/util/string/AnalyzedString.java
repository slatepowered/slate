package com.orbyfied.slate.util.string;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * String wrapper which stores data about the string.
 */
public class AnalyzedString {

    @Data
    public class Line {
        final int number;     // The line number
        final int startIndex; // The flat start index into the string
        final int endIndex;   // The flat end index into the string
        final String line;    // The content of the line

        public int getColumn(int index) {
            return index - startIndex;
        }
    }

    /** The utility string reader. */
    private final StringReader reader;

    /** The string. */
    private final String string;

    public AnalyzedString(String string) {
        this.reader = new StringReader(string);
        this.string = string;
    }

    /*
        String
     */

    public String getString() {
        return string;
    }

    public int length() {
        return string.length();
    }

    public char chatAt(int i) {
        return string.charAt(i);
    }

    @Override
    public String toString() {
        return string;
    }

    /*
        Lines
     */

    private int totalLineCount = -1;
    private List<Line> lines = new ArrayList<>();

    /**
     * Performs a binary search on the list of lines for the given index.
     *
     * @param index The index.
     * @return The line number.
     */
    public Line getLineForIndex(int index) {
        if (lines.size() < 11) {
            for (Line line : lines) {
                if (index >= line.startIndex && index <= line.endIndex) {
                    return line;
                }
            }

            return null;
        }

        int s = 0; // start index of the segment
        int l = lines.size(); // length of the segment
        int i = s + l / 2; // pivot index
        while (l > 5) {
            Line line = lines.get(i);
            if (index >= line.startIndex && index <= line.endIndex) {
                return line;
            }

            // cut search space in half to lhs
            if (index < line.startIndex) {
                l = l / 2;
                i = s + l / 2;
                continue;
            }

            // cut search in half to rhs
            l = l / 2;
            s += l;
            i = s + l / 2;
        }

        for (int j = 0; j < l; j++) {
            Line line = lines.get(s + j);
            if (index >= line.startIndex && index <= line.endIndex) {
                return line;
            }
        }

        return null;
    }

    public int getTotalLineCount() {
        if (totalLineCount == -1)
            totalLineCount = Strings.countLines(string, 0, -1);
        return totalLineCount;
    }

    public Line getLine(int lineNumber) {
        reader.index(lines.isEmpty() ? 0 : lines.get(lines.size() - 1).endIndex + 1);
        for (int n = lines.size(); n <= lineNumber; n++) {
            int start = reader.index();
            String line = reader.collect(c -> c != '\n');
            int end = reader.index() - 1;
            reader.advance();
            lines.add(new Line(n, start, end, line));
        }

        return lines.get(lineNumber);
    }

    public List<Line> getAllLines() {
        reader.index(lines.isEmpty() ? 0 : lines.get(lines.size() - 1).endIndex + 1);
        for (int n = lines.size(); ; n++) {
            int start = reader.index();
            String line = reader.collect(c -> c != '\n');
            int end = reader.index() - 1;
            reader.advance();
            lines.add(new Line(n, start, end, line));

            if (reader.current() == StringReader.EOF) {
                break;
            }
        }

        return lines;
    }

    /** Completely analyze the string and store the result in memory. */
    public AnalyzedString complete() {
        getAllLines();
        return this;
    }

}