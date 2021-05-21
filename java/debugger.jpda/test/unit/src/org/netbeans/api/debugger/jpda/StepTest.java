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

package org.netbeans.api.debugger.jpda;

import junit.framework.Test;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.junit.NbTestCase;


/**
 * Tests JPDA stepping actions: step in, step out and step over.
 *
 * @author Maros Sandor, Jan Jancura
 */
public class StepTest extends NbTestCase {

    private DebuggerManager dm = DebuggerManager.getDebuggerManager ();
    private String          sourceRoot = System.getProperty ("test.dir.src");
    private JPDASupport     support;

    public StepTest (String s) {
        super (s);
    }

    public static Test suite() {
        return JPDASupport.createTestSuite(StepTest.class);
    }
    
    public void testStepOver () throws Exception {
        try {
            JPDASupport.removeAllBreakpoints ();
            Utils.BreakPositions bp = Utils.getBreakPositions(sourceRoot + 
                    "org/netbeans/api/debugger/jpda/testapps/StepApp.java");
            LineBreakpoint lb = bp.getLineBreakpoints().get(0);
            dm.addBreakpoint (lb);
            support = JPDASupport.attach
                ("org.netbeans.api.debugger.jpda.testapps.StepApp");
            support.waitState (JPDADebugger.STATE_STOPPED);
            dm.removeBreakpoint (lb);
            int line = lb.getLineNumber();
            assertEquals (
                "Execution stopped in wrong class", 
                support.getDebugger ().getCurrentCallStackFrame ().
                    getClassName (), 
                "org.netbeans.api.debugger.jpda.testapps.StepApp"
            );
            assertEquals (
                "Execution stopped at wrong line", 
                line, 
                support.getDebugger ().getCurrentCallStackFrame ().
                    getLineNumber (null)
            );
            stepCheck (
                ActionsManager.ACTION_STEP_OVER, 
                "org.netbeans.api.debugger.jpda.testapps.StepApp", 
                ++line
            );
            stepCheck (
                ActionsManager.ACTION_STEP_OVER, 
                "org.netbeans.api.debugger.jpda.testapps.StepApp", 
                ++line
            );
            stepCheck (
                ActionsManager.ACTION_STEP_OVER, 
                "org.netbeans.api.debugger.jpda.testapps.StepApp", 
                ++line
            );
            stepCheck (
                ActionsManager.ACTION_STEP_OVER, 
                "org.netbeans.api.debugger.jpda.testapps.StepApp", 
                ++line
            );
            stepCheck (
                ActionsManager.ACTION_STEP_OVER, 
                "org.netbeans.api.debugger.jpda.testapps.StepApp", 
                ++line
            );
            support.doContinue ();
            support.waitState (JPDADebugger.STATE_DISCONNECTED);
        } finally {
            support.doFinish ();
        }
    }

