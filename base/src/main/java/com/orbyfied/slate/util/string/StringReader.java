package com.orbyfied.slate.util.string;

import lombok.Data;

import java.io.PrintStream;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A class to help with reading/parsing
 * strings.
 *
 * This includes basic methods for traversing
 * a string, parsing helpers and iterator/stream
 * providers.
 */
public class StringReader implements Iterable<Character> {

    // the digit table array thing
    private static final int[] DIGIT_TABLE = new int[256];

    static {
        Arrays.fill(DIGIT_TABLE, -1);
        DIGIT_TABLE['0'] = 0;
        DIGIT_TABLE['1'] = 1;
        DIGIT_TABLE['2'] = 2;
        DIGIT_TABLE['3'] = 3;
        DIGIT_TABLE['4'] = 4;
        DIGIT_TABLE['5'] = 5;
        DIGIT_TABLE['6'] = 6;
        DIGIT_TABLE['7'] = 7;
        DIGIT_TABLE['8'] = 8;
        DIGIT_TABLE['9'] = 9;
        DIGIT_TABLE['a'] = 10;
        DIGIT_TABLE['b'] = 11;
        DIGIT_TABLE['c'] = 12;
        DIGIT_TABLE['d'] = 13;
        DIGIT_TABLE['e'] = 14;
        DIGIT_TABLE['f'] = 15;
    }

    public static boolean isDigit(char c, int radix) {
        return getDigit(c, radix) != -1;
    }

    public static int getDigit(char c, int radix) {
        char c1 = Character.toLowerCase(c);
        if (c1 > 255) return -1;
        int nv = DIGIT_TABLE[c1];
        return nv < radix ? nv : -1;
    }

    /**
     * Character to indicate EOF.
     */
    public static final char DONE = '\uFFFF';
    public static final char END  = DONE;
    public static final char EOF  = DONE;

    public interface ForwardMatcherProvider<T> {
        ForwardMatcher<T> matcher();
    }

    public interface ForwardMatcher<T> {

        /** Does the next character match? */
        boolean next(StringReader reader);

        /** Get the resulting object. */
        T getResult();

    }

    @Data
    public static class SegmentStart {
        final int startIndex;
        final int startLine;
    }

    //////////////////////////////

    // the current index
    private int index;
    // the string to read
    private final String str;
    private AnalyzedString aStr;
    // the total string length
    private int len;
    // the current line number
    private int currentLine = 1;

    /* Not required, for ease of use. */
    private String fileName;
    private Stack<SegmentStart> starts = new Stack<>();

    /**
     * Constructor.
     * Creates a new string reader for the provided
     * string from the provided index.
     * @param str The string.
     * @param index The index.
     */
    public StringReader(String str, int index) {
        this.str   = str;
        this.len   = str.length();
        this.index = index;
    }

    /**
     * @see StringReader#StringReader(String, int)
     * Parameter {@code index} is defaulted to 0.
     */
    public StringReader(String str) {
        this(str, 0);
    }

    /**
     * Signal the (virtual) file this readers source
     * is sourced from.
     *
     * @param fileName The file name.
     * @return This.
     */
    public StringReader inFile(String fileName) {
        this.fileName = fileName;
        return this;
    }

    /**
     * Get the file name.
     * Can be null.
     */
    public String getFile() {
        return fileName;
    }

    public int currentLine() {
        return currentLine;
    }

    public StringReader currentLine(int currentLine) {
        this.currentLine = currentLine;
        return this;
    }

    /**
     * Begin a segment, pushing the start index
     * onto the indices stack.
     *
     * @return This.
     */
    public StringReader begin() {
        starts.push(new SegmentStart(index, currentLine));
        return this;
    }

    /**
     * End a segment, popping the start index
     * and creating a string location.
     *
     * @return The location.
     */
    public StringLocation end() {
        SegmentStart start = starts.pop();
        return new StringLocation(fileName, aStr(), start.startIndex, index(), start.getStartLine());
    }

