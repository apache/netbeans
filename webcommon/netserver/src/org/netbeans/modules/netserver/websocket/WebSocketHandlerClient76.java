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
