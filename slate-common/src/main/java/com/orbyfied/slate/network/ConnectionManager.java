package com.orbyfied.slate.network;

import com.orbyfied.slate.network.worker.ThreadConnectionWorker;
import com.orbyfied.slate.project.TODO;
import lombok.Getter;
import lombok.SneakyThrows;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Manages direct connections and the local server through Java NIO.
 */
public class ConnectionManager {

  private final AtomicBoolean active = new AtomicBoolean(false);

  /// The pool of connection workers
  private final @Getter List<ConnectionWorker> workerPool = new ArrayList<>();

  /// Selects the worker assigned to a connection
  private WorkerSelector workerSelector = WorkerSelector.roundRobin();

  /// The server channel and acceptor instances
  private ServerSocketChannel serverSocketChannel;
  private Selector acceptorSelector;
  private Thread acceptorThread;

  public boolean isActive() {
    return active.get();
  }

  @SneakyThrows
  public void bind(SocketAddress socketAddress) {
    serverSocketChannel = ServerSocketChannel.open();
    serverSocketChannel.bind(socketAddress);
    serverSocketChannel.configureBlocking(false);

    acceptorSelector = Selector.open();
    serverSocketChannel.register(acceptorSelector, SelectionKey.OP_ACCEPT);
  }

  @SneakyThrows
  public int servicePort() {
    return ((InetSocketAddress)serverSocketChannel.getLocalAddress()).getPort();
  }

  public void start() {
    active.set(true);

    // create and start acceptor thread
    acceptorThread = new Thread(() -> {
      try {
        while (isActive()) {
          acceptorSelector.select();
          Iterator<SelectionKey> iterator = acceptorSelector.selectedKeys().iterator();
          while (iterator.hasNext()) {
            SelectionKey key = iterator.next();
            iterator.remove();

            if (key.isAcceptable()) {
              // accept new connection
              SocketChannel client = serverSocketChannel.accept();
              client.configureBlocking(false);

              // create connection object
              Connection connection = new Connection(this, client);

              // submit connection to worker
              ConnectionWorker worker = workerSelector.next(this, connection);
              if (worker == null || !worker.isActive()) {
                TODO.todoEventLogging("ConnectionManager::accept", "WARN: workerSelector.next() returned null or inactive " + worker);
                continue;
              }

              worker.submitConnection(connection);
            }
          }
        }
      } catch (Exception ex) {
        TODO.todoErrorHandling(ex);
      }
    }, "ConnectionAcceptor");
    acceptorThread.setDaemon(true);
    acceptorThread.start();

    // finally, start workers
    for (ConnectionWorker worker : workerPool) {
      startAndInitializeWorker(worker);
    }
  }

  public void close() {
    active.set(false);

    try {
      serverSocketChannel.close();
    } catch (Exception ex) {
      TODO.todoErrorHandling(ex);
    }
  }

  @SneakyThrows
  private ConnectionWorker startAndInitializeWorker(ConnectionWorker worker) {
    worker.selector = Selector.open();
    worker.start();
    return worker;
  }

  public synchronized ConnectionManager provideWorkers(Collection<ConnectionWorker> workers) {
    workerPool.addAll(workers);
    for (ConnectionWorker worker : workers) {
      if (this.isActive() && !worker.isActive()) {
        startAndInitializeWorker(worker);
      }
    }

    return this;
  }

  public synchronized ConnectionManager allocateThreadedWorkers(int count) {
    List<ConnectionWorker> workers = new ArrayList<>();
    for (int i = 0; i < count; i++) {
      workers.add(new ThreadConnectionWorker(this));
    }

    return provideWorkers(workers);
  }

  public synchronized ConnectionManager workerSelector(WorkerSelector workerSelector) {
    this.workerSelector = workerSelector;
    return this;
  }

}
