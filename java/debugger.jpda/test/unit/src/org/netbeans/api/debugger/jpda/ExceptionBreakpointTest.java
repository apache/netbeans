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
 * Tests exception breakpoints.
 *
 * @author Maros Sandor, Jan Jancura
 */
public class ExceptionBreakpointTest extends NbTestCase {

    private JPDASupport     support;
    private DebuggerManager dm = DebuggerManager.getDebuggerManager ();

    private static final String CLASS_NAME = 
        "org.netbeans.api.debugger.jpda.testapps.ExceptionBreakpointApp";

    
    public ExceptionBreakpointTest (String s) {
        super (s);
    }

    public static Test suite() {
        return JPDASupport.createTestSuite(ExceptionBreakpointTest.class);
    }
    
    public void testMethodBreakpoints () throws Exception {
        try {
            ExceptionBreakpoint eb1 = ExceptionBreakpoint.create (
                "org.netbeans.api.debugger.jpda.testapps.ExceptionTestException", 
                ExceptionBreakpoint.TYPE_EXCEPTION_CAUGHT
            );
            TestBreakpointListener tbl = new TestBreakpointListener (
                "org.netbeans.api.debugger.jpda.testapps.ExceptionTestException", 
                eb1, 
                1
            );
            eb1.addJPDABreakpointListener (tbl);
            dm.addBreakpoint (eb1);

            support = JPDASupport.attach (CLASS_NAME);

            for (;;) {
                support.waitState (JPDADebugger.STATE_STOPPED);
                if (support.getDebugger ().getState () == 
                      JPDADebugger.STATE_DISCONNECTED
                ) break;
                support.doContinue ();
            }
            tbl.assertFailure ();

            dm.removeBreakpoint (eb1);
        } finally {
            support.doFinish ();
        }
    }

    private class TestBreakpointListener implements JPDABreakpointListener {

        private int                 hitCount;
        private AssertionError      failure;
        private String              exceptionClass;
        private ExceptionBreakpoint bpt;
        private int                 expectedHitCount;

        public TestBreakpointListener (
            String exceptionClass, 
            ExceptionBreakpoint bpt, 
            int expectedHitCount
        ) {
            this.exceptionClass = exceptionClass;
            this.bpt = bpt;
            this.expectedHitCount = expectedHitCount;
        }

        public void breakpointReached (JPDABreakpointEvent event) {
            try {
                checkEvent (event);
            } catch (AssertionError e) {
                failure = e;
            } catch (Throwable e) {
                failure = new AssertionError (e);
            }
        }

        private void checkEvent (JPDABreakpointEvent event) {
            assertEquals (
                "Breakpoint event: Bad exception location", 
                CLASS_NAME, 
                event.getReferenceType ().name ()
            );
            assertEquals (
                "Breakpoint event: Bad exception thrown", 
                exceptionClass, 
                event.getVariable ().getType ()
            );
            assertSame (
                "Breakpoint event: Bad event source", 
                bpt, 
                event.getSource ()
            );
            assertEquals (
                "Breakpoint event: Condition evaluation failed", 
                JPDABreakpointEvent.CONDITION_NONE, 
                event.getConditionResult ()
            );
            assertNotNull (
                "Breakpoint event: Context thread is null", 
                event.getThread ()
            );
            hitCount++;
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
