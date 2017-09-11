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

package org.openide.util;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.junit.Log;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Exceptions;
import org.openide.util.Task;

public class TaskTest extends NbTestCase {
    private Logger LOG;

    public TaskTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        LOG = Logger.getLogger("org.openide.util.Task." + getName());
    }

    
    
    public void testPlainTaskWaitsForBeingExecuted () throws Exception {
        R run = new R ();
        Task t = new Task (run);
        
        Thread thread = new Thread (t);
        synchronized (run) {
            thread.start ();
            run.wait ();
        }
        
        assertFalse ("Not finished", t.isFinished ());
        synchronized (run) {
            run.notify ();
        }
        
        t.waitFinished ();
        assertTrue ("Finished", t.isFinished ());
    }
    
    public void testTaskEMPTYIsFinished () throws Exception {
        assertTrue (Task.EMPTY.isFinished ());
    }
    
    public void testWaitFinishedOnEMPTYTaskReturnsImmediatelly () throws Exception {
        Task.EMPTY.waitFinished ();
    }

    public void testWaitWithTimeOutReturnsImmediatellyOnFinishedTasks () throws Exception {
        assertTrue ("Was successfully finished", Task.EMPTY.waitFinished (0));
    }

    public void testWaitWithTimeOutReturnsAfterTimeOutWhenTheTaskIsNotComputedAtAll () throws Exception {
        if (!canWait1s()) {
            LOG.warning("Skipping testWaitWithTimeOutReturnsAfterTimeOutWhenTheTaskIsNotComputedAtAll, as the computer is not able to wait 1s!");
            return;
        }
        
        long time = -1;
        
        CharSequence log = Log.enable("org.openide.util.Task", Level.FINER);
        for (int i = 1; i < 10; i++) {
            Task t = new Task (new R ());
            time = System.currentTimeMillis ();
            t.waitFinished (1000);
            time = System.currentTimeMillis () - time;

            assertFalse ("Still not finished", t.isFinished ());
            if (time >= 900 && time < 1100) {
                return;
            }
            LOG.log(Level.INFO, "Round {0} took {1}", new Object[]{i, time});
        }
        
        fail ("Something wrong happened the task should wait for 1000ms but it took: " + time + "\n" + log);
    }
    
    public void testWaitOnStrangeTaskThatStartsItsExecutionInOverridenWaitFinishedMethodLikeFolderInstancesDo () throws Exception {
        class MyTask extends Task {
            private int values;
            
            public MyTask () {
                notifyFinished ();
            }
            
            public void waitFinished () {
                notifyRunning ();
                values++;
                notifyFinished ();
            }
        }
        
        MyTask my = new MyTask ();
        assertTrue ("The task thinks that he is finished", my.isFinished ());
        assertTrue ("Ok, even with timeout we got the result", my.waitFinished (1000));
        assertEquals ("But the old waitFinished is called", 1, my.values);
    }
    
    public void testWaitOnStrangeTaskThatTakesReallyLongTime () throws Exception {
        class MyTask extends Task {
            public MyTask () {
                notifyFinished ();
            }
            
            public void waitFinished () {
                try {
                    Thread.sleep (5000);
                } catch (InterruptedException ex) {
                    fail ("Should not happen");
                }
            }
        }
        
        MyTask my = new MyTask ();
        assertTrue ("The task thinks that he is finished", my.isFinished ());
        assertFalse ("but still it get's called, but timeouts", my.waitFinished (1000));
    }
    
    final class R implements Runnable {
        public synchronized void run () {
            notify ();
            try {
                wait ();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    /*
     * see issue #130265
     */
    public void testWaitFinished0WaitsUntilFinished() throws Exception {
        Task task = new Task(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
        Thread thread = new Thread(task);
        thread.start();
        task.waitFinished(0);
        assertTrue ("Should be finished", task.isFinished());
    }
    
    static synchronized boolean canWait1s() throws Exception {
        for (int i = 0; i < 5; i++) {
            long before = System.currentTimeMillis();
            TaskTest.class.wait(1000);
            long after = System.currentTimeMillis();
            
            long delta = after - before;
            
            if (delta < 900 || delta > 1100) {
                return false;
            }
        }
        return true;
    }
}
