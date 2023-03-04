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

package org.netbeans.modules.profiler.heapwalk.details.api;

import java.util.List;
import org.netbeans.lib.profiler.heap.Heap;
import org.netbeans.lib.profiler.heap.JavaClass;

/**
 *
 * @author Tomas Hurka
 */
public final class StringDecoder {
    
    private final byte coder;
    private final List<String> values;
    private int HI_BYTE_SHIFT;
    private int LO_BYTE_SHIFT;

    public StringDecoder(Heap heap, byte c, List<String> val) {
        coder = c;
        values = val;
        if (coder == 1) {
            JavaClass utf16Class = heap.getJavaClassByName("java.lang.StringUTF16"); // NOI18N
            HI_BYTE_SHIFT = (Integer) utf16Class.getValueOfStaticField("HI_BYTE_SHIFT"); // NOI18N
            LO_BYTE_SHIFT = (Integer) utf16Class.getValueOfStaticField("LO_BYTE_SHIFT"); // NOI18N
        }
    }

    public int getStringLength() {
        int size = values.size();
        switch (coder) {
            case -1:
                return size;
            case 0:
                return size;
            case 1:
                return size / 2;
            default:
                return size;
        }
    }

    public String getValueAt(int index) {
        switch (coder) {
            case -1:
                return values.get(index);
            case 0: {
                char ch = (char) (Byte.valueOf(values.get(index)) & 0xff);
                return String.valueOf(ch);
            }
            case 1: {
                index *= 2;
                byte hiByte = Byte.valueOf(values.get(index));
                byte lowByte = Byte.valueOf(values.get(index + 1));
                char ch = (char) (((hiByte & 0xff) << HI_BYTE_SHIFT) |
                                 ((lowByte & 0xff) << LO_BYTE_SHIFT));
                return String.valueOf(ch);
            }
            default:
                return "?"; // NOI18N
        }
    }  
}
