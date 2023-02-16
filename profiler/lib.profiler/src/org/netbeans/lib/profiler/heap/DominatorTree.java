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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author Tomas Hurka
 */
class DominatorTree {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------
    
    private static final int BUFFER_SIZE = (64 * 1024) / 8;
    private static final int ADDITIONAL_IDS_THRESHOLD = 30;
    private static final int ADDITIONAL_IDS_THRESHOLD_DIRTYSET_SAME_SIZE = 5;
    
    //~ Instance fields ----------------------------------------------------------------------------------------------------------
    
    private HprofHeap heap;
    private LongBuffer multipleParents;
    private LongBuffer revertedMultipleParents;
    private LongBuffer currentMultipleParents;
    private LongHashMap map;
    private LongSet dirtySet;
    private int dirtySetSameSize;
    private Map canContainItself;
    private Map nearestGCRootCache = new NearestGCRootCache(400000);

    //~ Constructors -------------------------------------------------------------------------------------------------------------
    
    DominatorTree(HprofHeap h, LongBuffer multiParents) {
        heap = h;
        multipleParents = multiParents;
        currentMultipleParents = multipleParents;
        map = new LongHashMap(multiParents.getSize());
        dirtySet = new LongSet();
        try {
            revertedMultipleParents = multiParents.revertBuffer();
        } catch (IOException ex) {
            throw new IllegalArgumentException(ex.getLocalizedMessage(),ex);
        }
    }
    
    //~ Methods ------------------------------------------------------------------------------------------------------------------    
    
    synchronized void computeDominators() {
        boolean changed = true;
        boolean igonoreDirty;
        try {
            do {
                currentMultipleParents.rewind();
                igonoreDirty = !changed;
                changed = computeOneLevel(igonoreDirty);
                switchParents();
            } while (changed || !igonoreDirty);
        } catch (IOException ex) {
            Systems.printStackTrace(ex);
        }
        deleteBuffers();
        dirtySet = new LongSet();
    }
    
    private boolean computeOneLevel(boolean ignoreDirty) throws IOException {
        boolean changed = false;
        LongSet newDirtySet = new LongSet(map.size()/10);
        List<Long> additionalIds = new ArrayList<>();
        int additionalIndex = 0;
        // debug 
//        long processedId = 0;
//        long changedId = 0;
//        long index = 0;
//        List<Long> changedIds = new ArrayList();
//        List<Long> changedIdx = new ArrayList();
//        List<Boolean> addedBynewDirtySet = new ArrayList();
//        List<Long> oldDomIds = new ArrayList();
//        List<Long> newDomIds = new ArrayList();

//Systems.debug("New level, dirtyset size: "+dirtySet.size());
        for (;;) {
            long instanceId = readLong();
            if (instanceId == 0) {  // end of level
                if (additionalIndex >= additionalIds.size()) {
                    if (additionalIndex>0) {
//Systems.debug("Additional instances "+additionalIndex);
                    }
                    break;
                }
                instanceId = additionalIds.get(additionalIndex++).longValue();
            }
            long oldIdom = map.get(instanceId);
//index++;
            if (oldIdom == -1 || (oldIdom > 0 && (ignoreDirty || dirtySet.contains(oldIdom) || dirtySet.contains(instanceId)))) {            
//processedId++;
                LongMap.Entry entry = heap.idToOffsetMap.get(instanceId);
                LongIterator refIt = entry.getReferences();
                long newIdomId = refIt.next();
                boolean dirty = false;
                
                while(refIt.hasNext() && newIdomId != 0) {
                    long refIdObj = refIt.next();
                    newIdomId = intersect(newIdomId, refIdObj);
                }
                if (oldIdom == -1) {
//addedBynewDirtySet.add(newDirtySet.contains(instanceId) && !dirtySet.contains(instanceId));
                    map.put(instanceId, newIdomId);
                    if (newIdomId != 0) newDirtySet.add(newIdomId);
                    changed = true;
//changedId++;
//changedIds.add(instanceIdObj);
//changedIdx.add(index);
//oldDomIds.add(null);
//newDomIds.add(newIdomIdObj);
                } else if (oldIdom != newIdomId) {
//addedBynewDirtySet.add((newDirtySet.contains(oldIdom) || newDirtySet.contains(instanceId)) && !(dirtySet.contains(oldIdom) || dirtySet.contains(instanceId)));
                    newDirtySet.add(oldIdom);
                    if (newIdomId != 0) newDirtySet.add(newIdomId);
                    map.put(instanceId,newIdomId);
                    if (dirtySet.size() < ADDITIONAL_IDS_THRESHOLD || dirtySetSameSize >= ADDITIONAL_IDS_THRESHOLD_DIRTYSET_SAME_SIZE) {
                        updateAdditionalIds(instanceId, additionalIds);
                    }
                    changed = true;
//changedId++;
//changedIds.add(instanceIdObj);
//changedIdx.add(index);
//oldDomIds.add(oldIdomObj);
//newDomIds.add(newIdomIdObj);
                }
            }
        }
        if (dirtySet.size() != newDirtySet.size()) {
            dirtySetSameSize = 0;
        } else {
            dirtySetSameSize++;
        }
        dirtySet = newDirtySet;
//Systems.debug("Processed: "+processedId);
//Systems.debug("Changed:   "+changedId);
//Systems.debug("-------------------");
//printObjs(changedIds,oldDomIds,newDomIds, addedBynewDirtySet, changedIdx);
//Systems.debug("-------------------");
        return changed;
    }
        
