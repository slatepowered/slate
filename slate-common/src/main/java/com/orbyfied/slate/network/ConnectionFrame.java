package com.orbyfied.slate.network;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.nio.ByteBuffer;

@RequiredArgsConstructor
public final class ConnectionFrame {

  /// The mask to extract the flags from the 4 byte header field
  public static int HEADER_FLAGS_MASK = 0b1 << 31;
  /// Core header flag: Whether the frame is compressed
  public static int COMPRESSED = 1 << 31;

  private final Connection connection; // The source connection of this frame
  private final int size;              // The raw size of the network transmitted payload (before decrypt+decompress)
  private final int headerFlags;       // Core header flags extracted from the [length+flags: int32] field

  // Whether the frame was successfully completed
  @Getter boolean completed = false;

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

  @Override
  public String toString() {
    return "Frame(size: " + size + ", flags: " + Integer.toBinaryString(headerFlags >> 24) + ", position: " + buffer.position() + ", limit: " + buffer.limit() + ", completed: " + completed + ")";
  }

}
