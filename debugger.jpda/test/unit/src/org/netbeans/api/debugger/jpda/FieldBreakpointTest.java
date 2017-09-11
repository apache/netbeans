/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
