/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.java.lsp.server.htmlui;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.netbeans.html.boot.spi.Fn;

final class SimpleServer extends HttpServer<SimpleServer.ReqRes, SimpleServer.ReqRes, Object, SimpleServer.Context> {
    private final Map<String, Handler> maps = new TreeMap<>((s1, s2) -> {
        if (s1.length() != s2.length()) {
            return s2.length() - s1.length();
        }
        return s2.compareTo(s1);
    });
    private int max;
    private int min;
    /**
     * @GuardedBy("this")
     */
    private ServerSocketChannel server;
    /**
     * @GuardedBy("this")
     */
    private Selector connection;
    /**
     * @GuardedBy("this")
     */
    private Thread processor;
    private final List<Runnable> pendingActions = new ArrayList<>();

    private static final Pattern PATTERN_GET = Pattern.compile("(OPTIONS|HEAD|GET|POST|PUT|DELETE) */([^ \\?]*)(\\?[^ ]*)?");
    private static final Pattern PATTERN_HOST = Pattern.compile(".*^Host: *(.*):([0-9]+)$", Pattern.MULTILINE);
    private static final Pattern PATTERN_LENGTH = Pattern.compile(".*^Content-Length: ([0-9]+)$", Pattern.MULTILINE);
    static final Logger LOG = Logger.getLogger(SimpleServer.class.getName());

    private final Random random;

    SimpleServer() {
        this(new Random());
    }

    SimpleServer(Random random) {
        this.random = random;
    }

    @Override
    void addHttpHandler(Handler h, String path) {
        if (!path.startsWith("/")) {
            throw new IllegalStateException("Shall start with /: " + path);
        }
        maps.put(path.substring(1), h);
    }

    @Override
    void init(int from, int to) throws IOException {
        this.connection = Selector.open();
        this.min = from;
        this.max = to;
    }

    @Override
    synchronized void start() throws IOException {
        LOG.log(Level.INFO, "Listening for HTTP connections on port {0}", getServer().socket().getLocalPort());
        processor = new Thread(this::mainLoop, "HTTP server");
        processor.start();
    }

    private final synchronized Thread getProcessorThread() {
        return processor;
    }

    final void assertThread() {
        assert Thread.currentThread() == getProcessorThread();
    }

    @Override
    String getRequestURI(ReqRes r) {
        assertThread();
        return "/" + r.url;
    }

    @Override
    String getServerName(ReqRes r) {
        assertThread();
        return r.hostName;
    }

    @Override
    int getServerPort(ReqRes r) {
        assertThread();
        return r.hostPort;
    }

    @Override
    String getParameter(ReqRes r, String id) {
        assertThread();
        return (String) r.args.get(id);
    }

    @Override
    String getMethod(ReqRes r) {
        assertThread();
        return r.method;
    }

    @Override
    String getBody(ReqRes r) {
        assertThread();
        if (r.body == null) {
            return "";
        } else {
            return new String(r.body.array(), StandardCharsets.UTF_8);
        }
    }

    static int endOfHeader(String header) {
        return header.indexOf("\r\n\r\n");
    }

    @Override
    String getHeader(ReqRes r, String key) {
        assertThread();
        for (String l : r.header.split("\r\n")) {
            if (l.isEmpty()) {
                break;
            }
            if (l.startsWith(key + ":")) {
                return l.substring(key.length() + 1).trim();
            }
        }
        return null;
    }

    @Override
    Writer getWriter(ReqRes r) {
        assertThread();
        return r.writer;
    }

    @Override
    void setContentType(ReqRes r, String contentType) {
        assertThread();
        r.contentType = contentType;
    }

    @Override
    void setStatus(ReqRes r, int status) {
        assertThread();
        r.status = status;
    }

    @Override
    OutputStream getOutputStream(ReqRes r) {
        assertThread();
        return r.os;
    }

    @Override
    void suspend(ReqRes r) {
        assertThread();
        r.suspended = true;
        r.updateOperations();
    }

    @Override
    void resume(ReqRes r, Runnable whenReady) {
        connectionWakeup(() -> {
            assertThread();
            r.suspended = false;
            r.updateOperations();
            whenReady.run();
        });
    }

    @Override
    void setCharacterEncoding(ReqRes r, String encoding) {
        if (!encoding.equals("UTF-8")) {
            throw new IllegalStateException(encoding);
        }
    }

    @Override
    void addHeader(ReqRes r, String name, String value) {
        assertThread();
        r.headers.put(name, value);
    }

