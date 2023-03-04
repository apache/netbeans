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
import java.security.MessageDigest;
import java.util.Base64;
import java.util.List;




/**
 * @author ads
 *
 */
class WebSocketHandlerClient7 extends AbstractWSHandler7<WebSocketClientImpl> {

    WebSocketHandlerClient7( WebSocketClientImpl webSocketClient, int version ){
        super(webSocketClient);
        this.version = version;
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
        
        builder.append(Utils.HOST);
        builder.append(": ");                               // NOI18N
        builder.append(getWebSocketPoint().getUri().getHost());
        builder.append(Utils.CRLF);
        
        builder.append(Utils.WS_UPGRADE_1);
        builder.append(Utils.CRLF);
        
        builder.append(Utils.CONN_UPGRADE);
        builder.append(Utils.CRLF);
        
        if ( version >= 7 && version<= 10){
            builder.append("Sec-WebSocket-Origin: ");
        }
        else {
            builder.append("Origin: ");
        }
        builder.append( Utils.getOrigin(getWebSocketPoint().getUri()));
        builder.append( Utils.CRLF );
        
        builder.append("Sec-WebSocket-Protocol: chat");     // NOI18N
        builder.append( Utils.CRLF );
        
        builder.append("Sec-WebSocket-Version: ");          // NOI18N
        if ( version == 7 ){
            builder.append( version );
        }
        else if ( version > 7 && version <13){
            builder.append( 8 );
        }
        else {
            builder.append( 13 );
        }
        builder.append( Utils.CRLF );
        
        builder.append( Utils.KEY);
        builder.append(": ");
        builder.append( getSecKey());
        
        builder.append( Utils.CRLF );
        builder.append( Utils.CRLF );
        
        getWebSocketPoint().send(builder.toString().getBytes( 
                Charset.forName(Utils.UTF_8)), getWebSocketPoint().getKey() );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.netserver.websocket.WebSocketChanelHandler#read(java.nio.ByteBuffer)
     */
    @Override
    public void read( ByteBuffer byteBuffer ) throws IOException {
        if ( handshakeRed ){
            super.read(byteBuffer);
        }
        else {
            readHandshake( byteBuffer );
            handshakeRed = true;
            getWebSocketPoint().getWebSocketReadHandler().accepted(getKey());
        }
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.netserver.websocket.AbstractWSHandler7#isClient()
     */
    @Override
    protected boolean isClient() {
        return true;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.netserver.websocket.AbstractWSHandler7#readDelegate(byte[], int)
     */
    @Override
    protected void readDelegate( byte[] bytes, int dataType ) {
        getWebSocketPoint().getWebSocketReadHandler().read(getKey(), bytes, dataType);
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.netserver.websocket.AbstractWSHandler7#verifyMask(boolean)
     */
    @Override
    protected boolean verifyMask( boolean hasMask ) throws IOException {
        return true;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.netserver.websocket.AbstractWSHandler#getKey()
     */
    @Override
    protected SelectionKey getKey() {
        return getWebSocketPoint().getKey();
    }
    
    private void readHandshake( ByteBuffer buffer ) throws IOException {
        List<String> headers = Utils.readHttpRequest(getWebSocketPoint().getChannel(), 
                buffer);
        String acceptKey =null;
        String accept = Utils.ACCEPT+':';
        for (String header : headers) {
            if ( header.startsWith(accept))
            {
                acceptKey = header.substring(accept.length()).trim();
            }
        }
        if ( acceptKey == null ){
            throw new IOException("Wrong accept key on handshake received");    // NOI18N
        }
        else {
            String requiredKey = generateAcceptKey(getSecKey());
            if ( !acceptKey.equals(requiredKey)){
                throw new IOException("Wrong accept key on handshake received: "+    
                            requiredKey+" while expected is :"+requiredKey);    // NOI18N
            }
        }
    }
    
    private String getSecKey() {
        if ( myGeneratedKey == null ){
            byte[] bytes = new byte[ 16 ];
            getRandom().nextBytes(bytes);
            myGeneratedKey =  Base64.getEncoder().encodeToString(bytes);
        }
        return myGeneratedKey;
    }
    
    private boolean handshakeRed;
    private int version;
    private String myGeneratedKey;

}
