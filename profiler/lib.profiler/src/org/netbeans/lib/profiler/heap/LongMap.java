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

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * key - ID (long/int) of heap object
 * value (8/4) + 4 + 1 + (8/4)
 *  - offset (long/int) to dump file
 *  - instance index (int) - unique number of this {@link Instance} among all instances of the same Java Class
 *  - references flags (byte) - bit 0 set - has zero or one reference, 
 *                            - bit 1 set - has GC root
 *                            - bit 2 set - tree object
 *  - ID/offset (long/int) - ID if reference flag bit 0 is set, otherwise offset to reference list file
 *  - retained size (long/int) 
 *
 * @author Tomas Hurka
 */
class LongMap extends AbstractLongMap {

    private NumberList referenceList;

    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    class Entry extends AbstractLongMap.Entry {
        
        private static final byte NUMBER_LIST = 1;
        private static final byte GC_ROOT = 2;
        private static final byte TREE_OBJ = 4;
        
        //~ Instance fields ------------------------------------------------------------------------------------------------------

        private long offset;

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        private Entry(long off) {
            offset = off;
        }

        private Entry(long off,long value) {
            offset = off;
            putFoffset(offset + KEY_SIZE, value);
        }

        //~ Methods --------------------------------------------------------------------------------------------------------------

        void setIndex(int index) {
            dumpBuffer.putInt(offset + KEY_SIZE + FOFFSET_SIZE, index);
        }

        int getIndex() {
            return dumpBuffer.getInt(offset + KEY_SIZE + FOFFSET_SIZE);
        }

        void setTreeObj() {
            byte flags = (byte)(getFlags() | TREE_OBJ);
            setFlags(flags);
        }
        
        boolean isTreeObj() {
            return (getFlags() & TREE_OBJ) != 0;
        }

        boolean hasOnlyOneReference() {
            return (getFlags() & NUMBER_LIST) == 0;
        }
        
        void setNearestGCRootPointer(long instanceId) {
            byte flags = (byte)(getFlags() | GC_ROOT);
            setFlags(flags);
            if ((flags & NUMBER_LIST) != 0) {   // put GC root pointer on the first place in references list
                try {
                    referenceList.putFirst(getReferencesPointer(),instanceId);
                } catch (IOException ex) {
                    Systems.printStackTrace(ex);
                }
            }
        }

        long getNearestGCRootPointer() {
            try {
                byte flag = getFlags();
                if ((flag & GC_ROOT) != 0) { // has GC root pointer
                    long ref = getReferencesPointer();
                    if ((flag & NUMBER_LIST) != 0) { // get GC root pointer from number list
                        return referenceList.getFirstNumber(ref);
                    }
                    return ref;
                }
            } catch (IOException ex) {
                Systems.printStackTrace(ex);
            }
            return 0L;
        }
        
        void addReference(long instanceId) {
            try {
                byte flags = getFlags();
                long ref = getReferencesPointer();
                if ((flags & NUMBER_LIST) == 0) { // reference list is not used
                    if (ref == 0L) {    // no reference was set
                        setReferencesPointer(instanceId);
                    } else if (ref != instanceId) {    // one reference was set, switch to reference list
                       setFlags((byte)(flags | NUMBER_LIST));
                       long list = referenceList.addFirstNumber(ref,instanceId);
                       setReferencesPointer(list);
                    }
                } else { // use reference list
                    long newRef = referenceList.addNumber(ref,instanceId);
                    if (newRef != ref) {
                        setReferencesPointer(newRef);
                    }
                }
            } catch (IOException ex) {
                Systems.printStackTrace(ex);
            }
        }
        
        LongIterator getReferences() {
            byte flags = getFlags();
            long ref = getReferencesPointer();
            if ((flags & NUMBER_LIST) == 0) {
                if (ref == 0L) {
                    return LongIterator.EMPTY_ITERATOR;
                } else {
                    return LongIterator.singleton(ref);
                }
            } else {
                try {
                    return referenceList.getNumbersIterator(ref);
                } catch (IOException ex) {
                    Systems.printStackTrace(ex);
                }
            }
            return LongIterator.EMPTY_ITERATOR;
        }
        
        long getOffset() {
            return getFoffset(offset + KEY_SIZE);
        }

        void setRetainedSize(long size) {
            if (FOFFSET_SIZE == 4) {
                dumpBuffer.putInt(offset + KEY_SIZE + FOFFSET_SIZE + 4 + 1 + ID_SIZE, (int)size);
            } else {
                dumpBuffer.putLong(offset + KEY_SIZE + FOFFSET_SIZE + 4 + 1 + ID_SIZE, size);
            }
        }

        long getRetainedSize() {
            if (FOFFSET_SIZE == 4) {
                return dumpBuffer.getInt(offset + KEY_SIZE + FOFFSET_SIZE + 4 + 1 + ID_SIZE);
            }
            return dumpBuffer.getLong(offset + KEY_SIZE + FOFFSET_SIZE + 4 + 1 + ID_SIZE);            
        }

        private void setReferencesPointer(long instanceId) {
            putID(offset + KEY_SIZE + FOFFSET_SIZE + 4 + 1, instanceId);
        }

        private long getReferencesPointer() {
            return getID(offset + KEY_SIZE + FOFFSET_SIZE + 4 + 1);
        }

        private void setFlags(byte flags) {
            dumpBuffer.putByte(offset + KEY_SIZE + FOFFSET_SIZE + 4, flags);
        }

        private byte getFlags() {
            return dumpBuffer.getByte(offset + KEY_SIZE + FOFFSET_SIZE + 4);
        }
    }

