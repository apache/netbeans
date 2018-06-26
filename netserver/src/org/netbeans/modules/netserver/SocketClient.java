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
