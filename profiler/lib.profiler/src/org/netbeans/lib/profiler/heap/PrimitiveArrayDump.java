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

import java.util.List;


/**
 *
 * @author Tomas Hurka
 */
class PrimitiveArrayDump extends ArrayDump implements PrimitiveArrayInstance {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    private static final int charSize = 2;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    PrimitiveArrayDump(ClassDump cls, long offset) {
        super(cls, offset);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public long getSize() {
        long elementSize = dumpClass.getHprof().getValueSize(getType());

        return dumpClass.classDumpSegment.getMinimumInstanceSize() + HPROF_ARRAY_OVERHEAD + (elementSize * getLength());
    }

    public List /*<String>*/ getValues() {
        HprofByteBuffer dumpBuffer = dumpClass.getHprofBuffer();
        HprofHeap heap = dumpClass.getHprof();
        byte type = getType();
        long offset = getArrayStartOffset();

        return new PrimitiveArrayLazyList(dumpBuffer, getLength(), offset, heap.getValueSize(type), type);
    }

    char[] getChars(int start, int length) {
        assert getType() == HprofHeap.CHAR;

        char[] chars = new char[length];
        long offset = getArrayStartOffset() + ((long)start * (long)charSize);
        HprofByteBuffer dumpBuffer = dumpClass.getHprofBuffer();

        for (int i = 0; i < length; i++) {
            chars[i] = dumpBuffer.getChar(offset + (i * charSize));
        }

        return chars;
    }

    byte[] getBytes(int start, int length) {
        assert getType() == HprofHeap.BYTE;

        byte[] bytes = new byte[length];
        long offset = getArrayStartOffset() + ((long)start);
        HprofByteBuffer dumpBuffer = dumpClass.getHprofBuffer();

        for (int i = 0; i < length; i++) {
            bytes[i] = dumpBuffer.get(offset+i);
        }

        return bytes;
    }

    private long getArrayStartOffset() {
        int idSize = dumpClass.getHprofBuffer().getIDSize();

        return fileOffset + 1 + idSize + 4 + 4 + 1;
    }

    private byte getType() {
        HprofByteBuffer dumpBuffer = dumpClass.getHprofBuffer();
        int idSize = dumpBuffer.getIDSize();

        return dumpBuffer.get(fileOffset + 1 + idSize + 4 + 4);
    }
}
