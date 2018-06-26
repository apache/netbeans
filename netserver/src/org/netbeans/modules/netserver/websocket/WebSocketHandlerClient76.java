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
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Random;


/**
 * @author ads
 *
 */
class WebSocketHandlerClient76 extends WebSocketHandlerClient75 {
    
    private long MAX = 4294967295L;

    WebSocketHandlerClient76( WebSocketClientImpl webSocketClient ) {
        super(webSocketClient);
        myRandom = new Random(hashCode());
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.netserver.websocket.WebSocketHandlerClient75#sendHandshake()
     */
    @Override
    public void sendHandshake() {
        StringBuilder builder = new StringBuilder(Utils.GET);
        builder.append(' ');
        builder.append(getWebSocketPoint().getUri().getPath());
        builder.append(' ');
        builder.append( Utils.HTTP_11);
        builder.append(Utils.CRLF);
        
        builder.append(Utils.HOST);
        builder.append(": ");                               // NOI18N
        builder.append(getWebSocketPoint().getUri().getHost());
        builder.append(Utils.CRLF);
        
        builder.append(Utils.CONN_UPGRADE);
        builder.append(Utils.CRLF);
        
        builder.append(Utils.WS_UPGRADE);
        builder.append(Utils.CRLF);
        
        builder.append("Origin: ");
        builder.append( Utils.getOrigin(getWebSocketPoint().getUri()));
        builder.append(Utils.CRLF);
        
        builder.append(Utils.KEY1);
        builder.append(": ");                               // NOI18N
        builder.append( getKey1());
        builder.append(Utils.CRLF);
        
        builder.append(Utils.KEY2);
        builder.append(": ");                               // NOI18N
        builder.append( getKey2());
        builder.append(Utils.CRLF);
        
        builder.append(Utils.WS_PROTOCOL);
        builder.append(": chat");                             // NOI18N
        
        builder.append( Utils.CRLF );
        builder.append( Utils.CRLF );
        
        byte[] bytes = builder.toString().getBytes( 
                Charset.forName(Utils.UTF_8));
        byte[] generated = getContent();
        byte[] toSend = new byte[ bytes.length +generated.length];
        System.arraycopy(bytes, 0, toSend, 0, bytes.length);
        System.arraycopy(generated, 0, toSend, bytes.length, generated.length);
        getWebSocketPoint().send( toSend, getKey() );
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.netserver.websocket.WebSocketHandlerClient75#readHandshakeResponse(java.nio.ByteBuffer)
     */
    @Override
    protected void readHandshakeResponse( ByteBuffer buffer )
            throws IOException
    {
        byte[] md5Challenge = new byte[16];
        Utils.readHttpRequest(getWebSocketPoint().getChannel(), buffer, md5Challenge);
        boolean md5red = false;
        for( byte b: md5Challenge ){
            if ( b!= 0){
                md5red = true;
                break;
            }
        }
        if ( !md5red ){
            md5Challenge = readRequestContent(16);
        }
        if ( md5Challenge == null ){
            throw new IOException("Invalid handshake. Cannot read handshake content."); // NOI18N
        }
        else {
            byte[] challenge = Utils.produceChallenge76(getKey1(), 
                    getKey2(), getContent());
            if ( !Arrays.equals(md5Challenge, challenge)) {
                throw new IOException("Invalid handshake. Expected challenge :" + 
                        Arrays.toString(challenge)+
                		" differs from recieved : "+Arrays.toString( md5Challenge)); // NOI18N
            }
        }
    }
    
    private Random getRandom(){
        return myRandom;
    }
    
    private String getKey1(){
        if ( myKey1 == null ){
            myKey1 = generateKey();
        }
        return myKey1;
    }
    
    private String getKey2(){
        if ( myKey2 == null ){
            myKey2 = generateKey();
        }
        return myKey2;
    }
    
    private String generateKey(){
        int spaces = getRandom().nextInt( 12 ) + 1;
        int max = (int)(MAX/spaces);
        max = Math.abs(max);
        if ( max == Integer.MIN_VALUE){
            max = Integer.MAX_VALUE;
        }
        int num = getRandom().nextInt(max)+1;
        long prod = num * spaces;
        StringBuilder key = new StringBuilder( );
        key.append(prod);
        int randomCount = getRandom().nextInt( 12 ) + 1;
        for (int i=0; i<randomCount ; i++){
            int index = getRandom().nextInt(key.length());
            key.insert(index , getNoNumberChar());
        }
        for( int i=0; i<spaces; i++){
            int index = getRandom().nextInt(key.length()-1)+1;
            key.insert(index, ' ');
        }
        return key.toString();
    }
    
    private char getNoNumberChar(){
        char ch = (char)(getRandom().nextInt(0x7e-0x21+1)+0x21);
        if ( ch > 0x2f && ch< 0x3a){
            return getNoNumberChar();
        }
        return ch;
    }
    
    private byte[] getContent(){
        if ( myContent == null ){
            myContent = new byte[8];
            getRandom().nextBytes(myContent);
        }
        return myContent;
    }
    
    private Random myRandom;
    private String myKey1;
    private String myKey2;
    private byte[] myContent;
}
