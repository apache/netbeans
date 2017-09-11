/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.i18n.jelly;

import org.netbeans.jemmy.operators.*;
import org.netbeans.jellytools.*;
import org.netbeans.jellytools.properties.PropertySheetOperator;

/** Class implementing all necessary methods for handling "Internationalize [TestFrame]" TopFrameTypeImpl.
 *
 * @author eh103527
 * @version 1.0
 */
public class InternationalizeOperator extends NbDialogOperator {
    
    /** Creates new InternationalizeTestFrameFrameOperator that can handle it.
     */
    public InternationalizeOperator() {
        super(Bundle.getStringTrimmed("org.netbeans.modules.i18n.Bundle", "CTL_I18nDialogTitle"));
    }
    
    private JLabelOperator _lblBundleName;
    private JTextFieldOperator _txtBundleName;
    private JButtonOperator _btBrowse;
    private JButtonOperator _btNew;
    private JLabelOperator _lblComment;
    private JTextAreaOperator _txtComment;
    private JButtonOperator _btMetalScrollButton;
    private JButtonOperator _btMetalScrollButton2;
    private JLabelOperator _lblKey;
    private JLabelOperator _lblValue;
    private JTextAreaOperator _txtValue;
    private JButtonOperator _btMetalScrollButton3;
    private JButtonOperator _btMetalScrollButton4;
    private JComboBoxOperator _cboKey;
    private JTextFieldOperator _txtReplaceString;
    private JLabelOperator _lblReplaceString;
    private JButtonOperator _btFormat;
    private JButtonOperator _btArguments;
    private JButtonOperator _btReplace;
    private JButtonOperator _btSkip;
    private JButtonOperator _btInfo;
    private JButtonOperator _btClose;
    private JButtonOperator _btHelp;
    
    
    //******************************
    // Subcomponents definition part
    //******************************
    
    /** Tries to find "Bundle Name:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblBundleName() {
        if (_lblBundleName==null) {
            _lblBundleName = new JLabelOperator(this, Bundle.getStringTrimmed("org.netbeans.modules.i18n.Bundle", "LBL_BundleName"));
        }
        return _lblBundleName;
    }
    
    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtBundleName() {
        if (_txtBundleName==null) {
            _txtBundleName = new JTextFieldOperator(this);
        }
        return _txtBundleName;
    }
    
    /** Tries to find "Browse..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btBrowse() {
        if (_btBrowse==null) {
            _btBrowse = new JButtonOperator(this, Bundle.getStringTrimmed("org.netbeans.modules.i18n.Bundle", "CTL_BrowseButton"));
        }
        return _btBrowse;
    }
    
    /** Tries to find "New..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btNew() {
        if (_btNew==null) {
            _btNew = new JButtonOperator(this, Bundle.getStringTrimmed("org.netbeans.modules.i18n.Bundle", "CTL_NewButton"));
        }
        return _btNew;
    }
    
    /** Tries to find "Comment:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblComment() {
        if (_lblComment==null) {
            _lblComment = new JLabelOperator(this, Bundle.getStringTrimmed("org.netbeans.modules.i18n.Bundle", "LBL_Comment"));
        }
        return _lblComment;
    }
    
    /** Tries to find null JTextArea in this dialog.
     * @return JTextAreaOperator
     */
    public JTextAreaOperator txtComment() {
        if (_txtComment==null) {
            _txtComment = new JTextAreaOperator(this);
        }
        return _txtComment;
    }
    
    /** Tries to find null MetalScrollButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btMetalScrollButton() {
        if (_btMetalScrollButton==null) {
            _btMetalScrollButton = new JButtonOperator(this, 2);
        }
        return _btMetalScrollButton;
    }
    
    /** Tries to find null MetalScrollButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btMetalScrollButton2() {
        if (_btMetalScrollButton2==null) {
            _btMetalScrollButton2 = new JButtonOperator(this, 3);
        }
        return _btMetalScrollButton2;
    }
    
    /** Tries to find "Key:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblKey() {
        if (_lblKey==null) {
            _lblKey = new JLabelOperator(this, Bundle.getStringTrimmed("org.netbeans.modules.i18n.Bundle", "LBL_Key"));
        }
        return _lblKey;
    }
    
    /** Tries to find "Value:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblValue() {
        if (_lblValue==null) {
            _lblValue = new JLabelOperator(this, Bundle.getStringTrimmed("org.netbeans.modules.i18n.Bundle", "LBL_Value"));
        }
        return _lblValue;
    }
    
    /** Tries to find null JTextArea in this dialog.
     * @return JTextAreaOperator
     */
    public JTextAreaOperator txtValue() {
        if (_txtValue==null) {
            _txtValue = new JTextAreaOperator(this, 1);
        }
        return _txtValue;
    }
    
