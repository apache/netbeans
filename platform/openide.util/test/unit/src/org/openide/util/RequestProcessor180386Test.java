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
package org.openide.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;
import org.openide.util.Cancellable;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Tim Boudreau
 */
public class RequestProcessor180386Test extends NbTestCase {
    private static final Logger LOG = Logger.getLogger(RequestProcessor180386Test.class.getName());

    public RequestProcessor180386Test(java.lang.String testName) {
        super(testName);
    }

    public void testSubmit() throws Exception {
        class C implements Callable<String> {

            volatile boolean hasRun;

            @Override
            public String call() throws Exception {
                String result = "Hello";
                hasRun = true;
                return result;
            }
        }
        C c = new C();
        Future<String> f = RequestProcessor.getDefault().submit(c);
        assertEquals("Hello", f.get());
        assertTrue(c.hasRun);

        class R implements Runnable {

            volatile boolean hasRun;

            @Override
            public void run() {
                hasRun = true;
            }
        }
        R r = new R();
        f = RequestProcessor.getDefault().submit(r, "Goodbye");
        assertEquals("Goodbye", f.get());
        assertTrue(r.hasRun);
    }

    @RandomlyFails // NB-Core-Build #4352: notRun.empty
    public void testSomeTasksNotRunIfShutDown() throws Exception {
        final Object lock = new Object();
        int count = 10;
        final CountDownLatch waitAllLaunched = new CountDownLatch(count);
        final CountDownLatch waitOneFinished = new CountDownLatch(1);
        final RequestProcessor notificationThread = new RequestProcessor("notifier", 1, true, true);

        RequestProcessor rp = new RequestProcessor("TestRP", count * 2);
        class R implements Runnable {

            volatile boolean hasStarted;
            volatile boolean hasFinished;

            @Override
            public void run() {
                hasStarted = true;
                waitAllLaunched.countDown();
                synchronized (lock) {
                    try {
                        lock.wait();
                        if (Thread.interrupted()) {
                            return;
                        }
                    } catch (InterruptedException ex) {
                        return;
                    } finally {
                        new N(waitOneFinished).launch();
                    }
                    hasFinished = true;
                }
            }

            class N implements Runnable {
                private final CountDownLatch l;
                N (CountDownLatch l) {
                    this.l = l;
                }

                void launch() {
                    notificationThread.create(this).schedule(20);
                }

                @Override
                public void run() {
                    l.countDown();
                }

            }
        }
        Set<Future<String>> s = new HashSet<Future<String>>();
        Set<R> rs = new HashSet<R>();
        for (int i = 0; i < count; i++) {
            String currName = "Runnable " + i;
            R r = new R();
            rs.add(r);
            s.add(rp.submit(r, currName));
        }
        waitAllLaunched.await();
        synchronized (lock) {
            //Notify just one thread
            lock.notify();
        }
        waitOneFinished.await();
        List<Runnable> notRun = rp.shutdownNow();
        synchronized (lock) {
            lock.notifyAll();
        }
        boolean allFinished = true;
        int finishedCount = 0;
        for (R r : rs) {
            assertTrue(r.hasStarted);
            allFinished &= r.hasFinished;
            if (r.hasFinished) {
                finishedCount++;
            }
        }
        assertFalse("All tasks should not have completed", allFinished);
        assertTrue("At least one task shall complete", finishedCount >= 1);
        assertTrue(notRun.isEmpty());
        assertTrue(rp.isShutdown());
        //Technically not provable due to "spurious wakeups"
        //        assertEquals (1, finishedCount);

        try {
            RequestProcessor.getDefault().shutdown();
            fail("Should not be able to shutdown() default RP");
        } catch (Exception e) {
        }
        try {
            RequestProcessor.getDefault().shutdownNow();
            fail("Should not be able to shutdownNow() default RP");
        } catch (Exception e) {
        }
    }

