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
package org.netbeans.modules.netserver.api;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.modules.netserver.websocket.WebSocketServerImpl;
import org.openide.util.Parameters;
import org.openide.util.RequestProcessor;


/**
 * @author ads
 *
 */
public final class WebSocketServer  {
    
    private WebSocketServerImpl server;
    private RequestProcessor RP = new RequestProcessor("WebSocketServer");
    
    public WebSocketServer(SocketAddress address, WebSocketReadHandler handler) throws IOException {
        server = new WebSocketServerImpl(address);
        server.setWebSocketReadHandler(handler);
    }
    
    public void start() {
        RP.post(server);
    }
    
    public void stop() {
        server.stop();
    }
    
    public void sendMessage( @NonNull SelectionKey key , @NonNull String message){
        Parameters.notNull("key", key); //NOI18N
        Parameters.notNull("message", message); //NOI18N
        server.sendMessage(key, message);
    }
}
