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

import java.lang.ref.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.junit.*;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.util.TaskListener;

public class RequestProcessorTest extends NbTestCase {
    static {
        System.setProperty("org.openide.util.Lookup", "org.openide.util.RequestProcessorTest$Lkp");
    }

    private Logger log;

    public RequestProcessorTest(java.lang.String testName) {
        super(testName);
    }
    
    @Override
    protected int timeOut() {
        return 30000;
    }

    @Override
    protected void setUp () throws Exception {
        super.setUp();
        
        log = Logger.getLogger("test." + getName());
    }

    @Override
    protected Level logLevel() {
        return Level.FINE;
    }
    
    public void testStopAndSchedule() throws Exception {
        final boolean executed[] = { false };
        class R implements Runnable {
            @Override
            public void run() {
                executed[0] = true;
            }
        }
        
        RequestProcessor rp = new RequestProcessor("stopped");
        RequestProcessor.Task task = rp.create(new R());
        assertTrue("No runnables", rp.shutdownNow().isEmpty());
        task.schedule(0);
        task.waitFinished(500);
        assertFalse("Not executed at all", executed[0]);
    }
    
    public void testUseAsInCND() throws Exception {
        final RequestProcessor processor = new RequestProcessor("testUseAsInCND");
        final AtomicReference<String> threadName = new AtomicReference<String>();
        final Runnable task = new Runnable() {
            @Override
            public void run() {
                threadName.set(Thread.currentThread().getName());
            }
        };
        final String taskName = "MyTask";
        final RequestProcessor.Task rpTask = processor.create(new Runnable() {
            @Override
            public void run() {
                String oldName = Thread.currentThread().getName();
                Thread.currentThread().setName(taskName);
                try {
                    task.run();
                } finally {
                    Thread.currentThread().setName(oldName);
                }
            }
        });
        processor.post(rpTask);
        rpTask.waitFinished();
        
        assertEquals("Executed and named OK", taskName, threadName.get());
    }
    
    public void testStartCreatedJob() throws Exception {
        final RequestProcessor rp = new RequestProcessor("testStartCreatedJob");
        final boolean[] executed = new boolean[1];
        rp.post (new Runnable() {
            @Override
            public void run() {
                RequestProcessor.Task t = rp.create(new Runnable() {
                    @Override
                    public void run() {
                        executed[0] = true;
                    }
                });
                t.waitFinished();
            }
        }).waitFinished();
        assertTrue("Inner created task finished", executed[0]);
    }
    