    /**
     * End the current segment if present and reset to
     * the segments start position. If absent it will
     * reset to index 0.
     *
     * @return This.
     */
    public StringReader reset() {
        if (starts.size() != 0) {
            index(starts.pop().getStartIndex());
        } else {
            index(0);
        }

        return this;
    }

    /**
     * End a segment if present, otherwise use
     * the current index and capture one character.
     *
     * @see #end()
     * @return The location.
     */
    public StringLocation endOrHere() {
        if (starts.size() == 0)
            return here();
        return end();
    }

    public <S extends StringLocatable> S endOrHere(S locatable) {
        locatable.located(endOrHere());
        return locatable;
    }

    /**
     * Create a new string location at the current
     * position, capturing one character.
     *
     * @return The location.
     */
    public StringLocation here() {
        return new StringLocation(fileName, aStr(), index, index, currentLine);
    }

    public StringLocation indexToHere(int startIndex) {
        return new StringLocation(fileName, aStr(), startIndex, index, currentLine);
    }

    public StringLocation hereToIndex(int endIndex) {
        return new StringLocation(fileName, aStr(), index, endIndex, currentLine);
    }

    /**
     * Create a new string location starting from the current
     * position and spreading the offsets, capturing multiple
     * characters.
     *
     * @param offStart The offset to add to this index to get the start.
     * @param offEnd The offset to add to this index to get the end.
     * @return The location.
     */
    public StringLocation here(int offStart, int offEnd) {
        return new StringLocation(fileName, aStr(), index + offStart, index + offEnd, currentLine);
    }

    /**
     * If the string reader will have a
     * next character.
     *
     * @return True/false.
     */
    public boolean hasNext() {
        return index < len - 1;
    }

    /**
     * Clamps an index in to the minimum (0) and
     * maximum (string length) index.
     * @param index The index.
     * @return The clamped index.
     */
    public int clamp(int index) {
        return Math.min(len - 1, Math.max(0, index));
    }

    /**
     * Get a character from the string by
     * index. Clamped.
     * @param i The index.
     * @return The character.
     */
    public char peekAt(int i) {
        return str.charAt(clamp(i));
    }

    /**
     * Get a character from the string relative
     * to the current index. Not clamped, rather
     * returns {@link StringReader#DONE} if the
     * index is in an invalid position.
     * @param i The index.
     * @return The character or {@link StringReader#DONE}
     */
    public char peek(int i) {
        int idx = index + i;
        if (idx < 0 || idx >= len)
            return DONE;
        return str.charAt(idx);
    }

    /**
     * Advances the position by 1 and
     * returns the character or {@link StringReader#DONE}
     * if in an invalid position.
     * @return The character.
     */
    public char advance() {
        if ((index += 1) >= len || index < 0) return DONE;
        char c = str.charAt(index);
        if (c == '\n')
            currentLine++;
        return c;
    }

    /**
     * Advances the position by {@code a} and
     * returns the character or {@link StringReader#DONE}
     * if in an invalid position.
     * @param a The amount to advance by.
     * @return The character.
     */
    public char advance(int a) {
        for (int i = 0; i < a; i++)
            advance();
        return current();
    }

    /**
     * Decreases the position by 1 and
     * returns the character or {@link StringReader#DONE}
     * if in an invalid position.
     * @return The character.
     */
    public char prev() {
        if ((index -= 1) >= len || index < 0) return DONE;
        char c = str.charAt(index);
        if (c == '\n')
            currentLine--;
        return c;
    }

    /**
     * Decreases the position by {@code a} and
     * returns the character or {@link StringReader#DONE}
     * if in an invalid position.
     * @param a The amount to decrease by.
     * @return The character.
     */
    public char prev(int a) {
        for (int i = 0; i < a; i++)
            prev();
        return current();
    }

    /**
     * Returns the character at the current position
     * or {@link StringReader#DONE} if in an invalid position.
     * @return The character.
     */
    public char current() {
        if (index < 0 || index >= len) return DONE;
        return str.charAt(index);
    }

    /**
     * Returns the character at the current position
     * or {@link StringReader#DONE} if in an invalid position.
     * @return The character.
     */
    public char curr() {
        return current();
    }

