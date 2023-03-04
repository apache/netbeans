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

package org.netbeans.modules.openide.util;

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.openide.util.BaseUtilities;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.Union2;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ServiceProvider;
import org.openide.util.spi.MutexEventProvider;
import org.openide.util.spi.MutexImplementation;

/**
 *
 * @author Tomas Zezula
 */
@ServiceProvider(service = MutexEventProvider.class, position = 100)
public class NbMutexEventProvider implements MutexEventProvider {

    @Override
    public MutexImplementation createMutex() {
        return new Event();
    }

    private static final class Event implements MutexImplementation {

        @Override
        public boolean isReadAccess() {
            return javax.swing.SwingUtilities.isEventDispatchThread();
        }

        @Override
        public boolean isWriteAccess() {
            return javax.swing.SwingUtilities.isEventDispatchThread();
        }

        @Override
        public void writeAccess(Runnable runnable) {
            doEvent(runnable);
        }

        @Override
        public <T> T writeAccess(Mutex.ExceptionAction<T> action) throws MutexException {
            return doEventAccess(action);         
        }

        @Override
        public void readAccess(Runnable runnable) {
            doEvent(runnable);
        }

        @Override
        public <T> T readAccess(Mutex.ExceptionAction<T> action) throws MutexException {
            return doEventAccess(action);
        }

        @Override
        public void postReadRequest(Runnable run) {
            doEventRequest(run);
        }

        @Override
        public void postWriteRequest(Runnable run) {
            doEventRequest(run);
        }

        @Override
        public String toString() {
            return "EVENT - Full JRE"; // NOI18N
        }

        private static void doEvent(Runnable run) {
           if (EventQueue.isDispatchThread()) {
               run.run();
           } else {
               Lookup inherit = Lookup.getDefault();
               EventQueue.invokeLater(() -> {
                   Lookups.executeWith(inherit, run);
               });
           }
       }

        /** Methods for access to event queue.
        * @param run runabble to post later
        */
        private static void doEventRequest(Runnable run) {
            Lookup inherit = Lookup.getDefault();
            EventQueue.invokeLater(() -> {
                Lookups.executeWith(inherit, run);
            });
        }

        /** Methods for access to event queue and waiting for result.
        * @param run runnable to post later
        */
        private static <T> T doEventAccess(final Mutex.ExceptionAction<T> run)
        throws MutexException {
            if (isDispatchThread()) {
                try {
                    return run.run();
                } catch (RuntimeException e) {
                    throw e;
                } catch (Exception e) {
                    throw new MutexException(e);
                }
            }

            final AtomicReference<Union2<T,Throwable>> res = new AtomicReference<Union2<T,Throwable>>();
            final AtomicBoolean started = new AtomicBoolean(); // #210991
            final AtomicBoolean finished = new AtomicBoolean();
            final AtomicBoolean invoked = new AtomicBoolean();
            final Lookup inherit = Lookup.getDefault();
            try {
                class AWTWorker implements Runnable {
                    @Override
                    public void run() {
                        started.set(true);
                        Lookups.executeWith(inherit, () -> {
                            try {
                                res.set(Union2.<T,Throwable>createFirst(run.run()));
                            } catch (Exception e) {
                                res.set(Union2.<T,Throwable>createSecond(e));
                            } catch (LinkageError e) {
                                // #20467
                                res.set(Union2.<T,Throwable>createSecond(e));
                            } catch (StackOverflowError e) {
                                // #20467
                                res.set(Union2.<T,Throwable>createSecond(e));
                            }
                        });
                        finished.set(true);
                    }
                }

                AWTWorker w = new AWTWorker();
                EventQueue.invokeAndWait(w);
                invoked.set(true);
            } catch (InterruptedException e) {
                res.set(Union2.<T,Throwable>createSecond(e));
            } catch (InvocationTargetException e) {
                res.set(Union2.<T,Throwable>createSecond(e));
            }

            Union2<T,Throwable> _res = res.get();
            if (_res == null) {
                throw new IllegalStateException("#210991: got neither a result nor an exception; started=" + started + " finished=" + finished + " invoked=" + invoked);
            } else if (_res.hasFirst()) {
                return _res.first();
            } else {
                Throwable e = _res.second();
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                } else {
                    throw notifyException(e);
                }
            }
        }

        /** @return true iff current thread is EventDispatchThread */
        private static boolean isDispatchThread() {
            boolean dispatch = EventQueue.isDispatchThread();
            if (!dispatch && (BaseUtilities.getOperatingSystem() == BaseUtilities.OS_SOLARIS)) {
                // on solaris the event queue is not always recognized correctly
                // => try to guess by name
                dispatch = (Thread.currentThread().getClass().getName().indexOf("EventDispatchThread") >= 0); // NOI18N
            }
            return dispatch;
        }

        /** Notify exception and returns new MutexException */
        private static MutexException notifyException(Throwable t) {
            if (t instanceof InvocationTargetException) {
                t = unfoldInvocationTargetException((InvocationTargetException) t);
            }

            if (t instanceof Error) {
                annotateEventStack(t);
                throw (Error) t;
            }

            if (t instanceof RuntimeException) {
                annotateEventStack(t);
                throw (RuntimeException) t;
            }

            MutexException exc = new MutexException((Exception) t);
            exc.initCause(t);

            return exc;
        }

        private static void annotateEventStack(Throwable t) {
            //ErrorManager.getDefault().annotate(t, new Exception("Caught here in mutex")); // NOI18N
        }

        private static Throwable unfoldInvocationTargetException(InvocationTargetException e) {
            Throwable ret;

            do {
                ret = e.getTargetException();

                if (ret instanceof InvocationTargetException) {
                    e = (InvocationTargetException) ret;
                } else {
                    e = null;
                }
            } while (e != null);

            return ret;
        }
    }
}
