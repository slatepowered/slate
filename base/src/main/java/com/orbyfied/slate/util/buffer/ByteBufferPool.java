package com.orbyfied.slate.util.buffer;

import lombok.RequiredArgsConstructor;

import java.nio.ByteBuffer;
import java.util.Deque;
import java.util.LinkedList;

/**
 * @implNote Not thread safe!
 */
@RequiredArgsConstructor
public final class ByteBufferPool extends ByteBufferProvider {

  final ByteBufferFactory factory;
  final int minimumCapacity;

  Deque<ByteBuffer> openSet = new LinkedList<>();

  @Override
  public ByteBuffer acquire(int cap) {
    if (openSet.isEmpty()) {
      return factory.create(Math.max(minimumCapacity, cap));
    }

    ByteBuffer buf = openSet.pop();
    return buf.capacity() < cap ? factory.create(cap) : buf.clear();
  }

  @Override
  public ByteBuffer resize(ByteBuffer oldBuffer, int cap) {
    ByteBuffer buf = factory.create(cap + cap >> 1);
    buf.put(oldBuffer);
    return buf;
  }

  @Override
  public void release(ByteBuffer buffer) {
    if (buffer == null) return;
    openSet.add(buffer);
  }

  @Override
  public void close() {
    openSet.clear();
  }

}
