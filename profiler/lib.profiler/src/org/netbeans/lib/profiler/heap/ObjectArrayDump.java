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
class ObjectArrayDump extends ArrayDump implements ObjectArrayInstance {
    //~ Constructors -------------------------------------------------------------------------------------------------------------

    ObjectArrayDump(ClassDump cls, long offset) {
        super(cls, offset);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public long getSize() {
        int idSize = dumpClass.getHprofBuffer().getIDSize();

        return dumpClass.classDumpSegment.getMinimumInstanceSize() + HPROF_ARRAY_OVERHEAD + ((long)idSize * getLength());
    }

    public List /*<Instance>*/ getValues() {
        HprofByteBuffer dumpBuffer = dumpClass.getHprofBuffer();
        HprofHeap heap = dumpClass.getHprof();

        return new ObjectArrayLazyList(heap, dumpBuffer, getLength(), getOffset());
    }
    
    public List /*<ArrayItemValue>*/ getItems() {
        return new ObjectArrayValuesLazyList(dumpClass, getLength(), fileOffset);
//        List items = new ArrayList();
//        
//        int length = getLength();
//        for (int i = 0; i < length; i++)
//            items.add(new HprofArrayValue(dumpClass, fileOffset, i));
//        
//        return items;
    }

    long getOffset() {
        int idSize = dumpClass.getHprofBuffer().getIDSize();
        
        return fileOffset + 1 + idSize + 4 + 4 + idSize;
    }
}
