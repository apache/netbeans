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
package org.netbeans.qa.form.jda;

import org.netbeans.jemmy.operators.*;

/** Class implementing all necessary methods for handling "Create New Action" NbDialog.
 *
 * @author Jiri Vagner
 */
public class CreateNewActionOperator extends JDialogOperator {

    /** Creates new CreateNewAction that can handle it.
     */
    public CreateNewActionOperator() {
        super("Create New Action"); // NOI18N
    }

    private JLabelOperator _lblBackgroundTask;
    private JLabelOperator _lblAttributes;
    private JLabelOperator _lblMethod;
    private JLabelOperator _lblClass;
    private JTabbedPaneOperator _tbpAttributes;
    private String _selectPageBasic = "Basic";
    private JLabelOperator _lblText;
    private JLabelOperator _lblToolTip;
    private JLabelOperator _lblAccelerator;
    private JCheckBoxOperator _cbCtrl;
    private JCheckBoxOperator _cbAlt;
    private JLabelOperator _lblLetter;
    private JCheckBoxOperator _cbMetaMacOnly;
    private JCheckBoxOperator _cbShift;
    private JTextFieldOperator _txtJTextField;
    private JButtonOperator _btClear;
    private JTextFieldOperator _txtJTextField2;
    private JTextFieldOperator _txtJTextField3;
    private JLabelOperator _lblSmallIcon;
    private JButtonOperator _btIconButton;
    private JLabelOperator _lblLargeIcon;
    private JButtonOperator _btIconButton2;
    private JButtonOperator _btSetIcon;
    private JButtonOperator _btSetIcon2;
    private String _selectPageAdvanced = "Advanced"; // NOI18N
    private JLabelOperator _lblEnabledProperty;
    private JComboBoxOperator _cboJComboBox;
    private JTextFieldOperator _txtJTextField4;
    private JComboBoxOperator _cboJComboBox2;
    private JTextFieldOperator _txtJTextField5;
    private JLabelOperator _lblSelectedProperty;
    private JLabelOperator _lblBlockingDialogTitle;
    private JLabelOperator _lblBlockingDialogText;
    private JLabelOperator _lblBlockingType;
    private JComboBoxOperator _cboBlockingType;
    public static final String ITEM_NONE = "NONE"; // NOI18N
    public static final String ITEM_ACTION = "ACTION"; // NOI18N
    public static final String ITEM_COMPONENT = "COMPONENT"; // NOI18N
    public static final String ITEM_WINDOW = "WINDOW"; // NOI18N
    public static final String ITEM_APPLICATION = "APPLICATION"; // NOI18N
    private JTextFieldOperator _txtJTextField6;
    private JTextFieldOperator _txtJTextField7;
    private JCheckBoxOperator _cbJCheckBox;
    private JTextFieldOperator _txtJTextField8;
    private JButtonOperator _btChooseClass;
    private JComboBoxOperator _cboActionToEdit;
    public static final String ITEM_CREATENEWACTION = "Create New Action ..."; // NOI18N
    private JButtonOperator _btOK;
    private JButtonOperator _btCancel;


    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find "Background Task:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblBackgroundTask() {
        if (_lblBackgroundTask==null) {
            _lblBackgroundTask = new JLabelOperator(this, "Background Task:"); // NOI18N
        }
        return _lblBackgroundTask;
    }

    /** Tries to find "Attributes:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblAttributes() {
        if (_lblAttributes==null) {
            _lblAttributes = new JLabelOperator(this, "Attributes:"); // NOI18N
        }
        return _lblAttributes;
    }

    /** Tries to find "Method:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblMethod() {
        if (_lblMethod==null) {
            _lblMethod = new JLabelOperator(this, "Method:"); // NOI18N
        }
        return _lblMethod;
    }

    /** Tries to find "Class:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblClass() {
        if (_lblClass==null) {
            _lblClass = new JLabelOperator(this, "Class:"); // NOI18N
        }
        return _lblClass;
    }

    /** Tries to find null JTabbedPane in this dialog.
     * @return JTabbedPaneOperator
     */
    public JTabbedPaneOperator tbpAttributes() {
        if (_tbpAttributes==null) {
            _tbpAttributes = new JTabbedPaneOperator(this);
        }
        return _tbpAttributes;
    }

    /** Tries to find "Text:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblText() {
        if (_lblText==null) {
            _lblText = new JLabelOperator(selectPageBasic(), "Text:"); // NOI18N
        }
        selectPageBasic();
        return _lblText;
    }

    /** Tries to find "Tool Tip:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblToolTip() {
        if (_lblToolTip==null) {
            _lblToolTip = new JLabelOperator(selectPageBasic(), "Tool Tip:"); // NOI18N
        }
        selectPageBasic();
        return _lblToolTip;
    }

    /** Tries to find "Accelerator:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblAccelerator() {
        if (_lblAccelerator==null) {
            _lblAccelerator = new JLabelOperator(selectPageBasic(), "Accelerator:"); // NOI18N
        }
        selectPageBasic();
        return _lblAccelerator;
    }

    /** Tries to find "Ctrl" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbCtrl() {
        if (_cbCtrl==null) {
            _cbCtrl = new JCheckBoxOperator(selectPageBasic(), "Ctrl"); // NOI18N
        }
        selectPageBasic();
        return _cbCtrl;
    }

    /** Tries to find "Alt" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbAlt() {
        if (_cbAlt==null) {
            _cbAlt = new JCheckBoxOperator(selectPageBasic(), "Alt"); // NOI18N
        }
        selectPageBasic();
        return _cbAlt;
    }

    /** Tries to find "Letter:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblLetter() {
        if (_lblLetter==null) {
            _lblLetter = new JLabelOperator(selectPageBasic(), "Letter:"); // NOI18N
        }
        selectPageBasic();
        return _lblLetter;
    }

    /** Tries to find "Meta (Mac only)" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbMetaMacOnly() {
        if (_cbMetaMacOnly==null) {
            _cbMetaMacOnly = new JCheckBoxOperator(selectPageBasic(), "Meta (Mac only)"); // NOI18N
        }
        selectPageBasic();
        return _cbMetaMacOnly;
    }

    /** Tries to find "Shift" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbShift() {
        if (_cbShift==null) {
            _cbShift = new JCheckBoxOperator(selectPageBasic(), "Shift"); // NOI18N
        }
        selectPageBasic();
        return _cbShift;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtJTextField() {
        if (_txtJTextField==null) {
            _txtJTextField = new JTextFieldOperator(selectPageBasic());
        }
        selectPageBasic();
        return _txtJTextField;
    }

    /** Tries to find "Clear" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btClear() {
        if (_btClear==null) {
            _btClear = new JButtonOperator(selectPageBasic(), "Clear"); // NOI18N
        }
        selectPageBasic();
        return _btClear;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtJTextField2() {
        if (_txtJTextField2==null) {
            _txtJTextField2 = new JTextFieldOperator(selectPageBasic(), 1);
        }
        selectPageBasic();
        return _txtJTextField2;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtJTextField3() {
        if (_txtJTextField3==null) {
            _txtJTextField3 = new JTextFieldOperator(selectPageBasic(), 2);
        }
        selectPageBasic();
        return _txtJTextField3;
    }

    /** Tries to find "Small Icon:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblSmallIcon() {
        if (_lblSmallIcon==null) {
            _lblSmallIcon = new JLabelOperator(selectPageBasic(), "Small Icon:"); // NOI18N
        }
        selectPageBasic();
        return _lblSmallIcon;
    }

    /** Tries to find null IconButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btIconButton() {
        if (_btIconButton==null) {
            _btIconButton = new JButtonOperator(selectPageBasic(), 1);
        }
        selectPageBasic();
        return _btIconButton;
    }

    /** Tries to find "Large Icon:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblLargeIcon() {
        if (_lblLargeIcon==null) {
            _lblLargeIcon = new JLabelOperator(selectPageBasic(), "Large Icon:"); // NOI18N
        }
        selectPageBasic();
        return _lblLargeIcon;
    }

    /** Tries to find null IconButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btIconButton2() {
        if (_btIconButton2==null) {
            _btIconButton2 = new JButtonOperator(selectPageBasic(), 2);
        }
        selectPageBasic();
        return _btIconButton2;
    }

    /** Tries to find "Set Icon..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btSetIcon() {
        if (_btSetIcon==null) {
            _btSetIcon = new JButtonOperator(selectPageBasic(), "Set Icon..."); // NOI18N
        }
        selectPageBasic();
        return _btSetIcon;
    }

    /** Tries to find "Set Icon..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btSetIcon2() {
        if (_btSetIcon2==null) {
            _btSetIcon2 = new JButtonOperator(selectPageBasic(), "Set Icon...", 1); // NOI18N
        }
        selectPageBasic();
        return _btSetIcon2;
    }

    /** Tries to find "Enabled Property:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblEnabledProperty() {
        if (_lblEnabledProperty==null) {
            _lblEnabledProperty = new JLabelOperator(selectPageAdvanced(), "Enabled Property:"); // NOI18N
        }
        selectPageAdvanced();
        return _lblEnabledProperty;
    }

    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboJComboBox() {
        if (_cboJComboBox==null) {
            _cboJComboBox = new JComboBoxOperator(selectPageAdvanced());
        }
        selectPageAdvanced();
        return _cboJComboBox;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtJTextField4() {
        if (_txtJTextField4==null) {
            _txtJTextField4 = new JTextFieldOperator(selectPageAdvanced());
        }
        selectPageAdvanced();
        return _txtJTextField4;
    }

    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboJComboBox2() {
        if (_cboJComboBox2==null) {
            _cboJComboBox2 = new JComboBoxOperator(selectPageAdvanced(), 1);
        }
        selectPageAdvanced();
        return _cboJComboBox2;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtJTextField5() {
        if (_txtJTextField5==null) {
            _txtJTextField5 = new JTextFieldOperator(selectPageAdvanced(), 1);
        }
        selectPageAdvanced();
        return _txtJTextField5;
    }

    /** Tries to find "Selected Property:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblSelectedProperty() {
        if (_lblSelectedProperty==null) {
            _lblSelectedProperty = new JLabelOperator(selectPageAdvanced(), "Selected Property:"); // NOI18N
        }
        selectPageAdvanced();
        return _lblSelectedProperty;
    }

    /** Tries to find "Blocking Dialog Title:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblBlockingDialogTitle() {
        if (_lblBlockingDialogTitle==null) {
            _lblBlockingDialogTitle = new JLabelOperator(selectPageAdvanced(), "Blocking Dialog Title:"); // NOI18N
        }
        selectPageAdvanced();
        return _lblBlockingDialogTitle;
    }

    /** Tries to find "Blocking Dialog Text:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblBlockingDialogText() {
        if (_lblBlockingDialogText==null) {
            _lblBlockingDialogText = new JLabelOperator(selectPageAdvanced(), "Blocking Dialog Text:"); // NOI18N
        }
        selectPageAdvanced();
        return _lblBlockingDialogText;
    }

    /** Tries to find "Blocking Type:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblBlockingType() {
        if (_lblBlockingType==null) {
            _lblBlockingType = new JLabelOperator(selectPageAdvanced(), "Blocking Type:"); // NOI18N
        }
        selectPageAdvanced();
        return _lblBlockingType;
    }

    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboBlockingType() {
        if (_cboBlockingType==null) {
            _cboBlockingType = new JComboBoxOperator(selectPageAdvanced(), 2);
        }
        selectPageAdvanced();
        return _cboBlockingType;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtJTextField6() {
        if (_txtJTextField6==null) {
            _txtJTextField6 = new JTextFieldOperator(selectPageAdvanced(), 2);
        }
        selectPageAdvanced();
        return _txtJTextField6;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtJTextField7() {
        if (_txtJTextField7==null) {
            _txtJTextField7 = new JTextFieldOperator(selectPageAdvanced(), 3);
        }
        selectPageAdvanced();
        return _txtJTextField7;
    }

    /** Tries to find null JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbJCheckBox() {
        if (_cbJCheckBox==null) {
            _cbJCheckBox = new JCheckBoxOperator(this);
        }
        return _cbJCheckBox;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtJTextField8() {
        if (_txtJTextField8==null) {
            _txtJTextField8 = new JTextFieldOperator(this, 3);
        }
        return _txtJTextField8;
    }

    /** Tries to find "Choose Class" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btChooseClass() {
        if (_btChooseClass==null) {
            _btChooseClass = new JButtonOperator(this, "Choose Class");
        }
        return _btChooseClass;
    }

    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboActionToEdit() {
        if (_cboActionToEdit==null) {
            _cboActionToEdit = new JComboBoxOperator(this, 3);
        }
        return _cboActionToEdit;
    }

    /** Tries to find "OK" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btOK() {
        if (_btOK==null) {
            _btOK = new JButtonOperator(this, "OK"); // NOI18N
        }
        return _btOK;
    }

    /** Tries to find "Cancel" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btCancel() {
        if (_btCancel==null) {
            _btCancel = new JButtonOperator(this, "Cancel"); // NOI18N
        }
        return _btCancel;
    }


    //****************************************
    // Low-level functionality definition part
    //****************************************

    /** changes current selected tab
     * @param tabName String tab name */
    public void selectAttributesPage(String tabName) {
        tbpAttributes().selectPage(tabName);
    }

    /** changes current selected tab to "Basic"
     * @return JTabbedPaneOperator of parent tabbed pane
     */
    public JTabbedPaneOperator selectPageBasic() {
        tbpAttributes().selectPage(_selectPageBasic);
        return tbpAttributes();
    }

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkCtrl(boolean state) {
        if (cbCtrl().isSelected()!=state) {
            cbCtrl().push();
        }
    }

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkAlt(boolean state) {
        if (cbAlt().isSelected()!=state) {
            cbAlt().push();
        }
    }

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkMetaMacOnly(boolean state) {
        if (cbMetaMacOnly().isSelected()!=state) {
            cbMetaMacOnly().push();
        }
    }

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkShift(boolean state) {
        if (cbShift().isSelected()!=state) {
            cbShift().push();
        }
    }

    /** gets text for txtJTextField
     * @return String text
     */
    public String getLetter() {
        return txtJTextField().getText();
    }

    /** sets text for txtJTextField
     * @param text String text
     */
    public void setLetter(String text) {
        txtJTextField().setText(text);
    }

    /** types text for txtJTextField
     * @param text String text
     */
    public void typeLetter(String text) {
        txtJTextField().typeText(text);
    }

    /** clicks on "Clear" JButton
     */
    public void clear() {
        btClear().push();
    }

    /** gets text for txtJTextField2
     * @return String text
     */
    public String getToolTip() {
        return txtJTextField2().getText();
    }

    /** sets text for txtJTextField2
     * @param text String text
     */
    public void setToolTip(String text) {
        txtJTextField2().setText(text);
    }

    /** types text for txtJTextField2
     * @param text String text
     */
    public void typeToolTip(String text) {
        txtJTextField2().typeText(text);
    }

    /** gets text for txtJTextField3
     * @return String text
     */
    public String getText() {
        return txtJTextField3().getText();
    }

    /** sets text for txtJTextField3
     * @param text String text
     */
    public void setText(String text) {
        txtJTextField3().setText(text);
    }

    /** types text for txtJTextField3
     * @param text String text
     */
    public void typeText(String text) {
        txtJTextField3().typeText(text);
    }

    /** clicks on null IconButton
     */
    public void iconButton() {
        btIconButton().push();
    }

    /** clicks on null IconButton
     */
    public void iconButton2() {
        btIconButton2().push();
    }

    /** clicks on "Set Icon..." JButton
     */
    public void setSmallIcon() {
        btSetIcon().push();
    }

    /** clicks on "Set Icon..." JButton
     */
    public void setLargeIcon() {
        btSetIcon2().push();
    }

    /** changes current selected tab to "Advanced"
     * @return JTabbedPaneOperator of parent tabbed pane
     */
    public JTabbedPaneOperator selectPageAdvanced() {
        tbpAttributes().selectPage(_selectPageAdvanced);
        return tbpAttributes();
    }

    /** returns selected item for cboJComboBox
     * @return String item
     */
    public String getSelectedJComboBox() {
        return cboJComboBox().getSelectedItem().toString();
    }

    /** selects item for cboJComboBox
     * @param item String item
     */
    public void selectJComboBox(String item) {
        cboJComboBox().selectItem(item);
    }

    /** gets text for txtJTextField4
     * @return String text
     */
    public String getEnabledPropertyText() {
        return txtJTextField4().getText();
    }

    /** sets text for txtJTextField4
     * @param text String text
     */
    public void setEnabledPropertyText(String text) {
        txtJTextField4().setText(text);
    }

    /** types text for txtJTextField4
     * @param text String text
     */
    public void typeEnabledPropertyText(String text) {
        txtJTextField4().typeText(text);
    }

    /** returns selected item for cboJComboBox2
     * @return String item
     */
    public String getSelectedJComboBox2() {
        return cboJComboBox2().getSelectedItem().toString();
    }

    /** selects item for cboJComboBox2
     * @param item String item
     */
    public void selectJComboBox2(String item) {
        cboJComboBox2().selectItem(item);
    }

    /** gets text for txtJTextField5
     * @return String text
     */
    public String getSelectedPropertyText() {
        return txtJTextField5().getText();
    }

    /** sets text for txtJTextField5
     * @param text String text
     */
    public void setSelectedPropertyText(String text) {
        txtJTextField5().setText(text);
    }

    /** types text for txtJTextField5
     * @param text String text
     */
    public void typeSelectedPropertyText(String text) {
        txtJTextField5().typeText(text);
    }

    /** returns selected item for cboBlockingType
     * @return String item
     */
    public String getSelectedBlockingType() {
        return cboBlockingType().getSelectedItem().toString();
    }

    /** selects item for cboBlockingType
     * @param item String item
     */
    public void selectBlockingType(String item) {
        cboBlockingType().selectItem(item);
    }

    /** gets text for txtJTextField6
     * @return String text
     */
    public String getBlockingDialogTitle() {
        return txtJTextField6().getText();
    }

    /** sets text for txtJTextField6
     * @param text String text
     */
    public void setBlockingDialogTitle(String text) {
        txtJTextField6().setText(text);
    }

    /** types text for txtJTextField6
     * @param text String text
     */
    public void typeBlockingDialogTitle(String text) {
        txtJTextField6().typeText(text);
    }

    /** gets text for txtJTextField7
     * @return String text
     */
    public String getBlockingDialogText() {
        return txtJTextField7().getText();
    }

    /** sets text for txtJTextField7
     * @param text String text
     */
    public void setBlockingDialogText(String text) {
        txtJTextField7().setText(text);
    }

    /** types text for txtJTextField7
     * @param text String text
     */
    public void typeBlockingDialogText(String text) {
        txtJTextField7().typeText(text);
    }

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkJCheckBox(boolean state) {
        if (cbJCheckBox().isSelected()!=state) {
            cbJCheckBox().push();
        }
    }

    /** gets text for txtJTextField8
     * @return String text
     */
    public String getMethodName() {
        return txtJTextField8().getText();
    }

    /** sets text for txtJTextField8
     * @param text String text
     */
    public void setMethodName(String text) {
        txtJTextField8().setText(text);
    }

    /** types text for txtJTextField8
     * @param text String text
     */
    public void typeMethodName(String text) {
        txtJTextField8().typeText(text);
    }

    /** clicks on "Choose Class" JButton
     */
    public void chooseClass() {
        btChooseClass().pushNoBlock();
    }

    /** returns selected item for cboActionToEdit
     * @return String item
     */
    public String getSelectedActionToEdit() {
        return cboActionToEdit().getSelectedItem().toString();
    }

    /** selects item for cboActionToEdit
     * @param item String item
     */
    public void selectActionToEdit(String item) {
        cboActionToEdit().selectItem(item);
    }

    /** clicks on "OK" JButton
     */
    public void ok() {
        btOK().push();
    }

    /** clicks on "Cancel" JButton
     */
    public void cancel() {
        btCancel().push();
    }

    public void selectNode(String path) {
        this.chooseClass();

        ChooseClassOperator classOp = new ChooseClassOperator();
        JTreeOperator treeOp = classOp.treeTreeView();
        treeOp.clickOnPath(treeOp.findPath(path));
        classOp.ok();
    }
}

