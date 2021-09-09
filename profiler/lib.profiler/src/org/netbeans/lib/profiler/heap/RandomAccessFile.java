/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.lib.profiler.heap;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

final class RandomAccessFile {
    private final java.io.RandomAccessFile delegate;

    RandomAccessFile(File file, String mode) throws FileNotFoundException {
        this.delegate = new java.io.RandomAccessFile(file.delegate, mode);
    }

    long length() throws IOException {
        return delegate.length();
    }

    void write(byte[] arr) throws IOException {
        delegate.write(arr);
    }

    void write(byte[] arr, int off, int len) throws IOException {
        delegate.write(arr, off, len);;
    }

    void setLength(long fileSize) throws IOException {
        delegate.setLength(fileSize);
    }

    void seek(long newOffset) throws IOException {
        delegate.seek(newOffset);
    }

    void readFully(byte[] buf, int off, int len) throws IOException {
        delegate.readFully(buf, off, len);
    }

    void readFully(byte[] buf) throws IOException {
        delegate.readFully(buf);
    }

    long readLong() throws IOException {
        return delegate.readLong();
    }

    ByteBuffer mmap(FileChannel.MapMode mode, long size, boolean close) throws IOException {
        FileChannel ch = delegate.getChannel();
        try {
            return ch.map(mode, 0, size);
        } finally {
            if (close) {
                ch.close();
            }
        }
    }

    ByteBuffer[] mmapAsBuffers(FileChannel.MapMode mode, long length, long BUFFER_SIZE, long BUFFER_EXT) throws IOException {
        try (FileChannel channel = delegate.getChannel()) {
            ByteBuffer[] dumpBuffer = new ByteBuffer[(int) (((length + BUFFER_SIZE) - 1) / BUFFER_SIZE)];
            for (int i = 0; i < dumpBuffer.length; i++) {
                long position = i * BUFFER_SIZE;
                long size = Math.min(BUFFER_SIZE + BUFFER_EXT, length - position);
                dumpBuffer[i] = channel.map(mode, position, size);
            }
            return dumpBuffer;
        }
    }
}
