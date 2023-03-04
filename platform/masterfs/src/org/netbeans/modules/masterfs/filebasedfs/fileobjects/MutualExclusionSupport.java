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

package org.netbeans.modules.masterfs.filebasedfs.fileobjects;


import org.openide.util.WeakSet;

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.masterfs.filebasedfs.utils.FSException;

public final class MutualExclusionSupport<K> {

    private static final int TRIES = Integer.getInteger(               //#229903
            "org.netbeans.modules.masterfs.mutualexclusion.tries", 10); //NOI18N

    private final Map<K,Set<Closeable>> exclusive = Collections.synchronizedMap(new WeakHashMap<K,Set<Closeable>>());
    private final Map<K,Set<Closeable>> shared = Collections.synchronizedMap(new WeakHashMap<K,Set<Closeable>>());

    public MutualExclusionSupport() {
    }

    public synchronized Closeable addResource(final K key, final boolean isShared) throws IOException {
        boolean isInUse = true;        
        final Map<K,Set<Closeable>> unexpected = (isShared) ? exclusive : shared;
        final Map<K,Set<Closeable>> expected = (isShared) ? shared : exclusive;

        final Set<Closeable> unexpectedCounter = unexpected.get(key);
        Set<Closeable> expectedCounter = expected.get(key);

        for (int i = 0; i < TRIES && isInUse; i++) {
            isInUse = unexpectedCounter != null && unexpectedCounter.size() > 0;

            if (!isInUse) {            
                if (expectedCounter == null) {
                    expectedCounter = new WeakSet<Closeable>();
                    expected.put(key, expectedCounter);
                }
                isInUse = !isShared && expectedCounter.size() > 0;            
            }
            
            if (isInUse) {
                try {
                    wait(200);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }

        if (isInUse) {
            try {
                FSException.io(isShared ? "EXC_CannotGetSharedAccess" : "EXC_CannotGetExclusiveAccess", key.toString()); // NOI18N
            } catch (IOException x) {
                assert addStack(x, unexpectedCounter, expectedCounter);
                throw x;
            }
        }


        final Closeable retVal = new Closeable(key, isShared);
        expectedCounter.add(retVal);
        return retVal;
    }
    private boolean addStack(IOException x, Set<Closeable> unexpectedCounter, Set<Closeable> expectedCounter) {
        try {
            addStack(x, unexpectedCounter);
            addStack(x, expectedCounter);
        } catch (IllegalArgumentException e) { // #233546
            Logger log = Logger.getLogger(MutualExclusionSupport.class.getName());
            String unexpectedStr = unexpectedCounter == null
                    ? "null" //NOI18N
                    : Arrays.toString(unexpectedCounter.toArray());
            String expectedStr = expectedCounter == null
                    ? "null" //NOI18N
                    : Arrays.toString(expectedCounter.toArray());
            log.log(Level.WARNING, "Cannot add stack to exception: " //NOI18N
                    + "unexpectedCounter: {0}, expectedCounter: {1}", //NOI18N
                    new Object[]{unexpectedStr, expectedStr});
            log.log(Level.INFO, null, e);
            log.log(Level.INFO, "Exception x", x); //NOI18N
        }
        return true;
    }
    private void addStack(IOException x, Set<Closeable> cs) {
        if (cs != null) {
            for (Closeable c : cs) {
                Throwable stack = c.stack;
                if (stack != null) {
                    Throwable t = x;
                    while (t.getCause() != null) {
                        t = t.getCause();
                    }
                    t.initCause(stack);
                }
            }
        }
    }

    private synchronized void removeResource(final K key, final Closeable value, final boolean isShared) {
        final Map<K,Set<Closeable>> expected = isShared ? shared : exclusive;

        final Set<Closeable> expectedCounter = expected.get(key);
        if (expectedCounter != null) {
            expectedCounter.remove(value);
        }
    }

    synchronized boolean isBeingWritten(K file) {
        final Set<Closeable> counter = exclusive.get(file);
        return counter != null && !counter.isEmpty();
    }


    public final class Closeable {
        private final boolean isShared;
        private final Reference<K> keyRef;
        private boolean isClosed = false;
        Throwable stack;

        private Closeable(final K key, final boolean isShared) {
            this.isShared = isShared;
            this.keyRef = new WeakReference<K>(key);
            assert populateStack();
        }

        private boolean populateStack() {
            stack = new Throwable("opened stream here");
            return true;
        }

        public void close() {
            if (!isClosed()) {
                isClosed = true;
                final K key = keyRef.get();
                if (key != null) {
                    removeResource(key, this, isShared);
                }
            }
        }

        public boolean isClosed() {
            return isClosed;
        }
    }
}
