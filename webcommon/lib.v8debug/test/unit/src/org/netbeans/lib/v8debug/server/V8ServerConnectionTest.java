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

package org.netbeans.lib.v8debug.server;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.netbeans.lib.v8debug.V8Arguments;
import org.netbeans.lib.v8debug.V8Command;
import org.netbeans.lib.v8debug.V8Event;
import org.netbeans.lib.v8debug.V8Request;
import org.netbeans.lib.v8debug.V8Response;
import org.netbeans.lib.v8debug.V8StepAction;
import org.netbeans.lib.v8debug.commands.Continue;
import org.netbeans.lib.v8debug.connection.ClientConnection;
import org.netbeans.lib.v8debug.connection.HeaderProperties;
import org.netbeans.lib.v8debug.connection.ServerConnection;

/**
 *
 * @author Martin Entlicher
 */
public class V8ServerConnectionTest {
    
    @Test
    public void testConnection() throws IOException, InterruptedException {
        final ServerConnection sn = new ServerConnection();
        int port = sn.getPort();
        final Map<String, String> properties = new LinkedHashMap<>();
        properties.put(HeaderProperties.TYPE, "test connect");
        properties.put(HeaderProperties.V8_VERSION, "4.4.4.4");
        properties.put(HeaderProperties.PROTOCOL_VERSION, "1 test");
        properties.put(HeaderProperties.EMBEDDING_HOST, "V8 server test");
        ClientConnection cn = new ClientConnection(null, port);
        final TestConnectionListener listener = new TestConnectionListener(properties, sn, cn);
        
        Thread st = new Thread() {
            @Override public void run() {
                try {
                    sn.runConnectionLoop(properties, listener);
                } catch (IOException ex) {
                    Logger.getLogger(V8ServerConnectionTest.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        st.start();
        try { Thread.sleep(500); } catch (InterruptedException iex) {} // Wait for the server to start up
        try {
            cn.runEventLoop(listener);
        } catch (IOException ioex) {
            ioex.printStackTrace();
        }
        st.join();
        Assert.assertTrue(listener.getError(), listener.success());
        sn.closeServer();
    }
    
    private class TestConnectionListener implements ServerConnection.Listener, ClientConnection.Listener {
        
        private final Map<String, String> properties;
        private final ServerConnection sn;
        private final ClientConnection cn;

        private boolean headerSent = false;
        private boolean haveResponse = false;
        private boolean success = true;
        private String error = "";

        private TestConnectionListener(Map<String, String> properties, ServerConnection sn, ClientConnection cn) {
            this.properties = properties;
            this.sn = sn;
            this.cn = cn;
        }

        @Override
        public ServerConnection.ResponseProvider request(V8Request request) {
            if (!V8Command.Continue.equals(request.getCommand())) {
                success = false;
                error += "Server received command '"+request.getCommand()+"' instead of "+V8Command.Continue;
            }
            V8Arguments arguments = request.getArguments();
            if (!(arguments instanceof Continue.Arguments)) {
                success = false;
                error += "Server received arguments "+arguments+" not "+Continue.Arguments.class;
            } else {
                Continue.Arguments ca = (Continue.Arguments) arguments;
                V8StepAction stepAction = ca.getStepAction();
                if (!V8StepAction.in.equals(stepAction)) {
                    success = false;
                    error += "Wrong step action: "+stepAction;
                }
                if (2l != ca.getStepCount().getValue()) {
                    success = false;
                    error += "Wrong step count: "+ca.getStepCount().getValue();
                }
            }
            return ServerConnection.ResponseProvider.create(request.createSuccessResponse(1l, null, null, true));
        }

        @Override
        public void header(Map<String, String> properties) {
            headerSent = true;
            if (!this.properties.equals(properties)) {
                success = false;
                error = "Properties do not match!\nExpenting "+this.properties+", received "+properties;
            }
            try {
                cn.send(Continue.createRequest(10l, V8StepAction.in, 2));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public void response(V8Response response) {
            if (!V8Command.Continue.equals(response.getCommand())) {
                success = false;
                error += "Client received response command '"+response.getCommand()+"' instead of "+V8Command.Continue;
            }
            if (!response.isRunning()) {
                success = false;
                error += "Client should receive running.";
            }
            if (!response.isSuccess()) {
                success = false;
                error += "Client should receive success.";
            }
            haveResponse = true;
            try {
                cn.close();
                sn.closeCurrentConnection();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public void event(V8Event event) {
            
        }
        
        public boolean success() {
            return headerSent && haveResponse && success;
        }
        
        public String getError() {
            return error;
        }
        
    }
}