    public void testAwaitTermination() throws Exception {
        int count = 20;
        final Object lock = new Object();
        final CountDownLatch waitAllLaunched = new CountDownLatch(count);
        final CountDownLatch waitAll = new CountDownLatch(count);
        final RequestProcessor rp = new RequestProcessor("TestRP", count);
        class R implements Runnable {

            volatile boolean hasStarted;
            volatile boolean hasFinished;

            @Override
            public void run() {
                hasStarted = true;
                waitAllLaunched.countDown();
                synchronized (lock) {
                    try {
                        lock.wait();
                        if (Thread.interrupted()) {
                            return;
                        }
                    } catch (InterruptedException ex) {
                        return;
                    } finally {
                        hasFinished = true;
                        waitAll.countDown();
                    }
                }
            }
        }
        Set<Future<String>> s = new HashSet<Future<String>>();
        Set<R> rs = new HashSet<R>();
        for (int i = 0; i < count; i++) {
            String currName = "Runnable " + i;
            R r = new R();
            rs.add(r);
            s.add(rp.submit(r, currName));
        }
        waitAllLaunched.await();
        synchronized (lock) {
            //Notify just one thread
            lock.notifyAll();
        }
        rp.shutdown();
        boolean awaitTermination = rp.awaitTermination(1, TimeUnit.DAYS);
        assertTrue(awaitTermination);
        assertTrue(rp.isShutdown());
        assertTrue(rp.isTerminated());
    }

