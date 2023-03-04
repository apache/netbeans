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
import javax.swing.JTextArea;
import javax.swing.JTextField;
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.actions.OpenAction;
import org.netbeans.jellytools.modules.java.editor.GenerateCodeOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTextAreaOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.test.j2ee.EJBValidation;

/**
 *  Called from EJBValidation test suite.
 * 
 * @author Libor Martinek
 */
public class AddFinderMethodTest extends AddMethodTest {

    private boolean returnManyCardinality = true;
    protected String ejbql = null;
    private String toSearchFile;

    /** Creates a new instance of AddMethodTest */
    public AddFinderMethodTest(String name) {
        super(name);
    }

    public void testAddFinderMethod1InEB() throws IOException {
        beanName = "TestingEntity";
        dialogTitle = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action.Bundle", "LBL_AddFinderMethodAction");
        methodName = "findByTest1";
        returnManyCardinality = true;
        parameters = null;
        remote = Boolean.FALSE;
        local = Boolean.TRUE;
        ejbql = null;
        toSearchFile = beanName + "LocalHome.java";
        isDDModified = true;
        saveFile = true;
        addMethod();
    }

    public void testAddFinderMethod2InEB() throws IOException {
        beanName = "TestingEntity";
        dialogTitle = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action.Bundle", "LBL_AddFinderMethodAction");
        methodName = "findByTest3";
        returnManyCardinality = false;
        parameters = new String[][]{{"java.lang.String", "a"}};
        remote = Boolean.TRUE;
        local = Boolean.TRUE;
        ejbql = "SELECT OBJECT(o)\nFROM TestingEntity o\nWHERE o.key = ?1";
        toSearchFile = beanName + "LocalHome.java";
        isDDModified = true;
        saveFile = true;
        addMethod();
    }

    @Override
    protected void addMethod() throws IOException {
        EditorOperator editor = EditorWindowOperator.getEditor(beanName + "Bean.java");
        editor.select(11);

        // invoke Add Business Method dialog
        GenerateCodeOperator.openDialog(dialogTitle, editor);
        NbDialogOperator dialog = new NbDialogOperator(dialogTitle);
        JLabelOperator lblOper = new JLabelOperator(dialog, "Name");
        new JTextFieldOperator((JTextField) lblOper.getLabelFor()).setText(methodName);
        if (returnManyCardinality) {
            new JRadioButtonOperator(dialog, "Many").setSelected(true);
        } else {
            new JRadioButtonOperator(dialog, "One").setSelected(true);
        }
        fillParameters(dialog);
        setRemoteLocalCheckBox(dialog);
        if (ejbql != null) {
            lblOper = new JLabelOperator(dialog, "EJB QL:");
            new JTextAreaOperator((JTextArea)lblOper.getLabelFor()).setText(ejbql);
            //new JTextAreaOperator(dialog).setText(ejbql);
        }
        dialog.ok();
        if (toSearchFile != null) {
            Node openFile2 = new Node(new ProjectsTabOperator().getProjectRootNode(EJBValidation.EJB_PROJECT_NAME),
                    "Source Packages|test|" + toSearchFile);
            new OpenAction().performAPI(openFile2);
            final EditorOperator editor2 = EditorWindowOperator.getEditor(toSearchFile);
            editor2.txtEditorPane().waitText(methodName);
            // need to wait because sometimes is save() called sooner than it can take effect
            new EventTool().waitNoEvent(300);
            editor2.save();
            editor2.closeDiscard();
        }
        if (saveFile) {
            editor.save();
        }
        compareFiles();
    }
}
