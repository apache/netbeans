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
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.ActionsManagerListener;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.junit.NbTestCase;


/**
 * Tests JPDA stepping actions: step in, step out and step over.
 *
 * @author Maros Sandor, Jan Jancura
 */
public class AsynchStepTest extends NbTestCase {

    private DebuggerManager dm = DebuggerManager.getDebuggerManager ();
    private String          sourceRoot = System.getProperty ("test.dir.src");
    private JPDASupport     support;

    public AsynchStepTest (String s) {
        super (s);
    }
    
    public static Test suite() {
        return JPDASupport.createTestSuite(AsynchStepTest.class);
    }

    public void testStepOver () throws Exception {
        try {
            JPDASupport.removeAllBreakpoints ();
            Utils.BreakPositions bp = Utils.getBreakPositions(sourceRoot + 
                    "org/netbeans/api/debugger/jpda/testapps/AsynchStepApp.java");
            LineBreakpoint lb = bp.getLineBreakpoints().get(0);
            dm.addBreakpoint (lb);
            support = JPDASupport.attach
                ("org.netbeans.api.debugger.jpda.testapps.AsynchStepApp");
            support.waitState (JPDADebugger.STATE_STOPPED);
            assertEquals("Breakpoint "+lb+" is invalid, message: "+lb.getValidityMessage(), LineBreakpoint.VALIDITY.VALID, lb.getValidity());
            dm.removeBreakpoint (lb);
            assertNotNull("No Current Thread.", support.getDebugger ().getCurrentThread());
            assertNotNull("No Current Call Stack Frame.", support.getDebugger ().getCurrentCallStackFrame());
            assertEquals (
                "Execution stopped in wrong class", 
                support.getDebugger ().getCurrentCallStackFrame ().
                    getClassName (), 
                "org.netbeans.api.debugger.jpda.testapps.AsynchStepApp"
            );
            int line = lb.getLineNumber();
            assertEquals (
                "Execution stopped at wrong line", 
                line, 
                support.getDebugger ().getCurrentCallStackFrame ().
                    getLineNumber (null)
            );
            stepCheck (
                ActionsManager.ACTION_STEP_OVER, 
                "org.netbeans.api.debugger.jpda.testapps.AsynchStepApp", 
                ++line
            );
            stepCheck (
                ActionsManager.ACTION_STEP_OVER, 
                "org.netbeans.api.debugger.jpda.testapps.AsynchStepApp", 
                ++line
            );
            stepCheck (
                ActionsManager.ACTION_STEP_OVER, 
                "org.netbeans.api.debugger.jpda.testapps.AsynchStepApp", 
                ++line
            );
            stepCheck (
                ActionsManager.ACTION_STEP_OVER, 
                "org.netbeans.api.debugger.jpda.testapps.AsynchStepApp", 
                ++line
            );
            stepCheck (
                ActionsManager.ACTION_STEP_OVER, 
                "org.netbeans.api.debugger.jpda.testapps.AsynchStepApp", 
                ++line
            );
            support.doContinue ();
            support.waitState (JPDADebugger.STATE_DISCONNECTED);
        } finally {
            if (support != null) {
                support.doFinish ();
            }
        }
    }

    public void testStepInto () throws Exception {
        try {
            JPDASupport.removeAllBreakpoints ();
            Utils.BreakPositions bp = Utils.getBreakPositions(sourceRoot + 
                    "org/netbeans/api/debugger/jpda/testapps/AsynchStepApp.java");
            LineBreakpoint lb = bp.getLineBreakpoints().get(0);
            dm.addBreakpoint (lb);
            support = JPDASupport.attach
                ("org.netbeans.api.debugger.jpda.testapps.AsynchStepApp");
            support.waitState (JPDADebugger.STATE_STOPPED);
            dm.removeBreakpoint (lb);
            assertEquals (
                "Execution stopped in wrong class", 
                support.getDebugger ().getCurrentCallStackFrame ().
                    getClassName (), 
                "org.netbeans.api.debugger.jpda.testapps.AsynchStepApp"
            );
            int line = lb.getLineNumber();
            assertEquals (
                "Execution stopped at wrong line", 
                line, 
                support.getDebugger ().getCurrentCallStackFrame ().
                    getLineNumber (null)
            );

            stepCheck (
                ActionsManager.ACTION_STEP_INTO, 
                "org.netbeans.api.debugger.jpda.testapps.AsynchStepApp", 
                bp.getStopLine("Into1")
            );
//            stepCheck (ActionsManager.ACTION_STEP_INTO, "java.lang.Object", -1);
            stepCheck (
                ActionsManager.ACTION_STEP_OVER, 
                "org.netbeans.api.debugger.jpda.testapps.AsynchStepApp", 
                bp.getStopLine("Over1")
            );
            stepCheck (
                ActionsManager.ACTION_STEP_OVER, 
                "org.netbeans.api.debugger.jpda.testapps.AsynchStepApp", 
                line
            );
            stepCheck (
                ActionsManager.ACTION_STEP_OVER, 
                "org.netbeans.api.debugger.jpda.testapps.AsynchStepApp", 
                line + 1
            );
            stepCheck (
                ActionsManager.ACTION_STEP_INTO, 
                "org.netbeans.api.debugger.jpda.testapps.AsynchStepApp", 
                bp.getStopLine("Into2")
            );
            stepCheck (
                ActionsManager.ACTION_STEP_OVER, 
                "org.netbeans.api.debugger.jpda.testapps.AsynchStepApp", 
                bp.getStopLine("Over2")
            );
            stepCheck (
                ActionsManager.ACTION_STEP_INTO, 
                "org.netbeans.api.debugger.jpda.testapps.AsynchStepApp", 
                bp.getStopLine("Into3")
            );

            support.doContinue ();
            support.waitState (JPDADebugger.STATE_DISCONNECTED);
        } finally {
            if (support != null) {
                support.doFinish ();
            }
        }
    }

