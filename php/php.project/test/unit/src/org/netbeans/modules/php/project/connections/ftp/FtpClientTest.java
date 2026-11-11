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
package org.netbeans.modules.php.project.connections.ftp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.php.project.connections.ConfigManager;

public class FtpClientTest extends NbTestCase {

    public FtpClientTest(String name) {
        super(name);
    }

    public void testNegotiatesUtf8ForRemoteNames() throws Exception {
        try (FakeFtpServer server = new FakeFtpServer()) {
            ConfigManager manager = new ConfigManager(new ConfigManager.ConfigProvider() {
                @Override
                public String[] getConfigProperties() {
                    return FtpConnectionProvider.get().getPropertyNames().toArray(new String[0]);
                }

                @Override
                public Map<String, Map<String, String>> getConfigs() {
                    return ConfigManager.createEmptyConfigs();
                }
            });
            ConfigManager.Configuration configuration = manager.defaultConfiguration();
            FtpConnectionProvider.get().createRemoteConfiguration(configuration);
            configuration.putValue(FtpConnectionProvider.HOST, "127.0.0.1");
            configuration.putValue(FtpConnectionProvider.PORT, Integer.toString(server.getPort()));
            configuration.putValue(FtpConnectionProvider.ANONYMOUS_LOGIN, "true");
            configuration.putValue(FtpConnectionProvider.KEEP_ALIVE_INTERVAL, "0");
            configuration.putValue(FtpConnectionProvider.TIMEOUT, "10");

            FtpClient client = new FtpClient(new FtpConfiguration(configuration), null);
            try {
                client.connect();
                assertTrue(client.makeDirectory("производи"));
            } finally {
                client.disconnect(true);
            }
            server.awaitTermination();

            assertTrue("Client should query FEAT before using the control connection", server.featureRequested);
            assertEquals("MKD производи", new String(server.makeDirectoryCommand, StandardCharsets.UTF_8));
        }
    }

    private static final class FakeFtpServer implements AutoCloseable {

        private final ServerSocket serverSocket;
        private final Thread thread;
        private volatile IOException failure;
        private volatile boolean featureRequested;
        private volatile byte[] makeDirectoryCommand;

        FakeFtpServer() throws IOException {
            serverSocket = new ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"));
            thread = new Thread(this::serve, "NetBeans FTP test server");
            thread.setDaemon(true);
            thread.start();
        }

        int getPort() {
            return serverSocket.getLocalPort();
        }

        void awaitTermination() throws Exception {
            thread.join(5000);
            assertFalse("FTP test server did not terminate", thread.isAlive());
            if (failure != null) {
                throw failure;
            }
        }

        private void serve() {
            try (Socket socket = serverSocket.accept()) {
                socket.setSoTimeout(5000);
                InputStream input = socket.getInputStream();
                OutputStream output = socket.getOutputStream();
                reply(output, "220 test server ready");
                byte[] command;
                while ((command = readLine(input)) != null) {
                    String asciiCommand = new String(command, StandardCharsets.US_ASCII);
                    String verb = asciiCommand.split(" ", 2)[0].toUpperCase(java.util.Locale.ROOT);
                    switch (verb) {
                        case "FEAT":
                            featureRequested = true;
                            output.write("211-Features:\r\n UTF8\r\n211 End\r\n".getBytes(StandardCharsets.US_ASCII));
                            output.flush();
                            break;
                        case "USER":
                            reply(output, "331 Password required");
                            break;
                        case "PASS":
                            reply(output, "230 Logged in");
                            break;
                        case "TYPE":
                            reply(output, "200 Type set");
                            break;
                        case "SYST":
                            reply(output, "215 UNIX Type: L8");
                            break;
                        case "MKD":
                            makeDirectoryCommand = command;
                            reply(output, "257 Directory created");
                            break;
                        case "QUIT":
                            reply(output, "221 Goodbye");
                            return;
                        default:
                            reply(output, "200 OK");
                            break;
                    }
                }
            } catch (IOException ex) {
                failure = ex;
            }
        }

        private static byte[] readLine(InputStream input) throws IOException {
            ByteArrayOutputStream line = new ByteArrayOutputStream();
            int value;
            while ((value = input.read()) != -1) {
                if (value == '\n') {
                    byte[] bytes = line.toByteArray();
                    return bytes.length > 0 && bytes[bytes.length - 1] == '\r'
                            ? Arrays.copyOf(bytes, bytes.length - 1)
                            : bytes;
                }
                line.write(value);
            }
            return line.size() == 0 ? null : line.toByteArray();
        }

        private static void reply(OutputStream output, String reply) throws IOException {
            output.write((reply + "\r\n").getBytes(StandardCharsets.US_ASCII));
            output.flush();
        }

        @Override
        public void close() throws IOException {
            serverSocket.close();
        }
    }
}
