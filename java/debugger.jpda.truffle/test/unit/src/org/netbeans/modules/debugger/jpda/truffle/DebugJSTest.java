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
package org.netbeans.modules.debugger.jpda.truffle;

import java.io.File;
import junit.framework.Test;
import static junit.framework.TestCase.assertEquals;

import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.modules.debugger.jpda.truffle.frames.TruffleStackFrame;
import org.netbeans.modules.debugger.jpda.truffle.vars.TruffleVariable;
import org.netbeans.modules.debugger.jpda.truffle.vars.impl.TruffleScope;

public class DebugJSTest extends JPDATestCase {

    public DebugJSTest(String name) {
        super(name);
    }

    public static Test suite() {
        return createSuite(DebugJSTest.class);
    }

    public void testJSTypes() throws Exception {
        DebuggerManager dm = DebuggerManager.getDebuggerManager();
        File source = new File(sourceRoot, "org/netbeans/modules/debugger/jpda/truffle/scripts/Types.js");
        String sourcePath = source.getAbsolutePath();
        int debugLine = 51;
        String methodName = "typesTest";
        runScriptUnderJPDA("js", source.getAbsolutePath(), support -> {
            JPDADebugger debugger = support.getDebugger();
            TruffleStackFrame frame = checkStoppedAtScript(debugger.getCurrentThread(), sourcePath, debugLine);
            assertEquals("Bad method name", methodName, frame.getMethodName());
            checkVariableTypes(frame.getScopes());
            support.doContinue();
        });
    }

    private static void checkVariableTypes(TruffleScope[] scopes) {
        assertEquals(1, scopes.length);
        TruffleVariable[] variables = scopes[0].getVariables();
        assertEquals("this", variables[0].getName());
        checkVar(variables[1], "a1", "Array", "[]");
        checkVar(variables[2], "a2", "Array", "(3)[1, 2, [3, 4]]");

        checkVar(variables[3], "b1", "boolean", "true");
        checkVar(variables[4], "b2", "boolean", "false");

        checkVar(variables[5], "c1", "TestClass", "{}");

        checkVar(variables[6], "i1", "number", "0");
        checkVar(variables[7], "i2", "number", "42");
        checkVar(variables[8], "i3", "number", "42.42");
        checkVar(variables[9], "i4", "number", "-0");
        checkVar(variables[10], "i5", "number", "-Infinity");
        checkVar(variables[11], "i6", "number", "Infinity");
        checkVar(variables[12], "i7", "number", "-Infinity");
        checkVar(variables[13], "i8", "number", "NaN");

        checkVar(variables[14], "aSparse", "Array", null/*"(11)[1, 2, empty × 8, 10]"*/);
        assertTrue(variables[14].getValue().toString(), variables[14].getValue().toString().startsWith("(11)[")); // Do not rely on the exact format
        checkVar(variables[15], "s1", "string", "String");
        checkVar(variables[16], "f1", "pow2", null);
        assertTrue(variables[16].getValue().toString(), variables[16].getValue().toString().startsWith("function pow2(x)"));
        checkVar(variables[17], "d1", "Date", null);
        checkVar(variables[18], "undef", "undefined", "undefined");
        checkVar(variables[19], "nul", "null", "null");
        checkVar(variables[20], "sy", "symbol", "Symbol(symbolic)");
        checkVar(variables[21], "o1", "Object", "{}");
        checkVar(variables[22], "o2", "TestFncProp", "{fncProp: \"Property\", a: \"A\"}");
        checkVar(variables[23], "map", "Map", null);
    }

    private static void checkVar(TruffleVariable variable, String name, String type, String value) {
        assertEquals("Name", name, variable.getName());
        assertEquals("Type of " + name, type, variable.getType());
        if (value != null) {
            assertEquals("Value of " + name, value, variable.getValue());
        }
    }

}
