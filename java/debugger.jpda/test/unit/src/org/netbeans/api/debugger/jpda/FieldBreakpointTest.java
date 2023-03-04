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
import junit.framework.Test;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointListener;
import org.netbeans.junit.NbTestCase;

/**
 * Tests field breakpoints.
 *
 * @author Maros Sandor, Jan Jancura
 */
public class FieldBreakpointTest extends NbTestCase {

    private JPDASupport     support;
    private DebuggerManager dm = DebuggerManager.getDebuggerManager ();

    private static final String CLASS_NAME = 
            "org.netbeans.api.debugger.jpda.testapps.FieldBreakpointApp";

    public FieldBreakpointTest (String s) {
        super (s);
    }

    public static Test suite() {
        return JPDASupport.createTestSuite(FieldBreakpointTest.class);
    }
    
    public void testFieldBreakpoints() throws Exception {
        try {

            Utils.BreakPositions bp = Utils.getBreakPositions(System.getProperty ("test.dir.src") + 
                    "org/netbeans/api/debugger/jpda/testapps/FieldBreakpointApp.java");
            FieldBreakpoint fb2 = FieldBreakpoint.create (
                CLASS_NAME, 
                "y", 
                FieldBreakpoint.TYPE_MODIFICATION
            );
            TestBreakpointListener tb2 = new TestBreakpointListener (
                "y", 
                0,
                new int[] { bp.getStopLine("FY1"), bp.getStopLine("FY2"), bp.getStopLine("FY3") }
                //new int [] { 44, 47, 51 }
            );
            fb2.addJPDABreakpointListener (tb2);
            dm.addBreakpoint (fb2);

            FieldBreakpoint fb4 = FieldBreakpoint.create (
                CLASS_NAME + "$InnerStatic", 
                "w", 
                FieldBreakpoint.TYPE_MODIFICATION
            );
            TestBreakpointListener tb4 = new TestBreakpointListener (
                "InnerStatic.w", 
                0, 
                new int[] { bp.getStopLine("FW1"), bp.getStopLine("FW2"), bp.getStopLine("FW3") }
                //new int [] { 81, 84, 88 }
            );
            fb4.addJPDABreakpointListener (tb4);
            dm.addBreakpoint (fb4);

            FieldBreakpoint fb5 = FieldBreakpoint.create (
                CLASS_NAME + "$Inner", 
                "w", 
                FieldBreakpoint.TYPE_MODIFICATION
            );
            TestBreakpointListener tb5 = new TestBreakpointListener (
                "Inner.w", 
                0, 
                new int[] { bp.getStopLine("FIW1"), bp.getStopLine("FIW2"), bp.getStopLine("FIW3") }
                //new int [] { 102, 105, 109 }
            );
            fb5.addJPDABreakpointListener (tb5);
            dm.addBreakpoint (fb5);

            support = JPDASupport.attach (CLASS_NAME);
            for (;;) {
                support.waitState (JPDADebugger.STATE_STOPPED);
                if ( support.getDebugger ().getState () == 
                     JPDADebugger.STATE_DISCONNECTED
                ) 
                    break;
                support.doContinue ();
            }
            
//            tbl.assertFailure ();
            tb2.assertFailure ();
//            tb3.assertFailure ();
            tb4.assertFailure ();
            tb5.assertFailure ();

//            dm.removeBreakpoint (fb1);
            dm.removeBreakpoint (fb2);
//            dm.removeBreakpoint (fb3);
            dm.removeBreakpoint (fb4);
            dm.removeBreakpoint (fb5);
        } finally {
            support.doFinish ();
        }
    }

    private class TestBreakpointListener implements JPDABreakpointListener {

        private int                 hitCount;
        private int                 currentFieldValue;
        private AssertionError      failure;
        private String              variableName;
        private int[]               hitLines;

        public TestBreakpointListener (
                String variableName, 
                int initialValue, 
                int [] hitLines
                ) {
            this.variableName = variableName;
            this.hitLines = hitLines;
            currentFieldValue = initialValue;
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

        private void checkEvent (JPDABreakpointEvent event) 
        throws AbsentInformationException {
            
            FieldBreakpoint fb = (FieldBreakpoint) event.getSource ();

            System.out.println (
                variableName + " : "  + 
                event.getThread ().getCallStack () [0].getLineNumber (null) + 
                " : " + event.getVariable ().getValue ());
            
            if (hitCount >= hitLines.length) 
                throw new AssertionError (
                    "Breakpoint hit too many times for " + variableName + 
                    ": " + hitCount + " at " + 
                    event.getThread ().getCallStack () [0].getLineNumber (null)
                );
            int hitLine = hitLines [hitCount++];
            assertEquals (
                "Breakpoint event: Condition evaluation failed", 
                JPDABreakpointEvent.CONDITION_NONE, 
                event.getConditionResult ()
            );
            assertNotNull (
                "Breakpoint event: Context thread is null", 
                event.getThread ()
            );
            assertEquals (
                "Breakpoint event: Hit at wrong place", 
                hitLine, 
                event.getThread ().getCallStack () [0].getLineNumber (null)
            );
            Variable var = event.getVariable ();
            assertNotNull (
                "Breakpoint event: No variable information", 
                var
            );

            if (fb.getBreakpointType () == FieldBreakpoint.TYPE_ACCESS) {
                assertEquals (
                    "Breakpoint event: Wrong field value", 
                    Integer.toString (currentFieldValue), 
                    var.getValue ()
                );
            } else {
                currentFieldValue ++;
                assertEquals (
                    "Breakpoint event: Wrong field value of " + 
                    fb.getFieldName () + " at " + 
                    event.getThread ().getCallStack () [0].getLineNumber (null), 
                    Integer.toString (currentFieldValue), 
                    var.getValue ()
                );
            }
        }

        public void assertFailure () {
            if (failure != null) throw failure;
            assertEquals (
                "Breakpoint hit count mismatch for: " + variableName, 
                hitLines.length, 
                hitCount
            );
        }
    }
}
