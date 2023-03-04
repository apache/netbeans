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

import com.sun.jdi.StackFrame;
import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointListener;
import org.netbeans.junit.NbTestCase;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import junit.framework.Test;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.models.JPDAThreadImpl;


/**
 * Tests JPDAstep (step in, step out and step over).
 *
 * @author Roman Ondruska
 */
public class JPDAStepTest extends NbTestCase {

    private DebuggerManager dm = DebuggerManager.getDebuggerManager ();
    private String          sourceRoot = System.getProperty ("test.dir.src");
    private JPDASupport     support;
    
    private Object STEP_LOCK = new Object();
    
    private boolean stepExecFired = false;

    public JPDAStepTest (String s) {
        super (s);
    }

    public static Test suite() {
        return JPDASupport.createTestSuite(JPDAStepTest.class);
    }
    
     public void testStepInto () throws Exception {
        try {
            JPDASupport.removeAllBreakpoints ();
            Utils.BreakPositions bp = Utils.getBreakPositions(sourceRoot + 
                    "org/netbeans/api/debugger/jpda/testapps/StepApp.java");
            LineBreakpoint lb = bp.getLineBreakpoints().get(0);
            dm.addBreakpoint (lb);
            lb.addJPDABreakpointListener(new JPDABreakpointListener() {
                public void breakpointReached(JPDABreakpointEvent event) {
                    System.err.println("Breakpoint Reached: "+event.getSource());
                }
            });
            support = JPDASupport.attach
                ("org.netbeans.api.debugger.jpda.testapps.StepApp");
            support.waitState (JPDADebugger.STATE_STOPPED);
            dm.removeBreakpoint (lb);
            assertEquals (
                "Execution stopped in wrong class on breakpoint", 
                getCurrentClassName(),
                "org.netbeans.api.debugger.jpda.testapps.StepApp"
            );
            assertEquals (
                "Execution stopped at wrong line", 
                lb.getLineNumber(), 
                getCurrentLineNumber()
            );
            stepCheck (
                JPDAStep.STEP_OVER, 
                "org.netbeans.api.debugger.jpda.testapps.StepApp", 
                lb.getLineNumber() + 1
            );
            stepCheck (
                JPDAStep.STEP_INTO, 
                "org.netbeans.api.debugger.jpda.testapps.StepApp", 
                bp.getStopLine("Into1")
            );
            stepCheck (
                JPDAStep.STEP_INTO, 
                "org.netbeans.api.debugger.jpda.testapps.StepApp", 
                bp.getStopLine("Into2")
            );
            stepCheck (
                JPDAStep.STEP_INTO, 
                "org.netbeans.api.debugger.jpda.testapps.StepApp", 
                bp.getStopLine("Into3")
            );
            stepCheck (
                JPDAStep.STEP_OVER, 
                "org.netbeans.api.debugger.jpda.testapps.StepApp", 
                bp.getStopLine("Over4")
            );
            stepCheck (
                JPDAStep.STEP_INTO, 
                "org.netbeans.api.debugger.jpda.testapps.StepApp", 
                bp.getStopLine("Into5")
            );
           
            // resume VM
            ((JPDADebuggerImpl)support.getDebugger()).getVirtualMachine().resume();
            support.waitState (JPDADebugger.STATE_DISCONNECTED);
         
        } finally {
            support.doFinish ();
        }
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
                getCurrentClassName(),
                "org.netbeans.api.debugger.jpda.testapps.StepApp"
            );
            assertEquals (
                "Execution stopped at wrong line", 
                line, 
                getCurrentLineNumber()
            );
            stepCheck (
                JPDAStep.STEP_OVER, 
                "org.netbeans.api.debugger.jpda.testapps.StepApp", 
                ++line
            );
            stepCheck (
                JPDAStep.STEP_OVER, 
                "org.netbeans.api.debugger.jpda.testapps.StepApp", 
                ++line
            );
            stepCheck (
                JPDAStep.STEP_OVER, 
                "org.netbeans.api.debugger.jpda.testapps.StepApp", 
                ++line
            );
            stepCheck (
                JPDAStep.STEP_OVER, 
                "org.netbeans.api.debugger.jpda.testapps.StepApp", 
                ++line
            );
            stepCheck (
                JPDAStep.STEP_OVER, 
                "org.netbeans.api.debugger.jpda.testapps.StepApp", 
                ++line
            );
            
