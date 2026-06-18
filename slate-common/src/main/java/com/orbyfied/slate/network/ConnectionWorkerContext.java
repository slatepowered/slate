package com.orbyfied.slate.network;

import com.orbyfied.slate.util.buffer.ByteBufferProvider;

public abstract class ConnectionWorkerContext {

  public abstract ConnectionManager manager();

  /// Buffer pool for long-lived, large capacity buffers which may hold entire packets for the duration of their processing
  protected final ByteBufferProvider decodeBufferProvider = ByteBufferProvider.pooledDirect(4096);
  /// Buffer pool for potentially short-lived, low capacity intermediate socket IO buffers
  protected final ByteBufferProvider readBufferProvider = ByteBufferProvider.pooledDirect(1024);

  public ByteBufferProvider decodeBufferProvider() {
    return decodeBufferProvider;
  }

  public ByteBufferProvider readBufferProvider() {
    return readBufferProvider;
  }

  public abstract void remove(Connection connection);

}
