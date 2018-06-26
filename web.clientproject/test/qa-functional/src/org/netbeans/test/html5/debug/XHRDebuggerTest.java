/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.test.html5.debug;

import java.util.logging.Level;
import static junit.framework.Assert.assertEquals;
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
