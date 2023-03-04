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

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Tomas Hurka
 */
class TreeObject {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------
    
    private static final int BUFFER_SIZE = (64 * 1024) / 8;
    
    //~ Instance fields ----------------------------------------------------------------------------------------------------------
    
    private HprofHeap heap;
    private LongBuffer readBuffer;
    private LongBuffer writeBuffer;
    private Set<Long> unique;
//private long nextLevelSize;
    
    //~ Constructors -------------------------------------------------------------------------------------------------------------
    
    TreeObject(HprofHeap h, LongBuffer leaves) {
        heap = h;
        writeBuffer = leaves;
    }
    
    //~ Methods ------------------------------------------------------------------------------------------------------------------
    
    
    synchronized void computeTrees() {
        boolean changed;
        try {
            createBuffers();
            do {
                switchBuffers();
                changed = computeOneLevel();
//Systems.debug("Tree obj.   "+heap.idToOffsetMap.treeObj);
//if (changed) Systems.debug("Next level  "+nextLevelSize);
            } while (changed);
        } catch (IOException ex) {
            Systems.printStackTrace(ex);
        }
        deleteBuffers();
//Systems.debug("Done!");
    }
    
    private boolean computeOneLevel() throws IOException {
//nextLevelSize = 0;
        boolean changed = false;
        int idSize = heap.dumpBuffer.getIDSize();
        for (;;) {
            long instanceId = readLong();
            Instance instance;
            List fieldValues;
            Iterator valuesIt;
            long retainedSize = 0;
            
            if (instanceId == 0) {  // end of level
                break;
            }
            instance = heap.getInstanceByID(instanceId);
            if (instance instanceof ObjectArrayInstance) {
                ObjectArrayDump array = (ObjectArrayDump) instance;
                int arrSize = array.getLength();
                long offset = array.getOffset();
                long size = 0;
                LongSet refs = new LongSet();
                
                for  (int i=0; i<arrSize && size != -1; i++) {
                    long refInstanceId = heap.dumpBuffer.getID(offset + (i * idSize));
                    size = checkInstance(instanceId, refInstanceId, refs);
                    retainedSize += size;
                }
                changed |= processInstance(instance, size, retainedSize);
                continue;
            } else if (instance instanceof PrimitiveArrayInstance) {
                assert false:"Error - PrimitiveArrayInstance not allowed "+instance.getJavaClass().getName()+"#"+instance.getInstanceNumber();
                continue;
            } else if (instance instanceof ClassDumpInstance) {
                ClassDump javaClass = ((ClassDumpInstance) instance).classDump;
                
                fieldValues = javaClass.getStaticFieldValues();
            } else if (instance instanceof InstanceDump) {
                fieldValues = instance.getFieldValues();
            } else {
                if (instance == null) {
                    Systems.debug("HeapWalker Warning - null instance for " + instanceId); // NOI18N
                    continue;
                }
                throw new IllegalArgumentException("Illegal type " + instance.getClass()); // NOI18N
            }
            long size = 0;
            LongSet refs = new LongSet();
            valuesIt = fieldValues.iterator();
            while (valuesIt.hasNext() && size != -1) {
                FieldValue val = (FieldValue) valuesIt.next();
                
                if (val instanceof ObjectFieldValue) {
                    Instance refInstance = ((ObjectFieldValue) val).getInstance();
                    size = checkInstance(instanceId, refInstance, refs);
                    retainedSize += size;
                }
            }
            changed |= processInstance(instance, size, retainedSize);
        }
        return changed;
    }
    
    private boolean processInstance(Instance instance, long size, long retainedSize) throws IOException {
        if (size != -1) {
            LongMap.Entry entry = heap.idToOffsetMap.get(instance.getInstanceId());
            entry.setRetainedSize((int)(instance.getSize()+retainedSize));
            entry.setTreeObj();
            if (entry.hasOnlyOneReference()) {
                long gcRootPointer = entry.getNearestGCRootPointer();
                if (gcRootPointer != 0) {
                    if (unique.add(gcRootPointer)) {
                        writeLong(gcRootPointer);
                    }
                }
            }
            return true;
        }
        return false;
    }
    
    private void createBuffers() {
        readBuffer = new LongBuffer(BUFFER_SIZE, heap.cacheDirectory);
    }
    
    private void deleteBuffers() {
        readBuffer.delete();
        writeBuffer.delete();
    }
        
    private long readLong() throws IOException {
        return readBuffer.readLong();
    }
    
    private void switchBuffers() throws IOException {
        LongBuffer b = readBuffer;
        readBuffer = writeBuffer;
        writeBuffer = b;
        readBuffer.startReading();
        writeBuffer.reset();
        unique = new HashSet(4000);
    }
    
    private void writeLong(long instanceId) throws IOException {
        if (instanceId != 0) {
            writeBuffer.writeLong(instanceId);
//nextLevelSize++;
        }
    }
    
    private long checkInstance(long instanceId, Instance refInstance, LongSet refs) throws IOException {
        if (refInstance != null) {
            return checkInstance(instanceId, refInstance.getInstanceId(), refs);
        }
        return 0;
    }
    
    private long checkInstance(long instanceId, long refInstanceId, LongSet refs) throws IOException {
        if (refInstanceId != 0L) {
            LongMap.Entry refEntry = heap.idToOffsetMap.get(refInstanceId);
            
            if (refEntry == null) {
                return 0;
            }
            if (!refEntry.hasOnlyOneReference()) {
                return -1;
            }
            if (!refEntry.isTreeObj()) {
                return -1;
            }
            if (refs.add(refInstanceId)) {
                return 0;
            }
            return refEntry.getRetainedSize();
        }
        return 0;
    }
}
