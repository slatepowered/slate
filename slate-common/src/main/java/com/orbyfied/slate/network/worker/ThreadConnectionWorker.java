package com.orbyfied.slate.network.worker;

import com.orbyfied.slate.network.ConnectionManager;
import com.orbyfied.slate.network.ConnectionWorker;

public class ThreadConnectionWorker extends ConnectionWorker {

  public ThreadConnectionWorker(ConnectionManager manager) {
    super(manager);
  }

  private Thread thread;

  public boolean isActive() {
    return thread.isAlive();
  }

  public void start() {
    startOnThread();
  }

  /**
   * Start this worker on another thread.
   */
  public Thread startOnThread() {
    this.thread = new Thread(this,"ConnectionWorker");
    thread.setDaemon(true);
    thread.start();
    return thread;
  }

}
