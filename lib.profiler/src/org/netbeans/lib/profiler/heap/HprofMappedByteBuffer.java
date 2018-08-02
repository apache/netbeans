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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;


/**
 *
 * @author Tomas Hurka
 */
class HprofMappedByteBuffer extends HprofByteBuffer {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private MappedByteBuffer dumpBuffer;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    HprofMappedByteBuffer(File dumpFile) throws IOException {
        FileInputStream fis = new FileInputStream(dumpFile);
        FileChannel channel = fis.getChannel();
        length = channel.size();
        dumpBuffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, length);
        channel.close();
        readHeader();
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    char getChar(long index) {
        return dumpBuffer.getChar((int) index);
    }

    double getDouble(long index) {
        return dumpBuffer.getDouble((int) index);
    }

    float getFloat(long index) {
        return dumpBuffer.getFloat((int) index);
    }

    int getInt(long index) {
        return dumpBuffer.getInt((int) index);
    }

    long getLong(long index) {
        return dumpBuffer.getLong((int) index);
    }

    short getShort(long index) {
        return dumpBuffer.getShort((int) index);
    }

    // delegate to MappedByteBuffer
    byte get(long index) {
        return dumpBuffer.get((int) index);
    }

    synchronized void get(long position, byte[] chars) {
        dumpBuffer.position((int) position);
        dumpBuffer.get(chars);
    }
}
