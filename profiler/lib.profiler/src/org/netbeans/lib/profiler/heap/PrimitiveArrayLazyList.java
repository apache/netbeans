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

import java.util.AbstractList;


/**
 *
 * @author Tomas Hurka
 */
class PrimitiveArrayLazyList extends AbstractList {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private final HprofByteBuffer dumpBuffer;
    private final byte type;
    private final int elSize;
    private final int length;
    private final long offset;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    PrimitiveArrayLazyList(HprofByteBuffer buf, int len, long off, int els, byte t) {
        dumpBuffer = buf;
        length = len;
        offset = off;
        elSize = els;
        type = t;
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public Object get(int index) {
        return HprofInstanceValue.getTypeValue(dumpBuffer, offset + ((long)index * (long)elSize), type).toString();
    }

    public int size() {
        return length;
    }
}
