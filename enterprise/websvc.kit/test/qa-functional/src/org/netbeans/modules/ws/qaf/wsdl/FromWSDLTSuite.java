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

package org.netbeans.modules.ws.qaf.wsdl;

import java.io.File;
import java.io.IOException;
import junit.framework.Test;
import org.netbeans.api.project.Project;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.NewJavaFileNameLocationStepOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.SourcePackagesNode;
import org.netbeans.jemmy.JemmyProperties;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.modules.ws.qaf.WsValidation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * 
 * @author lukas
 */
public class FromWSDLTSuite extends WsValidation {
    
    /** Default constructor.
     * @param testName name of particular test case
    */
    public FromWSDLTSuite(String name) {
        super(name);
    }

    @Override
    protected String getWsClientPackage() {
        return "o.n.m.ws.qaf.client.wsdl"; //NOI18N
    }

    @Override
    protected String getWsClientProjectName() {
        return "WsClientFromWSDL"; //NOI18N
    }

    @Override
    protected String getWsName() {
        return super.getWsName();
    }

    @Override
    protected String getWsPackage() {
        return "o.n.m.ws.qaf.ws.wsdl"; //NOI18N
    }

    @Override
    protected String getWsProjectName() {
        return "WsFromWSDL"; //NOI18N
    }

    public static Test suite() {
        return NbModuleSuite.create(addServerTests(
                NbModuleSuite.createConfiguration(FromWSDLTSuite.class),
                "testWSFromWSDL",
                "testRefreshService",
                "testRefreshServiceAndReplaceWSDL"
                ).enableModules(".*").clusters(".*"));
    }
    
    public void testWSFromWSDL() throws IOException {
        File wsdl = new File(getDataDir(), "resources/AddNumbers.wsdl");
        String wsdlPath = wsdl.getCanonicalPath();
        createNewWSFromWSDL(getProject(), getWsName(), getWsPackage(), wsdlPath);
        JemmyProperties.setCurrentTimeout("JTreeOperator.WaitNextNodeTimeout", 60000); //NOI18N
        Node wsRootNode = new Node(getProjectRootNode(), WEB_SERVICES_NODE_NAME);
        wsRootNode.expand();
        Node wsNode = new Node(wsRootNode, "AddNumbers"); //NOI18N
        wsNode.expand();
        JemmyProperties.setCurrentTimeout("JTreeOperator.WaitNextNodeTimeout", 60000); //NOI18N
        new Node(wsNode, "addNumbers"); //NOI18N
        assertTrue("There's no \"oneWayInt\" node", wsNode.isChildPresent("oneWayInt")); //NOI18N
        assertEquals("Only two operations should be there", 2, wsNode.getChildren().length);
        SourcePackagesNode spn = new SourcePackagesNode(getProjectRootNode());
        spn.expand();
        new Node(spn, getWsPackage() + "|" + getWsName());
        EditorOperator eo = new EditorOperator(getWsName());
        assertTrue(eo.contains("AddNumbersFault_Exception"));
        assertTrue(eo.contains("org.netbeans.websvc.qatests.ws.addnumbers.AddNumbersPortType"));
        FileObject srcRoot = getProject().getProjectDirectory().getFileObject("src/java");
        File createdFile = new File(FileUtil.toFile(srcRoot), getWsPackage().replace('.', '/') + "/" + getWsName() + ".java");
        assertTrue("Ws Impl class has not been created", createdFile.exists());
    }
    
    protected void createNewWSFromWSDL(Project p, String name, String pkg, String wsdl) throws IOException {
        //Web Service from WSDL
        String fromWSDLLabel = Bundle.getStringTrimmed("org.netbeans.modules.websvc.core.dev.wizard.Bundle", "Templates/WebServices/WebServiceFromWSDL.java");
        createNewWSFile(p, fromWSDLLabel);
        NewJavaFileNameLocationStepOperator op = new NewJavaFileNameLocationStepOperator();
        op.txtObjectName().clearText();
        op.txtObjectName().typeText(name);
        op.cboPackage().clearText();
        op.cboPackage().typeText(pkg);
        JTextFieldOperator jtfo = new JTextFieldOperator(op, 0);
        jtfo.clearText();
        jtfo.typeText(wsdl);
        //Need to wait until WSPort txt field is populated
        jtfo = new JTextFieldOperator(op, 1);
        //the format of port selection is: "<serviceName>#<portName>"
        jtfo.waitText("#"); //NOI18N
        op.finish();
        boolean isAnt = getProjectType().isAntBasedProject();
        waitForWsImport("(wsimport-service-" + name, isAnt); //NOI18N
    }
    
    @Override
     public void testRefreshService() {
        refreshWSDL("service","AddNumbersService[AddNumbersPort]",false);
    }
    
    @Override
    public void testRefreshServiceAndReplaceWSDL() {
        refreshWSDL("service","AddNumbersService[AddNumbersPort]",true);
    }

}
