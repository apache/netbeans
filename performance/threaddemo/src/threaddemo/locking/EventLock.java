/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package threaddemo.locking;

import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.InvocationEvent;
import java.awt.event.PaintEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.Utilities;

// XXX could track read vs. write state

/**
 * Lock impl that works in the event thread.
 * @author Jesse Glick
 */
final class EventLock implements RWLock {

    public final static RWLock DEFAULT = new EventLock();

    private EventLock() {}

    public <T> T read(final LockAction<T> action) {
        if (isDispatchThread()) {
            return action.run();
        } else {
            final List<T> result = new ArrayList<T>(1);
            try {
                invokeAndWaitLowPriority(this, new Runnable() {
                    public void run() {
                        result.add(action.run());
                    }
                });
            } catch (InterruptedException e) {
                throw new IllegalStateException(e.toString());
            } catch (InvocationTargetException e) {
                Throwable t = e.getTargetException();
                if (t instanceof RuntimeException) {
                    throw (RuntimeException)t;
                } else if (t instanceof Error) {
                    throw (Error)t;
                } else {
                    throw new IllegalStateException(t.toString());
                }
            }
            return result.get(0);
        }
    }
    
    public <T, E extends Exception> T read(final LockExceptionAction<T,E> action) throws E {
        if (isDispatchThread()) {
            return action.run();
        } else {
            final Throwable[] exc = new Throwable[1];
            final List<T> result = new ArrayList<T>(1);
            try {
                invokeAndWaitLowPriority(this, new Runnable() {
                    public void run() {
                        try {
                            result.add(action.run());
                        } catch (Throwable t) {
                            exc[0] = t;
                        }
                    }
                });
            } catch (InterruptedException e) {
                throw new IllegalStateException(e.toString());
            } catch (InvocationTargetException e) {
                // Should not happen since we caught Exception above already:
                throw new IllegalStateException(e.getTargetException().toString());
            }
            if (exc[0] instanceof RuntimeException) {
                throw (RuntimeException)exc[0];
            } else if (exc[0] instanceof Error) {
                throw (Error)exc[0];
            } else if (exc[0] != null) {
                @SuppressWarnings("unchecked")
                E e = (E) exc[0];
                throw e;
            } else {
                return result.get(0);
            }
        }
    }
    
    public void readLater(final Runnable action) {
        invokeLaterLowPriority(this, action);
    }
    
    public <T> T write(LockAction<T> action) {
        return read(action);
    }
    
    public <T, E extends Exception> T write(LockExceptionAction<T,E> action) throws E {
        return read(action);
    }
    
    public void writeLater(Runnable action) {
        readLater(action);
    }
    
    public void read(Runnable action) {
        if (isDispatchThread()) {
            action.run();
        } else {
            try {
                invokeAndWaitLowPriority(this, action);
            } catch (InterruptedException e) {
                throw new IllegalStateException(e.toString());
            } catch (InvocationTargetException e) {
                Throwable t = e.getTargetException();
                if (t instanceof RuntimeException) {
                    throw (RuntimeException)t;
                } else if (t instanceof Error) {
                    throw (Error)t;
                } else {
                    throw new IllegalStateException(t.toString());
                }
            }
        }
    }
    
    public void write(Runnable action) {
        read(action);
    }
    
    public boolean canRead() {
        return isDispatchThread();
    }
    
    public boolean canWrite() {
        return isDispatchThread();
    }
    
    public String toString() {
        return "Locks.eventLock"; // NOI18N
    }
    
    /** @return true iff current thread is EventDispatchThread */
    static boolean isDispatchThread() {
        boolean dispatch = EventQueue.isDispatchThread ();
        if (!dispatch && Utilities.getOperatingSystem () == Utilities.OS_SOLARIS) {
            // on solaris the event queue is not always recognized correctly
            // => try to guess by name
            dispatch = (Thread.currentThread().getClass().getName().indexOf("EventDispatchThread") >= 0); // NOI18N
        }
        return dispatch;
    }

    /**
     * Similar to {@link EventQueue#invokeLater} but posts the event at the same
     * priority as paint requests, to avoid bad visual artifacts.
     */
    static void invokeLaterLowPriority(RWLock m, Runnable r) {
        Toolkit t = Toolkit.getDefaultToolkit();
        EventQueue q = t.getSystemEventQueue();
        q.postEvent(new PaintPriorityEvent(m, t, r, null, false));
    }
    
    /**
     * Similar to {@link EventQueue#invokeAndWait} but posts the event at the same
     * priority as paint requests, to avoid bad visual artifacts.
     */
    static void invokeAndWaitLowPriority(RWLock m, Runnable r)
            throws InterruptedException, InvocationTargetException {
        Toolkit t = Toolkit.getDefaultToolkit();
        EventQueue q = t.getSystemEventQueue();
        Object lock = new PaintPriorityEventLock();
        InvocationEvent ev = new PaintPriorityEvent(m, t, r, lock, true);
        synchronized (lock) {
            q.postEvent(ev);
            lock.wait();
        }
        Exception e = ev.getException();
        if (e != null) {
            throw new InvocationTargetException(e);
        }
    }
    
    private static final class PaintPriorityEvent extends InvocationEvent {
        private final RWLock m;
        public PaintPriorityEvent(RWLock m, Toolkit source, Runnable runnable, Object notifier, boolean catchExceptions) {
            super(source, PaintEvent.PAINT, runnable, notifier, catchExceptions);
            this.m = m;
        }
        public String paramString() {
            return super.paramString() + ",lock=" + m; // NOI18N
        }
    }
    private static final class PaintPriorityEventLock {}
    
}
