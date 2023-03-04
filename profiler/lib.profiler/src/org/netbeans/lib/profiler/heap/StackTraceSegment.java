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

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Tomas Hurka
 */
class StackTraceSegment extends TagBounds {

    private static final int SERIALNUM_DIV = 16;
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    HprofHeap hprofHeap;
    final int threadSerialNumberOffset;
    final int stackTraceSerialNumberOffset;
    final int lengthOffset;
    final int framesListOffset;
    final int numberOfFramesOffset;
    final int timeOffset;
    private Map serialNumToStackTrace;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    StackTraceSegment(HprofHeap heap, long start, long end) {
        super(HprofHeap.STACK_TRACE, start, end);

        hprofHeap = heap;
        timeOffset = 1;
        lengthOffset = timeOffset + 4;
        stackTraceSerialNumberOffset = lengthOffset + 4;
        threadSerialNumberOffset = stackTraceSerialNumberOffset + 4;
        numberOfFramesOffset = threadSerialNumberOffset + 4;
        framesListOffset = numberOfFramesOffset + 4;
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    StackTrace getStackTraceBySerialNumber(long stackTraceSerialNumber) {
        Long initialOffset;
        long[] offset;
        
        initSerialNumToFrame();
        initialOffset = (Long) serialNumToStackTrace.get(stackTraceSerialNumber / SERIALNUM_DIV);
        if (initialOffset == null) {
            initialOffset = new Long(startOffset);
        }
        offset = new long[] {initialOffset};
        while (offset[0] < endOffset) {
            long start = offset[0];
            long serialNumber = readStackTraceTag(offset);

            if (serialNumber == stackTraceSerialNumber) {
                return new StackTrace(this, start);
            }
        }
        return null;
    }

    private HprofByteBuffer getDumpBuffer() {
        HprofByteBuffer dumpBuffer = hprofHeap.dumpBuffer;

        return dumpBuffer;
    }

    private int readStackTraceTag(long[] offset) {
        long start = offset[0];

        if (hprofHeap.readTag(offset) != HprofHeap.STACK_TRACE) {
            return 0;
        }
        return getDumpBuffer().getInt(start + stackTraceSerialNumberOffset);
    }

    private synchronized void initSerialNumToFrame() {
        if (serialNumToStackTrace == null) {
            long[] offset = new long[] { startOffset };

            serialNumToStackTrace = new HashMap<>();
            while (offset[0] < endOffset) {
                long start = offset[0];
                long serialNumber = readStackTraceTag(offset);
                Long serialNumberMask = serialNumber/SERIALNUM_DIV;
                Long minOffset = (Long) serialNumToStackTrace.get(serialNumberMask);
                
                if (minOffset == null || minOffset > start) {
                    serialNumToStackTrace.put(serialNumberMask, start);
                }
            }
//            Systems.debug("serialNumToStackTrace size:"+serialNumToStackTrace.size());
        }
    }
}
