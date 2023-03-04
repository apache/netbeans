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
package org.netbeans.core.output2;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Heap based implementation of the Storage interface, over a byte array.
 *
 */
class HeapStorage implements Storage {
    private boolean closed = true;
    private byte[] bytes = new byte[2048];
    private int size = 0;

    public Storage toFileMapStorage() throws IOException {
        FileMapStorage result = new FileMapStorage();
        BufferResource<ByteBuffer> br = getReadBuffer(0, size);
        try {
            result.write(br.getBuffer());
            return result;
        } finally {
            if (br != null) {
                br.releaseBuffer();
            }
        }
    }

    @Override
    public BufferResource<ByteBuffer> getReadBuffer(
            final int start, final int length) throws IOException {

        return new HeapBufferResource(ByteBuffer.wrap(
                bytes, start, Math.min(length, bytes.length - start)));
    }

    public ByteBuffer getWriteBuffer(int length) throws IOException {
        return ByteBuffer.allocate(length);
    }

    public synchronized int write(ByteBuffer buf) throws IOException {
        closed = false;
        int oldSize = size;
        size += buf.limit();
        if (size > bytes.length) {
            byte[] oldBytes = bytes;
            bytes = new byte[Math.max (oldSize * 2, (buf.limit() * 2) + oldSize)]; 
            System.arraycopy (oldBytes, 0, bytes, 0, oldSize);
        }
        buf.flip();
        buf.get(bytes, oldSize, buf.limit());
        return oldSize;
    }

    @Override
    public void removeBytesFromEnd(int length) throws IOException {
        size = size - length;
    }

    public synchronized void dispose() {
        bytes = new byte[0];
        size = 0;
    }

    public synchronized int size() {
        return size;
    }

    public void flush() throws IOException {
        //N/A
    }

    public void close() throws IOException {
        closed = true;
    }

    public boolean isClosed() {
        return closed;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public synchronized void shiftStart(int byteOffset) {
        size -= byteOffset;
        System.arraycopy(bytes, byteOffset, bytes, 0, size);
    }
 
    private class HeapBufferResource implements BufferResource<ByteBuffer> {

        private ByteBuffer buffer;

        public HeapBufferResource(ByteBuffer buffer) {
            this.buffer = buffer;
        }

        @Override
        public ByteBuffer getBuffer() {
            return buffer;
        }

        @Override
        public void releaseBuffer() {
            this.buffer = null;
        }
    }
}
