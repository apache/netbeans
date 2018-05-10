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
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
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
                gcRoots = computeGCRootsFor(heap.getHeapTagBound(HprofHeap.ROOT_UNKNOWN));
                gcRoots.putAll(computeGCRootsFor(heap.getHeapTagBound(HprofHeap.ROOT_JNI_GLOBAL)));
                gcRoots.putAll(computeGCRootsFor(heap.getHeapTagBound(HprofHeap.ROOT_JNI_LOCAL)));
                gcRoots.putAll(computeGCRootsFor(heap.getHeapTagBound(HprofHeap.ROOT_JAVA_FRAME)));
                gcRoots.putAll(computeGCRootsFor(heap.getHeapTagBound(HprofHeap.ROOT_NATIVE_STACK)));
                gcRoots.putAll(computeGCRootsFor(heap.getHeapTagBound(HprofHeap.ROOT_STICKY_CLASS)));
                gcRoots.putAll(computeGCRootsFor(heap.getHeapTagBound(HprofHeap.ROOT_THREAD_BLOCK)));
                gcRoots.putAll(computeGCRootsFor(heap.getHeapTagBound(HprofHeap.ROOT_MONITOR_USED)));
                gcRoots.putAll(computeGCRootsFor(heap.getHeapTagBound(HprofHeap.ROOT_THREAD_OBJECT)));

                // HPROF HEAP 1.0.3
                gcRoots.putAll(computeGCRootsFor(heap.getHeapTagBound(HprofHeap.ROOT_INTERNED_STRING)));
                gcRoots.putAll(computeGCRootsFor(heap.getHeapTagBound(HprofHeap.ROOT_FINALIZING)));
                gcRoots.putAll(computeGCRootsFor(heap.getHeapTagBound(HprofHeap.ROOT_DEBUGGER)));
                gcRoots.putAll(computeGCRootsFor(heap.getHeapTagBound(HprofHeap.ROOT_REFERENCE_CLEANUP)));
                gcRoots.putAll(computeGCRootsFor(heap.getHeapTagBound(HprofHeap.ROOT_VM_INTERNAL)));
                gcRoots.putAll(computeGCRootsFor(heap.getHeapTagBound(HprofHeap.ROOT_JNI_MONITOR)));

                List rootList = new ArrayList(gcRoots.values());
                Collections.sort(rootList, new Comparator() {
                    public int compare(Object o1, Object o2) {
                        HprofGCRoot r1 = (HprofGCRoot) o1;
                        HprofGCRoot r2 = (HprofGCRoot) o2;
                        int kind = r1.getKind().compareTo(r2.getKind());

                        if (kind != 0) {
                            return kind;
                        }
                        return Long.compare(r1.getInstanceId(), r2.getInstanceId());
                    }
                });
                gcRootsList = Collections.unmodifiableList(rootList);
            }

            return gcRootsList;
        }
    }
    
    GCRoot getGCRoot(Long instanceId) {
        synchronized (gcRootLock) {
            if (gcRoots == null) {
                heap.getGCRoots();
            }

            return (GCRoot) gcRoots.get(instanceId);
        }
    }
    
    ThreadObjectGCRoot getThreadGCRoot(int threadSerialNumber) {
        synchronized (lastThreadObjGCLock) { 
            if (lastThreadObjGC != null && threadSerialNumber == lastThreadObjGC.getThreadSerialNumber()) {
                return lastThreadObjGC;
            }
            
            Iterator gcRootsIt = heap.getGCRoots().iterator();

            while(gcRootsIt.hasNext()) {
                Object gcRoot = gcRootsIt.next();

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
    

    private Map computeGCRootsFor(TagBounds tagBounds) {
        Map roots = new HashMap();

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
                    } else {
                        root = new HprofGCRoot(this, start);
                    }
                    roots.put(Long.valueOf(root.getInstanceId()), root);
                }
            }
        }
        return roots;
    }
}
