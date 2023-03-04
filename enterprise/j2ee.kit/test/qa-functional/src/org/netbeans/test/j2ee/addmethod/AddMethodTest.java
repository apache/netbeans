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

import java.awt.event.KeyEvent;
import java.io.IOException;
import javax.swing.JTextField;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jellytools.modules.java.editor.GenerateCodeOperator;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JListOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

/**
 *  Called from EJBValidation test suite.
 *
 * @author Libor Martinek, Jiri Skrivanek
 */
public class AddMethodTest extends AddMethodBase {

    protected String methodName;
    protected String returnType;
    protected String parameters[][];
    protected String exceptions[];
    protected Boolean remote;
    protected Boolean local;

    /** Creates a new instance of AddMethodTest */
    public AddMethodTest(String name) {
        super(name);
    }

    public void testAddBusinessMethod1InSB() throws IOException {
        beanName = "TestingSession";
        dialogTitle = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action.Bundle", "LBL_AddBusinessMethodAction");
        methodName = "testBusinessMethod1";
        returnType = "String";
        parameters = null;
        exceptions = null;
        remote = Boolean.TRUE;
        local = Boolean.TRUE;
        saveFile = true;
        addMethod();
    }

    public void testAddBusinessMethod2InSB() throws IOException {
        beanName = "TestingSession";
        dialogTitle = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action.Bundle", "LBL_AddBusinessMethodAction");
        methodName = "testBusinessMethod2";
        returnType = "String";
        parameters = new String[][]{{"String", "a"}, {"int", "b"}};
        exceptions = new String[]{"Exception"};
        remote = Boolean.TRUE;
        local = Boolean.FALSE;
        saveFile = true;
        addMethod();
    }

    public void testAddBusinessMethod1InEB() throws IOException {
        beanName = "TestingEntity";
        dialogTitle = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action.Bundle", "LBL_AddBusinessMethodAction");
        methodName = "testBusinessMethod1";
        returnType = "String";
        parameters = null;
        exceptions = null;
        remote = Boolean.TRUE;
        local = Boolean.TRUE;
        saveFile = true;
        addMethod();
    }

    public void testAddBusinessMethod2InEB() throws IOException {
        beanName = "TestingEntity";
        dialogTitle = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action.Bundle", "LBL_AddBusinessMethodAction");
        methodName = "testBusinessMethod2";
        returnType = "String";
        parameters = new String[][]{{"String", "a"}, {"boolean", "b"}};
        exceptions = new String[]{"Exception"};
        remote = Boolean.FALSE;
        local = Boolean.TRUE;
        saveFile = true;
        addMethod();
    }

    public void testAddCreateMethod1InEB() throws IOException {
        beanName = "TestingEntity";
        dialogTitle = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action.Bundle", "LBL_AddCreateMethodAction");
        methodName = "createTest1";
        // Create Method has no return type!!!
        returnType = null;
        parameters = null;
        exceptions = null;
        remote = Boolean.FALSE;
        local = Boolean.TRUE;
        toSearchInEditor = "public String ejbCreateTest1() throws CreateException";
        saveFile = true;
        addMethod();
    }

    public void testAddCreateMethod2InEB() throws IOException {
        beanName = "TestingEntity";
        dialogTitle = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action.Bundle", "LBL_AddCreateMethodAction");
        methodName = "createTest2";
        // Create Method has no return type!!!
        returnType = null;
        parameters = new String[][]{{"java.lang.String", "a"}, {"int", "b"}};
        exceptions = new String[]{"IOException"};
        remote = Boolean.TRUE;
        local = Boolean.TRUE;
        toSearchInEditor = "public String ejbCreateTest2(String a, int b) throws CreateException, IOException";
        saveFile = true;
        addMethod();
    }

    public void testAddHomeMethod1InEB() throws IOException {
        beanName = "TestingEntity";
        dialogTitle = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action.Bundle", "LBL_AddHomeMethodAction");
        methodName = "homeTestMethod1";
        returnType = "String";
        parameters = null;
        exceptions = null;
        remote = Boolean.TRUE;
        local = Boolean.TRUE;
        toSearchInEditor = "public String ejbHomeHomeTestMethod1()";
        saveFile = true;
        addMethod();
    }

    public void testAddHomeMethod2InEB() throws IOException {
        beanName = "TestingEntity";
        dialogTitle = Bundle.getStringTrimmed("org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action.Bundle", "LBL_AddHomeMethodAction");
        methodName = "homeTestMethod2";
        returnType = "String";
        parameters = new String[][]{{"java.lang.String", "a"}, {"int", "b"}};
        exceptions = new String[]{"Exception"};
        remote = Boolean.FALSE;
        local = Boolean.TRUE;
        toSearchInEditor = "public String ejbHomeHomeTestMethod2(String a, int b) throws Exception";
        saveFile = true;
        addMethod();
    }

