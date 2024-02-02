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
import junit.framework.Test;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;

import org.netbeans.api.debugger.ActionsManager;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.LineBreakpoint;
import org.netbeans.modules.debugger.jpda.truffle.frames.TruffleStackFrame;
import org.netbeans.modules.debugger.jpda.truffle.vars.TruffleVariable;

public class PolyglotTest extends JPDATestCase {

    private static final String ACTION_PAUSE_IN_GRAAL = "pauseInGraalScript";

    public PolyglotTest(String name) {
        super(name);
    }

    public static Test suite() {
        return createSuite(PolyglotTest.class);
    }

    @Override // The app loads scripts from the compiled classes location always
    protected String getBinariesPath(String sourcesPath) {
        String classesDir = System.getProperty("test.dir.classes");
        if (classesDir == null) {
            return super.getBinariesPath(sourcesPath);
        }
        String base = sourceRoot.getAbsolutePath();
        if (!sourcesPath.startsWith(base)) {
            return sourcesPath;
        }
        String relPath = sourcesPath.substring(base.length());
        while (relPath.startsWith(File.separator)) {
            relPath = relPath.substring(File.separator.length());
        }
        return new File(classesDir, relPath).getAbsolutePath();
    }
    
    public void testDisabled() {}

    // see spot marked with XXX
    public void disabled_testWeatherApp() throws Exception {
        DebuggerManager dm = DebuggerManager.getDebuggerManager();
        String sourcePathJS = getScriptSourceFile("Weather.js").getAbsolutePath();
        String sourcePathPython = getScriptSourceFile("Weather.py").getAbsolutePath();
        String sourcePathR = "org/netbeans/modules/debugger/jpda/truffle/scripts/Weather.r"; // relative path in R
        LineBreakpoint lb = LineBreakpoint.create(getJavaSourceFile("PolyglotWeatherApp.java").toURI().toURL().toString(), 30);
        dm.addBreakpoint(lb);
        runJavaUnderJPDA("org.netbeans.modules.debugger.jpda.truffle.testapps.PolyglotWeatherApp", support -> {
            final JPDADebugger debugger = support.getDebugger();
            CallStackFrame frame = debugger.getCurrentCallStackFrame();
            assertEquals(30, frame.getLineNumber(null));
            dm.removeBreakpoint(lb);
            ActionsManager actions = DebuggerManager.getDebuggerManager().getCurrentEngine().getActionsManager();
            assertFalse("Pause in Graal Script not enabled initially", actions.isEnabled(ACTION_PAUSE_IN_GRAAL));
            support.stepOver();
            assertTrue("Pause in Graal Script enabled after Engine creation", actions.isEnabled(ACTION_PAUSE_IN_GRAAL));
            support.stepOver();
            frame = debugger.getCurrentCallStackFrame();
            assertEquals(32, frame.getLineNumber(null));
            support.stepOver();
            frame = debugger.getCurrentCallStackFrame();
            assertEquals(33, frame.getLineNumber(null));
            actions.doAction(ACTION_PAUSE_IN_GRAAL);
            support.doContinue();
            support.waitState(JPDADebugger.STATE_STOPPED);

            // We should be suspended in the JavaScript
            TruffleStackFrame tframe = checkStoppedAtScript(debugger.getCurrentThread(), sourcePathJS, 124);
            assertEquals("JavaScript", tframe.getLanguage().getName());
            support.stepInto();
            checkStoppedAtScript(debugger.getCurrentThread(), sourcePathJS, 23);
            support.stepOver(); // loading Ruby // XXX locks core to 100% in app JVM till test timeout, GraalVM 11 v22.3.1
            support.stepOver();
            tframe = checkStoppedAtScript(debugger.getCurrentThread(), sourcePathJS, 26);
            TruffleVariable rubyWeather = findVariable(tframe.getScopes()[0], "Weather");
            assertNotNull("Weather variable", rubyWeather);
            assertEquals("Ruby", rubyWeather.getLanguage().getName());
            assertEquals("Weather", rubyWeather.getValue());
            support.stepOver();
            support.stepOver();
            //support.stepOver();
            checkStoppedAtScript(debugger.getCurrentThread(), sourcePathJS, 32);
            support.stepOver(); // loading R
            checkStoppedAtScript(debugger.getCurrentThread(), sourcePathJS, 35);
            support.stepOver();
            support.stepOver();
            support.stepOver();
            tframe = checkStoppedAtScript(debugger.getCurrentThread(), sourcePathJS, 40);
            TruffleVariable createModel = findVariable(tframe.getScopes()[0], "createModel");
            assertEquals("R", createModel.getLanguage().getName());
            assertEquals("closure", createModel.getType());
            support.stepOver(); // loading Python
            support.stepOver();
            tframe = checkStoppedAtScript(debugger.getCurrentThread(), sourcePathJS, 58);
            TruffleVariable purchase = findVariable(tframe.getScopes()[0], "Purchase");
            assertEquals("Python", purchase.getLanguage().getName());
            assertEquals("function", purchase.getType());
            support.stepOver();
            support.stepOver();
            tframe = checkStoppedAtScript(debugger.getCurrentThread(), sourcePathJS, 61);
            TruffleVariable cities = findVariable(tframe.getScopes()[0], "cities");
            assertEquals("Host", cities.getLanguage().getName());
            support.stepOver();
            support.stepOver();
            support.stepInto();
            tframe = checkStoppedAtScript(debugger.getCurrentThread(), sourcePathJS, 79);
            assertEquals("updateModel", tframe.getMethodName());
            support.stepOver();
            support.stepOver();
            support.stepOver();
            support.stepOver();

            // Calling into R:
            support.stepInto();
            tframe = checkStoppedAtScript(debugger.getCurrentThread(), sourcePathR, 27);
            assertEquals("R", tframe.getLanguage().getName());
            assertEquals("createModel", tframe.getMethodName());
            TruffleVariable getName = findVariable(tframe.getScopes()[0], "getName");
            assertEquals("JavaScript", getName.getLanguage().getName());
            support.stepOver();
            support.stepOver();
            checkStoppedAtScript(debugger.getCurrentThread(), sourcePathR, 33);
            support.stepOver();

            // Back in JavaScript:
            tframe = checkStoppedAtScript(debugger.getCurrentThread(), sourcePathJS, 92);
            assertEquals("JavaScript", tframe.getLanguage().getName());
            support.stepOver();
            support.stepOver();
            tframe = checkStoppedAtScript(debugger.getCurrentThread(), sourcePathJS, 97);
            TruffleVariable model = findVariable(tframe.getScopes()[0], "model");
            assertEquals("R", model.getLanguage().getName());
            support.stepOver();
            tframe = checkStoppedAtScript(debugger.getCurrentThread(), sourcePathJS, 98);
            TruffleVariable numCities = findVariable(tframe.getScopes()[0], "numCities");
            assertEquals("number", numCities.getType());
            assertEquals("5", numCities.getValue());
            support.stepOver();
            support.stepOver();
            support.stepOver();
            support.stepOver();

            // Calling into Ruby:
            support.stepInto();
            tframe = checkStoppedAtScript(debugger.getCurrentThread(), null, 22); // In an eval script
            assertEquals("Ruby", tframe.getLanguage().getName());
            support.stepOut();

            // Back in JavaScript
            tframe = checkStoppedAtScript(debugger.getCurrentThread(), sourcePathJS, 103);
            support.stepOver();
            tframe = checkStoppedAtScript(debugger.getCurrentThread(), sourcePathJS, 106);

            // Calling into Python:
            support.stepInto();
            tframe = checkStoppedAtScript(debugger.getCurrentThread(), null, 4); // In an eval script
            assertEquals("Python", tframe.getLanguage().getName());
            support.stepOver();
            support.stepInto();
            tframe = checkStoppedAtScript(debugger.getCurrentThread(), null, 9); // In an eval script
            assertEquals("fruits", tframe.getMethodName());
            support.stepOut();
            support.stepOut();

            // Back in JavaScript
            tframe = checkStoppedAtScript(debugger.getCurrentThread(), sourcePathJS, 106);
            assertEquals("JavaScript", tframe.getLanguage().getName());

            support.doContinue();
        });
    }
}