    @Override
    <WebSocket> void send(WebSocket socket, String s) {
    }

    /**
     * @return the port to listen to
     */
    @Override
    public int getPort() {
        try {
            return getServer().socket().getLocalPort();
        } catch (IOException ex) {
            return -1;
        }
    }

    synchronized void connectionWakeup(Runnable runOnMainLoop) {
        Selector localConnection = this.connection;
        this.pendingActions.add(runOnMainLoop);
        if (localConnection != null) {
            localConnection.wakeup();
        }
    }

    private void mainLoop() {
        ByteBuffer bb = ByteBuffer.allocate(2048);
        while (Thread.currentThread() == getProcessorThread()) {
            ServerSocketChannel localServer;
            Selector localConnection;
            Runnable[] pendings;

            SocketChannel toClose = null;
            try {
                synchronized (this) {
                    localServer = this.getServer();
                    localConnection = this.connection;
                    pendings = this.pendingActions.toArray(new Runnable[0]);
                    this.pendingActions.clear();
                }

                LOG.log(Level.FINEST, "Before select status: open server{0}, open connection {1}, pending {2}",
                        new Object[]{localServer.isOpen(), localConnection.isOpen(), pendings.length}
                );

                for (Runnable r : pendings) {
                    r.run();
                }

                int amount = localConnection.select();

                LOG.log(Level.FINEST, "After select: {0}", amount);
                if (amount == 0) {
                    LOG.log(Level.FINE, "No amount after select: {0}", amount);
                }

                Set<SelectionKey> readyKeys = localConnection.selectedKeys();
                Iterator<SelectionKey> it = readyKeys.iterator();
                while (it.hasNext()) {
                    SelectionKey key = it.next();
                    LOG.log(Level.FINEST, "Handling key {0}", key.attachment());
                    it.remove();

                    if (key.isAcceptable()) {
                        try {
                            SocketChannel channel = localServer.accept();
                            channel.configureBlocking(false);
                            SelectionKey another = channel.register(
                                    localConnection, SelectionKey.OP_READ
                            );
                            another.attach(new ReadHeader());
                        } catch (ClosedByInterruptException ex) {
                            LOG.log(Level.WARNING, "Interrupted while accepting", ex);
                            server.close();
                            server = null;
                            LOG.log(Level.INFO, "Accept server reset");
                        }
                    } else if (key.isReadable()) {
                        ((Buffer) bb).clear();
                        SocketChannel channel = (SocketChannel) key.channel();
                        toClose = channel;
                        channel.read(bb);
                        ((Buffer) bb).flip();

                        if (key.attachment() instanceof ReadHeader) {
                            ReadHeader readHeader = (ReadHeader) key.attachment();
                            ReqRes nextKey = readHeader.process(key, bb);
                            if (nextKey != null) {
                                key.attach(nextKey);
                                nextKey.updateOperations();
                            }
                        } else if (key.attachment() instanceof ReqRes) {
                            ReqRes req = (ReqRes) key.attachment();
                            req.readBody(key, bb);
                            req.updateOperations();
                        }
                    } else if (key.isWritable()) {
                        SocketChannel channel = (SocketChannel) key.channel();
                        toClose = channel;
                        if (key.attachment() instanceof ReqRes) {
                            ReqRes request = (ReqRes) key.attachment();
                            WriteReply write = request.handle(channel);
                            if (write != null) {
                                key.attach(write);
                                write.updateOperations();
                            }
                        } else if (key.attachment() instanceof WriteReply) {
                            WriteReply write = (WriteReply) key.attachment();
                            write.output(channel);
                        }
                    }
                }
            } catch (ThreadDeath td) {
                throw td;
            } catch (Throwable t) {
                LOG.log(Level.SEVERE, "Exception while handling request", t);
                if (toClose != null) {
                    try {
                        toClose.close();
                    } catch (IOException ioEx) {
                        LOG.log(Level.INFO, "While closing", ioEx);
                    }
                }
            }
        }

        synchronized (this) {
            try {
                LOG.fine("Closing connection");
                this.connection.close();
                LOG.fine("Closing server");
                this.getServer().close();
            } catch (IOException ex) {
                LOG.log(Level.WARNING, null, ex);
            } finally {
                notifyAll();
            }
        }
        LOG.fine("All notified, exiting server");
    }

