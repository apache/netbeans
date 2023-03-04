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
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;


/**
 * @author ads
 *
 */
final class Utils {
    
    public static final String UTF_8 = "UTF-8";                    // NOI18N
    
    private static final Charset UTF_CHARSET = Charset.forName(UTF_8);
    
    private static final char NEW_LINE = '\n';
    public static final int BYTES = 1000;
    
    static final String HTTP_11 = "HTTP/1.1";                       // NOI18N
    
    static final String HTTP_RESPONSE = HTTP_11+" 101 Web Socket Protocol Handshake"; // NOI18N
    
    static final String GET = "GET";
    
    static final String WS_UPGRADE = "Upgrade: WebSocket";                            // NOI18N
    
    static final String WS_UPGRADE_1 = "Upgrade: websocket";                          // NOI18N
    
    static final String CONN_UPGRADE = "Connection: Upgrade";                         // NOI18N
    
    static final String CRLF = "\r\n";                                                // NOI18N
    
    static final String HOST = "Host";                                                // NOI18N
    
    static final String WS_PROTOCOL = "WebSocket-Protocol";                           // NOI18N
    
    static final String VERSION = "Sec-WebSocket-Version";  // NOI18N
    static final String KEY = "Sec-WebSocket-Key";          // NOI18N
    static final String KEY1 = "Sec-WebSocket-Key1";        // NOI18N
    static final String KEY2 = "Sec-WebSocket-Key2";        // NOI18N
    static final String ACCEPT = "Sec-WebSocket-Accept";    // NOI18N
    
    private Utils(){
    }
    
    static List<String> readHttpRequest(SocketChannel socketChannel,
            ByteBuffer buffer ) throws IOException
    {
        return readHttpRequest(socketChannel, buffer, null );
    }
    
    static List<String> readHttpRequest(SocketChannel socketChannel,
            ByteBuffer buffer , byte[] content) throws IOException
    {
            List<String> headers = new LinkedList<String>();
            buffer.clear();
            StringBuilder builder = new StringBuilder();
            byte[] bytes = new byte[ BYTES ];
            boolean readContent = content != null;
            List<Byte> remaining = new LinkedList<Byte>();
            read: while( true ){
                int read = socketChannel.read( buffer );
                if ( read ==-1 ){
                    return Collections.emptyList();
                }
                buffer.flip();
                int size = buffer.limit();
                buffer.get( bytes , 0, size);
                buffer.clear();
                String stringValue = new String( bytes , 0, size, 
                        Charset.forName(UTF_8) );
                String fullString = stringValue;
                int index = stringValue.indexOf(NEW_LINE);
                if ( index == -1 ){
                    builder.append( stringValue );
                    if ( readContent ){
                        copyBytes(bytes, remaining, 0, size);
                    }
                }
                else {
                    if ( readContent ){
                        remaining = new LinkedList<Byte>();
                    }
                    builder.append( stringValue.subSequence(0, index));
                    String line = builder.toString().trim();
                    headers.add( line );
                    builder.setLength(0);
                    if ( line.isEmpty() ){
                        int start = stringValue.substring(0, index+1).getBytes(
                                UTF_CHARSET).length;
                        copyBytes(bytes, remaining, start, size );
                        break;
                    }
                    int fullIndex = index;
                    do {
                        stringValue = stringValue.substring( index +1);
                        index = stringValue.indexOf(NEW_LINE );
                        if ( index != -1){
                            fullIndex+=(index+1);
                            line = stringValue.substring( 0, index ).trim();
                            headers.add( line );
                            if ( line.isEmpty() ){
                                int start = fullString.substring(0, fullIndex+1).
                                        getBytes().length;
                                copyBytes(bytes, remaining, start, size );
                                break read;
                            }
                        }
                    }
                    while( index != -1 );
                    int start = fullString.substring(0, fullIndex+1).getBytes().length;
                    copyBytes(bytes, remaining, start, size );
                    builder.append( stringValue);
                }
            }

            if ( remaining.size() == 0 ){
                return headers;
            }
            if ( !readContent ){
                throw new IOException("Unexpected content on connection initialization");       // NOI18N
            }
            else {
                int size = content.length;
                int red = remaining.size();
                if ( red > size ){
                    throw new IOException("Unexpected content on connection initialization");       // NOI18N
                }
                ByteBuffer buf = ByteBuffer.allocate( size - red );
                while(red<size){
                    int read = socketChannel.read( buffer );
                    if ( read == -1){
                        return Collections.emptyList();
                    }
                    red+=read;
                }
                buf.flip();
                bytes = new byte[buf.capacity()];
                buf.get(bytes);
                int i=0;
                for( Byte b: remaining ){
                    content[i] = b;
                    i++;
                }
                System.arraycopy(bytes, 0, content, i, bytes.length );
            }
            return headers;
    }
    
    private static void copyBytes( byte[] src, List<Byte> dst , int startPos , 
            int lenght)
    {
        for( int i=startPos; i< lenght ; i++ ){
            dst.add( src[i]);
        }
    }
    
    static String getOrigin(URI uri ){
        String url = uri.toString();
        String host = uri.getHost();
        int index = url.indexOf(host);
        if ( index != -1 ){
            return url.substring( 0, index+host.length());
        }
        else {
            return uri.getScheme()+"://"+uri.getHost();
        }
    }
    
    static byte[] produceChallenge76( String key1, String key2, byte[] byteContent ) {
        ByteBuffer buffer = ByteBuffer.allocate(16).putInt(decodeNumber(key1))
                .putInt(decodeNumber(key2)).put(byteContent);
        buffer.flip();
        byte[] bytes = new byte[buffer.capacity()];
        buffer.get(bytes);
        try {
            return MessageDigest.getInstance("MD5").digest(bytes); // NOI18N
        }
        catch (NoSuchAlgorithmException e) {
            WebSocketServerImpl.LOG.log(Level.WARNING, null, e);
            return null;
        }
    }
    
    private static int decodeNumber(String code) {
        long number = 0;
        int spaces = 0;
        for (int i=0; i<code.length(); i++) {
            char c = code.charAt(i);
            if (c >= '0' && c <= '9') {
                number *= 10;
                number += (c-'0');
            }
            if (c == ' ') {
                spaces++;
            }
        }
        return (int)(number/spaces);
    }

}
