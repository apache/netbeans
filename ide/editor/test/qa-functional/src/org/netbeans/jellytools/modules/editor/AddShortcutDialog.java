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
package org.netbeans.jellytools.modules.editor;

import org.netbeans.jemmy.operators.*;
import org.netbeans.jemmy.util.NameComponentChooser;

/** Class implementing all necessary methods for handling "Add Shortcut Dialog" NbDialog.
 *
 * @author jp159440
 * @version 1.0
 */
public class AddShortcutDialog extends JDialogOperator {

    /** Creates new AddShortcutDialog that can handle it.
     */
    public AddShortcutDialog() {
        super("Add Shortcut");
    }

    private JLabelOperator _lblShortcut;
    private JLabelOperator _lblConflict;
    private JTextFieldOperator _txtJTextField;
    private JButtonOperator _btOK;
    private JButtonOperator _btCancel;
    private JButtonOperator _btClear;
    private JButtonOperator _btTab;


    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find "Shortcut:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblShortcut() {
        if (_lblShortcut==null) {
            _lblShortcut = new JLabelOperator(this, "Shortcut:");
        }
        return _lblShortcut;
    }
    
    /** Tries to find "Shortcut:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblConflict() {
        if (_lblConflict==null) {
            _lblConflict = new JLabelOperator(this,0);
        }
        return _lblConflict;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtJTextField() {
        if (_txtJTextField==null) {
            _txtJTextField = new JTextFieldOperator(this);
        }
        return _txtJTextField;
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

    /** Tries to find "Clear" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btClear() {
        if (_btClear==null) {
            _btClear = new JButtonOperator(this, "Clear");
        }
        return _btClear;
    }

    /** Tries to find "Tab" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btTab() {
        if (_btTab==null) {
            _btTab = new JButtonOperator(this, "Tab");
        }
        return _btTab;
    }


    //****************************************
    // Low-level functionality definition part
    //****************************************

    /** gets text for txtJTextField
     * @return String text
     */
    public String getJTextField() {
        return txtJTextField().getText();
    }

    /** sets text for txtJTextField
     * @param text String text
     */
    public void setJTextField(String text) {
        txtJTextField().setText(text);
    }

    /** types text for txtJTextField
     * @param text String text
     */
    public void typeJTextField(String text) {
        txtJTextField().typeText(text);
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

    /** clicks on "Clear" JButton
     */
    public void clear() {
        btClear().push();
    }

    /** clicks on "Tab" JButton
     */
    public void tab() {
        btTab().push();
    }


    //*****************************************
    // High-level functionality definition part
    //*****************************************

    /** Performs verification of AddShortcutDialog by accessing all its components.
     */
    public void verify() {
        lblShortcut();
        txtJTextField();
        btOK();
        btCancel();
        btClear();
        btTab();
    }

    /** Performs simple test of AddShortcutDialog
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        new AddShortcutDialog().verify();
        System.out.println("AddShortcutDialog verification finished.");
    }
}

