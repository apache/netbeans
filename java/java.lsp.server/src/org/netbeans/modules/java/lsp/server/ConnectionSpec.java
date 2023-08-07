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
import java.util.concurrent.ExecutionException;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import org.netbeans.api.sendopts.CommandException;
import org.netbeans.modules.java.lsp.server.Pipe.Connection;
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
    private final Boolean listen;
    private final int port;
    private final String pipe;
    // @GuardedBy (this)
    private final List<AutoCloseable> close = new ArrayList<>();
    // @GuardedBy (this)
    private final List<AutoCloseable> closed = new ArrayList<>();

    private ConnectionSpec(Boolean listen, int port, String pipe) {
        this.listen = listen;
        this.port = port;
        this.pipe = pipe;
    }

    public static ConnectionSpec parse(String spec) throws CommandException {
        if (spec == null || spec.isEmpty() || spec.equals("stdio")) { // NOI18N
            return new ConnectionSpec(null, -1, null);
        }
        final String listenPrefix = "listen:"; // NOI18N
        if (spec.startsWith(listenPrefix)) {
            int port = parsePort(spec.substring(listenPrefix.length()), spec);
            return new ConnectionSpec(true, port, null);
        }
        final String listenPipePrefix = "listen-pipe"; // NOI18N
        if (spec.equals(listenPipePrefix)) {
            return new ConnectionSpec(true, -1, null);
        }
        final String connectPrefix = "connect:"; // NOI18N
        if (spec.startsWith(connectPrefix)) {
            int port = parsePort(spec.substring(connectPrefix.length()), spec);
            return new ConnectionSpec(false, port, null);

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
            if (port != (-1)) {
                // listen on TCP
                ServerSocket server = new ServerSocket(port, 1, Inet4Address.getLoopbackAddress());
                close.add(server);
                int localPort = server.getLocalPort();
                Thread listeningThread = new Thread(prefix + " listening at port " + localPort) {
                    @Override
                    public void run() {
                        while (true) {
                            Socket socket = null;
                            try {
                                socket = server.accept();
                                close.add(socket);
                                connectToSocket(socket, prefix, session, serverSetter, launcher);
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
                out.write((prefix + " listening at port " + localPort).getBytes());
                out.flush();
            } else {
                // listen on named pipe/UNIX Domain Socket:
                Pipe pipe = Pipe.createListeningPipe(prefix);
                //TODO: multitenancy?
                close.add(pipe);
                Thread listeningThread = new Thread(prefix + " listening at pipe " + pipe.getName()) {
                    @Override
                    public void run() {
                        while (true) {
                            Connection connection = null;
                            try {
                                connection = pipe.connect();
                                close.add(connection);
                                connectToSocket(connection, pipe.getName(), prefix, session, serverSetter, launcher);
                            } catch (IOException ex) {
                                if (isClosed(connection)) {
                                    break;
                                }
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    }
                };
                listeningThread.start();
                out.write((prefix + " listening at pipe " + pipe.getName()).getBytes());
                out.flush();
            } 
        } else {
            // connect to TCP
            final Socket socket = new Socket(Inet4Address.getLoopbackAddress(), port);
            connectToSocket(socket, prefix, session, serverSetter, launcher);
        }
    }

    private <ServerType extends LspSession.ScheduledServer> void connectToSocket(
            final Socket socket, String prefix, LspSession session,
            BiConsumer<LspSession, ServerType> serverSetter,
            BiFunction<Pair<InputStream, OutputStream>, LspSession, ServerType> launcher) {

        final int connectTo = socket.getPort();
        Thread connectedThread = new Thread(prefix + " connected to " + connectTo) {
            @Override
            public void run() {
                try {
                    ServerType connectionObject = launcher.apply(Pair.of(socket.getInputStream(), socket.getOutputStream()), session);
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
    
    private <ServerType extends LspSession.ScheduledServer> void connectToSocket(
            final Connection connection, String name, String prefix, LspSession session,
            BiConsumer<LspSession, ServerType> serverSetter,
            BiFunction<Pair<InputStream, OutputStream>, LspSession, ServerType> launcher) {

        Thread connectedThread = new Thread(prefix + " connected to " + name) {
            @Override
            public void run() {
                try {
                    ServerType connectionObject = launcher.apply(Pair.of(connection.getIn(), connection.getOut()), session);
                    serverSetter.accept(session, connectionObject);
                    connectionObject.getRunningFuture().get();
                } catch (InterruptedException | ExecutionException ex) {
                    if (!isClosed(connection)) {
                        Exceptions.printStackTrace(ex);
                    }
                } finally {
                    serverSetter.accept(session, null);
                }
            }
        };
        connectedThread.start();
    }
    
    private boolean isClosed(AutoCloseable c) {
        synchronized (this) {
            return closed.contains(c);
        }
    }

    @Override
    public void close() throws IOException {
        synchronized (this) {
            for (AutoCloseable c : close) {
                try {
                    c.close();
                    closed.add(c);
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }
}
