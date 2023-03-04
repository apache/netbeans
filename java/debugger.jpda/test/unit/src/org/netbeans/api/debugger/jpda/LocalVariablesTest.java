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
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.junit.NbTestCase;

/**
 * Tests information about local variables.
 *
 * @author Maros Sandor, Jan Jancura
 */
public class LocalVariablesTest extends NbTestCase {

    private JPDASupport     support;
    private DebuggerManager dm = DebuggerManager.getDebuggerManager ();

    private static final String CLASS_NAME =
        "org.netbeans.api.debugger.jpda.testapps.LocalVariablesApp";


    public LocalVariablesTest(String s) {
        super(s);
    }

    public static Test suite() {
        return JPDASupport.createTestSuite(LocalVariablesTest.class);
    }
    
    public void testWatches () throws Exception {
        try {
            Utils.BreakPositions bp = Utils.getBreakPositions(System.getProperty ("test.dir.src") + 
                    "org/netbeans/api/debugger/jpda/testapps/LocalVariablesApp.java");
            LineBreakpoint lb = bp.getLineBreakpoints().get(0);
            dm.addBreakpoint (lb);

            support = JPDASupport.attach (CLASS_NAME);

            support.waitState (JPDADebugger.STATE_STOPPED);  // breakpoint hit

            CallStackFrame sf = support.getDebugger ().getCurrentCallStackFrame ();
            assertEquals (
                "Debugger stopped at wrong line", 
                lb.getLineNumber (), 
                sf.getLineNumber (null)
            );

            LocalVariable [] vars = sf.getLocalVariables ();
            assertEquals (
                "Wrong number of local variables", 
                4, 
                vars.length
            );
            Arrays.sort (vars, new Comparator () {
                public int compare (Object o1, Object o2) {
                    return ((LocalVariable) o1).getName ().compareTo (
                        ((LocalVariable) o2).getName ()
                    );
                }
            });
            assertEquals (
                "Wrong info about local variables", 
                "g", 
                vars [0].getName ()
            );
            assertEquals (
                "Wrong info about local variables", 
                "20", 
                vars [0].getValue ()
            );
            assertEquals (
                "Wrong info about local variables", 
                "int", 
                vars [0].getDeclaredType ()
            );
            assertEquals (
                "Wrong info about local variables", 
                "int", 
                vars [0].getType ()
            );
            assertEquals (
                "Wrong info about local variables", 
                CLASS_NAME, 
                vars [0].getClassName ()
            );

            assertEquals (
                "Wrong info about local variables", 
                "s", 
                vars [1].getName ()
            );
            assertEquals (
                "Wrong info about local variables", 
                "\"asdfghjkl\"", 
                vars [1].getValue ()
            );
            assertEquals (
                "Wrong info about local variables", 
                "java.lang.Object", 
                vars [1].getDeclaredType ()
            );
            assertEquals (
                "Wrong info about local variables", 
                "java.lang.String", 
                vars [1].getType ()
            );
            assertEquals (
                "Wrong info about local variables", 
                CLASS_NAME, 
                vars [1].getClassName ()
            );

            assertEquals (
                "Wrong info about local variables", 
                "x", 
                vars [2].getName ()
            );
            assertEquals (
                "Wrong info about local variables", 
                "40", 
                vars [2].getValue ()
            );
            assertEquals (
                "Wrong info about local variables", 
                "int", 
                vars [2].getDeclaredType ()
            );
            assertEquals (
                "Wrong info about local variables", 
                "int", 
                vars [2].getType ()
            );
            assertEquals (
                "Wrong info about local variables", 
                CLASS_NAME, 
                vars [2].getClassName ()
            );

            assertEquals (
                "Wrong info about local variables", 
                "y", 
                vars [3].getName ()
            );
            assertEquals (
                "Wrong info about local variables", 
                "50.5", 
                vars [3].getValue ()
            );
            assertEquals (
                "Wrong info about local variables", 
                "float", 
                vars [3].getDeclaredType ()
             );
            assertEquals (
                "Wrong info about local variables", 
                "float", 
                vars [3].getType ()
            );
            assertEquals (
                "Wrong info about local variables", 
                CLASS_NAME, 
                vars [3].getClassName ()
            );

            support.stepOver ();
            support.stepOver ();

            sf = support.getDebugger ().getCurrentCallStackFrame ();
            vars = sf.getLocalVariables ();
            assertEquals ("Wrong number of local variables", 4, vars.length);
            Arrays.sort (vars, new Comparator () {
                public int compare (Object o1, Object o2) {
                    return ((LocalVariable) o1).getName ().compareTo (
                        ((LocalVariable) o2).getName ()
                    );
                }
            });
            assertEquals (
                "Wrong info about local variables", 
                "g", 
                vars [0].getName ()
            );
            assertEquals (
                "Wrong info about local variables", 
                "\"ad\"", 
                vars [0].getValue ()
            );
            assertEquals (
                "Wrong info about local variables", 
                "java.lang.CharSequence", 
                vars [0].getDeclaredType ()
            );
            assertEquals (
                "Wrong info about local variables", 
                "java.lang.String", 
                vars [0].getType ()
             );
            assertEquals (
                "Wrong info about local variables", 
                CLASS_NAME, 
                vars [0].getClassName ()
            );

        } finally {
            support.doFinish ();
        }
    }
}
