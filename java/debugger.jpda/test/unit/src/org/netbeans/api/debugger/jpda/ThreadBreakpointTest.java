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

import junit.framework.Test;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointListener;
import org.netbeans.junit.NbTestCase;

/**
 * Tests thread breakpoints.
 *
 * @author Maros Sandor, Jan Jancura
 */
public class ThreadBreakpointTest extends NbTestCase {

    private JPDASupport     support;
    private DebuggerManager dm = DebuggerManager.getDebuggerManager ();

    private static final String CLASS_NAME = 
        "org.netbeans.api.debugger.jpda.testapps.ThreadBreakpointApp";

    
    public ThreadBreakpointTest (String s) {
        super (s);
    }

    public static Test suite() {
        return JPDASupport.createTestSuite(ThreadBreakpointTest.class);
    }
    
    public void testThreadBreakpoints () throws Exception {
        try {
            ThreadBreakpoint tb1 = ThreadBreakpoint.create ();
            tb1.setBreakpointType (ThreadBreakpoint.TYPE_THREAD_STARTED_OR_DEATH);
            TestBreakpointListener tbl = new TestBreakpointListener (10);
            tb1.addJPDABreakpointListener (tbl);
            dm.addBreakpoint (tb1);

            support = JPDASupport.attach (CLASS_NAME);

            for (int i = 0; ; i++) {
                if (tbl.getFailure() != null) {
                    throw tbl.getFailure();
                }
                if (i > 100) {
                    throw new AssertionError("Too many cycles of resume, continue does not seem to resume the app to finish.");
                }
                support.waitState (JPDADebugger.STATE_STOPPED);
                if (support.getDebugger ().getState () == 
                    JPDADebugger.STATE_DISCONNECTED
                ) break;
                support.doContinue ();
                Thread.sleep(100);
            }
            tbl.assertFailure ();

            dm.removeBreakpoint (tb1);
        } finally {
            support.doFinish();
        }
    }

    private class TestBreakpointListener implements JPDABreakpointListener {

        private int                 hitCount;
        private AssertionError      failure;
        private int                 expectedHitCount;

        public TestBreakpointListener (int expectedHitCount) {
            this.expectedHitCount = expectedHitCount;
        }

        public void breakpointReached (JPDABreakpointEvent event) {
            //System.err.println("Thread Breakpoint hit, hitCount was = "+hitCount+", failure was "+failure);
            try {
                checkEvent (event);
            } catch (AssertionError e) {
                failure = e;
            } catch (Throwable e) {
                failure = new AssertionError (e);
            }
        }
        
        public AssertionError getFailure() {
            return failure;
        }

        private void checkEvent (JPDABreakpointEvent event) {
//            ThreadBreakpoint tb = (ThreadBreakpoint) event.getSource();
//            assertEquals (
//                "Breakpoint event: Condition evaluation failed", 
//                JPDABreakpointEvent.CONDITION_NONE, 
//                event.getConditionResult ()
//            );
            assertNotNull (
                "Breakpoint event: Context thread is null", 
                event.getThread ()
            );
            JPDAThread thread = event.getThread ();
            if (thread.getName ().startsWith ("test-")) {
                JPDAThreadGroup group = thread.getParentThreadGroup ();
                assertEquals (
                    "Wrong thread group", 
                    "testgroup", 
                    group.getName ()
                );
                assertEquals (
                    "Wrong parent thread group", 
                    "main", 
                    group.getParentThreadGroup ().getName ()
                );
                assertEquals (
                    "Wrong number of child thread groups", 
                    0, 
                    group.getThreadGroups ().length
                );
                JPDAThread [] threads = group.getThreads ();
                for (int i = 0; i < threads.length; i++) {
                    JPDAThread jpdaThread = threads [i];
                    if ( !jpdaThread.getName ().startsWith ("test-")) 
                        throw new AssertionError 
                            ("Thread group contains an alien thread");
                    assertSame (
                        "Child/parent mismatch", 
                        jpdaThread.getParentThreadGroup (), 
                        group
                    );
                }
                hitCount++;
            }
            if (thread.getName ().startsWith ("DestroyJavaVM")) {
                // Wait a while to gather all events.
                try {
                    Thread.sleep(500);
                } catch (InterruptedException iex) {}
            }
        }

        public void assertFailure () {
            if (failure != null) throw failure;
            assertEquals (
                "Breakpoint hit count mismatch", 
                expectedHitCount, 
                hitCount
            );
        }
    }
}
