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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

