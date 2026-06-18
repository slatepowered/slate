package com.orbyfied.slate.util.buffer;

import java.nio.ByteBuffer;

@FunctionalInterface
public interface ByteBufferFactory {

  static ByteBufferFactory direct() {
    return ByteBuffer::allocateDirect;
  }

  ByteBuffer create(int capacity);

}
