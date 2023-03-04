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
package org.netbeans.modules.netserver.websocket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.charset.Charset;


/**
 * @author ads
 *
 */
class WebSocketHandler75 extends AbstractWSHandler75<WebSocketServerImpl> {
    
    public WebSocketHandler75( WebSocketServerImpl webSocketServer, SelectionKey key ) {
        super(webSocketServer);
        this.myKey = key;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.common.websocket.WebSocketChanelHandler#sendHandshake()
     */
    @Override
    public void sendHandshake( ) throws IOException {
        StringBuilder builder = new StringBuilder(Utils.HTTP_RESPONSE);
        builder.append(Utils.CRLF);
        builder.append(Utils.WS_UPGRADE);
        builder.append(Utils.CRLF);
        builder.append(Utils.CONN_UPGRADE);
        builder.append(Utils.CRLF);
        builder.append("WebSocket-Origin: ");                        // NOI18N
        String origin = getWebSocketPoint().getContext(myKey).getHeaders().get("Origin");      // NOI18N
        if ( origin != null ){
            builder.append( origin);
        }
        builder.append(Utils.CRLF);
        builder.append("WebSocket-Location: ws://");                 // NOI18N
        String host = getWebSocketPoint().getContext(myKey).getHeaders().get(Utils.HOST);                
        if ( host != null) {
            builder.append( host );
        }
        else {
            builder.append("127.0.0.1:");                            // NOI18N
            builder.append( ((InetSocketAddress)getWebSocketPoint().getAddress()).getPort());
        }
        String request = getWebSocketPoint().getContext(myKey).getRequestString();
        String url = "/"; // NOI18N
        if (request != null) {
            int index = request.indexOf(' ');
            if ( index != -1 ){
                request = request.substring(index).trim();
                index = request.indexOf(' ');
                if ( index !=-1 ){
                    url = request.substring( 0, index ).trim();
                }
            }
        }
        builder.append( url );
        builder.append( Utils.CRLF );
        String protocol = getWebSocketPoint().getContext(myKey).getHeaders().get(Utils.WS_PROTOCOL);
        if ( protocol != null ){
            builder.append( Utils.WS_PROTOCOL );
            builder.append(": ");               // NOI18N
            builder.append( protocol );
        }
        builder.append( Utils.CRLF );
        builder.append( Utils.CRLF );
        getWebSocketPoint().send(builder.toString().getBytes( 
                Charset.forName(Utils.UTF_8)), myKey );
    }
    
    @Override
    protected SelectionKey getKey(){
        return myKey;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.netserver.websocket.AbstractWSHandler75#readDelegate(byte[])
     */
    @Override
    protected void readDelegate( byte[] bytes ) {
        getWebSocketPoint().getWebSocketReadHandler().read(myKey, bytes, null);        
    }
    
    private final SelectionKey myKey;

}
