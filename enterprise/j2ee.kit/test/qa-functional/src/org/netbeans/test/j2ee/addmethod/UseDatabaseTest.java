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
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.ProjectsTabOperator;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.modules.java.editor.GenerateCodeOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.test.j2ee.EJBValidation;

/**
 *  Called from EJBValidation test suite.
 * 
 * @author Libor Martinek
 */
public class UseDatabaseTest extends AddMethodBase {

    private String name;

    /** Creates a new instance of AddMethodTest */
    public UseDatabaseTest(String name) {
        super(name);
    }

    public void testUseDatabase1InSB() throws IOException {
        beanName = "TestingSession";
        editorPopup = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.ejbcore.ui.logicalview.entries.Bundle", "LBL_UseDbAction");
        // "Choose Database"
        dialogTitle = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.ejbcore.ui.logicalview.entries.Bundle", "LBL_ChooseDatabase");
        name = "myTestingDatabase";
        toSearchInEditor = "getMyTestingDatabase";
        isDDModified = true;
        saveFile = true;
        addMethod();
    }

    protected void addMethod() throws IOException {
        EditorOperator editor = EditorWindowOperator.getEditor(beanName + "Bean.java");
        editor.select("private javax.ejb.SessionContext context;");

        GenerateCodeOperator.openDialog(editorPopup, editor);
        NbDialogOperator chooseDatabaseOper = new NbDialogOperator(dialogTitle);
        new JButtonOperator(chooseDatabaseOper, "Add...").pushNoBlock();
        NbDialogOperator addReferenceOper = new NbDialogOperator("Add Data Source Reference");
        new JTextFieldOperator((JTextField) new JLabelOperator(addReferenceOper, "Reference Name:").getLabelFor()).typeText(name);
        new JButtonOperator(addReferenceOper, "Add...").pushNoBlock();
        NbDialogOperator createDataSourceOper = new NbDialogOperator("Create Data Source");
        new JTextFieldOperator((JTextField) new JLabelOperator(createDataSourceOper, "JNDI Name:").getLabelFor()).typeText(name);
        new JComboBoxOperator(createDataSourceOper).selectItem("/sample");
        createDataSourceOper.ok();
        addReferenceOper.ok();
        chooseDatabaseOper.ok();
        editor.txtEditorPane().waitText(toSearchInEditor);
        if (saveFile) {
            editor.save();
        }
        compareFiles();
    }
}
