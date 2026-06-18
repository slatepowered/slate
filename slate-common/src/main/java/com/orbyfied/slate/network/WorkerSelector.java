package com.orbyfied.slate.network;

import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public interface WorkerSelector {

  static WorkerSelector roundRobin() {
    return new WorkerSelector() {
      final AtomicInteger index = new AtomicInteger(0);

      @Override
      public ConnectionWorker next(ConnectionManager manager, Connection connection) {
        final List<ConnectionWorker> pool = manager.getWorkerPool();
        return pool.get(index.incrementAndGet() % pool.size());
      }
    };
  }

  /**
   * Pick the worker which is to be assigned the given channel.
   */
  ConnectionWorker next(ConnectionManager manager, Connection connection);

}
