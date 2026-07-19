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
package org.netbeans.lib.profiler.results.cpu;

import java.lang.Thread.State;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Collections;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.netbeans.lib.profiler.filters.InstrumentationFilter;
import org.netbeans.lib.profiler.results.CCTNode;
import static org.junit.Assert.*;


/**
 *
 * @author Jaroslav Bachorik, Tomas Hurka
 */
public class StackTraceSnapshotBuilderTest {

    private StackTraceSnapshotBuilder instance;

    private final StackTraceElement[] elements0 = new StackTraceElement[] {
        new StackTraceElement("test.Class1", "method3", "Class1.java", 30),
        new StackTraceElement("test.Class1", "method2", "Class1.java", 20),
        new StackTraceElement("test.Class1", "method1", "Class1.java", 10)
    };

    private final StackTraceElement[] elementsDif = new StackTraceElement[] {
        new StackTraceElement("test.Class1", "method3", "Class1.java", 40),
        new StackTraceElement("test.Class1", "method4", "Class1.java", 30),
        new StackTraceElement("test.Class1", "method2", "Class1.java", 20),
        new StackTraceElement("test.Class1", "method1", "Class1.java", 10)
    };

    private final StackTraceElement[] elementsPlus = new StackTraceElement[] {
        new StackTraceElement("test.Class1", "method4", "Class1.java", 40),
        new StackTraceElement("test.Class1", "method3", "Class1.java", 30),
        new StackTraceElement("test.Class1", "method2", "Class1.java", 20),
        new StackTraceElement("test.Class1", "method1", "Class1.java", 10)
    };

    private final StackTraceElement[] elementsMinus = new StackTraceElement[] {
        new StackTraceElement("test.Class1", "method2", "Class1.java", 20),
        new StackTraceElement("test.Class1", "method1", "Class1.java", 10)
    };

    private final StackTraceElement[] elementsDup = new StackTraceElement[] {
        new StackTraceElement("test.Class1", "method3", "Class1.java", 30),
        new StackTraceElement("test.Class1", "method2", "Class1.java", 21),
        new StackTraceElement("test.Class1", "method1", "Class1.java", 10)
    };

    private Thread thread0;
    private Thread thread1;
    private Thread thread2;

    private ThreadSample[] stack0;
    private ThreadSample[] stackPlus;
    private ThreadSample[] stackMinus;
    private ThreadSample[] stackDif;
    private ThreadSample[] stackDup;
    

    public StackTraceSnapshotBuilderTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
        instance = new StackTraceSnapshotBuilder();

        thread0 = new Thread("Test thread 0");
        thread1 = new Thread("Test thread 1");
        thread2 = new Thread("Test thread 2");
        
        stack0 = new ThreadSample[] {
                createThreadSample(thread0, elements0),
                createThreadSample(thread1, elements0)
        };

        stackPlus = new ThreadSample[] {
                createThreadSample(thread0, elementsPlus),
                createThreadSample(thread1, elements0),
                createThreadSample(thread2, elements0)
        };

        stackMinus = new ThreadSample[] {
                createThreadSample(thread0, elementsMinus)
        };

        stackDif = new ThreadSample[] {
                createThreadSample(thread0, elementsDif)
        };

