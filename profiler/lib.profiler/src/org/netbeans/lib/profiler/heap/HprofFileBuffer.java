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

import java.io.EOFException;
import java.io.IOException;


/**
 *
 * @author Tomas Hurka
 */
class HprofFileBuffer extends HprofByteBuffer {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    private static final int MAX_bufferSizeBits = 17;
    private static final int MIN_bufferSizeBits = 7;
    private static final int MIN_bufferSize = 1 << MIN_bufferSizeBits;
    private static final int MIN_bufferSizeMask = MIN_bufferSize - 1;
    private static final int BUFFER_EXT = 8;

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    RandomAccessFile fis;
    private byte[] dumpBuffer;
    private long bufferStartOffset;
    private int bufferSizeBits;
    private int bufferSize;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    HprofFileBuffer(File dumpFile) throws IOException {
        fis = dumpFile.newRandomAccessFile("r");
        length = fis.length();
        bufferStartOffset = Long.MAX_VALUE;
        readHeader();
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    synchronized char getChar(long index) {
        int i = loadBufferIfNeeded(index);
        int ch1 = ((int) dumpBuffer[i++]) & 0xFF;
        int ch2 = ((int) dumpBuffer[i]) & 0xFF;

        return (char) ((ch1 << 8) + (ch2 << 0));
    }

    synchronized double getDouble(long index) {
        int i = loadBufferIfNeeded(index);

        return Double.longBitsToDouble(getLong(i));
    }

    synchronized float getFloat(long index) {
        int i = loadBufferIfNeeded(index);

        return Float.intBitsToFloat(getInt(i));
    }

    synchronized int getInt(long index) {
        int i = loadBufferIfNeeded(index);
        int ch1 = ((int) dumpBuffer[i++]) & 0xFF;
        int ch2 = ((int) dumpBuffer[i++]) & 0xFF;
        int ch3 = ((int) dumpBuffer[i++]) & 0xFF;
        int ch4 = ((int) dumpBuffer[i]) & 0xFF;

        return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
    }

    synchronized long getLong(long index) {
        return ((long) (getInt(index)) << 32) + (getInt(index + 4) & 0xFFFFFFFFL);
    }

    synchronized short getShort(long index) {
        int i = loadBufferIfNeeded(index);
        int ch1 = ((int) dumpBuffer[i++]) & 0xFF;
        int ch2 = ((int) dumpBuffer[i]) & 0xFF;

        return (short) ((ch1 << 8) + (ch2 << 0));
    }

    // delegate to MappedByteBuffer    
    synchronized byte get(long index) {
        int i = loadBufferIfNeeded(index);

        return dumpBuffer[i];
    }

    synchronized void get(long position, byte[] chars) {
        int i = loadBufferIfNeeded(position);

        if ((i + chars.length) < dumpBuffer.length) {
            System.arraycopy(dumpBuffer, i, chars, 0, chars.length);
        } else {
            try {
                fis.seek(position);
                fis.readFully(chars);
            } catch (IOException ex) {
                Systems.printStackTrace(ex);
            }
        }
    }

    private void setBufferSize(long newBufferStart) {
        if ((newBufferStart > bufferStartOffset) && (newBufferStart < (bufferStartOffset + (2 * bufferSize)))) { // sequential read -> increase buffer size

            if (bufferSizeBits < MAX_bufferSizeBits) {
                setBufferSize(bufferSizeBits + 1);
            }
        } else { // reset buffer size
            setBufferSize(MIN_bufferSizeBits);
        }
    }

    private void setBufferSize(int newBufferSizeBits) {
        bufferSizeBits = newBufferSizeBits;
        bufferSize = 1 << bufferSizeBits;
        dumpBuffer = new byte[bufferSize + BUFFER_EXT];
    }

    private int loadBufferIfNeeded(long index) {
        if ((index >= bufferStartOffset) && (index < (bufferStartOffset + bufferSize))) {
            return (int) (index - bufferStartOffset);
        }

        long newBufferStart = index & ~MIN_bufferSizeMask;
        setBufferSize(newBufferStart);

        try {
            fis.seek(newBufferStart);
            fis.readFully(dumpBuffer);

            //Systems.debug("Reading at "+newBufferStart+" size "+dumpBuffer.length+" thread "+Thread.currentThread().getName());
        } catch (EOFException ex) {
            // ignore
        } catch (IOException ex) {
            Systems.printStackTrace(ex);
        }

        bufferStartOffset = newBufferStart;

        return (int) (index - bufferStartOffset);
    }
}
