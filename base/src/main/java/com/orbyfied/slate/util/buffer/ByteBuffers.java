package com.orbyfied.slate.util.buffer;

import com.orbyfied.slate.util.misc.ANSI;
import lombok.SneakyThrows;

import java.nio.ByteBuffer;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;

public class ByteBuffers {

  static final CharsetDecoder UTF8_DECODER = StandardCharsets.UTF_8.newDecoder();

  public static byte[] remainingByteArray(ByteBuffer buffer) {
    byte[] bytes = new byte[buffer.limit() - buffer.position()];
    buffer.get(bytes);
    return bytes;
  }

  @SneakyThrows
  public static String remainingUTF8String(ByteBuffer buffer) {
    String str = UTF8_DECODER.decode(buffer).toString();
    buffer.position(buffer.limit());
    return str;
  }

  public static String remainingHexDump(ByteBuffer buffer) {
    StringBuilder b = new StringBuilder();
    for (int i = buffer.position(); i < buffer.limit(); i++) {
      b.append(" 0x").append(Integer.toHexString(buffer.get(i)));
    }

    return b.toString();
  }

  public static String dumpSurroundingWindow(ByteBuffer buffer) {
    final int windowSize = 12;

    StringBuilder topLine = new StringBuilder("\n");
    StringBuilder bottomLine = new StringBuilder();

    final int startIndex = Math.max(0, buffer.position() - windowSize);
    final int length = Math.min(windowSize * 2, Math.max(0, buffer.limit() - startIndex));

    ANSI.SyncBuilder bottomBuilder = new ANSI.SyncBuilder();
    ANSI.SyncBuilder topBuilder = new ANSI.SyncBuilder();

    for (int i = startIndex; i < startIndex + length; i++) {
      bottomBuilder.reset();
      topBuilder.reset();

      byte b = buffer.get(i);
      String cs;
      char c = (char) b;
      if (c == '\n') cs = "\\n";
      else if (c == '\t') cs = "\\t";
      else if (c == '\0') cs = "NUL";
      else cs = String.valueOf(c);

      topBuilder
          .f(i < buffer.limit() - 1 ? ANSI.PURPLE : ANSI.DARK_PURPLE)
          .f(i == buffer.position() ? ANSI.UNDERLINE + ANSI.WHITE + ANSI.BG_PURPLE : "")
          .p("  " + i);

      bottomBuilder.f(ANSI.GRAY).p("0x").f(ANSI.BLUE).p(Integer.toHexString(b)).f(ANSI.GRAY)
          .p(" ").f(cs.length() > 1 ? ANSI.DARK_GREEN : ANSI.GREEN).p(cs).f(ANSI.GRAY);

      ANSI.SyncBuilder.pad(bottomBuilder, topBuilder);
      bottomLine.append(bottomBuilder).append(" ");
      topLine.append(topBuilder).append(" ");
    }

    return topLine + "\n" + bottomLine;
  }

}
