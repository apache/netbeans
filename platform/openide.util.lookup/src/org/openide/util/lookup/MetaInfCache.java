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
package org.openide.util.lookup;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

final class MetaInfCache {
    private int knownInstancesCount;
    private final List<Reference<Object>> knownInstances;

    public MetaInfCache(int size) {
        knownInstances = new ArrayList<Reference<Object>>();
        for (int i = 0; i < size; i++) {
            knownInstances.add(null);
        }
    }

    public synchronized Object findInstance(Class<?> c) {
        int size = knownInstances.size();
        int index = hashForClass(c, size);
        for (int i = 0; i < size; i++) {
            Reference<Object> ref = knownInstances.get(index);
            if (ref == null) {
                break;
            }
            Object obj = ref.get();
            if (obj != null) {
                if (c == obj.getClass()) {
                    return obj;
                }
            }
            if (++index == size) {
                index = 0;
            }
        }
        return null;
    }

    public synchronized void storeInstance(Object o) {
        hashPut(o);
        int size = knownInstances.size();
        if (knownInstancesCount > size * 2 / 3) {
            MetaInfServicesLookup.LOGGER.log(Level.CONFIG, "Cache of size {0} is 2/3 full. Rehashing.", size);
            MetaInfCache newCache = new MetaInfCache(size * 2);
            for (Reference<Object> r : knownInstances) {
                if (r == null) {
                    continue;
                }
                Object instance = r.get();
                if (instance == null) {
                    continue;
                }
                newCache.storeInstance(instance);
            }

            this.knownInstances.clear();
            this.knownInstances.addAll(newCache.knownInstances);
            this.knownInstancesCount = newCache.knownInstancesCount;
        }
    }

    private void hashPut(Object o) {
        assert Thread.holdsLock(this);
        Class<?> c = o.getClass();
        int size = knownInstances.size();
        int index = hashForClass(c, size);
        for (int i = 0; i < size; i++) {
            Reference<Object> ref = knownInstances.get(index);
            Object obj = ref == null ? null : ref.get();
            if (obj == null) {
                knownInstances.set(index, new WeakReference<Object>(o));
                knownInstancesCount++;
                break;
            }
            if (++index == size) {
                index = 0;
            }
        }
    }
    
    private static int hashForClass(Class<?> c, int size) {
        return Math.abs(c.hashCode() % size);
    }
}
