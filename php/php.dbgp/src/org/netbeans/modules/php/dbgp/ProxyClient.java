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
package org.netbeans.modules.php.dbgp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.php.project.api.PhpOptions;

/**
 * @author Radek Matous
 */
class ProxyClient {
    private static final Logger LOGGER = Logger.getLogger(ProxyClient.class.getName());
    private String proxyHost;
    private int proxyPort;
    private int idePort;
    private String ideKey;

    /**
     * @return instance or null if proxy isn't enabled
     */
    static ProxyClient getInstance(DebuggerOptions options) {
        return options.getDebugProxy() != null ? new ProxyClient(options) : null;
    }

    private ProxyClient(DebuggerOptions options) {
        assert options != null;
        assert options.getDebugProxy().first() != null;
        assert options.getDebugProxy().second() != null;

        this.proxyHost = options.getDebugProxy().first();
        this.proxyPort = options.getDebugProxy().second();
        this.idePort = PhpOptions.getInstance().getDebuggerPort();
        this.ideKey = PhpOptions.getInstance().getDebuggerSessionId();
    }

    boolean register() {
        boolean retval = false;
        String command = getProxyInitCommand(idePort, ideKey);
        try {
            Socket socket = new Socket(proxyHost, proxyPort);
            OutputStream outputStream = socket.getOutputStream();
            InputStream inputStream = socket.getInputStream();
            try {
                sendCommand(outputStream, command);
                String response = getResponse(inputStream, command);
                retval = response != null;
            } finally {
                outputStream.close();
                inputStream.close();
                socket.close();
            }
        } catch (UnknownHostException ex) {
            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.log(Level.INFO, ex.getLocalizedMessage(), ex);
            }
        } catch (IOException ex) {
            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.log(Level.INFO, ex.getLocalizedMessage(), ex);
            }
        }
        return retval;
    }

    void unregister() {
        String command = getProxyStopCommand(ideKey);
        try {
            Socket socket = new Socket(proxyHost, proxyPort);
            socket.setSoTimeout(5000);
            try (OutputStream outputStream = socket.getOutputStream()) {
                sendCommand(outputStream, command);
            } finally {
                socket.close();
            }
        } catch (UnknownHostException ex) {
            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.log(Level.INFO, ex.getLocalizedMessage(), ex);
            }
        } catch (IOException ex) {
            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.log(Level.INFO, ex.getLocalizedMessage(), ex);
            }
        }
    }

    private static String getProxyInitCommand(int idePort, String ideKey) {
        return String.format("proxyinit -p %d -k %s -m 0", idePort, ideKey); //NOI18N
    }

    private static String getProxyStopCommand(String ideKey) {
        return String.format("proxystop -k %s", ideKey); //NOI18N
    }

    private static void sendCommand(OutputStream outputStream, String command) throws IOException {
        outputStream.write(command.getBytes(Charset.defaultCharset()));
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, String.format("send command: %s: ", command)); //NOI18N
        }
    }

    private static String getResponse(InputStream inputStream, String command) throws IOException {
        String retval = null;
        int available = 1024;
        byte[] responseBytes = new byte[available];
        int len = 0;
        for (int nextByte; (nextByte = inputStream.read()) != -1 && len < available; len++) {
            responseBytes[len] = (byte) nextByte;
        }
        if (len > 0) {
            retval = new String(responseBytes, 0, len, Charset.defaultCharset());
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, String.format("response(%s) is %s: ", command, retval)); //NOI18N
            }
        } else {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, String.format("No response after command(\"%s\"): ", command)); //NOI18N
            }
        }
        return retval;
    }

}
