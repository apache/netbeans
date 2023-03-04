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
    private static final String[] SCRIPT_EXTENSIONS = { "js", "py", "r", "ruby" };
    private static final String[] LAUNCHERS = { "js", "graalpython", "Rscript", "ruby" };

    public DebugAllBaseTest(String name) {
        super(name);
    }

    public static Test suite() {
        return createSuite(DebugAllBaseTest.class);
    }

    private void forAllScripts(ThrowableBiConsumer<String, File> scriptConsumer) {
        for (int i = 0; i < LAUNCHERS.length; i++) {
            String launcher = LAUNCHERS[i];
            File source = getScriptSourceFile(SCRIPT_NAME + "." + SCRIPT_EXTENSIONS[i]);
            try {
                scriptConsumer.accept(launcher, source);
            } catch (Throwable t) {
                throw new AssertionError(launcher + " " + source, t);
            }
        }
    }

    public void testBreakpoints() {
        DebuggerManager dm = DebuggerManager.getDebuggerManager();
        forAllScripts((launcher, source) -> {
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
        });
    }

    public void testBreakpointsConditional() {
        DebuggerManager dm = DebuggerManager.getDebuggerManager();
        forAllScripts((launcher, source) -> {
            String and;
            switch (launcher) {
                case "js": and = "&&"; break;
                case "Rscript": and = "&"; break;
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
        });
    }

    public void testSteps() {
        DebuggerManager dm = DebuggerManager.getDebuggerManager();
        forAllScripts((launcher, source) -> {
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
        });
    }

    public void testEval() {
        DebuggerManager dm = DebuggerManager.getDebuggerManager();
        forAllScripts((launcher, source) -> {
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
                    case "ruby": expr = "o.instance_variable_get :@ao"; break;
                    case "Rscript": expr = "o[\"ao\"]"; break;
                    default: expr = "o.ao";
                }
                v = debugger.evaluate(expr);
                tv = TruffleVariable.get(v);
                assertTrue(tv.getDisplayValue(), tv.getDisplayValue().contains("AO"));
                // arr[1] + arr[2]
                v = debugger.evaluate("arr[1] + arr[2]");
                tv = TruffleVariable.get(v);
                String result = launcher.equals("Rscript") ? "9" : "7"; // Arrays start at index 1 in R
                assertTrue(tv.getDisplayValue(), tv.getDisplayValue().contains(result));
                support.doContinue();
            });
        });
    }

    public void testLocalVariables() {
        DebuggerManager dm = DebuggerManager.getDebuggerManager();
        forAllScripts((launcher, source) -> {
            URL url = source.toURI().toURL();
            String sourcePath = source.getAbsolutePath();
            JSLineBreakpoint lb1 = new TruffleLineBreakpoint(url, 23);
            dm.addBreakpoint(lb1);
            runScriptUnderJPDA(launcher, source.getAbsolutePath(), support -> {
                JPDADebugger debugger = support.getDebugger();
                TruffleStackFrame frame = checkStoppedAtScript(debugger.getCurrentThread(), sourcePath, 23);
                if (launcher.equals("Rscript") && frame.getScopes().length == 0) {
                    support.doContinue();
                    return;
                }
                TruffleScope scope = frame.getScopes()[0];
                if (launcher.equals("js")) {
                    assertNull("a is not visible yet", findVariable(scope, "a"));
                    assertNull("o is not visible yet", findVariable(scope, "o"));
                    assertNull("arr is not visible yet", findVariable(scope, "arr"));
                }
                support.stepOver();
                scope = checkStoppedAtScript(debugger.getCurrentThread(), sourcePath, 24).getScopes()[0];
                assertNotNull("a is visible", findVariable(scope, "a"));
                if (launcher.equals("js")) {
                    assertNull("o is not visible yet", findVariable(scope, "o"));
                    assertNull("arr is not visible yet", findVariable(scope, "arr"));
                }
                support.stepOver();
                scope = checkStoppedAtScript(debugger.getCurrentThread(), sourcePath, 25).getScopes()[0];
                assertNotNull("a is visible", findVariable(scope, "a"));
                assertNotNull("o is visible", findVariable(scope, "o"));
                if (launcher.equals("js")) {
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
        });
    }

    public void testObjectProperties() {
        DebuggerManager dm = DebuggerManager.getDebuggerManager();
        forAllScripts((launcher, source) -> {
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
                String aoName = launcher.equals("ruby") ? "@ao" : "ao";
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
        });
    }
}
