/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.test.web;

import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jemmy.operators.*;

/**
 * Class implementing all necessary methods for handling "New Web Application"
 * NbDialog.
 *
 * @author dkolar
 * @author Jiri Skrivanek
 * @version 1.0
 */
public class NewWebProjectJSFFrameworkStepOperator extends NewProjectWizardOperator {

    /**
     * Creates new NewWebProjectJSFFrameworkStepOperator that can handle it.
     */
    public NewWebProjectJSFFrameworkStepOperator() {
        super("New Web Application");
    }
    private JTabbedPaneOperator _tbpJTabbedPane;
    private String _selectPageConfiguration = "Configuration";
    private JTextFieldOperator _txtServletURLMapping;
    private String _selectPageLibraries = "Libraries";
    private JRadioButtonOperator _rbRegisteredLibraries;
    private JComboBoxOperator _cboJComboBox;
    public static final String ITEM_JSF12 = "JSF 1.2";
    private JTextFieldOperator _txtLibraryName;
    private JTextFieldOperator _txtJSFDirectory;
    private JButtonOperator _btBrowse;
    private JRadioButtonOperator _rbCreateNewLibrary;
    private JLabelOperator _lblLibraryName;
    private JLabelOperator _lblJSFDirectory;
    private JTableOperator _tabSelectTheFrameworksYouWantToUseInYourWebApplication;
    private JRadioButtonOperator _rbServerLibrary;

    //******************************
    // Subcomponents definition part
    //******************************
    /*
     * Selects a JSF Framework to be added
     */
    public boolean setJSFFrameworkCheckbox() {
        Integer jsfRow = tabSelectTheFrameworksYouWantToUseInYourWebApplication().findCellRow("org.netbeans.modules.web.jsf");
        if (jsfRow != -1) {
            tabSelectTheFrameworksYouWantToUseInYourWebApplication().clickOnCell(jsfRow, 0);
            return true;
        } else {
            System.err.println("No JSF framework found!");
            return false;
        }
    }

