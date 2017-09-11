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

import junit.framework.Test;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointListener;
import org.netbeans.junit.NbTestCase;


/**
 * Tests method breakpoints.
 *
 * @author Maros Sandor, Jan Jancura
 */
public class MethodBreakpointTest extends NbTestCase {

    private JPDASupport     support;
    private DebuggerManager dm = DebuggerManager.getDebuggerManager ();

    private static final String CLASS_NAME =
        "org.netbeans.api.debugger.jpda.testapps.MethodBreakpointApp";

    public MethodBreakpointTest (String s) {
        super (s);
    }

    public static Test suite() {
        return JPDASupport.createTestSuite(MethodBreakpointTest.class);
    }
    
    public void testMethodEntryBreakpoints() throws Exception {
        Utils.BreakPositions bp = Utils.getBreakPositions(System.getProperty ("test.dir.src") + 
                "org/netbeans/api/debugger/jpda/testapps/MethodBreakpointApp.java");
        try {
            MethodBreakpoint mb1 = MethodBreakpoint.create (CLASS_NAME, "a");
            TestBreakpointListener tbl = new TestBreakpointListener 
                ("a", bp.getStopLine("a"), 1);
            mb1.addJPDABreakpointListener (tbl);
            dm.addBreakpoint(mb1);

            MethodBreakpoint mb2 = MethodBreakpoint.create (CLASS_NAME, "b");
            TestBreakpointListener tb2 = new TestBreakpointListener 
                ("b", bp.getStopLine("b"), 2);
            mb2.addJPDABreakpointListener (tb2);
            dm.addBreakpoint (mb2);

            MethodBreakpoint mb3 = MethodBreakpoint.create (CLASS_NAME, "c");
            TestBreakpointListener tb3 = new TestBreakpointListener
                ("c", bp.getStopLine("c"), 4);
            mb3.addJPDABreakpointListener (tb3);
            dm.addBreakpoint (mb3);

            MethodBreakpoint mb4 = MethodBreakpoint.create 
                (CLASS_NAME, "<init>");
            TestBreakpointListener tb4 = new TestBreakpointListener
                ("<init>", bp.getStopLine("init"), 1);
            mb4.addJPDABreakpointListener (tb4);
            dm.addBreakpoint (mb4);

            MethodBreakpoint mb5 = MethodBreakpoint.create
                (CLASS_NAME, "<clinit>");
            TestBreakpointListener tb5 = new TestBreakpointListener
                ("<clinit>", bp.getStopLine("cinit"), 1);
            mb5.addJPDABreakpointListener (tb5);
            dm.addBreakpoint (mb5);

            MethodBreakpoint mb6 = MethodBreakpoint.create (
                CLASS_NAME + "$InnerStatic", 
                "<clinit>"
            );
            TestBreakpointListener tb6 = new TestBreakpointListener (
                "InnerStatic.<clinit>", 
                bp.getStopLine("InnerStatic.cinit"), 
                1
            );
            mb6.addJPDABreakpointListener (tb6);
            dm.addBreakpoint (mb6);

            MethodBreakpoint mb7 = MethodBreakpoint.create (
                CLASS_NAME + "$InnerStatic", 
                "getW"
            );
            TestBreakpointListener tb7 = new TestBreakpointListener (
                "InnerStatic.getW", 
                bp.getStopLine("InnerStatic.getW"), 
                1
            );
            mb7.addJPDABreakpointListener (tb7);
            dm.addBreakpoint (mb7);

            MethodBreakpoint mb8 = MethodBreakpoint.create (
                CLASS_NAME + "$Inner", 
                "<init>"
            );
            TestBreakpointListener tb8 = new TestBreakpointListener (
                "Inner.<init>", 
                bp.getStopLine("Inner.init"), 
                4
            );
            mb8.addJPDABreakpointListener (tb8);
            dm.addBreakpoint (mb8);

            MethodBreakpoint mb9 = MethodBreakpoint.create (
                CLASS_NAME + "$Inner", 
                "getW"
            );
            TestBreakpointListener tb9 = new TestBreakpointListener (
                "Inner.getW", 
                bp.getStopLine("Inner.getW"), 
                8
            );
            mb9.addJPDABreakpointListener (tb9);
            dm.addBreakpoint (mb9);

            support = JPDASupport.attach (CLASS_NAME);

            for (;;) {
                support.waitState (JPDADebugger.STATE_STOPPED);
                if (support.getDebugger ().getState () == 
                    JPDADebugger.STATE_DISCONNECTED
                ) break;
                support.doContinue ();
            }
            tbl.assertFailure ();
            tb2.assertFailure ();
            tb3.assertFailure ();
            tb4.assertFailure ();
            tb5.assertFailure ();
            tb6.assertFailure ();
            tb7.assertFailure ();
            tb8.assertFailure ();
            tb9.assertFailure ();

            dm.removeBreakpoint (mb1);
            dm.removeBreakpoint (mb2);
            dm.removeBreakpoint (mb3);
            dm.removeBreakpoint (mb4);
            dm.removeBreakpoint (mb5);
            dm.removeBreakpoint (mb6);
            dm.removeBreakpoint (mb7);
            dm.removeBreakpoint (mb8);
            dm.removeBreakpoint (mb9);
        } finally {
            support.doFinish();
        }
    }

