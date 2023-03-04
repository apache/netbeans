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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

abstract class RandomAccessFile {
    abstract long length() throws IOException;
    abstract void write(byte[] arr) throws IOException;
    abstract void write(byte[] arr, int off, int len) throws IOException;
    abstract void setLength(long fileSize) throws IOException;
    abstract void seek(long newOffset) throws IOException;
    abstract void readFully(byte[] buf, int off, int len) throws IOException;
    abstract void readFully(byte[] buf) throws IOException;
    abstract long readLong() throws IOException;
    abstract ByteBuffer mmap(FileChannel.MapMode mode, long size, boolean close) throws IOException;
    abstract ByteBuffer[] mmapAsBuffers(FileChannel.MapMode mode, long length, long BUFFER_SIZE, long BUFFER_EXT) throws IOException;
}
