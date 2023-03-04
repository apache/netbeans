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
class HprofGCRoot extends HprofObject implements GCRoot {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    static Map kindMap;

    static {
        kindMap = new HashMap();
        kindMap.put(Integer.valueOf(HprofHeap.ROOT_UNKNOWN), GCRoot.UNKNOWN);
        kindMap.put(Integer.valueOf(HprofHeap.ROOT_JNI_GLOBAL), GCRoot.JNI_GLOBAL);
        kindMap.put(Integer.valueOf(HprofHeap.ROOT_JNI_LOCAL), GCRoot.JNI_LOCAL);
        kindMap.put(Integer.valueOf(HprofHeap.ROOT_JAVA_FRAME), GCRoot.JAVA_FRAME);
        kindMap.put(Integer.valueOf(HprofHeap.ROOT_NATIVE_STACK), GCRoot.NATIVE_STACK);
        kindMap.put(Integer.valueOf(HprofHeap.ROOT_STICKY_CLASS), GCRoot.STICKY_CLASS);
        kindMap.put(Integer.valueOf(HprofHeap.ROOT_THREAD_BLOCK), GCRoot.THREAD_BLOCK);
        kindMap.put(Integer.valueOf(HprofHeap.ROOT_MONITOR_USED), GCRoot.MONITOR_USED);
        kindMap.put(Integer.valueOf(HprofHeap.ROOT_THREAD_OBJECT), GCRoot.THREAD_OBJECT);
        // HPROF HEAP 1.0.3
        kindMap.put(Integer.valueOf(HprofHeap.ROOT_INTERNED_STRING), GCRoot.INTERNED_STRING);
        kindMap.put(Integer.valueOf(HprofHeap.ROOT_FINALIZING), GCRoot.FINALIZING);
        kindMap.put(Integer.valueOf(HprofHeap.ROOT_DEBUGGER), GCRoot.DEBUGGER);
        kindMap.put(Integer.valueOf(HprofHeap.ROOT_REFERENCE_CLEANUP), GCRoot.REFERENCE_CLEANUP);
        kindMap.put(Integer.valueOf(HprofHeap.ROOT_VM_INTERNAL), GCRoot.VM_INTERNAL);
        kindMap.put(Integer.valueOf(HprofHeap.ROOT_JNI_MONITOR), GCRoot.JNI_MONITOR);
    }

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    HprofGCRoots roots;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    HprofGCRoot(HprofGCRoots r, long offset) {
        super(offset);
        roots = r;
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    public Instance getInstance() {
        return roots.heap.getInstanceByID(getInstanceId());
    }

    public String getKind() {
        int k = getHprofBuffer().get(fileOffset);

        return (String) kindMap.get(Integer.valueOf(k & 0xff));
    }

    long getInstanceId() {
        return getHprofBuffer().getID(fileOffset + 1);
    }
    
    HprofByteBuffer getHprofBuffer() {
        return roots.heap.dumpBuffer;
    }
}
