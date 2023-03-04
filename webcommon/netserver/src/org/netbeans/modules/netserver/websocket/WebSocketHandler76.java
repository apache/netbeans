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
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.logging.Level;


/**
 * @author ads
 *
 */
class WebSocketHandler76 extends WebSocketHandler75 implements WebSocketChanelHandler {

    public WebSocketHandler76( WebSocketServerImpl webSocketServer , SelectionKey key) {
        super(webSocketServer, key );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.common.websocket.WebSocketHandler75#sendHandshake()
     */
    @Override
    public void sendHandshake( ) throws IOException {
        byte[] lastEightBytes = readRequestContent( );
        if ( lastEightBytes == null ){
            throw new IOException("Invalid handshake. Cannot read handshake content");  // NOI18N
        }
        StringBuilder builder = new StringBuilder(Utils.HTTP_RESPONSE);
        builder.append(Utils.CRLF);
        builder.append(Utils.WS_UPGRADE);
        builder.append(Utils.CRLF);
        builder.append(Utils.CONN_UPGRADE);
        builder.append(Utils.CRLF);
        builder.append("Sec-WebSocket-Origin: ");           // NOI18N
        String origin = getWebSocketPoint().getContext(getKey()).getHeaders().get("Origin");  // NOI18N
        if ( origin != null ){
            builder.append( origin);
        }
        builder.append(Utils.CRLF);
        builder.append("Sec-WebSocket-Location: ws://");    // NOI18N
        String host = getWebSocketPoint().getContext(getKey()).getHeaders().get(Utils.HOST);                
        if ( host != null) {
            builder.append( host );
        }
        else {
            builder.append("127.0.0.1:");                   // NOI18N
            builder.append( ((InetSocketAddress)getWebSocketPoint().getAddress()).getPort());
        }
        String request = getWebSocketPoint().getContext(getKey()).getRequestString();
        int index = request.indexOf(' ');
        String url = null;
        if ( index != -1 ){
            request = request.substring(index).trim();
            index = request.indexOf(' ');
            if ( index !=-1 ){
                url = request.substring( 0, index ).trim();
            }
        }
        else {
            url ="/";                                       // NOI18N
        }
        builder.append( url );
        builder.append( Utils.CRLF );
        builder.append( Utils.CRLF );
        byte[] headers = builder.toString().getBytes( 
                Charset.forName(Utils.UTF_8));
        byte[] responseContent = createResponseContent(getKey(), lastEightBytes);
        if ( responseContent == null ){
            close( );
            return;
        }
        byte[] response = new byte[ headers.length + responseContent.length ];
        System.arraycopy(headers, 0, response, 0, headers.length);
        System.arraycopy(responseContent, 0, response, headers.length, 
                responseContent.length);
        getWebSocketPoint().send(response , getKey() );
    }

    private byte[] createResponseContent(SelectionKey key,  byte[] lastEightBytes ) {
        Map<String, String> headers = getWebSocketPoint().getContext(key).getHeaders();
        String key1 = headers.get(Utils.KEY1);
        String key2 = headers.get(Utils.KEY2);
        return Utils.produceChallenge76(key1, key2, lastEightBytes);
    }

    private byte[] readRequestContent(  ) throws IOException {
        byte[] content = getWebSocketPoint().getContext( getKey()).getContent();
        boolean red = false;
        for( byte b : content ){
            if ( b!= 0){
                red = true;
                break;
            }
        }
        if ( red ){
            return content;
        }
        else {
            return readRequestContent( 8 );
        }
    }

}
