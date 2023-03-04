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
package org.netbeans.modules.ws.qaf.designer;

import junit.framework.Test;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.actions.SaveAllAction;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.ws.qaf.WebServicesTestBase;

/**
 *  Basic validation suite for web service designer
 *
 *  Duration of this test suite: aprox. 3min
 *
 * @author lukas.jungmann@sun.com
 */
public class WebServiceDesignerTest extends WebServicesTestBase {

    public WebServiceDesignerTest(String name) {
        super(name);
    }

    @Override
    protected String getProjectName() {
        return getName().indexOf("Ejb") < 0 ? "60_webapp" : "65_ejbmodule"; //NOI18N
    }

    public void testAddOperation() {
        addOperation("EmptyWs", 0, false); //NOI18N
    }

    public void testRemoveOperation() {
        removeOperation("EmptyWs", 1, false); //NOI18N
    }

    public void testAddOperation2() {
        addOperation("SampleWs", 2, false); //NOI18N
    }

    public void testRemoveOperation2() {
        removeOperation("SampleWs", 3, false); //NOI18N
    }

    public void testAddOperationToIntf() {
        addOperation("WsImpl", 1, true); //NOI18N
    }

    public void testRemoveOperationFromIntf() {
        removeOperation("WsImpl", 2, true); //NOI18N
    }

    public void testEjbAddOperation() {
        String wsName = "FromWSDL";
        int opCount = 2;
        openFileInEditor(wsName);
        assertEquals(opCount, WsDesignerUtilities.operationsCount(wsName));
        WsDesignerUtilities.invokeAddOperation(wsName);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ex) {
            //ignore
        }
        //Add Operation...
        String actionName = Bundle.getStringTrimmed("org.netbeans.modules.websvc.design.view.actions.Bundle", "TTL_AddWsOperation");
        NbDialogOperator dialog = new NbDialogOperator(actionName);
        JTextFieldOperator jtfo = new JTextFieldOperator(dialog, "operation");
        jtfo.clearText();
        jtfo.typeText("addedOp");
        dialog.ok();
        try {
            Thread.sleep(10000);
        } catch (InterruptedException ex) {
            //ignore
        }
//        new JTextFieldOperator(dialog, 2).setText("test1"); //NOI18N
//        new JTextFieldOperator(dialog, 1).setText("String"); //NOI18N
    }

    public void testEjbRemoveOperation() {
        String wsName = "FromWSDL";
        openFileInEditor(wsName);
        WsDesignerUtilities.invokeRemoveOperation(wsName, "addedOp", false); //NOI18N
        NbDialogOperator ndo = new NbDialogOperator("Question"); //NOI18N
        ndo.yes();
        try {
            Thread.sleep(5000);
        } catch (InterruptedException ex) {
            //ignore
        }
    }

    public void testGoToSource() {
        String wsName = "EmptyWs"; //NOI18N
        String opName = "test1"; //NOI18N
        openFileInEditor(wsName);
        WsDesignerUtilities.invokeGoToSource(wsName, opName);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            //ignore
        }
        EditorOperator eo = new EditorOperator(wsName);
        assertEquals(24, eo.getLineNumber());
