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

/**
 *
 * @author Tomas Hurka
 */
class LoadClassSegment extends TagBounds {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    HprofHeap hprofHeap;
    final int classIDOffset;
    final int classSerialNumberOffset;
    final int lengthOffset;
    final int nameStringIDOffset;
    final int stackTraceSerialOffset;
    final int timeOffset;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    LoadClassSegment(HprofHeap heap, long start, long end) {
        super(HprofHeap.LOAD_CLASS, start, end);

        int idSize = heap.dumpBuffer.getIDSize();
        hprofHeap = heap;
        timeOffset = 1;
        lengthOffset = timeOffset + 4;
        classSerialNumberOffset = lengthOffset + 4;
        classIDOffset = classSerialNumberOffset + 4;
        stackTraceSerialOffset = classIDOffset + idSize;
        nameStringIDOffset = stackTraceSerialOffset + 4;
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    LoadClass getClassByID(long classObjectID) {
        long[] offset = new long[] { startOffset };

        while (offset[0] < endOffset) {
            long start = offset[0];
            long classID = readLoadClassID(offset);

            if (classID == classObjectID) {
                return new LoadClass(this, start);
            }
        }

        return null;
    }

    LoadClass getClassBySerialNumber(int classSerialNumber) {
        long[] offset = new long[] { startOffset };

        while (offset[0] < endOffset) {
            long start = offset[0];
            int serial = readLoadClassSerialNumber(offset);

            if (serial == classSerialNumber) {
                return new LoadClass(this, start);
            }
        }

        return null;
    }
    
    void setLoadClassOffsets() {
        ClassDumpSegment classDumpSegment = hprofHeap.getClassDumpSegment();
        long[] offset = new long[] { startOffset };

        while (offset[0] < endOffset) {
            long start = offset[0];
            long classID = readLoadClassID(offset);
            ClassDump classDump = classDumpSegment.getClassDumpByID(classID);

            if (classDump != null) {
                classDump.setClassLoadOffset(start);
            }
        }
    }

    private HprofByteBuffer getDumpBuffer() {
        HprofByteBuffer dumpBuffer = hprofHeap.dumpBuffer;

        return dumpBuffer;
    }

    private int readLoadClassSerialNumber(long[] offset) {
        long start = offset[0];

        if (hprofHeap.readTag(offset) != HprofHeap.LOAD_CLASS) {
            return 0;
        }

        return getDumpBuffer().getInt(start + classSerialNumberOffset);
    }
    
    private long readLoadClassID(long[] offset) {
        long start = offset[0];

        if (hprofHeap.readTag(offset) != HprofHeap.LOAD_CLASS) {
            return 0;
        }

        return getDumpBuffer().getID(start + classIDOffset);
    }
}