    protected void addMethod() throws IOException {
        EditorOperator editor = new EditorOperator(beanName + "Bean.java");
        editor.select(11);

        // invoke Add Business Method dialog
        // handle that 'EJB Methods' popup is not enabled until scanning is finished
        NbDialogOperator dialog = null;
        try {
            waitScanFinished();
            GenerateCodeOperator.openDialog(dialogTitle, editor);
            dialog = new NbDialogOperator(dialogTitle);
        } catch (TimeoutExpiredException e) {
            // push Escape key to ensure there is no open menu
            MainWindowOperator.getDefault().pushKey(KeyEvent.VK_ESCAPE);
            waitScanFinished();
            GenerateCodeOperator.openDialog(dialogTitle, editor);
            dialog = new NbDialogOperator(dialogTitle);
        }

        JLabelOperator lblOper = new JLabelOperator(dialog, "Name");
        new JTextFieldOperator((JTextField) lblOper.getLabelFor()).setText(methodName);

        if (returnType != null) {
            JLabelOperator lblOperForReturnType = new JLabelOperator(dialog, "Return Type:");
            new JTextFieldOperator((JTextField) lblOperForReturnType.getLabelFor()).setText(returnType);
        }
        fillParameters(dialog);
        fillExceptions(dialog);
        setRemoteLocalCheckBox(dialog);
        dialog.ok();
        if (toSearchInEditor == null) {
            toSearchInEditor = computeSeachString();
        }
        editor.txtEditorPane().waitText(toSearchInEditor);
        if (saveFile) {
            editor.waitModified(true);
            // need to wait because sometimes is save() called sooner than it can take effect
            new EventTool().waitNoEvent(300);
            editor.save();
        }

        compareFiles();
    }

    private String computeSeachString() {
        StringBuilder text = new StringBuilder();
        text.append("public ");
        if (returnType == null) {
            text.append("void");
        } else {
            text.append(returnType);
        }
        text.append(" ");
        text.append(methodName);
        text.append("(");
        if (parameters != null) {
            for (int i = 0; i < parameters.length; i++) {
                if (i > 0) {
                    text.append(", ");
                }
                text.append(parameters[i][0]);
                text.append(" ");
                text.append(parameters[i][1]);
            }
        }
        text.append(")");
        return text.toString();
    }

    protected void fillParameters(NbDialogOperator dialog) {
        if (parameters != null) {
            new JTabbedPaneOperator(dialog).selectPage("Parameters");
            JTableOperator operator = new JTableOperator(dialog);

            for (int i = 0; i < parameters.length; i++) {
                new JButtonOperator(dialog, "Add").push();
                int rowCount = operator.getRowCount();
                // use setValueAt for combo box because changeCellObject may accidentally close dialog
                operator.setValueAt(parameters[i][0], rowCount - 1, 1);
                // use changeCellObject for text field to confirm changes
                operator.changeCellObject(rowCount - 1, 0, parameters[i][1]);
            }
        }
    }

    protected void fillExceptions(NbDialogOperator dialog) {
        if (exceptions != null) {
            new JTabbedPaneOperator(dialog).selectPage("Exceptions");
            for (int i = 0; i < exceptions.length; i++) {
                new JButtonOperator(dialog, "Add").pushNoBlock();
                NbDialogOperator findTypeOper = new NbDialogOperator("Find Type");
                new JTextFieldOperator(findTypeOper).setText(exceptions[i]);
                // wait for list populated
                JListOperator typesListOper = new JListOperator(findTypeOper);
                if (exceptions[i].equals("Exception")) {
                    // need to select correct item between other matches
                    typesListOper.selectItem("Exception (java.lang)");
                } else {
                    typesListOper.selectItem(exceptions[i]);
                }
                findTypeOper.ok();
            }
        }
    }

    protected void setRemoteLocalCheckBox(NbDialogOperator dialog) {
        if (remote != null && remote && (local == null || !local)) {
            new JRadioButtonOperator(dialog, "Remote").setSelected(remote);
        }
        if (local != null && local && (remote == null || !remote)) {
            new JRadioButtonOperator(dialog, "Local").setSelected(local);
        }
        if (local != null && local && remote != null && remote) {
            new JRadioButtonOperator(dialog, "Both").setSelected(local);
        }
    }
}
