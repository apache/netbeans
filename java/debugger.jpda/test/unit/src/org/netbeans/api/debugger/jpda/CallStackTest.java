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

import java.util.*;
import junit.framework.Test;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.debugger.jpda.SourcePath;
import org.netbeans.spi.debugger.jpda.SourcePathProvider;


/**
 * Tests information about stack call stacks.
 *
 * @author Maros Sandor
 */
public class CallStackTest extends NbTestCase {


    public CallStackTest (String s) {
        super (s);
    }
    
    public static Test suite() {
        return JPDASupport.createTestSuite(CallStackTest.class);
    }

    public void testInstanceCallStackInfo () throws Exception {
        JPDASupport support = null;
        try {
            JPDASupport.removeAllBreakpoints ();
            Utils.BreakPositions bp = Utils.getBreakPositions(System.getProperty ("test.dir.src") + 
                    "org/netbeans/api/debugger/jpda/testapps/CallStackApp.java");
            LineBreakpoint lb = bp.getLineBreakpoints().get(0);
            //lb.setPreferredClassName("org.netbeans.api.debugger.jpda.testapps.CallStackApp");
            DebuggerManager.getDebuggerManager ().addBreakpoint (lb);
            support = JPDASupport.attach (
                "org.netbeans.api.debugger.jpda.testapps.CallStackApp"
            );
            support.waitState (JPDADebugger.STATE_STOPPED);
            support.stepOver ();
            support.stepInto ();
            support.stepOver ();
            support.stepInto ();
            support.stepOver ();

            CallStackFrame sf = support.getDebugger ().
                getCurrentCallStackFrame ();

            List strata = sf.getAvailableStrata ();
            assertEquals (
                "Available strata", 
                1, 
                strata.size ()
            );
            assertEquals (
                "Java stratum is not available", 
                "Java", 
                strata.get (0)
            );
            assertEquals (
                "Java stratum is not default", 
                "Java", 
                sf.getDefaultStratum ()
            );
            assertEquals (
                "Wrong class name", 
                "org.netbeans.api.debugger.jpda.testapps.CallStackApp", 
                sf.getClassName ()
            );
            assertEquals (
                "Wrong line number", 
                bp.getStopLine("CallStackEval"), 
                sf.getLineNumber (null)
            );
            LocalVariable [] vars = sf.getLocalVariables ();
            assertEquals (
                "Wrong number of local variables", 
                1, vars.length
            );
            assertEquals (
                "Wrong info about local variables", 
                "im2", 
                vars [0].getName ()
            );
            assertEquals (
                "Wrong info about current method", 
                "m2", 
                sf.getMethodName ()
            );
            assertNotNull (
                "Wrong info about this object", 
                sf.getThisVariable ()
            );
            assertFalse (
                "Wrong info about obsolete method", 
                sf.isObsolete ()
            );

            JPDAThread thread = sf.getThread ();
            assertEquals (
                "Callstack and Thread info mismatch", 
                thread.getCallStack () [0], 
                sf
            );
            assertEquals (
                "Callstack and Thread info mismatch", 
                thread.getClassName (), 
                sf.getClassName ()
            );
            assertEquals (
                "Callstack and Thread info mismatch", 
                thread.getMethodName (), 
                sf.getMethodName ()
            );
            assertEquals (
                "Callstack and Thread info mismatch", 
                thread.getSourceName (null), 
                sf.getSourceName (null)
            );
        } finally {
            if (support != null)
                support.doFinish ();
        }
    }

    public void testStaticCallStackInfo() throws Exception {
        JPDASupport support = null;
        try {
            JPDASupport.removeAllBreakpoints ();
            Utils.BreakPositions bp = Utils.getBreakPositions(System.getProperty ("test.dir.src") + 
                    "org/netbeans/api/debugger/jpda/testapps/CallStackApp.java");
            LineBreakpoint lb = bp.getLineBreakpoints().get(0);
            lb.setPreferredClassName("org.netbeans.api.debugger.jpda.testapps.CallStackApp");
            DebuggerManager.getDebuggerManager ().addBreakpoint (lb);
            support = JPDASupport.attach (
                "org.netbeans.api.debugger.jpda.testapps.CallStackApp"
            );
            support.waitState (JPDADebugger.STATE_STOPPED);
            CallStackFrame sf = support.getDebugger ().
                getCurrentCallStackFrame ();

            List strata = sf.getAvailableStrata ();
            assertEquals (
                "Available strata", 1, strata.size ()
            );
            assertEquals (
                "Java stratum is not available", "Java", strata.get (0)
            );
            assertEquals (
                "Java stratum is not default", "Java", sf.getDefaultStratum ()
            );
            assertEquals (
                "Wrong class name", 
                "org.netbeans.api.debugger.jpda.testapps.CallStackApp", 
                sf.getClassName ()
            );
            assertEquals (
                "Wrong line number", 
                lb.getLineNumber(), 
                sf.getLineNumber (null)
            );

            LocalVariable [] vars = sf.getLocalVariables ();
            assertEquals (
                "Wrong number of local variables", 1, vars.length
            );
            assertEquals (
                "Wrong info about local variables", 
                "args", 
                vars[0].getName ()
            );
            assertEquals (
                "Wrong info about current method", 
                "main", 
                sf.getMethodName ()
            );
            assertNull (
                "Wrong info about this object", sf.getThisVariable ()
            );
            assertFalse (
                "Wrong info about obsolete method", sf.isObsolete ()
            );

            JPDAThread thread = sf.getThread();
            assertEquals (
                "Callstack and Thread info mismatch", 
                thread.getCallStack () [0], 
                sf
            );
            assertEquals (
                "Callstack and Thread info mismatch", 
                thread.getClassName (), 
                sf.getClassName ()
            );
            assertEquals (
                "Callstack and Thread info mismatch", 
                thread.getMethodName (), 
                sf.getMethodName ()
            );
            assertEquals (
                "Callstack and Thread info mismatch", 
                thread.getSourceName (null), 
                sf.getSourceName (null)
            );
        } finally {
            if (support != null)
                support.doFinish ();
        }
    }
}
