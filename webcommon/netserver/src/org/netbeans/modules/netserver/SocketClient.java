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
package org.netbeans.modules.netserver;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;


/**
 * @author ads
 *
 */
public class SocketClient extends SocketFramework {

    public SocketClient(SocketAddress address ) throws IOException {
        super();
        writeQueue = new ConcurrentLinkedQueue<ByteBuffer>();
        socketChannel = SocketChannel.open();
        socketChannel.configureBlocking( false );
        socketChannel.connect( address );
        socketChannel.register( getSelector(), SelectionKey.OP_CONNECT );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.netserver.SocketFramework#run()
     */
    @Override
    public void run() {
        super.run();
        chanelClosed(null);
        try {
            socketChannel.close();
        }
        catch (IOException e) {
            LOG.log(Level.WARNING, null, e);
        }
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.netserver.SocketFramework#chanelClosed(java.nio.channels.SelectionKey)
     */
    @Override
    protected void chanelClosed( SelectionKey key ) {
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.netserver.SocketFramework#getAddress()
     */
    @Override
    protected SocketAddress getAddress(){
        return socketChannel.socket().getRemoteSocketAddress();
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.netserver.SocketFramework#process(java.nio.channels.SelectionKey)
     */
    @Override
    protected void process( SelectionKey key ) throws IOException {
        if ( key.isConnectable()){
            finishConnect( key );
        }
        super.process(key);
    }
    
    protected SocketChannel getChannel(){
        return socketChannel;
    }

    protected void finishConnect( SelectionKey key) throws IOException {
        if ( socketChannel.isConnectionPending() ){
            socketChannel.finishConnect();
        }
        socketChannel.register(getSelector(), SelectionKey.OP_READ);
    }
    
    @Override
    protected Queue<ByteBuffer> getWriteQueue( SelectionKey key ){
        return writeQueue;
    }

    private SocketChannel socketChannel;
    private Queue<ByteBuffer> writeQueue;
}
