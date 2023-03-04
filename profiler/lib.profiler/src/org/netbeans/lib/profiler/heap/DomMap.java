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

import java.io.FileNotFoundException;
import java.io.IOException;


/**
 * key - ID (long/int) of heap object
 * value (8/4) + 4 + 1 + (8/4)
 *  - offset (long/int) to dump file
 *  - instance index (int) - unique number of this {@link Instance} among all instances of the same Java Class
 *  - references flags (byte) - bit 0 set - has zero or one reference, 
 *                            - bit 1 set - has GC root
 *                            - bit 2 set - tree object
 *  - ID/offset (long/int) - ID if reference flag bit 0 is set, otherwise offset to reference list file
 *  - retained size
 *
 * @author Tomas Hurka
 */
class DomMap extends AbstractLongMap {


    //~ Inner Classes ------------------------------------------------------------------------------------------------------------

    class Entry extends AbstractLongMap.Entry {
        
        //~ Instance fields ------------------------------------------------------------------------------------------------------

        private long offset;

        //~ Constructors ---------------------------------------------------------------------------------------------------------

        private Entry(long off) {
            offset = off;
        }
        
        private Entry(long off,long value) {
            offset = off;
            setIdom(value);
        }
        //~ Methods --------------------------------------------------------------------------------------------------------------

        void setIdom(long instanceId) {
            putID(offset + KEY_SIZE, instanceId);
        }

        long getIdom() {
            return getID(offset + KEY_SIZE);
        }
    }

    
    //~ Constructors -------------------------------------------------------------------------------------------------------------

    DomMap(int size,int idSize,int foffsetSize, CacheDirectory cacheDir) throws FileNotFoundException, IOException {
        super(size,idSize,foffsetSize,idSize,cacheDir);
    }

    //~ Methods ------------------------------------------------------------------------------------------------------------------

    Entry createEntry(long index) {
        return new Entry(index);
    }

    Entry createEntry(long index, long value) {
        return new Entry(index,value);
    }
    
    Entry get(long key) {
        return (Entry)super.get(key);
    }

    Entry put(long key, long value) {
        return (Entry)super.put(key,value);
    }


}
