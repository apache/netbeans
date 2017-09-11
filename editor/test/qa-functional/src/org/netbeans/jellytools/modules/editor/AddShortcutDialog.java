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

