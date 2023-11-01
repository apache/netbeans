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
import org.netbeans.api.debugger.jpda.Variable;
import org.netbeans.modules.debugger.jpda.truffle.breakpoints.TruffleLineBreakpoint;
import org.netbeans.modules.debugger.jpda.truffle.frames.TruffleStackFrame;
import org.netbeans.modules.debugger.jpda.truffle.vars.TruffleVariable;
import org.netbeans.modules.debugger.jpda.truffle.vars.impl.TruffleScope;
import org.netbeans.modules.javascript2.debug.breakpoints.JSLineBreakpoint;

public class DebugAllBaseTest extends JPDATestCase {

    private static final String SCRIPT_NAME = "DebuggerBase";

    private static final String JS_LAUNCHER = "js";
    private static final String PYTHON_LAUNCHER = "python";
    private static final String RSCRIPT_LAUNCHER = "Rscript";
    private static final String RUBY_LAUNCHER = "ruby";

    public DebugAllBaseTest(String name) {
        super(name);
    }

    public static Test suite() {
        return createSuite(DebugAllBaseTest.class);
    }

    private File getScriptSourceFileForLauncher(String launcher) {
        String ending;
        switch (launcher) {
            case "graalpython": // old name for 8
            case PYTHON_LAUNCHER: ending = "py"; break;
            case RSCRIPT_LAUNCHER: ending = "r"; break;
            default: ending = launcher;
        }
        return getScriptSourceFile(SCRIPT_NAME + "." + ending);
    }

    public void testBreakpoints_JS() throws Exception {
        runBreakpointsTest(JS_LAUNCHER);
    }

    public void testBreakpoints_Python() throws Exception {
        runBreakpointsTest(PYTHON_LAUNCHER);
    }

    public void testBreakpoints_R() throws Exception {
        runBreakpointsTest(RSCRIPT_LAUNCHER);
    }

    public void testBreakpoints_Ruby() throws Exception {
        runBreakpointsTest(RUBY_LAUNCHER);
    }

    private void runBreakpointsTest(String launcher) throws Exception {
        File source = getScriptSourceFileForLauncher(launcher);
        DebuggerManager dm = DebuggerManager.getDebuggerManager();
        URL url = source.toURI().toURL();
        JSLineBreakpoint lb1 = new TruffleLineBreakpoint(url, 25);
        dm.addBreakpoint(lb1);
        JSLineBreakpoint lb2 = new TruffleLineBreakpoint(url, 29);
        dm.addBreakpoint(lb2);
        String sourcePath = source.getAbsolutePath();
        runScriptUnderJPDA(launcher, source.getAbsolutePath(), support -> {
            JPDADebugger debugger = support.getDebugger();
            checkStoppedAtScript(debugger.getCurrentThread(), sourcePath, 25);
            support.doContinue();
            support.waitState(JPDADebugger.STATE_STOPPED);
            checkStoppedAtScript(debugger.getCurrentThread(), sourcePath, 29);
            dm.removeBreakpoint(lb2);
            support.doContinue();
        });
    }

    public void testBreakpointsConditional_JS() throws Exception {
        runBreakpointsConditionalTest(JS_LAUNCHER);
    }

    public void testBreakpointsConditional_Python() throws Exception {
        runBreakpointsConditionalTest(PYTHON_LAUNCHER);
    }

    public void testBreakpointsConditional_R() throws Exception {
        runBreakpointsConditionalTest(RSCRIPT_LAUNCHER);
    }

    public void testBreakpointsConditional_Ruby() throws Exception {
        runBreakpointsConditionalTest(RUBY_LAUNCHER);
    }

    public void runBreakpointsConditionalTest(String launcher) throws Exception {
        File source = getScriptSourceFileForLauncher(launcher);
        DebuggerManager dm = DebuggerManager.getDebuggerManager();
        String and;
        switch (launcher) {
            case JS_LAUNCHER: and = "&&"; break;
            case RSCRIPT_LAUNCHER: and = "&"; break;
            default: and = "and";
        }
        String condition = "n == 2 " + and + " n1 == 3";
        URL url = source.toURI().toURL();
        JSLineBreakpoint lb = new TruffleLineBreakpoint(url, 35);
        lb.setCondition(condition);
        dm.addBreakpoint(lb);
        String sourcePath = source.getAbsolutePath();
        runScriptUnderJPDA(launcher, source.getAbsolutePath(), support -> {
            JPDADebugger debugger = support.getDebugger();
            // The conditional breakpoint is hit two times:
            checkStoppedAtScript(debugger.getCurrentThread(), sourcePath, 35);
            support.doContinue();
            support.waitState(JPDADebugger.STATE_STOPPED);
            checkStoppedAtScript(debugger.getCurrentThread(), sourcePath, 35);
            support.doContinue();
        });
    }

