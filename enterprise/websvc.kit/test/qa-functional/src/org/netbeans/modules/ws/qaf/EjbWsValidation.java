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

import junit.framework.Test;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewJavaFileNameLocationStepOperator;
import org.netbeans.jellytools.modules.java.editor.GenerateCodeOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTreeOperator;
import org.netbeans.modules.ws.qaf.WebServicesTestBase.ProjectType;

/**
 *  Basic validation suite for web services support in the IDE
 *
 *  Duration of this test suite: aprox. 7min
 *
 * @author Lukas Jungmann, Jiri Skrivanek
 */
public class EjbWsValidation extends WsValidation {

    /** Default constructor.
     * @param testName name of particular test case
     */
    public EjbWsValidation(String name) {
        super(name);
    }

    @Override
    protected ProjectType getProjectType() {
        return ProjectType.EJB;
    }

    @Override
    protected String getWsProjectName() {
        return "WsInEjb"; //NOI18N
    }

    @Override
    protected String getWsClientProjectName() {
        return "WsClientInEjb"; //NOI18N
    }

    @Override
    protected String getWsName() {
        return "MyEjbWs"; //NOI18N
    }

    @Override
    protected String getWsPackage() {
        return "o.n.m.ws.qaf.ws.ejb"; //NOI18N
    }

    @Override
    protected String getWsURL() {
        String suffix = "?wsdl"; //NOI18N
        if (REGISTERED_SERVER.equals(ServerType.GLASSFISH)) {
            suffix = "?Tester"; //NOI18N
        }
        return "http://localhost:8080/" + getWsName() + "/" + getWsName() + suffix; //NOI18N
    }

    @Override
    protected String getWsClientPackage() {
        return getWsPackage(); //NOI18N
    }

    public static Test suite() {
        return createAllModulesServerSuite(Server.GLASSFISH, EjbWsValidation.class,
                "testCreateNewWs",
                "testAddOperation",
                "testSetSOAP",
                "testGenerateWSDL",
                "testStartServer",
                "testWsHandlers",
                "testDeployWsProject",
                "testTestWS",
                "testCreateWsClient",
                "testRefreshClientAndReplaceWSDL",
                "testCallWsOperationInSessionEJB",
                "testCallWsOperationInJavaClass",
                "testWsFromEJBinClientProject",
                "testWsClientHandlers",
                "testDeployWsClientProject",
                "testUndeployProjects",
                "testStopServer");
    }

    public void testWsFromEJBinClientProject() {
        String wsName = "WsFromEJB"; //NOI18N
        // Web Service
        String webServiceLabel = Bundle.getStringTrimmed("org.netbeans.modules.websvc.core.dev.wizard.Bundle", "Templates/WebServices/WebService.java");
        createNewWSFile(getProject(), webServiceLabel);
        NewJavaFileNameLocationStepOperator op = new NewJavaFileNameLocationStepOperator();
        op.setObjectName(wsName);
        op.setPackage(getWsPackage());
        JRadioButtonOperator jrbo = new JRadioButtonOperator(op, 1);
        jrbo.setSelected(true);
        new JButtonOperator(op, 0).pushNoBlock();
        //Browse Enterprise Bean
        String browseEjbDlgTitle = Bundle.getStringTrimmed("org.netbeans.modules.websvc.core.dev.wizard.Bundle", "LBL_BrowseBean_Title");
        NbDialogOperator ndo = new NbDialogOperator(browseEjbDlgTitle);
        JTreeOperator jto = new JTreeOperator(ndo);
        //Enterprise Beans
        String ejbNodeLabel = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.ejbjar.project.ui.Bundle", "LBL_node");
        Node ejbsNode = new Node(jto, getProjectName() + "|" + ejbNodeLabel);
        ejbsNode.expand();
        new Node(ejbsNode, "NewSession").select();
        ndo.ok();
        op.finish();
        JemmyProperties.setCurrentTimeout("JTreeOperator.WaitNextNodeTimeout", 60000); //NOI18N
        Node wsRootNode = new Node(getProjectRootNode(), WEB_SERVICES_NODE_NAME);
        wsRootNode.expand();
        Node wsNode = new Node(wsRootNode, wsName); //NOI18N
        wsNode.expand();
        JemmyProperties.setCurrentTimeout("JTreeOperator.WaitNextNodeTimeout", 60000); //NOI18N
        Node myBmNode = new Node(wsNode, "myBm"); //NOI18N
        assertEquals("Only one operation should be there", 1, wsNode.getChildren().length);
        EditorOperator eo = new EditorOperator(wsName);
        assertTrue(eo.contains("@Stateless"));
        assertTrue(eo.contains("@EJB"));
        assertTrue(eo.contains("ejbRef"));
        assertTrue(eo.contains("@WebMethod"));
        assertTrue(eo.contains("myBm"));
    }

    /**
     * Tests Call Web Service Operation action in a servlet
     */
    public void testCallWsOperationInSessionEJB() {
        assertServerRunning();
        //create a session bean
        String ejbName = "NewSession";
        //Enterprise
        String enterpriseLabel = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.ejbcore.resources.Bundle", "Templates/J2EE");
        //Session Bean
        String sessionBeanLabel = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.ejbcore.ejb.wizard.session.Bundle", "Templates/J2EE/Session");
        createNewFile(getWsClientProject(), enterpriseLabel, sessionBeanLabel);
        NewJavaFileNameLocationStepOperator op = new NewJavaFileNameLocationStepOperator();
        op.txtObjectName().clearText();
        op.txtObjectName().typeText(ejbName);
        op.cboPackage().clearText();
        op.cboPackage().typeText("org.mycompany.ejbs"); //NOI18N
        op.finish();
        new EventTool().waitNoEvent(2000);
        //Add business method
        final EditorOperator eo = new EditorOperator(ejbName); //NOI18N
        addBusinessMethod(eo, "myBm", "String"); //NOI18N
        //edit code in the EJB
        // add new line and select it
        eo.setCaretPosition("myBm() {", false); //NOI18N
        eo.insert("\n//xxx"); //NOI18N
        eo.select("//xxx"); //NOI18N
        callWsOperation(eo, "myIntMethod", eo.getLineNumber()); //NOI18N
        assertTrue("@WebServiceRef has not been found", eo.contains("@WebServiceRef")); //NOI18N
        assertFalse("Lookup present", eo.contains(getWsClientLookupCall()));
        eo.close(true);
    }

    protected void addBusinessMethod(EditorOperator eo, String mName, String mRetVal) {
        //Add Business Method...
        String actionName = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action.Bundle", "LBL_AddBusinessMethodAction");
        eo.setCaretPosition(16, 1);
        GenerateCodeOperator.openDialog(actionName, eo);
        addMethod(eo, actionName, mName, mRetVal);
    }
}
