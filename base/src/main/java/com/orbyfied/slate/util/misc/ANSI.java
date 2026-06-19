package com.orbyfied.slate.util.misc;

/**
 * Utilities for console formatting using ANSI/VT escape codes.
 * TODO: replace with proper console formatting
 */
public class ANSI {

  public static char ESC = (char) 27;

  /* Formatting Codes */
  public static final String RESET = "\u001B[0m";

  public static final String BLACK = "\u001B[30m";
  public static final String DARK_RED = "\u001B[31m";
  public static final String DARK_GREEN = "\u001B[32m";
  public static final String DARK_YELLOW = "\u001B[33m";
  public static final String DARK_BLUE = "\u001B[34m";
  public static final String DARK_PURPLE = "\u001B[35m";
  public static final String DARK_CYAN = "\u001B[36m";
  public static final String BRIGHT_GRAY = "\u001B[37m";

  public static final String GRAY = "\u001B[90m";
  public static final String RED = "\u001B[91m";
  public static final String GREEN = "\u001B[92m";
  public static final String YELLOW = "\u001B[93m";
  public static final String BLUE = "\u001B[94m";
  public static final String PURPLE = "\u001B[95m";
  public static final String CYAN = "\u001B[96m";
  public static final String WHITE = "\u001B[97m";

  public static final String BG_BLACK = "\u001B[40m";
  public static final String BG_DARK_RED = "\u001B[41m";
  public static final String BG_DARK_GREEN = "\u001B[42m";
  public static final String BG_DARK_YELLOW = "\u001B[43m";
  public static final String BG_DARK_BLUE = "\u001B[44m";
  public static final String BG_DARK_PURPLE = "\u001B[45m";
  public static final String BG_DARK_CYAN = "\u001B[46m";
  public static final String BG_BRIGHT_GRAY = "\u001B[47m";

  public static final String BG_GRAY = "\u001B[100m";
  public static final String BG_RED = "\u001B[101m";
  public static final String BG_GREEN = "\u001B[102m";
  public static final String BG_YELLOW = "\u001B[103m";
  public static final String BG_BLUE = "\u001B[104m";
  public static final String BG_PURPLE = "\u001B[105m";
  public static final String BG_CYAN = "\u001B[106m";
  public static final String BG_WHITE = "\u001B[107m";

  public static final String UNDERLINE = "\u001B[4m";
  public static final String BOLD = "\u001B[1m";

  //

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

  public static class SyncBuilder {
    private final StringBuilder text = new StringBuilder();
    private int visualLength = 0;

    public StringBuilder text() {
      return text;
    }

    public int visualLength() {
      return visualLength;
    }

    public SyncBuilder reset() {
      visualLength = 0;
      text.delete(0, text.length());
      return this;
    }

    public SyncBuilder format(Object fmt) {
      text.append(fmt);
      return this;
    }

    public SyncBuilder f(Object fmt) {
      return format(fmt);
    }

    public SyncBuilder append(Object ob) {
      String str = String.valueOf(ob);
      text.append(str);
      visualLength += str.length();
      return this;
    }

    public SyncBuilder p(Object ob) {
      return append(ob);
    }

    @Override
    public String toString() {
      return text + ANSI.RESET;
    }

    public static void pad(SyncBuilder... builders) {
      int maxVl = 0;
      for (SyncBuilder b : builders) {
        if (b.visualLength > maxVl) {
          maxVl = b.visualLength;
        }
      }

      for (SyncBuilder b : builders) {
        if (b.visualLength < maxVl) {
          int pad = maxVl - b.visualLength;
          b.text.append(" ".repeat(pad));
          b.visualLength += pad;
        }
      }
    }
  }

}
