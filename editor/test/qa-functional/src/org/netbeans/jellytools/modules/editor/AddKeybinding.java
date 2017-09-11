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

/*
 * AddKeybinding.java
 *
 * Created on 10/17/02 12:54 PM
 */
package org.netbeans.jellytools.modules.editor;

import org.netbeans.jemmy.operators.*;

/** Class implementing all necessary methods for handling "Add Keybinding" NbDialog.
 *
 * @author eh103527
 * @version 1.0
 */
public class AddKeybinding extends JDialogOperator {

    /** Creates new AddKeybinding that can handle it.
     */
    public AddKeybinding() {
        super(java.util.ResourceBundle.getBundle("org.netbeans.editor.Bundle").getString("MSP_AddTitle"));
    }

    private JLabelOperator _lblShortcutSequence;
    private JTextFieldOperator _txtShortcutSequence;
    private JTextAreaOperator _txtJTextArea;
    private JButtonOperator _btOK;
    private JButtonOperator _btClear;
    private JButtonOperator _btCancel;
    private JButtonOperator _btHelp;
    
    
    //******************************
    // Subcomponents definition part
    //******************************
    
    /** Tries to find "Shortcut Sequence:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblShortcutSequence() {
        if (_lblShortcutSequence==null) {
            _lblShortcutSequence = new JLabelOperator(this, java.util.ResourceBundle.getBundle("org.netbeans.modules.editor.options.Bundle").getString("LBL_KSIP_Sequence"));
        }
        return _lblShortcutSequence;
    }
    
    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtShortcutSequence() {
        if (_txtShortcutSequence==null) {
            _txtShortcutSequence = new JTextFieldOperator(this);
        }
        return _txtShortcutSequence;
    }
    
    /** Tries to find null JTextArea in this dialog.
     * @return JTextAreaOperator
     */
    public JTextAreaOperator txtJTextArea() {
        if (_txtJTextArea==null) {
            _txtJTextArea = new JTextAreaOperator(this);
        }
        return _txtJTextArea;
    }
    
    /** Tries to find "OK" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btOK() {
        if (_btOK==null) {
            _btOK = new JButtonOperator(this, java.util.ResourceBundle.getBundle("org.netbeans.modules.editor.options.Bundle").getString("KBEP_OK_LABEL"));
        }
        return _btOK;
    }
    
    /** Tries to find "Clear" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btClear() {
        if (_btClear==null) {
            _btClear = new JButtonOperator(this, java.util.ResourceBundle.getBundle("org.netbeans.modules.editor.options.Bundle").getString("KBEP_CLEAR_LABEL"));
        }
        return _btClear;
    }
    
    /** Tries to find "Cancel" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btCancel() {
        if (_btCancel==null) {
            _btCancel = new JButtonOperator(this, java.util.ResourceBundle.getBundle("org.openide.explorer.propertysheet.Bundle").getString("CTL_Cancel"));
        }
        return _btCancel;
    }
    
    /** Tries to find "Help" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btHelp() {
        if (_btHelp==null) {
            _btHelp = new JButtonOperator(this, java.util.ResourceBundle.getBundle("org.openide.explorer.propertysheet.Bundle").getString("CTL_Help"));
        }
        return _btHelp;
    }
    
    
    //****************************************
    // Low-level functionality definition part
    //****************************************
    
    /** gets text for txtShortcutSequence
     * @return String text
     */
    public String getShortcutSequence() {
        return txtShortcutSequence().getText();
    }
    
    /** sets text for txtShortcutSequence
     * @param text String text
     */
    public void setShortcutSequence(String text) {
        txtShortcutSequence().setText(text);
    }
    
    /** types text for txtShortcutSequence
     * @param text String text
     */
    public void typeShortcutSequence(String text) {
        txtShortcutSequence().typeText(text);
    }
    
    /** gets text for txtJTextArea
     * @return String text
     */
    public String getJTextArea() {
        return txtJTextArea().getText();
    }
    
    /** sets text for txtJTextArea
     * @param text String text
     */
    public void setJTextArea(String text) {
        txtJTextArea().setText(text);
    }
    
    /** types text for txtJTextArea
     * @param text String text
     */
    public void typeJTextArea(String text) {
        txtJTextArea().typeText(text);
    }
    
    /** clicks on "OK" JButton
     */
    public void oK() {
        btOK().push();
    }
    
    /** clicks on "Clear" JButton
     */
    public void clear() {
        btClear().push();
    }
    
    /** clicks on "Cancel" JButton
     */
    public void cancel() {
        btCancel().push();
    }
    
    /** clicks on "Help" JButton
     */
    public void help() {
        btHelp().push();
    }
    
    
    //*****************************************
    // High-level functionality definition part
    //*****************************************
    
    /** Performs verification of AddKeybinding by accessing all its components.
     */
    public void verify() {
        lblShortcutSequence();
        txtShortcutSequence();
        txtJTextArea();
        btOK();
        btClear();
        btCancel();
        btHelp();
    }
    
    /** Performs simple test of AddKeybinding
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        new AddKeybinding().verify();
        System.out.println("AddKeybinding verification finished.");
    }
}

