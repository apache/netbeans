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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import junit.framework.Test;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointListener;
import org.netbeans.junit.NbTestCase;

/**
 * Tests breakpoints deactivation.
 *
 */
public class BreakpointsDeactivationTest extends NbTestCase {

    private static final String TEST_APP_PATH = System.getProperty ("test.dir.src") + 
        "org/netbeans/api/debugger/jpda/testapps/LineBreakpointApp.java";
    
    private JPDASupport support;
    
    
    public BreakpointsDeactivationTest (String s) {
        super (s);
    }

    public static Test suite() {
        return JPDASupport.createTestSuite(BreakpointsDeactivationTest.class);
    }
    
    public void testBreakpointsDeactivation () throws Exception {
        try {
            Utils.BreakPositions bp = Utils.getBreakPositions(TEST_APP_PATH);
            LineBreakpoint[] lb = bp.getBreakpoints().toArray(new LineBreakpoint[0]);
            {
                LineBreakpoint b;
                b = lb[4];
                lb[4] = lb[2];
                lb[2] = b;
            }
            /*
            LineBreakpoint lb1 = LineBreakpoint.create (TEST_APP, 32);
            LineBreakpoint lb2 = LineBreakpoint.create (TEST_APP, 37);
            LineBreakpoint lb3 = LineBreakpoint.create (TEST_APP, 109);
            lb3.setPreferredClassName("org.netbeans.api.debugger.jpda.testapps.LineBreakpointApp$Inner");
            LineBreakpoint lb4 = LineBreakpoint.create (TEST_APP, 92);
            lb4.setPreferredClassName("org.netbeans.api.debugger.jpda.testapps.LineBreakpointApp$InnerStatic");
            LineBreakpoint lb5 = LineBreakpoint.create (TEST_APP, 41);
            */
            DebuggerManager dm = DebuggerManager.getDebuggerManager ();
            for (int i = 0; i < lb.length; i++) {
                dm.addBreakpoint (lb[i]);
            }

            TestBreakpointListener[] tb = new TestBreakpointListener[lb.length];
            for (int i = 0; i < lb.length; i++) {
                tb[i] = new TestBreakpointListener (lb[i]);
                lb[i].addJPDABreakpointListener (tb[i]);
            }
            support = JPDASupport.attach (
                "org.netbeans.api.debugger.jpda.testapps.LineBreakpointApp"
            );
            JPDADebugger debugger = support.getDebugger();
            assertEquals("Breakpoints should be active initially.", true, debugger.getBreakpointsActive());
            
            support.waitState (JPDADebugger.STATE_STOPPED);  // stopped on the first breakpoint
            
            final PropertyChangeEvent[] propertyPtr = new PropertyChangeEvent[] { null };
            debugger.addPropertyChangeListener(JPDADebugger.PROP_BREAKPOINTS_ACTIVE, new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    propertyPtr[0] = evt;
                }
            });
            debugger.setBreakpointsActive(false);
            assertNotNull(propertyPtr[0]);
            assertEquals(propertyPtr[0].getSource(), debugger);
            assertEquals(propertyPtr[0].getOldValue(), Boolean.TRUE);
            assertEquals(propertyPtr[0].getNewValue(), Boolean.FALSE);
            assertEquals("Breakpoints should be inactive after deactivation.", false, debugger.getBreakpointsActive());
            
            int j = 0;
            assertEquals (
                "Debugger stopped at wrong line for breakpoint " + j, 
                lb[j].getLineNumber (), 
                debugger.getCurrentCallStackFrame ().getLineNumber (null)
            );
            for (int i = j+1; i < tb.length; i++) {
                tb[i].checkNotNotified();
            }
            if (j < lb.length - 1) {
                support.doContinue();
            }
            
            for (int i = 0; i < tb.length; i++) {
                dm.removeBreakpoint (lb[i]);
            }
            support.waitState (JPDADebugger.STATE_DISCONNECTED);
        } finally {
            if (support != null) {
                support.doFinish ();
            }
        }
    }

    // innerclasses ............................................................
    
    private class TestBreakpointListener implements JPDABreakpointListener {

        private LineBreakpoint  lineBreakpoint;
        private int             conditionResult;

        private JPDABreakpointEvent event;
        private AssertionError      failure;

        public TestBreakpointListener (LineBreakpoint lineBreakpoint) {
            this (lineBreakpoint, JPDABreakpointEvent.CONDITION_NONE);
        }

        public TestBreakpointListener (
            LineBreakpoint lineBreakpoint, 
            int conditionResult
        ) {
            this.lineBreakpoint = lineBreakpoint;
            this.conditionResult = conditionResult;
        }

        @Override
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
            this.event = event;
            assertEquals (
                "Breakpoint event: Wrong source breakpoint", 
                lineBreakpoint, 
                event.getSource ()
            );
            assertNotNull (
                "Breakpoint event: Context thread is null", 
                event.getThread ()
            );

            int result = event.getConditionResult ();
            if ( result == JPDABreakpointEvent.CONDITION_FAILED && 
                 conditionResult != JPDABreakpointEvent.CONDITION_FAILED
            ) {
                failure = new AssertionError (event.getConditionException ());
            } else 
            if (result != conditionResult) {
                failure = new AssertionError (
                    "Unexpected breakpoint condition result: " + result
                );
            }
        }

        public void checkResult () {
            if (event == null) {
                CallStackFrame f = support.getDebugger ().
                    getCurrentCallStackFrame ();
                int ln = -1;
                if (f != null) {
                    ln = f.getLineNumber (null);
                }
                throw new AssertionError (
                    "Breakpoint was not hit (listener was not notified) " + ln
                );
            }
            if (failure != null) {
                throw failure;
            }
        }
        
        public void checkNotNotified() {
            if (event != null) {
                JPDAThread t = event.getThread();
                throw new AssertionError (
                    "Breakpoint was hit (listener was notified) in thread " + t
                );
            }
            if (failure != null) {
                throw failure;
            }
        }
    }
}