            // resume VM
            ((JPDADebuggerImpl)support.getDebugger()).getVirtualMachine().resume();
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
                getCurrentClassName(), 
                "org.netbeans.api.debugger.jpda.testapps.StepApp"
            );
            int line = lb.getLineNumber();
            assertEquals (
                "Execution stopped at wrong line", 
                line,
                getCurrentLineNumber()
            );
            stepCheck (
                JPDAStep.STEP_OVER, 
                "org.netbeans.api.debugger.jpda.testapps.StepApp", 
                line+1
            );
            stepCheck (
                JPDAStep.STEP_INTO, 
                "org.netbeans.api.debugger.jpda.testapps.StepApp", 
                bp.getStopLine("Into1")
            );
            stepCheck (
                JPDAStep.STEP_OVER, 
                "org.netbeans.api.debugger.jpda.testapps.StepApp", 
                bp.getStopLine("Into2")
            );
            stepCheck (
                JPDAStep.STEP_INTO, 
                "org.netbeans.api.debugger.jpda.testapps.StepApp", 
                bp.getStopLine("Into3")
            );
            stepCheck (
                JPDAStep.STEP_OUT, 
                "org.netbeans.api.debugger.jpda.testapps.StepApp", 
                bp.getStopLine("Into2")
            );
            stepCheck (
                JPDAStep.STEP_OUT, 
                "org.netbeans.api.debugger.jpda.testapps.StepApp", 
                line+1
            );
            
            // resume VM
            ((JPDADebuggerImpl)support.getDebugger()).getVirtualMachine().resume();
            support.waitState (JPDADebugger.STATE_DISCONNECTED);
        } finally {
            support.doFinish ();
        }
    }
    
    private void stepCheck (
        int stepType, 
        String clsExpected, 
        int lineExpected
    ) {
        stepExecFired = false;
        JPDAStep step = support.getDebugger().createJPDAStep(JPDAStep.STEP_LINE, stepType);
 
        step.addPropertyChangeListener(JPDAStep.PROP_STATE_EXEC, new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent evt) {
                        synchronized (STEP_LOCK) {
                            stepExecFired = true;
                            STEP_LOCK.notify();
                        }
                        
                        
                    }
        });
        step.addStep(support.getDebugger().getCurrentThread());
        ((JPDADebuggerImpl)support.getDebugger()).getVirtualMachine().resume();              
     
        synchronized (STEP_LOCK) {
            while (! stepExecFired) {
                try {
                    STEP_LOCK.wait ();
                } catch (InterruptedException ex) {
                    ex.printStackTrace ();
                }
            }
        }

        assertEquals(
            "Execution stopped in wrong class", 
            clsExpected, 
            getCurrentClassName()
        );
        assertEquals(
            "Execution stopped at wrong line", 
            lineExpected, 
            getCurrentLineNumber()
        );
        
    }
    
    private String getCurrentClassName() {
        JPDADebuggerImpl debugger = (JPDADebuggerImpl) support.getDebugger();
        
        String className = null;
        
        try {
            JPDAThreadImpl jpdaThread = (JPDAThreadImpl) debugger.getCurrentThread();
            if (jpdaThread == null) {
                System.err.println("NULL Current Thread!");
                Thread.dumpStack();
            } else {
                StackFrame sf = jpdaThread.getThreadReference().frame(0);
                if (sf == null) {
                    System.err.println("No stack frame!");
                    Thread.dumpStack();
                } else {
                    className = sf.location().declaringType().name();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return className;
    }
    
    private int getCurrentLineNumber() {
        JPDADebuggerImpl debugger = (JPDADebuggerImpl) support.getDebugger();
        
        int lineNumber = -1;
        
        try {
            lineNumber = ((JPDAThreadImpl)debugger.getCurrentThread()).
                getThreadReference().frame(0).location().lineNumber();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lineNumber;
    }
        
}

