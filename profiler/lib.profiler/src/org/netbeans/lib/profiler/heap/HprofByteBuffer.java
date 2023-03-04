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
import java.util.Date;
import java.util.ResourceBundle;
import static org.netbeans.lib.profiler.heap.Systems.DEBUG;


/**
 *
 * @author Tomas Hurka
 */
abstract class HprofByteBuffer {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    // Magic header
    static final String magic1 = "JAVA PROFILE 1.0.1"; // NOI18N
    static final String magic2 = "JAVA PROFILE 1.0.2"; // NOI18N
    static final String magic3 = "JAVA PROFILE 1.0.3"; // NOI18N
    static final int JAVA_PROFILE_1_0_1 = 1;
    static final int JAVA_PROFILE_1_0_2 = 2;
    static final int JAVA_PROFILE_1_0_3 = 3;
    static final int MINIMAL_SIZE = 30;

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    int idSize;
    int version;
    long headerSize;
    long length;
    long time;

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    static HprofByteBuffer createHprofByteBuffer(File dumpFile)
                                          throws IOException {
        long fileLen = dumpFile.length();

        if (fileLen < MINIMAL_SIZE) {
            String errText = ResourceBundle.getBundle("org/netbeans/lib/profiler/heap/Bundle")
                                           .getString("HprofByteBuffer_ShortFile"); // NOI18N
            throw new IOException(errText);
        }

        try {
            if (fileLen < Integer.MAX_VALUE) {
                return new HprofMappedByteBuffer(dumpFile);
            } else {
                return new HprofLongMappedByteBuffer(dumpFile);
            }
        } catch (IOException ex) {
            if (ex.getCause() instanceof OutOfMemoryError) { // can happen on 32bit Windows, since there is only 2G for memory mapped data for whole java process.

                return new HprofFileBuffer(dumpFile);
            }

            throw ex;
        }
    }

    static HprofByteBuffer createHprofByteBuffer(ByteBuffer bb) throws IOException {
        return new HprofMappedByteBuffer(bb);
    }

    abstract char getChar(long index);

    abstract double getDouble(long index);

    abstract float getFloat(long index);

    long getHeaderSize() {
        return headerSize;
    }

    long getID(long offset) {
        if (idSize == 4) {
            return ((long)getInt(offset)) & 0xFFFFFFFFL;
        } else if (idSize == 8) {
            return getLong(offset);
        }
        assert false;

        return -1;
    }

    int getIDSize() {
        return idSize;
    }

    int getFoffsetSize() {
        return length<Integer.MAX_VALUE ? 4 : 8;        
    }
    
    abstract int getInt(long index);

    abstract long getLong(long index);

    abstract short getShort(long index);

    long getTime() {
        return time;
    }

    long capacity() {
        return length;
    }

    abstract byte get(long index);

    abstract void get(long position, byte[] chars);

    void readHeader() throws IOException {
        long[] offset = new long[1];
        String magic = readStringNull(offset, MINIMAL_SIZE);

        if (DEBUG) {
            Systems.debug("Magic " + magic); // NOI18N
        }

        if (magic1.equals(magic)) {
            version = JAVA_PROFILE_1_0_1;
        } else if (magic2.equals(magic)) {
            version = JAVA_PROFILE_1_0_2;
        } else if (magic3.equals(magic)) {
            version = JAVA_PROFILE_1_0_3;
        } else {
            if (DEBUG) {
                Systems.debug("Invalid version"); // NOI18N
            }

            String errText = ResourceBundle.getBundle("org/netbeans/lib/profiler/heap/Bundle")
                                           .getString("HprofByteBuffer_InvalidFormat");
            throw new IOException(errText);
        }

        idSize = getInt(offset[0]);
        offset[0] += 4;
        time = getLong(offset[0]);
        offset[0] += 8;

        if (DEBUG) {
            Systems.debug("ID " + idSize); // NOI18N
        }

        if (DEBUG) {
            Systems.debug("Date " + new Date(time).toString()); // NOI18N
        }

        headerSize = offset[0];
    }

    private String readStringNull(long[] offset, int len) {
        StringBuilder s = new StringBuilder(20);
        byte b = get(offset[0]++);

        for (; (b > 0) && (s.length() < len); b = get(offset[0]++)) {
            s.append((char) b);
        }

        return s.toString();
    }
}