    @RandomlyFails
    public void testAwaitTerminationWaitsForNewlyAddedThreads() throws Exception {
        final RequestProcessor rp = new RequestProcessor("testAwaitTerminationWaitsForNewlyAddedThreads", 50, false);
        int count = 30;
        final CountDownLatch waitLock = new CountDownLatch(1);
        class R implements Runnable {
            boolean done;
            @Override
            public void run() {
                try {
                    waitLock.await();
                } catch (InterruptedException ex) {
                    done = true;
                } finally {
                    done = true;
                }
            }
        }
        Set<R> rs = new HashSet<R>();
        for (int i= 0; i < count; i++) {
            R r = new R();
            rs.add(r);
            rp.submit(r);
        }
        final CountDownLatch shutdownBegun = new CountDownLatch(1);
        Runnable shutdowner = new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                    rp.shutdown();
                    shutdownBegun.countDown();
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        };
        waitLock.countDown();
        new Thread(shutdowner).start();
        assertTrue(rp.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS));
        Thread.sleep (600);
        assertTrue (rp.isTerminated());
    }

    public void testInvokeAll() throws Exception {
        int count = 20;
        final CountDownLatch waitAll = new CountDownLatch(count);
        final RequestProcessor notificationThread = new RequestProcessor("notifier", 1, true, true);
        final RequestProcessor rp = new RequestProcessor("TestRP", count);
        try {
            class C implements Callable<String>, Runnable {

                private final String result;
                volatile boolean ran;

                C(String result) {
                    this.result = result;
                }

                @Override
                public String call() throws Exception {
                    ran = true;
                    //#182637 - the waiting thread can be notified before
                    //the Done flag on this runnable's future has been set,
                    //so ensure this thread's runnable has time to exit
                    //before we do the notification
                    notificationThread.create(this).schedule (20);
                    return result;
                }

                @Override
                public void run() {
                    waitAll.countDown();
                }
            }
            List<C> callables = new ArrayList<C>(count);
            List<Future<String>> fs;
            Set<String> names = new HashSet<String>(count);
            for (int i = 0; i < count; i++) {
                String name = "R" + i;
                names.add(name);
                C c = new C(name);
                callables.add(c);
            }
            fs = rp.invokeAll(callables);

            assertNotNull(fs);
            waitAll.await();
            assertEquals(0, waitAll.getCount());
            for (Future<String> f : fs) {
                assertTrue (f.isDone());
            }
            for (C c : callables) {
                assertTrue (c.ran);
            }
            Set<String> s = new HashSet<String>(count);
            for (Future<String> f : fs) {
                s.add(f.get());
            }
            assertEquals(names, s);
        } finally {
            rp.stop();
        }
    }

    public void testInvokeAllWithTimeout() throws Exception {
        int count = 20;
        final CountDownLatch blocker = new CountDownLatch(1);
        final RequestProcessor rp = new RequestProcessor("TestRP", count);
        try {
            class C implements Callable<String> {
                volatile boolean iAmSpecial;

                private final String result;
                volatile boolean ran;

                C(String result) {
                    this.result = result;
                }

                @Override
                public String call() throws Exception {
                    //Only one will be allowed to run, the rest
                    //will be cancelled
                    if (!iAmSpecial) {
                        blocker.await();
                    }
                    ran = true;
                    return result;
                }
            }
            List<C> callables = new ArrayList<C>(count);
            C special = new C("Special");
            special.iAmSpecial = true;
            callables.add(special);
            List<Future<String>> fs;
            Set<String> names = new HashSet<String>(count);
            for (int i = 0; i < count; i++) {
                String name = "R" + i;
                names.add(name);
                C c = new C(name);
                callables.add(c);
            }
            fs = rp.invokeAll(callables, 1000, TimeUnit.MILLISECONDS);
            assertNotNull(fs);
            for (Future<String> f : fs) {
                assertTrue (f.isDone());
            }
            for (C c : callables) {
                if (c == special) {
                    assertTrue (c.ran);
                } else {
                    assertFalse(c.ran);
                }
            }
        } finally {
            rp.stop();
        }
    }

    public void testInvokeAllSingleThread() throws Exception {
        int count = 20;
        final CountDownLatch waitAll = new CountDownLatch(count);
        final RequestProcessor rp = new RequestProcessor("TestRP", 1);
        class C implements Callable<String> {

            private final String result;

            C(String result) {
                this.result = result;
            }

            @Override
            public String call() throws Exception {
                waitAll.countDown();
                return result;
            }
        }
        List<C> l = new ArrayList<C>(count);
        List<Future<String>> fs;
        Set<String> names = new HashSet<String>(count);
        for (int i = 0; i < count; i++) {
            String name = "R" + i;
            names.add(name);
            C c = new C(name);
            l.add(c);
        }
        fs = rp.invokeAll(l);
        assertNotNull(fs);
        Set<String> s = new HashSet<String>(count);
        for (Future<String> f : fs) {
            s.add(f.get());
        }
        assertEquals(names, s);
    }

    @RandomlyFails // NB-Core-Build #4165: res==null
    public void testInvokeAny() throws Exception {
        int count = 20;
        final RequestProcessor rp = new RequestProcessor("TestRP", count + 1);
        class C implements Callable<String> {

            private final String result;

            C(String result) {
                this.result = result;
            }

            @Override
            public String call() throws Exception {
                if (Thread.interrupted()) {
                    return null;
                }
                return result;
            }
        }
        List<C> l = new ArrayList<C>(count);
        Set<String> names = new HashSet<String>(count);
        for (int i = 0; i < count; i++) {
            String name = "R" + i;
            names.add(name);
            C c = new C(name);
            l.add(c);
        }
        String res = rp.invokeAny(l);
        assertNotNull(res);
        assertTrue(res.startsWith("R"));
    }

    public void testInvokeAnySingleThread() throws Exception {
        int count = 1000;
        final RequestProcessor rp = new RequestProcessor("TestRP", 20);
        final CountDownLatch latch = new CountDownLatch(count);
        final Set<Thread> ts = Collections.synchronizedSet(new HashSet<Thread>());
        class C implements Callable<String> {

            volatile boolean hasRun;
            private final String name;

            C(String name) {
                this.name = name;
            }

            @Override
            public String call() throws Exception {
                latch.countDown();
                if (!"R17".equals(name)) {
                    //Block all but one thread until threads have entered
                    Thread.currentThread().suspend();
                }
                hasRun = true;
                return name;
            }
        }
        List<C> l = new ArrayList<C>(count);
        Set<String> names = new HashSet<String>(count);
        for (int i = 0; i < count; i++) {
            String name = "R" + i;
            names.add(name);
            C c = new C(name);
            l.add(c);
        }
        String res = rp.invokeAny(l);
        assertNotNull(res);
        assertTrue(res.startsWith("R"));
        int runCount = 0;
        for (C c : l) {
            if (c.hasRun) {
                runCount++;
            }
        }
        assertTrue("Not all " + count + " threads should have completed, but " + runCount + " did.", runCount < count);
        for (Thread t : ts) {
            t.resume();
        }
    }

    public void testInvokeAnyWithTimeout() throws Exception {
        int count = 20;
        final RequestProcessor rp = new RequestProcessor("TestRP", count + 1);
        final CountDownLatch latch = new CountDownLatch(1);
        class C implements Callable<String> {

            volatile boolean hasRun;
            private final String result;

            C(String result) {
                this.result = result;
            }

            @Override
            public String call() throws Exception {
                latch.await();
                if (Thread.interrupted()) {
                    return null;
                }
                hasRun = true;
                return result;
            }
        }
        List<C> l = new ArrayList<C>(count);
        Set<String> names = new HashSet<String>(count);
        for (int i = 0; i < count; i++) {
            String name = "R" + i;
            names.add(name);
            C c = new C(name);
            l.add(c);
        }
        //All threads are waiting on latch;  we should time out
        String res = rp.invokeAny(l, 400, TimeUnit.MILLISECONDS);
        assertNull(res);
        for (C c : l) {
            assertFalse(c.hasRun);
        }
    }

    public void testCancellation() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        class C implements Callable<String> {

            volatile boolean hasRun;
            volatile boolean interrupted;

            @Override
            public String call() throws Exception {
                try {
                    latch.await();
                } catch (InterruptedException e) {
                    interrupted = true;
                    return null;
                }
                if (Thread.interrupted()) {
                    interrupted = true;
                    return null;
                }
                hasRun = true;
                return "Hello";
            }
        }
        C c = new C();
        Future<String> f = RequestProcessor.getDefault().submit(c);
        f.cancel(true);
        latch.countDown();
        String s = null;
        try {
            s = f.get();
            fail("CancellationException should have been thrown");
        } catch (CancellationException e) {
        }
        assertNull(s);
        assertTrue(c.interrupted || !c.hasRun);
        assertFalse(c.hasRun);
    }

    public void testCancellablesGetCancelInvokedWithCallable() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final CountDownLatch exit = new CountDownLatch(1);
        class C implements Callable<String>, Cancellable {

            volatile boolean hasRun;
            volatile boolean interrupted;
            volatile boolean cancelled;

            @Override
            public String call() throws Exception {
                try {
                    try {
                        latch.await();
                    } catch (InterruptedException e) {
                        interrupted = true;
                        return null;
                    }
                    if (Thread.interrupted()) {
                        interrupted = true;
                        return null;
                    }
                    if (cancelled) {
                        return null;
                    }
                    hasRun = true;
                    return "Hello";
                } finally {
                    exit.countDown();
                }
            }

            @Override
            public boolean cancel() {
                cancelled = true;
                exit.countDown();
                return true;
            }
        }
        C c = new C();
        Future<String> f = RequestProcessor.getDefault().submit(c);
        f.cancel(true);
        assertTrue (c.cancelled);
        latch.countDown();
        exit.await();
        String s = null;
        try {
            s = f.get();
            fail ("Should have gotten cancellation exception");
        } catch (CancellationException e) {

        }
    }

    public void testCancellablesGetCancelInvokedWithRunnable() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final CountDownLatch exit = new CountDownLatch(1);
        class C implements Runnable, Cancellable {

            volatile boolean hasRun;
            volatile boolean interrupted;
            volatile boolean cancelled;

            @Override
            public void run() {
                try {
                    try {
                        latch.await();
                    } catch (InterruptedException e) {
                        interrupted = true;
                        return;
                    }
                    if (Thread.interrupted()) {
                        interrupted = true;
                        return;
                    }
                    if (cancelled) {
                        return;
                    }
                    hasRun = true;
                } finally {
                    exit.countDown();
                }
            }

            @Override
            public boolean cancel() {
                cancelled = true;
                exit.countDown();
                return true;
            }
        }
        C c = new C();
        Future<?> f = RequestProcessor.getDefault().submit(c);
        f.cancel(true);
        assertTrue (c.cancelled);
        latch.countDown();
        exit.await();
        try {
            f.get();
            fail ("Should have gotten cancellation exception");
        } catch (CancellationException e) {

        }
        assertFalse (c.hasRun);
    }

    public void testCancellablesThatSayTheyCantBeCancelledAreNotCancelledViaFutureDotCancel() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final CountDownLatch exit = new CountDownLatch(1);
        class C implements Runnable, Cancellable {

            volatile boolean hasRun;
            volatile boolean interrupted;
            volatile boolean cancelCalled;

            @Override
            public void run() {
                try {
                    try {
                        latch.await();
                    } catch (InterruptedException e) {
                        interrupted = true;
                        throw new AssertionError(e);
                    }
                    if (Thread.interrupted()) {
                        interrupted = true;
                        throw new AssertionError("Thread should not have been interrupted");
                    }
                    hasRun = true;
                } finally {
                    exit.countDown();
                }
            }

            @Override
            public boolean cancel() {
                cancelCalled = true;
                return false;
            }
        }
        C c = new C();
        Future<?> f = RequestProcessor.getDefault().submit(c);
        f.cancel(true);
        assertFalse (f.isCancelled());
        assertTrue (c.cancelCalled);
        latch.countDown();
        exit.await();
        f.get();
        assertFalse (f.isCancelled());
        assertTrue (c.hasRun);
    }

    public void testInvokeAllCancellation() throws Exception {
        int count = 20;
        final CountDownLatch waitAll = new CountDownLatch(count);
        final RequestProcessor rp = new RequestProcessor("TestRP", count);
        class C implements Callable<String>, Cancellable {

            private final String result;
            volatile boolean cancelCalled;

            C(String result) {
                this.result = result;
            }

            @Override
            public String call() throws Exception {
                waitAll.countDown();
                return cancelCalled ? null : result;
            }

            @Override
            public boolean cancel() {
                cancelCalled = true;
                return false;
            }
        }
        List<C> l = new ArrayList<C>(count);
        List<Future<String>> fs;
        Set<String> names = new HashSet<String>(count);
        for (int i = 0; i < count; i++) {
            String name = "R" + i;
            names.add(name);
            C c = new C(name);
            l.add(c);
        }
        fs = rp.invokeAll(l);
        assertNotNull(fs);
        Set<String> s = new HashSet<String>(count);
        for (Future<String> f : fs) {
            s.add(f.get());
        }
        assertEquals(names, s);
    }

    public void testCannotScheduleLongerThanIntegerMaxValue() throws Exception {
        Runnable r = new Runnable() {

            @Override
            public void run() {
                fail ("Should not have been run");
            }
        };
        try {
            Future<?> f = RequestProcessor.getDefault().schedule(r, Long.MAX_VALUE, TimeUnit.DAYS);
            f.cancel(true);
        } catch (Exception e) {}
    }

    public void testCannotScheduleNegativeDelay() throws Exception {
        Runnable r = new Runnable() {

            @Override
            public void run() {
                fail ("Should not have been run");
            }
        };
        try {
            RequestProcessor.getDefault().schedule(r, -1L, TimeUnit.MILLISECONDS);
            fail ("Negative value accepetd");
        } catch (Exception e) {}
        try {
            RequestProcessor.getDefault().scheduleAtFixedRate(r, -1L, 22L, TimeUnit.MILLISECONDS);
            fail ("Negative value accepetd");
        } catch (Exception e) {}
        try {
            RequestProcessor.getDefault().scheduleAtFixedRate(r, 200, -22L, TimeUnit.MILLISECONDS);
            fail ("Negative value accepetd");
        } catch (Exception e) {}
        try {
            RequestProcessor.getDefault().scheduleWithFixedDelay(r, -1L, 22L, TimeUnit.MILLISECONDS);
            fail ("Negative value accepetd");
        } catch (Exception e) {}
        try {
            RequestProcessor.getDefault().scheduleWithFixedDelay(r, 1L, -22L, TimeUnit.MILLISECONDS);
            fail ("Negative value accepetd");
        } catch (Exception e) {}
    }

    public void testTaskCanRescheduleItself() throws Exception {
        final CountDownLatch latch = new CountDownLatch(2);
        class R implements Runnable {
            volatile RequestProcessor.Task task;
            volatile int runCount;
            @Override
            public void run() {
                runCount++;
                if (runCount == 1) {
                    task.schedule(0);
                }
                latch.countDown();
            }
        }
        R r = new R();
        RequestProcessor.Task t = RequestProcessor.getDefault().create(r);
        r.task = t;
        t.schedule(0);
        latch.await ();
        assertEquals (r.runCount, 2);
    }

    public void testScheduleRepeatingSanityFixedRate() throws Exception {
        final CountDownLatch latch = new CountDownLatch(5);
        class C implements Runnable {
            volatile int runCount;
            @Override
            public void run() {
                runCount++;
                latch.countDown();
                if (latch.getCount() <= 0) {
                    waitABitToGiveMainThreadChanceToRun();
                }
            }
            private void waitABitToGiveMainThreadChanceToRun() {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        C c = new C();
        RequestProcessor.getDefault().scheduleWithFixedDelay(c, 0, 200, TimeUnit.MILLISECONDS);
//        latch.await(5000, TimeUnit.MILLISECONDS);
        latch.await();
        assertAtLeast (5, c.runCount);
    }

    public void testScheduleRepeatingSanityFixedDelay() throws Exception {
        final CountDownLatch latch = new CountDownLatch(5);
        class C implements Runnable {
            volatile int runCount;
            @Override
            public void run() {
                runCount++;
                latch.countDown();
                waitABitToGiveMainThreadChanceToRun();
            }

            private void waitABitToGiveMainThreadChanceToRun() {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        C c = new C();
        RequestProcessor.getDefault().scheduleAtFixedRate(c, 0, 200, TimeUnit.MILLISECONDS);
        latch.await(2000, TimeUnit.MILLISECONDS);

        assertAtLeast (5, c.runCount);
    }

    public void testScheduleOneShot() throws Exception {
        RequestProcessor rp = new RequestProcessor ("testScheduleOneShot", 5, true, true);
        try {
            class C implements Callable<String> {
                volatile long start = System.currentTimeMillis();
                private volatile long end;

                @Override
                public String call() throws Exception {
                    synchronized(this) {
                        end = System.currentTimeMillis();
                    }
                    return "Hello";
                }

                synchronized long elapsed() {
                    return end - start;
                }
            }
            C c = new C();
            long delay = 5000;
            //Use a 20 second timeout to have a reasonable chance of accuracy
            ScheduledFuture<String> f = rp.schedule(c, delay * 1000, TimeUnit.MICROSECONDS);
            assertEquals (5000, f.getDelay(TimeUnit.MILLISECONDS));
            assertNotNull(f.get());
            //Allow 4 seconds fudge-factor
            assertTrue (c.elapsed() > 4600);
            assertTrue (f.isDone());
        } finally {
            rp.stop();
        }
    }

    @RandomlyFails // NB-Core-Build #8322: hung
    public void testScheduleRepeatingIntervalsAreRoughlyCorrect() throws Exception {
        int runCount = 5;
        final CountDownLatch latch = new CountDownLatch(runCount);
        final List<Long> intervals = Collections.synchronizedList(new ArrayList<Long> (runCount));
//        long initialDelay = 30000;
//        long period = 20000;
//        long fudgeFactor = 4000;
        long initialDelay = 3000;
        long period = 2000;
        long fudgeFactor = 400;
        long expectedInitialDelay = initialDelay - fudgeFactor;
        long expectedPeriod = period - fudgeFactor;
        class C implements Runnable {
            volatile long start = System.currentTimeMillis();
            private int runCount;
            @Override
            public void run() {
                runCount++;
                try {
                    synchronized(this) {
                        long end = System.currentTimeMillis();
                        intervals.add (end - start);
                        start = end;
                    }
                } finally {
                    latch.countDown();
                }
            }
        }
        C c = new C();
        RequestProcessor rp = new RequestProcessor ("testScheduleRepeating", 5, true);
        try {
            Future<?> f = rp.scheduleWithFixedDelay(c, initialDelay, period, TimeUnit.MILLISECONDS);
    //        latch.await(initialDelay + fudgeFactor + ((runCount - 1) * (period + fudgeFactor)), TimeUnit.MILLISECONDS); //XXX
            latch.await();
            f.cancel(true);
            for (int i= 0; i < Math.min(runCount, intervals.size()); i++) {
                long expect = i == 0 ? expectedInitialDelay : expectedPeriod;
                assertTrue ("Expected at least " + expect + " milliseconds before run " + i + " but was " + intervals.get(i), intervals.get(i) >= expect);
            }
            //Ensure we have really exited
            try {
                f.get();
                fail ("CancellationException should have been thrown");
            } catch (CancellationException e) {}
            assertTrue(f.isCancelled());
            assertTrue(f.isDone());
        } finally {
            rp.stop();
        }
    }

    @RandomlyFails
    public void testScheduleFixedRateAreRoughlyCorrect() throws Exception {
        if (!TaskTest.canWait1s()) {
            LOG.warning("Skipping testWaitWithTimeOutReturnsAfterTimeOutWhenTheTaskIsNotComputedAtAll, as the computer is not able to wait 1s!");
            return;
        }
        int runCount = 5;
        final CountDownLatch latch = new CountDownLatch(runCount);
        final List<Long> intervals = Collections.synchronizedList(new ArrayList<Long> (runCount));
//        long initialDelay = 30000;
//        long period = 20000;
//        long fudgeFactor = 4000;
        long initialDelay = 3000;
        long period = 2000;
        long fudgeFactor = 400;
        long expectedInitialDelay = initialDelay - fudgeFactor;
        long expectedPeriod = period - fudgeFactor;
        class C implements Runnable {
            volatile long start = System.currentTimeMillis();
            private int runCount;
            @Override
            public void run() {
                runCount++;
                try {
                    synchronized(this) {
                        long end = System.currentTimeMillis();
                        intervals.add (end - start);
                        start = end;
                    }
                } finally {
                    latch.countDown();
                }
            }
        }
        C c = new C();
        RequestProcessor rp = new RequestProcessor ("testScheduleFixedRateAreRoughlyCorrect", 5, true);
        try {
            Future<?> f = rp.scheduleAtFixedRate(c, initialDelay, period, TimeUnit.MILLISECONDS);
            latch.await();
            f.cancel(true);
            StringBuilder failures = new StringBuilder();
            failures.append("Expected at least ").append(expectedInitialDelay).
                append(" milliseconds before run:\n");
            boolean fail = false;
            for (int i= 0; i < Math.min(runCount, intervals.size()); i++) {
                long expect = i == 0 ? expectedInitialDelay : expectedPeriod;
                failures.append("Round ").append(i).
                    append(" expected delay ").append(expect).
                    append(" but was ").append(intervals.get(i)).
                    append("\n");
                if (intervals.get(i) < expect) {
                    fail = true;
                }
            }
            if (fail) {
                fail(failures.toString());
            }
            //Ensure we have really exited
            try {
                f.get();
                fail ("CancellationException should have been thrown");
            } catch (CancellationException e) {}
            assertTrue(f.isCancelled());
            assertTrue(f.isDone());
        } finally {
            rp.stop();
        }
    }

    public void testScheduleFixedRateOnMultiThreadPoolDoesNotCauseConcurrentExecution() throws Exception {
        final AtomicInteger val = new AtomicInteger(0);
        final CountDownLatch latch = new CountDownLatch(10);
        class C implements Runnable {
            boolean failed;
            @Override
            public void run() {
                try {
                    int now = val.incrementAndGet();
                    if (now > 1) {
                        failed = true;
                        fail (now + " threads simultaneously in run()");
                    }
                    try {
                        //Intentionally sleep *longer* than the interval
                        //between executions.  We *want* to pile up all of the
                        //RP threads entering run() - synchronization should
                        //serialize them.  This test is to prove that this
                        //method will never be called concurrently from two threads
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {

                    }
                } finally {
                    val.decrementAndGet();
                    latch.countDown();
                }
            }
        }
        C c = new C();
        long initialDelay = 2000;
        long period = 10;
        RequestProcessor rp = new RequestProcessor("testScheduleFixedRateOnMultiThreadPoolDoesNotCauseConcurrentExecution", 10, true);
        rp.scheduleAtFixedRate(c, initialDelay, period, TimeUnit.MILLISECONDS);
        latch.await();
        assertFalse(c.failed);
        rp.stop();
    }

    @RandomlyFails
    public void testScheduleFixedRateWithShorterIntervalThanRunMethodTimeAreNotDelayed() throws Exception {
        final CountDownLatch latch = new CountDownLatch(10);
        final List<Long> intervals = new CopyOnWriteArrayList<Long>();
        class C implements Runnable {
            long start = Long.MIN_VALUE;

            @Override
            public void run() {
                long end = System.currentTimeMillis();
                if (start != Long.MIN_VALUE) {
                    intervals.add(end - start);
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    
                }
                start = System.currentTimeMillis();
                latch.countDown();
            }
        }
        C c = new C();
        long initialDelay = 100;
        long period = 100;
        RequestProcessor rp = new RequestProcessor("testScheduleFixedRateWithShorterIntervalThanRunMethodTimeAreNotDelayed", 10, true);
        ScheduledFuture<?> f = rp.scheduleAtFixedRate(c, initialDelay, period, TimeUnit.MILLISECONDS);
        latch.await();
        f.cancel(true);
        rp.stop();
        int max = intervals.size();
        for (int i= 0; i < max; i++) {
            long iv = intervals.get(i);
            assertFalse ("Interval " + i + " should have been at least less than requested interval * 1.5 with fixed rate" + iv, iv > 150);
        }
    }

    public void testCancelFutureInterruptsThreadEvenIfRequestProcessorForbidsIt() throws Exception {
        RequestProcessor rp = new RequestProcessor ("X", 3, false, true);
        final CountDownLatch releaseForRun = new CountDownLatch(1);
        final CountDownLatch enterLatch = new CountDownLatch(1);
        final CountDownLatch exitLatch = new CountDownLatch(1);
        class R implements Runnable {
            volatile boolean interrupted;
            @Override
            public void run() {
                enterLatch.countDown();
                try {
                    releaseForRun.await();
                } catch (InterruptedException ex) {
                    interrupted = true;
                }
                interrupted |= Thread.interrupted();
                exitLatch.countDown();
            }
        }
        R r = new R();
        Future<?> f = rp.submit(r);
        enterLatch.await();
        f.cancel(true);
        assertTrue (f.isCancelled());
        exitLatch.await();
        assertTrue (r.interrupted);
    }

    public void testCancelDoesNotInterruptIfNotPassedToFutureDotCancel() throws Exception {
        RequestProcessor rp = new RequestProcessor ("X", 3, false, true);
        final CountDownLatch releaseForRun = new CountDownLatch(1);
        final CountDownLatch enterLatch = new CountDownLatch(1);
        final CountDownLatch exitLatch = new CountDownLatch(1);
        class R implements Runnable {
            volatile boolean interrupted;
            @Override
            public void run() {
                enterLatch.countDown();
                try {
                    releaseForRun.await();
                } catch (InterruptedException ex) {
                    interrupted = true;
                }
                interrupted |= Thread.interrupted();
                exitLatch.countDown();
            }
        }
        R r = new R();
        Future<?> f = rp.submit(r);
        enterLatch.await();
        f.cancel(false);
        assertTrue (f.isCancelled());
        assertFalse (r.interrupted);
    }

    public void testSubmittedTasksExecutedBeforeShutdown() throws InterruptedException {
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch executedLatch = new CountDownLatch(2);
        Runnable dummyRunnable = new Runnable() {

            @Override
            public void run() {
                try {
                    startLatch.await();
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } finally {
                    executedLatch.countDown();
                }
            }
        };
        
        RequestProcessor rp = new RequestProcessor("X", 1);
        rp.submit(dummyRunnable);
        rp.submit(dummyRunnable);
        rp.shutdown();
        startLatch.countDown();
        
        assertTrue("Submitted tasks not executed", executedLatch.await(5, TimeUnit.SECONDS));
    }
    
    public void testExecutingTasksNotInterruptedOnShutdown() throws InterruptedException {
        final CountDownLatch startLatch = new CountDownLatch(2);
        final CountDownLatch blockingLatch = new CountDownLatch(1);
        final CountDownLatch executedLatch = new CountDownLatch(1);
        final AtomicBoolean interrupted = new AtomicBoolean(false);
        Runnable dummyRunnable = new Runnable() {

            @Override
            public void run() {
                try {
                    startLatch.countDown();
                    startLatch.await();
                    blockingLatch.await();
                } catch (InterruptedException ex) {
                    interrupted.set(true);
                    Thread.currentThread().interrupt();
                } finally {
                    executedLatch.countDown();
                }
            }
        };
        
        RequestProcessor rp = new RequestProcessor("X", 1);
        rp.submit(dummyRunnable);
        startLatch.countDown();
        try {
            startLatch.await();
        } catch (InterruptedException ex) {
            Exceptions.printStackTrace(ex);
        }
        rp.shutdown();
        blockingLatch.countDown();
        executedLatch.await();
        assertFalse("Executing tasks interrupted", interrupted.get());
    }
    
    public void testAwaitingTasksReturnedOnShutdownNow() throws InterruptedException {
        final CountDownLatch startupLatch = new CountDownLatch(2);
        final CountDownLatch blockingLatch = new CountDownLatch(1);
        Runnable blockingRunnable = new Runnable() {

            @Override
            public void run() {
                try {
                    startupLatch.countDown();
                    startupLatch.await();
                    blockingLatch.await();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        };
        
        Runnable awaitingRunnable = new Runnable() {

            @Override
            public void run() {
                // noop
            }
        };
        
        RequestProcessor rp = new RequestProcessor("X", 1);
        rp.submit(blockingRunnable);
        startupLatch.countDown();
        startupLatch.await();
        rp.submit(awaitingRunnable);
        Set<Runnable> awaiting = new HashSet<Runnable>(rp.shutdownNow());
        assertTrue("Awaiting task not returned on shutdownNow()", awaiting.contains(awaitingRunnable));
        assertFalse("Running task returned on shutdownNow()", awaiting.contains(blockingRunnable));
    }    

    private static void assertAtLeast(int exp, int real) {
        if (exp > real) {
            fail("Expecting at least " + exp + " but was only " + real);
        }
    }
}
