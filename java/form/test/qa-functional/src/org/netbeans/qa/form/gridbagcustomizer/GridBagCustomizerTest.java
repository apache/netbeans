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
package org.netbeans.qa.form.gridbagcustomizer;

import java.util.ArrayList;
import junit.framework.Test;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.EditAction;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.modules.form.ComponentInspectorOperator;
import org.netbeans.jellytools.modules.form.FormDesignerOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.qa.form.ExtJellyTestCase;

/**
 *
 * @author Adam Senk adam.senk[at]oracle.com
 * 
 * This test is testing new Grid Bag Layout customizer, that is included since NB 7.0
 */
public class GridBagCustomizerTest extends ExtJellyTestCase {

    public String FILE_NAME = "clear_JFrame";
    public String PACKAGE_NAME = "data";
    public String DATA_PROJECT_NAME = "SampleProject";
    public String FRAME_ROOT = "[JFrame]";

    public GridBagCustomizerTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return NbModuleSuite.create(NbModuleSuite.createConfiguration(GridBagCustomizerTest.class).addTest(
                "testInvokeAndClose").gui(true).clusters(".*").enableModules(".*"));

    }

    public void testInvokeAndClose() {

        ProjectsTabOperator pto = new ProjectsTabOperator();
        ProjectRootNode prn = pto.getProjectRootNode(DATA_PROJECT_NAME);
        prn.select();
        Node formnode = new Node(prn, "Source Packages|" + PACKAGE_NAME + "|" + FILE_NAME);
        formnode.select();
        log("Form node selected.");

        EditAction editAction = new EditAction();
        editAction.perform(formnode);
        log("Source Editor window opened.");

        OpenAction openAction = new OpenAction();
        openAction.perform(formnode);
        log("Form Editor window opened.");

        FormDesignerOperator designer = new FormDesignerOperator(FILE_NAME);
        designer.source();
        designer.design();
        
        new ComponentInspectorOperator().freezeNavigatorAndRun(new Runnable() {

            @Override
            public void run() {
                FormDesignerOperator designer = new FormDesignerOperator(FILE_NAME);
                ComponentInspectorOperator inspector = new ComponentInspectorOperator();
                //Action action=new ActionNoBlock(null,"Add From Palette|Swing Controls|Button");
                // inspector.performAction(action, "[JFrame]");
                Node actNode = new Node(inspector.treeComponents(), "[JFrame]"); // NOI18N

                
                actNode.select();
                runPopupOverNode("Add From Palette|Swing Controls|Button", actNode);

                runPopupOverNode("Add From Palette|Swing Controls|Button", actNode);
                runPopupOverNode("Add From Palette|Swing Controls|Button", actNode);
                runPopupOverNode("Add From Palette|Swing Controls|Button", actNode);
                runPopupOverNode("Set Layout|Grid Bag Layout", actNode);
                runPopupOverNode("Customize Layout...", actNode);

                CustomizeLayoutOperator clo = new CustomizeLayoutOperator();
                clo.dragNDrop(500, 500, 20, 20);

                
                clo.btYGridPlus().push();
                clo.btYGridPlus().push();
                clo.btXGridPlus().push();
                clo.btXGridPlus().push();
                clo.btXGridPlus().push();
                clo.btXGridPlus().push();
                clo.btXGridPlus().push();
                clo.btHGridPlus().push();
                clo.btHGridPlus().push();
                clo.btHGridPlus().push();
                clo.btHGridMinus().push();
                clo.btVGridPlus().push();
                clo.btVGridPlus().push();
                clo.btVGridPlus().push();
                clo.btVGridMinus().push();
                clo.btVGridMinus().push();
                clo.btYPaddingPlus().push();
                clo.btYPaddingPlus().push();
                clo.btYPaddingPlus().push();
                clo.btYPaddingPlus().push();
                clo.btYPaddingMinus().push();
                clo.btYPaddingMinus().push();
                clo.btXPaddingPlus().push();
                clo.btXPaddingPlus().push();
                clo.btXPaddingMinus().push();
                clo.btBothPaddingPlus().push();
                clo.btBothPaddingPlus().push();
                clo.btBothPaddingPlus().push();
                clo.btBothPaddingPlus().push();
                clo.btBothPaddingMinus().push();

                clo.btAllInsetsPlus().push();
                clo.btAllInsetsPlus().push();
                clo.btAllInsetsMinus().push();

                clo.btTopAndBottomInsetsPlus().push();
                clo.btTopAndBottomInsetsMinus().push();
                clo.btTopAndBottomInsetsMinus().push();

                clo.btLeftAndRightInsetsPlus().push();
                clo.btLeftAndRightInsetsPlus().push();
                clo.btLeftAndRightInsetsPlus().push();
                clo.btLeftAndRightInsetsMinus().push();

                clo.btBottomInsetsPlus().push();
                clo.btBottomInsetsPlus().push();
                clo.btBottomInsetsPlus().push();
                clo.btBottomInsetsPlus().push();
                clo.btBottomInsetsPlus().push();
                clo.btBottomInsetsMinus().push();
                clo.btBottomInsetsMinus().push();

                clo.btTopInsetsPlus().push();
                clo.btTopInsetsPlus().push();
                clo.btTopInsetsMinus().push();

                clo.btLeftInsetsPlus().push();
                clo.btLeftInsetsMinus().push();

                clo.btRightInsetsPlus().push();
                clo.btRightInsetsPlus().push();
                clo.btRightInsetsPlus().push();
                clo.btRightInsetsPlus().push();

                clo.btRightInsetsMinus().push();
                clo.btRightInsetsMinus().push();
                clo.btRightInsetsMinus().push();


                clo.btYWeightPlus().push();
                clo.btYWeightPlus().push();
                clo.btYWeightPlus().push();
                clo.btYWeightPlus().push();

                clo.btYWeightMinus().push();
                clo.btYWeightMinus().push();

                clo.btXWeightPlus().push();
                clo.btXWeightPlus().push();
                clo.btXWeightPlus().push();
                clo.btXWeightPlus().push();
                clo.btXWeightPlus().push();
                clo.btXWeightPlus().push();

                clo.btXWeightMinus().push();
                clo.btXWeightMinus().push();
                clo.btXWeightMinus().push();

                clo.btClose().push();

                
                ArrayList<String> lines = new ArrayList<String>();

                lines.add("jButton1.setText(\"jButton1\");");
                lines.add("gridBagConstraints = new java.awt.GridBagConstraints();");
                lines.add("gridBagConstraints.gridx = 5;");
                lines.add("gridBagConstraints.gridy = 2;");
                lines.add("gridBagConstraints.gridwidth = 3;");
                lines.add("gridBagConstraints.gridheight = 2;");
                lines.add("gridBagConstraints.ipadx = 4;");
                lines.add("gridBagConstraints.ipady = 5;");
                lines.add("gridBagConstraints.weightx = 0.3;");
                lines.add("gridBagConstraints.weighty = 0.2;");
                lines.add("gridBagConstraints.insets = new java.awt.Insets(1, 3, 3, 4);");
                lines.add("getContentPane().add(jButton1, gridBagConstraints);");
                lines.add("jButton1 = new javax.swing.JButton();");
                lines.add("private javax.swing.JButton jButton1;");


                findInCode(lines, designer);
            }
        });




    }
}
