/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.jshell.launch;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 * Wraps nonblocking sockets with blocking I/O streams. Provides an independent
 * socket/stream close callback. Close listener will be called exactly once per 
 * channel - so when read + write streams of the same channel both register , only one callback
 * will be called.
 * <p/>
 * Note that JDK has no specific Operation for 'close'. One must wait for READ, and that READ
 * is repeated forever until the consumer does not fully consume the input buffer contents. The NIOStreams.Input
 * will eagerly read some portion of input, assuming the messages out-of-band messages from the agent are typically
 * small.
 * 
 * @author sdedic
 */
class NIOStreams {
    /**
     * The preread buffer size
     */
    private static final int PREREAD_BUFFER_SIZE = 100;
    
    private static final Logger LOG = Logger.getLogger(NIOStreams.class.getName());
    
    private static final RequestProcessor CLOSE_RP = new RequestProcessor("SocketChannel close handler"); // NOI18N
    private static final int WATCH_TIMEOUT = 5000;
    
    // @GuardedBy(CLOSE_RP)
    private static StreamWatch WATCHER;

    /**
     * Creates a Ouptut stream over a nonblocking socket. The stream does NOT support close notification callbacks.
     * It is possible to specify write timeout after which the operation fails with an {@link IOException.
     * 
     * @param channel the nonblocking channel.
     * @param timeout timeout to write.
     * @return initialized OutputStream.
     * @throws IOException 
     */
    public static OutputStream createOutputStream(SocketChannel channel, int timeout) throws IOException {
        assert !channel.isBlocking();
        OutputStream o = new Output(channel, timeout);
        LOG.log(Level.FINER, "Created OutputStream {0} for socket {1}", new Object[] { o, channel });
        return o;
    }
    
    private static StreamWatch watch() throws IOException {
        StreamWatch watcher;
        synchronized (CLOSE_RP) {
            watcher = WATCHER;
            if (WATCHER == null) {
                WATCHER = watcher = new StreamWatch();
                CLOSE_RP.post(watcher);
            }
        }
        return watcher;
    }
    
    /**
     * Creates an InputStream with optional notification for remote close. 
     * @param channel the underlying nonblocking channel.
     * @param closeCallback
     * @return
     * @throws IOException 
     */
    public static InputStream createInputStream(SocketChannel channel, Consumer<SocketChannel> closeCallback) throws IOException {
        assert !channel.isBlocking();
        Input i = new Input(channel, closeCallback);
        LOG.log(Level.FINER, "Created InputStream {0} for socket {1}", new Object[] { i, channel });
        if (false && closeCallback != null) {
            InputData e = new InputData();
            e.closeCallback = closeCallback;
            e.input = i;
            watch().add(channel, e);
        }
        return i;
    }
    
    
    private static class InputData {
        Consumer<SocketChannel> closeCallback;
        Input                   input;
    }
    
    /**
     * Runs in background and polls READ on opened Inputs. If READ comes, attempts to pre-read
     * the input in order to find potential closed stream.
     * <p/>
     * IMHO it is not well possible to add a channel to a selector from another thread
     * without some kind of race. So the implementation will just enqueue request to add a 
     * selector key and wake up the selector for the case it is waiting at the moment.
     */
    private static final class StreamWatch implements Runnable {
        /**
         * Waitable selector for opened streams
         */
        private final Selector        openedStreams = Selector.open();
        
        // @GuardedBy(requests)
        private final Set<Channel>    watchedChannels = new HashSet<>();
        /**
         * Request to add to the selector.
         */
        // @GuardedBy(self)
        private final Map<SelectableChannel, InputData>  requests = new HashMap<>();
        
        StreamWatch() throws IOException {
        }
        
        public void add(SocketChannel channel, InputData info) throws IOException {
            synchronized (requests) {
                if (!watchedChannels.add(channel)) {
                    return;
                }
                openedStreams.wakeup();
                requests.put(channel, info);
            }
        }
        
        private void processRegistrations() {
            List<Map.Entry<SelectableChannel, InputData>> closedRequests = new ArrayList<>();
            synchronized (requests) {
                for (Map.Entry<SelectableChannel, InputData> e : requests.entrySet()) {
                        try {
                            SelectionKey key = e.getKey().register(openedStreams, SelectionKey.OP_READ);
                            key.attach(e.getValue());
                        } catch (ClosedChannelException ex) {
                            // sorry :)
                            closedRequests.add(e);
                        }

                }
                requests.clear();
            }

            for (Map.Entry<SelectableChannel, InputData> en : closedRequests) {
                InputData e = en.getValue();
                if (e.closeCallback != null) {
                    e.closeCallback.accept((SocketChannel)en.getKey());
                }
            }
        }
        
