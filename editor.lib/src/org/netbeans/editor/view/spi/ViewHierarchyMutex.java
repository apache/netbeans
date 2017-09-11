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

package org.netbeans.editor.view.spi;

/**
 * Mutex that allows only one thread to proceed
 * other threads must wait until the ONE finishes.
 * <br>
 * The thread that "holds" the mutex (has the mutex access granted)
 * may reenter the mutex any number of times
 * (just increasing a "depth" of the locking).
 * <br>
 * If the priority thread enters waiting the mutex
 * then it will get serviced first once the current thread
 * leaves the mutex.
 *
 * @author Miloslav Metelka
 * @version 1.00
 */

public class ViewHierarchyMutex {

    private Thread lockThread;

    private int lockDepth;
    
    private Thread waitingPriorityThread;
    
    public synchronized void lock() {
        Thread thread = Thread.currentThread();
        boolean priorityThread = isPriorityThread(thread);

        if (thread != lockThread) { // not nested locking
            // Will wait if either there is another thread already holding the lock
            // or if there is a priority thread waiting but it's not this thread
            while (lockThread != null
                || (waitingPriorityThread != null && waitingPriorityThread != thread)
            ) {
                try {
                    if (waitingPriorityThread == null && priorityThread) {
                        waitingPriorityThread = thread;
                    }

                    wait();

                } catch (InterruptedException e) {
                    waitingPriorityThread = null;
                }
            }

            lockThread = thread;

            if (thread == waitingPriorityThread) {
                waitingPriorityThread = null; // it's now allowed to enter
            }
        }

        lockDepth++;
    }
    
    public synchronized void unlock() {
        if (Thread.currentThread() != lockThread) {
            throw new IllegalStateException("Not locker"); // NOI18N
        }

        if (--lockDepth == 0) {
            lockThread = null;

            notifyAll(); // must all to surely notify waitingPriorityThread too
        }
    }
    
    /**
     * This method is intended to be called by the non-priority thread
     * that acquired the mutex to check whether there
     * is no priority thread (such as AWT event-notification thread)
     * waiting.
     * <br>
     * If there is a priority thread waiting the non-priority thread
     * should attempt to stop its work as soon as possible and unlock
     * the hierarchy.
     * <br>
     * The method must *not* be called without first locking the hierarchy
     * (it is intentionally not synchronized).
     */
    public boolean isPriorityThreadWaiting() {
        return (waitingPriorityThread != null);
    }
    
    protected boolean isPriorityThread(Thread thread) {
        return (thread != ViewLayoutQueue.getDefaultQueue().getWorkerThread());
    }

    /**
     * Return the thread that holds a lock on this mutex.
     * <br>
     * This method is intended for diagnostic purposes only to determine
     * an intruder thread that entered the view hierarchy without obtaining
     * the lock first.
     *
     * @return thread that currently holds a lock on the hierarchy or null
     *  if there is currently no thread holding a lock on the hierarchy.
     */
    public final synchronized Thread getLockThread() {
        return lockThread;
    }

}