    private static class RetainedSizeEntry implements Comparable {
        private final long instanceId;
        private final long retainedSize;
        
        private RetainedSizeEntry(long id,long size) {
            instanceId = id;
            retainedSize = size;
        }

        public int compareTo(Object o) {
            RetainedSizeEntry other = (RetainedSizeEntry) o;
            // bigger object are at beginning
            int diff = Long.compare(other.retainedSize, retainedSize);
            if (diff == 0) {
                // sizes are the same, compare ids
                return Long.compare(instanceId, other.instanceId);
            }
            return diff;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final RetainedSizeEntry other = (RetainedSizeEntry) obj;
            return this.instanceId == other.instanceId;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 31 * hash + (int) (this.instanceId ^ (this.instanceId >>> 32));
            return hash;
        }
    }
    
    //~ Constructors -------------------------------------------------------------------------------------------------------------

    LongMap(int size,int idSize,int foffsetSize,CacheDirectory cacheDir) throws FileNotFoundException, IOException {
        super(size,idSize,foffsetSize,foffsetSize + 4 + 1 + idSize + foffsetSize, cacheDir);
        referenceList = new NumberList(ID_SIZE, cacheDir);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    Entry createEntry(long index) {
        return new Entry(index);
    }
    
    Entry createEntry(long index,long value) {
        return new Entry(index,value);
    }
    
    Entry get(long key) {
        return (Entry)super.get(key);
    }

    Entry put(long key, long value) {
        return (Entry)super.put(key,value);
    }

    void flush() {
        referenceList.flush();
    }

    long[] getBiggestObjectsByRetainedSize(int number) {
        SortedSet bigObjects = new TreeSet();
        long[] bigIds = new long[number];
        long min = 0;
        for (long index=0;index<fileSize;index+=ENTRY_SIZE) {
            long id = getID(index);
            if (id != 0) {
                long retainedSize = createEntry(index).getRetainedSize();
                if (bigObjects.size()<number) {
                    bigObjects.add(new RetainedSizeEntry(id,retainedSize));
                    min = ((RetainedSizeEntry)bigObjects.last()).retainedSize;
                } else if (retainedSize>min) {
                    bigObjects.remove(bigObjects.last());
                    bigObjects.add(new RetainedSizeEntry(id,retainedSize));
                    min = ((RetainedSizeEntry)bigObjects.last()).retainedSize;
                }
            }
        }
        int i = 0;
        Iterator it = bigObjects.iterator();
        while(it.hasNext()) {
            bigIds[i++]=((RetainedSizeEntry)it.next()).instanceId;
        }
        return bigIds;
    }

    //---- Serialization support    
    void writeToStream(DataOutputStream out) throws IOException {
        super.writeToStream(out);
        referenceList.writeToStream(out);
    }
    
    LongMap(DataInputStream dis, CacheDirectory cacheDir) throws IOException {
        super(dis, cacheDir);
        referenceList = new NumberList(dis, cacheDir);
    }
}
