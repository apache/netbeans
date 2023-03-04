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

import java.io.IOException;

import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import junit.framework.Test;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointListener;
import org.netbeans.junit.NbTestCase;

/**
 * Tests line breakpoints at various places.
 *
 * @author Maros Sandor, Jan Jancura
 */
public class LineBreakpointTest extends NbTestCase {

    private static final String TEST_APP = Utils.getURL(System.getProperty ("test.dir.src") + 
        "org/netbeans/api/debugger/jpda/testapps/LineBreakpointApp.java");
    private static final String TEST_APP_PATH = System.getProperty ("test.dir.src") + 
        "org/netbeans/api/debugger/jpda/testapps/LineBreakpointApp.java";
    
    private JPDASupport support;
    
    
    public LineBreakpointTest (String s) {
        super (s);
    }

    public static Test suite() {
        return JPDASupport.createTestSuite(LineBreakpointTest.class);
    }
    
    public void testConditionalBreakpoint() throws Exception {
        Utils.BreakPositions bp = Utils.getBreakPositions(TEST_APP_PATH);
        doTestBreakpointComplete (
            bp.getStopLine("condition1"), 
            "x==22", 
            JPDABreakpointEvent.CONDITION_FALSE
        );
        doTestBreakpointComplete (
            bp.getStopLine("condition2"), 
            "x==60", 
            JPDABreakpointEvent.CONDITION_TRUE
        );
    }

