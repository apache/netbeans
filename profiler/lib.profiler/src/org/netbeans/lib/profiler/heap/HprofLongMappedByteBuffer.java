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


/**
 *
 * @author Tomas Hurka
 */
class HprofLongMappedByteBuffer extends HprofByteBuffer {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    private static int BUFFER_SIZE_BITS = 30;
    private static long BUFFER_SIZE = 1L << BUFFER_SIZE_BITS;
    private static int BUFFER_SIZE_MASK = (int) ((BUFFER_SIZE) - 1);
    private static int BUFFER_EXT = 32 * 1024;

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private final ByteBuffer[] dumpBuffer;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    HprofLongMappedByteBuffer(File dumpFile) throws IOException {
        long[] len = { 0 };
        this.dumpBuffer = dumpFile.mmapReadOnlyAsBuffers(len, BUFFER_SIZE, BUFFER_EXT);
        this.length = len[0];
        readHeader();
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    char getChar(long index) {
        return dumpBuffer[getBufferIndex(index)].getChar(getBufferOffset(index));
    }

    double getDouble(long index) {
        return dumpBuffer[getBufferIndex(index)].getDouble(getBufferOffset(index));
    }

    float getFloat(long index) {
        return dumpBuffer[getBufferIndex(index)].getFloat(getBufferOffset(index));
    }

    int getInt(long index) {
        return dumpBuffer[getBufferIndex(index)].getInt(getBufferOffset(index));
    }

    long getLong(long index) {
        return dumpBuffer[getBufferIndex(index)].getLong(getBufferOffset(index));
    }

    short getShort(long index) {
        return dumpBuffer[getBufferIndex(index)].getShort(getBufferOffset(index));
    }

    // delegate to MappedByteBuffer        
    byte get(long index) {
        return dumpBuffer[getBufferIndex(index)].get(getBufferOffset(index));
    }

    synchronized void get(long position, byte[] chars) {
        ByteBuffer buffer = dumpBuffer[getBufferIndex(position)];
        buffer.position(getBufferOffset(position));
        buffer.get(chars);
    }

    private int getBufferIndex(long index) {
        return (int) (index >> BUFFER_SIZE_BITS);
    }

    private int getBufferOffset(long index) {
        return (int) (index & BUFFER_SIZE_MASK);
    }
}
