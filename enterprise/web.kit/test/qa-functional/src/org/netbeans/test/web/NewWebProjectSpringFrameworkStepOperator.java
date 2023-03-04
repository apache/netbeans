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
package org.netbeans.test.web;

import org.netbeans.jellytools.NewProjectWizardOperator;
import org.netbeans.jemmy.operators.*;

/**
 * Class implementing all necessary methods for handling "New Web Application"
 * NbDialog.
 *
 * @author dkolar
 * @version 1.0
 */
public class NewWebProjectSpringFrameworkStepOperator extends NewProjectWizardOperator {

    /**
     * Creates new NewWebProjectSpringFrameworkStepOperator that can handle it.
     */
    public NewWebProjectSpringFrameworkStepOperator() {
        super("New Web Application");
    }
    private JLabelOperator _lblFrameworks;
    private JLabelOperator _lblSelectTheFrameworksYouWantToUseInYourWebApplication;
    private JLabelOperator _lblSpringWebMVC25Configuration;
    private JTabbedPaneOperator _tbpJTabbedPane;
    private String _selectPageConfiguration = "Configuration";
    private JLabelOperator _lblDispatcherName;
    private JLabelOperator _lblDispatcherMapping;
    private JTextFieldOperator _txtJTextField;
    private JTextFieldOperator _txtJTextField2;
    private String _selectPageLibraries = "Libraries";
    private JCheckBoxOperator _cbIncludeJSTL;
    private JTableOperator _tabSelectTheFrameworksYouWantToUseInYourWebApplication;
    private JLabelOperator _lblWizardDescriptor$FixedHeightLabel;

    //******************************
    // Subcomponents definition part
    //******************************
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

    /** Tries to find "Frameworks" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblFrameworks() {
        if (_lblFrameworks == null) {
            _lblFrameworks = new JLabelOperator(this, "Frameworks");
        }
        return _lblFrameworks;
    }

    /** Tries to find "Select the frameworks you want to use in your web application." JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblSelectTheFrameworksYouWantToUseInYourWebApplication() {
        if (_lblSelectTheFrameworksYouWantToUseInYourWebApplication == null) {
            _lblSelectTheFrameworksYouWantToUseInYourWebApplication = new JLabelOperator(this, "Select the frameworks you want to use in your web application.");
        }
        return _lblSelectTheFrameworksYouWantToUseInYourWebApplication;
    }

    /** Tries to find "Spring Web MVC 2.5 Configuration" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblSpringWebMVC25Configuration() {
        if (_lblSpringWebMVC25Configuration == null) {
            _lblSpringWebMVC25Configuration = new JLabelOperator(this, "Spring Web MVC 2.5 Configuration");
        }
        return _lblSpringWebMVC25Configuration;
    }

    /** Tries to find null JTabbedPane in this dialog.
     * @return JTabbedPaneOperator
     */
    public JTabbedPaneOperator tbpJTabbedPane() {
        if (_tbpJTabbedPane == null) {
            _tbpJTabbedPane = new JTabbedPaneOperator(this);
        }
        return _tbpJTabbedPane;
    }

    /** Tries to find "Dispatcher Name:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblDispatcherName() {
        if (_lblDispatcherName == null) {
            _lblDispatcherName = new JLabelOperator(selectPageConfiguration(), "Dispatcher Name:");
        }
        selectPageConfiguration();
        return _lblDispatcherName;
    }

    /** Tries to find "Dispatcher Mapping:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblDispatcherMapping() {
        if (_lblDispatcherMapping == null) {
            _lblDispatcherMapping = new JLabelOperator(selectPageConfiguration(), "Dispatcher Mapping:");
        }
        selectPageConfiguration();
        return _lblDispatcherMapping;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtJTextField() {
        if (_txtJTextField == null) {
            _txtJTextField = new JTextFieldOperator(selectPageConfiguration());
        }
        selectPageConfiguration();
        return _txtJTextField;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtJTextField2() {
        if (_txtJTextField2 == null) {
            _txtJTextField2 = new JTextFieldOperator(selectPageConfiguration(), 1);
        }
        selectPageConfiguration();
        return _txtJTextField2;
    }

    /** Tries to find "Include JSTL" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbIncludeJSTL() {
        if (_cbIncludeJSTL == null) {
            _cbIncludeJSTL = new JCheckBoxOperator(selectPageLibraries(), "Include JSTL");
        }
        selectPageLibraries();
        return _cbIncludeJSTL;
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

    /** Tries to find " " WizardDescriptor$FixedHeightLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblWizardDescriptor$FixedHeightLabel() {
        if (_lblWizardDescriptor$FixedHeightLabel == null) {
            _lblWizardDescriptor$FixedHeightLabel = new JLabelOperator(this, " ", 2);
        }
        return _lblWizardDescriptor$FixedHeightLabel;
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

    /** gets text for txtJTextField2
     * @return String text
     */
    public String getJTextField2() {
        return txtJTextField2().getText();
    }

    /** sets text for txtJTextField2
     * @param text String text
     */
    public void setJTextField2(String text) {
        txtJTextField2().setText(text);
    }

    /** types text for txtJTextField2
     * @param text String text
     */
    public void typeJTextField2(String text) {
        txtJTextField2().typeText(text);
    }

    /** changes current selected tab to "Libraries"
     * @return JTabbedPaneOperator of parent tabbed pane
     */
    public JTabbedPaneOperator selectPageLibraries() {
        tbpJTabbedPane().selectPage(_selectPageLibraries);
        return tbpJTabbedPane();
    }

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkIncludeJSTL(boolean state) {
        if (cbIncludeJSTL().isSelected() != state) {
            cbIncludeJSTL().push();
        }
    }

    //*****************************************
    // High-level functionality definition part
    //*****************************************
    /** Performs verification of NewWebProjectSpringFrameworkStepOperator by accessing all its components.
     */
    @Override
    public void verify() {
        lblFrameworks();
        lblSelectTheFrameworksYouWantToUseInYourWebApplication();
        lblSpringWebMVC25Configuration();
        tbpJTabbedPane();
        lblDispatcherName();
        lblDispatcherMapping();
        txtJTextField();
        txtJTextField2();
        cbIncludeJSTL();
        tabSelectTheFrameworksYouWantToUseInYourWebApplication();
        lblWizardDescriptor$FixedHeightLabel();
    }

    /** Returns error message shown in description area.
     * @return message in description area
     */
    public String getErrorMessage() {
        return new JTextPaneOperator(this).getToolTipText();
    }
}