    public void testSteps_JS() throws Exception {
        runStepsTest(JS_LAUNCHER);
    }

    public void testSteps_Python() throws Exception {
        runStepsTest(PYTHON_LAUNCHER);
    }

    public void testSteps_R() throws Exception {
        runStepsTest(RSCRIPT_LAUNCHER);
    }

    public void testSteps_Ruby() throws Exception {
        runStepsTest(RUBY_LAUNCHER);
    }

    public void runStepsTest(String launcher) throws Exception {
        File source = getScriptSourceFileForLauncher(launcher);
        DebuggerManager dm = DebuggerManager.getDebuggerManager();
        URL url = source.toURI().toURL();
        String sourcePath = source.getAbsolutePath();
        JSLineBreakpoint lb1 = new TruffleLineBreakpoint(url, 42);
        dm.addBreakpoint(lb1);
        runScriptUnderJPDA(launcher, source.getAbsolutePath(), support -> {
            JPDADebugger debugger = support.getDebugger();
            checkStoppedAtScript(debugger.getCurrentThread(), sourcePath, 42);
            support.stepOver();
            checkStoppedAtScript(debugger.getCurrentThread(), sourcePath, 43);
            support.stepInto();
            checkStoppedAtScript(debugger.getCurrentThread(), sourcePath, 23);
            support.stepOut();
            checkStoppedAtScript(debugger.getCurrentThread(), sourcePath, 43);
            support.doContinue();
        });
    }

    public void testEval_JS() throws Exception {
        runEvalTest(JS_LAUNCHER);
    }

    public void testEval_Python() throws Exception {
        runEvalTest(PYTHON_LAUNCHER);
    }

    public void testEval_R() throws Exception {
        runEvalTest(RSCRIPT_LAUNCHER);
    }

    public void testEval_Ruby() throws Exception {
        runEvalTest(RUBY_LAUNCHER);
    }

    public void runEvalTest(String launcher) throws Exception {
        File source = getScriptSourceFileForLauncher(launcher);
        DebuggerManager dm = DebuggerManager.getDebuggerManager();
        URL url = source.toURI().toURL();
        String sourcePath = source.getAbsolutePath();
        JSLineBreakpoint lb1 = new TruffleLineBreakpoint(url, 29);
        dm.addBreakpoint(lb1);
        runScriptUnderJPDA(launcher, source.getAbsolutePath(), support -> {
            JPDADebugger debugger = support.getDebugger();
            checkStoppedAtScript(debugger.getCurrentThread(), sourcePath, 29);
            // a = 20
            Variable v = debugger.evaluate("a");
            TruffleVariable tv = TruffleVariable.get(v);
            assertTrue(tv.getDisplayValue(), tv.getDisplayValue().contains("20"));
            // o.ao = "AO"
            String expr;
            switch (launcher) {
                case RUBY_LAUNCHER: expr = "o.instance_variable_get :@ao"; break;
                case RSCRIPT_LAUNCHER: expr = "o[\"ao\"]"; break;
                default: expr = "o.ao";
            }
            v = debugger.evaluate(expr);
            tv = TruffleVariable.get(v);
            assertTrue(tv.getDisplayValue(), tv.getDisplayValue().contains("AO"));
            // arr[1] + arr[2]
            v = debugger.evaluate("arr[1] + arr[2]");
            tv = TruffleVariable.get(v);
            String result = launcher.equals(RSCRIPT_LAUNCHER) ? "9" : "7"; // Arrays start at index 1 in R
            assertTrue(tv.getDisplayValue(), tv.getDisplayValue().contains(result));
            support.doContinue();
        });
    }

    public void testLocalVariables_JS() throws Exception {
        runLocalVariablesTest(JS_LAUNCHER);
    }

    public void testLocalVariables_Python() throws Exception {
        runLocalVariablesTest(PYTHON_LAUNCHER);
    }

    public void testLocalVariables_R() throws Exception {
        runLocalVariablesTest(RSCRIPT_LAUNCHER);
    }

    public void testLocalVariables_Ruby() throws Exception {
        runLocalVariablesTest(RUBY_LAUNCHER);
    }

