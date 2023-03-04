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
package org.netbeans.jellytools;

import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.JemmyException;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import javax.swing.JDialog;

/**
 * Handle "Source Packages and Folders" panel of the New Project wizard for
 * J2SE Ant Project.<br>
 * Usage:
 * <pre>
 * SourcePackageFoldersStepOperator spop = new SourcePackageFoldersStepOperator();
 * spop.addFolder();
 * 
 * spop.tblSourcePackageFolders().selectCell(0,0);
 * spop.remove();
 * spop.selectSourceLevel("JDK");
 * System.out.println(spop.lblOnlineError().getText());
 * </pre>
 *
 * @author tb115823
 */
public class SourcePackageFoldersStepOperator extends NewProjectWizardOperator {
    
    private JLabelOperator _lblSpecifyFolders;
    private JLabelOperator _lblSourcePackageFolders;
    private JLabelOperator _lblSourceLevel;
    private JComboBoxOperator _cboSourceLevel;
    private JButtonOperator _btAddFolder;
    private JButtonOperator _btRemove;
    private JLabelOperator _lblOnlineError;
    private JTableOperator _tblSourcePackageFolders;
           
    
    /** Tries to find "Specify folders containing source packages." JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblSpecifyFolders() {
        if (_lblSpecifyFolders==null) {
            String specifyFolders = Bundle.getString("org.netbeans.modules.java.freeform.ui.Bundle", "LBL_SourceFoldersPanel_jLabel1");
            _lblSpecifyFolders = new JLabelOperator(this, specifyFolders);// I18N
        }
        return _lblSpecifyFolders;
    }

    /** Tries to find "Source Package Folders:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblSourcePackageFolders() {
        if (_lblSourcePackageFolders==null) {
            String sourcePackageFolders = Bundle.getString("org.netbeans.modules.java.freeform.ui.Bundle", "LBL_SourceFoldersPanel_jLabel2");
            _lblSourcePackageFolders = new JLabelOperator(this, sourcePackageFolders);//I18N
        }
        return _lblSourcePackageFolders;
    }

    /** Tries to find "Source Level:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblSourceLevel() {
        if (_lblSourceLevel==null) {
            String sourcePackageFolders = Bundle.getString("org.netbeans.modules.java.freeform.ui.Bundle", "LBL_SourceFoldersPanel_jLabel3");
            _lblSourceLevel = new JLabelOperator(this, sourcePackageFolders);// I18N
        }
        return _lblSourceLevel;
    }

    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboSourceLevel() {
        if (_cboSourceLevel==null) {
            _cboSourceLevel = new JComboBoxOperator(this);
        }
        return _cboSourceLevel;
    }

    /** Tries to find "Add Folder..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btAddFolder() {
        if (_btAddFolder==null) {
            String addFolder = Bundle.getString("org.netbeans.modules.java.freeform.ui.Bundle", "BTN_SourceFoldersPanel_addFolder");
            _btAddFolder = new JButtonOperator(this, addFolder);// I18N
        }
        return _btAddFolder;
    }

    /** Tries to find "Remove" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btRemove() {
        if (_btRemove==null) {
            String remove = Bundle.getString("org.netbeans.modules.java.freeform.ui.Bundle", "BTN_SourceFoldersPanel_removeFolder");
            _btRemove = new JButtonOperator(this, remove);// I18N
        }
        return _btRemove;
    }

    /** Tries to find "OnlineError string" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblOnlineError() {
        if (_lblOnlineError==null) {
            _lblOnlineError = new JLabelOperator(this, 5);
        }
        return _lblOnlineError;
    }

    
    /** tries to find Source Package Folder table
     * @return JTableOperator
     */
    public JTableOperator tblSourcePackageFolders() {
        if ( _tblSourcePackageFolders==null ) {
            _tblSourcePackageFolders = new JTableOperator(this,0);
        }
        return _tblSourcePackageFolders;
    }

    
    
    /** returns selected item for cboSourceLevel
     * @return String item
     */
    public String getSelectedSourceLevel() {
        return cboSourceLevel().getSelectedItem().toString();
    }

    /** selects item for cboSourceLevel
     * @param item String item
     */
    public void selectSourceLevel(String item) {
        cboSourceLevel().selectItem(item);
    }

    /** clicks on "Add Folder..." JButton
     */
    public void addFolder() {
        btAddFolder().push();
    }

    /** clicks on "Remove" JButton
     */
    public void remove() {
        btRemove().push();
    }


    /** Performs verification of NewJ2SEAntProject by accessing all its components.
     */
    public void verify() {
        lblSpecifyFolders();
        lblSourcePackageFolders();
        lblSourceLevel();
        cboSourceLevel();
        btAddFolder();
        btRemove();
        lblOnlineError();
        tblSourcePackageFolders();
    }
    
}
