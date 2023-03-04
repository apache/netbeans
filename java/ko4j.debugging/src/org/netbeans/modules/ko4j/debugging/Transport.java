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
package org.netbeans.modules.ko4j.debugging;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.netbeans.modules.web.browser.api.PageInspector;
import org.netbeans.modules.web.webkit.debugging.api.TransportStateException;
import org.netbeans.modules.web.webkit.debugging.spi.Command;
import org.netbeans.modules.web.webkit.debugging.spi.Response;
import org.netbeans.modules.web.webkit.debugging.spi.ResponseCallback;
import org.netbeans.modules.web.webkit.debugging.spi.TransportImplementation;
import org.openide.util.Exceptions;

/**
 *
 * @author Jan Stola
 */
public class Transport implements TransportImplementation {
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private ResponseCallback callback;
    private MessageDispatcherImpl messageDispatcher;
    
    public Transport(Socket socket, MessageDispatcherImpl messageDispatcher) {
        try {
            this.messageDispatcher = messageDispatcher;
            input = new ObjectInputStream(socket.getInputStream());
            output = new ObjectOutputStream(socket.getOutputStream());
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        while (true) {
                            int length = input.readInt();
                            byte[] bytes = new byte[length];
                            input.readFully(bytes);
                            String message = new String(bytes, StandardCharsets.UTF_8);
                            JSONObject json = (JSONObject)JSONValue.parseWithException(message);
                            callback.handleResponse(new Response(json));
                        }
                    } catch (IOException ioex) {
                        ioex.printStackTrace();
                    } catch (ParseException pex) {
                        pex.printStackTrace();
                    }
                    Transport.this.messageDispatcher.dispatchMessage(PageInspector.MESSAGE_DISPATCHER_FEATURE_ID, null);
                }                
            });
            t.start();
        } catch (IOException ioex) {
            Exceptions.printStackTrace(ioex);
        }
    }

    @Override
    public boolean attach() {
        return true;
    }

    @Override
    public boolean detach() {
        return true;
    }

    @Override
    public synchronized void sendCommand(Command command) throws TransportStateException {
        try {
            String message = command.toString();
            byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
            output.writeInt(bytes.length);
            output.write(bytes);
            output.flush();
        } catch (IOException ioex) {
            ioex.printStackTrace();
        }
    }

    @Override
    public void registerResponseCallback(ResponseCallback callback) {
        this.callback = callback;
    }

    @Override
    public String getConnectionName() {
        return "";
    }

    @Override
    public URL getConnectionURL() {
        return null;
    }

    @Override
    public String getVersion() {
        return VERSION_1;
    }
    
}
