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
package org.netbeans.qa.form.binding;

import org.netbeans.jellytools.modules.form.ComponentInspectorOperator;
import org.netbeans.jellytools.modules.form.FormDesignerOperator;
import org.netbeans.jellytools.actions.*;
import org.netbeans.qa.form.ExtJellyTestCase;
import org.netbeans.jellytools.nodes.Node;
import junit.framework.Test;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.junit.NbModuleSuite;
import org.netbeans.qa.form.BindDialogOperator;

/**
 * Beans Binding basic test
 *
 * @author Jiri Vagner
 * 
 * <b>Adam Senk</b>
 * 20 April 2011 WORKS
 */
public class SimpleBeansBindingTest extends ExtJellyTestCase {

    /** Constructor required by JUnit */
    public SimpleBeansBindingTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        //TODO "testUpdateMode"
        return NbModuleSuite.create(
                NbModuleSuite.createConfiguration(SimpleBeansBindingTest.class).addTest(
                "testSimpleBeansBinding").gui(true).enableModules(".*").clusters(".*"));

    }

    /** Tests basic beans binding features */
    public void testSimpleBeansBinding() {
        String jLabel1NodePath = "[JFrame]|jLabel1 [JLabel]";  // NOI18N
        String jLabel2NodePath = "[JFrame]|jLabel2 [JLabel]";  // NOI18N
        String actionPath = "Bind|text";  // NOI18N
        String bindSource = "jLabel2";  // NOI18N
        String bindExpression = "${text}";  // NOI18N
        ProjectRootNode prn;
        ProjectsTabOperator pto;


        // create frame
        String frameName = createJFrameFile();


        pto = new ProjectsTabOperator();
        prn = pto.getProjectRootNode("SampleProject");
        prn.select();
        Node formnode = new Node(prn, "Source Packages|" + "data" + "|" + frameName);
        OpenAction openAction = new OpenAction();
        openAction.perform(formnode);
        FormDesignerOperator designer = new FormDesignerOperator(frameName);
        designer.source();
        designer.design();
        ComponentInspectorOperator inspector = new ComponentInspectorOperator();
        inspector.freezeNavigatorAndRun(new Runnable() {

            @Override
            public void run() {
                String jLabel1NodePath = "[JFrame]|jLabel1 [JLabel]";
                String actionPath = "Bind|text";
                // add two labels
                ComponentInspectorOperator inspector = new ComponentInspectorOperator();
                Node actNode = new Node(inspector.treeComponents(), "JFrame"); // NOI18N
                String itemPath = "Add From Palette|Swing Controls|Label"; // NOI18N
                runPopupOverNode(itemPath, actNode);
                runPopupOverNode(itemPath, actNode);

                // invoke bind dialog
                actNode = new Node(inspector.treeComponents(), jLabel1NodePath);
                runNoBlockPopupOverNode(actionPath, actNode);
            }
        });



        // bind jlabel1.text with jlabel2.text
        BindDialogOperator bindOp = new BindDialogOperator();
        bindOp.selectBindSource(bindSource);
        bindOp.setBindExpression(bindExpression);
        bindOp.ok();

        designer.source();
        designer.design();
        inspector = new ComponentInspectorOperator();
        inspector.freezeNavigatorAndRun(new Runnable() {

            @Override
            public void run() {
                String jLabel1NodePath = "[JFrame]|jLabel1 [JLabel]";
                String actionPath = "Bind|text";
                // add two labels
                ComponentInspectorOperator inspector = new ComponentInspectorOperator();

                // invoke bind dialog again ...
                Node actNode = new Node(inspector.treeComponents(), jLabel1NodePath);
                runNoBlockPopupOverNode(actionPath, actNode);
            }
        });

        // ... and check the values in binding dialog
        bindOp = new BindDialogOperator();
        assertEquals(bindOp.getSelectedBindSource(), bindSource);
        assertEquals(bindOp.getBindExpression(), bindExpression);
        bindOp.ok();

        // check generated binding code
        findInCode("createAutoBinding(org.jdesktop.beansbinding.AutoBinding.UpdateStrategy.READ_WRITE, jLabel2, org.jdesktop.beansbinding.ELProperty.create(\"${text}\"), jLabel1, org.jdesktop.beansbinding.BeanProperty.create(\"text\"));", designer);  // NOI18N
        findInCode("bindingGroup.bind();", designer);  // NOI18N

        formnode.select();
        openAction.perform(formnode);
        designer.source();
        designer.design();
        inspector = new ComponentInspectorOperator();
        String jLabel1Text = this.getTextValueOfLabelNonStatic(inspector, jLabel1NodePath);
        designer.source();
        designer.design();
        inspector = new ComponentInspectorOperator();
        String jLabel2Text = this.getTextValueOfLabelNonStatic(inspector, jLabel2NodePath);
        // get values of text properties of jLabels and test them
        assertEquals(jLabel1Text, jLabel2Text);
    }
}