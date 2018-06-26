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
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.logging.Level;


/**
 * @author ads
 *
 */
class WebSocketHandler76 extends WebSocketHandler75 implements WebSocketChanelHandler {

    public WebSocketHandler76( WebSocketServerImpl webSocketServer , SelectionKey key) {
        super(webSocketServer, key );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.common.websocket.WebSocketHandler75#sendHandshake()
     */
    @Override
    public void sendHandshake( ) throws IOException {
        byte[] lastEightBytes = readRequestContent( );
        if ( lastEightBytes == null ){
            throw new IOException("Invalid handshake. Cannot read handshake content");  // NOI18N
        }
        StringBuilder builder = new StringBuilder(Utils.HTTP_RESPONSE);
        builder.append(Utils.CRLF);
        builder.append(Utils.WS_UPGRADE);
        builder.append(Utils.CRLF);
        builder.append(Utils.CONN_UPGRADE);
        builder.append(Utils.CRLF);
        builder.append("Sec-WebSocket-Origin: ");           // NOI18N
        String origin = getWebSocketPoint().getContext(getKey()).getHeaders().get("Origin");  // NOI18N
        if ( origin != null ){
            builder.append( origin);
        }
        builder.append(Utils.CRLF);
        builder.append("Sec-WebSocket-Location: ws://");    // NOI18N
        String host = getWebSocketPoint().getContext(getKey()).getHeaders().get(Utils.HOST);                
        if ( host != null) {
            builder.append( host );
        }
        else {
            builder.append("127.0.0.1:");                   // NOI18N
            builder.append( ((InetSocketAddress)getWebSocketPoint().getAddress()).getPort());
        }
        String request = getWebSocketPoint().getContext(getKey()).getRequestString();
        int index = request.indexOf(' ');
        String url = null;
        if ( index != -1 ){
            request = request.substring(index).trim();
            index = request.indexOf(' ');
            if ( index !=-1 ){
                url = request.substring( 0, index ).trim();
            }
        }
        else {
            url ="/";                                       // NOI18N
        }
        builder.append( url );
        builder.append( Utils.CRLF );
        builder.append( Utils.CRLF );
        byte[] headers = builder.toString().getBytes( 
                Charset.forName(Utils.UTF_8));
        byte[] responseContent = createResponseContent(getKey(), lastEightBytes);
        if ( responseContent == null ){
            close( );
            return;
        }
        byte[] response = new byte[ headers.length + responseContent.length ];
        System.arraycopy(headers, 0, response, 0, headers.length);
        System.arraycopy(responseContent, 0, response, headers.length, 
                responseContent.length);
        getWebSocketPoint().send(response , getKey() );
    }

    private byte[] createResponseContent(SelectionKey key,  byte[] lastEightBytes ) {
        Map<String, String> headers = getWebSocketPoint().getContext(key).getHeaders();
        String key1 = headers.get(Utils.KEY1);
        String key2 = headers.get(Utils.KEY2);
        return Utils.produceChallenge76(key1, key2, lastEightBytes);
    }

    private byte[] readRequestContent(  ) throws IOException {
        byte[] content = getWebSocketPoint().getContext( getKey()).getContent();
        boolean red = false;
        for( byte b : content ){
            if ( b!= 0){
                red = true;
                break;
            }
        }
        if ( red ){
            return content;
        }
        else {
            return readRequestContent( 8 );
        }
    }

}
