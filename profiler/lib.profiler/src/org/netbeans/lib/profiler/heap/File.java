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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

final class File {
    final java.io.File delegate;

    File(File parent, String relative) {
        this(new java.io.File(parent.delegate, relative));
    }

    File(String fileName) {
        this(new java.io.File(fileName));
    }

    File(java.io.File real) {
        this.delegate = real;
    }

    static File createTempFile(String prefix, String suffix) throws IOException {
        return new File(java.io.File.createTempFile(prefix, suffix));
    }

    static File createTempFile(String prefix, String suffix, File cacheDirectory) throws IOException {
        return new File(java.io.File.createTempFile(prefix, suffix, cacheDirectory.delegate));
    }

    String getAbsolutePath() {
        return delegate.getAbsolutePath();
    }

    String getName() {
        return delegate.getName();
    }

    File getParentFile() {
        return new File(delegate.getParentFile());
    }

    boolean exists() {
        return delegate.exists();
    }

    boolean mkdir() {
        return delegate.mkdir();
    }

    void deleteOnExit() {
        delegate.deleteOnExit();
    }

    boolean isDirectory() {
        return delegate.isDirectory();
    }

    boolean canRead() {
        return delegate.canRead();
    }

    boolean canWrite() {
        return delegate.canWrite();
    }

    boolean isFile() {
        return delegate.isFile();
    }

    long length() {
        return delegate.length();
    }

    boolean delete() {
        return delegate.delete();
    }

    boolean renameTo(File bufferFile) {
        return delegate.renameTo(bufferFile.delegate);
    }

    DataOutputStream newDataOutputStream(int bufferSize) throws FileNotFoundException {
        return new DataOutputStream(new BufferedOutputStream(new FileOutputStream(delegate), bufferSize));
    }

    DataInputStream newDataInputStream(int bufferLength) throws FileNotFoundException {
        return new DataInputStream(new BufferedInputStream(new FileInputStream(delegate), bufferLength));
    }

    ByteBuffer mmapReadOnly() throws IOException {
        FileInputStream fis = new FileInputStream(delegate);
        FileChannel channel = fis.getChannel();
        ByteBuffer d = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
        channel.close();
        return d;
    }

    ByteBuffer force(FileChannel.MapMode mode, ByteBuffer buf) throws IOException {
        if (mode == FileChannel.MapMode.PRIVATE) {
            java.io.File newBufferFile = new java.io.File(delegate.getAbsolutePath()+".new"); // NOI18N
            int length = buf.capacity();
            new FileOutputStream(newBufferFile).getChannel().write(buf);
            delegate.delete();
            newBufferFile.renameTo(delegate);
            return new RandomAccessFile(this, "rw").mmap(mode, length, true); // NOI18N
        } else {
            ((MappedByteBuffer)buf).force();
            return buf;
        }
    }

    ByteBuffer[] mmapReadOnlyAsBuffers(long[] length, long bufferSize, long bufferExt) throws IOException {
        FileInputStream fis = new FileInputStream(delegate);
        try (FileChannel channel = fis.getChannel()) {
            length[0] = channel.size();
            ByteBuffer[] buffers = new ByteBuffer[(int) (((length[0] + bufferSize) - 1) / bufferSize)];

            for (int i = 0; i < buffers.length; i++) {
                long position = i * bufferSize;
                long size = Math.min(bufferSize + bufferExt, length[0] - position);
                buffers[i] = channel.map(FileChannel.MapMode.READ_ONLY, position, size);
            }
            return buffers;
        }
    }

    ByteBuffer[] force(FileChannel.MapMode MAP_MODE, ByteBuffer[] dumpBuffer, long bufferSize, long bufferExt, long entrySize) throws IOException {
        if (MAP_MODE == FileChannel.MapMode.PRIVATE) {
            java.io.File newBufferFile = new java.io.File(getAbsolutePath()+".new"); // NOI18N
            long length = delegate.length();
            FileChannel channel = new FileOutputStream(newBufferFile).getChannel();
            int offset_start = 0;

            for (int i = 0; i < dumpBuffer.length; i++) {
                ByteBuffer buf = dumpBuffer[i];
                long offset_end = (((i+1)*bufferSize)/entrySize)*entrySize + entrySize;

                if (offset_end > length) {
                    offset_end = length;
                }
                buf.limit((int)(offset_end - i*bufferSize));
                buf.position(offset_start);
                channel.write(buf);
                offset_start = (int)(offset_end - (i+1)*bufferSize);
            }
            delegate.delete();
            newBufferFile.renameTo(delegate);
            return new RandomAccessFile(this, "rw").mmapAsBuffers(MAP_MODE, length, bufferSize, bufferExt); // NOI18N
        } else {
            for (ByteBuffer buf : dumpBuffer) {
                ((MappedByteBuffer)buf).force();
            }
            return dumpBuffer;
        }
    }
}
