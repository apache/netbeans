/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.openide.util;

import java.awt.EventQueue;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.openide.util.BaseUtilities;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.Union2;
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
               EventQueue.invokeLater(run);
           }
       }

        /** Methods for access to event queue.
        * @param run runabble to post later
        */
        private static void doEventRequest(Runnable run) {
            EventQueue.invokeLater(run);
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
            try {
                class AWTWorker implements Runnable {
                    @Override
                    public void run() {
                        started.set(true);
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
