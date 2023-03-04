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

import org.netbeans.jemmy.operators.*;
import org.netbeans.jemmy.util.NameComponentChooser;
import org.netbeans.jemmy.operators.*;
import javax.swing.JTextField;
import org.netbeans.jellytools.NbDialogOperator;

/** Class implementing all necessary methods for handling "Add CMP Field..." NbPresenter.
 *
 * @author lm97939
 * @version 1.0
 */
public class AddCMPFieldDialog extends JDialogOperator {

    /**
     * Creates new AddCMPFieldDialog that can handle it.
     */
    public AddCMPFieldDialog() {
        super(org.netbeans.jellytools.Bundle.getStringTrimmed("org.netbeans.modules.j2ee.ejbcore.ui.logicalview.ejb.action.Bundle", "LBL_AddCmpFieldAction"));
    }

    private JTextFieldOperator _txtName;
    private JTextFieldOperator _txtDescription;
    private JComboBoxOperator _comboType;
    private JCheckBoxOperator _cbGetter;
    private JCheckBoxOperator _cbSetter;
    private JCheckBoxOperator _cbGetter2;
    private JCheckBoxOperator _cbSetter2;
    private JButtonOperator _btOK;
    private JButtonOperator _btCancel;


    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtName() {
        if (_txtName==null) {
            JLabelOperator lblOper = new JLabelOperator(this, "Name");
        	_txtName = new JTextFieldOperator((JTextField)lblOper.getLabelFor());
        }
        return _txtName;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtDescription() {
        if (_txtDescription==null) {
            _txtDescription = new JTextFieldOperator(this, 1);
        }
        return _txtDescription;
    }

    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator comboType() {
        if (_comboType==null) {
            //_txtType = new JTextFieldOperator(this,2);
            _comboType = new JComboBoxOperator(this);
        }
        return _comboType;
    }

    /** Tries to find "Getter" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbLocalGetter() {
        if (_cbGetter==null) {
            _cbGetter = new JCheckBoxOperator(this, "Getter");
        }
        return _cbGetter;
    }

    /** Tries to find "Setter" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbLocalSetter() {
        if (_cbSetter==null) {
            _cbSetter = new JCheckBoxOperator(this, "Setter");
        }
        return _cbSetter;
    }

    /** Tries to find "Getter" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbRemoteGetter() {
        if (_cbGetter2==null) {
            _cbGetter2 = new JCheckBoxOperator(this, "Getter", 1);
        }
        return _cbGetter2;
    }

    /** Tries to find "Setter" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbRemoteSetter() {
        if (_cbSetter2==null) {
            _cbSetter2 = new JCheckBoxOperator(this, "Setter", 1);
        }
        return _cbSetter2;
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

    /** gets text for txtDescription
     * @return String text
     */
    public String getDescription() {
        return txtDescription().getText();
    }

    /** sets text for txtDescription
     * @param text String text
     */
    public void setDescription(String text) {
        txtDescription().setText(text);
    }

    /** types text for txtDescription
     * @param text String text
     */
    public void typeDescription(String text) {
        txtDescription().typeText(text);
    }

    /** returns selected item for cboType
     * @return String item
     */
    public String getSelectedType() {
        return comboType().getTextField().getText();
    }

    /** selects item for cboType
     * @param item String item
     */
    public void setType(String item) {
        comboType().selectItem(item);
    }

    /** types text for cboType
     * @param text String text
     */
    public void typeType(String text) {
        comboType().getTextField().typeText(text);
    }

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkLocalGetter(boolean state) {
        if (cbLocalGetter().isSelected()!=state) {
            cbLocalGetter().push();
        }
    }

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkLocalSetter(boolean state) {
        if (cbLocalSetter().isSelected()!=state) {
            cbLocalSetter().push();
        }
    }

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkRemoteGetter(boolean state) {
        if (cbRemoteGetter().isSelected()!=state) {
            cbRemoteGetter().push();
        }
    }

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkRemoteSetter(boolean state) {
        if (cbRemoteSetter().isSelected()!=state) {
            cbRemoteSetter().push();
        }
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


    //*****************************************
    // High-level functionality definition part
    //*****************************************

    /**
     * Performs verification of AddCMPFieldDialog by accessing all its components.
     */
    public void verify() {
        txtName();
        txtDescription();
        comboType();
        cbLocalGetter();
        cbLocalSetter();
        cbRemoteGetter();
        cbRemoteSetter();
        btOK();
        btCancel();
    }

    /**
     * Performs simple test of AddCMPFieldDialog
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        new AddCMPFieldDialog().verify();
        System.out.println("AddCMPField verification finished.");
    }
}

