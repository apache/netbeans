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
package org.openide.explorer.view;

import java.awt.EventQueue;
import java.lang.ref.Reference;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Exceptions;

/**
 * This class checks that an object can be garbage collected, without affecting
 * event queue thread.
 * It's expected that the event queue thread have nothing to do with the GC process.
 * 
 * @author Martin Entlicher
 */
public class EQFriendlyGC {
    
    /**
     * Asserts that the object can be garbage collected.
     * It suspends event queue thread to assure that OutOfMemoryError is not
     * thrown in it instead of the current thread.
     * @param text the text to show when test fails.
     * @param ref the referent to object that should be GCed
     * @see NbTestCase#assertGC(java.lang.String, java.lang.ref.Reference)
     */
    static void assertGC(String text, Reference<?> ref) {
        final Object LOCK = new Object();
        blockEQ(LOCK);
        try {
            NbTestCase.assertGC(text, ref);
        } finally {
            // Resume EQ:
            synchronized (LOCK) {
                LOCK.notifyAll();
            }
        }
    }

    private static void blockEQ(final Object LOCK) {
        final AtomicBoolean eqStarted = new AtomicBoolean(false);
        new Thread() {
            @Override
            public void run() {
                try {
                    EventQueue.invokeAndWait(new Runnable() {
                        @Override
                        public void run() {
                            synchronized (eqStarted) {
                                eqStarted.set(true);
                                eqStarted.notifyAll();
                            }
                            synchronized (LOCK) {
                                try {
                                    LOCK.wait();
                                } catch (InterruptedException ex) {
                                    Exceptions.printStackTrace(ex);
                                }
                            }
                        }
                    });
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (InvocationTargetException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }.start();
        synchronized (eqStarted) {
            while(!eqStarted.get()) {
                try {
                    eqStarted.wait();
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
    }

}
