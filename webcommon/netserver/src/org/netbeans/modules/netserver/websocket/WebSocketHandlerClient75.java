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
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.charset.Charset;



/**
 * @author ads
 *
 */
class WebSocketHandlerClient75 extends AbstractWSHandler75<WebSocketClientImpl> {

    WebSocketHandlerClient75( WebSocketClientImpl webSocketClient ) {
        super(webSocketClient);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.netserver.websocket.AbstractWSHandler75#read(java.nio.ByteBuffer)
     */
    @Override
    public void read( ByteBuffer byteBuffer ) throws IOException {
        if ( handshakeRed ){
            super.read(byteBuffer);
        }
        else {
            readHandshakeResponse( byteBuffer );
            handshakeRed = true;
            getWebSocketPoint().getWebSocketReadHandler().accepted(getKey());
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.netserver.websocket.WebSocketChanelHandler#sendHandshake()
     */
    @Override
    public void sendHandshake() {
        StringBuilder builder = new StringBuilder(Utils.GET);
        builder.append(' ');
        builder.append(getWebSocketPoint().getUri().getPath());
        builder.append(' ');
        builder.append( Utils.HTTP_11);
        builder.append(Utils.CRLF);
        
        builder.append(Utils.WS_UPGRADE);
        builder.append(Utils.CRLF);
        
        builder.append(Utils.HOST);
        builder.append(": ");                               // NOI18N
        builder.append(getWebSocketPoint().getUri().getHost());
        builder.append(Utils.CRLF);
        
        builder.append("Origin: ");
        builder.append( Utils.getOrigin(getWebSocketPoint().getUri()));
        builder.append(Utils.CRLF);
        
        builder.append(Utils.WS_PROTOCOL);
        builder.append(": chat");                             // NOI18N
        
        builder.append( Utils.CRLF );
        builder.append( Utils.CRLF );
        
        getWebSocketPoint().send(builder.toString().getBytes( 
                Charset.forName(Utils.UTF_8)), getKey() );
    }
    
    protected void readHandshakeResponse( ByteBuffer buffer) throws IOException {
        Utils.readHttpRequest(getWebSocketPoint().getChannel(), buffer);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.netserver.websocket.AbstractWSHandler75#getKey()
     */
    @Override
    protected SelectionKey getKey() {
        return getWebSocketPoint().getKey();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.netserver.websocket.AbstractWSHandler75#readDelegate(byte[])
     */
    @Override
    protected void readDelegate( byte[] bytes ) {
        getWebSocketPoint().getWebSocketReadHandler().read(getKey(), bytes, null); 
    }
    
    private boolean handshakeRed;
}
