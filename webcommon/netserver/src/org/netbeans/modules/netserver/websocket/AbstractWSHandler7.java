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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;


import org.netbeans.modules.netserver.SocketFramework;


/**
 * @author ads
 *
 */
abstract class AbstractWSHandler7<T extends SocketFramework> extends AbstractWSHandler<T>
    implements WebSocketChanelHandler
{

    protected static final String SALT = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";     // NOI18N


    protected static final byte FINISH_BYTE = Integer.valueOf("10000000",     // NOI18N
        2).byteValue();

    protected static final byte CONT_BYTE = Integer.valueOf("00000000",     // NOI18N
        2).byteValue();

    /**
     * FIN bit is set and opcode is text ( equals 1 )
     */
    protected static final byte FIRST_BYTE_MESSAGE = Integer.valueOf("10000001",     // NOI18N
            2).byteValue();

    protected static final byte FIRST_CONT_BYTE_MESSAGE = Integer.valueOf("00000001",     // NOI18N
            2).byteValue();

    /**
     * FIN bit is set and opcode is close connection ( equals 8 )
     */
    protected static final byte CLOSE_CONNECTION_BYTE = Integer.valueOf("10001000",     // NOI18N
            2).byteValue();

    /**
     * FIN bit is set and opcode is binary ( equals 2 )
     */
    protected static final byte FIRST_BYTE_BINARY = Integer.valueOf("10000010",     // NOI18N
            2).byteValue();

    protected static final byte FIRST_CONT_BYTE_BINARY = Integer.valueOf("00000010",     // NOI18N
            2).byteValue();

    /*
     * Message max length which is marked in the message with 126 code in the
     * "Extended payload length" section
     */
    protected static final int LENGTH_LEVEL  = 0x10000;

    private static final Logger LOGGER = Logger.getLogger(AbstractWSHandler7.class.getName());

    private final AtomicReference<byte[]> readData = new AtomicReference<byte[]>();

    private final AtomicInteger prevFrameType = new AtomicInteger();

    AbstractWSHandler7(T t){
        super( t );
        myRandom = new Random( hashCode() );
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.web.common.websocket.WebSocketChanelHandler#read(java.nio.ByteBuffer)
     */
    @Override
    public void read(ByteBuffer byteBuffer) throws IOException {
        SocketChannel socketChannel = (SocketChannel) getKey().channel();

        while (true) {
            byteBuffer.clear();
            byteBuffer.limit(1);
            int size = socketChannel.read(byteBuffer);
            if (size == -1) {
                close();
                return;
            } else if (size == 0) {
                return;
            }
            byteBuffer.flip();
            byte leadingByte = byteBuffer.get();
            if (leadingByte == CLOSE_CONNECTION_BYTE) {
                // connection close
                close();
                return;
            } else if (leadingByte == FIRST_BYTE_MESSAGE
                    || leadingByte == FIRST_BYTE_BINARY) {
                prevFrameType.set(0);
                readData.set(null);
                // TODO : rewrite to the new methods returning byte[]
                if (!readFinalFrame( byteBuffer, socketChannel, leadingByte)){
                    return;
                }
            } else if (leadingByte == FIRST_CONT_BYTE_MESSAGE
                    || leadingByte == FIRST_CONT_BYTE_BINARY
                    || leadingByte == CONT_BYTE) {

                if (leadingByte == FIRST_CONT_BYTE_MESSAGE) {
                    prevFrameType.set(1);
                } else if (leadingByte == FIRST_CONT_BYTE_BINARY) {
                    prevFrameType.set(2);
                }

                byte[] data = readFrame(byteBuffer, socketChannel, prevFrameType.get());
                byte[] current = readData.get();
                if (current == null) {
                    readData.set(data);
                } else {
                    byte[] newData = new byte[current.length + data.length];
                    System.arraycopy(current, 0, newData, 0, current.length);
                    System.arraycopy(data, 0, newData, current.length, data.length);
                    readData.set(newData);
                }

            } else if (leadingByte == FINISH_BYTE) {
                byte[] current = readData.get();
                int currentFrameType = prevFrameType.get();
                byte[] data = readFrame(byteBuffer, socketChannel, currentFrameType);
                if (current == null) {
                    LOGGER.log(Level.INFO, "The previous data has been null");
                    if (data != null) {
                        readDelegate(data, currentFrameType);
                    }
                } else {
                    byte[] newData = new byte[current.length + data.length];
                    System.arraycopy(current, 0, newData, 0, current.length);
                    System.arraycopy(data, 0, newData, current.length, data.length);
                    readDelegate(newData, currentFrameType);
                }
                prevFrameType.set(0);
                readData.set(null);
            } else {
                // TODO : handle ping frame
                prevFrameType.set(0);
                readData.set(null);
                LOGGER.log(Level.INFO, "Unhandled frame {0}", leadingByte);
            }
        }
    }

    @Override
    public byte[] createTextFrame( String message ) {
        byte[] data = message.getBytes( Charset.forName( Utils.UTF_8));
        int length = data.length;
        byte[] lengthBytes;
        if ( length< 126){
            lengthBytes =new byte[]{ (byte)length };
        }
        else if (length < LENGTH_LEVEL){
            lengthBytes = new byte[]{126, (byte)(length>>8), (byte)(length&0xFF)};
        }
        else {
            lengthBytes = new byte[9];
            lengthBytes[0] = 127;
            for( int i =8; i>=1; i-- ){
                lengthBytes[i]=(byte)(length & 0xFF);
                length = length >>8;
            }
        }
        int startBytesCount;
        if ( isClient() ){
            startBytesCount = 5;
            lengthBytes[0]=(byte)(lengthBytes[0]|0x80);
        }
        else {
            startBytesCount = 1;

        }
        byte[] result = new byte[data.length+lengthBytes.length+startBytesCount];
        result[0] = FIRST_BYTE_MESSAGE;
        System.arraycopy(lengthBytes, 0, result, 1, lengthBytes.length);
        /*
         *  Don't fill mask at all. XOR with 0 mask doesn't change the value
         *  XXX: data could be masked
         */
        System.arraycopy( data, 0 , result, lengthBytes.length+startBytesCount, data.length);
        return result;
    }

    protected byte[] mask( byte[] maskedMessage , boolean hasMask) {
        if (hasMask) {
            byte[] result = new byte[maskedMessage.length - 4];
            for (int i = 4; i < maskedMessage.length; i++) {
                byte unsignedMask = (byte) (maskedMessage[i % 4] & 0xFF);
                result[i - 4] = (byte) (unsignedMask ^ maskedMessage[i]);
            }
            return result;
        }
        else {
            return maskedMessage;
        }
    }

    protected String generateAcceptKey(String key ){
        StringBuilder builder = new StringBuilder( key );
        builder.append(SALT);
        try {
            return Base64.getEncoder().encodeToString( MessageDigest.getInstance(
                    "SHA").digest(builder.toString().getBytes(  // NOI18N
                            Charset.forName(Utils.UTF_8))));
        }
        catch (NoSuchAlgorithmException e) {
            WebSocketServerImpl.LOG.log(Level.WARNING, null , e);
            return null;
        }
    }

    /*
     * Method could be used in {@link #createTextFrame(String)} for setting
     * mask ( currently trivial static mask is used ) instead of {@link #isClient()}
     * method usage and for 16 bit sec-websocket key in initial WS client request
     *
     */
    protected Random getRandom(){
        return myRandom;
    }

    private boolean readFinalFrame( ByteBuffer byteBuffer,
            SocketChannel socketChannel, byte leadingByte) throws IOException
    {
        int frameType = leadingByte == FIRST_BYTE_MESSAGE? 1:2;
        byteBuffer.clear();
        byteBuffer.limit(1);
        int size ;
        do {
            size = socketChannel.read(byteBuffer);
            if ( size==-1 ){
                close( );
                return false;
            }
        }
        while( size ==0 && !isStopped());
        if ( isStopped() ){
            close();
            return false;
        }
        byteBuffer.flip();
        byte masknLength = byteBuffer.get();
        boolean hasMask =masknLength<0;
        if ( !verifyMask(hasMask) ){
            close();
            return false;
        }
        int length = masknLength&0x7F;
        if ( length <126 ){
            return readData(byteBuffer, socketChannel, frameType, length, hasMask );
        }
        else if ( length ==126 ){
            byteBuffer.clear();
            byteBuffer.limit(2);
            do {
                size = socketChannel.read(byteBuffer);
                if (  size==-1 ){
                    close( );
                    return false;
                }
            }
            while(byteBuffer.position()<byteBuffer.limit() && !isStopped());
            if ( isStopped() ){
                close();
                return false;
            }
            byteBuffer.flip();
            length = byteBuffer.getShort()&0xFFFF;
            return readData(byteBuffer, socketChannel, frameType, length, hasMask );
        }
        else if ( length ==127 ){
            byteBuffer.clear();
            byteBuffer.limit(8);
            do {
                size = socketChannel.read(byteBuffer);
                if (  size==-1 ){
                    close( );
                    return false;
                }
            }
            while(byteBuffer.position()<byteBuffer.limit() && !isStopped());
            if ( isStopped() ){
                close();
                return false;
            }
            byteBuffer.flip();
            long longLength = byteBuffer.getLong();
            return readData(byteBuffer, socketChannel, frameType, longLength ,
                    hasMask);
        }
        return true;
    }

    private byte[] readFrame(ByteBuffer byteBuffer, SocketChannel socketChannel,
            int frameType) throws IOException {

        byteBuffer.clear();
        byteBuffer.limit(1);
        int size;
        do {
            size = socketChannel.read(byteBuffer);
            if (size == -1) {
                close();
                return null;
            }
        } while (size == 0 && !isStopped());
        if (isStopped()) {
            close();
            return null;
        }
        byteBuffer.flip();
        byte masknLength = byteBuffer.get();
        boolean hasMask = masknLength < 0;
        if (!verifyMask(hasMask)) {
            close();
            return null;
        }
        int length = masknLength & 0x7F;
        if (length < 126) {
            return readFrameData(byteBuffer, socketChannel, frameType, length, hasMask);
        } else if (length == 126) {
            byteBuffer.clear();
            byteBuffer.limit(2);
            do {
                size = socketChannel.read(byteBuffer);
                if (size == -1) {
                    close();
                    return null;
                }
            } while (byteBuffer.position() < byteBuffer.limit() && !isStopped());
            if (isStopped()) {
                close();
                return null;
            }
            byteBuffer.flip();
            length = byteBuffer.getShort() & 0xFFFF;
            return readFrameData(byteBuffer, socketChannel, frameType, length, hasMask);
        } else { // length == 127
            assert length == 127 : length;
            byteBuffer.clear();
            byteBuffer.limit(8);
            do {
                size = socketChannel.read(byteBuffer);
                if (size == -1) {
                    close();
                    return null;
                }
            } while (byteBuffer.position() < byteBuffer.limit() && !isStopped());
            if (isStopped()) {
                close();
                return null;
            }
            byteBuffer.flip();
            long longLength = byteBuffer.getLong();
            return readFrameDataCheck(byteBuffer, socketChannel, frameType, longLength,
                    hasMask);
        }
    }

    private boolean readData( ByteBuffer byteBuffer,
            SocketChannel socketChannel, int frameType, int length ,
            boolean hasMask) throws IOException
    {
        byteBuffer.clear();
        int frameSize = hasMask?length +4:length;
        if ( frameSize <0 ){
            readData(byteBuffer, socketChannel, frameType, (long)length, hasMask );
        }
        byte[] result = readData( byteBuffer, socketChannel, frameSize);
        if ( result == null ){
            return false;
        }
        readDelegate(mask( result, hasMask), frameType);

        return true;
    }

    private byte[] readFrameData(ByteBuffer byteBuffer,
            SocketChannel socketChannel, int frameType, int length,
            boolean hasMask) throws IOException {
        byteBuffer.clear();
        int frameSize = hasMask ? length + 4 : length;
        if (frameSize < 0) {
            return readFrameDataCheck(byteBuffer, socketChannel, frameType, (long) length, hasMask);
        }
        byte[] result = mask(readData(byteBuffer, socketChannel, frameSize), hasMask);
        return result;
    }

    private byte[] readData( ByteBuffer byteBuffer,
            SocketChannel socketChannel, int size ) throws IOException
    {
        int redBytes =0;
        byte[] result = new byte[ size ];
        int fullBufferCount =0;
        if (size < byteBuffer.capacity()) {
            byteBuffer.limit(size);
        }
        while( redBytes <size && !isStopped()){
            int red = socketChannel.read( byteBuffer );
            if ( red == -1){
                close();
                return null;
            }
            if ( red ==0 ){
                continue;
            }
            redBytes += red;
            if (redBytes%byteBuffer.capacity() == 0){
                byteBuffer.flip();
                byteBuffer.get( result , fullBufferCount*byteBuffer.capacity(),
                        byteBuffer.limit());
                fullBufferCount++;
                byteBuffer.clear();
                int resultRed = fullBufferCount*byteBuffer.capacity();
                if ( size- resultRed<=byteBuffer.capacity()){
                    byteBuffer.limit( size -resultRed);
                }
            }
        }
        if ( isStopped() ){
            close();
            return null;
        }
        byteBuffer.flip();
        int savedBytes = byteBuffer.capacity()*fullBufferCount;
        byteBuffer.get( result , savedBytes, size - savedBytes);
        return result;
    }

    private boolean readData(ByteBuffer byteBuffer,
            SocketChannel socketChannel, int frameType, long length ,
            boolean hasMask ) throws IOException
    {
        int shift = (int)(length>>32);
        if ( shift != 0 ){
            throw new RuntimeException("Data frame is too big. " +
                    "Cannot handle it. Implementation should be rewritten.");
        }
        else {
            readData(byteBuffer, socketChannel, frameType, (int)length , hasMask );
        }
        return true;
    }

    private byte[] readFrameDataCheck(ByteBuffer byteBuffer,
            SocketChannel socketChannel, int frameType, long length ,
            boolean hasMask ) throws IOException
    {
        int shift = (int)(length>>32);
        if ( shift != 0 ){
            throw new RuntimeException("Data frame is too big. " +
                    "Cannot handle it. Implementation should be rewritten.");
        }
        return readFrameData(byteBuffer, socketChannel, frameType, (int)length , hasMask );
    }

    /**
     * XXX: method could be changed to method which generate random mask.
     * In the latter case this mask should be applied on data in <code>createTextFrame</code> method
     * @return
     */
    protected abstract boolean isClient();

    protected abstract void readDelegate( byte[] bytes , int dataType ) ;

    protected abstract boolean verifyMask( boolean hasMask ) throws IOException ;

    private Random myRandom;

}
