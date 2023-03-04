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
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * @author ads
 *
 */
public abstract class SocketFramework implements Runnable {
    
    protected static final Logger LOG = Logger.getLogger( 
            SocketServer.class.getCanonicalName());
    
    public SocketFramework() throws IOException {
        keys = new ConcurrentLinkedQueue<SelectionKey>();
        selector = Selector.open();
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
    }
    
    public void close( SelectionKey key ) throws IOException {
        chanelClosed( key );
        key.channel().close();
        key.cancel();
    }
    
    public void stop(){
        stop = true;
        getSelector().wakeup();
    }
    
    public void send( byte[] data , SelectionKey key ){
        getWriteQueue(key).add(ByteBuffer.wrap(data));
        keys.add(key);
        getSelector().wakeup();
    }
    
    public boolean isStopped(){
        return stop;
    }
    
    protected void doRun() throws IOException {
        while (!stop) {
            while (true) {
                SelectionKey key = keys.poll();
                if (key == null) {
                    break;
                }
                else {
                    if (key.isValid()) {
                        int currentOps = key.interestOps();
                        key.interestOps(currentOps|SelectionKey.OP_WRITE);
                    }
                }
            }
            getSelector().select();
            if ( isStopped() ){
                return;
            }

            for (Iterator<SelectionKey> iterator = getSelector().selectedKeys()
                    .iterator(); iterator.hasNext();)
            {
                SelectionKey key = iterator.next();
                iterator.remove();

                if (!key.isValid()) {
                    continue;
                }
                
                try {
                    process(key);
                }
                catch( ClosedChannelException e ){
                    close(key);
                }
                catch( IOException e ){
                    LOG.log(Level.INFO, null, e);
                    close(key);
                }
            }
        }
        getSelector().close();
    }
    
    protected void process( SelectionKey key ) throws IOException {
        if (key.isReadable()) {
            readData(key);
        }
        if (key.isValid() && key.isWritable()) {
            writeData(key);
        }        
    }
    
    protected abstract void chanelClosed( SelectionKey key );
    
    protected abstract SocketAddress getAddress();
    
    protected abstract Queue<ByteBuffer> getWriteQueue( SelectionKey key );
    
    protected void setReadHandler( ReadHandler handler ){
        this.handler = handler;
    }
    
    protected ReadHandler getReadHandler(){
        return handler;
    }
    
    protected Selector getSelector(){
        return selector;
    }
    
    protected void readData( SelectionKey key ) throws IOException {
        handler.read(key);
    }
    
    protected void writeData( SelectionKey key ) throws IOException  {
        Queue<ByteBuffer> queue = getWriteQueue(key);
        int ops = SelectionKey.OP_READ;
        while( queue!= null ){
            ByteBuffer buffer = queue.peek();
            if ( buffer == null ){
                break;
            }
            else {
                int length = buffer.remaining();
                int written = ((SocketChannel)key.channel()).write(buffer);
                if (written < length) {
                    // Not all bytes written. Socket's output buffer is full probably.
                    // Keep the rest of this buffer in the write queue and wait until
                    // the channel is writable again.
                    ops |= SelectionKey.OP_WRITE;
                    break;
                } else {
                    // The whole content of the buffer written => remove it from the queue
                    queue.poll();
                }
            }
        }
        key.interestOps(ops);
    }
    
    private Selector selector;
    private Queue<SelectionKey> keys;
    private ReadHandler handler;
    private volatile boolean stop;

}