        @Override
        public void run() {
            for (;;) {
                try {
                    processRegistrations();
                    
                    int sel;
                    sel = openedStreams.select(WATCH_TIMEOUT);
                    Set<SelectionKey> keys = openedStreams.selectedKeys();

                    // remove open channels from the selector keys
                    for (Iterator<SelectionKey> it  = keys.iterator(); it.hasNext() ;) {
                        SelectionKey k = it.next();
                        Channel ch = k.channel();
                        // notify for invalid/cancelled keys and closed channels.
                        if (k.isValid() && ch.isOpen()) {
                            InputData info = (InputData)k.attachment();
                            if (info.input == null || !info.input.preReadContents()) {
                                // the channel is still alive
                                it.remove();
                            }
                        }
                    }
                    if (keys.isEmpty()) {
                        continue;
                    }
                    
                    List<Channel> removed = new ArrayList<>();
                    LOG.log(Level.FINER, "Closehandler woke up with {0} channels, keys: {1}", new Object[] {sel, keys});
                    for (SelectionKey k : keys) {
                        InputData info = (InputData)k.attachment();
                        Channel ch = k.channel();
                        removed.add(ch);
                        try {
                            if (info.input != null) {
                                // prevent sending the notification 2 times.
                                info.input.notifyClose();
                            } else {
                                info.closeCallback.accept((SocketChannel)ch);
                            }
                            k.cancel();
                        } catch (Exception ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                    synchronized (requests) {
                        watchedChannels.removeAll(removed);
                    }
                } catch (IOException ex) {
                    LOG.log(Level.INFO, "Exception occurred during close processing", ex);
                }
            }
        }
    }
    
    /**
     * Simple implementation of Output, which will block until the SocketChannel
     * does not accept all the data to be sent.
     */
    private static final class Output extends OutputStream {
        private final SocketChannel channel;
        private final Selector  selector;
        private final int timeout;
        
        private ByteBuffer singleBuffer = ByteBuffer.allocate(1);

        public Output(SocketChannel channel, int timeout) throws IOException {
            this.timeout = timeout;
            
            this.channel = channel;
            selector = Selector.open();
            channel.register(selector, SelectionKey.OP_WRITE);
        }
        
        @Override
        public void close() throws IOException {
            selector.wakeup();
            channel.close();
            super.close();
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            ByteBuffer bb = ByteBuffer.wrap(b, off, len);
            
            while (bb.remaining() > 0) {
                channel.write(bb);
                if (bb.remaining() == 0) {
                    return;
                }
                // wait for socket to become ready again
                int sel = selector.select(timeout);
                if (sel == 0) {
                    throw new IOException("Timed out");
                } else if (!channel.isConnected()) {
                    throw new EOFException("Closed");
                }
            }
        }

        @Override
        public void write(int b) throws IOException {
            byte out = (byte)(b & 0xff);
            singleBuffer.clear();
            singleBuffer.put(out);
            singleBuffer.flip();
            int sel;
            
            do {
                int written = channel.write(singleBuffer);
                if (written > 0) {
                    return;
                }
                sel = selector.select(timeout);
                if (!channel.isConnected()) {
                    throw new EOFException("Connection closed");
                }
            } while (sel > 0);
            throw new IOException("Timed out");
        }
        
        public String toString() {
            return "NIO-Output@" + Integer.toString(hashCode(), 16);
        }
    }
    
    /**
     * This inputStream will eagerly read everything coming from the socket. The reading is done because
     * it is probably the only way how to detect an end-of-stream condition.
     */
    public static class Input extends InputStream {
        /**
         * The close callback. Will be called if a regular read() encounters EOF.
         */
        private Consumer<SocketChannel> closeCallback;
        private final SocketChannel channel;
        private final Selector  selector;
        private int   timeout = 0;
        private ByteBuffer  prereadContents = ByteBuffer.allocate(PREREAD_BUFFER_SIZE);
        
        /**
         * Will be set to true once EOF is encountered. Cannot be reset.
         */
        private boolean eof;
        
        private boolean localClose;
        
        private final ByteBuffer singleBuffer = ByteBuffer.allocate(1);

        public Input(SocketChannel channel, Consumer<SocketChannel> closeCallback) throws IOException {
            this.closeCallback = closeCallback;
            this.channel = channel;
            selector = Selector.open();
            channel.register(selector, SelectionKey.OP_READ);
        }
        
        boolean preReadContents() throws IOException {
            synchronized (this) {
                int count;
                while (true) {
                    count = channel.read(prereadContents);
                    if (prereadContents.remaining() == 0) {
                        ByteBuffer expand = ByteBuffer.allocate(prereadContents.capacity() * 2);
                        prereadContents.flip();
                        expand.put(prereadContents);
                        prereadContents = expand;
                    } else {
                        break;
                    }
                }
                LOG.log(Level.FINE, "Preread channel {0}: got {1} bytes, buffer: {2}", new Object[] { channel, prereadContents.position(), prereadContents.toString() });
                if (count != -1 && channel.isOpen()) {
                    return false;
                }
                // reached end-of-stream
                eof = true;
            }
            return true; 
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int totalRead = 0;
            ByteBuffer bb = ByteBuffer.wrap(b, off, len);
            
            int read = 0;
            while (true) {
                synchronized (this) {
                    // first empty the preread buffer, if not empty
                    if (prereadContents.position() > 0) {
                        prereadContents.flip();
                        int end = prereadContents.limit();
                        int l = Math.min(end, len);
                        prereadContents.limit(l);
                        // transfer bytes
                        bb.put(prereadContents);
                        // restore the read limit
                        prereadContents.limit(end);
                        // compact the buffer, moving contents down and restore position and limit
                        prereadContents.compact();
                        LOG.log(Level.FINE, "Preread from channel {0}: given {1} bytes, preread buffer: {2}", new Object[] { channel, l, prereadContents.toString() });
                        if (l == len) {
                            return l;
                        }

                        off += l;
                        len -= l;
                        totalRead = l;
                    }
                    if (eof || !channel.isConnected()) {
                        read = -1;
                        break;
                    }
                }
                LOG.log(Level.FINE, "Reading from channel: {0}", channel);
                read = channel.read(bb);
                LOG.log(Level.FINE, "Reading from channel: {0}, read {1} bytes", new Object[] { channel, read });
                if (read < 0) {
                    break;
                }
                totalRead += read;
                if (bb.remaining() == 0) {
                    return totalRead;
                }
                selector.select(timeout);
                synchronized (this) {
                    if (eof || !channel.isConnected()) {
                        read = -1;
                        break;
                    }
                }
            }
            if (totalRead == 0 && read < 0) {
                notifyClose();
                return -1;
            } else {
                return totalRead;
            }
        }

        @Override
        public void close() throws IOException {
            synchronized (this) {
                eof = true;
                localClose = true;
                selector.wakeup();
            }
            super.close();
        }
        
        private void notifyClose() {
            Consumer<SocketChannel> c;
            synchronized (this) {
                c = this.closeCallback;
                closeCallback = null;
            }
            if (c != null) {
                c.accept(channel);
            }
        }

        @Override
        public int read() throws IOException {
            boolean end = false;
            
            while (!end) {
                synchronized (this) {
                    if (prereadContents.position() > 0) {
                        prereadContents.limit(prereadContents.position());
                        prereadContents.position(0);
                        int b = prereadContents.get();
                        prereadContents.compact();
                        LOG.log(Level.FINE, "Single read from channel: {0}, got from preread. Buffe state: {1}", new Object[] { channel, prereadContents});
                        return b;
                    }
                    end = eof || !channel.isConnected();
                }
                singleBuffer.clear();
            
                int read = channel.read(singleBuffer);
                LOG.log(Level.FINE, "Single read from channel: {0}, got: {1}", new Object[] { channel, read });
                if (read > 0) {
                    singleBuffer.flip();
                    return singleBuffer.get();
                } else if (read == -1) {
                    break;
                }
                selector.select(timeout);
                synchronized (this) {
                   end = eof || !channel.isConnected();
                }
            }
            notifyClose();
            return -1;
        }
        
        public String toString() {
            return "NIO-Input@" + Integer.toString(hashCode(), 16);
        }
    }
}
