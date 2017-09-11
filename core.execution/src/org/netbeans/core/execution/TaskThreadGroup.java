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

package org.netbeans.core.execution;


/**
*
* @author Ales Novak
* @version 0.10 Mar 19, 1998
*/
final class TaskThreadGroup extends ThreadGroup {
    /** lock for waitFor/wakeUpProcesses */
    private Object TIMER = new Object();

    /** true if RunClass thread entered waitFor - out of main method */
    private boolean runClassThreadOut = false;

    /** true iff the ThreadGroup can be finalized - that is after all threads die */
    private boolean finalizable;

    /** Is the process dead? The group can be marked as dead but still have running threads */
    private boolean dead;
    
    /** reference to a RunClassThread */
    private RunClassThread runClassThread;

    TaskThreadGroup(ThreadGroup parent, String name) {
        super(parent, name);

        dead = false;
    }

    /**
    * @return true iff there is not any window there is not more than two threads
    * and first one if any must be waiting for next event in the queue (EventDispatchThread) and
    * the second must be instanceof ExecutionEngine$RunClass thread and must wait
    * external processes are never "dead"
    */
    private boolean isProcessDead(boolean inside) {

        synchronized (this) {
            if (dead) {
                return true;
            }

            // first System.out.println must be before sync block
            if (! finalizable) {
                return false;
            }

            int count;
            count = activeCount();

            /* Following lines patch bug in implementaion of java.lang.-Thread/ThreadGroup
               The bug - if new Thread(...) call is issued and the thread is not started.
               The thread is added as a member to its thread group - such threads are counted in
               calls activeCount() and enumerate(Thread[] ts, ...) although they do not live - e.g
               call destroy() to threadgroup always throws an exception. Solution is to start suspicious
               threads. I added try - catch block for already started threads.
               This is patch for some code used inside Swing - code like this " ... = new Thread().getThreadGroup();"
               see - javax.swing.SystemEventQueueUtilities$RunnableCanvas.maybeRegisterEventDispatchThread()
               Original error was that TaskThreadGroups that had not any threads at thread dump were not removed.
               !!! remove spagetti code if the bug is repaired in JDK.
            */
            if (count > 1) {
                int active = 0;
                Thread[] threads =
                    new Thread[count];
                enumerate(threads);
                for (int i = threads.length; --i >= 0;) {
                    // #33630 - ignore daemon threads
                    if ((threads[i] != null) &&
                            (threads[i].isAlive() && !threads[i].isDaemon())
                       ) {
                        if (++active > 1) {
                            return false;
                        }
                    }
                }
                count = active;
            }

            if (ExecutionEngine.hasWindows(this)) {
                return false;
            }

            if (count == 0) {
                return true; // no thread, no wins
            }

            // one thread remains - RunClass
            if (inside) {
                return true;
            }
            return false;
        }
    }

    /** blocks until this ThreadGroup die - isProcessDead = true
    */
    void waitFor() throws InterruptedException {
        boolean inside = Thread.currentThread() instanceof RunClassThread;
        synchronized (TIMER) {
            try {
                while (! isProcessDead(inside)) {
                    TIMER.wait(1000);
                }
            } finally {
                TIMER.notifyAll();
		synchronized(this) {
                    dead = true;
                }
            }
        }
    }
    
    /** Marks this group as finalizable. */
    synchronized void setFinalizable() {
        finalizable = true;
    }

    /** sets the group as dead - called from DefaultSysProcess */
    void kill() {
        synchronized (TIMER) {
            if (! dead) {
                dead = true;
                TIMER.notifyAll();
                try {
                    TIMER.wait(3000);
                } catch (InterruptedException e) {
                }
            }
        }
    }
    
    public void setRunClassThread(RunClassThread t) {
        runClassThread = t;
    }
    
    public RunClassThread getRunClassThread() {
        return runClassThread;
    }
}
