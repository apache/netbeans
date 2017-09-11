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

import org.netbeans.jellytools.Bundle;
import org.netbeans.jemmy.operators.*;
import org.netbeans.jemmy.util.NameComponentChooser;

/** Class implementing all necessary methods for handling "New Bundle" FileSelector.
 *
 * @author eh103527
 * @version 1.0
 */
public class NewBundleOperator extends JDialogOperator {
    
    /** Creates new NewBundle that can handle it.
     */
    public NewBundleOperator() {
        super(Bundle.getStringTrimmed("org.netbeans.modules.i18n.Bundle", "CTL_Template_Dialog_Title"));
    }
    
    private JLabelOperator _lblFilesystem;
    private JComboBoxOperator _cboFilesystem;
    private JTreeOperator _treeTreeView;
    private JLabelOperator _lblObjectName;
    private JTextFieldOperator _txtObjectName;
    private JButtonOperator _btOK;
    private JButtonOperator _btCancel;
    
    
    //******************************
    // Subcomponents definition part
    //******************************
    
    /** Tries to find " Filesystem:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblFilesystem() {
        if (_lblFilesystem==null) {
            _lblFilesystem = new JLabelOperator(this, Bundle.getStringTrimmed("org.netbeans.modules.i18n.Bundle", "CTL_Template_Dialog_RootTitle"));
        }
        return _lblFilesystem;
    }
    
    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboFilesystem() {
        if (_cboFilesystem==null) {
            _cboFilesystem = new JComboBoxOperator(this);
        }
        return _cboFilesystem;
    }
    
    /** Tries to find null TreeView$ExplorerTree in this dialog.
     * @return JTreeOperator
     */
    public JTreeOperator treeTreeView() {
        if (_treeTreeView==null) {
            _treeTreeView = new JTreeOperator(this);
        }
        return _treeTreeView;
    }
    
    /** Tries to find "Object  Name" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblObjectName() {
        if (_lblObjectName==null) {
            _lblObjectName = new JLabelOperator(this, Bundle.getStringTrimmed("org.netbeans.modules.i18n.Bundle", "LBL_TemplateName"));
        }
        return _lblObjectName;
    }
    
    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtObjectName() {
        if (_txtObjectName==null) {
            _txtObjectName = new JTextFieldOperator(this);
        }
        return _txtObjectName;
    }
    
    /** Tries to find "OK" ButtonBarButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btOK() {
        if (_btOK==null) {
            _btOK = new JButtonOperator(this, Bundle.getStringTrimmed("org.netbeans.modules.i18n.Bundle", "CTL_OKButton"));
        }
        return _btOK;
    }
    
    /** Tries to find "Cancel" ButtonBarButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btCancel() {
        if (_btCancel==null) {
            _btCancel = new JButtonOperator(this, Bundle.getStringTrimmed("org.netbeans.modules.i18n.Bundle", "CTL_CancelButton"));
        }
        return _btCancel;
    }
    
    
    //****************************************
    // Low-level functionality definition part
    //****************************************
    
    /** returns selected item for cboFilesystem
     * @return String item
     */
    public String getSelectedFilesystem() {
        return cboFilesystem().getSelectedItem().toString();
    }
    
    /** selects item for cboFilesystem
     * @param item String item
     */
    public void selectFilesystem(String item) {
        cboFilesystem().selectItem(item);
    }
    
    /** gets text for txtObjectName
     * @return String text
     */
    public String getObjectName() {
        return txtObjectName().getText();
    }
    
    /** sets text for txtObjectName
     * @param text String text
     */
    public void setObjectName(String text) {
        txtObjectName().setText(text);
    }
    
    /** types text for txtObjectName
     * @param text String text
     */
    public void typeObjectName(String text) {
        txtObjectName().typeText(text);
    }
    
    /** clicks on "OK" ButtonBarButton
     */
    public void ok() {
        btOK().pushNoBlock();
    }
    
    /** clicks on "Cancel" ButtonBarButton
     */
    public void cancel() {
        btCancel().push();
    }
    
    
    //*****************************************
    // High-level functionality definition part
    //*****************************************
    
    /** Performs verification of NewBundle by accessing all its components.
     */
    public void verify() {
        lblFilesystem();
        cboFilesystem();
        treeTreeView();
        lblObjectName();
        txtObjectName();
        btOK();
        btCancel();
    }
    
    public void createDefault() {
        
    }
    
    /** Performs simple test of NewBundle
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        new NewBundleOperator().verify();
        System.out.println("NewBundle verification finished.");
    }
}

