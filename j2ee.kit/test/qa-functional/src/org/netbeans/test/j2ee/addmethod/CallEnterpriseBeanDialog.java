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

import javax.swing.JTextField;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JRadioButtonOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JTreeOperator;

/** Class implementing all necessary methods for handling "Call Enterprise Bean" NbPresenter.
 *
 * @author lm97939
 * @version 1.0
 */
public class CallEnterpriseBeanDialog extends JDialogOperator {

    /**
     * Creates new CallEnterpriseBeanDialog that can handle it.
     */
    public CallEnterpriseBeanDialog() {
        super(Bundle.getString("org.netbeans.modules.j2ee.ejbcore.ui.logicalview.entries.Bundle", "LBL_CallEjbActionTitle"));
    }
    private JCheckBoxOperator _cbConvertCheckedExceptionsToRuntimeException;
    private JRadioButtonOperator _rbGenerateInlineLookupCode;
    private JRadioButtonOperator _rbExistingClass;
    private JTextFieldOperator _txtExistingClass;
    private JTreeOperator _tree;
    private JTextFieldOperator _txtReferenceName;
    private JRadioButtonOperator _rbLocal;
    private JRadioButtonOperator _rbRemote;
    private JButtonOperator _btOK;
    private JButtonOperator _btCancel;

    //******************************
    // Subcomponents definition part
    //******************************
    /** Tries to find "Convert Checked Exceptions to RuntimeException" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbConvertCheckedExceptionsToRuntimeException() {
        if (_cbConvertCheckedExceptionsToRuntimeException == null) {
            _cbConvertCheckedExceptionsToRuntimeException = new JCheckBoxOperator(this, Bundle.getStringTrimmed("org.netbeans.modules.j2ee.ejbcore.ui.logicalview.entries.Bundle", "LBL_ConvertToRuntime"));
        }
        return _cbConvertCheckedExceptionsToRuntimeException;
    }

    /** Tries to find "Generate Inline Lookup Code" JRadioButton in this dialog.
     * @return JRadioButtonOperator
     */
    public JRadioButtonOperator rbGenerateInlineLookupCode() {
        if (_rbGenerateInlineLookupCode == null) {
            _rbGenerateInlineLookupCode = new JRadioButtonOperator(this, "Generate Inline Lookup Code");
        }
        return _rbGenerateInlineLookupCode;
    }

    /** Tries to find "Existing Class" JRadioButton in this dialog.
     * @return JRadioButtonOperator
     */
    public JRadioButtonOperator rbExistingClass() {
        if (_rbExistingClass == null) {
            _rbExistingClass = new JRadioButtonOperator(this, "Existing Class");
        }
        return _rbExistingClass;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtExistingClass() {
        if (_txtExistingClass == null) {
            _txtExistingClass = new JTextFieldOperator(this);
        }
        return _txtExistingClass;
    }

    /** Tries to find null TreeView$ExplorerTree in this dialog.
     * @return JTreeOperator
     */
    public JTreeOperator tree() {
        if (_tree == null) {
            _tree = new JTreeOperator(this);
        }
        return _tree;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtReferenceName() {
        if (_txtReferenceName == null) {
            JLabelOperator lblOper = new JLabelOperator(this, "Reference Name:");
            _txtReferenceName = new JTextFieldOperator((JTextField) lblOper.getLabelFor());
            //_txtReferenceName = new JTextFieldOperator(this, 2);
        }
        return _txtReferenceName;
    }

    /** Tries to find "Local" JRadioButton in this dialog.
     * @return JRadioButtonOperator
     */
    public JRadioButtonOperator rbLocal() {
        if (_rbLocal == null) {
            _rbLocal = new JRadioButtonOperator(this, "Local");
        }
        return _rbLocal;
    }

    /** Tries to find "Remote" JRadioButton in this dialog.
     * @return JRadioButtonOperator
     */
    public JRadioButtonOperator rbRemote() {
        if (_rbRemote == null) {
            _rbRemote = new JRadioButtonOperator(this, "Remote");
        }
        return _rbRemote;
    }

    /** Tries to find "OK" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btOK() {
        if (_btOK == null) {
            _btOK = new JButtonOperator(this, "OK");
        }
        return _btOK;
    }

    /** Tries to find "Cancel" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btCancel() {
        if (_btCancel == null) {
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
    public void checkConvertCheckedExceptionsToRuntimeException(boolean state) {
        if (cbConvertCheckedExceptionsToRuntimeException().isSelected() != state) {
            cbConvertCheckedExceptionsToRuntimeException().push();
        }
    }

    /** clicks on "Generate Inline Lookup Code" JRadioButton
     */
    public void generateInlineLookupCode() {
        rbGenerateInlineLookupCode().push();
    }

    /** clicks on "Existing Class" JRadioButton
     */
    public void existingClass() {
        rbExistingClass().push();
    }

    /** gets text for txtExistingClass
     * @return String text
     */
    public String getExistingClass() {
        return txtExistingClass().getText();
    }

    /** sets text for txtExistingClass
     * @param text String text
     */
    public void setExistingClass(String text) {
        txtExistingClass().setText(text);
    }

    /** types text for txtExistingClass
     * @param text String text
     */
    public void typeExistingClass(String text) {
        txtExistingClass().typeText(text);
    }

    /** gets text for txtReferenceName
     * @return String text
     */
    public String getReferenceName() {
        return txtReferenceName().getText();
    }

    /** sets text for txtReferenceName
     * @param text String text
     */
    public void setReferenceName(String text) {
        txtReferenceName().setText(text);
    }

    /** types text for txtReferenceName
     * @param text String text
     */
    public void typeReferenceName(String text) {
        txtReferenceName().typeText(text);
    }

    /** clears text for txtReferenceName
     */
    public void clearReferenceName() {
        txtReferenceName().clearText();
    }

    /** clicks on "Local" JRadioButton
     */
    public void local() {
        rbLocal().push();
    }

    /** clicks on "Remote" JRadioButton
     */
    public void remote() {
        rbRemote().push();
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
     * Performs verification of CallEnterpriseBeanDialog by accessing all its components.
     */
    public void verify() {
        cbConvertCheckedExceptionsToRuntimeException();
        rbGenerateInlineLookupCode();
        rbExistingClass();
        txtExistingClass();
        tree();
        txtReferenceName();
        rbLocal();
        rbRemote();
        btOK();
        btCancel();
    }

    /**
     * Performs simple test of CallEnterpriseBeanDialog
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        new CallEnterpriseBeanDialog().verify();
        System.out.println("CallEnterpriseBean verification finished.");
    }
}
