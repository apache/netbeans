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
package org.netbeans.qa.form.gaps;

import java.io.IOException;
import junit.framework.Test;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.modules.form.ComponentInspectorOperator;
import org.netbeans.jellytools.modules.form.FormDesignerOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.operators.ContainerOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.qa.form.ExtJellyTestCase;

/**
 *
 * @author Adam Senk adam.senk@oracle.com
 *
 * This is Functional test of Gap visualization feature (since 7.2)
 */
public class GapsTest extends ExtJellyTestCase {

    public String DATA_PROJECT_NAME = "SampleProject";
    public String PACKAGE_NAME = "data";
    public String PROJECT_NAME = "Java";
    private String FILE_NAME = "Gaps";
    public String FRAME_ROOT = "[JFrame]";
    public String workdirpath;
    public Node formnode;
    private ProjectsTabOperator pto;
    ProjectRootNode prn;
    FormDesignerOperator opDesigner;
    ContainerOperator jfo;

    public GapsTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return NbModuleSuite.create(NbModuleSuite.createConfiguration(GapsTest.class).addTest(
                "testOpenCloseGapDialog",
                "testpopUpDialogInvoke",
                "testNewSizeOfGap",
                "testpopUpDialogOnButton").gui(true).clusters(".*").enableModules(".*"));

    }

    @Override
    public void setUp() throws IOException {
        openDataProjects(DATA_PROJECT_NAME);
        workdirpath = getWorkDir().getParentFile().getAbsolutePath();
        System.out.println("########  " + getName() + "  #######");

        pto = new ProjectsTabOperator();
        prn = pto.getProjectRootNode(DATA_PROJECT_NAME);
        prn.select();

        formnode = new Node(prn, "Source Packages|" + PACKAGE_NAME + "|" + FILE_NAME);
        formnode.select();

        OpenAction openAction = new OpenAction();
        openAction.perform(formnode);
    }
    
     public void testOpenCloseGapDialog() {
        opDesigner = new FormDesignerOperator(FILE_NAME);
        opDesigner.clickMouse(400, 70, 2);
         
        EditLayoutSpaceOperator elso = new EditLayoutSpaceOperator();
        elso.verifySmall();
        elso.Cancel();
    }
     
     public void testpopUpDialogInvoke() {
        opDesigner = new FormDesignerOperator(FILE_NAME);
        opDesigner.clickForPopup(400, 70);
        JPopupMenuOperator jpmo= new JPopupMenuOperator();
        waitNoEvent(500);
        jpmo.pushMenuNoBlock("Edit Layout Space...");
        waitNoEvent(500);
        EditLayoutSpaceOperator elso = new EditLayoutSpaceOperator();
        
        elso.Ok();
    }
     
     public void testpopUpDialogOnButton() {
        opDesigner = new FormDesignerOperator(FILE_NAME);
        
        ComponentInspectorOperator cio = new ComponentInspectorOperator();
        Node inspectorRootNode = new Node(cio.treeComponents(), FRAME_ROOT);
        inspectorRootNode.select();
        inspectorRootNode.expand();
        
        Node buttonNode = new Node(inspectorRootNode, "jButton1 [JButton]");
        buttonNode.callPopup();
        
        JPopupMenuOperator jpmo= new JPopupMenuOperator();
        waitNoEvent(500);
        jpmo.pushMenuNoBlock("Edit Layout Space...");
        waitNoEvent(500);
        
        
        
        EditLayoutSpaceOperator elso = new EditLayoutSpaceOperator();
        
        elso.verify();
        
        assertEquals("default small", (String) elso.cbBottom().getItemAt(0));
        assertEquals("default medium", (String) elso.cbBottom().getItemAt(1));
        assertEquals("default large", (String) elso.cbBottom().getItemAt(2));
        
        assertEquals("default", (String) elso.cbLeft().getItemAt(0));
        elso.Cancel();
    }

    public void testNewSizeOfGap() {
        opDesigner = new FormDesignerOperator(FILE_NAME);
        ComponentInspectorOperator cio = new ComponentInspectorOperator();
        Node inspectorRootNode = new Node(cio.treeComponents(), FRAME_ROOT);
        inspectorRootNode.select();
        inspectorRootNode.expand();
        
        Node buttonNode = new Node(inspectorRootNode, "jButton1 [JButton]");
        buttonNode.callPopup();
        
        JPopupMenuOperator jpmo= new JPopupMenuOperator();
        waitNoEvent(500);
        jpmo.pushMenuNoBlock("Edit Layout Space...");
        waitNoEvent(500);
        
        EditLayoutSpaceOperator elso = new EditLayoutSpaceOperator();
        elso.setSizeOfGapTop("800");
        waitNoEvent(500);

        findInCode(".addContainerGap(800, Short.MAX_VALUE)", opDesigner);
    }
        
}
