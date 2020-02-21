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

package org.netbeans.modules.cnd.modelimpl.repository;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.cnd.debug.CndTraceFlags;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.openide.util.WeakSet;

/**
 *
 */
public class KeyManager {

    private final KeyStorage storage;
    private static final int KEY_MANAGER_DEFAULT_CAPACITY;
    private static final int KEY_MANAGER_DEFAULT_SLICED_NUMBER;
    static {
        int nrProc = CndUtils.getConcurrencyLevel();
        if (nrProc <= 4) {
            KEY_MANAGER_DEFAULT_SLICED_NUMBER = 32;
            KEY_MANAGER_DEFAULT_CAPACITY = 512;
        } else {
            KEY_MANAGER_DEFAULT_SLICED_NUMBER = 128;
            KEY_MANAGER_DEFAULT_CAPACITY = 128;
        }
    }
    private static final KeyManager instance = new KeyManager();

    /** Creates a new instance of KeyManager */
    private KeyManager() {
        storage = new KeyStorage(KEY_MANAGER_DEFAULT_SLICED_NUMBER, KEY_MANAGER_DEFAULT_CAPACITY);
    }

    public static KeyManager instance() {
        return instance;
    }
    private static final class Lock {}
    private final Object lock = new Lock();

    /**
     * returns shared uid instance equal to input one.
     *
     * @param uid - interested shared uid
     * @return the shared instance of uid
     * @exception NullPointerException If the <code>uid</code> parameter
     *                                 is <code>null</code>.
     */
    public final Key getSharedKey(Key key) {
        if (key == null) {
            throw new NullPointerException("null string is illegal to share"); // NOI18N
        }
        Key outKey = null;
        synchronized (lock) {
            outKey = storage.getShared(key);
        }
        assert (outKey != null);
        assert (outKey.equals(key));
        return outKey;
    }

    public final void dispose() {
        storage.dispose();
    }

    private static final class KeyStorage {

        private final WeakSet<Key>[] instances;
        private final int segmentMask; // mask
        private final int initialCapacity;

        private KeyStorage(int sliceNumber, int initialCapacity) {
            // Find power-of-two sizes best matching arguments
            int ssize = 1;
            while (ssize < sliceNumber) {
                ssize <<= 1;
            }
            segmentMask = ssize - 1;
            this.initialCapacity = initialCapacity;
            @SuppressWarnings("unchecked")
            WeakSet<Key>[] ar = new WeakSet[ssize];
            for (int i = 0; i < ar.length; i++) {
                ar[i] = new WeakSet<>(initialCapacity);
            }
            instances = ar;
        }

        private WeakSet<Key> getDelegate(Key key) {
            int index = key.hashCode() & segmentMask;
            return instances[index];
        }

        public final Key getShared(Key key) {
            Key out = getDelegate(key).putIfAbsent(key);
            return out;
        }

        public final void dispose() {
            for (int i = 0; i < instances.length; i++) {
                if (instances[i].size() > 0) {
                    if (CndTraceFlags.TRACE_SLICE_DISTIBUTIONS) {
                        Object[] arr = instances[i].toArray();
                        System.out.println("Key cache " + instances[i].size()); // NOI18N
                        Map<Class, Integer> classes = new HashMap<>();
                        for (Object o : arr) {
                            if (o != null) {
                                Integer num = classes.get(o.getClass());
                                if (num != null) {
                                    num = new Integer(num.intValue() + 1);
                                } else {
                                    num = new Integer(1);
                                }
                                classes.put(o.getClass(), num);
                            }
                        }
                        for (Map.Entry<Class, Integer> e : classes.entrySet()) {
                            System.out.println("   " + e.getValue() + " of " + e.getKey().getName()); // NOI18N
                        }
                    }
                    instances[i].clear();
                    instances[i].resize(initialCapacity);
                }
            }
        }
    }
}
