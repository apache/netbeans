/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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