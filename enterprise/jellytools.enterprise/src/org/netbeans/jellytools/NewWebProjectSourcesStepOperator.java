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

import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import javax.swing.JTextField;

/**
 * Handle "Existing Sources and Libraries" panel of the New Web Project with
 * Existing Sources wizard.<br>
 * <ol>
 * <li>Label and TextField Web Pages Folder: <code>txtWebPagesFolder().setText()</code>
 * <li>Label and TextField Liraries Folder: <code>txtLirariesFolder().setText()</code>
 * <li>Button for browsing Project Location: <code>btBrowseLocation().pushNoBlock()</code>
 * <li>Label and TextField Project Folder: <code>txtProjectFolder().getText()</code>
 * <li>ComboBox SourceStructure: <code>cbSourceStructure().selectItem("item")</code>
 * <li>ComboBox Server: <code>cbSourceStructure().selectItem("item")</code>
 * <li>ComboBox J2EE Version: <code>cbJ2EEVersion().selectItem("item")</code>
 * <li>Label and TextField Context Path: <code>txtContextPath().getText()</code>
 * <li>CheckBox Set as Main Project: <code>cbSetAsMainProject().setSelected(true)</code>
 * </ol>
 * <p><b>Support for source and test packages folders lists is not implemented yet</b></p>
 * @author ms113234
 */
public class NewWebProjectSourcesStepOperator extends NewProjectWizardOperator {
    
    /** Components operators. */
    //Web Application
    private JLabelOperator      _lblWebPagesFolder;
    private JTextFieldOperator  _txtWebPagesFolder;
    private JButtonOperator     _btBrowseWebPages;
    private JLabelOperator      _lblLirariesFolder;
    private JTextFieldOperator  _txtLirariesFolder;
    private JButtonOperator     _btBrowseLibraries;
    private JButtonOperator     _btAddSourceFolder;
    private JButtonOperator     _btRemoveSourceFolder;
    private JButtonOperator     _btAddTestFolder;
    private JButtonOperator     _btRemoveTestFolder;
    
    
    /** Returns operator for label Web Pages Folder:
     * @return JLabelOperator
     */
    public JLabelOperator lblWebPagesFolder() {
        if(_lblWebPagesFolder == null) {
            _lblWebPagesFolder = new JLabelOperator(this,
                    Bundle.getStringTrimmed(
                    "org.netbeans.modules.web.project.ui.wizards.Bundle",
                    "LBL_IW_WebPagesLocation_Label"));
        }
        return _lblWebPagesFolder;
    }
    
    
    /** Returns operator of web pages folder textfield
     * @return JTextOperator
     */
    public JTextFieldOperator txtWebPagesFolder() {
        if(_txtWebPagesFolder == null) {
            if ( lblWebPagesFolder().getLabelFor()!=null ) {
                _txtWebPagesFolder = new JTextFieldOperator((JTextField)lblWebPagesFolder().getLabelFor());
            }
        }
        return _txtWebPagesFolder;
    }
    
    /** Returns operator for browse web pages folder button
     * @return JButtonOperator
     */
    public JButtonOperator btBrowseWebPages() {
        if ( _btBrowseWebPages==null ) {
            _btBrowseWebPages = new JButtonOperator(this,
                    Bundle.getStringTrimmed(
                    "org.netbeans.modules.web.project.ui.wizards.Bundle",
                    "LBL_NWP1_BrowseLocation_Button3"), 0);
        }
        return _btBrowseWebPages;
    }
    
    /** Returns operator for label Libraries Folder:
     * @return JLabelOperator
     */
    public JLabelOperator lblLibrariesFolder() {
        if(_lblLirariesFolder == null) {
            _lblLirariesFolder = new JLabelOperator(this,
                    Bundle.getStringTrimmed(
                    "org.netbeans.modules.web.project.ui.wizards.Bundle",
                    "LBL_IW_LibrariesLocation_Label"));
        }
        return _lblLirariesFolder;
    }
    
    
    /** Returns operator of libraries folder textfield
     * @return JTextOperator
     */
    public JTextFieldOperator txtLibrariesFolder() {
        if(_txtLirariesFolder == null) {
            if ( lblWebPagesFolder().getLabelFor()!=null ) {
                _txtLirariesFolder = new JTextFieldOperator((JTextField)lblLibrariesFolder().getLabelFor());
            }
        }
        return _txtLirariesFolder;
    }
    
    /** Returns operator for browse libraries folder button
     * @return JButtonOperator
     */
    public JButtonOperator btBrowseLibraries() {
        if ( _btBrowseLibraries==null ) {
            _btBrowseLibraries = new JButtonOperator(this,
                    Bundle.getStringTrimmed(
                    "org.netbeans.modules.web.project.ui.wizards.Bundle",
                    "LBL_NWP1_BrowseLocation_Button3"), 1);
        }
        return _btBrowseLibraries;
    }
    
    
    /** Returns operator for add source folder button
     * @return JButtonOperator
     */
    public JButtonOperator btAddSourceFolder() {
        if ( _btAddSourceFolder==null ) {
            _btAddSourceFolder = new JButtonOperator(this,
                    Bundle.getStringTrimmed(
                    "org.netbeans.modules.web.project.ui.wizards.Bundle",
                    "CTL_AddFolder"), 0);
        }
        return _btAddSourceFolder;
    }
    
    /** Returns operator for remove source folder button
     * @return JButtonOperator
     */
    public JButtonOperator btRemoveSourceFolder() {
        if ( _btRemoveSourceFolder==null ) {
            _btRemoveSourceFolder = new JButtonOperator(this,
                    Bundle.getStringTrimmed(
                    "org.netbeans.modules.web.project.ui.wizards.Bundle",
                    "CTL_RemoveFolder"), 0);
        }
        return _btRemoveSourceFolder;
    }
    
    /** Returns operator for add test folder button
     * @return JButtonOperator
     */
    public JButtonOperator btAddTestFolder() {
        if ( _btAddTestFolder==null ) {
            _btAddTestFolder = new JButtonOperator(this,
                    Bundle.getStringTrimmed(
                    "org.netbeans.modules.web.project.ui.wizards.Bundle",
                    "CTL_AddFolder"), 1);
        }
        return _btAddTestFolder;
    }
    
    /** Returns operator for remove test folder button
     * @return JButtonOperator
     */
    public JButtonOperator btRemoveTestFolder() {
        if ( _btRemoveTestFolder==null ) {
            _btRemoveTestFolder = new JButtonOperator(this,
                    Bundle.getStringTrimmed(
                    "org.netbeans.modules.web.project.ui.wizards.Bundle",
                    "CTL_RemoveFolder"), 1);
        }
        return _btRemoveTestFolder;
    }
    
    
    /** Performs verification by accessing all sub-components */
    public void verify() {
        lblWebPagesFolder();
        txtWebPagesFolder();
        lblLibrariesFolder();
        txtLibrariesFolder();
        btBrowseWebPages();
        btBrowseLibraries();
        btAddSourceFolder();
        btRemoveSourceFolder();
        btAddTestFolder();
        btRemoveTestFolder();
    }
}
