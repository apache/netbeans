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
package org.netbeans.modules.ws.qaf;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JDialog;
import junit.framework.Test;
import org.netbeans.api.project.Project;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewJavaFileNameLocationStepOperator;
import org.netbeans.jellytools.OutputTabOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.actions.CleanJavaProjectAction;
import org.netbeans.jellytools.modules.java.editor.GenerateCodeOperator;
import org.netbeans.jellytools.modules.web.NewJspFileNameStepOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.junit.MockServices;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.Listener;
import org.netbeans.modules.j2ee.dd.api.web.Servlet;
import org.netbeans.modules.j2ee.dd.api.web.ServletMapping;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.websvc.api.jaxws.project.config.Endpoint;
import org.netbeans.modules.websvc.api.jaxws.project.config.Endpoints;
import org.netbeans.modules.websvc.api.jaxws.project.config.EndpointsProvider;
import org.netbeans.modules.websvc.api.jaxws.project.config.Handler;
import org.netbeans.modules.websvc.api.jaxws.project.config.HandlerChain;
import org.netbeans.modules.websvc.api.jaxws.project.config.HandlerChains;
import org.netbeans.modules.websvc.api.jaxws.project.config.HandlerChainsProvider;
import org.netbeans.modules.websvc.api.jaxws.bindings.BindingsHandler;
import org.netbeans.modules.websvc.api.jaxws.bindings.BindingsHandlerChain;
import org.netbeans.modules.websvc.api.jaxws.bindings.BindingsHandlerChains;
import org.netbeans.modules.websvc.api.jaxws.bindings.BindingsModel;
import org.netbeans.modules.websvc.api.jaxws.bindings.BindingsModelFactory;
import org.netbeans.modules.xml.retriever.catalog.Utilities;
import org.netbeans.modules.xml.xam.ModelSource;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *  Basic validation suite for web services support in the IDE
 *
 *  Duration of this test suite: aprox. 8min
 *
 * @author lukas.jungmann@sun.com
 */
public class WsValidation extends WebServicesTestBase {

    protected static final String WEB_SERVICES_NODE_NAME = Bundle.getStringTrimmed("org.netbeans.modules.websvc.core.jaxws.nodes.Bundle", "LBL_WebServices");
    private static final String WEB_SERVICE_CLIENTS_NODE_NAME = Bundle.getStringTrimmed("org.netbeans.modules.websvc.core.jaxws.nodes.Bundle", "LBL_ServiceReferences");
    private static final Logger LOG = Logger.getLogger(WsValidation.class.getName());
    private static List<String> resourceConfDialogClosed = new ArrayList<String>();

    private static int foId = 0;

    protected enum HandlerType {

        LOGICAL,
        MESSAGE;

        public String getFileTypeLabel() {
            switch (this) {
                case LOGICAL:
                    return Bundle.getStringTrimmed("org.netbeans.modules.websvc.core.dev.wizard.Bundle", "Templates/WebServices/LogicalHandler.java");
                case MESSAGE:
                    return Bundle.getStringTrimmed("org.netbeans.modules.websvc.core.dev.wizard.Bundle", "Templates/WebServices/SOAPMessageHandler.java");
            }
            throw new AssertionError("Unknown type: " + this); //NOI18N
        }

        public String getMessageType() {
            switch (this) {
                case LOGICAL:
                    return "LogicalMessage"; //NOI18N
                case MESSAGE:
                    return "SOAPMessage"; //NOI18N
            }
            throw new AssertionError("Unknown type: " + this); //NOI18N
        }
    }

    /** Creates a new instance of WsValidation */
    public WsValidation(String name) {
        super(name, Server.GLASSFISH);
    }

    public String getProjectName() {
        return getName().indexOf("Client") > -1 //NOI18N
                ? getWsClientProjectName()
                : getWsProjectName();
    }

    protected String getWsProjectName() {
        return "WsInWeb"; //NOI18N
    }

    protected String getWsClientProjectName() {
        return "WsClientInWeb"; //NOI18N
    }

    protected String getWsName() {
        return "MyWebWs"; //NOI18N
    }

    protected String getWsPackage() {
        return "o.n.m.ws.qaf.ws"; //NOI18N
    }

    protected String getWsClientPackage() {
        return getWsPackage(); //NOI18N
    }

    protected String getWsURL() {
        int port;
        String suffix = "?wsdl"; //NOI18N
        switch (REGISTERED_SERVER) {
            case TOMCAT:
                port = 8084;
                break;
            case JBOSS:
                port = 8080;
                break;
            case SJSAS:
            case GLASSFISH:
                port = 8080;
                suffix = "?Tester"; //NOI18N
                break;
            default:
                throw new AssertionError("Unsupported server"); //NOI18N
        }
        return "http://localhost:" + port + "/" + getProjectName() + "/" + getWsName() + suffix;
    }

