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

        private static final Pattern HTTP_PATTERN = Pattern.compile("(GET|HEAD) (/[^\\s]*) HTTP/1\\.[01]");
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
                            String httpVerb = matcher.group(1);
                            String resource = matcher.group(2);
                            if (resource.endsWith("moved.zip")) {
                                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                                try {
                                    out.print("HTTP/1.0 301 Moved Permanently\r\n");
                                    out.print("Location: http://localhost:4444/glassfish/v3-prelude/release/glassfish-v3-prelude-ml.zip\r\n");
                                    out.print("\r\n");
                                    out.flush();
                                } finally {
                                    out.close();
                                }
                            } else if (resource.endsWith("prelude-ml.zip")) {
                                OutputStream rawOut = new BufferedOutputStream(socket.getOutputStream());
                                try {
                                    PrintWriter out = new PrintWriter(rawOut, true);
                                    try {
                                        out.print("HTTP/1.0 200 OK\r\n");
                                        out.print("Content-Type: application/octet-stream\r\n");
                                        out.print("Connection: close\r\n");
                                        out.print("\r\n");
                                        out.flush();
                                        if(httpVerb.equals("GET")) {
                                            ZipOutputStream zip = new ZipOutputStream(rawOut);
                                            try {
                                                writeFile(zip, "gf_fake.jar", 1024 * 1024 * 10);
                                                writeFile(zip, "test/gf_fake_lib.jar", 1024 * 1024 * 5);
                                                zip.flush();
                                                zip.finish();
                                            } finally {
                                                zip.close();
                                            }
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
