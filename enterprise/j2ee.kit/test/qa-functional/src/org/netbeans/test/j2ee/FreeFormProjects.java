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
package org.netbeans.test.j2ee;

import java.io.File;
import junit.framework.Test;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.NewWebProjectNameLocationStepOperator;
import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.CloseAction;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.modules.j2ee.J2eeTestCase;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JFileChooserOperator;
import org.netbeans.junit.NbModuleSuite;

/**
 *
 * @author lm97939
 */
public class FreeFormProjects extends J2eeTestCase {

    /** Creates a new instance of AddMethodTest */
    public FreeFormProjects(String name) {
        super(name);
    }

    public static Test suite() {
        return createAllModulesServerSuite(Server.ANY, FreeFormProjects.class,
                "testEjbWithSources",
                "testEarWithSources");
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        System.out.println("########  " + getName() + "  #######");
    }

    public void testEjbWithSources() {
        String location = new File(getDataDir(), "freeform_projects/Secure/Secure-ejb").getAbsolutePath();
        NewProjectWizardOperator npwo = NewProjectWizardOperator.invoke();
        npwo.selectCategory("Java EE");
        npwo.selectProject("EJB Module with Existing Sources");
        npwo.next();
        NewWebProjectNameLocationStepOperator npnlso = new NewWebProjectNameLocationStepOperator();
        npnlso.txtLocation().setText(location);
        npnlso.next();
        //server settings panel - accept defaults
        npnlso.next();
        new JButtonOperator(npwo, "Add Folder...", 0).pushNoBlock();
        JFileChooserOperator j = new JFileChooserOperator();
        j.chooseFile("src" + File.separator + "java");
        j.approveSelection();
        npnlso.finish();
        //wait project appear in projects view
        Node projectNode = new ProjectsTabOperator().getProjectRootNode("Secure-ejb");
        // wait classpath scanning finished
        waitScanFinished();
        Node beansNode = new Node(projectNode, Bundle.getStringTrimmed("org.netbeans.modules.j2ee.ejbjar.project.ui.Bundle", "LBL_node"));
        Node node = new Node(beansNode, "AccountStateSB");
        node.expand();
        String children[] = node.getChildren();
        assertTrue("AccountStateSB node has no children.", children.length > 0);
        new OpenAction().perform(node);
        new EditorOperator("AccountStateBean").close();
        new Node(projectNode, "Configuration Files|ejb-jar.xml");
        MainWindowOperator mwo = MainWindowOperator.getDefault();
        mwo.getTimeouts().setTimeout("Waiter.WaitingTime", 300000);
        // Build project
        projectNode.performPopupAction("Clean and Build");
        mwo.waitStatusText("Finished");
        new CloseAction().perform(projectNode);
    }

    public void testEarWithSources() {
        String location = new File(getDataDir(), "freeform_projects/Secure").getAbsolutePath();
        NewProjectWizardOperator npwo = NewProjectWizardOperator.invoke();
        npwo.selectCategory("Java EE");
        npwo.selectProject("Enterprise Application with Existing Sources");
        npwo.next();
        NewWebProjectNameLocationStepOperator npnlso = new NewWebProjectNameLocationStepOperator();
        npnlso.txtLocation().setText(location);
        npnlso.next();
        npnlso.next();
        npnlso.btFinish().pushNoBlock();
        new NbDialogOperator("Warning").ok();
        npnlso.getTimeouts().setTimeout("ComponentOperator.WaitStateTimeout", 120000);
        npnlso.waitClosed();
        //wait project appear in projects view
        Node rootNode = new ProjectsTabOperator().getProjectRootNode("Secure");

        Node n = new Node(rootNode, "Java EE Modules|Secure-war.war");
        n.performPopupAction("Open Project");
        n = new Node(rootNode, "Java EE Modules|Secure-ejb.jar");
        n.performPopupAction("Open Project");
        // wait classpath scanning finished
        waitScanFinished();
        MainWindowOperator mwo = MainWindowOperator.getDefault();
        mwo.getTimeouts().setTimeout("Waiter.WaitingTime", 300000);
        // Build project
        rootNode.performPopupAction("Clean and Build");
        mwo.waitStatusText("Finished");
    }
}