    /**
     * Creates a new web service in a web project and checks whether web service
     * node has been created in the project view and web service implementation
     * class has been opened in the editor
     * @throws java.io.IOException
     */
    public void testCreateNewWs() throws IOException {
        // Web Service
        String webServiceLabel = Bundle.getStringTrimmed("org.netbeans.modules.websvc.core.dev.wizard.Bundle", "Templates/WebServices/WebService.java");
        createNewWSFile(getProject(), webServiceLabel);
        NewJavaFileNameLocationStepOperator op = new NewJavaFileNameLocationStepOperator();
        op.setObjectName(getWsName());
        op.setPackage(getWsPackage());
        op.finish();
        // needed for slower machines
        JemmyProperties.setCurrentTimeout("JTreeOperator.WaitNextNodeTimeout", 60000); //NOI18N
        //TODO: following nodes should be expanded by default - this test should check it as well
        Node wsRootNode = new Node(getProjectRootNode(), WEB_SERVICES_NODE_NAME);
        wsRootNode.expand();
        new Node(wsRootNode, getWsName());
        new EditorOperator(getWsName());
        checkNonJSR109Service();
    }

    /**
     * Tests adding operation to webservice using
     * - add operation action from editor's popup menu
     * - add operation action from ws node's context menu
     */
    public void testAddOperation() {
        final EditorOperator eo = new EditorOperator(getWsName());
        //Add Web Service Operation...
        String actionName = Bundle.getStringTrimmed("org.netbeans.modules.websvc.core.actions.Bundle", "LBL_AddWsOperationAction");
        //invoke action from editor's insert menu
        GenerateCodeOperator.openDialog(actionName, eo);
        addWsOperation(eo, "myStringMethod", "String"); //NOI18N
        //invoke action from ws node's context menu
        Node wsRootNode = new Node(getProjectRootNode(), WEB_SERVICES_NODE_NAME);
        wsRootNode.expand();
        Node wsImplNode = new Node(wsRootNode, getWsName());
        //Add Operation...
        actionName = Bundle.getStringTrimmed("org.netbeans.modules.websvc.core.jaxws.actions.Bundle", "LBL_OperationAction");
        wsImplNode.callPopup().pushMenuNoBlock(actionName);
        addWsOperation(eo, "myIntMethod", "int[]"); //NOI18N
    }

