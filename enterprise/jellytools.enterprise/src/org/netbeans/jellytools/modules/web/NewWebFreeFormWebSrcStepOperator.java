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

package org.netbeans.jellytools.modules.web;

import javax.swing.JComboBox;
import javax.swing.JTextField;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jemmy.operators.*;


/** Class implementing all necessary methods for handling "New Web Application
 * with Existing Ant Script - Web Sources" wizard step.
 *
 * @author Martin.Schovanek@sun.com
 * @version 1.0
 */
public class NewWebFreeFormWebSrcStepOperator extends WizardOperator {

    /** Creates new NewWebFreeFormWebSrcStepOperator that can handle it.
     */
    public NewWebFreeFormWebSrcStepOperator() {
        super(Helper.freeFormWizardTitle());
    }
    
    private JLabelOperator _lblWebPagesFolder;
    private JTextFieldOperator _txtWebPagesFolder;
    private JButtonOperator _btBrowse;
    private JLabelOperator _lblContextPath;
    private JTextFieldOperator _txtContextPath;
    private JLabelOperator _lblJ2EESpecificationLevel;
    private JComboBoxOperator _cboJ2EESpecificationLevel;
    public static final String ITEM_J2EE14 = Bundle.getStringTrimmed(
            "org.netbeans.modules.web.freeform.ui.Bundle",
            "TXT_J2EESpecLevel_0");
    public static final String ITEM_J2EE13 = Bundle.getStringTrimmed(
            "org.netbeans.modules.web.freeform.ui.Bundle",
            "TXT_J2EESpecLevel_1");
    
    
    //******************************
    // Subcomponents definition part
    //******************************
    
    /** Tries to find "Web Pages Folder:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblWebPagesFolder() {
        if (_lblWebPagesFolder==null) {
            String webPagesFolder = Bundle.getStringTrimmed(
                    "org.netbeans.modules.web.freeform.ui.Bundle",
                    "LBL_WebPagesPanel_WebPagesLocation_Label");
            _lblWebPagesFolder = new JLabelOperator(this, webPagesFolder);
        }
        return _lblWebPagesFolder;
    }
    
    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtWebPagesFolder() {
        if (_txtWebPagesFolder==null) {
            if (lblWebPagesFolder().getLabelFor()!=null) {
                _txtWebPagesFolder = new JTextFieldOperator(
                        (JTextField) lblWebPagesFolder().getLabelFor());
            } else {
                _txtWebPagesFolder = new JTextFieldOperator(this);
            }
        }
        return _txtWebPagesFolder;
    }
    
    /** Tries to find "Browse..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btBrowse() {
        if (_btBrowse==null) {
            String browse = Bundle.getStringTrimmed(
                    "org.netbeans.modules.web.freeform.ui.Bundle",
                    "BTN_BasicProjectInfoPanel_browseAntScript");
            _btBrowse = new JButtonOperator(this, browse);
        }
        return _btBrowse;
    }
    
    /** Tries to find "Context Path:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblContextPath() {
        if (_lblContextPath==null) {
            String contextPath = Bundle.getStringTrimmed(
                    "org.netbeans.modules.web.freeform.ui.Bundle",
                    "LBL_WebPagesPanel_ContextPath_Label");
            _lblContextPath = new JLabelOperator(this, contextPath);
        }
        return _lblContextPath;
    }
    
    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtContextPath() {
        if (_txtContextPath==null) {
            if (lblContextPath().getLabelFor()!=null) {
                _txtContextPath = new JTextFieldOperator(
                        (JTextField) lblContextPath().getLabelFor());
            } else {
                _txtContextPath = new JTextFieldOperator(this, 1);
            }
        }
        return _txtContextPath;
    }
    
    /** Tries to find "J2EE Specification Level:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblJ2EESpecificationLevel() {
        if (_lblJ2EESpecificationLevel==null) {
            String j2eeLevel = Bundle.getStringTrimmed(
                    "org.netbeans.modules.web.freeform.ui.Bundle",
                    "LBL_WebPagesPanel_J2EESpecLevel_Label");
            _lblJ2EESpecificationLevel = new JLabelOperator(this, j2eeLevel);
        }
        return _lblJ2EESpecificationLevel;
    }
    
    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboJ2EESpecificationLevel() {
        if (_cboJ2EESpecificationLevel==null) {
            if (lblJ2EESpecificationLevel().getLabelFor()!=null) {
                _cboJ2EESpecificationLevel = new JComboBoxOperator(
                        (JComboBox) lblJ2EESpecificationLevel().getLabelFor());
            } else {
                _cboJ2EESpecificationLevel = new JComboBoxOperator(this);
            }
        }
        return _cboJ2EESpecificationLevel;
    }
    
    
    //****************************************
    // Low-level functionality definition part
    //****************************************
    
    /** gets text for txtWebPagesFolder
     * @return String text
     */
    public String getWebPagesFolder() {
        return txtWebPagesFolder().getText();
    }
    
    /** sets text for txtWebPagesFolder
     * @param text String text
     */
    public void setWebPagesFolder(String text) {
        txtWebPagesFolder().setText(text);
    }
    
    /** types text for txtWebPagesFolder
     * @param text String text
     */
    public void typeWebPagesFolder(String text) {
        txtWebPagesFolder().typeText(text);
    }
    
    /** clicks on "Browse..." JButton
     */
    public void browse() {
        btBrowse().push();
    }
    
    /** gets text for txtContextPath
     * @return String text
     */
    public String getContextPath() {
        return txtContextPath().getText();
    }
    
    /** sets text for txtContextPath
     * @param text String text
     */
    public void setContextPath(String text) {
        txtContextPath().setText(text);
    }
    
    /** types text for txtContextPath
     * @param text String text
     */
    public void typeContextPath(String text) {
        txtContextPath().typeText(text);
    }
    
    /** returns selected item for cboJ2EESpecificationLevel
     * @return String item
     */
    public String getSelectedJ2EESpecificationLevel() {
        return cboJ2EESpecificationLevel().getSelectedItem().toString();
    }
    
    /** selects item for cboJ2EESpecificationLevel
     * @param item String item
     */
    public void selectJ2EESpecificationLevel(String item) {
        cboJ2EESpecificationLevel().selectItem(item);
    }
    
    
    //*****************************************
    // High-level functionality definition part
    //*****************************************
    
    /** Performs verification of NewWebFreeFormWebSrcStepOperator by accessing all its components.
     */
    public void verify() {
        lblWebPagesFolder();
        txtWebPagesFolder();
        btBrowse();
        lblContextPath();
        txtContextPath();
        lblJ2EESpecificationLevel();
        cboJ2EESpecificationLevel();
    }
}
