package com.orbyfied.slate.util.buffer;

import java.nio.ByteBuffer;

public abstract class ByteBufferProvider {

  public static ByteBufferPool pooledDirect(int minimumCapacity) {
    return new ByteBufferPool(ByteBufferFactory.direct(), minimumCapacity);
  }

  /**
   * Try and acquire a byte buffer with the given capacity.
   *
   * @param cap The capacity.
   * @return The buffer instance.
   */
  public abstract ByteBuffer acquire(int cap);

  /**
   * Resize the given buffer to the given capacity, copying the data in the provided
   * old buffer and update internal indices if needed.
   *
   * @param buffer The old buffer.
   * @param cap The capacity to resize to.
   * @return The new byte buffer.
   */
  public abstract ByteBuffer resize(ByteBuffer buffer, int cap);

  /**
   * Unlock the given buffer to be reassigned for another purpose.
   *
   * @param buffer The byte buffer to release.
   */
  public abstract void release(ByteBuffer buffer);

  /**
   * Release all pooled resources for this source.
   */
  public abstract void close();

}