    /**
     * If the reader is at the end of
     * the string.
     */
    public boolean ended() {
        return index >= len;
    }

    /* Collection */

    // predicate to always return true
    private static final Predicate<Character> ALWAYS = c -> true;

    /**
     * Collects until the end of the string.
     *
     * @return The string.
     */
    public String collect() {
        return collect(ALWAYS, null);
    }

    /**
     * Collect all characters matching the predicate
     * into a string until one doesn't match or the end
     * of the string is reached.
     *
     * @param predicate The character predicate.
     * @param offEnd The offset to skip at the end.
     * @return The string collected.
     */
    public String collect(Predicate<Character> predicate, int offEnd) {
        String str = collect(predicate);
        advance(offEnd);
        return str;
    }

    /**
     * Collect all characters matching the predicate
     * into a string until one doesn't match or the end
     * of the string is reached.
     *
     * @param predicate The character predicate.
     * @return The string collected.
     */
    public String collect(Predicate<Character> predicate) {
        return collect(predicate, null);
    }

    /**
     * Collect all characters matching the predicate
     * into a string until one doesn't match or the end
     * of the string is reached, skipping characters
     * matched by the skip predicate.
     *
     * @param predicate The character predicate.
     * @param skip The skip character predicate.
     * @param offEnd The offset to add at the end.
     * @return The string collected.
     */
    public String collect(Predicate<Character> predicate, Predicate<Character> skip, int offEnd) {
        String str = collect(predicate, skip);
        advance(offEnd);
        return str;
    }

    /**
     * Collect all characters matching the predicate
     * into a string until one doesn't match or the end
     * of the string is reached, skipping characters
     * matched by the skip predicate.
     *
     * @param predicate The character predicate.
     * @param skip The skip character predicate.
     * @return The string collected.
     */
    public String collect(Predicate<Character> predicate, Predicate<Character> skip) {
        return collect(predicate, skip, null);
    }

    /**
     * Collect all characters matching the predicate
     * into a string until one doesn't match or the end
     * of the string is reached, skipping characters
     * matched by the skip predicate.
     *
     * @param predicate The character predicate.
     * @param skip The skip character predicate.
     * @param charEval The character consumer for
     *                 evaluation, called upon each
     *                 non-skipped character.
     * @return The string collected.
     */
    public String collect(Predicate<Character> predicate, Predicate<Character> skip, Consumer<Character> charEval) {
        if (predicate == null)
            predicate = ALWAYS;
        StringBuilder b = new StringBuilder();
        char c = current();
        while (c != DONE) {
            boolean sf = skip != null && skip.test(c);
            if (!sf) {
                if (!predicate.test(c))
                    break;

                if (charEval != null)
                    charEval.accept(c);
                b.append(c);
            }

            c = advance();
        }

        return b.toString();
    }

    /**
     * Peek-collect the string from the current
     * index, collecting the string until a character
     * does not match the predicate or the end
     * is reached without altering the state.
     *
     * @param predicate The character predicate.
     * @return The collected string.
     */
    public String pCollect(Predicate<Character> predicate) {
        return pCollect(predicate, null);
    }

    /**
     * Peek-collect the string from the current
     * index, collecting the string until a character
     * does not match the predicate or the end
     * is reached without altering the state, skipping
     * characters matched by the skip predicate.
     *
     * @param predicate The character predicate.
     * @param skip The character skip predicate.
     * @return The collected string.
     */
    public String pCollect(Predicate<Character> predicate, Predicate<Character> skip) {
        if (predicate == null)
            predicate = ALWAYS;
        StringBuilder b = new StringBuilder();
        int off = 0;
        char c;
        while ((c = peek(off++)) != DONE && predicate.test(c)) {
            if (skip == null || !skip.test(c)) {
                b.append(c);
            }
        }

        return b.toString();
    }

