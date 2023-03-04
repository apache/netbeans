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

package org.netbeans.api.debugger.jpda;

import com.sun.jdi.AbsentInformationException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import junit.framework.Test;

import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Exceptions;

/**
 * Test of concurrent debugging.
 * <p>
 * Debugging of an application with multiple threads, we test concurrent use of
 * breakpoints, conditional breakpoints, steps, evaluations, pop frames,
 * queries for stack frames, local variables and monitors,
 * while also resuming other threads or while stepping.
 * App control (resume of threads and stepping is performed concurrently with
 * queries from multiple threads.
 * <p>
 * Thus a multi-threaded application is accessed from multiple debugger threads.
 *
 * @author Martin Entlicher
 */
public class ConcurrencyTest extends NbTestCase {

    private JPDASupport     support;
    
    public ConcurrencyTest(String s) {
        super (s);
    }

    public static Test suite() {
        return JPDASupport.createTestSuite(ConcurrencyTest.class);
    }
    
    @Override
    protected void setUp () throws Exception {
        super.setUp ();
        JPDASupport.removeAllBreakpoints ();
        Utils.BreakPositions bp = Utils.getBreakPositions(System.getProperty ("test.dir.src")+
                                  "org/netbeans/api/debugger/jpda/testapps/ConcurrencyApp.java");
        List<LineBreakpoint> lbs = bp.getLineBreakpoints();
        //lbs.get(0).setSuspend(JPDABreakpoint.SUSPEND_ALL);
        lbs.get(1).setCondition("i == Math.round(Math.max(Math.PI, Math.sin(10.0)))");
        for (LineBreakpoint lb : lbs) {
            DebuggerManager.getDebuggerManager ().addBreakpoint (lb);
        }
        support = JPDASupport.attach (
            "org.netbeans.api.debugger.jpda.testapps.ConcurrencyApp"
        );
        support.waitState (JPDADebugger.STATE_STOPPED);
        // We're stopped on the 0th breakpoint in some thread.
    }

    /**
     * The app stops on breakpoint in multiple threads and concurrent stepping
     * in different threads is performed.
     */
    public void testSteppingInTwoThreads () throws Exception {
        try {
            JPDADebugger debugger = support.getDebugger();
            JPDAThread currentThread = debugger.getCurrentThread();
            assertNotNull("Current thread not found!", currentThread);
            assertTrue("Current thread is not suspended!", currentThread.isSuspended());

            Thread.sleep(1000); // Let all app threads started
            JPDAThread t2 = null;
            List<JPDAThread> allThreads = debugger.getThreadsCollector().getAllThreads();
            for (JPDAThread t : allThreads) {
                if (t != currentThread && t.getName().startsWith("Concurrency thread ")) {
                    t2 = t;
                    break;
                }
            }
            assertNotNull("Second application thread not found!", t2);
            t2.suspend(); // Probably already hit the breakpoint, but do suspend for sure.
            // Remove all breakpoints so that they do not interfere with steps:
            Breakpoint[] bpts = DebuggerManager.getDebuggerManager ().getBreakpoints();
            for (Breakpoint b : bpts) {
                DebuggerManager.getDebuggerManager ().removeBreakpoint(b);
            }

            // Perform stepping in two threads:
            SteppingThread st1 = new SteppingThread(debugger, currentThread);
            SteppingThread st2 = new SteppingThread(debugger, t2);
            st1.start();
            st2.start();

            st1.join();
            st2.join();
            if (st1.getFailMessage() != null) {
                fail(st1.getFailMessage());
            }
            if (st2.getFailMessage() != null) {
                fail(st2.getFailMessage());
            }
        } finally {
            support.doFinish ();
        }
    }

    private class SteppingThread extends Thread {

        private JPDADebugger debugger;
        private JPDAThread t;
        private String failMessage = null;

        public SteppingThread(JPDADebugger debugger, JPDAThread t) {
            this.debugger = debugger;
            this.t = t;
            setName("SteppingThread for "+t.getName());
        }

        public String getFailMessage() {
            return failMessage;
        }

        @Override
        public void run() {
            JPDAStep step = debugger.createJPDAStep(JPDAStep.STEP_LINE, JPDAStep.STEP_INTO);
            final StepMonitor monitor = new StepMonitor();
            step.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    // Step done
                    System.err.println("  "+t.getName()+": step finished.");
                    monitor.stepFinished();
                }
            });
            System.err.println("Stepping started in "+getName());
            try {
                System.err.println("  " + t.getName() + ": frame = "+t.getCallStack()[0].getClassName()+"."+t.getCallStack()[0].getMethodName()+"():"+t.getCallStack()[0].getLineNumber(null));
            } catch (AbsentInformationException ex) {
                Exceptions.printStackTrace(ex);
            }
            try {
                for (int i = 0; i < 1000; i++) {
                    System.err.println("  "+t.getName()+": waiting for step i = "+i);
                    if (monitor.waitForStep(10000)) {
                        step.addStep(t);
                        monitor.stepStarted();
                        System.err.println("  "+t.getName()+": step started i = "+i);
                        t.resume();
                    } else {
                        System.err.println("  "+t.getName()+": step not finished. Thread suspended = "+t.isSuspended());
                        t.suspend();
                        System.err.println("  "+t.getName()+": state = "+t.getState());
                        try {
                            System.err.println("  " + t.getName() + ": frame = "+t.getCallStack()[0].getClassName()+"."+t.getCallStack()[0].getMethodName()+"():"+t.getCallStack()[0].getLineNumber(null));
                        } catch (AbsentInformationException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                        try {
                            failMessage = "Step not finished: " + t.getName() + ": frame = " + t.getCallStack()[0].getClassName() + "." + t.getCallStack()[0].getMethodName() + "():" + t.getCallStack()[0].getLineNumber(null);
                        } catch (AbsentInformationException ex) {
                            failMessage = "Step not finished in some absent information location.";
                        }
                        break;
                    }
                }
            } catch (InterruptedException iex) {
                // Done.
            }
        }
    }

    private static class StepMonitor {

        private boolean step = false;

        public StepMonitor() {}

        public synchronized void stepStarted() {
            step = true;
        }

        public synchronized void stepFinished() {
            step = false;
            notify();
        }

        public synchronized boolean waitForStep(long timeout) throws InterruptedException {
            if (step) {
                wait(timeout);
                return !step;
            } else {
                return true;
            }
        }

        public synchronized boolean isInStep() {
            return step;
        }
    }

    /**
     * The app stops on breakpoint in multiple threads. One thread is queried
     * for stack frames, local variables, monitors, etc. while we perform steps
     * in other threads.
     */
    public void testSteppingWhileFramesQueries() throws Exception {
        try {
        } finally {
            support.doFinish ();
        }
    }

    /**
     * The app stops on breakpoint in multiple threads. One thread is queried
     * for stack frames, local variables, monitors, etc. while we periodically
     * resume other threads, which hit a breakpoint.
     */
    public void testResumingWhileFramesQueries() throws Exception {
        try {
        } finally {
            support.doFinish ();
        }
    }

    /**
     * The app stops on breakpoint in multiple threads. One thread is queried
     * for stack frames, local variables, monitors, etc. while other threads
     * periodically hit a conditional breakpoint, whose condition contain
     * method calls.
     */
    public void testConditionalBreakpointWhileFramesQueries() throws Exception {
        try {
        } finally {
            support.doFinish ();
        }
    }

}
