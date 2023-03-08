/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.lib.editor.codetemplates.storage.ui;

import java.awt.event.KeyEvent;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import junit.framework.Test;
import org.netbeans.jellytools.JellyTestCase;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.OptionsOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JEditorPaneOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTabbedPaneOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

/**
 *
 * @author: Arthur Sadykov
 */
public class CodeTemplatesPanelTest extends JellyTestCase {

    private JEditorPaneOperator editorPaneOperator;
    private JButtonOperator editParametersButtonOperator;
    private OptionsOperator optionsOperator;
    private JButtonOperator okButtonOperator;
    private NbDialogOperator dialogOperator;

    public CodeTemplatesPanelTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return createModuleTest(CodeTemplatesPanelTest.class);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        optionsOperator = OptionsOperator.invoke();
        optionsOperator.selectEditor();
        JTabbedPaneOperator tabbedPaneOperator = new JTabbedPaneOperator(optionsOperator);
        tabbedPaneOperator.selectPage("Code Templates");
        JLabelOperator languageLabelOperator = new JLabelOperator(tabbedPaneOperator, "Language");
        JComboBoxOperator languageComboBoxOperator =
                new JComboBoxOperator((JComboBox) languageLabelOperator.getLabelFor());
        languageComboBoxOperator.selectItem("Java");
        JButtonOperator newButtonOperator = new JButtonOperator(tabbedPaneOperator, "New");
        newButtonOperator.push();
        NbDialogOperator newCodeTemplateDialogOperator = new NbDialogOperator("New Code Template");
        JLabelOperator abbreviationLabelOperator = new JLabelOperator(newCodeTemplateDialogOperator, "Abbreviation:");
        JTextFieldOperator abbreviationTextFieldOperator =
                new JTextFieldOperator(((JTextField) abbreviationLabelOperator.getLabelFor()));
        abbreviationTextFieldOperator.setText("test_test_test");
        newCodeTemplateDialogOperator.ok();
        editorPaneOperator = new JEditorPaneOperator(tabbedPaneOperator);
        editParametersButtonOperator = new JButtonOperator(tabbedPaneOperator, "Edit parameters...");
    }

    public void testWhenThereIsNoTextInExpandedTextPaneThenEditParametersButtonMustBeDisabled() {
        editorPaneOperator.setText("");
        assertEditParametersButtonIsDisabled();
    }

    public void testWhenThereIsOnlyOneParameterWithoutNameInExpandedTextPaneThenEditParametersButtonMustBeEnabled() {
        editorPaneOperator.typeText("$");
        assertEditParametersButtonIsDisabled();
        editorPaneOperator.typeText("{");
        assertEditParametersButtonIsDisabled();
        editorPaneOperator.typeText("}");
        assertEditParametersButtonIsEnabled();
    }

    public void testWhenThereIsAtLeastOneMasterParameterInExpandedTextPaneThenEditParametersButtonMustBeEnabled() {
        editorPaneOperator.setText("${T type=\"java.io.File\" editable=false} ${name newVarName} = new ${File}(\"\");");
        assertEditParametersButtonIsEnabled();
    }

    public void testWhenThereIsNoMasterParametersInExpandedTextPaneThenEditParametersButtonMustBeDisabled() {
        editorPaneOperator.setText("value");
        assertEditParametersButtonIsDisabled();
    }

    public void testWhenThereIsOnlyCursorParameterInExpandedTextPaneThenEditParametersButtonMustBeDisabled() {
        editorPaneOperator.setText("${cursor}");
        assertEditParametersButtonIsDisabled();
    }

    public void testWhenThereIsOnlySelectionParameterInExpandedTextPaneThenEditParametersButtonMustBeDisabled() {
        editorPaneOperator.setText("${selection}");
        assertEditParametersButtonIsDisabled();
    }

    public void testWhenThereIsOnlyNoFormatParameterInExpandedTextPaneThenEditParametersButtonMustBeDisabled() {
        editorPaneOperator.setText("${no-format}");
        assertEditParametersButtonIsDisabled();
    }

    public void testWhenThereIsOnlyNoIndentParameterInExpandedTextPaneThenEditParametersButtonMustBeDisabled() {
        editorPaneOperator.setText("${no-indent}");
        assertEditParametersButtonIsDisabled();
    }

    public void testWhenTableContainsOnlyOneParameterWithoutHintThenOkButtonMustBeEnabled() {
        editorPaneOperator.setText("${param}");
        assertOkButtonIsEnabled();
    }

    public void testWhenAllHintsInTableAreValidThenOkButtonMustBeEnabled() {
        editorPaneOperator.setText(
                "${param array editable=false} "
                + "${param cast editable=false} "
                + "${param completionInvoke editable=false} "
                + "${param currClassFQName editable=false} "
                + "${param currClassName editable=false} "
                + "${param currMethodName editable=false} "
                + "${param currPackageName editable=false} "
                + "${param instanceOf=\"java.lang.Object\" editable=false} "
                + "${param iterable editable=false} "
                + "${param iterableElementType editable=false} "
                + "${param leftSideType editable=false} "
                + "${param named editable=false} "
                + "${param newVarName editable=false} "
                + "${param rightSideType editable=false} "
                + "${param staticImport=\"org.junit.Assert.assertEquals\" editable=false} "
                + "${param type=\"java.util.regex.Pattern\" editable=false} "
                + "${param typeVar editable=false} "
                + "${param uncaughtExceptionCatchStatements editable=false} "
                + "${param uncaughtExceptionType editable=false} "
        );
        assertOkButtonIsEnabled();
    }

    public void testTableMustBeFilledInCorrectly() {
        editorPaneOperator.setText("${Map type=\"java.util.Map\" default=\"Map\" ordering=3 editable=false}"
                + "<${String ordering=1 completionInvoke default=\"String\"}, "
                + "${Object completionInvoke ordering=2 default=\"Object\"}> "
                + "${name ordering=4 newVarName} = "
                + "new ${HashMap type=\"java.util.HashMap\" default=\"HashMap\" ordering=0 editable=false}<>();");
        editParametersButtonOperator.push();
        dialogOperator = new NbDialogOperator("Edit Template Parameters");
        okButtonOperator = new JButtonOperator(dialogOperator, "OK");
        JTableOperator tableOperator = new JTableOperator(dialogOperator);
        String r0_c0 = (String) tableOperator.getValueAt(0, 0);
        String r1_c0 = (String) tableOperator.getValueAt(1, 0);
        String r2_c0 = (String) tableOperator.getValueAt(2, 0);
        String r3_c0 = (String) tableOperator.getValueAt(3, 0);
        String r4_c0 = (String) tableOperator.getValueAt(4, 0);
        String r0_c1 = (String) tableOperator.getValueAt(0, 1);
        String r1_c1 = (String) tableOperator.getValueAt(1, 1);
        String r2_c1 = (String) tableOperator.getValueAt(2, 1);
        String r3_c1 = (String) tableOperator.getValueAt(3, 1);
        String r4_c1 = (String) tableOperator.getValueAt(4, 1);
        String r0_c2 = (String) tableOperator.getValueAt(0, 2);
        String r1_c2 = (String) tableOperator.getValueAt(1, 2);
        String r2_c2 = (String) tableOperator.getValueAt(2, 2);
        String r3_c2 = (String) tableOperator.getValueAt(3, 2);
        String r4_c2 = (String) tableOperator.getValueAt(4, 2);
        int r0_c3 = (int) tableOperator.getValueAt(0, 3);
        int r1_c3 = (int) tableOperator.getValueAt(1, 3);
        int r2_c3 = (int) tableOperator.getValueAt(2, 3);
        int r3_c3 = (int) tableOperator.getValueAt(3, 3);
        int r4_c3 = (int) tableOperator.getValueAt(4, 3);
        boolean r0_c4 = (boolean) tableOperator.getValueAt(0, 4);
        boolean r1_c4 = (boolean) tableOperator.getValueAt(1, 4);
        boolean r2_c4 = (boolean) tableOperator.getValueAt(2, 4);
        boolean r3_c4 = (boolean) tableOperator.getValueAt(3, 4);
        boolean r4_c4 = (boolean) tableOperator.getValueAt(4, 4);
        boolean r0_c5 = (boolean) tableOperator.getValueAt(0, 5);
        boolean r1_c5 = (boolean) tableOperator.getValueAt(1, 5);
        boolean r2_c5 = (boolean) tableOperator.getValueAt(2, 5);
        boolean r3_c5 = (boolean) tableOperator.getValueAt(3, 5);
        boolean r4_c5 = (boolean) tableOperator.getValueAt(4, 5);
        dialogOperator.cancel();
        assertEquals("Map", r0_c0);
        assertEquals("String", r1_c0);
        assertEquals("Object", r2_c0);
        assertEquals("name", r3_c0);
        assertEquals("HashMap", r4_c0);
        assertEquals("type=\"java.util.Map\"", r0_c1);
        assertEquals("", r1_c1);
        assertEquals("", r2_c1);
        assertEquals("newVarName", r3_c1);
        assertEquals("type=\"java.util.HashMap\"", r4_c1);
        assertEquals("Map", r0_c2);
        assertEquals("String", r1_c2);
        assertEquals("Object", r2_c2);
        assertEquals("", r3_c2);
        assertEquals("HashMap", r4_c2);
        assertEquals(3, r0_c3);
        assertEquals(1, r1_c3);
        assertEquals(2, r2_c3);
        assertEquals(4, r3_c3);
        assertEquals(0, r4_c3);
        assertEquals(false, r0_c4);
        assertEquals(true, r1_c4);
        assertEquals(true, r2_c4);
        assertEquals(false, r3_c4);
        assertEquals(false, r4_c4);
        assertEquals(false, r0_c5);
        assertEquals(true, r1_c5);
        assertEquals(true, r2_c5);
        assertEquals(true, r3_c5);
        assertEquals(false, r4_c5);
    }

    public void testWhenExpandedTextPaneContainsUnsupportedHintsThenIgnoreThem() {
        editorPaneOperator.setText("${param1 unsupportedHint} ${name newVarName} = "
                + "${param2 instanceof=\"java.lang.String\"}.create();");
        editParametersButtonOperator.push();
        dialogOperator = new NbDialogOperator("Edit Template Parameters");
        okButtonOperator = new JButtonOperator(dialogOperator, "OK");
        JTableOperator tableOperator = new JTableOperator(dialogOperator);
        int rowCount = tableOperator.getRowCount();
        dialogOperator.cancel();
        assertEquals(2, rowCount);
    }

    public void testWhenEditParametersDialogContainsInvalidMainHintsThenOkButtonMustBeDisabled() {
        editorPaneOperator.setText("${param}");
        editParametersButtonOperator.push();
        dialogOperator = new NbDialogOperator("Edit Template Parameters");
        okButtonOperator = new JButtonOperator(dialogOperator, "OK");
        JTableOperator tableOperator = new JTableOperator(dialogOperator);
        tableOperator.clickForEdit(0, 1);
        tableOperator.typeKey('t');
        tableOperator.pushKey(KeyEvent.VK_ENTER);
        assertOkButtonIsDisabled();
    }

    public void testWhenEditParametersDialogContainsInvalidOrderingHintsThenOkButtonMustBeDisabled() {
        editorPaneOperator.setText("${param}");
        editParametersButtonOperator.push();
        dialogOperator = new NbDialogOperator("Edit Template Parameters");
        okButtonOperator = new JButtonOperator(dialogOperator, "OK");
        JTableOperator tableOperator = new JTableOperator(dialogOperator);
        tableOperator.clickForEdit(0, 3);
        tableOperator.typeKey('t');
        tableOperator.pushKey(KeyEvent.VK_ENTER);
        assertOkButtonIsDisabled();
    }

    public void testInterpolationsWithMultiple$ShouldBeHandledCorrectly() {
        editorPaneOperator.setText("$$${param3} Integer $${param2} Float ${param1} $$$${param4}");
        editParametersButtonOperator.push();
        dialogOperator = new NbDialogOperator("Edit Template Parameters");
        dialogOperator.ok();
        assertEquals("$$${param3} Integer $${param2} Float ${param1} $$$${param4}", editorPaneOperator.getText());
    }

    private void assertEditParametersButtonIsEnabled() {
        assertTrue("The 'Edit parameters' button must be enabled", editParametersButtonOperator.isEnabled());
    }

    private void assertEditParametersButtonIsDisabled() {
        assertFalse("The 'Edit parameters' button must be disabled", editParametersButtonOperator.isEnabled());
    }

    private void assertOkButtonIsEnabled() {
        editParametersButtonOperator.push();
        dialogOperator = new NbDialogOperator("Edit Template Parameters");
        okButtonOperator = new JButtonOperator(dialogOperator, "OK");
        boolean enabled = okButtonOperator.isEnabled();
        dialogOperator.cancel();
        assertTrue("The 'OK' button must be enabled", enabled);
    }

    private void assertOkButtonIsDisabled() {
        boolean enabled = okButtonOperator.isEnabled();
        dialogOperator.cancel();
        assertFalse("The 'OK' button must be disabled", enabled);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        optionsOperator.cancel();
    }
}