    private void updateAdditionalIds(final long instanceId, final List<Long> additionalIds) {
        Instance i = heap.getInstanceByID(instanceId);
//Systems.debug("Inspecting "+printInstance(instanceIdObj));
        if (i != null) {
            for (Object v : i.getFieldValues()) {
                if (v instanceof ObjectFieldValue) {
                    Instance val = ((ObjectFieldValue)v).getInstance();
                    if (val != null) {
                        long idp = val.getInstanceId();
                        Long idO = idp;
                        long idomO = map.get(idp);
                        if (idomO > 0) {
                            additionalIds.add(idO);
//Systems.debug("  Adding "+printInstance(idO));
                        }
                    }
                }
            }
        }
    }
    
    private void deleteBuffers() {
        multipleParents.delete();
        revertedMultipleParents.delete();
    }
        
    private long readLong() throws IOException {
        return currentMultipleParents.readLong();
    }
    
    long getIdomId(long instanceId, LongMap.Entry entry) {
        long idomEntry = map.get(instanceId);
        if (idomEntry != -1) {
            return idomEntry;
        }
        if (entry == null) {
            entry = heap.idToOffsetMap.get(instanceId);
        }
        return entry.getNearestGCRootPointer();
    }
    
    boolean hasInstanceInChain(int tag, Instance i) {
        ClassDump javaClass;
        long idom;
        long instanceId;
        
        if (tag == HprofHeap.PRIMITIVE_ARRAY_DUMP) {
            return false;
        }        
        javaClass = (ClassDump) i.getJavaClass();
        if (canContainItself == null) {
            canContainItself = new HashMap(heap.getAllClasses().size()/2);
        }
        if (tag == HprofHeap.INSTANCE_DUMP) {
            Boolean canContain = (Boolean) canContainItself.get(javaClass);

            if (canContain == null) {
                canContain = Boolean.valueOf(javaClass.canContainItself());
                canContainItself.put(javaClass,canContain);
            }
            if (!canContain.booleanValue()) {
                return false;
            }
        }
        instanceId = i.getInstanceId();
        idom = getIdomId(instanceId);
        for (;idom!=0;idom=getIdomId(idom)) {
            Instance ip = heap.getInstanceByID(idom);
            JavaClass cls = ip.getJavaClass();
            
            if (javaClass.equals(cls)) {
                return true;
            }
        }
        return false;
    }

