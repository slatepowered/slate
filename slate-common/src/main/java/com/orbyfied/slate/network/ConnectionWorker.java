package com.orbyfied.slate.network;

import com.orbyfied.slate.project.TODO;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;

@RequiredArgsConstructor
public abstract class ConnectionWorker extends ConnectionWorkerContext implements Runnable {

  private final ConnectionManager manager;

  /// The connections waiting to be accepted by this worker
  private final ConcurrentLinkedDeque<Connection> registrationQueue = new ConcurrentLinkedDeque<>();

  protected Selector selector;

  public void submitConnection(Connection connection) {
    registrationQueue.add(connection);
    selector.wakeup();
  }

  @Override
  public ConnectionManager manager() {
    return manager;
  }

  @Override
  public void remove(Connection connection) {

  }

  /**
   * Check whether this worker is active.
   */
  public abstract boolean isActive();

  /**
   * Start this connection worker.
   */
  public abstract void start();

  @SneakyThrows
  @Override
  public void run() {
    try {
      while (manager.isActive()) {
        selector.select();

        Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
        while (iterator.hasNext()) {
          SelectionKey key = iterator.next();
          iterator.remove();

          if (key.isReadable()) {
            Connection connection = (Connection) key.attachment();
            connection.readKey(key);
          }
        }

        // check for submitted connections
        Connection connection;
        while ((connection = registrationQueue.poll()) != null) {
          connection.assigned(this);
          connection.getChannel().register(
              selector,
              SelectionKey.OP_READ,
              connection
          );
        }
      }
    } catch (Exception ex) {
      TODO.todoErrorHandling(ex);
    }
  }

}
