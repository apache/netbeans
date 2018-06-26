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
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.charset.Charset;


/**
 * @author ads
 *
 */
class WebSocketHandler75 extends AbstractWSHandler75<WebSocketServerImpl> {
    
    public WebSocketHandler75( WebSocketServerImpl webSocketServer, SelectionKey key ) {
        super(webSocketServer);
        this.myKey = key;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.common.websocket.WebSocketChanelHandler#sendHandshake()
     */
    @Override
    public void sendHandshake( ) throws IOException {
        StringBuilder builder = new StringBuilder(Utils.HTTP_RESPONSE);
        builder.append(Utils.CRLF);
        builder.append(Utils.WS_UPGRADE);
        builder.append(Utils.CRLF);
        builder.append(Utils.CONN_UPGRADE);
        builder.append(Utils.CRLF);
        builder.append("WebSocket-Origin: ");                        // NOI18N
        String origin = getWebSocketPoint().getContext(myKey).getHeaders().get("Origin");      // NOI18N
        if ( origin != null ){
            builder.append( origin);
        }
        builder.append(Utils.CRLF);
        builder.append("WebSocket-Location: ws://");                 // NOI18N
        String host = getWebSocketPoint().getContext(myKey).getHeaders().get(Utils.HOST);                
        if ( host != null) {
            builder.append( host );
        }
        else {
            builder.append("127.0.0.1:");                            // NOI18N
            builder.append( ((InetSocketAddress)getWebSocketPoint().getAddress()).getPort());
        }
        String request = getWebSocketPoint().getContext(myKey).getRequestString();
        String url = "/"; // NOI18N
        if (request != null) {
            int index = request.indexOf(' ');
            if ( index != -1 ){
                request = request.substring(index).trim();
                index = request.indexOf(' ');
                if ( index !=-1 ){
                    url = request.substring( 0, index ).trim();
                }
            }
        }
        builder.append( url );
        builder.append( Utils.CRLF );
        String protocol = getWebSocketPoint().getContext(myKey).getHeaders().get(Utils.WS_PROTOCOL);
        if ( protocol != null ){
            builder.append( Utils.WS_PROTOCOL );
            builder.append(": ");               // NOI18N
            builder.append( protocol );
        }
        builder.append( Utils.CRLF );
        builder.append( Utils.CRLF );
        getWebSocketPoint().send(builder.toString().getBytes( 
                Charset.forName(Utils.UTF_8)), myKey );
    }
    
    @Override
    protected SelectionKey getKey(){
        return myKey;
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.netserver.websocket.AbstractWSHandler75#readDelegate(byte[])
     */
    @Override
    protected void readDelegate( byte[] bytes ) {
        getWebSocketPoint().getWebSocketReadHandler().read(myKey, bytes, null);        
    }
    
    private final SelectionKey myKey;

}
