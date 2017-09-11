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

package org.netbeans.lib.editor.util;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Mutex that allows only one thread to proceed
 * other threads must wait until that one finishes.
 * <br>
 * The thread that "holds" the mutex (has the mutex access granted)
 * may reenter the mutex arbitrary number of times
 * (just increasing a "depth" of the locking).
 * <br>
 * If the priority thread enters waiting on the mutex
 * then it will get serviced first once the current thread
 * leaves the mutex.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class PriorityMutex {
    
    // -J-Dorg.netbeans.lib.editor.util.PriorityMutex.level=FINEST
    // FINE:  When TIMEOUTS_BEFORE_LOGGING reached start to dump the thread that acquired the lock
    // FINER: In addition store the stack trace of the lock() call (expensive - calls new Exception())
    private static final Logger LOG = Logger.getLogger(PriorityMutex.class.getName());
    
    private static final int WAIT_TIMEOUT = 2000;
    
    private static final int TIMEOUTS_BEFORE_LOGGING = 5;

    private Thread lockThread;

    private int lockDepth;
    
    private Thread waitingPriorityThread;
    
    private Exception logLockStackTrace;
    
    /**
     * Acquire the ownership of the mutex.
     *
     * <p>
     * The following pattern should always be used:
     * <pre>
     *   mutex.lock();
     *   try {
     *       ...
     *   } finally {
     *       mutex.unlock();
     *   }
     * </pre>
     */
    public synchronized void lock() {
        boolean log = LOG.isLoggable(Level.FINE);
        Thread thread = Thread.currentThread();
        int waitTimeouts = 0;
        try {
            if (thread != lockThread) { // not nested locking
                // Will wait if either there is another thread already holding the lock
                // or if there is a priority thread waiting but it's not this thread
                while (lockThread != null
                    || (waitingPriorityThread != null && waitingPriorityThread != thread)
                ) {
                    if (waitingPriorityThread == null && isPriorityThread()) {
                        waitingPriorityThread = thread;
                    }
                    wait(WAIT_TIMEOUT);
                    if (log && ++waitTimeouts > TIMEOUTS_BEFORE_LOGGING) {
                        LOG.fine("PriorityMutex: Timeout expired for thread " + // NOI18N
                                thread + "\n  waiting for lockThread=" + lockThread + "\n");
                        if (logLockStackTrace != null) {
                            LOG.log(Level.INFO, "Locker thread's lock() call follows:", logLockStackTrace);
                        }
                        waitTimeouts = 0;
                    }
                }
                lockThread = thread;
                if (log && LOG.isLoggable(Level.FINER)) {
                    logLockStackTrace = new Exception();
                    logLockStackTrace.fillInStackTrace();
                }
                assert (lockDepth == 0);
                if (thread == waitingPriorityThread) {
                    waitingPriorityThread = null; // it's now allowed to enter
                }
            } else {
                
            }
            lockDepth++;
        } catch (InterruptedException e) {
            waitingPriorityThread = null;
            throw new Error("Interrupted mutex acquiring"); // NOI18N
        }
    }
    
    /**
     * Release the ownership of the mutex.
     *
     * @see #lock()
     */
    public synchronized void unlock() {
        if (Thread.currentThread() != lockThread) {
            throw new IllegalStateException("Not locker. lockThread=" + lockThread); // NOI18N
        }
        if (--lockDepth == 0) {
            lockThread = null;
            logLockStackTrace = null;
            notifyAll(); // must all to surely notify waitingPriorityThread too
        }
    }
    
    /**
     * Can be called by the thread
     * that acquired the mutex to check whether there
     * is a priority thread (such as AWT event-notification thread)
     * waiting.
     * <br>
     * If there is a priority thread waiting the non-priority thread
     * should attempt to stop its work as soon and release the ownership
     * of the mutex.
     * <br>
     * The method must *not* be called without first taking the ownership
     * of the mutex (it is intentionally not synchronized).
     */
    public boolean isPriorityThreadWaiting() {
        return (waitingPriorityThread != null);
    }
    
    /**
     * Return a thread that acquired this mutex.
     * <br>
     * This method is intended for diagnostic purposes only.
     *
     * @return thread that currently acquired lock the mutex
        or <code>null</code>
     *  if there is currently no thread holding that acquired this mutex.
     */
    public final synchronized Thread getLockThread() {
        return lockThread;
    }

    /**
     * Return true if the current thread that is entering this method
     * is a priority thread
     * and should be allowed to enter as soon as possible.
     *
     * <p>
     * The default implementation assumes that
     * {@link javax.swing.SwingUtilities#isEventDispatchThread()}
     * is a priority thread.
     *
     * @return true if the entering thread is a priority thread.
     */
    protected boolean isPriorityThread() {
        return javax.swing.SwingUtilities.isEventDispatchThread();
    }

    @Override
    public String toString() {
        return "lockThread=" + lockThread + ", lockDepth=" + lockDepth; // NOI18N
    }

}