    /**
     * Splits the remaining string on
     * all given characters.
     *
     * @param chars The characters to split on.
     * @return The list of strings.
     */
    public List<String> split(char... chars) {
        List<String> list = new ArrayList<>(len / 10);
        HashSet<Character> charSet = new HashSet<>();
        for (char c : chars)
            charSet.add(c);
        while (current() != DONE) {
            list.add(collect(c -> !charSet.contains(c)));
            advance();
        }

        return list;
    }

    /**
     * Splits the remaining string on all characters
     * matched by the predicate.
     *
     * @param splitter The split character predicate.
     * @return The strings.
     */
    public List<String> split(Predicate<Character> splitter) {
        Predicate<Character> nSplitter = splitter.negate();
        List<String> list = new ArrayList<>();
        while (current() != DONE) {
            list.add(collect(nSplitter));
            advance();
        }

        return list;
    }

    /**
     * Skips characters until a character doesn't match
     * the predicate or the end of the string is reached.
     *
     * @param predicate The character predicate.
     * @param offEnd The offset to skip at the end.
     * @return The amount of characters consumed.
     */
    public StringReader consume(Predicate<Character> predicate, int offEnd) {
        char c = current();
        while (c != DONE && predicate.test(c))
            c = advance();
        advance(offEnd);
        return this;
    }

    /**
     * Skips characters until a character doesn't match
     * the predicate or the end of the string is reached.
     *
     * @param predicate The character predicate.
     * @return The amount of characters consumed.
     */
    public StringReader consume(Predicate<Character> predicate) {
        char c = current();
        while (c != DONE && predicate.test(c))
            c = advance();
        return this;
    }

    /**
     * Skips characters until a character doesn't match
     * the predicate or the end of the string is reached.
     *
     * @param predicate The character predicate.
     * @param offEnd The offset to skip at the end.
     * @return The amount of characters consumed.
     */
    public StringReader xconsume(Predicate<StringReader> predicate, int offEnd) {
        char c = current();
        while (c != DONE && predicate.test(this))
            c = advance();
        advance(offEnd);
        return this;
    }

    /**
     * Skips characters until a character doesn't match
     * the predicate or the end of the string is reached.
     *
     * @param predicate The character predicate.
     * @return The amount of characters consumed.
     */
    public StringReader xconsume(Predicate<StringReader> predicate) {
        char c = current();
        while (c != DONE && predicate.test(this))
            c = advance();
        return this;
    }

    // skips all whitespace
    public StringReader consumeWhitespace() {
        return consume(Character::isWhitespace);
    }

    /**
     * Skips characters until a character doesn't match
     * the predicate or the end of the string is reached.
     *
     * @param predicate The character predicate.
     * @param offEnd The offset to skip at the end.
     * @return The amount of characters consumed.
     */
    public int cconsume(Predicate<Character> predicate, int offEnd) {
        int i = index;
        char c = current();
        while (c != DONE && predicate.test(c))
            c = advance();
        i = index - i;
        advance(offEnd);
        return i;
    }

    /**
     * Skips characters until a character doesn't match
     * the predicate or the end of the string is reached.
     *
     * @param predicate The character predicate.
     * @return The amount of characters consumed.
     */
    public int cconsume(Predicate<Character> predicate) {
        int i = index;
        char c = current();
        while (c != DONE && predicate.test(c))
            c = advance();
        return index - i;
    }

    // skips all whitespace
    public int cconsumeWhitespace() {
        return cconsume(Character::isWhitespace);
    }

    /**
     * Get the current index.
     */
    public int index() {
        return index;
    }

    /**
     * Set the current index.
     *
     * @param i The index.
     * @return This.
     */
    public StringReader index(int i) {
        if (index > i) {
            final int l = index - i;
            for (int i2 = 0; i2 < l; i2++)
                prev();
        } else {
            final int l = i - index;
            for (int i2 = 0; i2 < l; i2++)
                advance();
        }

        return this;
    }

    /**
     * Get the string this reader is reading.
     */
    public String str() {
        return str;
    }

    public AnalyzedString aStr() {
        if (aStr == null) {
            aStr = new AnalyzedString(str);
            aStr.getAllLines();
        }

        return aStr;
    }