    public void testStepInto () throws Exception {
        try {
            JPDASupport.removeAllBreakpoints ();
            Utils.BreakPositions bp = Utils.getBreakPositions(sourceRoot + 
                    "org/netbeans/api/debugger/jpda/testapps/StepApp.java");
            LineBreakpoint lb = bp.getLineBreakpoints().get(0);
            dm.addBreakpoint (lb);
            support = JPDASupport.attach
                ("org.netbeans.api.debugger.jpda.testapps.StepApp");
            support.waitState (JPDADebugger.STATE_STOPPED);
            dm.removeBreakpoint (lb);
            assertEquals (
                "Execution stopped in wrong class", 
                support.getDebugger ().getCurrentCallStackFrame ().
                    getClassName (), 
                "org.netbeans.api.debugger.jpda.testapps.StepApp"
            );
            assertEquals (
                "Execution stopped at wrong line", 
                lb.getLineNumber(), 
                support.getDebugger ().getCurrentCallStackFrame ().
                    getLineNumber (null)
            );

            stepCheck (
                ActionsManager.ACTION_STEP_INTO, 
                "org.netbeans.api.debugger.jpda.testapps.StepApp", 
                bp.getStopLine("1into")
            );
//            stepCheck (ActionsManager.ACTION_STEP_INTO, "java.lang.Object", -1);
            stepCheck (
                ActionsManager.ACTION_STEP_OVER, 
                "org.netbeans.api.debugger.jpda.testapps.StepApp", 
                bp.getStopLine("1into") + 1
            );
            stepCheck (
                ActionsManager.ACTION_STEP_OVER, 
                "org.netbeans.api.debugger.jpda.testapps.StepApp", 
                lb.getLineNumber()
            );
            stepCheck (
                ActionsManager.ACTION_STEP_OVER, 
                "org.netbeans.api.debugger.jpda.testapps.StepApp", 
                lb.getLineNumber() + 1
            );
            stepCheck (
                ActionsManager.ACTION_STEP_INTO, 
                "org.netbeans.api.debugger.jpda.testapps.StepApp", 
                bp.getStopLine("Into1")
            );
            stepCheck (
                ActionsManager.ACTION_STEP_OVER, 
                "org.netbeans.api.debugger.jpda.testapps.StepApp", 
                bp.getStopLine("Into2")
            );
            stepCheck (
                ActionsManager.ACTION_STEP_INTO, 
                "org.netbeans.api.debugger.jpda.testapps.StepApp", 
                bp.getStopLine("Into3")
            );

            support.doContinue ();
            support.waitState (JPDADebugger.STATE_DISCONNECTED);
        } finally {
            support.doFinish ();
        }
    }

    public void testStepOut () throws Exception {
        try {
            JPDASupport.removeAllBreakpoints ();
            Utils.BreakPositions bp = Utils.getBreakPositions(sourceRoot + 
                    "org/netbeans/api/debugger/jpda/testapps/StepApp.java");
            LineBreakpoint lb = bp.getLineBreakpoints().get(0);
            dm.addBreakpoint (lb);
            support = JPDASupport.attach
                ("org.netbeans.api.debugger.jpda.testapps.StepApp");
            support.waitState (JPDADebugger.STATE_STOPPED);
            dm.removeBreakpoint (lb);
            assertEquals (
                "Execution stopped in wrong class", 
                support.getDebugger ().getCurrentCallStackFrame ().
                    getClassName (), 
                "org.netbeans.api.debugger.jpda.testapps.StepApp"
            );
            assertEquals (
                "Execution stopped at wrong line", 
                lb.getLineNumber(), 
                support.getDebugger ().getCurrentCallStackFrame ().
                    getLineNumber (null)
            );
            stepCheck (
                ActionsManager.ACTION_STEP_OVER, 
                "org.netbeans.api.debugger.jpda.testapps.StepApp", 
                lb.getLineNumber() + 1
            );
            stepCheck (
                ActionsManager.ACTION_STEP_INTO, 
                "org.netbeans.api.debugger.jpda.testapps.StepApp", 
                bp.getStopLine("Into1")
            );
            stepCheck (
                ActionsManager.ACTION_STEP_OVER, 
                "org.netbeans.api.debugger.jpda.testapps.StepApp", 
                bp.getStopLine("Into2")
            );
            stepCheck (
                ActionsManager.ACTION_STEP_INTO, 
                "org.netbeans.api.debugger.jpda.testapps.StepApp", 
                bp.getStopLine("Into3")
            );
            stepCheck (
                ActionsManager.ACTION_STEP_OUT, 
                "org.netbeans.api.debugger.jpda.testapps.StepApp", 
                bp.getStopLine("Into2")
            );
            stepCheck (
                ActionsManager.ACTION_STEP_OUT, 
                "org.netbeans.api.debugger.jpda.testapps.StepApp", 
                lb.getLineNumber() + 1
            );

            support.doContinue ();
            support.waitState (JPDADebugger.STATE_DISCONNECTED);
        } finally {
            support.doFinish ();
        }
    }

    private void stepCheck (
        Object stepType, 
        String clsExpected, 
        int lineExpected
    ) {
        support.step (stepType);
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
