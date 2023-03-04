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
 * Tests class breakpoints.
 *
 * @author Maros Sandor, Jan Jancura
 */
public class ClassBreakpointTest extends NbTestCase {

    private JPDASupport     support;
    private JPDADebugger    debugger;
    private DebuggerManager dm = DebuggerManager.getDebuggerManager ();

    private static final String CLASS_NAME = "org.netbeans.api.debugger.jpda.testapps.ClassBreakpointApp";

    public static Test suite() {
        return JPDASupport.createTestSuite(ClassBreakpointTest.class);
    }

    public ClassBreakpointTest (String s) {
        super (s);
    }

    public void testMethodBreakpoints() throws Exception {
        try {
            ClassLoadUnloadBreakpoint cb1 = ClassLoadUnloadBreakpoint.create("org.netbeans.api.debugger.jpda.testapps.ClassBreakpointTest1", false, ClassLoadUnloadBreakpoint.TYPE_CLASS_LOADED);
            TestBreakpointListener tbl = new TestBreakpointListener("org.netbeans.api.debugger.jpda.testapps.ClassBreakpointTest1", 1);
            cb1.addJPDABreakpointListener(tbl);
            dm.addBreakpoint(cb1);

            ClassLoadUnloadBreakpoint cb2 = ClassLoadUnloadBreakpoint.create("*", false, ClassLoadUnloadBreakpoint.TYPE_CLASS_LOADED);
            TestBreakpointListener tb2 = new TestBreakpointListener(null, -1);
            cb2.addJPDABreakpointListener(tb2);
            dm.addBreakpoint(cb2);

            support = JPDASupport.attach (CLASS_NAME);
            debugger = support.getDebugger();

            for (;;) {
                support.waitState (JPDADebugger.STATE_STOPPED);
                if (debugger.getState() == JPDADebugger.STATE_DISCONNECTED) break;
                support.doContinue();
            }
            tbl.assertFailure();
            tb2.assertFailure();

            dm.removeBreakpoint(cb1);
            dm.removeBreakpoint(cb2);
        } finally {
            support.doFinish();
        }
    }

    private class TestBreakpointListener implements JPDABreakpointListener {

        private int                 hitCount;
        private AssertionError      failure;
        private String              className;
        private int                 expectedHitCount;

        public TestBreakpointListener(String className, int expectedHitCount) {
            this.className = className;
            this.expectedHitCount = expectedHitCount;
        }

        public void breakpointReached(JPDABreakpointEvent event) {
            try {
                checkEvent(event);
            } catch (AssertionError e) {
                failure = e;
            } catch (Throwable e) {
                failure = new AssertionError(e);
            }
        }

        private void checkEvent(JPDABreakpointEvent event) {
            ClassLoadUnloadBreakpoint cb = (ClassLoadUnloadBreakpoint) event.getSource();

            assertEquals("Breakpoint event: Condition evaluation failed", JPDABreakpointEvent.CONDITION_NONE, event.getConditionResult());
            assertNotNull("Breakpoint event: Context thread is null", event.getThread());
            assertNotNull("Breakpoint event: Reference type is null", event.getReferenceType());
            if (className != null)
            {
                assertEquals("Breakpoint event: Hit at wrong class", className, event.getReferenceType().name());
            }

            hitCount++;
        }

        public void assertFailure() {
            if (failure != null) throw failure;
            if (expectedHitCount != -1)
            {
                assertEquals("Breakpoint hit count mismatch for: " + className, expectedHitCount, hitCount);
            }
        }
    }
}