    public void runLocalVariablesTest(String launcher) throws Exception {
        File source = getScriptSourceFileForLauncher(launcher);
        DebuggerManager dm = DebuggerManager.getDebuggerManager();
        URL url = source.toURI().toURL();
        String sourcePath = source.getAbsolutePath();
        JSLineBreakpoint lb1 = new TruffleLineBreakpoint(url, 23);
        dm.addBreakpoint(lb1);
        runScriptUnderJPDA(launcher, source.getAbsolutePath(), support -> {
            JPDADebugger debugger = support.getDebugger();
            TruffleStackFrame frame = checkStoppedAtScript(debugger.getCurrentThread(), sourcePath, 23);
            if (launcher.equals(RSCRIPT_LAUNCHER) && frame.getScopes().length == 0) {
                support.doContinue();
                return;
            }
            TruffleScope scope = frame.getScopes()[0];
            if (launcher.equals(JS_LAUNCHER)) {
                assertNull("a is not visible yet", findVariable(scope, "a"));
                assertNull("o is not visible yet", findVariable(scope, "o"));
                assertNull("arr is not visible yet", findVariable(scope, "arr"));
            }
            support.stepOver();
            scope = checkStoppedAtScript(debugger.getCurrentThread(), sourcePath, 24).getScopes()[0];
            assertNotNull("a is visible", findVariable(scope, "a"));
            if (launcher.equals(JS_LAUNCHER)) {
                assertNull("o is not visible yet", findVariable(scope, "o"));
                assertNull("arr is not visible yet", findVariable(scope, "arr"));
            }
            support.stepOver();
            scope = checkStoppedAtScript(debugger.getCurrentThread(), sourcePath, 25).getScopes()[0];
            assertNotNull("a is visible", findVariable(scope, "a"));
            assertNotNull("o is visible", findVariable(scope, "o"));
            if (launcher.equals(JS_LAUNCHER)) {
                assertNull("arr is not visible yet", findVariable(scope, "arr"));
            }
            support.stepOver();
            support.stepOver();
            scope = checkStoppedAtScript(debugger.getCurrentThread(), sourcePath, 27).getScopes()[0];
            assertNotNull("a is visible", findVariable(scope, "a"));
            assertNotNull("o is visible", findVariable(scope, "o"));
            assertNotNull("arr is visible", findVariable(scope, "arr"));
            support.doContinue();
        });
    }

    public void testObjectProperties_JS() throws Exception {
        runObjectPropertiesTest(JS_LAUNCHER);
    }

    public void testObjectProperties_Python() throws Exception {
        runObjectPropertiesTest(PYTHON_LAUNCHER);
    }

    public void testObjectProperties_R() throws Exception {
        runObjectPropertiesTest(RSCRIPT_LAUNCHER);
    }

    public void testObjectProperties_Ruby() throws Exception {
        runObjectPropertiesTest(RUBY_LAUNCHER);
    }

    public void runObjectPropertiesTest(String launcher) throws Exception {
        File source = getScriptSourceFileForLauncher(launcher);
        DebuggerManager dm = DebuggerManager.getDebuggerManager();
        URL url = source.toURI().toURL();
        String sourcePath = source.getAbsolutePath();
        JSLineBreakpoint lb1 = new TruffleLineBreakpoint(url, 29);
        dm.addBreakpoint(lb1);
        runScriptUnderJPDA(launcher, source.getAbsolutePath(), support -> {
            JPDADebugger debugger = support.getDebugger();
            TruffleStackFrame frame = checkStoppedAtScript(debugger.getCurrentThread(), sourcePath, 29);
            TruffleVariable o = findVariable(frame.getScopes()[0], "o");
            assertNotNull("Variable o not found!", o);
            Object[] children = o.getChildren();
            String aoName = launcher.equals(RUBY_LAUNCHER) ? "@ao" : "ao";
            boolean hasAO = false;
            for (Object ch : children) {
                if (ch instanceof TruffleVariable && ((TruffleVariable) ch).getName().equals(aoName)) {
                    hasAO = true;
                    break;
                }
            }
            StringBuilder chstr = new StringBuilder("(" + children.length + ")");
            if (!hasAO) {
                for (Object ch : children) {
                    chstr.append(ch.getClass().getName());
                    chstr.append('{');
                    if (ch instanceof TruffleVariable) {
                        chstr.append(((TruffleVariable) ch).getName());
                        chstr.append(": ");
                        chstr.append(((TruffleVariable) ch).getValue());
                    }
                    chstr.append('}');
                }
            }
            assertTrue("AO child was not found, children = " + chstr, hasAO);
            support.doContinue();
        });
    }
}
