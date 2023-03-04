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

package org.netbeans.modules.projectapi.nb;

// XXX COPIED from org.openide.util w/ changes:
//     weak -> soft
//     timeout
//     removed map key functionality

import java.lang.ref.WeakReference;
import org.openide.util.BaseUtilities;
import org.openide.util.RequestProcessor;

/**
 * A weak reference which is held strongly for a while after last access.
 * Lifecycle:
 * <ol>
 * <li>Created. Referent held strongly. A task is scheduled into the request
 *     processor for some time in the future (currently 15 seconds).</li>
 * <li>Expired. After the timeout, the reference switches to a normal weak
 *     reference.</li>
 * <li>Touched. If the value is accessed before it is garbage collected,
 *     whether the reference is expired or not, the reference is "touched".
 *     This means that the referent is again held strongly and the timeout
 *     is started from scratch.</li>
 * <li>Dead. If after expiry there is no access before the next full GC cycle,
 *     the GC algorithm may reclaim the reference. In this case the reference
 *     of course dies.</li>
 * </ol>
 * @author Jesse Glick
 */
public final class TimedWeakReference<T> extends WeakReference<T> implements Runnable {
    
    public static int TIMEOUT = 15000;
    
    private static final RequestProcessor RP = new RequestProcessor("TimedWeakReference"); // NOI18N
    
    private RequestProcessor.Task task;
    
    private T o;
    
    /** Time when the object was last time touched */
    private long touched;
    
    /**
     * Create a weak reference with timeout.
     * @param o the referent
     */
    public TimedWeakReference(T o) {
        super(o, BaseUtilities.activeReferenceQueue());
        this.o = o;
        task = RP.create(this);
        task.schedule(TIMEOUT);
    }
    
    public synchronized void run() {
        if (o != null) {
            //System.err.println("Expire " + k);
            // how long we've really been idle
            long unused  = System.currentTimeMillis() - touched;
            if (unused > TIMEOUT / 2) {
                o = null;
                touched = 0;
            } else {
                task.schedule(TIMEOUT - (int) unused);
            }
        }
    }
    
    public synchronized T get() {
        if (o == null) {
            o = super.get();
        }
        if (o != null) {
            // touch me
            //System.err.println("Touch " + k);
            if (touched == 0) {
                task.schedule(TIMEOUT);
            } 
            touched = System.currentTimeMillis();
            return o;
        } else {
            return null;
        }
    }
    
}
