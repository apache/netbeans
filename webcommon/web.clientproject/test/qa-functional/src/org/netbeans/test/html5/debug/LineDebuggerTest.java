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
package org.netbeans.test.html5.debug;

import java.util.Map;
import java.util.logging.Level;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.EditorWindowOperator;
import org.netbeans.jellytools.modules.debugger.actions.ContinueAction;
import org.netbeans.jellytools.modules.debugger.actions.StepIntoAction;
import org.netbeans.jellytools.modules.debugger.actions.StepOverAction;
import org.netbeans.jellytools.modules.debugger.actions.ToggleBreakpointAction;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.html5.Browser;
import org.netbeans.test.html5.GeneralHTMLProject;

/**
 *
 * @author Vladimir Riha
 */
public class LineDebuggerTest extends JavaScriptDebugger {

    public LineDebuggerTest(String args) {
        super(args);
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(LineDebuggerTest.class).addTest(
                "testOpenProject",
                "testBreakpointRemoteFile",
                "testDebugModifications",
                "testDebugNoModifications",
                "testDeleteBreakpoint").enableModules(".*").clusters(".*").honorAutoloadEager(true));
    }

    public void testOpenProjectChromeNB() throws Exception {
        startTest();
        setProxy();
        LineDebuggerTest.current_project = "simpleProject";
        openProject("simpleProject");
        setRunConfiguration("Chrome with NetBeans Integration", true, true);
        setProxy();
        endTest();
    }

    public void testOpenProject() throws Exception {
        startTest();
        setProxy();
        LineDebuggerTest.current_project = "simpleProject";
        openProject("simpleProject");
        setRunConfiguration("Embedded WebKit Browser", true, true);
        setProxy();
        endTest();
    }

    /**
     * Case: Inserts several line breakpoint prior to running file in browser.
     * During debugging, StepInto, StepOver and Continue actions are used and
     * value of variable is checked in variables window. Also checks if file
     * with breakpoint is opened and given line focused.
     */
    public void testDebugNoModifications() throws Exception {
        startTest();
        openFile("debug.html", LineDebuggerTest.current_project);
        EditorOperator eo = new EditorOperator("debug.html");
        setLineBreakpoint(eo, "window.console.log(a);");

        openFile("linebp.js", LineDebuggerTest.current_project);
        eo = new EditorOperator("linebp.js");
        setLineBreakpoint(eo, "console.log(\"start\");");
        setLineBreakpoint(eo, "if (action === \"build\") {");
        setLineBreakpoint(eo, "var d = new Date();");
        eo.close();
        runFile(LineDebuggerTest.current_project, "debug.html");
        evt.waitNoEvent(GeneralHTMLProject.RUN_WAIT_TIMEOUT);
        EditorOperator currentFile = EditorWindowOperator.getEditor();
        VariablesOperator vo = new VariablesOperator("Variables");

        assertEquals("Unexpected file opened at breakpoint", "debug.html", currentFile.getName());
        assertEquals("Debugger stopped at wrong line", 14, currentFile.getLineNumber());
        evt.waitNoEvent(1000);
        waitForVariable("step");
        assertEquals("Step variable is unexpected", "1", ((Map<String, Variable>) vo.getVariables()).get("step").value);

        new ContinueAction().performMenu();
        evt.waitNoEvent(1000);
        currentFile = EditorWindowOperator.getEditor();
        vo = new VariablesOperator("Variables");
        evt.waitNoEvent(1000);
        waitForVariable("step");
        assertEquals("Unexpected file opened at breakpoint", "linebp.js", currentFile.getName());
        assertEquals("Debugger stopped at wrong line", 4, currentFile.getLineNumber());
        assertEquals("Step variable is unexpected", "2", ((Map<String, Variable>) vo.getVariables()).get("step").value);

        new StepOverAction().performMenu();
        new StepOverAction().performMenu();
        new StepOverAction().performMenu();
        evt.waitNoEvent(500);
        currentFile = EditorWindowOperator.getEditor();
        vo = new VariablesOperator("Variables");
        evt.waitNoEvent(1000);
        assertEquals("Debugger stopped at wrong line", 7, currentFile.getLineNumber());
        waitForVariable("step");
        assertEquals("Step variable is unexpected", "3", ((Map<String, Variable>) vo.getVariables()).get("step").value);

        new ContinueAction().performMenu();
        evt.waitNoEvent(1000);
        currentFile = EditorWindowOperator.getEditor();
        vo = new VariablesOperator("Variables");
        assertEquals("Debugger stopped at wrong line", 15, currentFile.getLineNumber());
        evt.waitNoEvent(1000);
        waitForVariable("step");
        assertEquals("Step variable is unexpected", "4", ((Map<String, Variable>) vo.getVariables()).get("step").value);

        new StepOverAction().performMenu();
        new StepOverAction().performMenu();
        evt.waitNoEvent(1000);
        currentFile = EditorWindowOperator.getEditor();
        vo = new VariablesOperator("Variables");
        assertEquals("Debugger stopped at wrong line", 17, currentFile.getLineNumber());
        evt.waitNoEvent(1000);
        waitForVariable("step");
        assertEquals("Step variable is unexpected", "5", ((Map<String, Variable>) vo.getVariables()).get("step").value);

        new StepIntoAction().performMenu();
        new StepIntoAction().performMenu();
        evt.waitNoEvent(1000);
        vo = new VariablesOperator("Variables");
        evt.waitNoEvent(1000);
        waitForVariable("step");
        if(GeneralHTMLProject.inEmbeddedBrowser){ // embedded browser stops on line with function(){
            assertEquals("Step variable is unexpected", "5", ((Map<String, Variable>) vo.getVariables()).get("step").value);
        }else{
            assertEquals("Step variable is unexpected", "6", ((Map<String, Variable>) vo.getVariables()).get("step").value);    
        }

        endTest();
    }

    /**
     * Case: Add some breakpoints to embedded JS and JS file, then it tries
     * several times to add and delete lines preceding the breakpoint
     */
    public void testDebugModifications() throws Exception {
        startTest();

        openFile("debugMod.html", LineDebuggerTest.current_project);
        EditorOperator eo = new EditorOperator("debugMod.html");
        setLineBreakpoint(eo, "window.console.log(a);");
        openFile("linebpMod.js", LineDebuggerTest.current_project);
        eo = new EditorOperator("linebpMod.js");
        setLineBreakpoint(eo, "console.log(\"start\");");
        eo.close();
        runFile(LineDebuggerTest.current_project, "debugMod.html");
        evt.waitNoEvent(GeneralHTMLProject.RUN_WAIT_TIMEOUT);
        new ContinueAction().performMenu();
        evt.waitNoEvent(1000);
        EditorOperator currentFile = EditorWindowOperator.getEditor();
        currentFile.setCaretPositionToEndOfLine(3);
        currentFile.insert("\nconsole.log(\"1js\");\nconsole.log(\"2js\");\nconsole.log(\"3js\");");
//        if (LineDebuggerTest.inEmbeddedBrowser) { // workaround for 226022
//            (new EmbeddedBrowserOperator("Web Browser")).close();
//            saveAndWait(currentFile, 1000);
//            runFile(LineDebuggerTest.current_project, "debugMod.html");
//            evt.waitNoEvent(GeneralHTMLProject.RUN_WAIT_TIMEOUT);
//        } else {
        saveAndWait(currentFile, 1500);

        new ContinueAction().performMenu();
        evt.waitNoEvent(1000);
        currentFile = EditorWindowOperator.getEditor();
        assertEquals("Unexpected file opened at breakpoint", "linebpMod.js", currentFile.getName());
        assertEquals("Debugger stopped at wrong line", 7, currentFile.getLineNumber());
        currentFile.deleteLine(4);
        currentFile.deleteLine(4);

//        if (LineDebuggerTest.inEmbeddedBrowser) {
//            (new EmbeddedBrowserOperator("Web Browser")).close();
//            saveAndWait(currentFile, 1000);
//            runFile(LineDebuggerTest.current_project, "debugMod.html");
//            evt.waitNoEvent(GeneralHTMLProject.RUN_WAIT_TIMEOUT);
//        } else {
        saveAndWait(currentFile, 1500);

        new ContinueAction().performMenu();
        evt.waitNoEvent(1000);
        currentFile = EditorWindowOperator.getEditor();
        assertEquals("Unexpected file opened at breakpoint", "linebpMod.js", currentFile.getName());
        assertEquals("Debugger stopped at wrong line", 5, currentFile.getLineNumber());


        currentFile.setCaretPositionToEndOfLine(3);
        currentFile.insert("\nconsole.log(\"1js\");\nconsole.log(\"2js\");\nconsole.log(\"3js\");");        

//        if (LineDebuggerTest.inEmbeddedBrowser) {
//            (new EmbeddedBrowserOperator("Web Browser")).close();
//            saveAndWait(currentFile, 1000);
//            runFile(LineDebuggerTest.current_project, "debugMod.html");
//            evt.waitNoEvent(GeneralHTMLProject.RUN_WAIT_TIMEOUT);
//        } else {
        saveAndWait(currentFile, 1500);

        new ContinueAction().performMenu();
        evt.waitNoEvent(1000);
        currentFile = EditorWindowOperator.getEditor();
        assertEquals("Unexpected file opened at breakpoint", "linebpMod.js", currentFile.getName());
        assertEquals("Debugger stopped at wrong line", 8, currentFile.getLineNumber());
        currentFile.close();

        endTest();
    }

    @Override
    public void setUp() {
        try {
            cleanBreakpoints();
        } catch (TimeoutExpiredException e) {
            LOGGER.log(Level.INFO, "Variables window was not opened");
        }
    }

    @Override
    public void tearDown() {
        try {
            (new Browser()).closeBrowser("Web Browser");
        } catch (Exception e) {
            LOGGER.log(Level.INFO, "Unable to close browser " + e.getMessage());
        }
    }

    /**
     * Case: Remove line breakpoint and make sure it is not hit on next run
     */
    public void testDeleteBreakpoint() throws Exception {
        startTest();
        openFile("debug.html", LineDebuggerTest.current_project);
        EditorOperator eo = new EditorOperator("debug.html");
        setLineBreakpoint(eo, "window.console.log(a);");

        openFile("linebp.js", LineDebuggerTest.current_project);
        eo = new EditorOperator("linebp.js");
        setLineBreakpoint(eo, "console.log(\"start\");");
        eo.close();
        runFile(LineDebuggerTest.current_project, "debug.html");
        evt.waitNoEvent(GeneralHTMLProject.RUN_WAIT_TIMEOUT);
        new ContinueAction().performMenu();
        evt.waitNoEvent(1000);
        EditorOperator currentFile = EditorWindowOperator.getEditor();
//        if (LineDebuggerTest.inEmbeddedBrowser) { // workaround for 226022
//            (new EmbeddedBrowserOperator("Web Browser")).close();
//        }
        currentFile.select("console.log(\"start\");"); // NOI18N
        new ToggleBreakpointAction().perform(currentFile.txtEditorPane());

        currentFile.close();
        runFile(LineDebuggerTest.current_project, "debug.html");
        evt.waitNoEvent(GeneralHTMLProject.RUN_WAIT_TIMEOUT);
        new ContinueAction().performMenu();
        evt.waitNoEvent(1000);
        currentFile = EditorWindowOperator.getEditor();
        assertEquals("Unexpected file opened at breakpoint", "debug.html", currentFile.getName());
        cleanBreakpoints();
        endTest();
    }

    /**
     * Case: Adds line breakpoint to remote file and checks that breakpoint is
     * hit and file opened with focused line
     */
    public void testBreakpointRemoteFile() throws Exception {
        startTest();
        dummyEdit("debug.html", LineDebuggerTest.current_project, "debug.html"); // workaround for automated tests on slow machines
        waitForRemoteFiles(LineDebuggerTest.current_project);
        openRemoteFile("common.js", LineDebuggerTest.current_project);
        openFile("debug.html", LineDebuggerTest.current_project);
        runFile(LineDebuggerTest.current_project, "debug.html");
        evt.waitNoEvent(GeneralHTMLProject.RUN_WAIT_TIMEOUT);
        EditorOperator eo = new EditorOperator("common.js");
        setLineBreakpoint(eo, "document.write(msg);");
        eo.close();
        runFile(LineDebuggerTest.current_project, "debug.html"); // issue 225407 workaround
        evt.waitNoEvent(GeneralHTMLProject.RUN_WAIT_TIMEOUT);
        EditorOperator currentFile = EditorWindowOperator.getEditor();
        assertEquals("Unexpected file opened at breakpoint", "common.js [r/o]", currentFile.getName());
        assertEquals("Debugger stopped at wrong line", 424, currentFile.getLineNumber());
        endTest();
    }
}
