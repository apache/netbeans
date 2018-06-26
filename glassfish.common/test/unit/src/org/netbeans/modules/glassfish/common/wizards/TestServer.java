/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.glassfish.common.wizards;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author Petr Hejl
 */
public class TestServer {

    private final ServerSocket serverSocket;

    private Thread serverThread;

    public TestServer(ServerSocket serverSocket) {
        assert serverSocket != null;
        this.serverSocket = serverSocket;
    }

    public static TestServer runSimpleServer(int port, int range) throws IOException {
        ServerSocket socket;
        int remaining = range;
        while (true) {
            try {
                socket = new ServerSocket(port + range - remaining);
                break;
            } catch (IOException ex) {
                remaining--;
                if (remaining <= 0) {
                    throw ex;
                }
            }
        }

        final TestServer server = new TestServer(socket);
        server.serverThread = new Thread() {

            @Override
            public void run() {
                try {
                    while (true) {
                        ServerThread service = new ServerThread(server.serverSocket.accept());
                        service.start();
                    }
                } catch (SocketException ex) {
                    // exiting
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

        };
        server.serverThread.start();
        return server;
    }

    public int getPort() {
        return serverSocket.getLocalPort();
    }

    public void cancel() throws IOException {
        serverSocket.close();
        serverThread.interrupt();
    }

    private static class ServerThread extends Thread {

        private static final Pattern HTTP_PATTERN = Pattern.compile("GET (/[^\\s]*) HTTP/1\\.[01]");
        private final Socket socket;

        public ServerThread(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                try {
                    String inputLine = in.readLine();
                    if (inputLine != null) {
                        Matcher matcher = HTTP_PATTERN.matcher(inputLine);
                        if (matcher.matches()) {
                            while ((inputLine = in.readLine()) != null) {
                                if (inputLine.equals("")) {
                                    break;
                                }
                            }
                            String resource = matcher.group(1);
                            if (resource.endsWith(".txt")) {
                                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                                try {
                                    out.print("HTTP/1.0 200 OK\r\n");
                                    out.print("Content-Type: text/plain\r\n");
                                    out.print("Connection: close\r\n");
                                    out.print("\r\n");
                                    out.print("glassfish/v3-prelude/release/glassfish-v3-prelude-ml.zip\r\n");
                                    out.flush();
                                } finally {
                                    out.close();
                                }
                            } else if (resource.endsWith(".zip")) {
                                OutputStream rawOut = new BufferedOutputStream(socket.getOutputStream());
                                try {
                                    PrintWriter out = new PrintWriter(rawOut, true);
                                    try {
                                        out.print("HTTP/1.0 200 OK\r\n");
                                        out.print("Content-Type: application/octet-stream\r\n");
                                        out.print("Connection: close\r\n");
                                        out.print("\r\n");
                                        out.flush();
                                        ZipOutputStream zip = new ZipOutputStream(rawOut);
                                        try {
                                            writeFile(zip, "gf_fake.jar", 1024 * 1024 * 10);
                                            writeFile(zip, "test/gf_fake_lib.jar", 1024 * 1024 * 5);
                                            zip.flush();
                                            zip.finish();
                                        } finally {
                                            zip.close();
                                        }
                                    } finally {
                                        out.close();
                                    }
                                } finally {
                                    rawOut.close();
                                }
                            } else {
                                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                                try {
                                out.print("HTTP/1.0 404 Not Found\r\n");
                                out.print("Connection: close\r\n");
                                out.print("\r\n");
                                out.flush();
                                } finally {
                                    out.close();
                                }
                            }
                        }
                    }
                } finally {
                    try {
                        in.close();
                    } finally {
                        socket.close();
                    }
                }
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }

    }

    private static void writeFile(ZipOutputStream zip, String name, int size) throws IOException {
        Random random = new Random();
        zip.putNextEntry(new ZipEntry(name));
        for (int i = 0; i < size; i++) {
            zip.write(random.nextInt(Integer.MAX_VALUE - 1) + 1);
        }
        zip.closeEntry();
    }
}
