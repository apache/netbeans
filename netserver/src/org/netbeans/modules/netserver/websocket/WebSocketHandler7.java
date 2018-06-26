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
