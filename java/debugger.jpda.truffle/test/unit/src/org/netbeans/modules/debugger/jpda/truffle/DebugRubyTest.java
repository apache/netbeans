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
package org.netbeans.modules.debugger.jpda.truffle;

import java.io.File;
import java.net.URL;
import junit.framework.Test;

import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.modules.debugger.jpda.truffle.breakpoints.TruffleLineBreakpoint;
import org.netbeans.modules.debugger.jpda.truffle.frames.TruffleStackFrame;
import org.netbeans.modules.debugger.jpda.truffle.vars.TruffleVariable;
import org.netbeans.modules.debugger.jpda.truffle.vars.impl.TruffleScope;
import org.netbeans.modules.javascript2.debug.breakpoints.JSLineBreakpoint;

public class DebugRubyTest extends JPDATestCase {

    public DebugRubyTest(String name) {
        super(name);
    }

    public static Test suite() {
        return createSuite(DebugRubyTest.class);
    }

    public void testRubyTypes() throws Exception {
        DebuggerManager dm = DebuggerManager.getDebuggerManager();
        File source = new File(sourceRoot, "org/netbeans/modules/debugger/jpda/truffle/scripts/Types.ruby");
        URL url = source.toURI().toURL();
        String sourcePath = source.getAbsolutePath();
        int debugLine = 46;
        String methodName = "typesTest";
        JSLineBreakpoint lb1 = new TruffleLineBreakpoint(url, debugLine);
        dm.addBreakpoint(lb1);
        runScriptUnderJPDA("ruby", source.getAbsolutePath(), support -> {
            JPDADebugger debugger = support.getDebugger();
            TruffleStackFrame frame = checkStoppedAtScript(debugger.getCurrentThread(), sourcePath, debugLine);
            assertTrue("Bad method name: " + frame.getMethodName(), frame.getMethodName().contains(methodName));
            checkVariableTypes(frame.getScopes());
            support.doContinue();
        });
    }

    private static void checkVariableTypes(TruffleScope[] scopes) {
        assertEquals(1, scopes.length);
        TruffleVariable[] variables = scopes[0].getVariables();
        assertEquals("self", variables[0].getName());
        checkVar(variables[1], "a1", "Array", "[]");
        checkVar(variables[2], "a2", "Array", "[1, 2, [3, 4]]");

        checkVar(variables[3], "b1", "", "true");
        checkVar(variables[4], "b2", "", "false");

        checkVar(variables[5], "null", "NilClass", "nil");

        checkVar(variables[6], "i1", "", "0");
        checkVar(variables[7], "i2", "", "42");
        checkVar(variables[8], "i3", "", "42.42");
        checkVar(variables[9], "i4", "", "-0.0");
        checkVar(variables[10], "i5", "", "-Infinity");
        checkVar(variables[11], "i6", "", "Infinity");
        checkVar(variables[12], "i7", "", "-Infinity");
        checkVar(variables[13], "i8", "", "NaN");

        checkVar(variables[14], "nc", "Complex", "(2+3i)");
        checkVar(variables[15], "nr", "Rational", "(11/2)");
        checkVar(variables[16], "f", "Method", null);
        assertTrue(variables[16].getValue().toString(), variables[16].getValue().toString().contains("Callable"));
        checkVar(variables[17], "d", "Time", null);
        checkVar(variables[18], "str", "String", "\"A String\"");
        checkVar(variables[19], "symbol", "Symbol", ":symbolic");
        checkVar(variables[20], "hash", "Hash", "{:a=>1, \"b\"=>2}");
    }

    private static void checkVar(TruffleVariable variable, String name, String type, String value) {
        assertEquals("Name", name, variable.getName());
        assertEquals("Type of " + name, type, variable.getType());
        if (value != null) {
            assertEquals("Value of " + name, value, variable.getValue());
        }
    }

}