//      see: http://www.netbeans.org/issues/show_bug.cgi?id=150923
//        wsName = "WsImpl"; //NOI18N
//        openFileInEditor(wsName);
//        WsDesignerUtilities.invokeGoToSource(wsName, opName);
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException ex) {
//            //ignore
//        }
//        eo = new EditorOperator(wsName);
//        assertEquals(18, eo.getLineNumber());
        wsName = "SampleWs"; //NOI18N
        opName = "sayHi"; //NOI18N
        openFileInEditor(wsName);
        WsDesignerUtilities.invokeGoToSource(wsName, opName);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            //ignore
        }
        eo = new EditorOperator(wsName);
        assertEquals(33, eo.getLineNumber());
    }

    //only sanity test (see if there's no exception)
    //some checks can be added later
    public void testOperationButtons() {
        String wsName = "SampleWs"; //NOI18N
        WsDesignerUtilities.invokeAdvanced(wsName);
        try {
            //slow down a bit
            Thread.sleep(1000);
        } catch (InterruptedException ex) {
            //ignore
        }
        NbDialogOperator o = new NbDialogOperator(wsName);
        o.cancel();
        String opName = "voidOperation"; //NOI18N
        WsDesignerUtilities.clickOnButton(wsName, opName, 0);
        WsDesignerUtilities.clickOnButton(wsName, opName, 2);
        WsDesignerUtilities.clickOnExpander(wsName, opName);
        WsDesignerUtilities.clickOnExpander(wsName, opName);
        WsDesignerUtilities.clickOnButton(wsName, opName, 1);
        opName = "sayHi"; //NOI18N
        WsDesignerUtilities.clickOnButton(wsName, opName, 1);
        WsDesignerUtilities.clickOnButton(wsName, opName, 0);
        WsDesignerUtilities.clickOnExpander(wsName, opName);
        WsDesignerUtilities.clickOnExpander(wsName, opName);
        WsDesignerUtilities.clickOnButton(wsName, opName, 0);
        WsDesignerUtilities.clickOnButton(wsName, opName, 2);
        WsDesignerUtilities.clickOnButton(wsName, opName, 0);
    }

    private void addOperation(String wsName, int opCount, boolean hasInterface) {
        openFileInEditor(wsName);
        assertEquals(opCount, WsDesignerUtilities.operationsCount(wsName));
        WsDesignerUtilities.invokeAddOperation(wsName);
        //Add Operation...
        String actionName = Bundle.getStringTrimmed("org.netbeans.modules.websvc.core.jaxws.actions.Bundle", "LBL_OperationAction");
        NbDialogOperator dialog = new NbDialogOperator(actionName);
        new JTextFieldOperator(dialog, "operation").setText("test1"); //NOI18N
        new JTextFieldOperator(dialog, "java.lang.String").setText("String"); //NOI18N
        dialog.ok();
        try {
            //slow down a bit
            Thread.sleep(1500);
        } catch (InterruptedException ex) {
            //ignore
        }
        new SaveAllAction().performAPI();
        WsDesignerUtilities.source(wsName);
        EditorOperator eo = new EditorOperator(wsName);
        assertNotNull(eo);
        if (hasInterface) {
            assertFalse(eo.contains("import javax.jws.WebMethod;")); //NOI18N
            assertFalse(eo.contains("@WebMethod(operationName = \"test1\")")); //NOI18N
//            see http://www.netbeans.org/issues/show_bug.cgi?id=150896
//            assertEquals(opCount + 1, WsDesignerUtilities.operationsCount(wsName));
        } else {
            assertTrue(eo.contains("import javax.jws.WebMethod;")); //NOI18N
            assertTrue(eo.contains("@WebMethod(operationName = \"test1\")")); //NOI18N
            assertEquals(opCount + 1, WsDesignerUtilities.operationsCount(wsName));
        }
        assertTrue(eo.contains("public String test1() {")); //NOI18N
        //check ws endpoint interface
        if (hasInterface) {
            //XXX-rather should find interface from the source
            String iName = "EndpointI"; //NOI18N
            openFileInEditor(iName);
            EditorOperator eo2 = new EditorOperator(iName);
            assertTrue(eo2.contains("public String test1();")); //NOI18N
            eo2.close();
        }
    }

    private void removeOperation(String wsName, int opCount, boolean hasInterface) {
        openFileInEditor(wsName);
        WsDesignerUtilities.invokeRemoveOperation(wsName, "test1", opCount % 2 == 0); //NOI18N
        NbDialogOperator ndo = new NbDialogOperator("Question"); //NOI18N
        ndo.yes();
        //see: http://www.netbeans.org/issues/show_bug.cgi?id=150896
        if (!hasInterface) {
            assertEquals(opCount - 1, WsDesignerUtilities.operationsCount(wsName));
        }
        new SaveAllAction().performAPI();
        WsDesignerUtilities.source(wsName);
        EditorOperator eo = new EditorOperator(wsName);
        assertNotNull(eo);
        assertFalse(eo.contains("@WebMethod(operationName = \"test1\")")); //NOI18N
        if (hasInterface) {
            assertTrue(eo.contains("public String test1() {")); //NOI18N
        } else {
            assertFalse(eo.contains("public String test1() {")); //NOI18N
        }
        //check ws endpoint interface
        if (hasInterface) {
            //XXX-rather should find interface from the source
            String iName = "EndpointI"; //NOI18N
            openFileInEditor(iName);
            EditorOperator eo2 = new EditorOperator(iName);
            assertNotNull(eo2);
            assertFalse(eo2.contains("public String test1();")); //NOI18N
            eo2.close();
        }
    }

    private void openFileInEditor(String fileName) {
        //XXX:
        //there's some weird bug:
        //if project with webservices is checked out from VCS (cvs)
        //and its class is opened in the editor then there's no
        //web service designer or it is not initialized correctly :(
        Node wsNode = new Node(getProjectRootNode(), "Web Services");
        if (wsNode.isCollapsed()) {
            wsNode.expand();
        }
        //end
        SourcePackagesNode spn = new SourcePackagesNode(getProjectRootNode());
        Node n = new Node(spn, "samples|" + fileName); //NOI18N
        new OpenAction().perform(n);
    }

    public static Test suite() {
        return NbModuleSuite.create(addServerTests(Server.GLASSFISH,
                NbModuleSuite.createConfiguration(WebServiceDesignerTest.class),
                "testAddOperation", //NOI18N
                "testAddOperation2", //NOI18N
                "testAddOperationToIntf", //NOI18N
                "testOperationButtons", //NOI18N
                "testGoToSource", //NOI18N
                "testRemoveOperation", //NOI18N
                "testRemoveOperation2", //NOI18N
                "testRemoveOperationFromIntf" //NOI18N
//                "testEjbAddOperation", //NOI18N
//                "testEjbRemoveOperation" //NOI18N
                ).enableModules(".*").clusters(".*")); //NOI18N
    }
}
