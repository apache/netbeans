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
import java.nio.file.Files;

final class JavaIoFile extends File {
    static final Factory IO = new Factory() {
        @Override
        public File newFile(File parent, String relative) {
            return new JavaIoFile(this, (JavaIoFile) parent, relative);
        }

        @Override
        public File newFile(String fileName) {
            return new JavaIoFile(this, fileName);
        }

        @Override
        public File newFile(java.io.File real) {
            return new JavaIoFile(this, real);
        }

        @Override
        public File createTempFile(String prefix, String suffix, File cacheDirectory) throws IOException {
            if (cacheDirectory == null) {
                return newFile(Files.createTempFile(prefix, suffix).toFile());
            } else {
                return newFile(Files.createTempFile(((JavaIoFile)cacheDirectory).delegate.toPath(), prefix, suffix).toFile());
            }
        }
    };

    final java.io.File delegate;

    private JavaIoFile(Factory io, JavaIoFile parent, String relative) {
        this(io, new java.io.File(parent.delegate, relative));
    }

    private JavaIoFile(Factory io, String fileName) {
        this(io, new java.io.File(fileName));
    }

    private JavaIoFile(Factory io, java.io.File real) {
        super(io);
        this.delegate = real;
    }


    @Override
    String getAbsolutePath() {
        return delegate.getAbsolutePath();
    }

    @Override
    String getName() {
        return delegate.getName();
    }

    @Override
    File getParentFile() {
        return io.newFile(delegate.getParentFile());
    }

    @Override
    boolean exists() {
        return delegate.exists();
    }

    @Override
    boolean mkdir() {
        return delegate.mkdir();
    }

    @Override
    void deleteOnExit() {
        delegate.deleteOnExit();
    }

    @Override
    boolean isDirectory() {
        return delegate.isDirectory();
    }

    @Override
    boolean canRead() {
        return delegate.canRead();
    }

    @Override
    boolean canWrite() {
        return delegate.canWrite();
    }

    @Override
    boolean isFile() {
        return delegate.isFile();
    }

    @Override
    long length() {
        return delegate.length();
    }

    @Override
    boolean delete() {
        return delegate.delete();
    }

    @Override
    boolean renameTo(File bufferFile) {
        if (bufferFile instanceof JavaIoFile) {
            return delegate.renameTo(((JavaIoFile) bufferFile).delegate);
        } else {
            return false;
        }
    }

    @Override
    RandomAccessFile newRandomAccessFile(String mode) throws FileNotFoundException {
        return new RandomAccess(this, mode);
    }

    @Override
    DataOutputStream newDataOutputStream(int bufferSize) throws FileNotFoundException {
        return new DataOutputStream(new BufferedOutputStream(new FileOutputStream(delegate), bufferSize));
    }

    @Override
    DataInputStream newDataInputStream(int bufferLength) throws FileNotFoundException {
        return new DataInputStream(new BufferedInputStream(new FileInputStream(delegate), bufferLength));
    }

    @Override
    ByteBuffer mmapReadOnly() throws IOException {
        try (FileChannel channel = new FileInputStream(delegate).getChannel()) {
            return channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
        }
    }

    @Override
    ByteBuffer force(FileChannel.MapMode mode, ByteBuffer buf) throws IOException {
        if (mode == FileChannel.MapMode.PRIVATE) {
            java.io.File newBufferFile = new java.io.File(delegate.getAbsolutePath()+".new"); // NOI18N
            int length = buf.capacity();
            new FileOutputStream(newBufferFile).getChannel().write(buf);
            delegate.delete();
            newBufferFile.renameTo(delegate);
            return newRandomAccessFile("rw").mmap(mode, length, true); // NOI18N
        } else {
            ((MappedByteBuffer)buf).force();
            return buf;
        }
    }

    @Override
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

    @Override
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
            return newRandomAccessFile("rw").mmapAsBuffers(MAP_MODE, length, bufferSize, bufferExt); // NOI18N
        } else {
            for (ByteBuffer buf : dumpBuffer) {
                ((MappedByteBuffer)buf).force();
            }
            return dumpBuffer;
        }
    }

    private static final class RandomAccess extends RandomAccessFile {

        private final java.io.RandomAccessFile delegate;

        RandomAccess(JavaIoFile file, String mode) throws FileNotFoundException {
            super();
            this.delegate = new java.io.RandomAccessFile(file.delegate, mode);
        }

        @Override
        long length() throws IOException {
            return delegate.length();
        }

        @Override
        void write(byte[] arr) throws IOException {
            delegate.write(arr);
        }

        @Override
        void write(byte[] arr, int off, int len) throws IOException {
            delegate.write(arr, off, len);
        }

        @Override
        void setLength(long fileSize) throws IOException {
            delegate.setLength(fileSize);
        }

        @Override
        void seek(long newOffset) throws IOException {
            delegate.seek(newOffset);
        }

        @Override
        void readFully(byte[] buf, int off, int len) throws IOException {
            delegate.readFully(buf, off, len);
        }

        @Override
        void readFully(byte[] buf) throws IOException {
            delegate.readFully(buf);
        }

        @Override
        long readLong() throws IOException {
            return delegate.readLong();
        }

        @Override
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

        @Override
        ByteBuffer[] mmapAsBuffers(FileChannel.MapMode mode, long length, long BUFFER_SIZE, long BUFFER_EXT) throws IOException {
            try (final FileChannel channel = delegate.getChannel()) {
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
}