    /**
     * Tests setting SOAP version on the web service
     * - check set to SOAP 1.2
     * - check set to SOAP 1.1
     */
    public void testSetSOAP() {
        //Switch to SOAP 1.2
        String actionName = Bundle.getStringTrimmed("org.netbeans.modules.websvc.core.actions.Bundle", "LBL_SetSoap12");
        EditorOperator eo = new EditorOperator(getWsName() + ".java"); //NOI18N
        GenerateCodeOperator.openDialog(actionName, eo);
        eo.save();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            //ignore
        }
        assertTrue("missing @BindingType", eo.contains("@BindingType")); //NOI18N
        //Switch to SOAP 1.1
        actionName = Bundle.getStringTrimmed("org.netbeans.modules.websvc.core.actions.Bundle", "LBL_SetSoap11");
        GenerateCodeOperator.openDialog(actionName, eo);
        eo.save();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            //ignore
        }
        assertFalse("has @BindingType", eo.getText().contains("@BindingType(")); //NOI18N
        assertFalse("has namespace", eo.getText().contains("SOAPBinding.SOAP12HTTP_BINDING")); //NOI18N
    }

    public void testDeployWsProject() throws IOException {
        deployProject(getProjectName());
    }

    /**
     * Tests Test Web Service action on the web service node
     * -check if a browser is opened on the correct URL
     */
    public void testTestWS() {
        assertServerRunning();
        MockServices.setServices(TestURLDisplayer.class);
        TestURLDisplayer td = TestURLDisplayer.getInstance();
        td.invalidateURL();
        Node wsNode = new Node(getProjectRootNode(), WEB_SERVICES_NODE_NAME + "|" + getWsName()); //NOI18N
        String testWsLabel = Bundle.getStringTrimmed("org.netbeans.modules.websvc.core.jaxws.actions.Bundle", "LBL_TesterPageAction");
        wsNode.performPopupAction(testWsLabel);
        try {
            assertEquals("Wrong URL.", getWsURL(), td.waitURL().toString()); //NOI18N
        } catch (InterruptedException ex) {
            //ignore
        }
        td.invalidateURL();
    }

    /**
     * Tests Generate SOAP-over-HTTP Wrapper action on the web service node
     * -check if a ws client is present
     * -check if a RESTful service is generated and contains required operations
     */
    public void testGenerateWrapper() throws Exception {
        assertServerRunning();
        Node wsNode = new Node(getProjectRootNode(), WEB_SERVICES_NODE_NAME + "|" + getWsName()); //NOI18N
        String wrapperLabel = Bundle.getStringTrimmed("org.netbeans.modules.websvc.core.jaxws.actions.Bundle", "LBL_ConvertToRestAction");
        wsNode.performPopupActionNoBlock(wrapperLabel);
        closeResourcesConfDialog();
        String progressLabel = Bundle.getStringTrimmed("org.netbeans.modules.websvc.core.jaxws.saas.Bundle", "MSG_GENERATING_REST_RESOURCE");
        try {
            // wait at most 60 second until progress dialog dismiss
            JemmyProperties.setCurrentTimeout("ComponentOperator.WaitStateTimeout", 60000); //NOI18N
            new JDialogOperator(progressLabel).waitClosed();
        } catch (TimeoutExpiredException e) {
            // ignore when progress dialog was closed before we started to wait for it
        }
        waitForWsImport("(wsimport-", getProjectType().isAntBasedProject()); //NOI18N
        Node wsClientNode = new Node(getProjectRootNode(), WEB_SERVICE_CLIENTS_NODE_NAME + "|" + getWsName()); //NOI18N
        wsClientNode.expand();
        String restLabel = Bundle.getStringTrimmed("org.netbeans.modules.websvc.rest.nodes.Bundle", "LBL_RestServices");
        Node restNode = new Node(getProjectRootNode(), restLabel);
        restNode.expand();
        String restName = getWsName() + "Port"; //NOI18N
        Node restWsNode = new Node(restNode, restName);
        restWsNode.expand();
        EditorOperator eo = new EditorOperator(restName + ".java"); //NOI18N
        assertTrue("myIntMethod missing", eo.contains("myIntMethod")); //NOI18N
        assertTrue("@GET missing", eo.contains("@GET")); //NOI18N
        assertTrue("@Consumes missing", eo.contains("@Consumes")); //NOI18N
        assertTrue("@Produces missing", eo.contains("@Produces")); //NOI18N
        assertTrue("@Path missing", eo.contains("@Path")); //NOI18N
        assertTrue("myStringMethod missing", eo.contains("myStringMethod")); //NOI18N
        assertTrue("getPort missing", eo.contains("getPort()")); //NOI18N
        eo.close(true);
    }

    /**
     * Tests Generate WSDL action on the web service node
     */
    public void testGenerateWSDL() throws IOException {
        Node wsNode = new Node(getProjectRootNode(), WEB_SERVICES_NODE_NAME + "|" + getWsName()); //NOI18N
        String genWSDLLabel = Bundle.getStringTrimmed("org.netbeans.modules.websvc.core.jaxws.actions.Bundle", "LBL_Generate_WSDL");
        wsNode.performPopupAction(genWSDLLabel);
        String genTitle = Bundle.getStringTrimmed("org.netbeans.modules.websvc.core.jaxws.actions.Bundle", "TTL_GenCopyWSDL");
        NbDialogOperator ndo = new NbDialogOperator(genTitle);
        //Check do not copy WSDL
        new JCheckBoxOperator(ndo).clickMouse();
        ndo.ok();
        waitForWsImport("(wsgen-" + getWsName(), getProjectType().isAntBasedProject()); //NOI18N
        new CleanJavaProjectAction().perform();
    }

    public void testDeployWsClientProject() throws IOException {
        deployProject(getProjectName());
    }

    /**
     * Creates a new web service client in a web project and checks whether web
     * service client node has been created in the project view
     * @throws java.io.IOException
     */
    public void testCreateWsClient() throws IOException {
        assertServerRunning();
        //Web Service Client
        String wsClientLabel = Bundle.getStringTrimmed("org.netbeans.modules.websvc.core.client.wizard.Bundle", "Templates/WebServices/WebServiceClient");
        createNewWSFile(getProject(), wsClientLabel);
        NewJavaFileNameLocationStepOperator op = new NewJavaFileNameLocationStepOperator();
        new JButtonOperator(op, 3).push();
        //Browse Web Services
        String browseWsLabel = Bundle.getStringTrimmed("org.netbeans.modules.websvc.core.client.wizard.Bundle", "TTL_SelectService");
        NbDialogOperator ndo = new NbDialogOperator(browseWsLabel);
        JTreeOperator jto = new JTreeOperator(ndo);
        jto.selectPath(jto.findPath(getWsProjectName() + "|" + getWsName())); //NOI18N
        ndo.ok();
        op.finish();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ie) {
        }
        //expand ws client node
        // needed for slower machines
        JemmyProperties.setCurrentTimeout("JTreeOperator.WaitNextNodeTimeout", 60000); //NOI18N
        Node wsClientRootNode = new Node(getProjectRootNode(), WEB_SERVICE_CLIENTS_NODE_NAME);
        wsClientRootNode.expand();
        Node wsClientNode = new Node(wsClientRootNode, getWsName()); //NOI18N
        wsClientNode.expand();
        boolean isAnt = getProjectType().isAntBasedProject();
        waitForWsImport(isAnt ? "wsimport-client-" : "wsimport", isAnt); //NOI18N
        Node wsClientServiceNode = new Node(wsClientNode, getWsName()); //NOI18N
        wsClientServiceNode.expand();
        Node wsClientPortNode = new Node(wsClientServiceNode, getWsName()); //NOI18N
        wsClientPortNode.expand();
        assertTrue(wsClientPortNode.isChildPresent("myStringMethod")); //NOI18N
        assertTrue(wsClientPortNode.isChildPresent("myIntMethod")); //NOI18N
        assertEquals("Wrong number of operations.", 3, wsClientPortNode.getChildren().length);
    }

    /**
     * Tests Call Web Service Operation action in a servlet
     */
    public void testCallWsOperationInServlet() {
        assertServerRunning();
        //create a servlet
        //Web
        String webLabel = Bundle.getStringTrimmed("org.netbeans.modules.web.core.Bundle", "Templates/JSP_Servlet");
        //Servlet
        String servletLabel = Bundle.getStringTrimmed("org.netbeans.modules.web.core.Bundle", "Templates/JSP_Servlet/Servlet.java");
        createNewFile(getWsClientProject(), webLabel, servletLabel);
        NewJavaFileNameLocationStepOperator op = new NewJavaFileNameLocationStepOperator();
        JComboBoxOperator jcbo = new JComboBoxOperator(op, 1);
        jcbo.typeText("org.mycompany.servlets"); //NOI18N
        op.finish();
        //edit code in the servlet
        EditorOperator eo = new EditorOperator("NewServlet"); //NOI18N
        // add new line and select it
        eo.setCaretPosition("\"</h1>\");", false); //NOI18N
        eo.insert("\n//xxx"); //NOI18N
        eo.select("//xxx"); //NOI18N
        callWsOperation(eo, "myIntMethod", eo.getLineNumber()); //NOI18N
        assertTrue("@WebServiceRef has not been found", eo.contains("@WebServiceRef")); //NOI18N
        eo.close(true);
    }

    /**
     * Test Call Web Service Operation action in a JSP
     */
    public void testCallWsOperationInJSP() {
        assertServerRunning();
        //create new JSP
        //Web
        String webLabel = Bundle.getStringTrimmed("org.netbeans.modules.web.core.Bundle", "Templates/JSP_Servlet");
        //JSP
        String servletLabel = Bundle.getStringTrimmed("org.netbeans.modules.web.core.Bundle", "Templates/JSP_Servlet/JSP.jsp");
        createNewFile(getWsClientProject(), webLabel, servletLabel);
        NewJspFileNameStepOperator op = new NewJspFileNameStepOperator();
        op.setJSPFileName("index1"); //NOI18N
        op.finish();
        //edit code in JSP
        EditorOperator eo = new EditorOperator("index1"); //NOI18N
        eo.setCaretPosition("</h1>", false); //NOI18N
        eo.insert("\n<!-- xxx -->"); //NOI18N
        eo.select("<!-- xxx -->"); //NOI18N
        callWsOperation(eo, "myStringMethod", eo.getLineNumber()); //NOI18N
        eo.close(true);
    }

    /**
     * Test Call Web Service Operation action in a regular java file
     */
    public void testCallWsOperationInJavaClass() {
        assertServerRunning();
        //Create new Java class
        createNewFile(getWsClientProject(), "Java", "Java Class");
        NewJavaFileNameLocationStepOperator op = new NewJavaFileNameLocationStepOperator();
        op.setPackage("org.mycompany.classes"); //NOI18N
        op.finish();
        final EditorOperator eo = new EditorOperator("NewClass"); //NOI18N
        eo.replace("}", "    public void callMethod() {\n\t//xxx\n    }\n}\n"); //NOI18N
        eo.select("//xxx"); //NOI18N
        callWsOperation(eo, "myIntMethod", eo.getLineNumber()); //NOI18N
        eo.close(true);
    }

    public void testWsHandlers() throws IOException {
        createHandler(getHandlersPackage(), "WsMsgHandler1", HandlerType.MESSAGE); //NOI18N
        createHandler(getHandlersPackage(), "WsMsgHandler2", HandlerType.MESSAGE); //NOI18N
        createHandler(getHandlersPackage(), "WsLogHandler1", HandlerType.LOGICAL); //NOI18N
        createHandler(getHandlersPackage(), "WsLogHandler2", HandlerType.LOGICAL); //NOI18N
        FileObject fo = getProjectSourceRoot().getFileObject(getWsPackage().replace('.', '/')); //NOI18N
        File handlerCfg = new File(FileUtil.toFile(fo), getWsName() + "_handler.xml"); //NOI18N
        Node serviceNode = new Node(getProjectRootNode(), WEB_SERVICES_NODE_NAME + "|" + getWsName()); //NOI18N
        configureHandlers(serviceNode, handlerCfg, true);
    }

    public void testWsClientHandlers() throws IOException {
        assertServerRunning();
        createHandler(getHandlersPackage(), "WsMsgHandler1", HandlerType.MESSAGE); //NOI18N
        createHandler(getHandlersPackage(), "WsMsgHandler2", HandlerType.MESSAGE); //NOI18N
        createHandler(getHandlersPackage(), "WsLogHandler1", HandlerType.LOGICAL); //NOI18N
        createHandler(getHandlersPackage(), "WsLogHandler2", HandlerType.LOGICAL); //NOI18N
        String wsName = getWsName();
        File handlerCfg;
        if (getProjectType().isAntBasedProject()) {
            FileObject fo = getProject().getProjectDirectory().getFileObject("src/conf/"); //NOI18N
            if (fo == null) {
                fo = getProject().getProjectDirectory();
            }
            String path = "xml-resources/web-service-references/" + wsName + "/bindings/"; //NOI18N
            handlerCfg = new File(FileUtil.toFile(fo), path + wsName + "_handler.xml"); //NOI18N
        } else {
            handlerCfg = new File(FileUtil.toFile(getProject().getProjectDirectory()),
                    "src/jaxws-bindings/" + wsName + "_handler.xml"); //NOI18N
        }
        Node clientNode = new Node(getProjectRootNode(), WEB_SERVICE_CLIENTS_NODE_NAME + "|" + getWsName()); //NOI18N
        configureHandlers(clientNode, handlerCfg, false);
    }

    /**
     * Cleanup method - undeploys projects deployed by this suite
     */
    public void testUndeployProjects() throws IOException {
        undeployProject(getWsProjectName());
        undeployProject(getWsClientProjectName());
    }

    /**
     * Test for Refresh Service action of Web Services node (from WSDL)
     */
    public void testRefreshService() {
        refreshWSDL("service", "", false);
    }

    /**
     * Test for Refresh Client action of Web Services References node
     */
    public void testRefreshClient() {
        refreshWSDL("client", "", false);
    }

    /**
     * Test for Refresh Service action of Web Services node (from WSDL)
     * including WSDL regeneration
     */
    public void testRefreshServiceAndReplaceWSDL() {
        refreshWSDL("service", "", true);
    }

    /**
     * Test for Refresh Client action of Web Services References node
     * including WSDL regeneration
     */
    public void testRefreshClientAndReplaceWSDL() {
        refreshWSDL("client", "", true);
    }

    public static Test suite() {
        return createAllModulesServerSuite(Server.GLASSFISH, WsValidation.class,
                "testCreateNewWs",
                "testAddOperation",
                "testSetSOAP",
                "testStartServer",
                "testWsHandlers",
                "testDeployWsProject",
                "testTestWS",
                "testGenerateWrapper",
                "testGenerateWSDL",
                "testDeployWsProject",
                "testCreateWsClient",
                "testCallWsOperationInServlet",
                "testCallWsOperationInJSP",
                "testCallWsOperationInJavaClass",
                "testRefreshClient",
                "testWsClientHandlers",
                "testDeployWsClientProject",
                "testUndeployProjects",
                "testStopServer");
    }

    protected void addWsOperation(EditorOperator eo, String opName, String opRetVal) {
        //Add Operation
        String actionName = Bundle.getStringTrimmed("org.netbeans.modules.websvc.core.jaxws.actions.Bundle", "TITLE_OperationAction");
        addMethod(eo, actionName, opName, opRetVal);
    }

    protected void addMethod(final EditorOperator eo, String dlgTitle, String opName, String opRetVal) {
        NbDialogOperator dialog = new NbDialogOperator(dlgTitle);
        JTextFieldOperator jtfo = new JTextFieldOperator(dialog, 1); //NOI18N
        jtfo.clearText();
        jtfo.typeText(opName);
        jtfo = new JTextFieldOperator(dialog, 0); //NOI18N
        jtfo.clearText();
        jtfo.typeText(opRetVal);
        dialog.ok();
        eo.save();
        waitForTextInEditor(eo, opName);
    }

    protected void callWsOperation(final EditorOperator eo, String opName, int line) {
        eo.select(line);
        //Call Web Service Operation...
        String actionName = Bundle.getStringTrimmed("org.netbeans.modules.websvc.core.actions.Bundle", "LBL_CallWebServiceOperation");
        if (eo.getToolTipText().contains(".java")) { //NOI18N
            //java files do have Insert code
            GenerateCodeOperator.openDialog(actionName, eo);
        } else {
            //Web Service Client Resources
            String actionGroupName = Bundle.getStringTrimmed("org.netbeans.modules.websvc.core.actions.Bundle", "LBL_WebServiceClientActionGroup");
            try {
                new ActionNoBlock(null, actionGroupName + "|" + actionName).performPopup(eo); //NOI18N
            } catch (TimeoutExpiredException tee) {
                eo.select(line);
                new ActionNoBlock(null, actionGroupName + "|" + actionName).performPopup(eo); //NOI18N
            }
        }
        //Select Operation to Invoke
        String dlgTitle = Bundle.getStringTrimmed("org.netbeans.modules.websvc.core.actions.Bundle", "TTL_SelectOperation");
        NbDialogOperator ndo = new NbDialogOperator(dlgTitle);
        JTreeOperator jto = new JTreeOperator(ndo);
        jto.selectPath(jto.findPath(
                getWsClientProjectName() + "|" + getWsName()//NOI18N
                + "|" + getWsName() + "|" //NOI18N
                + getWsName() + "Port|" + opName)); //NOI18N
        ndo.ok();
        waitForTextInEditor(eo, "port." + opName); //NOI18N
        try {
            Thread.sleep(1500);
        } catch (InterruptedException ex) {
            //ignore
        }
    }

    protected String getWsClientLookupCall() {
        return getWsClientPackage() + "." + getWsName() + "Service " +
                "service = new " +
                getWsClientPackage() + "." + getWsName() + "Service();";
    }

    protected void createHandler(String pkg, String name, HandlerType type) {
        createNewWSFile(getProject(), type.getFileTypeLabel());
        NewJavaFileNameLocationStepOperator op = new NewJavaFileNameLocationStepOperator();
        op.txtObjectName().clearText();
        op.txtObjectName().typeText(name);
        op.cboPackage().clearText();
        op.cboPackage().typeText(pkg);
        op.finish();
        EditorOperator eo = new EditorOperator(name);
        assertTrue(eo.contains(type.getMessageType()));
        eo.close();
    }

    private void configureHandlers(Node n, File handlerCfg, boolean isService) throws IOException {
        assertFalse(handlerCfg.exists());
        //Configure Handlers...
        String handlersLabel = Bundle.getStringTrimmed("org.netbeans.modules.websvc.spi.support.Bundle", "LBL_ConfigureHandlerAction");
        n.performPopupActionNoBlock(handlersLabel);
        //Configure Message Handlers
        String handlersDlgLabel = Bundle.getStringTrimmed("org.netbeans.modules.websvc.core.jaxws.nodes.Bundle", "TTL_MessageHandlerPanel");
        NbDialogOperator ndo = new NbDialogOperator(handlersDlgLabel);

        //add 2 handlers
        String[] handlers = {"WsMsgHandler1", "WsLogHandler1"}; //NOI18N
        addHandlers(ndo, handlers);
        ndo.ok();
        EditorOperator eo = null;
        if (isService) {
            eo = new EditorOperator(getWsName());
            assertTrue("missing @HandlerChain", //NOI18N
                    eo.getText().contains("@HandlerChain(file = \"" + handlerCfg.getName() + "\")")); //NOI18N
        } else {
            boolean isAnt = getProjectType().isAntBasedProject();
            waitForWsImport(isAnt ? "wsimport-client-clean" : "wsimport", isAnt); //NOI18N
        }
        assertTrue(handlerCfg.exists());
        FileObject fo = FileUtil.toFileObject(handlerCfg);
        checkHandlers(new String[]{
                    "WsLogHandler1", "WsMsgHandler1" //NOI18N
                }, fo, isService);

        //remove one handler
        n.performPopupActionNoBlock(handlersLabel);
        ndo = new NbDialogOperator(handlersDlgLabel);
        removeHandlers(ndo, new String[]{"WsLogHandler1"}); //NOI18N
        ndo.ok();
        if (isService) {
            assertTrue("missing @HandlerChain", //NOI18N
                    eo.getText().contains("@HandlerChain(file = \"" + handlerCfg.getName() + "\")")); //NOI18N
        } else {
            boolean isAnt = getProjectType().isAntBasedProject();
            waitForWsImport(isAnt ? "wsimport-client-clean" : "wsimport", isAnt); //NOI18N
        }
        assertTrue(handlerCfg.exists());
        checkHandlers(new String[]{"WsMsgHandler1"}, fo, isService); //NOI18N

        //add remaining handlers
        n.performPopupActionNoBlock(handlersLabel);
        ndo = new NbDialogOperator(handlersDlgLabel);
        addHandlers(ndo, new String[]{"WsLogHandler1", "WsLogHandler2", "WsMsgHandler2"}); //NOI18N
        ndo.ok();
        if (isService) {
            assertTrue("missing @HandlerChain", //NOI18N
                    eo.contains("@HandlerChain(file = \"" + handlerCfg.getName() + "\")")); //NOI18N
        } else {
            boolean isAnt = getProjectType().isAntBasedProject();
            waitForWsImport(isAnt ? "wsimport-client-clean" : "wsimport", isAnt); //NOI18N
        }
        assertTrue(handlerCfg.exists());
        checkHandlers(new String[]{
                    "WsLogHandler1", "WsLogHandler2", //NOI18N
                    "WsMsgHandler1", "WsMsgHandler2" //NOI18N
                }, fo, isService);

        //move up one handler
        n.performPopupActionNoBlock(handlersLabel);
        ndo = new NbDialogOperator(handlersDlgLabel);
        moveUpHandler(ndo, "WsLogHandler2"); //NOI18N
        ndo.ok();
        if (isService) {
            assertTrue("missing @HandlerChain", //NOI18N
                    eo.contains("@HandlerChain(file = \"" + handlerCfg.getName() + "\")")); //NOI18N
        } else {
            boolean isAnt = getProjectType().isAntBasedProject();
            waitForWsImport(isAnt ? "wsimport-client-clean" : "wsimport", isAnt); //NOI18N
        }
        assertTrue(handlerCfg.exists());
        checkHandlers(new String[]{
                    "WsLogHandler2", "WsLogHandler1", //NOI18N
                    "WsMsgHandler1", "WsMsgHandler2" //NOI18N
                }, fo, isService);

        //move down another one
        n.performPopupActionNoBlock(handlersLabel);
        ndo = new NbDialogOperator(handlersDlgLabel);
        moveDownHandler(ndo, "WsMsgHandler1"); //NOI18N
        ndo.ok();
        if (isService) {
            assertTrue("missing @HandlerChain", //NOI18N
                    eo.contains("@HandlerChain(file = \"" + handlerCfg.getName() + "\")")); //NOI18N
        } else {
            boolean isAnt = getProjectType().isAntBasedProject();
            waitForWsImport(isAnt ? "wsimport-client-clean" : "wsimport", isAnt); //NOI18N
        }
        assertTrue(handlerCfg.exists());
        checkHandlers(new String[]{
                    "WsLogHandler2", "WsLogHandler1", //NOI18N
                    "WsMsgHandler2", "WsMsgHandler1" //NOI18N
                }, fo, isService);

        //finally remove all handlers
        n.performPopupActionNoBlock(handlersLabel);
        ndo = new NbDialogOperator(handlersDlgLabel);
        removeHandlers(ndo, new String[]{
                    "WsMsgHandler2", "WsLogHandler2", //NOI18N
                    "WsLogHandler1", "WsMsgHandler1"
                }); //NOI18N
        ndo.ok();

        if (isService) {
            assertFalse("offending @HandlerChain", //NOI18N
                    eo.contains("@HandlerChain(file = \"" + handlerCfg.getName() + "\")")); //NOI18N
            assertFalse(handlerCfg.exists());
        } else {
            boolean isAnt = getProjectType().isAntBasedProject();
            waitForWsImport(isAnt ? "wsimport-client-clean" : "wsimport", isAnt); //NOI18N
        }
    }

    /**
     * Check non-JSR-109 service (web service DD, application DD)
     *
     * @throws java.io.IOException
     */
    protected void checkNonJSR109Service() throws IOException {
        if (ServerType.TOMCAT.equals(REGISTERED_SERVER)) {
            FileObject projectHome = getProject().getProjectDirectory();
            FileObject webInfFO = projectHome.getFileObject("web/WEB-INF"); //NOI18N
            //check sun-jaxws.xml
            FileObject sunJaxWsFO = webInfFO.getFileObject("sun-jaxws.xml"); //NOI18N
            assertNotNull("sun-jaxws.xml present", sunJaxWsFO); //NOI18N
            assertTrue("sun-jaxws.xml present", FileUtil.toFile(sunJaxWsFO).exists()); //NOI18N
            Endpoints endpoints = EndpointsProvider.getDefault().getEndpoints(sunJaxWsFO);
            assertEquals("Should have one endpoint", 1, endpoints.getEndpoints().length); //NOI18N
            Endpoint endpoint = endpoints.findEndpointByName(getWsName());
            assertNotNull(getWsName() + " is missing in sun-jaxws.xml", endpoint); //NOI18N
            //check web.xml
            FileObject webXmlFO = webInfFO.getFileObject("web.xml"); //NOI18N
            WebApp webDD = DDProvider.getDefault().getDDRoot(webXmlFO);
            Listener[] listeners = webDD.getListener();
            assertEquals("1 listener present", 1, listeners.length); //NOI18N
            assertEquals("Invalid listener class", //NOI18N
                    "com.sun.xml.ws.transport.http.servlet.WSServletContextListener", //NOI18N
                    listeners[0].getListenerClass());
            Servlet[] servlets = webDD.getServlet();
            assertEquals("1 servlet present", 1, servlets.length); //NOI18N
            assertEquals("Invalid servlet name", getWsName(), servlets[0].getServletName()); //NOI18N
            assertEquals("Invalid servlet class", //NOI18N
                    "com.sun.xml.ws.transport.http.servlet.WSServlet", //NOI18N
                    servlets[0].getServletClass());
            ServletMapping[] mappings = webDD.getServletMapping();
            assertEquals("1 servlet mapping present", 1, mappings.length); //NOI18N
            assertEquals("Invalid servlet mapping name", getWsName(), mappings[0].getServletName()); //NOI18N
            assertEquals("Invalid url pattern", "/" + getWsName(), //NOI18N
                    mappings[0].getUrlPattern());
        }
    }

    protected void waitForWsImport(String targetName, boolean isAnt) throws IOException {
        OutputTabOperator oto = new OutputTabOperator(targetName); //NOI18N
        oto.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 180000); //NOI18N
        oto.waitText(isAnt ? "(total time: " : "Total time: "); //NOI18N
        dumpOutput();
        assertTrue(targetName + " failed.", oto.getText().contains("BUILD SUCCESS")); //NOI18N
        oto.close();
    }

    protected void waitForTextInEditor(final EditorOperator eo, final String text) {
        try {
            new Waiter(new Waitable() {
                @Override
                public Object actionProduced(Object obj) {
                    return eo.contains(text) ? Boolean.TRUE : null; //NOI18N
                }

                @Override
                public String getDescription() {
                    return ("Editor contains " + text); //NOI18N
                }
            }).waitAction(null);
        } catch (InterruptedException ie) {
            throw new JemmyException("Interrupted.", ie); //NOI18N
        }
    }

    protected Project getWsClientProject() {
        ProjectRootNode node = new ProjectsTabOperator().getProjectRootNode(getWsClientProjectName());
        Project p = ((org.openide.nodes.Node) node.getOpenideNode()).getLookup().lookup(Project.class);
        assertNotNull("Project can't be null", p); //NOI18N
        return p;
    }

    protected String getHandlersPackage() {
        return "o.n.m.ws.qaf.handlers"; //NOI18N
    }

    private void addHandlers(NbDialogOperator ndo, String[] handlers) {
        JButtonOperator jbo = new JButtonOperator(ndo, "Add...");
        //Add Message Handler Class
        String addHandlerDlg = Bundle.getStringTrimmed("org.netbeans.modules.websvc.utilities.ui.Bundle", "TTL_SelectHandler");
        //Source Packages
        String srcPkgLabel = Bundle.getStringTrimmed("org.netbeans.modules.java.j2seproject.Bundle", "NAME_src.dir");
        for (int i = 0; i < handlers.length; i++) {
            jbo.pushNoBlock();
            NbDialogOperator ndo2 = new NbDialogOperator(addHandlerDlg);
            JTreeOperator jto2 = new JTreeOperator(ndo2);
            Node spn = new Node(jto2, srcPkgLabel);
            Node pkg = new Node(spn, getHandlersPackage());
            Node handler = new Node(pkg, handlers[i]);
            handler.select();
            ndo2.ok();
        }
    }

    private void removeHandlers(NbDialogOperator ndo, String[] handlers) {
        //Confirm Handler Configuration Change
        String changeTitle = Bundle.getStringTrimmed("org.netbeans.modules.websvc.spi.support.Bundle", "TTL_CONFIRM_DELETE");
        JTableOperator jto = new JTableOperator(ndo);
        for (int i = 0; i < handlers.length; i++) {
            jto.selectCell(jto.findCellRow(handlers[i]), jto.findCellColumn(handlers[i]));
            //Remove
            new JButtonOperator(ndo, "Remove").pushNoBlock();
            new NbDialogOperator(changeTitle).yes();
        }
    }

    private void moveUpHandler(NbDialogOperator ndo, String handler) {
        JTableOperator jto = new JTableOperator(ndo);
        jto.selectCell(jto.findCellRow(handler), jto.findCellColumn(handler));
        new JButtonOperator(ndo, "Move Up").push();
    }

    private void moveDownHandler(NbDialogOperator ndo, String handler) {
        JTableOperator jto = new JTableOperator(ndo);
        jto.selectCell(jto.findCellRow(handler), jto.findCellColumn(handler));
        new JButtonOperator(ndo, "Move Down").push();
    }

    /**
     * According to parameter this method invokes Refresh action on proper node in
     * Projects tab
     * @param type
     */
    public void refreshWSDL(String type, java.lang.String wsname, boolean includeSources) {
        ProjectsTabOperator prj = new ProjectsTabOperator();
        JTreeOperator prjtree = new JTreeOperator(prj);
        ProjectRootNode prjnd;
        Node actual;
        NbDialogOperator ccr;
        boolean isAnt = getProjectType().isAntBasedProject();
        String refreshActionLbl = Bundle.getStringTrimmed("org.netbeans.modules.websvc.core.jaxws.actions.Bundle", "LBL_RefreshAction");
        if (type.equalsIgnoreCase("service")) {
            prjnd = new ProjectRootNode(prjtree, getWsProjectName());
            if (!wsname.equalsIgnoreCase("")) {
                actual = new Node(prjnd, "Web Services|" + wsname); //NOI18N
            } else {
                actual = new Node(prjnd, "Web Services|" + getWsName()); //NOI18N
            }
            actual.performPopupActionNoBlock(refreshActionLbl); //NOI18N
            ccr = new NbDialogOperator("Confirm Service Refresh"); //NOI18N
            new EventTool().waitNoEvent(1000);
            if (includeSources) {
                new JCheckBoxOperator(ccr, 0).push();
                new EventTool().waitNoEvent(1000);
            }
            ccr.yes();
            try {
                waitForWsImport(isAnt ? "wsimport-service" : "wsimport", isAnt); //NOI18N
            } catch (IOException ex) {
                LOG.log(Level.WARNING, null, ex);
                fail("refreshing wsdl failed, see the log for stacktrace"); //NOI18N
            }
        } else {
            prjnd = new ProjectRootNode(prjtree, getWsClientProjectName());
            actual = new Node(prjnd, "Web Service References|" + getWsName()); //NOI18N
            actual.performPopupActionNoBlock(refreshActionLbl); //NOI18N
            ccr = new NbDialogOperator("Confirm Client Refresh"); //NOI18N
            new EventTool().waitNoEvent(1000);
            if (includeSources) {
                new JCheckBoxOperator(ccr, 0).push();
                new EventTool().waitNoEvent(1000);
            }
            ccr.yes();
            try {
                waitForWsImport(isAnt ? "wsimport-client-" : "wsimport", isAnt); //NOI18N
            } catch (IOException ex) {
                LOG.log(Level.WARNING, null, ex);
                fail("refreshing wsdl failed, see the log for stacktrace"); //NOI18N
            }
        }
    }

    private void checkHandlers(String[] handlerClasses, FileObject handlerConfigFO, boolean isService) throws IOException {
        //Let's keep the config file to resolve possible issues
        handlerConfigFO.copy(FileUtil.toFileObject(getWorkDir()), handlerConfigFO.getName() + foId++, "xml"); //NOI18N
        if (isService) {
            HandlerChains hChains = HandlerChainsProvider.getDefault().getHandlerChains(handlerConfigFO);
            HandlerChain[] chains = hChains.getHandlerChains();
            assertEquals(1, chains.length);
            Handler[] handlers = chains[0].getHandlers();
            assertEquals("Some handler is missing?", handlerClasses.length, handlers.length); //NOI18N
            for (int i = 0; i < handlerClasses.length; i++) {
                Handler h = handlers[i];
                assertEquals(getHandlersPackage() + "." + handlerClasses[i], h.getHandlerName());
                assertEquals(getHandlersPackage() + "." + handlerClasses[i], h.getHandlerClass());
            }
        } else {
            ModelSource ms = Utilities.getModelSource(handlerConfigFO, false);
            BindingsModel bindingsModel = BindingsModelFactory.getDefault().getModel(ms);
            BindingsHandlerChains bChains = bindingsModel.getGlobalBindings().getDefinitionsBindings().getHandlerChains();
            Collection<BindingsHandlerChain> bHChains = bChains.getHandlerChains();
            assertEquals(1, bHChains.size());
            Collection<BindingsHandler> bHandlers = bHChains.iterator().next().getHandlers();
            assertEquals(handlerClasses.length, bHandlers.size());
            int i = 0;
            for (BindingsHandler h : bHandlers) {
                assertEquals(getHandlersPackage() + "." + handlerClasses[i],
                        h.getHandlerClass().getClassName());
                i++;
            }
        }
    }

    protected void closeResourcesConfDialog() {
        if (!resourceConfDialogClosed.contains(getProjectName()) && getJavaEEversion().equals(JavaEEVersion.JAVAEE5)) {
            new Thread("Close REST Resources Configuration dialog") {
                private boolean found = false;
                private static final String dlgLbl = "REST Resources Configuration";

                @Override
                public void run() {
                    while (!found) {
                        try {
                            sleep(300);
                        } catch (InterruptedException ex) {
                            // ignore
                        }
                        JDialog dlg = JDialogOperator.findJDialog(dlgLbl, true, true);
                        if (null != dlg) {
                            found = true;
                            new NbDialogOperator(dlg).ok();
                            resourceConfDialogClosed.add(getProjectName());
                        }
                    }
                }
            }.start();
        }
    }
}
