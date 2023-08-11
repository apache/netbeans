/*
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
package org.netbeans.modules.java.lsp.server;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import org.netbeans.api.sendopts.CommandException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Pair;

@NbBundle.Messages({
    "# {0} - specification to parse",
    "MSG_ConnectionSpecError=Cannot parse '{0}' as connection. Use 'stdio', 'listen:<port>' or 'connect:<port>'",
    "# {0} - specification to parse",
    "# {1} - port to parse",
    "MSG_PortParseError=Cannot parse '{1}' as port in '{0}'"
})
final class ConnectionSpec implements Closeable {
    private static final int HASH_LEN = 16;
    private final Boolean listen;
    private final boolean hash;
    private final int port;
    // @GuardedBy (this)
    private final List<Closeable> close = new ArrayList<>();
    // @GuardedBy (this)
    private final List<Closeable> closed = new ArrayList<>();

    private ConnectionSpec(Boolean listen, boolean hash, int port) {
        this.listen = listen;
        this.hash = hash;
        this.port = port;
    }

    public static ConnectionSpec parse(String spec) throws CommandException {
        if (spec == null || spec.isEmpty() || spec.equals("stdio")) { // NOI18N
            return new ConnectionSpec(null, false, -1);
        }
        final String listenPrefix = "listen:"; // NOI18N
        if (spec.startsWith(listenPrefix)) {
            int port = parsePort(spec.substring(listenPrefix.length()), spec);
            return new ConnectionSpec(true, false, port);
        }
        final String listenHashPrefix = "listen-hash:"; // NOI18N
        if (spec.startsWith(listenHashPrefix)) {
            int port = parsePort(spec.substring(listenHashPrefix.length()), spec);
            return new ConnectionSpec(true, true, port);
        }
        final String connectPrefix = "connect:"; // NOI18N
        if (spec.startsWith(connectPrefix)) {
            int port = parsePort(spec.substring(connectPrefix.length()), spec);
            return new ConnectionSpec(false, false, port);

        }
        throw new CommandException(555, Bundle.MSG_ConnectionSpecError(spec));
    }

    private static Integer parsePort(String port, String spec) throws CommandException {
        try {
            return Integer.parseInt(port);
        } catch (NumberFormatException ex) {
            throw new CommandException(556, Bundle.MSG_PortParseError(port, spec));
        }
    }

    public <ServerType extends LspSession.ScheduledServer> void prepare(
            String prefix, InputStream in, OutputStream out, LspSession session,
            BiConsumer<LspSession, ServerType> serverSetter,
            BiFunction<Pair<InputStream, OutputStream>, LspSession, ServerType> launcher) throws IOException {

        if (listen == null) {
            // stdio
            ServerType connectionObject = launcher.apply(Pair.of(in, out), session);
            serverSetter.accept(session, connectionObject);
            try {
                connectionObject.getRunningFuture().get();
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            } catch (ExecutionException ex) {
                Throwable cause = ex.getCause();
                if (cause instanceof Error) {
                    throw (Error) cause;
                }
                Exceptions.printStackTrace(ex);
            } finally {
                serverSetter.accept(session, null);
            }
        } else if (listen) {
            // listen on TCP
            ServerSocket server = new ServerSocket(port, 1, Inet4Address.getLoopbackAddress());
            close.add(server);

            char[] hashContent;
            if (hash) {
                byte[] hashBytes = new byte[HASH_LEN];
                new Random().nextBytes(hashBytes);
                hashContent = new char[hashBytes.length * 2];
                int idx = 0;
                for (byte b : hashBytes) {
                    hashContent[idx + 0] = Integer.toHexString((b >> 4) & 0xFF).charAt(0);
                    hashContent[idx + 1] = Integer.toHexString((b >> 0) & 0xFF).charAt(0);
                    idx += 2;
                }
            } else {
                hashContent = null;
            }
            int localPort = server.getLocalPort();
            Thread listeningThread = new Thread(prefix + " listening at port " + localPort) {
                @Override
                public void run() {
                    while (true) {
                        Socket socket = null;
                        try {
                            socket = server.accept();
                            close.add(socket);
                            connectToSocket(socket, prefix, session, serverSetter, launcher, hashContent);
                        } catch (IOException ex) {
                            if (isClosed(server)) {
                                break;
                            }
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            };
            listeningThread.start();
            StringBuilder message = new StringBuilder();
            message.append(prefix).append(" listening at port ").append(localPort);
            if (hash) {
                message.append(" with hash ");
                for (char c : hashContent) {
                    message.append(c);
                }
            }
            message.append("\n");
            out.write(message.toString().getBytes());
            out.flush();
        } else {
            // connect to TCP
            final Socket socket = new Socket(Inet4Address.getLoopbackAddress(), port);
            connectToSocket(socket, prefix, session, serverSetter, launcher, null);
        }
    }

    private <ServerType extends LspSession.ScheduledServer> void connectToSocket(
            final Socket socket, String prefix, LspSession session,
            BiConsumer<LspSession, ServerType> serverSetter,
            BiFunction<Pair<InputStream, OutputStream>, LspSession, ServerType> launcher,
            char[] hashContent) {

        final int connectTo = socket.getPort();
        Thread connectedThread = new Thread(prefix + " connected to " + connectTo) {
            @Override
            public void run() {
                try {
                    InputStream in = socket.getInputStream();

                    if (hashContent != null) {
                        for (char c : hashContent) {
                            byte b = (byte) in.read();

                            if (b != c) {
                                throw new IOException("Hash validation failed!");
                            }
                        }
                    }

                    ServerType connectionObject = launcher.apply(Pair.of(in, socket.getOutputStream()), session);
                    serverSetter.accept(session, connectionObject);
                    connectionObject.getRunningFuture().get();
                } catch (IOException | InterruptedException | ExecutionException ex) {
                    if (!isClosed(socket)) {
                        Exceptions.printStackTrace(ex);
                    }
                } finally {
                    serverSetter.accept(session, null);
                }
            }
        };
        connectedThread.start();
    }
    
    private boolean isClosed(Closeable c) {
        synchronized (this) {
            return closed.contains(c);
        }
    }

    @Override
    public void close() throws IOException {
        synchronized (this) {
            for (Closeable c : close) {
                try {
                    c.close();
                    closed.add(c);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }
}
