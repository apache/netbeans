/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.lib.profiler.heap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Tomas Hurka
 */
class HprofGCRoots {

    final HprofHeap heap;
    private ThreadObjectHprofGCRoot lastThreadObjGC;
    final private Object lastThreadObjGCLock = new Object();
    private Map gcRoots;
    final private Object gcRootLock = new Object();
    private List gcRootsList;

    HprofGCRoots(HprofHeap h) {
        heap = h;
    }
    
    Collection getGCRoots() {
        synchronized (gcRootLock) {
            if (gcRoots == null) {
                gcRoots = new HashMap(16384);
                gcRootsList = new ArrayList(16384);
                computeGCRootsFor(heap.getHeapTagBound(HprofHeap.ROOT_UNKNOWN));
                computeGCRootsFor(heap.getHeapTagBound(HprofHeap.ROOT_JNI_GLOBAL));
                computeGCRootsFor(heap.getHeapTagBound(HprofHeap.ROOT_JNI_LOCAL));
                computeGCRootsFor(heap.getHeapTagBound(HprofHeap.ROOT_JAVA_FRAME));
                computeGCRootsFor(heap.getHeapTagBound(HprofHeap.ROOT_NATIVE_STACK));
                computeGCRootsFor(heap.getHeapTagBound(HprofHeap.ROOT_STICKY_CLASS));
                computeGCRootsFor(heap.getHeapTagBound(HprofHeap.ROOT_THREAD_BLOCK));
                computeGCRootsFor(heap.getHeapTagBound(HprofHeap.ROOT_MONITOR_USED));
                computeGCRootsFor(heap.getHeapTagBound(HprofHeap.ROOT_THREAD_OBJECT));

                // HPROF HEAP 1.0.3
                computeGCRootsFor(heap.getHeapTagBound(HprofHeap.ROOT_INTERNED_STRING));
                computeGCRootsFor(heap.getHeapTagBound(HprofHeap.ROOT_FINALIZING));
                computeGCRootsFor(heap.getHeapTagBound(HprofHeap.ROOT_DEBUGGER));
                computeGCRootsFor(heap.getHeapTagBound(HprofHeap.ROOT_REFERENCE_CLEANUP));
                computeGCRootsFor(heap.getHeapTagBound(HprofHeap.ROOT_VM_INTERNAL));
                computeGCRootsFor(heap.getHeapTagBound(HprofHeap.ROOT_JNI_MONITOR));

                gcRootsList = Collections.unmodifiableList(gcRootsList);
            }

            return gcRootsList;
        }
    }
    
    Object getGCRoots(Long instanceId) {
        synchronized (gcRootLock) {
            if (gcRoots == null) {
                heap.getGCRoots();
            }

            return gcRoots.get(instanceId);
        }
    }
    
    ThreadObjectGCRoot getThreadGCRoot(int threadSerialNumber) {
        synchronized (lastThreadObjGCLock) { 
            if (lastThreadObjGC != null && threadSerialNumber == lastThreadObjGC.getThreadSerialNumber()) {
                return lastThreadObjGC;
            }
            
            for (GCRoot gcRoot : heap.getGCRoots()) {
                if (gcRoot instanceof ThreadObjectHprofGCRoot) {
                    ThreadObjectHprofGCRoot threadObjGC = (ThreadObjectHprofGCRoot) gcRoot;
                    if (threadSerialNumber == threadObjGC.getThreadSerialNumber()) {
                        lastThreadObjGC = threadObjGC;
                        return threadObjGC;
                    }
                }
            }
            return null;
        }
    }
    

    private void computeGCRootsFor(TagBounds tagBounds) {
        if (tagBounds != null) {
            int rootTag = tagBounds.tag;
            long[] offset = new long[] { tagBounds.startOffset };

            while (offset[0] < tagBounds.endOffset) {
                long start = offset[0];

                if (heap.readDumpTag(offset) == rootTag) {
                    HprofGCRoot root;
                    if (rootTag == HprofHeap.ROOT_THREAD_OBJECT) {
                        root = new ThreadObjectHprofGCRoot(this, start);                        
                    } else if (rootTag == HprofHeap.ROOT_JAVA_FRAME) {
                        root = new JavaFrameHprofGCRoot(this, start);
                    } else if (rootTag == HprofHeap.ROOT_JNI_LOCAL) {
                        root = new JniLocalHprofGCRoot(this, start);
                    } else {
                        root = new HprofGCRoot(this, start);
                    }
                    Long objectId = Long.valueOf(root.getInstanceId());
                    Object val = gcRoots.get(objectId);
                    if (val == null) {
                        gcRoots.put(objectId, root);
                    } else if (val instanceof GCRoot) {
                        Collection vals = new ArrayList(2);
                        vals.add(val);
                        vals.add(root);
                        gcRoots.put(objectId, vals);
                    } else {
                        ((Collection)val).add(root);
                    }
                    gcRootsList.add(root);
                }
            }
        }
    }
}