    /*
     * Selects a Spring MVC Framework to be added
     */
    public boolean setSpringFrameworkCheckbox() {
        Integer springRow = tabSelectTheFrameworksYouWantToUseInYourWebApplication().findCellRow("org.netbeans.modules.spring.webmvc");
        if (springRow != -1) {
            tabSelectTheFrameworksYouWantToUseInYourWebApplication().clickOnCell(springRow, 0);
            return true;
        } else {
            System.err.println("No Spring framework found!");
            return false;
        }
    }

//*********************************************************************
    /** Tries to find null JTabbedPane in this dialog.
     * @return JTabbedPaneOperator
     */
    public JTabbedPaneOperator tbpJTabbedPane() {
        if (_tbpJTabbedPane == null) {
            _tbpJTabbedPane = new JTabbedPaneOperator(this);
        }
        return _tbpJTabbedPane;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtServletURLMapping() {
        if (_txtServletURLMapping == null) {
            _txtServletURLMapping = new JTextFieldOperator(selectPageConfiguration());
        }
        selectPageConfiguration();
        return _txtServletURLMapping;
    }

    /** Tries to find "Registered Libraries:" JRadioButton in this dialog.
     * @return JRadioButtonOperator
     */
    public JRadioButtonOperator rbRegisteredLibraries() {
        if (_rbRegisteredLibraries == null) {
            _rbRegisteredLibraries = new JRadioButtonOperator(selectPageLibraries(), "Registered Libraries:");
        }
        selectPageLibraries();
        return _rbRegisteredLibraries;
    }

    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboJComboBox() {
        if (_cboJComboBox == null) {
            _cboJComboBox = new JComboBoxOperator(selectPageLibraries());
        }
        selectPageLibraries();
        return _cboJComboBox;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtLibraryName() {
        if (_txtLibraryName == null) {
            _txtLibraryName = new JTextFieldOperator(selectPageLibraries());
        }
        selectPageLibraries();
        return _txtLibraryName;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtJSFDirectory() {
        if (_txtJSFDirectory == null) {
            _txtJSFDirectory = new JTextFieldOperator(selectPageLibraries(), 1);
        }
        selectPageLibraries();
        return _txtJSFDirectory;
    }

    /** Tries to find "Browse..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btBrowse() {
        if (_btBrowse == null) {
            _btBrowse = new JButtonOperator(selectPageLibraries(), "Browse...");
        }
        selectPageLibraries();
        return _btBrowse;
    }

    /** Tries to find "Create New Library" JRadioButton in this dialog.
     * @return JRadioButtonOperator
     */
    public JRadioButtonOperator rbCreateNewLibrary() {
        if (_rbCreateNewLibrary == null) {
            _rbCreateNewLibrary = new JRadioButtonOperator(selectPageLibraries(), org.netbeans.jellytools.Bundle.getString("org.netbeans.api.project.libraries.Bundle", "LibrariesCustomizer.createLibrary.title"));
        }
        selectPageLibraries();
        return _rbCreateNewLibrary;
    }

    /** Tries to find "Library Name:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblLibraryName() {
        if (_lblLibraryName == null) {
            _lblLibraryName = new JLabelOperator(selectPageLibraries(), "Library Name:");
        }
        selectPageLibraries();
        return _lblLibraryName;
    }

    /** Tries to find "JSF Directory:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblJSFDirectory() {
        if (_lblJSFDirectory == null) {
            _lblJSFDirectory = new JLabelOperator(selectPageLibraries(), "JSF Folder:");
        }
        selectPageLibraries();
        return _lblJSFDirectory;
    }

    /** Tries to find null JTable in this dialog.
     * @return JTableOperator
     */
    public JTableOperator tabSelectTheFrameworksYouWantToUseInYourWebApplication() {
        if (_tabSelectTheFrameworksYouWantToUseInYourWebApplication == null) {
            _tabSelectTheFrameworksYouWantToUseInYourWebApplication = new JTableOperator(this);
        }
        return _tabSelectTheFrameworksYouWantToUseInYourWebApplication;
    }

    /** Tries to find "Server Library" JRadioButton in this dialog.
     * @return JRadioButtonOperator
     */
    public JRadioButtonOperator rbServerLibrary() {
        if (_rbServerLibrary == null) {
            _rbServerLibrary = new JRadioButtonOperator(selectPageLibraries(), "Server Library:");
        }
        selectPageLibraries();
        return _rbServerLibrary;
    }

    //****************************************
    // Low-level functionality definition part
    //****************************************
    /** changes current selected tab
     * @param tabName String tab name */
    public void selectJTabbedPanePage(String tabName) {
        tbpJTabbedPane().selectPage(tabName);
    }

    /** changes current selected tab to "Configuration"
     * @return JTabbedPaneOperator of parent tabbed pane
     */
    public JTabbedPaneOperator selectPageConfiguration() {
        tbpJTabbedPane().selectPage(_selectPageConfiguration);
        return tbpJTabbedPane();
    }

    /** gets text for txtServletURLMapping
     * @return String text
     */
    public String getServletURLMapping() {
        return txtServletURLMapping().getText();
    }

    /** sets text for txtServletURLMapping
     * @param text String text
     */
    public void setServletURLMapping(String text) {
        txtServletURLMapping().setText(text);
    }

    /** types text for txtServletURLMapping
     * @param text String text
     */
    public void typeServletURLMapping(String text) {
        txtServletURLMapping().typeText(text);
    }

    /** changes current selected tab to "Libraries"
     * @return JTabbedPaneOperator of parent tabbed pane
     */
    public JTabbedPaneOperator selectPageLibraries() {
        tbpJTabbedPane().selectPage(_selectPageLibraries);
        return tbpJTabbedPane();
    }

    /** clicks on "Registered Libraries:" JRadioButton
     */
    public void registeredLibraries() {
        rbRegisteredLibraries().push();
    }

    /** returns selected item for cboJComboBox
     * @return String item
     */
    public String getSelectedJComboBox() {
        return cboJComboBox().getSelectedItem().toString();
    }

    /** selects item for cboJComboBox
     * @param item String item
     */
    public void selectJComboBox(String item) {
        cboJComboBox().selectItem(item);
    }

    /** gets text for txtLibraryName
     * @return String text
     */
    public String getLibraryName() {
        return txtLibraryName().getText();
    }

    /** sets text for txtLibraryName
     * @param text String text
     */
    public void setLibraryName(String text) {
        txtLibraryName().setText(text);
    }

    /** types text for txtLibraryName
     * @param text String text
     */
    public void typeLibraryName(String text) {
        txtLibraryName().typeText(text);
    }

    /** gets text for txtJSFDirectory
     * @return String text
     */
    public String getJSFDirectory() {
        return txtJSFDirectory().getText();
    }

    /** sets text for txtJSFDirectory
     * @param text String text
     */
    public void setJSFDirectory(String text) {
        txtJSFDirectory().setText(text);
    }

    /** types text for txtJSFDirectory
     * @param text String text
     */
    public void typeJSFDirectory(String text) {
        txtJSFDirectory().typeText(text);
    }

    /** clicks on "Browse..." JButton
     */
    public void browse() {
        btBrowse().push();
    }

    /** clicks on "Create New Library" JRadioButton
     */
    public void createNewLibrary() {
        rbCreateNewLibrary().push();
    }

    //*****************************************
    // High-level functionality definition part
    //*****************************************
    /** Performs verification of NewWebProjectJSFFrameworkStepOperator by accessing all its components.
     */
    @Override
    public void verify() {
        tbpJTabbedPane();
        txtServletURLMapping();
        rbRegisteredLibraries();
        cboJComboBox();
        txtLibraryName();
        txtJSFDirectory();
        btBrowse();
        rbCreateNewLibrary();
        lblLibraryName();
        lblJSFDirectory();
        tabSelectTheFrameworksYouWantToUseInYourWebApplication();
    }
    
    /** Returns error message shown in description area.
     * @return message in description area
     */
    public String getErrorMessage() {
        return new JTextPaneOperator(this).getToolTipText();
    }
}