    private Handler findHandler(String url) {
        LOG.log(Level.FINE, "Searching for handler for page {0}", url);
        for (Map.Entry<String, Handler> entry : maps.entrySet()) {
            if (url.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        throw new IllegalStateException("No mapping for " + url + " among " + maps);
    }

    private static void parseArgs(final Map<String, ? super String> context, final String args) throws UnsupportedEncodingException {
        if (args != null) {
            for (String arg : args.substring(1).split("&")) {
                String[] valueAndKey = arg.split("=");
                if (valueAndKey.length != 2) {
                    continue;
                }

                String key = URLDecoder.decode(valueAndKey[1], "US-ASCII");
                for (int idx = 0;;) {
                    idx = key.indexOf("%", idx);
                    if (idx == -1) {
                        break;
                    }
                    int ch = Integer.parseInt(key.substring(idx + 1, idx + 3), 16);
                    key = key.substring(0, idx) + (char) ch + key.substring(idx + 3);
                    idx++;
                }

                context.put(valueAndKey[0], key);
            }
        }
    }

    @Override
    public synchronized void shutdownNow() {
        Thread inter = processor;
        if (inter != null) {
            processor = null;
            LOG.fine("Processor cleaned");
            inter.interrupt();
            LOG.fine("Processor interrupted");
            try {
                wait(5000);
            } catch (InterruptedException ex) {
                LOG.log(Level.WARNING, null, ex);
            }
            LOG.fine("After waiting");
        }
    }

    /**
     * Computes todays's date .
     */
    static String date(Date date) {
        return date("Date: ", date != null ? date : new Date());
    }

    static String date(String prefix, Date date) {
        DateFormat f = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL, Locale.US);
        f.setTimeZone(TimeZone.getTimeZone("GMT")); // NOI18N
        return prefix + f.format(date);
    }

    public synchronized ServerSocketChannel getServer() throws IOException {
        if (server == null) {
            ServerSocketChannel s = ServerSocketChannel.open();
            s.configureBlocking(false);

            for (int i = min; i <= max; i++) {
                int at = min + random.nextInt(max - min + 1);
                InetSocketAddress address = new InetSocketAddress(at);
                try {
                    s.socket().bind(address);
                } catch (IOException ex) {
                    LOG.log(Level.FINE, "Cannot bind to " + at, ex);
                    continue;
                }
                server = s;
                break;
            }

            server.register(this.connection, SelectionKey.OP_ACCEPT);
        }
        return server;
    }

    final class Context implements ThreadFactory {

        private final String id;
        Executor RUN;
        Thread RUNNER;

        Context(String id) {
            this.id = id;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, "Processor for " + id);
            RUNNER = t;
            return t;
        }
    }

    @Override
    Context initializeRunner(String id) {
        Context c = new Context(id);
        c.RUN = Executors.newSingleThreadExecutor(c);
        return c;
    }

    @Override
    void runSafe(Context c, Runnable r, Fn.Presenter presenter) {
        class Wrap implements Runnable {

            @Override
            public void run() {
                if (presenter != null) {
                    try ( Closeable c = Fn.activate(presenter)) {
                        r.run();
                    } catch (IOException ex) {
                        // go on
                    }
                } else {
                    r.run();
                }
            }
        }
        if (c.RUNNER == Thread.currentThread()) {
            if (presenter != null) {
                Runnable w = new Wrap();
                w.run();
            } else {
                r.run();
            }
        } else {
            Runnable w = new Wrap();
            c.RUN.execute(w);
        }
    }

    final class ReadHeader {

        private final StringBuilder buffer = new StringBuilder();

        final ReqRes process(SelectionKey key, ByteBuffer chunk) throws UnsupportedEncodingException {
            String text = new String(chunk.array(), 0, chunk.limit(), "US-ASCII");
            buffer.append(text);
            int fullHeader = buffer.indexOf("\r\n\r\n");
            if (fullHeader == -1) {
                return null;
            }
            String header = text.substring(0, fullHeader);

            Matcher m = PATTERN_GET.matcher(header);
            String url = m.find() ? m.group(2) : null;
            String args = url != null && m.groupCount() == 3 ? m.group(3) : null;
            String method = m.group(1);

            Map<String, String> context;
            if (args != null) {
                Map<String, String> c = new HashMap<>();
                parseArgs(c, args);
                context = Collections.unmodifiableMap(c);
            } else {
                context = Collections.emptyMap();
            }

            Matcher length = PATTERN_LENGTH.matcher(header);
            ByteBuffer body = null;
            if (length.find()) {
                int contentLength = Integer.parseInt(length.group(1));
                body = ByteBuffer.allocate(contentLength);
                ((Buffer) chunk).position(fullHeader + 4);
                body.put(chunk);
            }

            Handler h = findHandler(url);
            Matcher hostMatch = PATTERN_HOST.matcher(header);
            String host = null;
            int port = -1;
            if (hostMatch.find()) {
                host = hostMatch.group(1);
                port = Integer.parseInt(hostMatch.group(2));
            }
            if (host != null) {
                LOG.log(Level.FINER, "Host {0}:{1}", new Object[]{host, port});
            }
            return new ReqRes(h, key, url, context, host, port, header, method, body);
        }
    }

