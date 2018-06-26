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
