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

package org.openide.util.lookup.implspi;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of the active reference queue.
 * @since 8.1
 */
public final class ActiveQueue {
    private ActiveQueue() {}

    private static final Logger LOGGER = Logger.getLogger(ActiveQueue.class.getName());
    private static Impl activeReferenceQueue;

    /**
     * Gets the active reference queue.
     * @return the singleton queue
     */
    public static synchronized ReferenceQueue<Object> queue() {
        if (activeReferenceQueue == null) {
            activeReferenceQueue = new Impl();
            Daemon.ping();
        }
        return activeReferenceQueue;
    }

    private static final class Impl extends ReferenceQueue<Object> {
        
        Impl() {
        }

        @Override
        public Reference<Object> poll() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Reference<? extends Object> remove(long timeout) throws IllegalArgumentException, InterruptedException {
            if (timeout < 0) {
                throw new IllegalArgumentException("Negative timeout value");
            } else if (Thread.currentThread() != Daemon.running) {
                throw new InterruptedException();
            }

            return super.remove(timeout);
        }

        @Override
        public Reference<? extends Object> remove() throws InterruptedException {
            if (Thread.currentThread() != Daemon.running) {
                throw new InterruptedException();
            }

            return super.remove();
        }
    }

    private static final class Daemon extends Thread {
        private static boolean initialized;
        private static Daemon running;
        
        public Daemon() {
            super("Active Reference Queue Daemon");
        }
        
        static synchronized void ping() {
            if (!initialized) {
                try {
                    Daemon t = new Daemon();
                    t.setPriority(Thread.MIN_PRIORITY);
                    t.setDaemon(true);
                    t.start();
                    LOGGER.fine("starting thread");
                    running = t;
                } catch (SecurityException ex) {
                    LOGGER.log(Level.FINE, "cannot start thread", ex);
                } finally {
                    initialized = true;
                }
            }
        }
        
        static synchronized boolean isActive() {
            return running != null;
        }
        
        static synchronized Impl obtainQueue() {
            return activeReferenceQueue;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    Impl impl = obtainQueue();
                    if (impl == null) {
                        return;
                    }
                    Reference<?> ref = impl.remove();
                    LOGGER.log(Level.FINE, "Got dequeued reference {0}", new Object[] { ref });
                    if (!(ref instanceof Runnable)) {
                        LOGGER.log(Level.WARNING, "A reference not implementing runnable has been added to the Utilities.activeReferenceQueue(): {0}", ref.getClass());
                        continue;
                    }
                    // do the cleanup
                    try {
                        ((Runnable) ref).run();
                    } catch (ThreadDeath td) {
                        throw td;
                    } catch (Throwable t) {
                        // Should not happen.
                        // If it happens, it is a bug in client code, notify!
                        LOGGER.log(Level.WARNING, "Cannot process " + ref, t);
                    } finally {
                        // to allow GC
                        ref = null;
                    }
                } catch (InterruptedException ignored) {
                    // Can happen during VM shutdown, it seems.
                }
            }
        }
    }
}