    /** Tries to find null MetalScrollButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btMetalScrollButton3() {
        if (_btMetalScrollButton3==null) {
            _btMetalScrollButton3 = new JButtonOperator(this, 4);
        }
        return _btMetalScrollButton3;
    }
    
    /** Tries to find null MetalScrollButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btMetalScrollButton4() {
        if (_btMetalScrollButton4==null) {
            _btMetalScrollButton4 = new JButtonOperator(this, 5);
        }
        return _btMetalScrollButton4;
    }
    
    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboKey() {
        if (_cboKey==null) {
            _cboKey = new JComboBoxOperator(this);
        }
        return _cboKey;
    }
    
    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtReplaceString() {
        if (_txtReplaceString==null) {
            _txtReplaceString = new JTextFieldOperator(this, 2);
        }
        return _txtReplaceString;
    }
    
    /** Tries to find "Replace String:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblReplaceString() {
        if (_lblReplaceString==null) {
            _lblReplaceString = new JLabelOperator(this, Bundle.getStringTrimmed("org.netbeans.modules.i18n.Bundle", "LBL_ReplaceFormat"));
        }
        return _lblReplaceString;
    }
    
    /** Tries to find "Format..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btFormat() {
        if (_btFormat==null) {
            _btFormat = new JButtonOperator(this, Bundle.getStringTrimmed("org.netbeans.modules.i18n.Bundle", "CTL_Format"));
        }
        return _btFormat;
    }
    
    /** Tries to find "Arguments..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btArguments() {
        if (_btArguments==null) {
            _btArguments = new JButtonOperator(this, Bundle.getStringTrimmed("org.netbeans.modules.i18n.Bundle", "LBL_Arguments"));
        }
        return _btArguments;
    }
    
    /** Tries to find "Replace" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btReplace() {
        if (_btReplace==null) {
            _btReplace = new JButtonOperator(this, Bundle.getStringTrimmed("org.netbeans.modules.i18n.Bundle", "CTL_ReplaceButton"));
        }
        return _btReplace;
    }
    
    /** Tries to find "Skip" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btSkip() {
        if (_btSkip==null) {
            _btSkip = new JButtonOperator(this, Bundle.getStringTrimmed("org.netbeans.modules.i18n.Bundle", "CTL_ReplaceButton"));
        }
        return _btSkip;
    }
    
    /** Tries to find "Info" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btInfo() {
        if (_btInfo==null) {
            _btInfo = new JButtonOperator(this, Bundle.getStringTrimmed("org.netbeans.modules.i18n.Bundle", "CTL_InfoButton"));
        }
        return _btInfo;
    }
    
    /** Tries to find "Close" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btClose() {
        if (_btClose==null) {
            _btClose = new JButtonOperator(this, Bundle.getStringTrimmed("org.netbeans.modules.i18n.Bundle", "CTL_CloseButton"));
        }
        return _btClose;
    }
    
    /** Tries to find "Help" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btHelp() {
        if (_btHelp==null) {
            _btHelp = new JButtonOperator(this, Bundle.getStringTrimmed("org.netbeans.modules.i18n.Bundle", "CTL_HelpButton"));
        }
        return _btHelp;
    }
    
    
    //****************************************
    // Low-level functionality definition part
    //****************************************
    
    /** gets text for txtBundleName
     * @return String text
     */
    public String getBundleName() {
        return txtBundleName().getText();
    }
    
    /** sets text for txtBundleName
     * @param text String text
     */
    public void setBundleName(String text) {
        txtBundleName().setText(text);
    }
    
    /** types text for txtBundleName
     * @param text String text
     */
    public void typeBundleName(String text) {
        txtBundleName().typeText(text);
    }
    
