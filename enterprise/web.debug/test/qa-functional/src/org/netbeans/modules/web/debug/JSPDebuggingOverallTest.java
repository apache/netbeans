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
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.DebugProjectAction;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.modules.debugger.AttachDialogOperator;
import org.netbeans.jellytools.modules.debugger.SessionsOperator;
import org.netbeans.jellytools.modules.debugger.actions.ContinueAction;
import org.netbeans.jellytools.modules.debugger.actions.DebugJavaFileAction;
import org.netbeans.jellytools.modules.j2ee.J2eeTestCase;
import org.netbeans.jellytools.modules.j2ee.actions.RefreshAction;
import org.netbeans.jellytools.modules.j2ee.actions.RestartAction;
import org.netbeans.jellytools.modules.j2ee.actions.StartAction;
import org.netbeans.jellytools.modules.j2ee.actions.StartDebugAction;
import org.netbeans.jellytools.modules.j2ee.actions.StopAction;
import org.netbeans.jellytools.modules.j2ee.nodes.J2eeServerNode;
import org.netbeans.jellytools.modules.web.nodes.WebPagesNode;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.operators.ContainerOperator;

/**
 * Test of web application debugging. Manual test specification is here:
 * http://wiki.netbeans.org/TS_70_WebEnterpriseDebug
 *
 * @author Jiri Skrivanek
 */
public class JSPDebuggingOverallTest extends J2eeTestCase {

    /**
     * status bar tracer used to wait for state
     */
    private MainWindowOperator.StatusTextTracer stt;

