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
  static final int HEADER_SIZE = 4; // [length+flags: int32]

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
      if (incompleteFrame != null) {
        incompleteFrame.discard();
      }

      workerContext.remove(this);
    }
  }

  /// Called by the {@link ConnectionManager} once a readable key was received for this channel
  protected final void readKey(SelectionKey key) throws IOException {
    ByteBuffer buf = ensureReadBuffer();
    buf.position(0).limit(buf.capacity() - HEADER_SIZE);

    int readState = 0; // enum: [ANY, FINISHED, INCOMPLETE_HEADER]
    boolean positionedAtFrameHeader = false;
    while (true) {
//      System.out.println("-> rs: " + readState + ", buf(pos: " + buf.position() + ", lim: " + buf.limit() + "), frame: " + incompleteFrame + ", pafh: " + positionedAtFrameHeader);
      if ((readState != 1 && !positionedAtFrameHeader) || readState == 2) {
        // if the read hasnt finished yet, read from the channel
        int pos = buf.position(); // store read position
//        System.out.println(ByteBuffers.dumpSurroundingWindow(buf));
        int read = channel.read(buf);
        if (read < 0) {
          TODO.todoEventLogging("Connection", "WARN: Peer disconnected, read status " + read + " from socket");
          close();
          return;
        }

//        System.out.println(ByteBuffers.dumpSurroundingWindow(buf));
        if (buf.position() - pos == 0) {
          if (incompleteFrame == null) {
            return; // early exit, no more frames left to read
          }

          readState = 1; // FINISHED
        } else {
          readState = 0; // ANY
        }

        // 'flip' buffer to ready for read
        buf.limit(buf.position()).position(pos);
      } else if (readState == 1 && !positionedAtFrameHeader) {
        return;
      }

//      System.out.println("<- rs: " + readState + ", buf(pos: " + buf.position() + ", lim: " + buf.limit() + "), frame: " + incompleteFrame + ", pafh: " + positionedAtFrameHeader);

      // starting a new frame, allocate buffer etc
      if (incompleteFrame == null) {
        if (buf.remaining() < HEADER_SIZE) {
          // partial header,
          if (readState == 1 || true /* todo */) {
            TODO.todoEventLogging("Connection", "Partial header: remaining(" + buf.remaining() + ") limit(" + buf.limit() + ")");
            return;
          }

          readState = 2; // INCOMPLETE_HEADER -- it must read more data
          continue; // no available data, wait for more from system
                    // we can avoid another iteration and exit early because there's no way it read like 2 bytes
                    // but still has more available in the socket
        }

        final int sizeAndFlags = buf.getInt();
        final int size = sizeAndFlags & ~ConnectionFrame.HEADER_FLAGS_MASK;
        final int flags = sizeAndFlags & ConnectionFrame.HEADER_FLAGS_MASK;

        incompleteFrame = new ConnectionFrame(this, size, flags);
        if (size < 0 || size > MAX_FRAME_SIZE || size > trust.getMaxFrameSize()) {
          TODO.todoEventLogging("Connection", "WARN: Received frame of size " + size + " at trust " + trust + ", closing channel");
          close();
          return;
        }

        // allocate frame buffer and finalize new frame
        incompleteFrame.buffer = workerContext.decodeBufferProvider().acquire(size).limit(size);
        positionedAtFrameHeader = false;
      }

      // copy payload data to frame buffer
      final ByteBuffer frameBuf = incompleteFrame.buffer;

      int remainingFrame = frameBuf.remaining();
      if (buf.remaining() > remainingFrame) {
        // we read another frame, prepare for that
        frameBuf.put(frameBuf.position(), buf, buf.position(), remainingFrame);
        frameBuf.position(frameBuf.position() + remainingFrame);

        buf.position(buf.position() + remainingFrame); // prepare to write to the end of the buffer and then parse from that position
        positionedAtFrameHeader = true;
      } else {
        frameBuf.put(buf);

        // clear buffer as we have exhausted the read buffer
        // at this point, continue to next iteration to see
        // if there is any data remaining
        buf.position(0).limit(buf.capacity() - HEADER_SIZE);
      }

      // check if we read the entire frame
      if (!frameBuf.hasRemaining()) {
        // completed a frame
        frameBuf.flip();
        incompleteFrame.completed = true;
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