    /** clicks on "Browse..." JButton
     */
    public void browse() {
        btBrowse().push();
    }
    
    /** clicks on "New..." JButton
     */
    public void clickNew() {
        btNew().push();
    }
    
    /** gets text for txtComment
     * @return String text
     */
    public String getComment() {
        return txtComment().getText();
    }
    
    /** sets text for txtComment
     * @param text String text
     */
    public void setComment(String text) {
        txtComment().setText(text);
    }
    
    /** types text for txtComment
     * @param text String text
     */
    public void typeComment(String text) {
        txtComment().typeText(text);
    }
    
    /** clicks on null MetalScrollButton
     */
    public void metalScrollButton() {
        btMetalScrollButton().push();
    }
    
    /** clicks on null MetalScrollButton
     */
    public void metalScrollButton2() {
        btMetalScrollButton2().push();
    }
    
    /** gets text for txtValue
     * @return String text
     */
    public String getValue() {
        return txtValue().getText();
    }
    
    /** sets text for txtValue
     * @param text String text
     */
    public void setValue(String text) {
        txtValue().setText(text);
    }
    
    /** types text for txtValue
     * @param text String text
     */
    public void typeValue(String text) {
        txtValue().typeText(text);
    }
    
    /** clicks on null MetalScrollButton
     */
    public void metalScrollButton3() {
        btMetalScrollButton3().push();
    }
    
    /** clicks on null MetalScrollButton
     */
    public void metalScrollButton4() {
        btMetalScrollButton4().push();
    }
    
    /** returns selected item for cboKey
     * @return String item
     */
    public String getSelectedKey() {
        return cboKey().getSelectedItem().toString();
    }
    
    /** selects item for cboKey
     * @param item String item
     */
    public void selectKey(String item) {
        cboKey().selectItem(item);
    }
    
    /** types text for cboKey
     * @param text String text
     */
    public void typeKey(String text) {
        cboKey().typeText(text);
    }
    
    /** gets text for txtReplaceString
     * @return String text
     */
    public String getReplaceString() {
        return txtReplaceString().getText();
    }
    
    /** sets text for txtReplaceString
     * @param text String text
     */
    public void setReplaceString(String text) {
        txtReplaceString().setText(text);
    }
    
    /** types text for txtReplaceString
     * @param text String text
     */
    public void typeReplaceString(String text) {
        txtReplaceString().typeText(text);
    }
    
    /** clicks on "Format..." JButton
     */
    public void format() {
        btFormat().push();
    }
    
    /** clicks on "Arguments..." JButton
     */
    public void arguments() {
        btArguments().push();
    }
    
    /** clicks on "Replace" JButton
     */
    public void replace() {
        btReplace().push();
    }
    
    /** clicks on "Skip" JButton
     */
    public void skip() {
        btSkip().push();
    }
    
    /** clicks on "Info" JButton
     */
    public void info() {
        btInfo().push();
    }
    
    /** clicks on "Close" JButton
     */
    public void close() {
        btClose().push();
    }
    
    /** clicks on "Help" JButton
     */
    public void help() {
        btHelp().push();
    }
    
    
    //*****************************************
    // High-level functionality definition part
    //*****************************************
    
    /** Performs verification of InternationalizeTestFrameFrameOperator by accessing all its components.
     */
    public void verify() {
        lblBundleName();
        txtBundleName();
        btBrowse();
        btNew();
        lblComment();
        txtComment();
        btMetalScrollButton();
        btMetalScrollButton2();
        lblKey();
        lblValue();
        txtValue();
        btMetalScrollButton3();
        btMetalScrollButton4();
        cboKey();
        txtReplaceString();
        lblReplaceString();
        btFormat();
        btArguments();
        btReplace();
        btSkip();
        btInfo();
        btClose();
        btHelp();
    }
    
    public void createNewBundle(String name) {
        btNew().pushNoBlock();
        NewBundleOperator nbo=new NewBundleOperator();
        
    }
    
    /** Performs simple test of InternationalizeTestFrameFrameOperator
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        new InternationalizeOperator().verify();
        System.out.println("InternationalizeOperator verification finished.");
    }
}

