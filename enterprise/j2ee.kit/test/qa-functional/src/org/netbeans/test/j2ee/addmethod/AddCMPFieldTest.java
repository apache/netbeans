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
package org.netbeans.test.j2ee.addmethod;

import java.io.IOException;
import javax.swing.JTextField;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.EditorWindowOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jellytools.nodes.ProjectRootNode;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.test.j2ee.EJBValidation;

/**
 *  Called from EJBValidation test suite.
 * 
 * @author Libor Martinek
 */
public class AddCMPFieldTest extends AddMethodBase {

    protected String methodName;
    protected String returnType;
    private String description;
    private Boolean localGetter;
    private Boolean localSetter;
    private Boolean remoteGetter;
    private Boolean remoteSetter;

    /** Creates a new instance of AddMethodTest */
    public AddCMPFieldTest(String name) {
        super(name);
    }

    public void testAddCMPField1InEB() throws IOException {
        beanName = "TestingEntity";
        editorPopup = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action.Bundle", "LBL_AddCmpFieldAction");
        methodName = "cmpTestField1x";
        description = null;
        returnType = "String";
        isDDModified = true;
        saveFile = true;
        addMethod();
    }

    public void testAddCMPField2InEB() throws IOException {
        beanName = "TestingEntity";
        editorPopup = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action.Bundle", "LBL_AddCmpFieldAction");
        methodName = "cmpTestField2x";
        description = null; //"Test Field";
        returnType = "int";
        localGetter = Boolean.TRUE;
        localSetter = Boolean.FALSE;
        remoteGetter = Boolean.TRUE;
        remoteSetter = Boolean.TRUE;
        isDDModified = true;
        saveFile = true;
        addMethod();
    }

    protected void addMethod() throws IOException {
        EditorOperator editor = EditorWindowOperator.getEditor(beanName + "Bean.java");
        editor.select(11);

        // invoke Add CMP Field dialog
        ProjectsTabOperator prj = new ProjectsTabOperator();
        ProjectRootNode prjnd = prj.getProjectRootNode(EJBValidation.EJB_PROJECT_NAME);
        Node node = new Node(prjnd, "Enterprise Beans|" + beanName);
        node.performPopupActionNoBlock("Add|Add CMP Field...");

        AddCMPFieldDialog dialog = new AddCMPFieldDialog();
        JLabelOperator lblOper = new JLabelOperator(dialog, "Name");
        new JTextFieldOperator((JTextField) lblOper.getLabelFor()).setText(methodName);
        if (description != null) {
            dialog.setDescription(description);
            lblOper = new JLabelOperator(dialog, "Description");
            new JTextFieldOperator((JTextField) lblOper.getLabelFor()).setText(description);
        }

        lblOper = new JLabelOperator(dialog, "Type");
        new JTextFieldOperator((JTextField) lblOper.getLabelFor()).setText(returnType);

        if (localGetter != null) {
            dialog.checkLocalGetter(localGetter);
        }
        if (localSetter != null) {
            dialog.checkLocalSetter(localSetter);
        }
        if (remoteGetter != null) {
            dialog.checkRemoteGetter(remoteGetter);
        }
        if (remoteSetter != null) {
            dialog.checkRemoteSetter(remoteSetter);
        }
        dialog.ok();
        editor.txtEditorPane().waitText(methodName);
        if (saveFile) {
            editor.waitModified(true);
            // need to wait because sometimes is save() called sooner than it can take effect
            new EventTool().waitNoEvent(300);
            editor.save();
        }

        compareFiles();
    }
}
