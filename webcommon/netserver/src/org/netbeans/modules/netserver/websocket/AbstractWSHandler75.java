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
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.netbeans.modules.netserver.SocketFramework;


/**
 * @author ads
 *
 */
abstract class AbstractWSHandler75<T extends SocketFramework> extends AbstractWSHandler<T> 
    implements WebSocketChanelHandler 
{
    AbstractWSHandler75( T framework ){
        super(framework);
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.common.websocket.WebSocketChanelHandler#read(java.nio.ByteBuffer)
     */
    @Override
    public void read( ByteBuffer byteBuffer ) throws IOException {
        SocketChannel socketChannel = (SocketChannel) getKey().channel();
        byte[] bytes = new byte[Utils.BYTES];
        List<List<Byte>> messages = new LinkedList<List<Byte>>();
        List<Byte> message = new LinkedList<Byte>();
        boolean newMessage = false;
        while (!isStopped()) {
            byteBuffer.clear();
            if (socketChannel.read(byteBuffer) == -1) {
                close();
                return;
            }
            byteBuffer.flip();
            byteBuffer.get(bytes, 0, byteBuffer.limit() );
            int start =0;
            if (bytes[0] == 0 && !newMessage) {
                start =1;
                newMessage = true;
                if (!message.isEmpty()) {
                    messages.add(new ArrayList<Byte>(message));
                }
                message.clear();
            }
            for (int i = start; i < byteBuffer.limit(); i++) {
                if (bytes[i] == (byte) 255) {
                    messages.add(new ArrayList<Byte>(message));
                    message.clear();
                    newMessage = false;
                }
                else {
                    message.add(bytes[i]);
                }
            }
            if (message.isEmpty()) {
                break;
            }
        }
        if ( isStopped() ){
            close();
            return ;
        }
        for (List<Byte> list : messages) {
            bytes = new byte[list.size()];
            int i = 0;
            for (Byte byt : list) {
                bytes[i] = byt;
                i++;
            }
            readDelegate(bytes);
        }
    }
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.web.common.websocket.WebSocketChanelHandler#createTextFrame(java.lang.String)
     */
    @Override
    public byte[] createTextFrame( String message ) {
        byte[] data = message.getBytes( Charset.forName( Utils.UTF_8));
        byte[] result = new byte[ data.length +2 ];
        result[0] = 0;
        result[ data.length +1 ]=(byte)255;
        System.arraycopy(data, 0, result, 1, data.length);
        return result;
    }
    
    protected byte[] readRequestContent(  int size ) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(size);
        SocketChannel socketChannel = (SocketChannel) getKey().channel();
        try {
            while ( buffer.hasRemaining() && !isStopped()){
                if ( socketChannel.read( buffer ) == -1){
                    close();
                }
            }
            if ( isStopped() ){
                close();
                return null;
            }
            byte[] bytes = new byte[buffer.capacity()];
            buffer.flip();
            buffer.get( bytes );
            return bytes;
        }
        catch( IOException e ){
            close();
        }
        return null;
    }
    
    protected abstract void readDelegate( byte[] bytes );
    
}
