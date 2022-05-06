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

import static java.lang.System.currentTimeMillis;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Ignore;
import org.junit.Test;

public class TaskTest {

    private volatile boolean runHasBeenExecuted = false;
    private volatile Task executedListenerTask = null;

    //--------------------------------------------------------------------------
    private static void assertFinished(final Task task) {

        assertTrue(task.isFinished());
    }

    //--------------------------------------------------------------------------
    private static void assertWaitFinishedReturnsImmediately(final Task task) {

        final long begin = currentTimeMillis();
        task.waitFinished();
        final long duration = currentTimeMillis() - begin;
        // shorter than 1 milisecond
        assertEquals("The Task.waitFinished() took longer than milisecond. "
                + "This is not neseserily a bug.", 0, duration);
    }
    
    //--------------------------------------------------------------------------
    private static void assertWaitFinishedWithTimeoutReturnsImmediately(final Task task)
            throws Exception {

        final long begin = currentTimeMillis();
        task.waitFinished(0);
        final long duration = currentTimeMillis() - begin;
        // shorter than 1 milisecond
        assertEquals("The Task.waitFinished(long) took longer than milisecond. "
                + "This is not neseserily a bug.", 0, duration);
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
        assertEquals(task, this.executedListenerTask);
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

        assertEquals(task, this.executedListenerTask);
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

        assertEquals(task, this.executedListenerTask);
    }
    
    //--------------------------------------------------------------------------
    @Test
    public void taksIsNotFinished_untilRunMethodCompletes() 
            throws Exception {

        Object lock = new Object();
        Task task = new Task(() -> {
            synchronized (lock) {
                lock.notify(); // let the test thread continue
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    fail("This shall never happen as the test does not call 'interrupt()'.");
                }
            }
        });

        assertFalse(task.isFinished());

        synchronized (lock) {
            new Thread(task).start();
            lock.wait(); // wait for task to start
        }

        assertFalse(task.isFinished());

        synchronized (lock) {
            lock.notify(); //let the task finish
        }
        task.waitFinished(); 

        assertTrue(task.isFinished());
    }
    
    //--------------------------------------------------------------------------
    @Test
    public void waitFinished0_waitsUntilTaskIsFinished() throws Exception {
        
        Object lock = new Object();
        Task task = new Task(() -> {
            synchronized (lock) {
                lock.notify(); // let the test thread continue
                try {
                    lock.wait();
                    lock.wait(500); // let waitFinished(0) to wait
                } catch (InterruptedException e) {
                    fail("This shall never happen as the test does not call 'interrupt()'.");
                }
            }
        });

        assertFalse(task.isFinished());

        synchronized (lock) {
            new Thread(task).start();
            lock.wait(); // wait for task to start
        }

        assertFalse(task.isFinished());

        synchronized (lock) {
            lock.notify(); //let the task finish
        }
        final long begin = currentTimeMillis();
        
        assertTrue(task.waitFinished(0));
        
        final long duration = currentTimeMillis() - begin;
        
        assertTrue("Task.waitFinished(long) waited shorter than expected", duration >= 500);
        assertTrue(task.isFinished());
    }
    
    //--------------------------------------------------------------------------
    @Test
    public void waitFinishedWithTimeout_returnsAfterTimeout_whenTaksIsNotExecutedAtAll()
            throws Exception {
 
        Task task = new Task(()->{});
        
        assertFalse(task.isFinished());
        
        final long begin = currentTimeMillis();
        
        assertFalse(task.waitFinished(500));
        
        final long duration = currentTimeMillis() - begin;
        
        assertTrue("Task.waitFinished(long) waited shorter than expected", duration >= 500);
        assertFalse(task.isFinished());
    }

    //--------------------------------------------------------------------------
    @Test
    public void waitOnTask_thatStartsItsExecutionWithOverridenWaitFinishedMethod() 
            throws Exception { // like FolderInstances do
        
        class MyTask extends Task {

            private int values = 0;

            public MyTask() {
                notifyFinished();
            }

            @Override
            public void waitFinished() {
                notifyRunning();
                values++;
                notifyFinished();
            }
        }

        MyTask my = new MyTask(); 
        assertTrue(my.isFinished()); //The task thinks that he is finished.
        assertTrue(my.waitFinished(1000)); //Even with timeout we got the result,
        assertEquals(1, my.values); //but the old waitFinished is called.
    }

    //--------------------------------------------------------------------------
    @Test
    public void waitOnStrangeTask_thatTakesReallyLongTime() throws Exception {
        
        class MyTask extends Task {

            public MyTask() {
                notifyFinished();
            }

            @Override
            public void waitFinished() {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    fail("Should not happen");
                }
            }
        }

        MyTask my = new MyTask();
        assertTrue(my.isFinished()); //The task thinks that he is finished,
        assertFalse(my.waitFinished(1000)); //but still it get's called, but timeouts.
    }

    //--------------------------------------------------------------------------
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
