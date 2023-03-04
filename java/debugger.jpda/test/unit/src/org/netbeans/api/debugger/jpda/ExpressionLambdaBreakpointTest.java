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

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import junit.framework.Test;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointListener;
import org.netbeans.junit.NbTestCase;

/**
 * Tests lambda expression stepping action
 *
 * @author aksinsin
 */
public class ExpressionLambdaBreakpointTest extends NbTestCase {

    private static final String TEST_APP_PATH = System.getProperty ("test.dir.src") + 
        "org/netbeans/api/debugger/jpda/testapps/ExpressionLambdaBreakpointApp.java";
    
    private JPDASupport support;
    
    public ExpressionLambdaBreakpointTest (String s) {
        super (s);
    }

    public static Test suite() {
        return JPDASupport.createTestSuite(ExpressionLambdaBreakpointTest.class);
    }
    
    public void testLambdaBreakpoints() throws Exception {
        try {
            Utils.BreakPositions bp = Utils.getBreakPositions(TEST_APP_PATH);
            LineBreakpoint[] lb = bp.getBreakpoints().toArray(new LineBreakpoint[0]);
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
                "org.netbeans.api.debugger.jpda.testapps.ExpressionLambdaBreakpointApp"
            );
            
            JPDADebugger debugger = support.getDebugger();
            int lambdaBpLineHitCount = 6; //total list vaues + 1
            for (int j = 0; j < lambdaBpLineHitCount; j++) {
                support.waitState (JPDADebugger.STATE_STOPPED);  // j-th breakpoint hit
                assertEquals (
                    "Debugger stopped at wrong line for breakpoint", 
                    lb[0].getLineNumber (), 
                    debugger.getCurrentCallStackFrame ().getLineNumber (null)
                );
                
                if (j == 0) {
                    support.stepOver();
                    assertEquals (
                    "Debugger stopped at wrong line for breakpoint", 
                    lb[0].getLineNumber ()+ 1, 
                    debugger.getCurrentCallStackFrame ().getLineNumber (null)
                );
                }
                support.doContinue();
            }
            
            
            support.waitState (JPDADebugger.STATE_STOPPED);
            
            for (int i = 0; i < tb.length; i++) {
                tb[i].checkResult ();
            }
            for (int i = 0; i < tb.length; i++) {
                dm.removeBreakpoint (lb[i]);
            }
            Map<String, Variable> variablesByName = getVariablesByName(debugger.getCurrentCallStackFrame ().getLocalVariables());
            assertTrue("Wrong computation value of lambda expression filter",checkMirrorValues(variablesByName, new Object[]{"a","b","c"}));
            
            support.doContinue ();
            support.waitState (JPDADebugger.STATE_DISCONNECTED);
        } finally {
            if (support != null) support.doFinish ();
        }
    }

    private static Map<String, Variable> getVariablesByName(LocalVariable[] vars) {
        Map<String, Variable> map = new LinkedHashMap<>();
        for (LocalVariable lv : vars) {
            assertTrue("Not mutable", lv instanceof MutableVariable);
            map.put(lv.getName(), lv);
        }
        return map;
    }
    
    private boolean checkMirrorValues(Map<String, Variable>  mirrorValues, Object[] actualValues){
        String variableNameKey = "nonEmptyListCollection";
        if(mirrorValues.containsKey(variableNameKey)){
            Variable get = mirrorValues.get(variableNameKey);
            List<String> list = (List)get.createMirrorObject();
            return list.equals(Arrays.asList(actualValues));
        }
        return false;
    }
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
        
    }
    
}
