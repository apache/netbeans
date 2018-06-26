/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
