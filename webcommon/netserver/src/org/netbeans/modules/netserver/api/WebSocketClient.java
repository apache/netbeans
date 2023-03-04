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
import java.net.URI;
import org.netbeans.modules.netserver.websocket.WebSocketClientImpl;
import org.openide.util.RequestProcessor;

/**
 *
 */
public final class WebSocketClient {

    private WebSocketClientImpl client;
    private RequestProcessor RP = new RequestProcessor("WebSocketClient");
    
    public WebSocketClient(URI uri , ProtocolDraft draft, WebSocketReadHandler handler) throws IOException {
        client = new WebSocketClientImpl(uri, draft);
        client.setWebSocketReadHandler(handler);
    }
    
    public WebSocketClient(URI uri, WebSocketReadHandler handler) throws IOException {
        this(uri , ProtocolDraft.getRFC(), handler);
    }

    public void start() {
        RP.post(client);
    }
    
    public void stop() {
        client.stop();
    }
    
    public void sendMessage(String message) {
        client.sendMessage(message);
    }

    public URI getURI(){
        return client.getUri();
    }
    

}