    final class ReqRes {

        private final SelectionKey delegate;
        private final Handler h;
        final String url;
        final String hostName;
        final int hostPort;
        final Map<String, String> args;
        final String header;
        final String method;
        final ByteBuffer body;
        private final ByteArrayOutputStream os = new ByteArrayOutputStream();
        final Writer writer = new OutputStreamWriter(os, StandardCharsets.UTF_8);
        final Map<String, String> headers = new LinkedHashMap<>();
        String contentType;
        int status = 200;
        boolean computed;
        boolean suspended;

        public ReqRes(
                Handler h, SelectionKey delegate,
                String url, Map<String, String> args, String host,
                int port, String header, String method, ByteBuffer body
        ) {
            this.h = h;
            this.delegate = delegate;
            this.url = url;
            this.hostName = host;
            this.hostPort = port;
            this.header = header;
            this.args = args;
            this.method = method;
            this.body = body;
        }

        void updateOperations() {
            if (body != null && body.remaining() > 0) {
                delegate.interestOps(SelectionKey.OP_READ);
            } else if (suspended) {
                delegate.interestOps(0);
            } else {
                delegate.interestOps(SelectionKey.OP_WRITE);
            }
        }

        public WriteReply handle(SocketChannel channel) throws IOException {
            if (!computed) {
                computed = true;
                h.service(SimpleServer.this, this, this);
            }
            if (suspended) {
                channel.write(ByteBuffer.allocate(0));
                return null;
            }

            if (contentType == null) {
                contentType = "content/unknown"; // NOI18N
            }

            ByteBuffer bb = ByteBuffer.allocate(8192);
            ((Buffer) bb).clear();

            LOG.log(Level.FINE, "Serving page request {0}", url); // NOI18N
            ((Buffer) bb).clear();
            putString(bb, "HTTP/1.1 " + status + "\r\n");
            putString(bb, "Connection: close\r\n");
            putString(bb, "Server: Browser presenter\r\n");
            putString(bb, date(null));
            putString(bb, "\r\n");
            putString(bb, "Content-Type: " + contentType + "\r\n");
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                putString(bb, entry.getKey() + ":" + entry.getValue() + "\r\n");
            }
            putString(bb, "Pragma: no-cache\r\nCache-control: no-cache\r\n");
            putString(bb, "\r\n");
            ((Buffer) bb).flip();

            return new WriteReply(delegate, url, bb, ByteBuffer.wrap(toByteArray()));
        }

        byte[] toByteArray() throws IOException {
            writer.close();
            return os.toByteArray();
        }

        void readBody(SelectionKey key, ByteBuffer chunk) {
            body.put(chunk);
        }

        @Override
        public String toString() {
            return "Request[" + method + ":" + url + "]";
        }
    }

    final class WriteReply {

        private final SelectionKey delegate;
        private final String url;
        private final ByteBuffer header;
        private final ByteBuffer body;

        WriteReply(SelectionKey delegate, String url, ByteBuffer header, ByteBuffer body) {
            this.delegate = delegate;
            this.url = url;
            this.header = header;
            this.body = body;
        }

        void updateOperations() {
            delegate.interestOps(SelectionKey.OP_WRITE);
        }

        void output(SocketChannel channel) throws IOException {
            try {
                if (header.remaining() > 0) {
                    channel.write(header);
                    return;
                }
                if (body.remaining() > 0) {
                    channel.write(body);
                } else {
                    channel.close();
                }
            } finally {
                if (!channel.isOpen()) {
                    LOG.log(Level.FINE, "channel for {0} not open, closing", url);
                    delegate.attach(null);
                    delegate.cancel();
                }
            }

        }
    }

    private static void putString(ByteBuffer bb, String text) throws UnsupportedEncodingException {
        bb.put(text.getBytes("US-ASCII"));
    }
}
