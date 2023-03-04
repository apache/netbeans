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

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 *
 * @author Tomas Hurka
 */
class StringSegment extends TagBounds {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private final int UTF8CharsOffset;
    private final int lengthOffset;
    private final int stringIDOffset;
    private final int timeOffset;
    private LongHashMap stringIDMap;
    private HprofHeap hprofHeap;
    private Map stringCache = Collections.synchronizedMap(new StringCache());
    //~ Constructors -------------------------------------------------------------------------------------------------------------

    StringSegment(HprofHeap heap, long start, long end) {
        super(HprofHeap.STRING, start, end);

        int idSize = heap.dumpBuffer.getIDSize();
        hprofHeap = heap;
        timeOffset = 1;
        lengthOffset = timeOffset + 4;
        stringIDOffset = lengthOffset + 4;
        UTF8CharsOffset = stringIDOffset + idSize;
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    String getStringByID(long stringID) {
        Long stringIDObj = stringID;
        String string = (String) stringCache.get(stringIDObj);
        if (string == null) {
            string = createStringByID(stringID);
            stringCache.put(stringIDObj,string);
        }
        return string;
    }
    
    private String createStringByID(long stringID) {
        return getString(getStringOffsetByID(stringID));
    }
    
    private String getString(long start) {
        HprofByteBuffer dumpBuffer = getDumpBuffer();

        if (start == -1) {
            return "<unknown string>"; // NOI18N
        }

        int len = dumpBuffer.getInt(start + lengthOffset);
        byte[] chars = new byte[len - dumpBuffer.getIDSize()];
        dumpBuffer.get(start + UTF8CharsOffset, chars);

        return new String(chars, StandardCharsets.UTF_8);
    }

    private synchronized long getStringOffsetByID(long stringID) {
        if (stringIDMap == null) {
            stringIDMap = new LongHashMap(32768);

            long[] offset = new long[] { startOffset };

            while (offset[0] < endOffset) {
                long start = offset[0];
                long sID = readStringTag(offset);
                if (sID != 0) {
                    stringIDMap.put(sID, start);
                }
            }
        }
        if (stringID == 0) {
            return -1; // string not found
        }
        return stringIDMap.get(stringID);
    }

    private HprofByteBuffer getDumpBuffer() {
        HprofByteBuffer dumpBuffer = hprofHeap.dumpBuffer;

        return dumpBuffer;
    }

    private long readStringTag(long[] offset) {
        long start = offset[0];

        if (hprofHeap.readTag(offset) != HprofHeap.STRING) {
            return 0;
        }

        return getDumpBuffer().getID(start + stringIDOffset);
    }

    private static class StringCache extends LinkedHashMap {
        private static final int SIZE = 1000;
        
        StringCache() {
            super(SIZE,0.75f,true);
        }

        protected boolean removeEldestEntry(Map.Entry eldest) {
            if (size() > SIZE) {
                return true;
            }
            return false;
        }
    }

}
