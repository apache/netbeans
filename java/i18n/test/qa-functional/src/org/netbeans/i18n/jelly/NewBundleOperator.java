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

