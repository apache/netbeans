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
package org.netbeans.modules.cnd.repository.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * A LongHashMap that is sliced by several chunks to reduce concurrency
 *
 */
public class SlicedLongHashMap<K> {

    private final LongHashMap<K>[] instances;
    private final int sliceNumber;
    private final int segmentMask; // mask

    @SuppressWarnings({"unchecked", "rawtypes"})
    public SlicedLongHashMap(int sliceNumber, int sliceCapacity) {
        // Find power-of-two sizes best matching arguments
        int ssize = 1;
        while (ssize < sliceNumber) {
            ssize <<= 1;
        }
        segmentMask = ssize - 1;
        this.sliceNumber = ssize;
        instances = new LongHashMap[ssize];
        for (int i = 0; i < sliceNumber; i++) {
            instances[i] = new LongHashMap<K>(sliceCapacity);
        }
    }

    private LongHashMap<K> getDelegate(K key) {
        int index = key.hashCode() & segmentMask;
        return instances[index];

    }

    public long put(K key, long value) {
        return getDelegate(key).put(key, value);
    }

    public long get(K key) {
        return getDelegate(key).get(key);
    }

    public long remove(K key) {
        return getDelegate(key).remove(key);
    }

    public int size() {
        int size = 0;
        for (int i = 0; i < sliceNumber; i++) {
            size += instances[i].size();
        }
        return size;
    }

    public Collection<K> keySet() {
        Collection<K> res = new ArrayList<K>(size());
        for (int i = 0; i < sliceNumber; i++) {
            res.addAll(instances[i].keySet());
        }
        return Collections.<K>unmodifiableCollection(res);
    }

    public Collection<LongHashMap.Entry<K>> entrySet() {
        Collection<LongHashMap.Entry<K>> res = new ArrayList<LongHashMap.Entry<K>>(size());
        for (int i = 0; i < sliceNumber; i++) {
            res.addAll(instances[i].entrySet());
        }
        return Collections.<LongHashMap.Entry<K>>unmodifiableCollection(res);
    }
}
