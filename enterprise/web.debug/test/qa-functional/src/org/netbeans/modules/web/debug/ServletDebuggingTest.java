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
package org.netbeans.modules.web.debug;

import java.io.File;
import java.io.IOException;
import junit.framework.Test;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.modules.debugger.actions.ApplyCodeChangesAction;
import org.netbeans.jellytools.modules.debugger.actions.ContinueAction;
import org.netbeans.jellytools.modules.debugger.actions.StepIntoAction;
import org.netbeans.jellytools.modules.debugger.actions.StepOutAction;
import org.netbeans.jellytools.modules.debugger.actions.StepOverAction;
import org.netbeans.jellytools.modules.j2ee.J2eeTestCase;
import org.netbeans.jellytools.modules.j2ee.nodes.J2eeServerNode;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;

/** Test of web application debugging. Manual test specification is here:
 * http://wiki.netbeans.org/TS_70_WebEnterpriseDebug
 *
 * @author Jiri Skrivanek
 */
public class ServletDebuggingTest extends J2eeTestCase {

    /** Status bar tracer used to wait for state. */
    private MainWindowOperator.StatusTextTracer stt;
    // name of sample web application project
    private static final String SAMPLE_WEB_PROJECT_NAME = "MainTestApplication";  //NOI18N
    // line number of breakpoint
    private static int line;
    // servlet node in Projects view
    private Node servletNode;
    
