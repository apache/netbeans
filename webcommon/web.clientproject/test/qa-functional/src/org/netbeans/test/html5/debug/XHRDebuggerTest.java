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

import java.util.logging.Level;
import junit.framework.Test;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.EditorWindowOperator;
import org.netbeans.jellytools.modules.debugger.actions.ContinueAction;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.test.html5.Browser;
import org.netbeans.test.html5.GeneralHTMLProject;
import static org.netbeans.test.html5.GeneralHTMLProject.LOGGER;

/**
 *
 * @author Vladimir Riha
 */
public class XHRDebuggerTest extends JavaScriptDebugger {

    public XHRDebuggerTest(String args) {
        super(args);
    }

    public static Test suite() {
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(XHRDebuggerTest.class).addTest(
                "testOpenProject",
                "testCreateBreakpointMixed",
                "testAllXHR",
                "testDisableAll",
                "testDeleteXHRBreakpoint").enableModules(".*").clusters(".*").honorAutoloadEager(true));
    }

    @Override
    public void setUp() {
        try {
            cleanBreakpoints();
        } catch (TimeoutExpiredException e) {
            LOGGER.log(Level.INFO, "Breakpoints window was not opened");
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
     * Case: Add new XHR breakpoint with partial URL and check that code
     * execution stops there
     */
    public void testCreateBreakpointHTML() throws Exception {
        startTest();
        setXHRBreakpoint("lines");
        openFile("xhr.html", LineDebuggerTest.current_project);
        runFile(LineDebuggerTest.current_project, "xhr.html");
        evt.waitNoEvent(GeneralHTMLProject.RUN_WAIT_TIMEOUT);
        EditorOperator currentFile = EditorWindowOperator.getEditor();
        assertEquals("Debugger stopped at wrong line", 19, currentFile.getLineNumber());
        endTest();
    }

    /**
     * Case: Add new XHR breakpoint with partial URL and check that code
     * execution stops there
     */
    public void testCreateBreakpointMixed() throws Exception {
        startTest();
        setXHRBreakpoint("lines");
        setXHRBreakpoint("dummy");
        openFile("xhr.html", LineDebuggerTest.current_project);
        runFile(LineDebuggerTest.current_project, "xhr.html");
        evt.waitNoEvent(GeneralHTMLProject.RUN_WAIT_TIMEOUT);
        EditorOperator currentFile = EditorWindowOperator.getEditor();
        assertEquals("Debugger stopped at wrong line", 19, currentFile.getLineNumber());
        new ContinueAction().performMenu();
        evt.waitNoEvent(1000);
        currentFile = EditorWindowOperator.getEditor();
        assertEquals("Debugger stopped at wrong line", 34, currentFile.getLineNumber());
        endTest();
    }

    /**
     * Case: Add new XHR breakpoint with empty filter
     */
    public void testAllXHR() throws Exception {
        startTest();
        setXHRBreakpoint("");
        openFile("xhr.html", LineDebuggerTest.current_project);
        runFile(LineDebuggerTest.current_project, "xhr.html");
        evt.waitNoEvent(GeneralHTMLProject.RUN_WAIT_TIMEOUT);
        EditorOperator currentFile = EditorWindowOperator.getEditor();
        assertEquals("Debugger stopped at wrong line", 19, currentFile.getLineNumber());
        new ContinueAction().performMenu();
        evt.waitNoEvent(1000);
        currentFile = EditorWindowOperator.getEditor();
        assertEquals("Debugger stopped at wrong line", 34, currentFile.getLineNumber());
        endTest();
    }

    /**
     * Case: Disables all breakpoints while waiting on breakpoint
     */
    public void testDisableAll() throws Exception {
        startTest();
        setXHRBreakpoint("");
        openFile("xhr.html", LineDebuggerTest.current_project);
        runFile(LineDebuggerTest.current_project, "xhr.html");
        evt.waitNoEvent(GeneralHTMLProject.RUN_WAIT_TIMEOUT);
        EditorOperator currentFile = EditorWindowOperator.getEditor();
        assertEquals("Debugger stopped at wrong line", 19, currentFile.getLineNumber());
        disableAllBreakpoints();
        new ContinueAction().performMenu();

        endTest();
    }

    /**
     * Case: Creates XHR breakpoint with some URL, runs file, changes URL filter
     * and checks that change is reflected
     */
    public void testDeleteXHRBreakpoint() throws Exception {
        startTest();
        setXHRBreakpoint("");
        openFile("xhr.html", LineDebuggerTest.current_project);
        runFile(LineDebuggerTest.current_project, "xhr.html");
        evt.waitNoEvent(GeneralHTMLProject.RUN_WAIT_TIMEOUT);
        EditorOperator currentFile = EditorWindowOperator.getEditor();
        assertEquals("Debugger stopped at wrong line", 19, currentFile.getLineNumber());
        currentFile.setCaretPositionToLine(10);
        cleanBreakpoints();
        runFile(LineDebuggerTest.current_project, "xhr.html");
        evt.waitNoEvent(GeneralHTMLProject.RUN_WAIT_TIMEOUT);
        currentFile = EditorWindowOperator.getEditor();
        assertEquals("Code execution stopped on breakpoint but it shouldn't", 10, currentFile.getLineNumber());
        endTest();
    }
}
