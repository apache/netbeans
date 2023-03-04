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
class HprofArrayValue extends HprofObject implements ArrayItemValue {
    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    final ClassDump dumpClass;
    final int index;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    HprofArrayValue(ClassDump cls, long offset, int number) {
        // compute HprofArrayValue unique (file) offset.
        // (+1) added to differ this instance with number == 0 from defining instance
        super(offset + number + 1);
        dumpClass = cls;
        index = number;
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public Instance getDefiningInstance() {
        return new ObjectArrayDump(dumpClass, getInstanceArrayOffset());
    }

    public int getIndex() {
        return index;
    }

    public Instance getInstance() {
        HprofHeap heap = dumpClass.getHprof();
        HprofByteBuffer dumpBuffer = heap.dumpBuffer;
        int idSize = dumpBuffer.getIDSize();

        long instanceId = dumpBuffer.getID(getInstanceArrayOffset() + 1 + idSize + 4 + 4 + idSize + ((long)index * (long)idSize));

        return heap.getInstanceByID(instanceId);
    }

    private long getInstanceArrayOffset() {
        // see computation in super() above
        return fileOffset - index - 1;
    }
}
