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
import java.util.TreeMap;

/**
 *
 * @author Tomas Hurka
 */
class HprofGCRoots {

    final HprofHeap heap;
    private Map<Integer, ThreadObjectHprofGCRoot> threadObjGC;
    private final Object lastThreadObjGCLock = new Object();
    private Map<Long,GCRoot> gcRoots;
    private final Object gcRootLock = new Object();
    private List gcRootsList;

    HprofGCRoots(HprofHeap h) {
        heap = h;
    }

    Collection<GCRoot> getGCRoots() {
        synchronized (gcRootLock) {
            if (gcRoots == null) {
                List<GCRoot> rootList = new ArrayList<>();
                computeGCRootsFor(heap.getHeapTagBound(HprofHeap.ROOT_UNKNOWN), rootList);
                computeGCRootsFor(heap.getHeapTagBound(HprofHeap.ROOT_JNI_GLOBAL), rootList);
                computeGCRootsFor(heap.getHeapTagBound(HprofHeap.ROOT_JNI_LOCAL), rootList);
                computeGCRootsFor(heap.getHeapTagBound(HprofHeap.ROOT_JAVA_FRAME), rootList);
                computeGCRootsFor(heap.getHeapTagBound(HprofHeap.ROOT_NATIVE_STACK), rootList);
                computeGCRootsFor(heap.getHeapTagBound(HprofHeap.ROOT_STICKY_CLASS), rootList);
                computeGCRootsFor(heap.getHeapTagBound(HprofHeap.ROOT_THREAD_BLOCK), rootList);
                computeGCRootsFor(heap.getHeapTagBound(HprofHeap.ROOT_MONITOR_USED), rootList);
                computeGCRootsFor(heap.getHeapTagBound(HprofHeap.ROOT_THREAD_OBJECT), rootList);

                // HPROF HEAP 1.0.3
                computeGCRootsFor(heap.getHeapTagBound(HprofHeap.ROOT_INTERNED_STRING), rootList);
                computeGCRootsFor(heap.getHeapTagBound(HprofHeap.ROOT_FINALIZING), rootList);
                computeGCRootsFor(heap.getHeapTagBound(HprofHeap.ROOT_DEBUGGER), rootList);
                computeGCRootsFor(heap.getHeapTagBound(HprofHeap.ROOT_REFERENCE_CLEANUP), rootList);
                computeGCRootsFor(heap.getHeapTagBound(HprofHeap.ROOT_VM_INTERNAL), rootList);
                computeGCRootsFor(heap.getHeapTagBound(HprofHeap.ROOT_JNI_MONITOR), rootList);

                rootList.sort(new Comparator() {
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
            Map<Long,GCRoot> roots;
            if (gcRoots == null) {
                heap.getGCRoots();
                roots = new HashMap<>();
                for (GCRoot r : getGCRoots()) {
                    roots.put(r.getInstance().getInstanceId(), r);
                }
                gcRoots = roots;
            } else {
                roots = gcRoots;
            }

            return (GCRoot) roots.get(instanceId);
        }
    }

    ThreadObjectGCRoot getThreadGCRoot(int threadSerialNumber) {
        Map<Integer, ThreadObjectHprofGCRoot> map;
        synchronized (lastThreadObjGCLock) {
            if (threadObjGC == null) {
                Iterator gcRootsIt = heap.getGCRoots().iterator();
                map = new TreeMap<>();
                while (gcRootsIt.hasNext()) {
                    Object gcRoot = gcRootsIt.next();

                    if (gcRoot instanceof ThreadObjectHprofGCRoot) {
                        ThreadObjectHprofGCRoot tohGC = (ThreadObjectHprofGCRoot) gcRoot;
                        map.put(tohGC.getThreadSerialNumber(), tohGC);
                    }
                }
                threadObjGC = map;
            } else {
                map = threadObjGC;
            }
        }
        return map.get(threadSerialNumber);
    }

    private void computeGCRootsFor(TagBounds tagBounds, Collection<GCRoot> roots) {
        if (tagBounds != null) {
            int rootTag = tagBounds.tag;
            long[] offset = new long[]{tagBounds.startOffset};

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
                    roots.add(root);
                }
            }
        }
    }
}
