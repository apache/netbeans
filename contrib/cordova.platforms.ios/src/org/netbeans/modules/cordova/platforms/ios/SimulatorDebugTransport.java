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
package org.netbeans.modules.cordova.platforms.ios;

import com.dd.plist.BinaryPropertyListParser;
import com.dd.plist.NSObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONObject;
import org.netbeans.modules.cordova.platforms.api.WebKitDebuggingSupport;

/**
 *
 * @author Jan Becicka
 */
public class SimulatorDebugTransport extends IOSDebugTransport {
    private static final String LOCALHOST_IPV6 = "::1"; // NOI18N
    private static final int port = 27753;
    private static final Logger LOG
            = Logger.getLogger(SimulatorDebugTransport.class.getName());

    private Socket socket;

    @Override
    protected void sendCommand(JSONObject command) throws Exception {
        String cmd = createJSONCommand(command);
        if (LOG.isLoggable(Level.FINEST)) {
            LOG.log(Level.FINEST, "\n\nSending:\n{0}\n{1}",             //NOI18N
                    new Object[]{command.toJSONString(), cmd});
        }
        sendBinaryMessage(plistXmlToBinary(cmd));
    }

    @Override
    protected void sendCommand(String xml) throws Exception {
        LOG.log(Level.FINEST, "\n\nSending:\n{0}", xml);
        sendBinaryMessage(plistXmlToBinary(xml));
    }

    private void sendBinaryMessage(byte[] bytes) throws IOException {
        if (socket == null || socket.isClosed()) {
            return;
        }
        OutputStream os = socket.getOutputStream();
        byte[] lenght = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN).putInt(bytes.length).array();
        os.write(lenght);
        os.write(bytes);
    }

    @Override
    protected NSObject readData() throws Exception {
        InputStream is = socket.getInputStream();
        byte[] sizeBuffer = new byte[4];
        int count = is.read(sizeBuffer);
        while (count < 4) {
            count += is.read(sizeBuffer, count, 4 - count);
        }
        int size = ByteBuffer.wrap(sizeBuffer, 0, 4).getInt();
        byte[] content = new byte[size];
        count = is.read(content);
        while (count < size) {
            final int read = is.read(content, count, size - count);
            if (read == -1) {
                boolean s = keepGoing;
                stop();
                if (s) {
                    WebKitDebuggingSupport.getDefault().stopDebugging(false);
                }
                return null;
            }
            count += read;
        }
        assert count == size;
        NSObject object = BinaryPropertyListParser.parse(content);
        return object;
    }

    @Override
    protected void stop() {
        super.stop();
        try {
            socket.close();
        } catch (Exception e) {
            LOG.log(Level.FINE , e.getMessage(), e);
        }
    }

    @Override
    public String getConnectionName() {
        return "iOS Simulator"; // NOI18N
    }

    @Override
    public String getVersion() {
        return "1.0"; // NOI18N
    }

    @Override
    protected void init() throws Exception {
        if (socket != null && (socket.isConnected() || !socket.isClosed())) {
            socket.close();
        }
        for (long stop = System.nanoTime() + TimeUnit.MINUTES.toNanos(2); stop > System.nanoTime();) {
            try {
                socket = new Socket(LOCALHOST_IPV6, port);
                break;
            } catch (java.net.ConnectException ex) {
                Thread.sleep(5000);
                continue;
            }
        }
    }
    
}