    public void testMethodExitBreakpoints() throws Exception {
        Utils.BreakPositions bp = Utils.getBreakPositions(System.getProperty ("test.dir.src") + 
                "org/netbeans/api/debugger/jpda/testapps/MethodBreakpointApp.java");
        try {
            MethodBreakpoint mb1 = MethodBreakpoint.create (
                CLASS_NAME + "$AbstractInner", "compute"
            );
            mb1.setBreakpointType(MethodBreakpoint.TYPE_METHOD_EXIT);
            TestBreakpointListener tbl = new TestBreakpointListener 
                ("compute", bp.getStopLine("Rcompute"), 1, "1.0");
            mb1.addJPDABreakpointListener (tbl);
            dm.addBreakpoint(mb1);
            
            MethodBreakpoint mb2 = MethodBreakpoint.create (
                CLASS_NAME + "$InterfaceInner", "getString"
            );
            mb2.setBreakpointType(MethodBreakpoint.TYPE_METHOD_EXIT);
            TestBreakpointListener tb2 = new TestBreakpointListener 
                ("getString", bp.getStopLine("RgetString"), 1, "\"Hello\"");
            mb2.addJPDABreakpointListener (tb2);
            dm.addBreakpoint(mb2);
            
            support = JPDASupport.attach (CLASS_NAME);

            for (;;) {
                support.waitState (JPDADebugger.STATE_STOPPED);
                if (support.getDebugger ().getState () == 
                    JPDADebugger.STATE_DISCONNECTED
                ) break;
                support.doContinue ();
            }
            tbl.assertFailure ();
            tb2.assertFailure ();

            dm.removeBreakpoint (mb1);
            dm.removeBreakpoint (mb2);
        } finally {
            support.doFinish();
        }
    }

    private class TestBreakpointListener implements JPDABreakpointListener {

        private int                 hitCount;
        private AssertionError      failure;
        private String methodName;
        private int hitLine;
        private int expectedHitCount;
        private String returnValue;

        public TestBreakpointListener (
            String methodName, 
            int hitLine, 
            int expectedHitCount
        ) {
            this.methodName = methodName;
            this.hitLine = hitLine;
            this.expectedHitCount = expectedHitCount;
        }

        public TestBreakpointListener (
            String methodName, 
            int hitLine, 
            int expectedHitCount,
            String returnValue
        ) {
            this(methodName, hitLine, expectedHitCount);
            this.returnValue = returnValue;
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

        private void checkEvent (JPDABreakpointEvent event) {
            MethodBreakpoint mb = (MethodBreakpoint) event.getSource ();

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
                event.getThread ().getLineNumber (null)
            );
            assertEquals (
                "Breakpoint event: Hit at wrong method", 
                mb.getMethodName (), 
                event.getThread ().getMethodName ()
            );
            
            if (returnValue != null) {
                Variable retVar = event.getVariable();
                assertNotNull(
                        "Breakpoint event: The return value must not be null!",
                        retVar);
                ReturnVariable returnVariable = (ReturnVariable) retVar;
                assertEquals(
                        "Breakpoint event: Wrong method name hit",
                        methodName, returnVariable.methodName());
                assertEquals(
                        "Breakpoint event: Wrong return value",
                        returnValue, returnVariable.getValue());
            }

            hitCount++;
        }

        public void assertFailure () {
            if (failure != null) 
                throw failure;
            assertEquals (
                "Breakpoint hit count mismatch for: " + methodName, 
                expectedHitCount, 
                hitCount
            );
        }
    }
}