    public void testStepOut () throws Exception {
        try {
            JPDASupport.removeAllBreakpoints ();
            Utils.BreakPositions bp = Utils.getBreakPositions(sourceRoot + 
                    "org/netbeans/api/debugger/jpda/testapps/AsynchStepApp.java");
            LineBreakpoint lb = bp.getLineBreakpoints().get(0);
            dm.addBreakpoint (lb);
            support = JPDASupport.attach
                ("org.netbeans.api.debugger.jpda.testapps.AsynchStepApp");
            support.waitState (JPDADebugger.STATE_STOPPED);
            dm.removeBreakpoint (lb);
            assertEquals (
                "Execution stopped in wrong class", 
                support.getDebugger ().getCurrentCallStackFrame ().
                    getClassName (), 
                "org.netbeans.api.debugger.jpda.testapps.AsynchStepApp"
            );
            int line = lb.getLineNumber();
            assertEquals (
                "Execution stopped at wrong line", 
                line, 
                support.getDebugger ().getCurrentCallStackFrame ().
                    getLineNumber (null)
            );
            stepCheck (
                ActionsManager.ACTION_STEP_OVER, 
                "org.netbeans.api.debugger.jpda.testapps.AsynchStepApp", 
                line + 1
            );
            stepCheck (
                ActionsManager.ACTION_STEP_INTO, 
                "org.netbeans.api.debugger.jpda.testapps.AsynchStepApp", 
                bp.getStopLine("Into2")
            );
            stepCheck (
                ActionsManager.ACTION_STEP_OVER, 
                "org.netbeans.api.debugger.jpda.testapps.AsynchStepApp", 
                bp.getStopLine("Over2")
            );
            stepCheck (
                ActionsManager.ACTION_STEP_INTO, 
                "org.netbeans.api.debugger.jpda.testapps.AsynchStepApp", 
                bp.getStopLine("Into3")
            );
            stepCheck (
                ActionsManager.ACTION_STEP_OUT, 
                "org.netbeans.api.debugger.jpda.testapps.AsynchStepApp", 
                bp.getStopLine("Over2")
            );
            stepCheck (
                ActionsManager.ACTION_STEP_OUT, 
                "org.netbeans.api.debugger.jpda.testapps.AsynchStepApp", 
                line + 1
            );

            support.doContinue ();
            support.waitState (JPDADebugger.STATE_DISCONNECTED);
        } finally {
            if (support != null) {
                support.doFinish ();
            }
        }
    }

    private void stepCheck (
        final Object stepType, 
        String clsExpected, 
        int lineExpected
    ) {
        final boolean[] waitLock = new boolean[] { false };
        support.stepAsynch (stepType, new ActionsManagerListener() {
            public void actionPerformed(Object action) {
                if (stepType == action) {
                    synchronized (waitLock) {
                        waitLock[0] = true;
                        waitLock.notify();
                    }
                }
            }
            public void actionStateChanged(Object action, boolean enabled) {
            }
        });
        synchronized (waitLock) {
            try {
                waitLock.wait(10000);
            } catch (InterruptedException iex) {
            }
            assertEquals("Asynchronous action did not notify...", true, waitLock[0]);
        }
        // The asynchronous action has finished, wait for the debugger to stop...
        support.waitState (JPDADebugger.STATE_STOPPED);
        assertEquals(
            "Execution stopped in wrong class", 
            clsExpected, 
            support.getDebugger ().getCurrentCallStackFrame ().getClassName ()
        );
        assertEquals (
            "Execution stopped at wrong line", 
            lineExpected, 
            support.getDebugger ().getCurrentCallStackFrame ().
                getLineNumber (null)
        );
    }
}
