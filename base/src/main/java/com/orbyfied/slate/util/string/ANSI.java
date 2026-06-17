package com.orbyfied.slate.util.string;

public class ANSI {

    public static char ESC = (char) 27;

    /* Formatting Codes */
    public static final String RESET = "\u001B[0m";
    public static final String BLACK = "\u001B[30m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String PURPLE = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";

    public static String getRGBForeground(int r, int g, int b) {
        return ESC + "[38;2;" + Math.min(r, 255) + ";" + Math.min(g, 255) + ";" + Math.min(b, 255)  + "m";
    }

    /**
     * Formats the given text with the given format
     * if the flag is true.
     */
    public static String format(boolean flag, String text, String format) {
        if (!flag)
            return text;
        return format + text + RESET;
    }

    public static String format(String text, String format) {
        return format + text + RESET;
    }

}