        stackDup = new ThreadSample[] {
                createThreadSample(thread0, elementsDup),
                createThreadSample(thread1, elements0)
        };

    }

    @After
    public void tearDown() {
        instance = null;
    }

    /**
     * Test of createSnapshot method, of class StackTraceSnapshotBuilder.
     * Empty data
     */
    @Test
    public void testCreateSnapshotEmpty() {
        System.out.println("create snapshot : empty");

        try {
            instance.createSnapshot(System.currentTimeMillis());
            fail("Attempt to create an empty snapshot should throw NoDataAvailableException");
        } catch (CPUResultsSnapshot.NoDataAvailableException ex) {
        }
    }

    @Test
    public void testCreateSnapshotOneSample() throws CPUResultsSnapshot.NoDataAvailableException {
        System.out.println("create snapshot : one sample");

        addStacktrace(stack0, 0);
        CPUResultsSnapshot snapshot = instance.createSnapshot(System.currentTimeMillis());
        assertTrue(snapshot.collectingTwoTimeStamps);
        assertEquals(instance.methodInfos.size(), snapshot.nInstrMethods);
    }

    @Test
    public void testCreateSnapshotNoChanges() throws CPUResultsSnapshot.NoDataAvailableException {
        System.out.println("create snapshot : two samples");

        addStacktrace(stack0, 0);
        addStacktrace(stack0, 500000);

        CPUResultsSnapshot snapshot = instance.createSnapshot(System.currentTimeMillis());
        assertTrue(snapshot.collectingTwoTimeStamps);
        assertEquals(instance.methodInfos.size(), snapshot.nInstrMethods);
    }

    @Test
    public void testCreateSnapshotMinus() throws CPUResultsSnapshot.NoDataAvailableException {
        System.out.println("create snapshot : minus");

        addStacktrace(stack0, 0);
        addStacktrace(stackMinus, 500000);

        CPUResultsSnapshot snapshot = instance.createSnapshot(System.currentTimeMillis());
        assertTrue(snapshot.collectingTwoTimeStamps);
        assertEquals(instance.methodInfos.size(), snapshot.nInstrMethods);
    }

    @Test
    public void testCreateSnapshotPlus() throws CPUResultsSnapshot.NoDataAvailableException {
        System.out.println("create snapshot : plus");

        addStacktrace(stack0, 0);
        addStacktrace(stackPlus, 500000);

        CPUResultsSnapshot snapshot = instance.createSnapshot(System.currentTimeMillis());
        assertTrue(snapshot.collectingTwoTimeStamps);
        assertEquals(instance.methodInfos.size(), snapshot.nInstrMethods);
    }

    @Test
    public void testCreateSnapshotPlusMinus() throws CPUResultsSnapshot.NoDataAvailableException {
        System.out.println("create snapshot : plus->minus");

        addStacktrace(stack0, 0);
        addStacktrace(stackPlus, 500000);
        addStacktrace(stackMinus, 1000000);

        CPUResultsSnapshot snapshot = instance.createSnapshot(System.currentTimeMillis());
        assertTrue(snapshot.collectingTwoTimeStamps);
        assertEquals(instance.methodInfos.size(), snapshot.nInstrMethods);
    }

    @Test
    public void testCreateSnapshotMinusPlus() throws CPUResultsSnapshot.NoDataAvailableException {
        System.out.println("create snapshot : minus->plus");

        addStacktrace(stack0, 0);
        addStacktrace(stackMinus, 500000);
        addStacktrace(stackPlus, 1000000);

        CPUResultsSnapshot snapshot = instance.createSnapshot(System.currentTimeMillis());
        assertTrue(snapshot.collectingTwoTimeStamps);
        assertEquals(instance.methodInfos.size(), snapshot.nInstrMethods);
    }

    @Test
    public void testCreateSnapshotDup() throws CPUResultsSnapshot.NoDataAvailableException {
        System.out.println("create snapshot : dup");

        addStacktrace(stack0, 0);
        addStacktrace(stackDup, 500000);

        CPUResultsSnapshot snapshot = instance.createSnapshot(System.currentTimeMillis());
        assertTrue(snapshot.collectingTwoTimeStamps);
        assertEquals(instance.methodInfos.size(), snapshot.nInstrMethods);
        CPUCCTContainer container = snapshot.getContainerForThread((int) stack0[0].getThreadId(), CPUResultsSnapshot.METHOD_LEVEL_VIEW);
        assertEquals(container.getThreadName(),thread0.getName());
        PrestimeCPUCCTNode root = container.getRootNode();
        assertEquals(1, root.getNCalls());
        CCTNode[] childrens = root.getChildren();
        assertEquals(1, childrens.length);
        PrestimeCPUCCTNode ch = (PrestimeCPUCCTNode) childrens[0];
        assertEquals("test.Class1.method1()", ch.getNodeName());
        assertEquals(1, ch.getNCalls());
        CCTNode[] childrens1 = ch.getChildren();
        assertEquals(2, childrens1.length);
        PrestimeCPUCCTNode ch1 = (PrestimeCPUCCTNode) childrens1[0];
        if (ch1.isSelfTimeNode()) {
            ch1 = (PrestimeCPUCCTNode) childrens1[1];
        }
        assertEquals("test.Class1.method2()", ch1.getNodeName());
        assertEquals(1, ch1.getNCalls());
        CCTNode[] childrens2 = ch1.getChildren();
        assertEquals(2, childrens2.length);
        PrestimeCPUCCTNode ch2 = (PrestimeCPUCCTNode) childrens2[0];
        if (ch2.isSelfTimeNode()) {
            ch2 = (PrestimeCPUCCTNode) childrens2[1];
        }
        assertEquals("test.Class1.method3()", ch2.getNodeName());
        assertEquals(2, ch2.getNCalls());
    }

    @Test
    public void testAddStacktrace() {
        System.out.println("add stacktrace");

        addStacktrace(stack0, 0);
        assertTrue(instance.methodInfos.size()-1  == elements0.length);
        assertTrue(instance.threadIds.size() == stack0.length);
        assertTrue(instance.threadNames.size() == stack0.length);
        assertFalse(-1L == instance.currentDumpTimeStamp);
    }

    @Test
    public void testAddStacktraceThreadInfo() {
        ThreadMXBean tbean = ManagementFactory.getThreadMXBean();
        ThreadInfo tinfo = tbean.getThreadInfo(Thread.currentThread().getId(), Integer.MAX_VALUE);

        assertNotNull(tinfo);
        instance.addStacktrace(new ThreadInfo[] { tinfo }, 0);

        assertTrue(instance.threadIds.contains(tinfo.getThreadId()));
        assertTrue(instance.threadNames.contains(tinfo.getThreadName()));
        assertTrue(instance.lastStackTrace.get().containsKey(tinfo.getThreadId()));
    }

    @Test
    public void testAddStacktraceDuplicate() {
        System.out.println("add stacktrace : duplicate");

        long stamp = 0;
        addStacktrace(stack0, stamp);

        int miSize = instance.methodInfos.size()-1;
        int tIdSize = instance.threadIds.size();
        long timestamp = instance.currentDumpTimeStamp;

        addStacktrace(stack0, stamp);
        assertTrue(instance.stackTraceCount == 1);

    }

    @Test
    public void testAddStacktracePlus() {
        System.out.println("add stacktrace : plus");

        addStacktrace(stack0, 0);

        long timestamp = 500000;

        addStacktrace(stackPlus, timestamp);

        assertEquals(Math.max(stack0.length, stackPlus.length), instance.threadIds.size());
        assertEquals(Math.max(elements0.length, elementsPlus.length), instance.methodInfos.size()-1);
        assertEquals(timestamp, instance.currentDumpTimeStamp);
    }

    @Test
    public void testAddStacktracePlusWaiting() {
        System.out.println("add stacktrace : plus/waiting");

        addStacktrace(stack0, 0);

        long timestamp = 500000;
        setState(stackPlus[0],Thread.State.WAITING);

        addStacktrace(stackPlus, timestamp);

        assertEquals(Math.max(stack0.length, stackPlus.length), instance.threadIds.size());
        assertEquals(Math.max(elements0.length, elementsPlus.length), instance.methodInfos.size()-1);
        assertEquals(timestamp, instance.currentDumpTimeStamp);
    }

    @Test
    public void testAddStacktracePlusWaitingThread() {
        System.out.println("add stacktrace : plus/waiting; additional thread");

        addStacktrace(stack0, 0);

        long timestamp = 500000;
        setState(stackPlus[2],Thread.State.WAITING);

        addStacktrace(stackPlus, timestamp);

        assertEquals(Math.max(stack0.length, stackPlus.length), instance.threadIds.size());
        assertEquals(Math.max(elements0.length, elementsPlus.length), instance.methodInfos.size()-1);
        assertEquals(timestamp, instance.currentDumpTimeStamp);
    }

    @Test
    public void testAddStacktraceWaitingPlus() {
        System.out.println("add stacktrace : waiting/plus");

        setState(stack0[0],Thread.State.WAITING);
        addStacktrace(stack0, 0);

        long timestamp = 500000;
        setState(stackPlus[2],Thread.State.RUNNABLE);

        addStacktrace(stackPlus, timestamp);

        assertEquals(Math.max(stack0.length, stackPlus.length), instance.threadIds.size());
        assertEquals(Math.max(elements0.length, elementsPlus.length), instance.methodInfos.size()-1);
        assertEquals(timestamp, instance.currentDumpTimeStamp);
    }

    @Test
    public void testAddStacktraceMinus() {
        System.out.println("add stacktrace : minus");

        addStacktrace(stack0, 0);

        long timestamp = 500000;

        addStacktrace(stackMinus, timestamp);

        assertEquals(Math.max(stack0.length, stackMinus.length), instance.threadIds.size());
        assertEquals(Math.max(elements0.length, elementsMinus.length), instance.methodInfos.size()-1);
        assertEquals(timestamp, instance.currentDumpTimeStamp);
    }

    @Test
    public void testAddStacktraceMinusWaiting() {
        System.out.println("add stacktrace : minus/waiting");

        addStacktrace(stack0, 0);

        long timestamp = 500000;
        setState(stackMinus[0], Thread.State.WAITING);
        addStacktrace(stackMinus, timestamp);

        assertEquals(Math.max(stack0.length, stackMinus.length), instance.threadIds.size());
        assertEquals(Math.max(elements0.length, elementsMinus.length), instance.methodInfos.size()-1);
        assertEquals(timestamp, instance.currentDumpTimeStamp);
    }

    @Test
    public void testAddStacktraceMinusWaitingThread() {
        System.out.println("add stacktrace : minus/waiting; additional thread");

        setState(stack0[1], Thread.State.WAITING);
        addStacktrace(stack0, 0);

        long timestamp = 500000;
        setState(stackMinus[0], Thread.State.WAITING);
        addStacktrace(stackMinus, timestamp);

        assertEquals(Math.max(stack0.length, stackMinus.length), instance.threadIds.size());
        assertEquals(Math.max(elements0.length, elementsMinus.length), instance.methodInfos.size()-1);
        assertEquals(timestamp, instance.currentDumpTimeStamp);
    }

    @Test
    public void testAddStacktraceWaitingMinus() {
        System.out.println("add stacktrace : waiting/minus");

        setState(stack0[0], Thread.State.WAITING);
        addStacktrace(stack0, 0);

        long timestamp = 500000;
        setState(stackMinus[0], Thread.State.RUNNABLE);
        addStacktrace(stackMinus, timestamp);

        assertEquals(Math.max(stack0.length, stackMinus.length), instance.threadIds.size());
        assertEquals(Math.max(elements0.length, elementsMinus.length), instance.methodInfos.size()-1);
        assertEquals(timestamp, instance.currentDumpTimeStamp);
    }

    @Test
    public void testAddStacktraceDif() {
        System.out.println("add stacktrace : diff");

        addStacktrace(stack0, 0);

        long timestamp = 500000;

        addStacktrace(stackDif, timestamp);

        assertEquals(Math.max(stack0.length, stackDif.length), instance.threadIds.size());
        for(StackTraceElement element : elements0) {
            if (!instance.methodInfos.contains(new StackTraceSnapshotBuilder.MethodInfo(element))) {
                fail();
            }
        }
        for(StackTraceElement element : elementsDif) {
            if (!instance.methodInfos.contains(new StackTraceSnapshotBuilder.MethodInfo(element))) {
                fail();
            }
        }
        assertEquals(timestamp, instance.currentDumpTimeStamp);
    }

    @Test
    public void testAddStacktraceDifWaiting() {
        System.out.println("add stacktrace : diff/waiting");

        addStacktrace(stack0, 0);

        long timestamp = 500000;
        setState(stackDif[0], Thread.State.WAITING);
        
        addStacktrace(stackDif, timestamp);

        assertEquals(Thread.State.WAITING, instance.lastStackTrace.get().get(thread0.getId()).getThreadState());

        assertEquals(Math.max(stack0.length, stackDif.length), instance.threadIds.size());
        for(StackTraceElement element : elements0) {
            if (!instance.methodInfos.contains(new StackTraceSnapshotBuilder.MethodInfo(element))) {
                fail();
            }
        }
        for(StackTraceElement element : elementsDif) {
            if (!instance.methodInfos.contains(new StackTraceSnapshotBuilder.MethodInfo(element))) {
                fail();
            }
        }
        assertEquals(timestamp, instance.currentDumpTimeStamp);
    }

    @Test
    public void testAddStacktraceDifWaitingBlocked() {
        System.out.println("add stacktrace : diff/waiting/blocked");

        addStacktrace(stack0, 0);

        long timestamp = 500000;
        setState(stackDif[0], Thread.State.WAITING);

        addStacktrace(stackDif, timestamp);

        setState(stack0[0], Thread.State.BLOCKED);

        timestamp += 500000;

        addStacktrace(stack0, timestamp);

        assertEquals(Thread.State.BLOCKED, instance.lastStackTrace.get().get(thread0.getId()).getThreadState());

        assertEquals(Math.max(stack0.length, stackDif.length), instance.threadIds.size());
        for(StackTraceElement element : elements0) {
            if (!instance.methodInfos.contains(new StackTraceSnapshotBuilder.MethodInfo(element))) {
                fail();
            }
        }
        for(StackTraceElement element : elementsDif) {
            if (!instance.methodInfos.contains(new StackTraceSnapshotBuilder.MethodInfo(element))) {
                fail();
            }
        }
        assertEquals(timestamp, instance.currentDumpTimeStamp);
    }

    @Test
    public void testAddStacktraceDifBlockedWaiting() {
        System.out.println("add stacktrace : diff/blocked/waiting");

        addStacktrace(stack0, 0);

        long timestamp = 500000;
        setState(stackDif[0], Thread.State.BLOCKED);

        addStacktrace(stackDif, timestamp);

        setState(stack0[0], Thread.State.WAITING);

        timestamp += 500000;

        addStacktrace(stack0, timestamp);

        assertEquals(Thread.State.WAITING, instance.lastStackTrace.get().get(thread0.getId()).getThreadState());

        assertEquals(Math.max(stack0.length, stackDif.length), instance.threadIds.size());
        for(StackTraceElement element : elements0) {
            if (!instance.methodInfos.contains(new StackTraceSnapshotBuilder.MethodInfo(element))) {
                fail();
            }
        }
        for(StackTraceElement element : elementsDif) {
            if (!instance.methodInfos.contains(new StackTraceSnapshotBuilder.MethodInfo(element))) {
                fail();
            }
        }
        assertEquals(timestamp, instance.currentDumpTimeStamp);
    }

    @Test
    public void testAddStacktraceWaitingDif() {
        System.out.println("add stacktrace : waiting/diff");

        setState(stack0[0], Thread.State.WAITING);
        addStacktrace(stack0, 0);

        long timestamp = 500000;


        addStacktrace(stackDif, timestamp);

        assertEquals(Math.max(stack0.length, stackDif.length), instance.threadIds.size());
        for(StackTraceElement element : elements0) {
            if (!instance.methodInfos.contains(new StackTraceSnapshotBuilder.MethodInfo(element))) {
                fail();
            }
        }
        for(StackTraceElement element : elementsDif) {
            if (!instance.methodInfos.contains(new StackTraceSnapshotBuilder.MethodInfo(element))) {
                fail();
            }
        }
        assertEquals(timestamp, instance.currentDumpTimeStamp);
    }

    @Test
    public void testAddStacktraceWaitingDifRunnable() {
        System.out.println("add stacktrace : waiting/diff/runnable");

        setState(stack0[0], Thread.State.WAITING);
        addStacktrace(stack0, 0);

        long timestamp = 500000;

        setState(stackDif[0], Thread.State.RUNNABLE);
        addStacktrace(stackDif, timestamp);

        assertEquals(Math.max(stack0.length, stackDif.length), instance.threadIds.size());
        for(StackTraceElement element : elements0) {
            if (!instance.methodInfos.contains(new StackTraceSnapshotBuilder.MethodInfo(element))) {
                fail();
            }
        }
        for(StackTraceElement element : elementsDif) {
            if (!instance.methodInfos.contains(new StackTraceSnapshotBuilder.MethodInfo(element))) {
                fail();
            }
        }
        assertEquals(timestamp, instance.currentDumpTimeStamp);
    }

    @Test
    public void testAddStackTraceNew() {
        System.out.println("add stacktrace : new");

        setState(stack0[0], Thread.State.NEW);

        try {
            addStacktrace(stack0, 500000);
            assertFalse(instance.threadNames.contains(stack0[0].getThreadName()));
        } catch (IllegalStateException ex) {}
    }

    @Test
    public void testAddStackTraceWasTerminated() {
        System.out.println("add stacktrace : terminated->runnable");

        setState(stack0[0], Thread.State.TERMINATED);

        try {
            addStacktrace(stack0, 0);
            setState(stack0[0], Thread.State.RUNNABLE);
            addStacktrace(stack0, 500000);
            fail();
        } catch (IllegalStateException ex) {}
    }


    @Test
    public void testAddStackTraceRunnable() {
        System.out.println("add stacktrace : runnable");

        setState(stack0[0], Thread.State.RUNNABLE);

        addStacktrace(stack0, 500000);

        assertEquals(500000, instance.currentDumpTimeStamp);
        assertEquals(Thread.State.RUNNABLE, instance.lastStackTrace.get().get(thread0.getId()).getThreadState());
    }

    @Test
    public void testAddStackTraceWaiting() {
        System.out.println("add stacktrace : waiting");

        setState(stack0[0], Thread.State.WAITING);

        addStacktrace(stack0, 500000);

        assertEquals(500000, instance.currentDumpTimeStamp);
        assertEquals(Thread.State.WAITING, instance.lastStackTrace.get().get(thread0.getId()).getThreadState());
    }

    @Test
    public void testAddStackTraceTimedWaiting() {
        System.out.println("add stacktrace : timed waiting");

        setState(stack0[0], Thread.State.TIMED_WAITING);

        addStacktrace(stack0, 500000);

        assertEquals(500000, instance.currentDumpTimeStamp);
        assertEquals(Thread.State.TIMED_WAITING, instance.lastStackTrace.get().get(thread0.getId()).getThreadState());
    }

    @Test
    public void testAddStackTraceBlocked() {
        System.out.println("add stacktrace : blocked");

        setState(stack0[0], Thread.State.BLOCKED);

        addStacktrace(stack0, 500000);

        assertEquals(500000, instance.currentDumpTimeStamp);
        assertEquals(Thread.State.BLOCKED, instance.lastStackTrace.get().get(thread0.getId()).getThreadState());
    }

    @Test
    public void testAddStackTraceTerminated() {
        System.out.println("add stacktrace : terminated");

        setState(stack0[0], Thread.State.TERMINATED);

        addStacktrace(stack0, 500000);

        assertEquals(500000, instance.currentDumpTimeStamp);
        assertEquals(Thread.State.TERMINATED, instance.lastStackTrace.get().get(thread0.getId()).getThreadState());
    }

    @Test
    public void testAddStackTraceWaitRun() {
        System.out.println("add stacktrace : wait->run");

        addStacktrace(stack0, 0);
        setState(stack0[0], Thread.State.WAITING);
        
        addStacktrace(stack0, 500000);

        assertEquals(500000, instance.currentDumpTimeStamp);
        assertEquals(Thread.State.WAITING, instance.lastStackTrace.get().get(thread0.getId()).getThreadState());

        setState(stack0[0], Thread.State.RUNNABLE);
        addStacktrace(stack0, 1000000);

        assertEquals(1000000, instance.currentDumpTimeStamp);
        assertEquals(Thread.State.RUNNABLE, instance.lastStackTrace.get().get(thread0.getId()).getThreadState());
    }

    @Test
    public void testAddStackTraceWaitWait() {
        System.out.println("add stacktrace : wait->wait");

        addStacktrace(stack0, 0);
        setState(stack0[0], Thread.State.WAITING);

        addStacktrace(stack0, 500000);

        assertEquals(500000, instance.currentDumpTimeStamp);
        assertEquals(Thread.State.WAITING, instance.lastStackTrace.get().get(thread0.getId()).getThreadState());
        addStacktrace(stack0, 1000000);

        assertEquals(1000000, instance.currentDumpTimeStamp);
        assertEquals(Thread.State.WAITING, instance.lastStackTrace.get().get(thread0.getId()).getThreadState());
    }

    @Test
    public void testAddStackTraceWaitBlocked() {
        System.out.println("add stacktrace : wait->blocked");

        addStacktrace(stack0, 0);
        setState(stack0[0], Thread.State.WAITING);

        addStacktrace(stack0, 500000);

        assertEquals(500000, instance.currentDumpTimeStamp);
        assertEquals(Thread.State.WAITING, instance.lastStackTrace.get().get(thread0.getId()).getThreadState());
        setState(stack0[0], Thread.State.BLOCKED);
        addStacktrace(stack0, 1000000);

        assertEquals(1000000, instance.currentDumpTimeStamp);
        assertEquals(Thread.State.BLOCKED, instance.lastStackTrace.get().get(thread0.getId()).getThreadState());
    }



    @Test
    public void testReset() {
        System.out.println("reset");
        ThreadMXBean tbean = ManagementFactory.getThreadMXBean();
        instance.addStacktrace(tbean.getThreadInfo(tbean.getAllThreadIds(), Integer.MAX_VALUE), System.nanoTime());
        instance.addStacktrace(tbean.getThreadInfo(tbean.getAllThreadIds(), Integer.MAX_VALUE), System.nanoTime());

        instance.reset();
        assertTrue(instance.methodInfos.size()-1 == 0);
        assertTrue(instance.threadIds.size() == 0);
        assertTrue(instance.threadNames.size() == 0);
        assertEquals(-1L, instance.currentDumpTimeStamp);
        //assertEquals(-1L, instance.firstDumpTimeStamp);
        assertEquals(0, instance.stackTraceCount);

        try {
            instance.createSnapshot(System.currentTimeMillis());
            fail();
        } catch (CPUResultsSnapshot.NoDataAvailableException ex) {
        }
    }

    @Test
    public void testIgnoredThreadName() {
        System.out.println("ignored thread name");

        String ignoredThread = "Thread 0";
        instance.setIgnoredThreads(Collections.singleton(ignoredThread));

        addStacktrace(stack0, 0);
        assertFalse(instance.threadNames.contains(ignoredThread));
    }

    private ThreadSample createThreadSample(Thread t, StackTraceElement[] stack) {
        return new ThreadSample(t.getName(), t.getId(), State.RUNNABLE, stack);
    }

    private void setState(ThreadSample tinfo, State s) {
        tinfo.threadState = s;
    }

    private void addStacktrace(ThreadSample[] tinfos, long time) {
        StackTraceSnapshotBuilder.SampledThreadInfo[] samples =
                new StackTraceSnapshotBuilder.SampledThreadInfo[tinfos.length];
        for (int i = 0; i < tinfos.length; i++) {
            samples[i] = tinfos[i].toSampledThreadInfo(instance.getFilter());
        }
        instance.addStacktrace(samples, time);
    }

    private static final class ThreadSample {
        private final String threadName;
        private final long threadId;
        private State threadState;
        private final StackTraceElement[] stackTrace;

        private ThreadSample(String threadName, long threadId, State threadState,
                             StackTraceElement[] stackTrace) {
            this.threadName = threadName;
            this.threadId = threadId;
            this.threadState = threadState;
            this.stackTrace = stackTrace;
        }

        private long getThreadId() {
            return threadId;
        }

        private String getThreadName() {
            return threadName;
        }

        private StackTraceSnapshotBuilder.SampledThreadInfo toSampledThreadInfo(
                InstrumentationFilter filter) {
            return new StackTraceSnapshotBuilder.SampledThreadInfo(
                    threadName, threadId, threadState, stackTrace, filter);
        }
    }
}