    public JSPDebuggingOverallTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return createAllModulesServerSuite(Server.GLASSFISH, JSPDebuggingOverallTest.class,
                "testOpenProjects",
                // "testSetTomcatPort", // use this for tomcat
                "testRunProject",
                "testSetBreakpoint",
                "testDebugProject",
                "testDebugReload",
                "testAttachDebugger",
                "testDebugAfterBreakpoint",
                "testDebugAndStopServer",
                "testStartAnotherSession",
                "testJavaSession",
                "testStopServer");
    }

    /** Print test name and initialize status bar tracer. */
    @Override
    public void setUp() {
        System.out.println("########  " + getName() + "  #######");
        stt = MainWindowOperator.getDefault().getStatusTextTracer();
        // start to track Main Window status bar
        stt.start();
        // increase timeout to 60 seconds when waiting for status bar text
        MainWindowOperator.getDefault().getTimeouts().setTimeout("Waiter.WaitingTime", 60000);
    }

    /** Stops status bar tracer. */
    @Override
    public void tearDown() {
        stt.stop();
    }
    // name of sample web application project
    private static final String SAMPLE_WEB_PROJECT_NAME = "MainTestApplication";  //NOI18N

    /** Opens test projects. */
    public void testOpenProjects() throws IOException {
        String[] projects = {"MainTestApplication", "TestFreeformLibrary", "TestLibrary", "TestTagLibrary"}; //NOI18N
        for (int i = 0; i < projects.length; i++) {
            openProjects(new File(getDataDir(), projects[i]).getAbsolutePath());
            waitScanFinished();
        }
        new Action("Run|Set Main Project|" + SAMPLE_WEB_PROJECT_NAME, null).perform();
        Utils.suppressBrowserOnRun(SAMPLE_WEB_PROJECT_NAME);
        waitScanFinished();
    }

    /** Set a random port for Tomcat server and socket debugger transport. */
    public void testSetTomcatPort() throws Exception {
        Utils.setTomcatProperties();
    }

    /** Run project. */
    public void testRunProject() {
        new Action(null, "Run").perform(new ProjectsTabOperator().getProjectRootNode(SAMPLE_WEB_PROJECT_NAME));
        Utils.waitFinished(this, SAMPLE_WEB_PROJECT_NAME, "run");
    }

    /** Set breakpoint.
     * - open index.jsp
     * - select <h1> in editor
     * - toggle breakpoint at selected line
     */
    public void testSetBreakpoint() throws Exception {
        new OpenAction().performAPI(new Node(new WebPagesNode(SAMPLE_WEB_PROJECT_NAME), "index.jsp")); // NOI18N
        // find sample file in Editor
        EditorOperator eo = new EditorOperator("index.jsp"); // NOI18N
        Utils.setBreakpoint(eo, "<h1>"); // NOI18N
    }

    /** Debug project.
     * - on project node call Debug Project popup
     * - wait until debug task is finished
     */
    public void testDebugProject() {
        Node rootNode = new ProjectsTabOperator().getProjectRootNode(SAMPLE_WEB_PROJECT_NAME);
        rootNode.performPopupActionNoBlock("Debug");
        Utils.waitFinished(this, SAMPLE_WEB_PROJECT_NAME, "debug");
        //MainWindowOperator.getDefault().waitStatusText("Finished building "+SAMPLE_WEB_PROJECT_NAME+" (debug)");
    }

    /** Reload browser while debugging.
     * - reload page
     * - wait until debugger stops at previously set breakpoint
     * - continue debugging
     * - finish debugger
     */
    public void testDebugReload() {
        Utils.reloadPage(SAMPLE_WEB_PROJECT_NAME);
        // check breakpoint reached
        // wait status text "Thread main stopped at SampleClass1.java:##"
        EditorOperator eo = new EditorOperator("index.jsp"); // NOI18N
        int line = eo.getLineNumber();
        stt.waitText("index.jsp:" + line);
        new ContinueAction().perform();
        Utils.finishDebugger();
    }

    /** Attach debugger.
     * - call Debug|Attach Debugger... main menu item
     * - in Attach dialog set socket attach, port and click OK
     * - wait User program running appears in status bar
     * - reload page in browser
     * - check breakpoint reached
     * - finish debugger
     */
    public void testAttachDebugger() {
        // assuming server is running in debug mode
        AttachDialogOperator ado = AttachDialogOperator.invoke();
        // "JPDA Debugger"
        String jpdaDebuggerLabel = Bundle.getString("org.netbeans.modules.debugger.jpda.ui.Bundle", "CTL_Connector_name");
        ado.selectDebugger(jpdaDebuggerLabel);
        ado.selectConnector(AttachDialogOperator.ITEM_SOCKET_ATTACH);
        ado.setPort(Utils.getSocketPort());
        ado.ok();
        // "User program running"
        String runningLabel = Bundle.getString("org.netbeans.modules.debugger.jpda.ui.Bundle", "CTL_Debugger_running");
        stt.waitText(runningLabel);
        Utils.reloadPage(SAMPLE_WEB_PROJECT_NAME);
        // check breakpoint reached
        // wait status text "Thread main stopped at SampleClass1.java:##"
        EditorOperator eo = new EditorOperator("index.jsp"); // NOI18N
        int line = eo.getLineNumber();
        stt.waitText("index.jsp:" + line);
        Utils.finishDebugger();
    }

    /** Restart debugger after breakpoint reached.
     * - start to debug main project from main menu
     * - wait until debugger stops at previously set breakpoint
     * - finish debugger
     * - start debugger again
     * - wait until debugger stops at breakpoint
     * - finish debugger
     */
    public void testDebugAfterBreakpoint() {
        // start debugging
        new DebugProjectAction().perform();
        Utils.waitFinished(this, SAMPLE_WEB_PROJECT_NAME, "debug");
        Utils.reloadPage(SAMPLE_WEB_PROJECT_NAME);
        // check the first breakpoint reached
        // wait status text "Thread main stopped at index.jsp:##"
        EditorOperator eo = new EditorOperator("index.jsp"); // NOI18N
        int line = eo.getLineNumber();
        stt.waitText("index.jsp:" + line);
        stt.clear();
        Utils.finishDebugger();
        // start debugger again
        new DebugProjectAction().perform();
        Utils.waitFinished(this, SAMPLE_WEB_PROJECT_NAME, "debug");
        Utils.reloadPage(SAMPLE_WEB_PROJECT_NAME);
        stt.waitText("index.jsp:" + line); // NOI18N
        Utils.finishDebugger();
    }

    /** Restart debugger after server stopped.
     * - start to debug main project from main menu
     * - wait until debugger stops at previously set breakpoint
     * - check proper state of actions on server node
     * - stop server
     * - start debugger again
     * - wait until debugger stops at breakpoint
     * - finish debugger
     */
    public void testDebugAndStopServer() {
        // start debugging
        new DebugProjectAction().perform();
        Utils.waitFinished(this, SAMPLE_WEB_PROJECT_NAME, "debug");
        Utils.reloadPage(SAMPLE_WEB_PROJECT_NAME);
        // check the first breakpoint reached
        // wait status text "Thread main stopped at index.jsp:##"
        EditorOperator eo = new EditorOperator("index.jsp"); // NOI18N
        int line = eo.getLineNumber();
        stt.waitText("index.jsp:" + line);
        stt.clear();

        // check actions on server node are in proper state
        J2eeServerNode serverNode = new J2eeServerNode(Utils.DEFAULT_SERVER);
        assertFalse("Start action on server node should be disabled when stopped at breakpoint.", new StartAction().isEnabled(serverNode));
        assertTrue("Stop action on server node should be enabled when stopped at breakpoint.", new StopAction().isEnabled(serverNode));
        assertTrue("Restart action on server node should be enabled when stopped at breakpoint.", new RestartAction().isEnabled(serverNode));
        assertFalse("Start in Debug Mode action on server node should be disabled when stopped at breakpoint.", new StartDebugAction().isEnabled(serverNode));
        assertTrue("Refresh action on server node should be enabled when stopped at breakpoint.", new RefreshAction().isEnabled(serverNode));

        serverNode.stop();
        // start debugger again
        new DebugProjectAction().perform();
        Utils.waitFinished(this, SAMPLE_WEB_PROJECT_NAME, "debug");
        Utils.reloadPage(SAMPLE_WEB_PROJECT_NAME);
        stt.waitText("index.jsp:" + line);
        Utils.finishDebugger();
    }

    static void verifyActiveNode(Node node) {
        try {
            node.select();
        } catch (TimeoutExpiredException e) {
        }
    }

    static void verifyServerNode(J2eeServerNode serverNode) {
        verifyActiveNode(serverNode);
        assertTrue("Refresh action on server node should be allways enabled.", new RefreshAction().isEnabled(serverNode));
        new RefreshAction().perform(serverNode);
    }

    /** Start another session.
     * - start to debug main project from main menu
     * - wait until debugger stops at previously set breakpoint
     * - try to start debugger again
     * - wait until message informing that server is in suspended state appears
     * - try to run project
     * - wait until message informing that server is in suspended state appears
     * - finish debugger
     * - wait for page in browser and close it
     */
    public void testStartAnotherSession() {
        // start debugging
        new DebugProjectAction().perform();
        Utils.waitFinished(this, SAMPLE_WEB_PROJECT_NAME, "debug");
        Utils.reloadPage(SAMPLE_WEB_PROJECT_NAME);
        // check the first breakpoint reached
        // wait status text "Thread main stopped at index.jsp:##"
        EditorOperator eo = new EditorOperator("index.jsp"); // NOI18N
        int line = eo.getLineNumber();
        stt.waitText("index.jsp:" + line);
        new StopAction().perform();

        new DebugProjectAction().perform();
        OutputTabOperator outputOper = new OutputTabOperator(SAMPLE_WEB_PROJECT_NAME);
        // "Cannot perform required operation, since the server is currently in suspended state and thus cannot handle any requests."
        String suspendedMessage = Bundle.getString("org.netbeans.modules.j2ee.deployment.impl.Bundle", "MSG_ServerSuspended");
        outputOper.waitText(suspendedMessage);
        outputOper.close();

        Action runProjectAction = new Action(null, "Run");
        runProjectAction.perform(new ProjectsTabOperator().getProjectRootNode(SAMPLE_WEB_PROJECT_NAME));
        outputOper = new OutputTabOperator(SAMPLE_WEB_PROJECT_NAME);
        outputOper.waitText(suspendedMessage);
        outputOper.close();
        Utils.finishDebugger();
    }

    /** Test concurrent java and jsp debugging sessions. Also test debugging
     * of jsp in sub folder.
     * - open main class MyBean.java
     * - set breakpoint in it
     * - start debugger from popup on MyBean node
     * - wait until debugger stops at breakpoint
     * - open page simpleInclude.jsp in incl folder
     * - set breakpoint in it
     * - start debugger from popup on simpleInclude.jsp node
     * - wait until debugger stops at breakpoint
     * - open Window|Debugging|Session view
     * - call Make Current popup on MyBean debugging session
     * - wait until pointer is on breakpoint in MyBean.java
     * - call Make Current popup on simpleInclude.jsp debugging session
     * - wait until pointer is on breakpoint in simpleInclude.jsp
     * - call Finish All popup in Sessions view
     * - wait until debugger is finished
     * - close Sessions view
     */
    public void testJavaSession() throws Exception {
        Node beanNode = new Node(new SourcePackagesNode(SAMPLE_WEB_PROJECT_NAME), "org.netbeans.test|MyBean.java"); //NOI18N
        new OpenAction().performAPI(beanNode); // NOI18N
        EditorOperator eoBean = new EditorOperator("MyBean.java"); // NOI18N
        final int lineJavaSource = Utils.setBreakpoint(eoBean, "System.out.println"); // NOI18N
        new DebugJavaFileAction().perform(beanNode);
        stt.waitText("MyBean.java:" + lineJavaSource); //NOI18N

        Node pageNode = new Node(new WebPagesNode(SAMPLE_WEB_PROJECT_NAME), "incl|simpleInclude.jsp"); //NOI18N
        new OpenAction().performAPI(pageNode);
        EditorOperator eoPage = new EditorOperator("simpleInclude.jsp"); // NOI18N
        final int lineJSP = Utils.setBreakpoint(eoPage, "incl/simpleInclude.jsp"); // NOI18N
        // "Debug File"
        new Action(null, new DebugJavaFileAction().getPopupPath()).perform(pageNode);
        Utils.waitFinished(this, SAMPLE_WEB_PROJECT_NAME, "debug");
        Utils.reloadPage(SAMPLE_WEB_PROJECT_NAME + "/incl/simpleInclude.jsp");
        stt.waitText("simpleInclude.jsp:" + lineJSP); //NOI18N

        SessionsOperator so = SessionsOperator.invoke();
        so.makeCurrent("MyBean"); //NOI18N
        // wait pointer in editor (two annotations there)
        new Waiter(new Waitable() {

            @Override
            public Object actionProduced(Object editorOper) {
                return ((EditorOperator) editorOper).getAnnotations(lineJavaSource).length == 2 ? Boolean.TRUE : null;
            }

            @Override
            public String getDescription() {
                return ("Wait 2 annotations in editor."); // NOI18N
            }
        }).waitAction(eoBean);
        so.makeCurrent("MainTestApplication");
        // wait pointer in editor (three annotations there)
        new Waiter(new Waitable() {

            @Override
            public Object actionProduced(Object editorOper) {
                return ((EditorOperator) editorOper).getAnnotations(lineJSP).length == 2 ? Boolean.TRUE : null;
            }

            @Override
            public String getDescription() {
                return ("Wait 2 annotations in editor."); // NOI18N
            }
        }).waitAction(eoPage);
        ContainerOperator debugToolbarOper = Utils.getDebugToolbar();
        so.finishAll();
        // wait until Debug toolbar dismiss
        debugToolbarOper.waitComponentShowing(false);
    }

    /** Stop server just for clean-up.
     * - stop server and wait until it finishes
     */
    public void testStopServer() {
        J2eeServerNode serverNode = new J2eeServerNode(Utils.DEFAULT_SERVER);
        verifyServerNode(serverNode);
        if (serverNode.getServerState() != J2eeServerNode.STATE_STOPPED) {
            serverNode.stop();
        }
    }
}
