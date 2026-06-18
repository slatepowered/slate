package com.orbyfied.slate.network;

import com.orbyfied.slate.network.meta.Trust;
import com.orbyfied.slate.project.TODO;
import com.orbyfied.slate.util.buffer.ByteBuffers;
import lombok.Getter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents a direct TCP connection between two endpoints on the network, with
 * slate communication and security conventions provided.
 *
 * Packets sent over this connection must **always** be encoded as [length: int32][packet],
 * as the connection object will handle partial packet transmissions.
 *
 * This connection may handle special, direct communication control such
 * as base authorization and encryption.
 *
 * Connections must remain handled by the same thread for their lifetime.
 */
public class Connection {

  static final int MAX_FRAME_SIZE = 1024 * 1024 * 128; // 128 MB
  static final int HEADER_SIZE = 4 + 2; // [length: int32] [channel: int16]

  /// The connection manager instance
  private final ConnectionManager manager;

  /// The time at which this connection was created
  private final @Getter long creationTime;

  /// Whether this connection is open/active
  protected volatile @Getter boolean open;

  /// The raw selectable socket channel
  private final @Getter SocketChannel channel;

  /// The context of the worker responsible for handling this connection.
  private @Getter ConnectionWorkerContext workerContext;

  // Reused/pooled read buffer
  protected ByteBuffer socketReadBuffer;

  // Information about the frame currently being read
  protected ConnectionFrame incompleteFrame;

  /* Connection authorization state */
  protected Trust trust = Trust.UNAUTHORIZED;

  Connection(ConnectionManager manager, SocketChannel channel) {
    this.manager = manager;
    this.creationTime = System.currentTimeMillis();
    this.channel = channel;
    this.open = true;
  }

  // Called when this connection is assigned a worker
  protected void assigned(ConnectionWorkerContext context) {
    this.workerContext = context;
  }

  private ByteBuffer ensureReadBuffer() {
    return socketReadBuffer != null ? socketReadBuffer : (socketReadBuffer = workerContext.readBufferProvider().acquire(2048));
  }

  /**
   * Close this connection, release the resources and unregister the instance.
   */
  private void close() {
    try {
      if (channel.isOpen()) {
        channel.close();
      }
    } catch (IOException ex) {
      TODO.todoErrorHandling(ex);
    }

    this.open = false;
    if (workerContext != null) {
      workerContext.readBufferProvider().release(socketReadBuffer);
      workerContext.remove(this);
    }
  }

  /// Called by the {@link ConnectionManager} once a readable key was received for this channel
  protected final void readKey(SelectionKey key) throws IOException {
    ByteBuffer buf = ensureReadBuffer();
    buf.limit(buf.capacity() - HEADER_SIZE);
    boolean finishedRead = false;
    boolean startedIncompleteFrame = false;
    while (true) {
      if (!finishedRead && !startedIncompleteFrame) {
        // if the read hasnt finished yet, read from the channel
        int read = channel.read(buf);
        if (read < 0) {
          TODO.todoEventLogging("Connection", "WARN: Peer disconnected, read status " + read + " from socket");
          return;
        }

        if (read == 0) {
          if (incompleteFrame == null) {
            return; // early exit, no more frames left to read
          }

          finishedRead = true;
        }

        buf.flip();
      }

      // starting a new frame, allocate buffer etc
      if (incompleteFrame == null) {
        if (buf.remaining() < HEADER_SIZE) {
          buf.limit(buf.capacity());
          continue; // no available data, wait for more from system
                    // we can avoid another iteration and exit early because there's no way it read like 2 bytes
                    // but still has more available in the socket
        }

        final int size = buf.getInt();
        incompleteFrame = new ConnectionFrame(this, size);
        if (size < 0 || size > MAX_FRAME_SIZE || size > trust.getMaxFrameSize()) {
          TODO.todoEventLogging("Connection", "WARN: Received frame of size " + size + " at trust " + trust + ", closing channel");
          close();
          return;
        }

        // parse rest of the header
        incompleteFrame.channel = buf.getShort();

        // allocate frame buffer and finalize new frame
        incompleteFrame.buffer = workerContext.decodeBufferProvider().acquire(size).limit(size);
        startedIncompleteFrame = false;
      }

      // copy payload data to frame buffer
      final ByteBuffer frameBuf = incompleteFrame.buffer;
      final int frameSize = incompleteFrame.size();

      int remainingFrame = frameBuf.remaining();
      if (buf.remaining() > remainingFrame) {
        // we read another frame, prepare for that
        frameBuf.put(frameBuf.position(), buf, buf.position(), remainingFrame);
        frameBuf.position(frameBuf.position() + remainingFrame);
        buf.position(buf.position() + remainingFrame); // prepare to write to the end of the buffer and then parse from that position
        startedIncompleteFrame = true;
      } else {
        frameBuf.put(buf);
        buf.position(0).limit(buf.capacity() - HEADER_SIZE);
      }

      // check if we read the entire frame
      if (frameBuf.position() >= frameSize) {
        // completed a frame
        frameBuf.flip();
        completedIncomingFrameGuarded(incompleteFrame);
        incompleteFrame = null;
      }
    }
  }

  public static final AtomicInteger REC = new AtomicInteger(0);

  /**
   * Called when a completed frame has been read and is ready to be decoded and processed.
   *
   * @param frame The frame to be processed.
   */
  protected final void completedIncomingFrameGuarded(ConnectionFrame frame) {
    try {
      System.out.println("Read frame sized " + frame.size() + " with UTF8: " + ByteBuffers.remainingUTF8String(frame.buffer()));
      REC.incrementAndGet();
    } catch (Exception ex) {
      TODO.todoErrorHandling("An exception occurred while processing Connection frame of size " + frame.size(), ex);
    } finally {
      frame.discard(); // must be called in every code path to return pooled decoding buffers
    }
  }

}
