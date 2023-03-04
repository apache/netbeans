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

import javax.swing.JComboBox;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

/**
 * Handle "Server and Settings" panel of the New Web Project wizard.
 * Components on the panel differs according to type of project selected.<br><br>
 * <u>Web Application</u><br>
 * <ol>
 * <li>ComboBox Server: <code>cbSourceStructure().selectItem("item")</code>
 * <li>Button for Adding new Server: <code>btAdd().pushNoBlock()</code>
 * <li>ComboBox J2EE Version: <code>cbJ2EEVersion().selectItem("item")</code>
 * <li>Label and TextField Context Path: <code>txtContextPath().setText()</code>
 * </ol>
 * <u>Web Project with Existing Sources</u><br>
 * <ol>
 * <li>ComboBox Server: <code>cbSourceStructure().selectItem("item")</code>
 * <li>Button for Adding new Server: <code>btAdd().pushNoBlock()</code>
 * <li>ComboBox J2EE Version: <code>cbJ2EEVersion().selectItem("item")</code>
 * <li>Label and TextField Context Path: <code>txtContextPath().setText()</code>
 * </ol>
 * @author  dk198696
 */
public class NewWebProjectServerSettingsStepOperator extends NewProjectWizardOperator {

    private JLabelOperator _lblContextPath;
    private JLabelOperator _lblServer;
    private JLabelOperator _lblJavaEEVersion;
    private JTextFieldOperator _txtContextPath;
    private JCheckBoxOperator _cbCopyServerJARFilesToLibrariesFolder;
    private JComboBoxOperator _cboServer;
    private JButtonOperator _btAdd;
    private JComboBoxOperator _cboJavaEEVersion;
    public static final String ITEM_JAVAEE5 = "Java EE 5";
    public static final String ITEM_J2EE14 = "J2EE 1.4";
    public static final String ITEM_J2EE13 = "J2EE 1.3";
    private JLabelOperator _lblAddToEnterpriseApplication;
    private JComboBoxOperator _cboAddToEnterpriseApplication;
    private JLabelOperator _lblSharingJARLicenseRemark;

    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find "Context Path:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblContextPath() {
        if (_lblContextPath==null) {
            _lblContextPath = new JLabelOperator(this, "Context Path:");
        }
        return _lblContextPath;
    }

    /** Tries to find "Server:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblServer() {
        if (_lblServer==null) {
            _lblServer = new JLabelOperator(this, "Server:");
        }
        return _lblServer;
    }

    /** Tries to find "Java EE Version:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblJavaEEVersion() {
        if (_lblJavaEEVersion==null) {
            _lblJavaEEVersion = new JLabelOperator(this, "Java EE Version:");
        }
        return _lblJavaEEVersion;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtContextPath() {
        if (_txtContextPath==null) {
            _txtContextPath = new JTextFieldOperator(this);
        }
        return _txtContextPath;
    }

    //TODO: remove this, the checkbox is probably no longer in the dialog!
    /** Tries to find "Copy Server JAR Files to Libraries Folder" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbCopyServerJARFilesToLibrariesFolder() {
        if (_cbCopyServerJARFilesToLibrariesFolder==null) {
            _cbCopyServerJARFilesToLibrariesFolder = new JCheckBoxOperator(this, "Copy Server JAR Files to Libraries Folder");
        }
        return _cbCopyServerJARFilesToLibrariesFolder;
    }

    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboServer() {
        if (_cboServer==null) {
            JLabelOperator jlo =new JLabelOperator(this, "Server:");
            _cboServer = new JComboBoxOperator((JComboBox)jlo.getLabelFor());
        }
        return _cboServer;
    }

    /** Tries to find "Add..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btAdd() {
        if (_btAdd==null) {
            _btAdd = new JButtonOperator(this, "Add...");
        }
        return _btAdd;
    }

    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboJavaEEVersion() {
        if (_cboJavaEEVersion==null) {
            JLabelOperator jlo =new JLabelOperator(this, "Java EE Version:");
            _cboJavaEEVersion = new JComboBoxOperator((JComboBox)jlo.getLabelFor());
        }
        return _cboJavaEEVersion;
    }

    /** Tries to find "Add to Enterprise Application:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblAddToEnterpriseApplication() {
        if (_lblAddToEnterpriseApplication==null) {
            _lblAddToEnterpriseApplication = new JLabelOperator(this, "Add to Enterprise Application:");
        }
        return _lblAddToEnterpriseApplication;
    }

    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboAddToEnterpriseApplication() {
        if (_cboAddToEnterpriseApplication==null) {
            _cboAddToEnterpriseApplication = new JComboBoxOperator(this, 2);
        }
        return _cboAddToEnterpriseApplication;
    }

    /** Tries to find "<html>There may be legal considerations when sharing server JAR files. Be sure to check the license for your server to make sure you can distribute server JAR files to other developers.</html>" WizardDescriptor$FixedHeightLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblSharingJARLicenseRemark() {
        if (_lblSharingJARLicenseRemark == null) {
            _lblSharingJARLicenseRemark = new JLabelOperator(this, "There may be legal considerations when sharing server JAR files");
        }
        return _lblSharingJARLicenseRemark;
    }

    //****************************************
    // Low-level functionality definition part
    //****************************************

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

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkCopyServerJARFilesToLibrariesFolder(boolean state) {
        if (cbCopyServerJARFilesToLibrariesFolder().isSelected()!=state) {
            cbCopyServerJARFilesToLibrariesFolder().push();
        }
    }

    /** returns selected item for cboServer
     * @return String item
     */
    public String getSelectedServer() {
        return cboServer().getSelectedItem().toString();
    }

    /** selects item for cboServer
     * @param item String item
     */
    public void selectServer(String item) {
        cboServer().selectItem(item);
    }

    /** clicks on "Add..." JButton
     */
    public void add() {
        btAdd().pushNoBlock();
    }

    /** returns selected item for cboJavaEEVersion
     * @return String item
     */
    public String getSelectedJavaEEVersion() {
        return cboJavaEEVersion().getSelectedItem().toString();
    }

    /** selects item for cboJavaEEVersion
     * @param item String item
     */
    public void selectJavaEEVersion(String item) {
        cboJavaEEVersion().selectItem(item);
    }

    /** returns selected item for cboAddToEnterpriseApplication
     * @return String item
     */
    public String getSelectedAddToEnterpriseApplication() {
        return cboAddToEnterpriseApplication().getSelectedItem().toString();
    }

    /** selects item for cboAddToEnterpriseApplication
     * @param item String item
     */
    public void selectAddToEnterpriseApplication(String item) {
        cboAddToEnterpriseApplication().selectItem(item);
    }

    //*****************************************
    // High-level functionality definition part
    //*****************************************

    /** Performs verification of NewWebApplication by accessing all its components.
     */
    @Override
    public void verify() {
        lblContextPath();
        lblServer();
        lblJavaEEVersion();
        txtContextPath();
        cbCopyServerJARFilesToLibrariesFolder();
        cboServer();
        btAdd();
        cboJavaEEVersion();
        lblAddToEnterpriseApplication();
        cboAddToEnterpriseApplication();
        lblSharingJARLicenseRemark();
        btBack();
        btNext();
        btFinish();
        btCancel();
        btHelp();
    }
}

