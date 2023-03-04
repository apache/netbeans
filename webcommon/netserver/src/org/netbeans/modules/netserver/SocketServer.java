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
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;


/**
 * @author ads
 *
 */
public class SocketServer extends SocketFramework {
    
    public SocketServer(SocketAddress address ) throws IOException {
        super();
        serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        serverChannel.socket().bind(address);
        
        serverChannel.register(getSelector(), SelectionKey.OP_ACCEPT);
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        try {
            doRun();
        }
        catch (IOException e) {
            LOG.log(Level.WARNING, null, e);
        }
        finally {
            try {
                serverChannel.close();
            }
            catch (IOException e) {
                LOG.log(Level.WARNING, null, e);
            }
        }
    }
    
    @Override
    protected SocketAddress getAddress(){
        return serverChannel.socket().getLocalSocketAddress();
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.netserver.SocketFramework#process(java.nio.channels.SelectionKey)
     */
    @Override
    protected void process( SelectionKey key ) throws IOException {
        if ( key.isAcceptable() ){
            acceptConnection(key);
        }
        else {
            super.process(key);
        }
    }
    
    @Override
    protected Queue<ByteBuffer> getWriteQueue( SelectionKey key ){
        Object attachment = key.attachment();
        return (Queue<ByteBuffer>) attachment;
    }
    
    protected void initWriteQueue( SelectionKey key ){
        if ( key.attachment() == null ){
            key.attach( new ConcurrentLinkedQueue<ByteBuffer>());
        } 
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.netserver.SocketFramework#chanelClosed(java.nio.channels.SelectionKey)
     */
    @Override
    protected void chanelClosed( SelectionKey key ) {
    } 
    
    private void acceptConnection( SelectionKey key ) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

        SocketChannel socketChannel = serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
        socketChannel.register(getSelector(), SelectionKey.OP_READ);
        initWriteQueue( socketChannel.keyFor(getSelector()));
    }

    private ServerSocketChannel serverChannel;

}