    public ServletDebuggingTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return createAllModulesServerSuite(Server.GLASSFISH, ServletDebuggingTest.class,
                "testSetBreakpoint",
                "testDebugProject",
                // testStepOut must be before testStepInto to prevent stopping debugger at JDK sources
                "testStepOut",
                "testStepInto",
                "testStepOver",
                "testApplyCodeChanges",
                "testStopServer");
    }

    /** Print test name and initialize status bar tracer. */
    @Override
    public void setUp() throws IOException {
        System.out.println("########  " + getName() + "  #######");
        stt = MainWindowOperator.getDefault().getStatusTextTracer();
        // start to track Main Window status bar
        stt.start();
        // increase timeout to 60 seconds when waiting for status bar text
        MainWindowOperator.getDefault().getTimeouts().setTimeout("Waiter.WaitingTime", 60000);
        // find servlet node in Projects view
        openProjects(new File(getDataDir(), SAMPLE_WEB_PROJECT_NAME).getAbsolutePath());
        waitScanFinished();
        servletNode = new Node(new SourcePackagesNode(SAMPLE_WEB_PROJECT_NAME),
                "org.netbeans.test.servlets|DivideServlet.java"); //NOI18N
    }

    /** Stops status bar tracer. */
    @Override
    public void tearDown() {
        stt.stop();
    }

    /** Set breakpoint.
     * - open Source Packages|org.netbeans.test.servlets|DivideServlet.java
     * - select <h1> in editor
     * - toggle breakpoint at selected line
     */
    public void testSetBreakpoint() throws Exception {
        Utils.suppressBrowserOnRun(SAMPLE_WEB_PROJECT_NAME);
        waitScanFinished();
        new OpenAction().performAPI(servletNode);
        // find file in Editor
        EditorOperator eo = new EditorOperator("DivideServlet.java"); // NOI18N
        line = Utils.setBreakpoint(eo, "<h1>"); // NOI18N
    }

    /** Debug project.
     * - debug project
     * - wait until debugger is started
     */
    public void testDebugProject() {
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(SAMPLE_WEB_PROJECT_NAME);
        rootNode.performPopupActionNoBlock("Debug");
        Utils.waitFinished(this, SAMPLE_WEB_PROJECT_NAME, "debug");
    }
    
    /** Step into in Servlet.
     * - call "Debug File"  popup on servlet's node
     * - wait until debugger stops at previously set breakpoint
     * - call Debug|Step Into from main menu
     * - call Debug|Step Into from main menu to confirm method selection
     * - wait until debugger stops at next line
     * - call Debug|Step Into from main menu again
     * - wait until debugger stops at line in Divider.java
     * - find and close editor tab with Divider.java
     * - continue
     */
    public void testStepInto() {
        debugServlet();
        new StepIntoAction().perform();
        waitText("DivideServlet.java:" + (line + 2)); //NOI18N
        new StepIntoAction().perform();
        // org.netbeans.test.freeformlib.Divider should be used in servlet when #199615 is fixed
        waitText("Multiplier.java:"); //NOI18N
        new EditorOperator("Multiplier.java").close(); //NOI18N
        new ContinueAction().perform();
    }

    /** Step out from servlet.
     * - call Debug File popup on servlet's node
     * - wait until debugger stops at previously set breakpoint
     * - call Debug|Step Out from main menu
     * - wait until debugger stops in doGet method
     * - continue
     */
    public void testStepOut() {
        debugServlet();
        new StepOutAction().perform();
        // it stops at doGet method
        waitText("DivideServlet.java:"); //NOI18N
        new ContinueAction().perform();
    }

    /** Step over servlet.
     * - call Debug File popup on servlet's node
     * - wait until debugger stops at previously set breakpoint
     * - call Debug|Step Over from main menu
     * - wait until debugger stops at next line
     * - call Debug|Step Over from main menu again
     * - wait until debugger stops at next line
     * - continue
     */
    public void testStepOver() {
        debugServlet();
        new StepOverAction().perform();
        waitText("DivideServlet.java:" + (line + 2)); //NOI18N
        new StepOverAction().perform();
        waitText("DivideServlet.java:" + (line + 4)); //NOI18N
        new ContinueAction().perform();
    }

    /** Apply code changes in servlet.
     * - call Debug File popup on servlet's node
     * - wait until debugger stops at previously set breakpoint
     * - replace "Servlet DIVIDE" by "Servlet DIVIDE Changed" in DivideServlet.java
     * - call Debug|Apply Code Changes from main menu
     * - wait until debugger stops somewhere in DivideServlet.java
     * - finish debugger
     * - open URL connection and wait for changed text
     */
    public void testApplyCodeChanges() {
        debugServlet();
        EditorOperator eo = new EditorOperator("DivideServlet.java"); // NOI18N
        eo.replace("Servlet DIVIDE", "Servlet DIVIDE Changed"); //NOI18N
        new ApplyCodeChangesAction().perform();
        Utils.waitFinished(this, SAMPLE_WEB_PROJECT_NAME, "debug-fix");
        waitText("DivideServlet.java:"); //NOI18N
        Utils.finishDebugger();
        Utils.waitText(SAMPLE_WEB_PROJECT_NAME + "/DivideServlet", 240000, "Servlet DIVIDE Changed");
    }

    /** Stop server just for clean-up.
     * - stop server and wait until it finishes
     */
    public void testStopServer() {
        J2eeServerNode serverNode = new J2eeServerNode(Utils.DEFAULT_SERVER);
        JSPDebuggingOverallTest.verifyServerNode(serverNode);
        serverNode.stop();
    }
    
    /**
     * - call Debug File popup on servlet's node
     * - close Set URI dialog
     * - wait until project execution is finished
     * - reload page because browser doesn't open automatically
     * - wait until debugger stops at previously set breakpoint
     */
    private void debugServlet() {
        new ActionNoBlock(null, "Debug File").perform(servletNode);
        String setURITitle = Bundle.getString("org.netbeans.modules.web.project.ui.Bundle", "TTL_setServletExecutionUri");
        new NbDialogOperator(setURITitle).ok();
        Utils.waitFinished(this, SAMPLE_WEB_PROJECT_NAME, "debug");
        // reload page because browser is suppressed
        Utils.reloadPage(SAMPLE_WEB_PROJECT_NAME + "/DivideServlet");
        waitText("DivideServlet.java:" + line); //NOI18N
    }

    /**
     * Waits for text in status bar or in debugger console. Used to check that
     * breakpoint was reached.
     *
     * @param text text to wait for
     */
    private void waitText(String text) {
        stt.waitText(text);
        stt.clear();
    }
}
