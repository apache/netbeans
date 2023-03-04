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
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.EditorWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTextAreaOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jellytools.modules.java.editor.GenerateCodeOperator;
import org.netbeans.jemmy.EventTool;

/**
 *  Called from EJBValidation test suite.
 * 
 * @author Libor Martinek
 */
public class AddSelectMethodTest extends AddMethodTest {

    protected String ejbql = null;
    private String toSearchFile;

    /** Creates a new instance of AddMethodTest */
    public AddSelectMethodTest(String name) {
        super(name);
    }

    public void testAddSelectMethod1InEB() throws IOException {
        beanName = "TestingEntity";
        dialogTitle = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action.Bundle", "LBL_AddSelectMethodAction");
        methodName = "ejbSelectByTest1";
        returnType = "int";
        parameters = null;
        ejbql = null;
        toSearchFile = methodName;
        isDDModified = true;
        saveFile = true;
        addMethod();
    }

    public void testAddSelectMethod2InEB() throws IOException {
        beanName = "TestingEntity";
        dialogTitle = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action.Bundle", "LBL_AddSelectMethodAction");
        methodName = "ejbSelectByTest2";
        returnType = "int";
        parameters = new String[][]{{"java.lang.String", "a"}};
        ejbql = null;
        toSearchFile = methodName;
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
        if (returnType != null) {
            lblOper = new JLabelOperator(dialog, "Return Type:");
            new JTextFieldOperator((JTextField) lblOper.getLabelFor()).setText(returnType);
        }
        fillParameters(dialog);
        if (ejbql != null) {
            lblOper = new JLabelOperator(dialog, "EJB QL:");
            new JTextAreaOperator((JTextArea) lblOper.getLabelFor()).setText(ejbql);
            //new JTextAreaOperator(dialog).setText(ejbql);
        }
        dialog.ok();
        if (toSearchFile != null) {
            editor.txtEditorPane().waitText(toSearchInEditor);
        }
        if (saveFile) {
            editor.waitModified(true);
            // need to wait because sometimes is save() called sooner than it can take effect
            new EventTool().waitNoEvent(300);
            editor.save();
        }
        compareFiles();
    }
}
