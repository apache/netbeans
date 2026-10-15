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
package org.openide.util;

import java.lang.ref.SoftReference;

import java.util.Map;


/**
 * A soft reference which is held strongly for a while after last access.
 * Lifecycle:
 * <ol>
 * <li>Created. Referent held strongly. A task is scheduled into the request
 *     processor for some time in the future (currently 30 seconds).</li>
 * <li>Expired. After the timeout, the reference switches to a normal soft
 *     reference.</li>
 * <li>Touched. If the value is accessed before it is garbage collected,
 *     whether the reference is expired or not, the reference is "touched".
 *     This means that the referent is again held strongly and the timeout
 *     is started from scratch.</li>
 * <li>Dead. If after expiry there is no access before the next full GC cycle,
 *     the GC algorithm may reclaim the reference. In this case the reference
 *     of course dies. As a bonus, it will try to remove itself as the value
 *     from a map of your choice, to make it convenient to use these references
 *     as values in a caching map without leaking memory for the key.</li>
 * </ol>
 * @author Jesse Glick
 */
final class TimedSoftReference<T> extends SoftReference<T> implements Runnable {
    private static final int TIMEOUT = 30000;
    private static final RequestProcessor RP = new RequestProcessor("TimedSoftReference"); // NOI18N
    private RequestProcessor.Task task;
    private T o;
    private final Map m;
    private final Object k;

    /** Time when the object was last time touched */
    private long touched;

    /**
     * Create a soft reference with timeout.
     * The supplied map serves double duty as a synchronization lock
     * for the reference's state changes.
     * @param o the referent
     * @param m a map in which this reference may serve as a value
     * @param k the key whose value in <code>m</code> may be this reference
     */
    TimedSoftReference(T o, Map m, Object k) {
        super(o, BaseUtilities.activeReferenceQueue());
        this.o = o;
        this.m = m;
        this.k = k;
        try {
            this.task = RP.create(this);
            this.task.schedule(TIMEOUT);
        } catch (SecurityException ex) {
            // behave as regular SoftReference
            this.o = null;
            this.task = null;
        }
    }

    public void run() {
        synchronized (m) {
            if (o != null) {
                //System.err.println("Expire " + k);
                // how long we've really been idle
                long unused = System.currentTimeMillis() - touched;

                if (unused > (TIMEOUT / 2)) {
                    o = null;
                    touched = 0;
                } else {
                    task.schedule(TIMEOUT - (int) unused);
                }
            } else {
                // clean up map ref, we are dead
                //System.err.println("Die " + k);
                m.remove(k);
            }
        }
    }

    @Override
    public T get() {
        synchronized (m) {
            if (o == null) {
                o = super.get();
            }

            if (o != null) {
                // touch me
                //System.err.println("Touch " + k);
                if (touched == 0) {
                    if (task != null) {
                        task.schedule(TIMEOUT);
                    }
                }

                touched = System.currentTimeMillis();

                return o;
            } else {
                return null;
            }
        }
    }
}
