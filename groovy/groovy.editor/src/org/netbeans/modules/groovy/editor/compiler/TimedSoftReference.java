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
package org.netbeans.modules.groovy.editor.compiler;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.function.Consumer;
import org.openide.util.BaseUtilities;
import org.openide.util.RequestProcessor;

/**
 * Copied from openide.util, slightly adapted: does not take map and key, but remove callback.
 * @author sdedic
 */
final class TimedSoftReference<T> extends SoftReference<T> implements Runnable {
    private static final int TIMEOUT = 30000;
    private static final RequestProcessor RP = new RequestProcessor("TimedSoftReference"); // NOI18N
    private RequestProcessor.Task task;
    private T o;
    private final Consumer<Reference<T>> removeFunc;

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
    TimedSoftReference(T o, Consumer<Reference<T>> removeFunc) {
        super(o, BaseUtilities.activeReferenceQueue());
        this.o = o;
        this.removeFunc = removeFunc;
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
        synchronized (this) {
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
                return;
            }
        }
        removeFunc.accept(this);
    }

    public T get() {
        synchronized (this) {
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

