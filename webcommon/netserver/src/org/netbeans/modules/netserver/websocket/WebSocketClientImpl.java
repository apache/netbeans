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

import org.netbeans.modules.netserver.api.ProtocolDraft;
import org.netbeans.modules.netserver.api.WebSocketReadHandler;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.logging.Logger;

import org.netbeans.modules.netserver.ReadHandler;
import org.netbeans.modules.netserver.SocketClient;
import org.netbeans.modules.netserver.SocketFramework;
import org.netbeans.modules.netserver.api.ProtocolDraft.Draft;


/**
 * @author ads
 *
 */
public class WebSocketClientImpl extends SocketClient {
    
    static final Logger LOG = SocketFramework.LOG; 
    
    public WebSocketClientImpl( URI uri , ProtocolDraft draft) throws IOException {
        this(new InetSocketAddress( uri.getHost() , uri.getPort()), draft);
        this.uri = uri;
    }
    
    public WebSocketClientImpl( URI uri ) throws IOException {
        this( uri , ProtocolDraft.getRFC());
    }

    private WebSocketClientImpl( SocketAddress address , ProtocolDraft draft) throws IOException {
        super(address);
        setReadHandler( new WebSocketClientHandler());
        
        if ( draft.isRFC() || draft.getDraft() ==null ){
            setHandler( new WebSocketHandlerClient7(this, draft.getVersion() ));
        }
        else if ( draft.getDraft() == Draft.Draft75 ){
            setHandler( new WebSocketHandlerClient75(this));
        }
        else if ( draft.getDraft() == Draft.Draft76 ){
            setHandler( new WebSocketHandlerClient76(this));
        }
    }
    
    public void sendMessage( String message){
        SelectionKey key = getKey();
        if ( key == null ){
	    stop();
            return;
        }
        byte[] bytes = getHandler().createTextFrame( message);
        send(bytes , key); 
    }
    
    public void setWebSocketReadHandler( WebSocketReadHandler handler ){
        this.handler = handler ;
    }
    
    public WebSocketReadHandler getWebSocketReadHandler(){
        return handler;
    }
    
    public URI getUri(){
        return uri;
    }
    
    @Override
    public void close( SelectionKey key ) throws IOException {
        if ( key == null){
            return;
        }
        super.close(key);
        stop();
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.netserver.SocketClient#chanelClosed(java.nio.channels.SelectionKey)
     */
    @Override
    protected void chanelClosed( SelectionKey key ) {
        getWebSocketReadHandler().closed(key);
    }
    
    protected SelectionKey getKey(){
        return getChannel().keyFor( getSelector());
    }
    
    @Override
    protected SocketChannel getChannel(){
        return super.getChannel();
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.netserver.SocketClient#finishConnect(java.nio.channels.SelectionKey)
     */
    @Override
    protected void finishConnect(SelectionKey key) throws IOException {
        super.finishConnect(key);
        
        getHandler().sendHandshake();
    }
    
    protected class WebSocketClientHandler implements ReadHandler {
        
        public WebSocketClientHandler() {
            byteBuffer = ByteBuffer.allocate(Utils.BYTES);
        }

        /* (non-Javadoc)
         * @see org.netbeans.modules.netserver.ReadHandler#read(java.nio.channels.SelectionKey)
         */
        @Override
        public void read( SelectionKey key ) throws IOException {
            getHandler().read(byteBuffer);
        }
        
        private ByteBuffer byteBuffer;
    }

    void setHandler( WebSocketChanelHandler handler ){
        innerHandler = handler;
    }
    
    WebSocketChanelHandler getHandler(){
        return innerHandler;
    }


    private volatile WebSocketReadHandler handler;
    private URI uri;
    private WebSocketChanelHandler innerHandler;
}
