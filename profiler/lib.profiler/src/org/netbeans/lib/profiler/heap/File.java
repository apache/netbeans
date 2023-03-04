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
package org.netbeans.lib.profiler.heap;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel.MapMode;

abstract class File {
    interface Factory {
        File newFile(File parent, String relative);
        File newFile(String fileName);
        File newFile(java.io.File real);
        File createTempFile(String prefix, String suffix, File cacheDirectory) throws IOException;
    }

    final Factory io;

    File(Factory io) {
        this.io = io;
    }

    abstract String getAbsolutePath();
    abstract String getName();
    abstract File getParentFile();
    abstract boolean exists();
    abstract boolean mkdir();
    abstract void deleteOnExit();
    abstract boolean isDirectory();
    abstract boolean canRead();
    abstract boolean canWrite();
    abstract boolean isFile();
    abstract long length();
    abstract boolean delete();
    abstract boolean renameTo(File bufferFile);
    abstract RandomAccessFile newRandomAccessFile(String mode) throws FileNotFoundException;
    abstract DataOutputStream newDataOutputStream(int bufferSize) throws FileNotFoundException;
    abstract DataInputStream newDataInputStream(int bufferLength) throws FileNotFoundException;
    abstract ByteBuffer mmapReadOnly() throws IOException;
    abstract ByteBuffer force(MapMode mode, ByteBuffer buf) throws IOException;
    abstract ByteBuffer[] mmapReadOnlyAsBuffers(long[] length, long bufferSize, long bufferExt) throws IOException;
    abstract ByteBuffer[] force(MapMode mode, ByteBuffer[] dumpBuffer, long bufferSize, long bufferExt, long entrySize) throws IOException;
}
