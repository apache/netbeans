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
import java.util.function.BiConsumer;
import org.netbeans.api.sendopts.CommandException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

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
    private final List<Closeable> close = new ArrayList<>();

    private ConnectionSpec(Boolean listen, int port) {
        this.listen = listen;
        this.port = port;
    }

    public static ConnectionSpec parse(String spec) throws CommandException {
        if (spec == null || spec.isEmpty() || spec.equals("stdio")) { // NOI18N
            return new ConnectionSpec(null, -1);
        }
        final String listenPrefix = "listen:"; // NOI18N
        if (spec.startsWith(listenPrefix)) {
            int port = parsePort(spec.substring(listenPrefix.length()), spec);
            return new ConnectionSpec(true, port);
        }
        final String connectPrefix = "connect:"; // NOI18N
        if (spec.startsWith(connectPrefix)) {
            int port = parsePort(spec.substring(connectPrefix.length()), spec);
            return new ConnectionSpec(false, port);

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

    public void prepare(String prefix, InputStream in, OutputStream out, BiConsumer<InputStream, OutputStream> launcher) throws IOException {
        if (listen == null) {
            // stdio
            launcher.accept(in, out);
        } else if (listen) {
            // listen on TCP
            ServerSocket server = new ServerSocket(port, 1, Inet4Address.getLoopbackAddress());
            close.add(server);
            int localPort = server.getLocalPort();
            Thread listeningThread = new Thread(prefix + " listening at port " + localPort) {
                @Override
                public void run() {
                    while (true) {
                        try {
                            Socket socket = server.accept();
                            close.add(socket);
                            connectToSocket(socket, prefix, launcher);
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            };
            listeningThread.start();
            out.write((prefix + " listening at port " + localPort).getBytes());
            out.flush();
        } else {
            // connect to TCP
            final Socket socket = new Socket(Inet4Address.getLoopbackAddress(), port);
            connectToSocket(socket, prefix, launcher);
        }
    }

    private void connectToSocket(final Socket socket, String prefix, BiConsumer<InputStream, OutputStream> launcher) {
        final int connectTo = socket.getPort();
        Thread connectedThread = new Thread(prefix + " connected to " + connectTo) {
            @Override
            public void run() {
                try {
                    launcher.accept(socket.getInputStream(), socket.getOutputStream());
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        };
        connectedThread.start();
    }

    @Override
    public void close() throws IOException {
        for (Closeable c : close) {
            c.close();
        }
    }
}
