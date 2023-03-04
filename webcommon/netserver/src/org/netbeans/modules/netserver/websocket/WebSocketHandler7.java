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
import java.nio.channels.SelectionKey;
import java.nio.charset.Charset;
import java.util.logging.Level;


/**
 * @author ads
 *
 */
class WebSocketHandler7 extends AbstractWSHandler7<WebSocketServerImpl> {
    
    
    public WebSocketHandler7( WebSocketServerImpl webSocketServer, SelectionKey key ) {
        super( webSocketServer ); 
        this.myKey=key;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.common.websocket.WebSocketChanelHandler#sendHandshake()
     */
    @Override
    public void sendHandshake( ) throws IOException {
        String acceptKey = createAcceptKey( getKey() );
        if ( acceptKey == null ){
            close( );
            return;
        }
        StringBuilder builder = new StringBuilder(Utils.HTTP_RESPONSE);
        builder.append(Utils.CRLF);
        builder.append(Utils.WS_UPGRADE);
        builder.append(Utils.CRLF);
        builder.append(Utils.CONN_UPGRADE);
        builder.append(Utils.CRLF);
        builder.append("Sec-WebSocket-Origin: ");           // NOI18N
        String origin = getWebSocketPoint().getContext(myKey).getHeaders().get("Sec-WebSocket-Origin");  // NOI18N
        if ( origin == null ){
            origin = getWebSocketPoint().getContext(myKey).getHeaders().get("Origin");  // NOI18N
        }
        if ( origin != null ){
            builder.append( origin);
        }
        builder.append(Utils.CRLF);
        builder.append(Utils.ACCEPT);
        builder.append(": ");
        builder.append(acceptKey);
        builder.append( Utils.CRLF );
        builder.append( Utils.CRLF );
        getWebSocketPoint().send(builder.toString().getBytes(
                Charset.forName(Utils.UTF_8)), myKey );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.netserver.websocket.AbstractWSHandler7#isClient()
     */
    @Override
    protected boolean isClient() {
        return false;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.netserver.websocket.AbstractWSHandler7#getKey()
     */
    @Override
    protected SelectionKey getKey() {
        return myKey;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.netserver.websocket.AbstractWSHandler7#readDelegate(byte[], int)
     */
    @Override
    protected void readDelegate( byte[] bytes, int dataType ) {
        getWebSocketPoint().getWebSocketReadHandler().read(getKey(), bytes, dataType);        
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.netserver.websocket.AbstractWSHandler7#onHasMask(boolean)
     */
    @Override
    protected boolean verifyMask( boolean hasMask ) throws IOException {
        if ( !hasMask ){
            WebSocketServerImpl.LOG.log(Level.WARNING, 
                    "Unexpected client data. Frame is not masked"); // NOI18N
            close();
            return false;
        }
        return true;
    }
    
    private String createAcceptKey(SelectionKey key ){
        String originalKey = getWebSocketPoint().getContext(key).getHeaders().get(Utils.KEY);
        if ( originalKey == null ){
            return null;
        }
        return generateAcceptKey(originalKey);
    }
    
    private SelectionKey myKey;
    
}