    private Long getNearestGCRootPointer(Long instanceIdLong) {
        LongMap.Entry entry;
        Long nearestGCLong = (Long) nearestGCRootCache.get(instanceIdLong);
        Long nearestGC;
        if (nearestGCLong != null) {
            return nearestGCLong;
        }
        entry = heap.idToOffsetMap.get(instanceIdLong.longValue());
        nearestGC = Long.valueOf(entry.getNearestGCRootPointer());
        nearestGCRootCache.put(instanceIdLong,nearestGC);
        return nearestGC;
    }
    
    private long getIdomId(long instanceIdLong) {
        long idom = map.get(instanceIdLong);
        
        if (idom != -1) {
            return idom;
        }
        return getNearestGCRootPointer(instanceIdLong);
    }
    
    private long intersect(long idomId, long refId) {
        if (idomId == refId) {
            return idomId;
        }
        if (idomId == 0 || refId == 0) {
            return 0;
        }
        LongSet leftIdoms = new LongSet(200);
        LongSet rightIdoms = new LongSet(200);        
        long leftIdom = idomId;
        long rightIdom = refId;

        
        leftIdoms.add(leftIdom);
        rightIdoms.add(rightIdom);
        while(true) {
            if (rightIdom == 0 && leftIdom == 0) return 0;
            if (leftIdom != 0) {
                leftIdom = getIdomId(leftIdom);
                if (leftIdom != 0) {
                    if (rightIdoms.contains(leftIdom)) {
                        return leftIdom;
                    }
                    leftIdoms.add(leftIdom);
                }
            }
            if (rightIdom != 0) {
                rightIdom = getIdomId(rightIdom);
                if (rightIdom != 0) {
                    if (leftIdoms.contains(rightIdom)) {
                        return rightIdom;
                    }
                    rightIdoms.add(rightIdom);
                }
            }
        }
    }

    private void switchParents() {
        if (currentMultipleParents == revertedMultipleParents) {
            currentMultipleParents = multipleParents;
        } else {
            currentMultipleParents = revertedMultipleParents;
        }
    }

    // debugging 
    private void printObjs(List<Long> changedIds, List<Long> oldDomIds, List<Long> newDomIds, List<Boolean> addedByDirtySet, List<Long> changedIdx) {
        if (changedIds.size()>20) return;
        TreeMap<Integer,String> m = new TreeMap<>();
        
        for (int i=0; i<changedIds.size(); i++) {
            Long iid = changedIds.get(i);
            Long oldDom = oldDomIds.get(i);
            Long newDom = newDomIds.get(i);
            Long index = changedIdx.get(i);
            Boolean addedByDirt = addedByDirtySet.get(i);
            Instance ii = heap.getInstanceByID(iid.longValue());
            int number = ii.getInstanceNumber();
            String text = "Index: "+index+(addedByDirt?" New ":" Old ")+printInstance(iid);
            
            text+=" OldDom "+printInstance(oldDom);
            text+=" NewDom: "+printInstance(newDom);
            m.put(number,text);
        }
        for (String value : m.values()) {
            Systems.debug(value);
        }
    }
    
    // debugging
    String printInstance(Long instanceid) {
        if (instanceid == null || instanceid.longValue() == 0) {
            return "null";
        }
        Instance ii = heap.getInstanceByID(instanceid.longValue());
        return ii.getJavaClass().getName()+"#"+ii.getInstanceNumber();
        
    }

    //---- Serialization support
    void writeToStream(DataOutputStream out) throws IOException {
        map.writeToStream(out);
    }

    DominatorTree(HprofHeap h, DataInputStream dis) throws IOException {
        heap = h;
        map = new LongHashMap(dis);
    }
    
    private static final class NearestGCRootCache extends LinkedHashMap {
        private final int maxSize;
        
        private NearestGCRootCache(int size) {
            super(size,0.75F,true);
            maxSize = size;
        }

        protected boolean removeEldestEntry(Map.Entry eldest) {
            return size() > maxSize;
        }

    }
}