    /**
     * Create a new substring reader, from the start offset
     * plus the current index to the end offset plus the
     * current index.
     *
     * @param startOffset The offset of the start point.
     * @param endOffset The offset of the end point.
     * @return The reader.
     */
    public StringReader sub(int startOffset, int endOffset) {
        int len = index + endOffset;
        StringReader reader = new StringReader(str, index + startOffset);
        reader.len = len;
        return reader;
    }

    /**
     * Create a new substring reader from the start
     * index to the end index.
     *
     * @param start The start index.
     * @param end The end index.
     * @return The reader.
     */
    public StringReader subAt(int start, int end) {
        int len = this.len - end;
        StringReader reader = new StringReader(str, start);
        reader.len = len;
        return reader;
    }

    /**
     * Create a new reader of the current
     * string at the current index.
     *
     * @return The new reader.
     */
    public StringReader branch() {
        return new StringReader(str, index);
    }

    /* Utility */

    /**
     * Escape all characters matching the
     * to-escape predicate in the remaining string
     * using the escape function.
     *
     * @param toEscape The predicate for characters to escape.
     * @param escapeFunc The escape function.
     *                   Escapes the given character.
     * @return The escaped string.
     */
    public String escapeRemaining(Predicate<Character> toEscape,
                                  Function<Character, String> escapeFunc) {
        StringBuilder b = new StringBuilder();
        char c = current();
        while (c != DONE) {
            if (toEscape.test(c)) {
                b.append(escapeFunc.apply(c));
            } else {
                b.append(c);
            }

            c = advance();
        }

        return b.toString();
    }

    /**
     * Create an iterator of the remaining
     * string data. This will modify the state,
     * to avoid it modifying the state use
     * {@link #pIterator()}
     *
     * @return The iterator.
     */
    @Override
    public Iterator<Character> iterator() {
        return new Iterator<Character>() {
            boolean doneFirst = false;

            @Override
            public boolean hasNext() {
                return StringReader.this.hasNext();
            }

            @Override
            public Character next() {
                if (!doneFirst) {
                    doneFirst = true;
                    return curr();
                }

                return StringReader.this.advance();
            }
        };
    }

    /**
     * Create an iterator of the remaining
     * string data. This will utilize peek
     * and will not modify the state.
     *
     * @return The iterator.
     */
    public Iterator<Character> pIterator() {
        return new Iterator<Character>() {
            // the current index
            int index = index() - 1;

            @Override
            public boolean hasNext() {
                return index < len - 1;
            }

            @Override
            public Character next() {
                return peekAt(++index);
            }
        };
    }

    /**
     * Create a character stream from
     * this string reader, this utilizes
     * {@link #iterator()} meaning it will
     * modify the state, to avoid this use
     * {@link #pStream()}
     *
     * @return The stream.
     */
    public Stream<Character> stream() {

        return StreamSupport.stream(
                Spliterators.spliterator(iterator(), len - index, 0),
                false);
    }

    /**
     * Create a character stream from
     * this string reader, this utilizes
     * {@link #pIterator()} meaning it will
     * not modify the state.
     *
     * @return The stream.
     */
    public Stream<Character> pStream() {
        return StreamSupport.stream(
                Spliterators.spliterator(pIterator(), len - index, 0),
                false);
    }

    /* Parsing */

    /**
     * Consumes the string inputted fully and returns true
     * if present, otherwise fails and returns false.
     *
     * Note that it does not reset back to the starting
     * position automatically.
     *
     * It leaves the cursor of the reader at the first character
     * encountered to not match the string when failed, or the
     * character after the entire string if it succeeds.
     *
     * @param str The string to consume.
     * @return If it was able to consume the full string.
     */
    public boolean consumeFullString(String str) {
        final int l1 = str.length() - 1;
        int i = 0;
        while (str.charAt(i) == curr()) {
            if (i == l1) {
                advance();
                return true;
            }

            i++;
            advance();
        }

        return false;
    }

