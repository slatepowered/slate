package com.orbyfied.slate.util.buffer;

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

}
