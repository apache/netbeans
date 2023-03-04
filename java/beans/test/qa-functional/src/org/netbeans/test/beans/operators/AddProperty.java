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
/*
 * AddProperty.java
 *
 * Created on 2/25/14 2:53 PM
 */
package org.netbeans.test.beans.operators;

import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.*;

/** Class implementing all necessary methods for handling "Add Property" NbDialog.
 *
 * @author jprox
 * @version 1.0
 */
public class AddProperty extends JDialogOperator {


    /** Creates new AddProperty that can handle it.
     */
    public AddProperty() {
        super("Add Property");
    }

    private JLabelOperator _lblName;
    private JLabelOperator _lblType;
    private JCheckBoxOperator _cbStatic;
    private JRadioButtonOperator _rbPrivate;
    private JCheckBoxOperator _cbFinal;
    private JRadioButtonOperator _rbPackage;
    private JRadioButtonOperator _rbProtected;
    private JRadioButtonOperator _rbPublic;
    private JCheckBoxOperator _cbBound;
    private JTextFieldOperator _txtBoundName;
    private JCheckBoxOperator _cbVetoable;
    private JRadioButtonOperator _rbGenerateGetterAndSetter;
    private JRadioButtonOperator _rbGenerateGetter;
    private JRadioButtonOperator _rbGenerateSetter;
    private JCheckBoxOperator _cbGenerateJavadoc;
    private JCheckBoxOperator _cbIndexed;
    private JCheckBoxOperator _cbGeneratePropertyChangeSupport;
    private JCheckBoxOperator _cbGenerateVetoableChangeSupport;
    private JComboBoxOperator _cboType;    
    private JButtonOperator _btBrowse;
    private JTextFieldOperator _txtName;
    private JLabelOperator _lblJLabel;
    private JTextFieldOperator _txtDefaultValue;
    private JLabelOperator _lblJLabel2;
    private JLabelOperator _lblPreview;
    private JLabelOperator _lblError;
    private JEditorPaneOperator _txtJEditorPane;
    private JButtonOperator _btWindowsScrollBarUI$WindowsArrowButton;
    private JButtonOperator _btWindowsScrollBarUI$WindowsArrowButton2;
    private JButtonOperator _btOK;
    private JButtonOperator _btCancel;

    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find "Name:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblName() {
        if (_lblName==null) {
            _lblName = new JLabelOperator(this, "Name:");
        }
        return _lblName;
    }

    /** Tries to find "Type:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblType() {
        if (_lblType==null) {
            _lblType = new JLabelOperator(this, "Type:");
        }
        return _lblType;
    }

    /** Tries to find "static" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbStatic() {
        if (_cbStatic==null) {
            _cbStatic = new JCheckBoxOperator(this, "static");
        }
        return _cbStatic;
    }

    /** Tries to find "private" JRadioButton in this dialog.
     * @return JRadioButtonOperator
     */
    public JRadioButtonOperator rbPrivate() {
        if (_rbPrivate==null) {
            _rbPrivate = new JRadioButtonOperator(this, "private");
        }
        return _rbPrivate;
    }

    /** Tries to find "final" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbFinal() {
        if (_cbFinal==null) {
            _cbFinal = new JCheckBoxOperator(this, "final");
        }
        return _cbFinal;
    }

    /** Tries to find "package" JRadioButton in this dialog.
     * @return JRadioButtonOperator
     */
    public JRadioButtonOperator rbPackage() {
        if (_rbPackage==null) {
            _rbPackage = new JRadioButtonOperator(this, "package");
        }
        return _rbPackage;
    }

    /** Tries to find "protected" JRadioButton in this dialog.
     * @return JRadioButtonOperator
     */
    public JRadioButtonOperator rbProtected() {
        if (_rbProtected==null) {
            _rbProtected = new JRadioButtonOperator(this, "protected");
        }
        return _rbProtected;
    }

    /** Tries to find "public" JRadioButton in this dialog.
     * @return JRadioButtonOperator
     */
    public JRadioButtonOperator rbPublic() {
        if (_rbPublic==null) {
            _rbPublic = new JRadioButtonOperator(this, "public");
        }
        return _rbPublic;
    }

    /** Tries to find "Bound" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbBound() {
        if (_cbBound==null) {
            _cbBound = new JCheckBoxOperator(this, "Bound");
        }
        return _cbBound;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtBoundName() {
        if (_txtBoundName==null) {
            _txtBoundName = new JTextFieldOperator(this);
        }
        return _txtBoundName;
    }

    /** Tries to find "Vetoable" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbVetoable() {
        if (_cbVetoable==null) {
            _cbVetoable = new JCheckBoxOperator(this, "Vetoable");
        }
        return _cbVetoable;
    }

    /** Tries to find "Generate getter and setter" JRadioButton in this dialog.
     * @return JRadioButtonOperator
     */
    public JRadioButtonOperator rbGenerateGetterAndSetter() {
        if (_rbGenerateGetterAndSetter==null) {
            _rbGenerateGetterAndSetter = new JRadioButtonOperator(this, "Generate getter and setter");
        }
        return _rbGenerateGetterAndSetter;
    }

    /** Tries to find "Generate getter" JRadioButton in this dialog.
     * @return JRadioButtonOperator
     */
    public JRadioButtonOperator rbGenerateGetter() {
        if (_rbGenerateGetter==null) {
            _rbGenerateGetter = new JRadioButtonOperator(this, "Generate getter", 1);
        }
        return _rbGenerateGetter;
    }

    /** Tries to find "Generate setter" JRadioButton in this dialog.
     * @return JRadioButtonOperator
     */
    public JRadioButtonOperator rbGenerateSetter() {
        if (_rbGenerateSetter==null) {
            _rbGenerateSetter = new JRadioButtonOperator(this, "Generate setter");
        }
        return _rbGenerateSetter;
    }

    /** Tries to find "Generate javadoc" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbGenerateJavadoc() {
        if (_cbGenerateJavadoc==null) {
            _cbGenerateJavadoc = new JCheckBoxOperator(this, "Generate javadoc");
        }
        return _cbGenerateJavadoc;
    }

    /** Tries to find "Indexed" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbIndexed() {
        if (_cbIndexed==null) {
            _cbIndexed = new JCheckBoxOperator(this, "Indexed");
        }
        return _cbIndexed;
    }

    /** Tries to find "Generate Property Change Support" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbGeneratePropertyChangeSupport() {
        if (_cbGeneratePropertyChangeSupport==null) {
            _cbGeneratePropertyChangeSupport = new JCheckBoxOperator(this, "Generate Property Change Support");
        }
        return _cbGeneratePropertyChangeSupport;
    }

    /** Tries to find "Generate Vetoable Change Support" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbGenerateVetoableChangeSupport() {
        if (_cbGenerateVetoableChangeSupport==null) {
            _cbGenerateVetoableChangeSupport = new JCheckBoxOperator(this, "Generate Vetoable Change Support");
        }
        return _cbGenerateVetoableChangeSupport;
    }

    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboType() {
        if (_cboType==null) {
            _cboType = new JComboBoxOperator(this);
        }
        return _cboType;
    }

    /** Tries to find "Browse..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btBrowse() {
        if (_btBrowse==null) {
            _btBrowse = new JButtonOperator(this, "Browse...");
        }
        return _btBrowse;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtName() {
        if (_txtName==null) {
            _txtName = new JTextFieldOperator(this, 2);
        }
        return _txtName;
    }

    /** Tries to find "=" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblJLabel() {
        if (_lblJLabel==null) {
            _lblJLabel = new JLabelOperator(this, "=");
        }
        return _lblJLabel;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtDefaultValue() {
        if (_txtDefaultValue==null) {
            _txtDefaultValue = new JTextFieldOperator(this, 3);
        }
        return _txtDefaultValue;
    }

    /** Tries to find ";" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblJLabel2() {
        if (_lblJLabel2==null) {
            _lblJLabel2 = new JLabelOperator(this, ";");
        }
        return _lblJLabel2;
    }

    /** Tries to find "Preview:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblPreview() {
        if (_lblPreview==null) {
            _lblPreview = new JLabelOperator(this, "Preview:");
        }
        return _lblPreview;
    }
    
    public JLabelOperator lblError() {
        if (_lblError==null) {
            _lblError = new JLabelOperator(this, 5);                                        
        }
        return _lblError;
    }

    /** Tries to find null JEditorPane in this dialog.
     * @return JEditorPaneOperator
     */
    public JEditorPaneOperator txtPreview() {
        if (_txtJEditorPane==null) {
            _txtJEditorPane = new JEditorPaneOperator(this);
        }
        return _txtJEditorPane;
    }

    /** Tries to find null WindowsScrollBarUI$WindowsArrowButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btWindowsScrollBarUI$WindowsArrowButton() {
        if (_btWindowsScrollBarUI$WindowsArrowButton==null) {
            _btWindowsScrollBarUI$WindowsArrowButton = new JButtonOperator(this, 2);
        }
        return _btWindowsScrollBarUI$WindowsArrowButton;
    }

    /** Tries to find null WindowsScrollBarUI$WindowsArrowButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btWindowsScrollBarUI$WindowsArrowButton2() {
        if (_btWindowsScrollBarUI$WindowsArrowButton2==null) {
            _btWindowsScrollBarUI$WindowsArrowButton2 = new JButtonOperator(this, 3);
        }
        return _btWindowsScrollBarUI$WindowsArrowButton2;
    }

    /** Tries to find "OK" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btOK() {
        if (_btOK==null) {
            _btOK = new JButtonOperator(this, "OK");
        }
        return _btOK;
    }

    /** Tries to find "Cancel" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btCancel() {
        if (_btCancel==null) {
            _btCancel = new JButtonOperator(this, "Cancel");
        }
        return _btCancel;
    }


    //****************************************
    // Low-level functionality definition part
    //****************************************

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkStatic(boolean state) {
        if (cbStatic().isSelected()!=state) {
            cbStatic().push();
        }
    }

    /** clicks on "private" JRadioButton
     */
    public void privateBt() {
        rbPrivate().push();
    }

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkFinal(boolean state) {
        if (cbFinal().isSelected()!=state) {
            cbFinal().push();
        }
    }

    /** clicks on "package" JRadioButton
     */
    public void packageBt() {
        rbPackage().push();
    }

    /** clicks on "protected" JRadioButton
     */
    public void protectedBt() {
        rbProtected().push();
    }

    /** clicks on "public" JRadioButton
     */
    public void publicBt() {
        rbPublic().push();
    }

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkBound(boolean state) {
        if (cbBound().isSelected()!=state) {
            cbBound().push();
        }
    }

    /** gets text for txtJTextField
     * @return String text
     */
    public String getJTextField() {
        return txtBoundName().getText();
    }

    /** sets text for txtJTextField
     * @param text String text
     */
    public void setJTextField(String text) {
        txtBoundName().setText(text);
    }

    /** types text for txtJTextField
     * @param text String text
     */
    public void typeJTextField(String text) {
        txtBoundName().typeText(text);
    }

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkVetoable(boolean state) {
        if (cbVetoable().isSelected()!=state) {
            cbVetoable().push();
        }
    }

    /** clicks on "Generate getter and setter" JRadioButton
     */
    public void generateGetterAndSetter() {
        rbGenerateGetterAndSetter().push();
    }

    /** clicks on "Generate getter" JRadioButton
     */
    public void generateGetter() {
        rbGenerateGetter().push();
    }

    /** clicks on "Generate setter" JRadioButton
     */
    public void generateSetter() {
        rbGenerateSetter().push();
    }

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkGenerateJavadoc(boolean state) {
        if (cbGenerateJavadoc().isSelected()!=state) {
            cbGenerateJavadoc().push();
        }
    }

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkIndexed(boolean state) {
        if (cbIndexed().isSelected()!=state) {
            cbIndexed().push();
        }
    }

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkGeneratePropertyChangeSupport(boolean state) {
        if (cbGeneratePropertyChangeSupport().isSelected()!=state) {
            cbGeneratePropertyChangeSupport().push();
        }
    }

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkGenerateVetoableChangeSupport(boolean state) {
        if (cbGenerateVetoableChangeSupport().isSelected()!=state) {
            cbGenerateVetoableChangeSupport().push();
        }
    }

    /** returns selected item for cboType
     * @return String item
     */
    public String getSelectedType() {
        return cboType().getSelectedItem().toString();
    }

    /** selects item for cboType
     * @param item String item
     */
    public void selectType(String item) {
        cboType().selectItem(item);
    }

    /** types text for cboType
     * @param text String text
     */
    public void typeType(String text) {
        cboType().typeText(text);
    }

    /** clicks on "Browse..." JButton
     */
    public void browse() {
        btBrowse().push();
    }

    /** gets text for txtName
     * @return String text
     */
    public String getName() {
        return txtName().getText();
    }

    /** sets text for txtName
     * @param text String text
     */
    public void setName(String text) {
        txtName().setText(text);
    }

    /** types text for txtName
     * @param text String text
     */
    public void typeName(String text) {
        txtName().typeText(text);
    }

    /** gets text for txtJTextField2
     * @return String text
     */
    public String getJTextField2() {
        return txtDefaultValue().getText();
    }

    /** sets text for txtJTextField2
     * @param text String text
     */
    public void setJTextField2(String text) {
        txtDefaultValue().setText(text);
    }

    /** types text for txtJTextField2
     * @param text String text
     */
    public void typeJTextField2(String text) {
        txtDefaultValue().typeText(text);
    }

    /** gets text for txtJEditorPane
     * @return String text
     */
    public String getPrevierew() {
        return txtPreview().getText();
    }
        
    /** clicks on "OK" JButton
     */
    public void ok() {
        new EventTool().waitNoEvent(500);
        btOK().push();
    }

    /** clicks on "Cancel" JButton
     */
    public void cancel() {
        btCancel().push();
    }

}