    public void testWaitFinishedByVladimir() throws Exception {
        final RequestProcessor RP = new RequestProcessor("BrokenRP", 2);
        final Logger LOG = Logger.getLogger("test.waitFinishedByVladimir");
        final AtomicBoolean outerDone = new AtomicBoolean(false);
        RequestProcessor.Task outerTask;
        outerTask = RP.post(new Runnable() {
            @Override
            public void run() {
                try {
                    final AtomicBoolean innerDone = new AtomicBoolean(false);
                    RequestProcessor.Task innerTask = RP.post(new Runnable() {
                        @Override
                        public void run() {
                            LOG.info("Task1 start");
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                            LOG.info("Task1 finished 1");
                            innerDone.set(true);
                            LOG.info("Task1 finished marked");
                        }
                    });
                    LOG.info("wait Task1");
                    Thread.sleep(1000);
                    innerTask.waitFinished();
                    LOG.info("after wait Task1 " + innerDone);
                    outerDone.set(innerDone.get());
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
        outerTask.waitFinished();
        LOG.info("after wait Post " + outerDone);
        assertTrue(outerDone.get());
    }    
    
    public void testNonParallelReSchedule() throws Exception {
        final AtomicInteger counter = new AtomicInteger();
        final AtomicInteger peek = new AtomicInteger();
        peek.set(1);

        class R implements Runnable {

            @Override
            public void run() {
                try {
                    int cnt = counter.incrementAndGet();
                    Thread.sleep(200);
                    int now = counter.get();
                    if (now > peek.get()) {
                        peek.set(now);
                    }
                    counter.decrementAndGet();
                } catch (InterruptedException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
        R run = new R();
        RequestProcessor RP = new RequestProcessor("testNonParallelReSchedule", 20);
        RequestProcessor.Task task = RP.create(run);
        for (int i = 0; i < 20; i++) {
            task.schedule(0);
            Thread.sleep(10);
        }
        for (int i = 0; i < 50; i++) {
            task.waitFinished();
        }

        assertEquals("At most one task at once", 1, peek.get());
    }
    
    
    /** A test to check that objects are executed in the right order.
     */
    public void testOrder () throws Exception {
        final int[] count = new int[1];
        final String[] fail = new String[1];
        
        class X extends Object 
        implements Runnable, Comparable {
            public int order;
            
            public void run () {
                if (order != count[0]++) {
                    if (fail[0] == null) {
                        fail[0] = "Executing task " + order + " instead of " + count[0];
                    }
                }
            }
            
            public int compareTo (Object o) {
                X x = (X)o;
                
                return System.identityHashCode (x) - System.identityHashCode (this);
            }
            
            @Override
            public String toString () {
                return "O: " + order;
            }
        }
        
        // prepare the tasks 
        X[] arr = new X[10];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = new X ();
        }
        
        // sort it
//        Arrays.sort (arr);
        
        for (int i = 0; i < arr.length; i++) {
            arr[i].order = i;
        }
        
        // execute the task as quickly as possible (only those with the same time
        // can have wrong order
        RequestProcessor.Task[] wait = new RequestProcessor.Task[arr.length];
        for (int i = 0; i < arr.length; i++) {
            wait[i] = RequestProcessor.postRequest (arr[i]);
        }
        
        // wait to all tasks to finish
        for (int i = 0; i < arr.length; i++) {
            wait[i].waitFinished ();
        }
        
        if (fail[0] != null) {
            fail (fail[0]);
        }
            
    }
    
    public void testTaskLeakWhenCancelled() throws Exception {
        Runnable r = new Runnable() {public void run() {}};

        // schedule (1hour) and cancel immediatelly
        new RequestProcessor(getName()).post(r, 3600*1000).cancel();
        
        WeakReference<Runnable> wr = new WeakReference<Runnable>(r);
        r = null;
        assertGC("runnable should be collected", wr);
    }

    public void testStackOverFlowInRunnable() throws Exception {
        Runnable r = new Runnable() {public void run() { throw new StackOverflowError(); }};

        CharSequence msgs = Log.enable("org.openide.util", Level.SEVERE);
        new RequestProcessor(getName()).post(r).waitFinished();
        if (msgs.toString().contains("fillInStackTrace")) {
            fail("There shall be no fillInStackTrace:\n" + msgs);
        }
    }

    /* This might be issue as well, but taking into account the typical lifecycle
        of a RP and its size, I won't invest in fixing this now. 
     *//*
    public void testRPLeakWhenLastTaskCancelled() throws Exception {
        Runnable r = new Runnable() {public void run() {}};

        // schedule (1hour) and cancel immediatelly
        RequestProcessor rp = new RequestProcessor(getName());
        rp.post(r, 3600*1000).cancel();
        
        WeakReference wr = new WeakReference(rp);
        rp = null;
        assertGC("runnable should be collected", wr);
    } /**/  

    @RandomlyFails
    public void testScheduleAndIsFinished() throws InterruptedException {
        class Run implements Runnable {
            public boolean run;
            public boolean second;
            
            public synchronized void run() {
                if (run) {
                    second = true;
                    return;
                }
                
                try {
                    notifyAll();
                    wait();
                } catch (InterruptedException ex) {
                    fail(ex.getMessage());
                }
                run = true;
            }
        }
        
        
        Run r = new Run();
        RequestProcessor.Task task;
        synchronized (r) {
            task = new RequestProcessor(getName()).post(r);
            r.wait();
            task.schedule(200);
            r.notifyAll();
        }

        Thread.sleep(100);
        assertTrue("Run successfully", r.run);
        assertFalse("Not for the second time1", r.second);
        assertFalse("Not finished as it is scheduled", task.isFinished());
        assertFalse("Not for the second time2", r.second);
        
        task.waitFinished();
        assertTrue("Finished now", task.isFinished());
        assertTrue("Run again", r.second);
        
    }

    static final class Priority {
        static final RequestProcessor RP = new RequestProcessor(Priority.class);
    }

    /**
     * A test that check that priorities are handled well.
     */
    public void testPriorityQueue() throws Exception {
        
        final Runnable[] arr = new Runnable[5];
        
        class R implements Runnable {
            
            public int index;
            
            public R (int i) {
                index = i;
            }
            
            public synchronized void run () {
                for (int i = 0; /*i < arr.length*/; i++) {
                    if (arr[i] == null) {
                        arr[i] = this;
                        break;
                    }
                }
                
            }
            
            @Override
            public String toString () {
                return " R index " + index;
            }
        }       

        Runnable r[] = new Runnable[5];
        // expected order of execution
        for (int i = 0; i<5; i++) {
            r[i] = new R(i);
        }
        
        RequestProcessor rp = Priority.RP;
        
        RequestProcessor.Task t[] = new RequestProcessor.Task[5];
        synchronized (r[0]) {
            t[4] = rp.post(r[0], 0, 3);
            t[0] = rp.post(r[4], 0, 1);
            t[1] = rp.post(r[2], 0, 2);
            t[2] = rp.post(r[1], 0, 2);
            t[3] = rp.post(r[3], 0, 2);            
            t[2].setPriority(3);
        }
        
        for (int i = 0; i<5; i++) {
            t[i].waitFinished();
        }
        
        StringBuilder order = new StringBuilder();
        boolean fail = false;
        for (int i = 0; i<5; i++) {
            R next = (R) arr[i];
            order.append(i).append(" is ").append(next.index).append("\n");
            if (next.index != i) {
                order.append("Expected at ").append(i).append(" but was ").append(next.index).append("\n");
                fail = true;
            }
        }
        if (fail) {
            fail(order.toString());
        }
    }
    
    /** Test bug http://www.netbeans.org/issues/show_bug.cgi?id=31906
     */
    public void testBug31906_SimulateDataFolderTest () {
        RequestProcessor rp = new RequestProcessor ("dataFolderTest");
        
        class X implements Runnable {
            private RequestProcessor.Task wait;
            private int cnt;
            
            public synchronized void run () {
                if (wait != null) {
                    wait.waitFinished ();
                    cnt++;
                } else {
                    cnt++;
                }
            }
            
            public synchronized void assertCnt (String msg, int cnt) {
                assertEquals (msg, cnt, this.cnt);
                this.cnt = 0;
            }
            
            public synchronized void waitFor (RequestProcessor.Task t) {
                wait = t;
            }
            
        }
        X[] arr = { new X(), new X() };
        RequestProcessor.Task[] tasks = { 
            rp.create (arr[0]), 
            rp.create (arr[1])
        };
        tasks[0].setPriority(Thread.NORM_PRIORITY - 1);
        tasks[1].setPriority(Thread.NORM_PRIORITY + 1);
        
        tasks[0].schedule(0);
        tasks[1].schedule(0);

        tasks[0].waitFinished();
        arr[0].assertCnt (" Once", 1);
        tasks[1].waitFinished ();
        arr[1].assertCnt (" Once as well", 1);

        tasks[0].schedule(100);
        tasks[1].schedule(100);
        tasks[0].schedule(10);
        tasks[1].schedule(10);

        tasks[0].waitFinished();
        tasks[1].waitFinished();

        arr[0].assertCnt (" 1a", 1);
        arr[1].assertCnt (" 1b", 1);

        arr[0].waitFor (tasks[1]);
        tasks[1].schedule(100);
        tasks[0].schedule(10);
        tasks[0].waitFinished ();
        arr[0].assertCnt (" task 0 is executed", 1);
        arr[1].assertCnt (" but it also executes task 1", 1);

        tasks[0].schedule(10);
        tasks[0].waitFinished ();
        arr[0].assertCnt (" task O is executed", 1);
        arr[1].assertCnt (" but it does not execute 1", 0);
    }
    
    
    /** Test priority inversion and whether it is properly notified
     */
    public void testPriorityInversionProblemAndItsDiagnosis () throws Exception {
        RequestProcessor rp = new RequestProcessor ("testPriorityInversionProblemAndItsDiagnosis");
        
        final Runnable[] arr = new Runnable[3];
        
        class R implements Runnable {
            
            public int index;
            public Task t;
            
            public R (int i) {
                index = i;
            }
            
            public synchronized void run () {
                for (int i = 0; /*i < arr.length*/; i++) {
                    if (arr[i] == null) {
                        arr[i] = this;
                        break;
                    }
                }
                
                if (t != null) {
                    t.waitFinished ();
                }
            }
            
            @Override
            public String toString () {
                return " R index " + index;
            }
        }
         
        R r1 = new R (1);
        R r2 = new R (2);
        R r3 = new R (3);
        
        Task t1;
        Task t2;
        Task t3;
        
        synchronized (r1) {
            t1 = rp.post (r1);
            t2 = rp.post (r2);
            
            // r1 will call the waitFinished of r3
            r1.t = t3 = rp.post (r3);
        }
        
        t1.waitFinished ();
        t2.waitFinished ();
        t3.waitFinished ();
        
        assertEquals ("First started is t1", r1, arr[0]);
        assertEquals ("Second started is t3", r3, arr[1]);
        assertEquals ("Last started is t2", r2, arr[2]);
        
        // now we should ensure that the RP warned everyone about the 
        // priority inheritance and all its possible complications (t2 running
        // later than t3)
    }

    public void testPriorityInversionOnFinishedTasks () throws Exception {
        RequestProcessor rp = new RequestProcessor (getName());

        class R extends Handler implements Runnable {
            RequestProcessor.Task waitFor;
            boolean msgOk;

            public R (int i) {
            }

            public void run () {
                if (waitFor != null) {
                    waitFor.waitFinished();
                }
            }

            @Override
            public void publish(LogRecord record) {
                if (record.getMessage().contains("not running it synchronously")) {
                    msgOk = true;
                    waitFor.schedule(100);
                }
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() throws SecurityException {
            }
        }
        R snd = new R(2);
        snd.waitFor = rp.post(new R(1));

        RequestProcessor.logger().addHandler(snd);
        Level prev = RequestProcessor.logger().getLevel();
        RequestProcessor.logger().setLevel(Level.FINEST);
        try {
            snd.waitFor.waitFinished();
            assertTrue("Finished", snd.waitFor.isFinished());

            RequestProcessor.Task task = rp.post(snd);
            task.waitFinished();
            assertTrue("Finished as well", task.isFinished());

            assertTrue("Message arrived", snd.msgOk);
        } finally {
            RequestProcessor.logger().setLevel(prev);
            RequestProcessor.logger().removeHandler(snd);
        }
    }
    
    /** Test of finalize method, of class org.openide.util.RequestProcessor. */
    public void testFinalize() throws Exception {
        RequestProcessor rp = new RequestProcessor ("toGarbageCollect");
        Reference<RequestProcessor> ref = new WeakReference<RequestProcessor> (rp);
        Reference<Task> task;
        
        final Object lock = new Object ();
        
        
        synchronized (lock) {
            task = new WeakReference<Task> (rp.post (new Runnable () {
                public void run () {
                    synchronized (lock) {
                        lock.notify ();
                    }
                }
            }));
            
            
            rp = null;

            doGc (10, null);
            
            if (ref.get () == null) {
                fail ("garbage collected even a task is planed."); // NOI18N
            }
            
            // run the task
            lock.wait ();
            
        }
        
        doGc (1000, task);
        
        if (task.get () != null) {
            fail ("task is not garbage collected.");
        }
        
        doGc (1000, ref);
        if (ref.get () != null) {
            fail ("not garbage collected at all."); // NOI18N
        }
        
    }
    
    /** Check whether task is finished when it should be.
     */
    public void testCheckFinished () {
        doCheckFinished(false);
    }
    public void testCheckFinishedWithFalse () {
        doCheckFinished(true);
    }
    
    private void doCheckFinished(boolean usefalse) {
        RequestProcessor rp = new RequestProcessor ("Finish");

class R extends Object implements Runnable {
    RequestProcessor.Task t;
    
    public void run () {
        if (t.isFinished ()) {
            fail ("Finished when running");
        }
    }
}
        
        R r = new R ();
        RequestProcessor.Task task = usefalse ? rp.create(r, false) : rp.create (r);
        r.t = task;

        if (task.isFinished ()) {
            fail ("Finished after creation");
        }
     
        doCommonTestWithScheduling(task);
    }

    private void doCommonTestWithScheduling(final RequestProcessor.Task task) {
     
        task.schedule (200);
        
        if (task.isFinished ()) {
            fail ("Finished when planed");
        }
        
        task.waitFinished ();
        
        if (!task.isFinished ()) {
            fail ("Not finished after waitFinished");
        }
        
        task.schedule (200);
        
        if (task.isFinished ()) {
            fail ("Finished when planed");
        }
    }

    public void testCheckFinishedWithTrue () {
        RequestProcessor rp = new RequestProcessor ("Finish");
        
        class R extends Object implements Runnable {
            RequestProcessor.Task t;
            
            public void run () {
                if (t.isFinished ()) {
                    fail ("Finished when running");
                }
            }
        }
        
        R r = new R ();
        RequestProcessor.Task task = rp.create(r, true);
        r.t = task;

        assertTrue("It has to be finished after creation", task.isFinished());

        task.waitFinished();

        // rest is the same
        doCommonTestWithScheduling(task);
    }
        

    /** Test to check the waiting in request processor.
    */
    public void testWaitFinishedOnNotStartedTask () throws Exception {
        Counter x = new Counter ();
        final RequestProcessor.Task task = RequestProcessor.getDefault().create (x);
        
        //
        // Following code tests whether the RP.create().waitFinished really blocks
        // until somebody schedules the task.
        //
        class WaitThread extends Thread {
            public boolean finished;
            
            @Override
            public void run () {
                task.waitFinished ();
                synchronized (this) {
                    finished = true;
                    notifyAll ();
                }
            }
            
            public synchronized void w (int timeOut) throws Exception {
                if (!finished) {
                    wait (timeOut);
                }
            }
        }
        WaitThread wt = new WaitThread ();
        wt.start ();
        wt.w (100);
        assertTrue ("The waitFinished has not ended, because the task has not been planned", !wt.finished);
        task.schedule (0);
        wt.w (0);
        assertTrue ("The waitFinished finished, as the task is now planned", wt.finished);
        x.assertCnt ("The task has been executed", 1);
    }
    
    public void testTheCancelOfNonStartedTask() {
        Counter x = new Counter ();
        RequestProcessor rp = new RequestProcessor ("testTheCancelOfNonStartedTask");
        final RequestProcessor.Task task = rp.create (x);
        assertFalse("Not started tasks cannot be cancelled", task.cancel());
        assertFalse("But not finished", task.isFinished());
        assertFalse("Can be cancelled only once", task.cancel());
    }

    public void testTheCancelOfFinishedTask() {
        Counter x = new Counter ();
        RequestProcessor rp = new RequestProcessor ("testTheCancelOfFinishedTask");
        final RequestProcessor.Task task = rp.post(x);
        task.waitFinished();
        assertTrue("Finished", task.isFinished());
        assertFalse("Too late to cancel", task.cancel());
    }

    public void testTheCancelOfRunningTask() throws InterruptedException {
        final CountDownLatch started = new CountDownLatch(1);
        final CountDownLatch allowedToFinish = new CountDownLatch(1);
        Counter x = new Counter () {
            @Override
            public void run() {
                started.countDown();
                super.run();
                for (;;) try {
                    allowedToFinish.await();
                    break;
                } catch (InterruptedException ex) {
                    continue;
                }
            }
        };
        RequestProcessor rp = new RequestProcessor ("testTheCancelOfRunningTask");
        final RequestProcessor.Task task = rp.post(x);
        started.await();
        assertFalse("Finished", task.isFinished());
        assertFalse("Too late to cancel", task.cancel());
        allowedToFinish.countDown();
        assertFalse("nothing to cancel", task.cancel());
        task.waitFinished();
        assertTrue("Now it is finished", task.isFinished());
        assertFalse("Still nothing to cancel", task.cancel());
    }

    public void testTheCancelOfFutureTask() {
        Counter x = new Counter ();
        RequestProcessor rp = new RequestProcessor ("testTheCancelOfFutureTask");
        final RequestProcessor.Task task = rp.create (x);
        task.schedule(20000);
        assertTrue("Sure, that one can be cancelled", task.cancel());
        assertTrue("After cancle we are finished", task.isFinished());
        assertFalse("Can be cancelled only once", task.cancel());
    }
    
    /** Test to check the waiting in request processor.
    */
    public void testWaitFinishedOnNotStartedTaskFromRPThread () throws Exception {
        Counter x = new Counter ();
        RequestProcessor rp = new RequestProcessor ("testWaitFinishedOnNotStartedTaskFromRPThread");
        final RequestProcessor.Task task = rp.post(x, Integer.MAX_VALUE);
        
        //
        // Following code tests whether the RP.create().waitFinished really blocks
        // until somebody schedules the task.
        //
        class WaitTask implements Runnable {
            public boolean finished;
            
            public synchronized void run () {
                task.waitFinished ();
                finished = true;
                notifyAll ();
            }
            
            public synchronized void w (int timeOut) throws Exception {
                if (!finished) {
                    wait (timeOut);
                }
            }
        }
        WaitTask wt = new WaitTask ();
        rp.post (wt);
        wt.w (0);
        assertTrue ("The task.waitFinished has to finish, otherwise the RequestProcessor thread will stay occupied forever", wt.finished);
        x.assertCnt ("The task has been executed - wait from RP made it start", 1);
    }
        
    public void testWaitFinished2 () {        
        Counter x = new Counter ();
        final RequestProcessor.Task task = RequestProcessor.getDefault().create (x);
        task.schedule (500);
        if (task.cancel ()) {
            assertTrue("Marked as finished after cancel", task.isFinished());
            task.waitFinished();
        }

        // does a task that is scheduled means that it is not finished?
        task.schedule (200);
        task.waitFinished();
        x.assertCnt ("Wait does not wait for finish of scheduled tasks, that already has been posted", 1);
    }
    
    public void testWaitFinishedFromItself() {
        class R implements Runnable {

            int cnt;
            RequestProcessor.Task waitFor;

            @Override
            public void run() {
                cnt++;
                waitFor.waitFinished();
            }
        }

        R r = new R();
        r.waitFor = RequestProcessor.getDefault().create(r);
        r.waitFor.schedule(0);
        r.waitFor.waitFinished();

        assertEquals("Executed once", 1, r.cnt);
    }
    
    /** Ensure that it is safe to call schedule() while the task is running
     * (should finish the task and run it again).
     */
    public void testScheduleWhileRunning() throws Exception {
        class X implements Runnable {
            public synchronized void run() {
                try {
                    if (cnt == 0) {
                        this.notify(); // #1
                        this.wait(9999); // #2
                        cnt++;
                    } else {
                        cnt++;
                        this.notify(); // #3
                    }
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
            public int cnt = 0;
        }
        X x = new X();
        synchronized (x) {
            RequestProcessor.Task task = RequestProcessor.postRequest(x);
            x.wait(9999); // #1
            assertEquals(0, x.cnt);
            task.schedule(0);
            x.notify(); // #2
            x.wait(9999); // #3
            assertEquals(2, x.cnt);
        }
    }
    
    /** Make sure it is safe to call waitFinished() on a task from within
     * a task listener.
     */
    public void testWaitFinishedFromNotification() throws Exception {
        class X implements Runnable {
            private Task task;
            private int cnt;
            public synchronized Task start() {
                if (task == null) {
                    task = RequestProcessor.postRequest(this);
                }
                return task;
            }
            public void run() {
                cnt++;
            }
            public int getCount() {
                return cnt;
            }
            public void block() {
                start().waitFinished();
            }
        }
        final X x = new X();
        final Object lock = "wait for task to finish";
        final boolean[] finished = new boolean[1];
        x.start().addTaskListener(new TaskListener() {
            public void taskFinished(Task t) {
                x.block();
                finished[0] = true;
                synchronized (lock) {
                    lock.notify();
                }
            }
        });
        synchronized (lock) {
            lock.wait(5000);
        }
        assertTrue(finished[0]);
        assertEquals(1, x.getCount());
    }

    /** Make sure that successfully canceled task is not performed.
     */
    public void testCancel() throws Exception {
        class X implements Runnable {
            public boolean performed = false;
            public void run() {
                performed = true;
            }
        }
        
        X x = new X();
        final boolean[] finished = new boolean[1];
        finished[0] = false;
        
        // post task with some delay
        RequestProcessor.Task task = RequestProcessor.postRequest(x, 1000);
        task.addTaskListener(new TaskListener() {
            @Override
            public void taskFinished(Task t) {
                finished[0] = true;
            }
        });

        boolean canceled = task.cancel();
        assertTrue("Task is canceled now", canceled);
        assertTrue("Cancelling actually means finished", finished[0]);
        Thread.sleep(1500); // wait longer than task delay
        assertFalse("Task should not be performed", x.performed);
    }
    
    public void testWaitWithTimeOutCanFinishEvenTheTaskHasNotRun () throws Exception {
        class Run implements Runnable {
            public boolean runned;
            public synchronized void run () {
                runned = true;
            }
        }
        
        Run run = new Run ();
        
        synchronized (run) {
            RequestProcessor.Task task = RequestProcessor.getDefault ().post (run);
            task.waitFinished (100);
            assertFalse ("We are here and the task has not finished", run.runned);
            assertFalse ("Not finished", task.isFinished ());
        }
    }
    
    public void testWhenWaitingForALimitedTimeFromTheSameProcessorThenInterruptedExceptionIsThrownImmediatelly () throws Exception {
        Counter x = new Counter ();
        RequestProcessor rp = new RequestProcessor ("testWaitFinishedOnNotStartedTaskFromRPThread");
        final RequestProcessor.Task task = rp.create (x);
        
        class WaitTask implements Runnable {
            public boolean finished;
            
            public synchronized void run () {
                long time = System.currentTimeMillis ();
                try {
                    task.waitFinished (1000);
                    fail ("This should throw an exception. Btw time was: " + (System.currentTimeMillis () - time));
                } catch (InterruptedException ex) {
                    // ok, this is expected
                } finally {
                    time = System.currentTimeMillis () - time;
                    notifyAll ();
                }
                if (time > 100) {
                    fail ("Exception should be thrown quickly. Was: " + time);
                }
                finished = true;
            }
            
        }
        WaitTask wt = new WaitTask ();
        synchronized (wt) {
            rp.post (wt);
            wt.wait ();
        }
        assertTrue ("The task.waitFinished has to finish", wt.finished);
        x.assertCnt ("The task has NOT been executed", 0);
    }
    
    public void testWhenWaitingForAlreadyFinishedTaskWithTimeOutTheResultIsGood () throws Exception {
        Counter x = new Counter ();
        RequestProcessor rp = new RequestProcessor ("testWaitFinishedOnStartedTaskFromRPThread");
        final RequestProcessor.Task task = rp.post (x);
        task.waitFinished ();
        x.assertCnt ("The task has been executed before", 1);
        
        class WaitTask implements Runnable {
            public boolean finished;
            
            public synchronized void run () {
                notifyAll ();
                try {
                    assertTrue ("The task has been already finished", task.waitFinished (1000));
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                    fail ("Should not happen");
                }
                finished = true;
            }
            
        }
        WaitTask wt = new WaitTask ();
        synchronized (wt) {
            rp.post (wt);
            wt.wait ();
        }
        assertTrue ("The task.waitFinished has to finish", wt.finished);
    }
    
    /**
     * A processing thread must survive throwable thrown during
     * execution of given taks. RuntimeException
     */
    public void testSurvivesException() throws Exception {
        doSurviveTest(false); // NPE
        doSurviveTest(true);  // AssertionError
    }


    private void doSurviveTest(final boolean error) throws Exception {
        RequestProcessor rp = new RequestProcessor("SurvivesTest");
        Counter x = new Counter ();
        
        final Locker lock = new Locker();
        
        rp.post (new Runnable() {
            public void run() {
                lock.waitOn();
                
                if (error) {
                    throw new AssertionError();
                } else {
                    throw new NullPointerException();
                }
            }
        });
        
        rp.post(x);
        lock.notifyOn();
        
        x.assertCntWaiting("Second task not performed after " +
                     (error ? "error" : "exception"), 1);
    }
    
    public void testCancelInterruptsTheRunningThread () throws Exception {
        RequestProcessor rp = new RequestProcessor ("Cancellable", 1, true);
        
        class R implements Runnable {
            private String name;
            
            public boolean checkBefore;
            public boolean checkAfter;
            public boolean interrupted;
            
            public R (String n) {
                this.name = n;
            }
            
            public synchronized void run () {
                checkBefore = Thread.interrupted();
                
                log.info("in runnable " + name + " check before: " + checkBefore);
                
                notifyAll ();

                log.info("in runnable " + name + " after notify");
                
                try {
                    wait ();
                    log.info("in runnable " + name + " after wait, not interrupted");
                    interrupted = false;
                } catch (InterruptedException ex) {
                    interrupted = true;
                    log.info("in runnable " + name + " after wait, interrupted");
                }
                
                notifyAll ();
                
                log.info("in runnable " + name + " after notifyAll");

                try {
                    wait ();
                    log.info("in runnable " + name + " after second wait, not interrupted");
                    checkAfter = Thread.interrupted();
                } catch (InterruptedException ex) {
                    log.info("in runnable " + name + " after second wait, interrupted");
                    checkAfter = true;
                }
                
                log.info("in runnable " + name + " checkAfter: " + checkAfter);
                
                notifyAll ();
            }
        }
        
        R r = new R ("First");
        RequestProcessor.Task t;
        synchronized (r) {
            t = rp.post (r);
            r.wait ();
            assertTrue ("The task is already running", !t.cancel ());
            log.info("Main checkpoint1");
            r.wait ();
            log.info("Main checkpoint2");
            r.notifyAll ();
            log.info("Main checkpoint3");
            r.wait ();
            log.info("Main checkpoint4");
            assertTrue ("The task has been interrupted", r.interrupted);
            assertTrue ("Not before", !r.checkBefore);
            assertTrue ("Not after - as the notification was thru InterruptedException", !r.checkAfter);
        }
        log.info("Main checkpoint5");
        t.waitFinished();
        log.info("Main checkpoint6");
        /*
        try {
            assertGC("no", new java.lang.ref.WeakReference(this));
        } catch (Error e) {
            // ok
        }
         */
        
        // interrupt after the task has finished
        r = new R ("Second");
        synchronized (r) {
            t = rp.post (r);
            log.info("Second checkpoint1");
            r.wait ();
            r.notifyAll ();
            log.info("Second checkpoint2");
            r.wait ();
            log.info("Second checkpoint3");
            assertTrue ("The task is already running", !t.cancel ());
            log.info("Second checkpoint4");
            r.notifyAll ();
            log.info("Second checkpoint5");
            r.wait ();
            assertTrue ("The task has not been interrupted by exception", !r.interrupted);
            assertTrue ("Not interupted before", !r.checkBefore);
            assertTrue ("But interupted after", r.checkAfter);
        }
        log.info("Second checkpoint6");
        t.waitFinished();
        log.info("Second checkpoint7");
    }

    public void testCancelDoesNotInterruptTheRunningThread () throws Exception {
        RequestProcessor rp = new RequestProcessor ("Not Cancellable", 1, false);
        
        class R implements Runnable {
            public boolean checkBefore;
            public boolean checkAfter;
            public boolean interrupted;
            
            public synchronized void run () {
                checkBefore = Thread.interrupted();
                
                notifyAll ();
                
                try {
                    wait ();
                    interrupted = false;
                } catch (InterruptedException ex) {
                    interrupted = true;
                }
                
                notifyAll ();
                
                try {
                    wait ();
                } catch (InterruptedException ex) {
                }
                
                checkAfter = Thread.interrupted();
                
                notifyAll ();
            }
        }
        
        R r = new R ();
        synchronized (r) {
            RequestProcessor.Task t = rp.post (r);
            r.wait ();
            assertTrue ("The task is already running", !t.cancel ());
            r.notifyAll ();
            r.wait ();
            r.notifyAll ();
            r.wait ();
            assertFalse ("The task has not been interrupted", r.interrupted);
            assertTrue ("Not before", !r.checkBefore);
            assertTrue ("Not after - as the notification was thru InterruptedException", !r.checkAfter);
        }
        
        // interrupt after the task has finished
        r = new R ();
        synchronized (r) {
            RequestProcessor.Task t = rp.post (r);
            r.wait ();
            r.notifyAll ();
            r.wait ();
            assertTrue ("The task is already running", !t.cancel ());
            r.notifyAll ();
            r.wait ();
            assertTrue ("The task has not been interrupted by exception", !r.interrupted);
            assertFalse ("Not interupted before", r.checkBefore);
            assertFalse ("Not interupted after", r.checkAfter);
        }
    }
    
    public void testInterruptedStatusIsClearedBetweenTwoTaskExecution () throws Exception {
        RequestProcessor rp = new RequestProcessor ("testInterruptedStatusIsClearedBetweenTwoTaskExecution", 1, true);
        
        final RequestProcessor.Task[] task = new RequestProcessor.Task[1];
        // test interrupted status is cleared after task ends
        class Fail implements Runnable {
            public boolean checkBefore;
            public Thread runIn;
            public boolean goodThread;
            
            public synchronized void run () {
                if (runIn == null) {
                    runIn = Thread.currentThread();
                    task[0].schedule (0);
                    
                    // wait to make sure the task is scheduled
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    goodThread = Thread.currentThread () == runIn;
                }
                    
                checkBefore = runIn.isInterrupted();
                // set the flag for next execution
                runIn.interrupt();
                
                notifyAll ();
            }
        }
        
        Fail f = new Fail ();
        synchronized (f) {
            task[0] = rp.post (f);
            
            // wait for the first execution
            f.wait ();
        }
        // wait for the second
        task[0].waitFinished ();
        
        /* Shall be true, but sometimes the threads get GCed, so we cannot really check that.
        assertTrue ("This shall be always true, but if not, than it does not mean too much"
            + " just that the tasks were not executed in the same thread. In such case it "
            + " this test does not do anything useful as it needs to execute the task twice "
            + " in the same thread", f.goodThread);
        */
        
        if (f.goodThread) {
            assertTrue ("Interrupted state has been cleared between two executions of the task", !f.checkBefore);
        }
    }
    
    public void testInterruptedStatusWorksInInversedTasks() throws Exception {
        RequestProcessor rp = new RequestProcessor ("testInterruptedStatusWorksInInversedTasks", 1, true);
        
        class Fail implements Runnable {
            public Fail (String n) {
                name = n;
            }
            
            private String name;
            public RequestProcessor.Task wait;
            public Object lock;
            public Exception ex;

            public volatile boolean executed;
            public volatile boolean checkBefore;
            public volatile boolean checkAfter;
            
            @Override
            public void run () {
                synchronized (this) {
                    executed = true;
                    checkBefore = Thread.interrupted();
                    log("checkBefore: " + checkBefore);
                    notifyAll();
                }
                if (lock != null) {
                    synchronized (lock) {
                        lock.notify();
                        try {
                            lock.wait();
                        } catch (InterruptedException interrex) {
                            this.ex = interrex;
                            interrex.printStackTrace();
                            fail ("No InterruptedException");
                        }
                        log.info("wait for lock over");
                    }
                }
                
                if (wait != null) {
                    wait.schedule(100);
                    wait.waitFinished();
                }
                
                synchronized (this) {
                    checkAfter = Thread.interrupted();
                    log.info("checkAfter: " + checkAfter);
                    notifyAll();
                }
            }
            
            @Override
            public String toString () {
                return name;
            }
        }
        
        Object initLock = new Object();
        
        Fail smaller = new Fail("smaller");
        smaller.lock = initLock;
        Fail bigger = new Fail("BIGGER");
        RequestProcessor.Task smallerTask, biggerTask;
        
        
        smallerTask = rp.create (smaller);
        biggerTask = rp.create (bigger);
        
        bigger.wait = smallerTask;
        
        synchronized (initLock) {
            log.info("schedule 0");
            biggerTask.schedule(0);
            initLock.wait();
            initLock.notifyAll();
            log.info("doing cancel");
            assertFalse ("Already running", biggerTask.cancel());
            log.info("biggerTask cancelled");
        }

        biggerTask.waitFinished();
        log.info("waitFinished over");
        
        assertTrue("Bigger executed", bigger.executed);
        assertTrue("Smaller executed", smaller.executed);
        
        assertFalse("bigger not interrupted at begining", bigger.checkBefore);
        assertFalse("smaller not interrupted at all", smaller.checkBefore);
        assertFalse("smaller not interrupted at all2", smaller.checkAfter);
        assertTrue("bigger interrupted at end", bigger.checkAfter);
        
    }

    @RandomlyFails // NB-Core-Build #1211
    public void testInterruptedStatusWorksInInversedTasksWhenInterruptedSoon() throws Exception {
        log.info("starting testInterruptedStatusWorksInInversedTasksWhenInterruptedSoon");
        RequestProcessor rp = new RequestProcessor ("testInterruptedStatusWorksInInversedTasksWhenInterruptedSoon", 1, true);
        log.info("rp created: " + rp);
        class Fail implements Runnable {
            public Fail(String n) {
                name = n;
            }
            
            private String name;
            public RequestProcessor.Task wait;
            public Object lock;
            
            public boolean checkBefore;
            public boolean checkAfter;
            
            public volatile boolean alreadyCanceled;
            
            public void run () {
                synchronized (this) {
                    checkBefore = Thread.interrupted();
                    log.info(name + " checkBefore: " + checkBefore);
                    notifyAll();
                }
                if (lock != null) {
                    synchronized (lock) {
                        lock.notify();
                    }
                }
                
                if (wait != null) {
                    // we cannot call Thread.sleep, so lets slow things own 
                    // in other way

                    log(name + " do waitFinished");
                    wait.waitFinished();
                    log(name + " waitFinished in task is over");
                    
                    log.info(name + " slowing by using System.gc");
                    while (!alreadyCanceled) {
                        System.gc ();
                    }
                    log.info(name + " ended slowing");
                    
                }
                
                synchronized (this) {
                    checkAfter = Thread.interrupted();
                    log.info(name + " checkAfter: " + checkAfter);
                    notifyAll();
                }
            }
        }
        
        Object initLock = new Object();
        
        Fail smaller = new Fail("smaller");
        Fail bigger = new Fail("bigger");
        RequestProcessor.Task smallerTask, biggerTask;
        
        
        smallerTask = rp.create (smaller);
        biggerTask = rp.create (bigger);
        log.info("tasks created. small: " + smallerTask + " big: " + biggerTask);
        
        bigger.lock = initLock;
        bigger.wait = smallerTask;
        
        synchronized (initLock) {
            log.info("Do schedule");
            biggerTask.schedule(0);
            initLock.wait();
            log.info("do cancel");
            assertFalse ("Already running", biggerTask.cancel());
            bigger.alreadyCanceled = true;
            log.info("cancel done");
        }

        biggerTask.waitFinished();
        log.info("waitFinished is over");
        
        assertFalse("bigger not interrupted at begining", bigger.checkBefore);
        assertFalse("smaller not interrupted at all", smaller.checkBefore);
        assertFalse("smaller not interrupted at all2", smaller.checkAfter);
        assertTrue("bigger interrupted at end", bigger.checkAfter);
    }
    
    public void testTaskFinishedOnCancelFiredAfterTaskHasReallyFinished() throws Exception {
        RequestProcessor rp = new RequestProcessor("Cancellable", 1, true);
        
        class X implements Runnable {
            
            volatile boolean reallyFinished = false;
            
            public synchronized void run() {
                notifyAll();
                
                try {
                    wait();
                } catch (InterruptedException e) {
                    // interrupted by Task.cancel()
                }
                
                notifyAll();
                
                try {
                    wait();
                } catch (InterruptedException e) {
                }
                
                reallyFinished = true;
            }
        }
        
        final X x = new X();
        synchronized (x) {
            RequestProcessor.Task t = rp.post(x);
            t.addTaskListener(new TaskListener() {
                public void taskFinished(Task t) {
                    assertTrue(x.reallyFinished);
                }
            });
            x.wait();
            t.cancel();
            x.wait();
            x.notifyAll();
        }
    }

    private static class TestHandler extends Handler {
        boolean stFilled = false;
        boolean exceptionCaught = false;

        @Override
        public void publish(LogRecord rec) {
            if (rec.getThrown() != null) {
                for (StackTraceElement elem : rec.getThrown().getStackTrace()) {
                    if (elem.getMethodName().contains("testStackTraceFillingDisabled")) {
                        stFilled = true;
                        break;
                    }
                }
                exceptionCaught = true;
            }
        }

        public void clear() {
            stFilled = false;
            exceptionCaught = false;
        }

        @Override
        public void flush() {
        }

        @Override
        public void close() throws SecurityException {
        }
    }

    public void testStackTraceFillingDisabled() throws InterruptedException {
        boolean ea = false;
        assert (ea = true);
        assertTrue("Test must be run with enabled assertions", ea);
        Logger l = RequestProcessor.logger();
        TestHandler handler = new TestHandler();
        l.addHandler(handler);
        try {
            RequestProcessor rp = new RequestProcessor("test rp #1", 1);
            Task t = rp.post(new Runnable() {

                public void run() {
                    throw new RuntimeException("Testing filled stacktrace");
                }
            });
//            t.waitFinished(); // does not work, thread gets notified before the exception is logged
            int timeout = 0;
            while (! handler.exceptionCaught && timeout++ < 100) {
                Thread.sleep(50);
            }
            assertTrue("Waiting for task timed out", timeout < 100);
            assertTrue("Our testing method not found in stack trace", handler.stFilled);

            handler.clear();
            timeout = 0;
            rp = new RequestProcessor("test rp #2", 1, false, false);
            t = rp.post(new Runnable() {

                public void run() {
                    throw new RuntimeException("Testing 'short' stacktrace");
                }
            });
            while (! handler.exceptionCaught && timeout++ < 100) {
                Thread.sleep(50);
            }
            assertTrue("Waiting for task timed out", timeout < 100);
            assertFalse("Our testing method found in stack trace", handler.stFilled);
        } finally {
            l.removeHandler(handler);
        }
    }

    private static void doGc(int count, Reference<?> toClear) {
        java.util.ArrayList<byte[]> l = new java.util.ArrayList<> (count);
        while (count-- > 0) {
            if (toClear != null && toClear.get () == null) break;
            
            l.add (new byte[1000]);
            System.gc ();
            System.runFinalization();
	    try {
	        Thread.sleep(10);
	    } catch (InterruptedException e) {}
        }
    }

    private static class Counter extends Object implements Runnable {
        private int count = 0;

        public synchronized void run () {
            count++;
        }
        
        public synchronized void assertCnt (String msg, int cnt) {
            assertEquals (msg, cnt, this.count);
            this.count = 0;
        }

        public synchronized void assertCntWaiting(String msg, int cnt) {
            // have to wait actively to recognize starvation :-(
            for (int i=1; i<10; i++) {
                try { wait(20*i*i); } catch (InterruptedException e) {}
                if (count == cnt) { // passed
                    count = 0;
                    return;
                }
            }
            assertEquals (msg, cnt, count); // let it fail
        }
    }

    private static class Locker {
        boolean ready = false;
        
        public synchronized void waitOn() {
            while (ready == false) {
                try {
                    wait();
                } catch (InterruptedException e) {}
            }
        }
        
        public synchronized void notifyOn() {
            ready = true;
            notifyAll();
        }
    }
}
