/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.openide.util;

import java.util.logging.Level;
import java.util.logging.Logger;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.netbeans.junit.Log;
import static java.lang.System.nanoTime;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import org.junit.Ignore;


public class TaskTest {
    private final static long tenMiliseconds = 10000000; // in nanosecods
    
    private static final Logger LOG = Logger.getLogger("org.openide.util.TaskTest");

    private volatile boolean runHasBeenExecuted = false;
    private volatile Task executedListenerTask = null;

    //--------------------------------------------------------------------------
    private static void assertFinished(final Task task) {

        assertTrue(task.isFinished());
    }

    //--------------------------------------------------------------------------
    private static void assertWaitFinishedReturnsImmediately(final Task task) {

        final long begin = nanoTime();
        task.waitFinished();
        final long duration = nanoTime() - begin;

        assertTrue("The Task.waitFinished() took longer than 10 miliseconds. "
                + "This is not neseserily a bug.", duration < tenMiliseconds);
    }
    
    //--------------------------------------------------------------------------
    private static void assertWaitFinishedWithTimeoutReturnsImmediately(final Task task)
            throws Exception {

        final long begin = nanoTime();
        task.waitFinished(0);
        final long duration = nanoTime()- begin;

        assertTrue("The Task.waitFinished(long) took longer than milisecond. "
                + "This is not neseserily a bug.", duration < tenMiliseconds);
    }

    //--------------------------------------------------------------------------
    @Test
    public void emptyTask_isImmediatelyFinished_andNeverWaits()
            throws Exception {

        assertFinished(Task.EMPTY);
        assertWaitFinishedReturnsImmediately(Task.EMPTY);
        assertWaitFinishedWithTimeoutReturnsImmediately(Task.EMPTY);
        assertEquals("task null", Task.EMPTY.toString());
        assertEquals("null", Task.EMPTY.debug());

        Task empty = new Task(null);
        assertFinished(empty);
        assertWaitFinishedReturnsImmediately(empty);
        assertWaitFinishedWithTimeoutReturnsImmediately(empty);
        assertEquals("task null", empty.toString());
        assertEquals("null", empty.debug());
    }

    //--------------------------------------------------------------------------
    @Test
    public void runningEmptyTask_doesNothing() {

        try {
            Task.EMPTY.run();
        } catch (final NullPointerException e) {
            fail("NullPointerException shall never happen.");
        }
    }

    //--------------------------------------------------------------------------
    @Test
    public void runningTask_executesRunnableAndListeners() {

        this.runHasBeenExecuted = false;
        this.executedListenerTask = null;

        Task task = new Task(() -> {
            this.runHasBeenExecuted = true;
        });
        task.addTaskListener((t) -> {
            this.executedListenerTask = t;
        });

        assertFalse(task.isFinished());
        assertNotEquals("null", task.debug());

        task.run();

        assertTrue(this.runHasBeenExecuted);
        assertSame(task, this.executedListenerTask);
        assertTrue(task.isFinished());
    }

    //--------------------------------------------------------------------------
    @Test
    public void runningTask_doesNotRunRemovedListeners() {

        this.executedListenerTask = null;
        TaskListener listener = (t) -> {
            this.executedListenerTask = t;
        };

        Task task = new Task(() -> {
        });
        task.addTaskListener(listener);
        task.removeTaskListener(listener);

        task.run();

        assertTrue(task.isFinished());
        assertNull(this.executedListenerTask);
    }

    //--------------------------------------------------------------------------
    @Test
    public void finishedTask_executesAddedListenerImmediately() {

        this.executedListenerTask = null;

        Task task = new Task(() -> {
        });
        task.run();

        assertNull(this.executedListenerTask);
        assertTrue(task.isFinished());

        task.addTaskListener((t) -> {
            this.executedListenerTask = t;
        });

        assertSame(task, this.executedListenerTask);
        assertTrue(task.isFinished()); // still finished
    }
    
    //--------------------------------------------------------------------------
    @Ignore("Current implementation allows null listener but then Task.run throws NPE :(")
    @Test
    public void addTaskListener_throwsNullPointer_whenGivenNullArgument() {

        Task task = new Task(() -> {
        });
        try {
            task.addTaskListener(null);
            fail();
        } catch (NullPointerException e) {
            //good
        }
    }

    //--------------------------------------------------------------------------
    @Test
    public void removeTaskListener_doesNothing_whenGivenNullArgument() {

        this.executedListenerTask = null;
        
        TaskListener listener = (t) -> {
            this.executedListenerTask = t;
        };

        Task task = new Task(() -> {
        });
        task.addTaskListener(listener);
        task.removeTaskListener(null);
        task.run();

        assertSame(task, this.executedListenerTask);
    }
    
    @Test
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
    
    // this test is covered by emptyTask_isImmediatelyFinished_andNeverWaits and will be removed in next PR
    @Test
    public void testTaskEMPTYIsFinished () throws Exception {
        assertTrue (Task.EMPTY.isFinished ());
    }
        // this test is covered by emptyTask_isImmediatelyFinished_andNeverWaits and will be removed in next PR
    @Test
    public void testWaitFinishedOnEMPTYTaskReturnsImmediatelly () throws Exception {
        Task.EMPTY.waitFinished ();
    }
    // this test is covered by emptyTask_isImmediatelyFinished_andNeverWaits and will be removed in next PR
    @Test
    public void testWaitWithTimeOutReturnsImmediatellyOnFinishedTasks () throws Exception {
        assertTrue ("Was successfully finished", Task.EMPTY.waitFinished (0));
    }
    @Test
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
    @Test
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
    @Test
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
    @Test
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