    /**
     * Peek-consumes the string inputted fully and returns true
     * if present, otherwise fails and returns false.
     *
     * @param str The string to consume.
     * @return If it was able to consume the full string.
     */
    public boolean pConsumeFullString(String str) {
        final int l1 = str.length() - 1;
        int start = index();
        int i = 0;
        while (str.charAt(i) == peekAt(start + i)) {
            if (i == l1) {
                return true;
            }

            i++;
        }

        return false;
    }

    /**
     * Uses the provided matcher to read a value
     * forwards in the stream.
     *
     * Note that it does not reset back to the starting
     * position automatically.
     *
     * It leaves the cursor of the reader at the first character
     * encountered to not match when failed, or the
     * character after the entire match if it succeeds.
     *
     * @param matcher The matcher to use.
     * @param <T> The result type.
     * @return The result or null if it failed.
     */
    public <T> T matchForward(ForwardMatcher<T> matcher) {
        prev();
        while (matcher.next(this));
        return matcher.getResult();
    }

    /**
     * @see #matchForward(ForwardMatcher)
     */
    public <T> T matchForward(ForwardMatcherProvider<T> matcherProvider) {
        return matchForward(matcherProvider.matcher());
    }

    public int collectInt(final int radix) {
        boolean neg = false;
        int  r = 0;
        char c    ;
        if (current() == '-') {
            neg = true;
            advance();
        }
        while ((c = current()) != DONE) {
            if (c == '_' || c == '\'') { advance(); continue; }
            int nv = getDigit(c, radix);
            if (nv == -1) { break; }
            r *= radix;
            r += nv;
            advance();
        }

        return neg ? -r : r;
    }

    public int collectInt() {
        return collectInt(10);
    }

    public long collectLong(final int radix) {
        boolean neg = false;
        long r = 0;
        char c;
        if (current() == '-') {
            neg = true;
            advance();
        }
        while ((c = current()) != DONE) {
            if (c == '_' || c == '\'') { advance(); continue; }
            int nv = getDigit(c, radix);
            if (nv == -1) { break; }
            r *= radix;
            r += nv;
            advance();
        }

        return neg ? -r : r;
    }

    public long collectLong() {
        return collectLong(10);
    }

    public float collectFloat() {
        boolean neg = false;
        if (current() == '-') {
            neg = true;
            advance();
        }
        float f = collectInt(10);
        if (current() == '.') {
            advance();
            char  c;
            float r = 0;
            float m = 0.1f;
            while ((c = current()) != DONE) {
                if (c == '_' || c == '\'') { advance(); continue; }
                int nv = getDigit(c, 10);
                if (nv == -1) { break; }
                r += nv * m;
                m *= 0.1;
                advance();
            }
            f += r;
        }
        return neg ? -f : f;
    }

    public double collectDouble() {
        // parse negative
        boolean neg = false;
        if (current() == '-') {
            neg = true;
            advance();
        }

        double f = collectLong(10);
        if (current() == '.') {
            advance();
            char c;
            double r = 0;
            double m = 0.1f;
            while ((c = current()) != DONE) {
                if (c == '_' || c == '\'') { advance(); continue; }
                int nv = getDigit(c, 10);
                if (nv == -1) { break; }
                r += nv * m;
                m *= 0.1;
                advance();
            }
            f += r;
        } else if (current() == 'E' || current() == 'e') {
            advance();
            long pow = collectLong();
            f = f * Math.pow(10, pow);
        }

        return neg ? -f : f;
    }

    /**
     * Reads the given number of charcters into a string,
     * if there are not enough characters it will silently fail
     * and return the 'incomplete' string.
     *
     * @param count The number of characters to read.
     * @return The string.
     */
    public String read(int count) {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < count && !ended(); i++) {
            b.append(current());
            advance();
        }

        return b.toString();
    }

    /* Debug */

    public void debugPrint(String s, PrintStream stream) {
        stream.println("[READER:" + s + "] current: '" + current() + "', index: " + index() + ", nv10: " + getDigit(current(), 10));
    }

    public void debugPrint(String s) {
        debugPrint(s, System.out);
    }

}

