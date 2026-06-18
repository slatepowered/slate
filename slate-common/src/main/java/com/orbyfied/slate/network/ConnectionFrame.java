package com.orbyfied.slate.network;

import lombok.RequiredArgsConstructor;

import java.nio.ByteBuffer;

@RequiredArgsConstructor
public final class ConnectionFrame {

  private final Connection connection;
  private final int size;

  short channel;
  volatile ByteBuffer buffer;

  // To be called when the frame is done being decoded (return buffer to channel)
  // It is very important that this is invoked on every code path
  public void discard() {
    if (buffer == null) {
      return;
    }

    final ByteBuffer buf = buffer;
    buffer = null;
    connection.getWorkerContext().decodeBufferProvider().release(buf);
  }

  public Connection connection() {
    return connection;
  }

  public int size() {
    return size;
  }

  public short channel() {
    return channel;
  }

  public ByteBuffer buffer() {
    return buffer;
  }

}