    public void testMultipleLineBreakpoints () throws Exception {
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
            
            for (int j = 0; j < lb.length; j++) {
                support.waitState (JPDADebugger.STATE_STOPPED);  // j-th breakpoint hit
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
            }
            
            for (int i = 0; i < tb.length; i++) {
                tb[i].checkResult ();
            }
            for (int i = 0; i < tb.length; i++) {
                dm.removeBreakpoint (lb[i]);
            }
            support.doContinue ();
            support.waitState (JPDADebugger.STATE_DISCONNECTED);
        } finally {
            if (support != null) support.doFinish ();
        }
    }

    public void testStaticBlockBreakpoint() throws Exception {
        Utils.BreakPositions bp = Utils.getBreakPositions(TEST_APP_PATH);
        doTestBreakpointComplete(bp.getStopLine("staticx"));
        doTestBreakpointComplete(bp.getStopLine("staticx2"));
    }

    public void testStaticInnerClassBreakpoint() throws Exception {
        Utils.BreakPositions bp = Utils.getBreakPositions(TEST_APP_PATH);
        doTestBreakpointComplete(bp.getStopLine("IS1"));
        doTestBreakpointComplete(bp.getStopLine("IS2"));
        doTestBreakpointComplete(bp.getStopLine("IS3"));
    }

    public void testMainLineBreakpoint() throws Exception {
        Utils.BreakPositions bp = Utils.getBreakPositions(TEST_APP_PATH);
        doTestBreakpointComplete(bp.getStopLine("M1"));
    }

    public void testConstructorLineBreakpoint() throws Exception {
        Utils.BreakPositions bp = Utils.getBreakPositions(TEST_APP_PATH);
        doTestBreakpointComplete(bp.getStopLine("C1"));
    }

    public void testInnerLineBreakpoint () throws Exception {
        Utils.BreakPositions bp = Utils.getBreakPositions(TEST_APP_PATH);
        doTestBreakpointComplete (bp.getStopLine("I1"));
        doTestBreakpointComplete (bp.getStopLine("I2"));
        doTestBreakpointComplete (bp.getStopLine("I3"));
    }

    private void doTestBreakpointComplete (
        int line, 
        String condition, 
        int conditionResult
    ) throws IOException, IllegalConnectorArgumentsException,
    DebuggerStartException {
        try {
            LineBreakpoint lb = doTestBreakpoint (
                line, 
                condition, 
                conditionResult
            );
            if ( condition == null || 
                 conditionResult == JPDABreakpointEvent.CONDITION_TRUE
            ) {
                support.doContinue();
                support.waitState (JPDADebugger.STATE_DISCONNECTED);
            }
            DebuggerManager.getDebuggerManager ().removeBreakpoint (lb);
        } finally {
            if (support != null) support.doFinish();
        }
    }

    private void doTestBreakpointComplete (int line) throws IOException, 
    IllegalConnectorArgumentsException, DebuggerStartException {
        doTestBreakpointComplete (
            line, 
            null, 
            JPDABreakpointEvent.CONDITION_NONE
        );
    }

    private LineBreakpoint doTestBreakpoint (
        int         line, 
        String      condition, 
        int         conditionResult
    ) throws IOException, IllegalConnectorArgumentsException, 
    DebuggerStartException {
        JPDASupport.removeAllBreakpoints ();
        LineBreakpoint lb = LineBreakpoint.create (TEST_APP, line);
        /*
        if (73 <= line && line <= 98) {
            lb.setPreferredClassName("org.netbeans.api.debugger.jpda.testapps.LineBreakpointApp$InnerStatic");
        } else if (100 <= line && line <= 115) {
            lb.setPreferredClassName("org.netbeans.api.debugger.jpda.testapps.LineBreakpointApp$Inner");
        }
         */
        lb.setCondition (condition);
        TestBreakpointListener tbl = new TestBreakpointListener 
            (lb, conditionResult);
        lb.addJPDABreakpointListener (tbl);
        DebuggerManager.getDebuggerManager ().addBreakpoint (lb);

        support = JPDASupport.attach (
            "org.netbeans.api.debugger.jpda.testapps.LineBreakpointApp"
        );

        if ( condition == null || 
             conditionResult == JPDABreakpointEvent.CONDITION_TRUE
        ) {
            support.waitState (JPDADebugger.STATE_STOPPED);
        } else {
            support.waitState (JPDADebugger.STATE_DISCONNECTED);
        }

        tbl.checkResult ();
        return lb;
    }

    
    /**
     * Tests debugger's ability to make difference between different projects
     * with the same classes while getting the locations during class-loaded event.
     *
     * 1. The user creates 2 classes: ${test.dir.src}/.../LineBreakpointApp.java
     *    and ${test.dir.src_2}/.../LineBreakpointApp.java
     * 2. Then set a breakpoint in ${test.dir.src_2}/.../LineBreakpointApp.java.
     * 
     * Debugger should stop _only_ in the second project. If debugger stopped in
     * the first one, then assertion violation would arise because of source path
     * equality test.
     */
    public void testBreakpointUnambiguity1 () throws Exception {
        try {
            Utils.BreakPositions bp = Utils.getBreakPositions(System.getProperty ("test.dir.src") + 
                    "org/netbeans/api/debugger/jpda/testapps/LineBreakpointApp.java");
            LineBreakpoint lb1 = LineBreakpoint.create (TEST_APP, bp.getStopLine("condition1"));
//            lb1.setSourceRoot(System.getProperty ("test.dir.src"));
            DebuggerManager dm = DebuggerManager.getDebuggerManager ();
            dm.addBreakpoint (lb1);
            
            TestBreakpointListener tb1 = new TestBreakpointListener (lb1);
            lb1.addJPDABreakpointListener (tb1);
            
            support = JPDASupport.attach (
                "org.netbeans.api.debugger.jpda.testapps.LineBreakpointApp"
            );
            JPDADebugger debugger = support.getDebugger();

            support.waitState (JPDADebugger.STATE_STOPPED);  // breakpoint hit, the source root is correct
            assertEquals (
                "Debugger stopped at wrong line", 
                lb1.getLineNumber (), 
                debugger.getCurrentCallStackFrame ().getLineNumber (null)
            );

            tb1.checkResult ();
            support.doContinue();
            support.waitState (JPDADebugger.STATE_DISCONNECTED);
            dm.removeBreakpoint (lb1);
            support.doFinish ();
            /*
            // Second run - BP should not be hit with a different source root - viz testBreakpointUnambiguity2()
            support = null;
            lb1 = LineBreakpoint.create (TEST_APP, 39);
            lb1.setSourceRoot(System.getProperty ("test.dir.src")+"_2");
            dm = DebuggerManager.getDebuggerManager ();
            dm.addBreakpoint (lb1);
            
            tb1 = new TestBreakpointListener (lb1);
            lb1.addJPDABreakpointListener (tb1);
            
            support = JPDASupport.attach (
                "org.netbeans.api.debugger.jpda.testapps.LineBreakpointApp"
            );
            debugger = support.getDebugger();
            
            support.waitState (JPDADebugger.STATE_STOPPED); // Stopped or disconnected
            assertEquals(
                    "Debugger should not stop on BP with faked source root",
                    debugger.getState(),
                    JPDADebugger.STATE_DISCONNECTED
            );
            tb1.checkNotNotified();
            dm.removeBreakpoint (lb1);
             */
        } finally {
            if (support != null) support.doFinish ();
        }
    }

    /**
     * Tests debugger's ability to make difference between different projects
     * with the same classes while getting the locations during class-loaded event.
     *
     * 1. The user creates 2 classes: ${test.dir.src}/.../LineBreakpointApp.java
     *    and ${test.dir.src_2}/.../LineBreakpointApp.java
     * 2. Then set a breakpoint in ${test.dir.src_2}/.../LineBreakpointApp.java.
     * 
     * Debugger should stop _only_ in the second project. If debugger stopped in
     * the first one, then assertion violation would arise because of source path
     * equality test.
     */
    public void testBreakpointUnambiguity2 () throws Exception {
        try {
            Utils.BreakPositions bp = Utils.getBreakPositions(System.getProperty ("test.dir.src") + 
                    "org/netbeans/api/debugger/jpda/testapps/LineBreakpointApp.java");
            LineBreakpoint lb1 = LineBreakpoint.create(
                    Utils.getURL(System.getProperty ("user.home") + // intentionally bad path
                    java.io.File.separator +
                    "org/netbeans/api/debugger/jpda/testapps/LineBreakpointApp.java"), bp.getStopLine("condition1"));
            //lb1.setSourceRoot(System.getProperty ("test.dir.src") + "_2");
            DebuggerManager dm = DebuggerManager.getDebuggerManager ();
            dm.addBreakpoint (lb1);
            
            TestBreakpointListener tb1 = new TestBreakpointListener (lb1);
            lb1.addJPDABreakpointListener (tb1);
            
            support = JPDASupport.attach (
                "org.netbeans.api.debugger.jpda.testapps.LineBreakpointApp"
            );
            JPDADebugger debugger = support.getDebugger();

            support.waitState (JPDADebugger.STATE_STOPPED); // Stopped or disconnected
            assertEquals(
                    "Debugger should not stop on BP with faked source root",
                    debugger.getState(),
                    JPDADebugger.STATE_DISCONNECTED
            );
            
            tb1.checkNotNotified();
            dm.removeBreakpoint (lb1);
        } finally {
            if (support != null) support.doFinish ();
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
            )
                failure = new AssertionError (event.getConditionException ());
            else 
            if (result != conditionResult)
                failure = new AssertionError (
                    "Unexpected breakpoint condition result: " + result
                );
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
            if (failure != null) throw failure;
        }
        
        public void checkNotNotified() {
            if (event != null) {
                JPDAThread t = event.getThread();
                throw new AssertionError (
                    "Breakpoint was hit (listener was notified) in thread " + t
                );
            }
            if (failure != null) throw failure;
        }
    }
}
