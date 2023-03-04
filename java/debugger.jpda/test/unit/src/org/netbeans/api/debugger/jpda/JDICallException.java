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
import org.netbeans.api.debugger.jpda.Utils.BreakPositions;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointEvent;
import org.netbeans.api.debugger.jpda.event.JPDABreakpointListener;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Exceptions;

/**
 *
 * @author felipee
 */
public class JDICallException extends NbTestCase {

    private DebuggerManager dm = DebuggerManager.getDebuggerManager();
    private String sourceRoot = System.getProperty("test.dir.src");
    private JPDASupport support;
    private Object STEP_LOCK = new Object();
    private boolean stepExecFired = false;

    public JDICallException(String s) {
        super(s);
    }

    public static Test suite() {
        return JPDASupport.createTestSuite(JDICallException.class);
    }
    
    public void testInvokeException() throws Exception {


        try {

            JPDASupport.removeAllBreakpoints();
            BreakPositions bp;

            bp = Utils.getBreakPositions(sourceRoot + "org/netbeans/api/debugger/jpda/testapps/StepApp.java");

            LineBreakpoint lb = bp.getLineBreakpoints().get(0);
            dm.addBreakpoint(lb);
            lb.addJPDABreakpointListener(new JPDABreakpointListener() {

                public void breakpointReached(JPDABreakpointEvent event) {
                    System.err.println("Breakpoint Reached: " + event.getSource());
                }
            });

            support = JPDASupport.attach("org.netbeans.api.debugger.jpda.testapps.StepApp2");
            support.waitState(JPDADebugger.STATE_STOPPED);
            dm.removeBreakpoint(lb);
            JPDASupport.removeAllBreakpoints();
        } finally {
            support.doFinish();
        }

    }
}
