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

import java.awt.Component;
import javax.swing.JComboBox;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NewFileWizardOperator;
import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jemmy.operators.*;


/**
 * Handle "Name And Location" panel of the New JSP File wizard.<br>
 * Usage:
 * <pre>
 *      NewFileWizardOperator wop = NewFileWizardOperator.invoke();
 *      wop.selectCategory("Web");
 *      wop.selectFileType("JSP");
 *      wop.next();
 *      NewJspFileNameStepOperator op = new NewJspFileNameStepOperator();
 *      op.setJspFileName("index1");
 *      op.finish();
 * </pre>
 *
 * @author Martin.Schovanek@sun.com
 */
public class NewJspFileNameStepOperator extends WizardOperator {
    private JLabelOperator _lblNameAndLocation;
    private JLabelOperator _lblJSPFileName;
    private JTextFieldOperator _txtJSPFileName;
    private JLabelOperator _lblProject;
    private JTextFieldOperator _txtProject;
    private JLabelOperator _lblLocation;
    private JComboBoxOperator _cboLocation;
    private JLabelOperator _lblFolder;
    private JTextFieldOperator _txtFolder;
    private JButtonOperator _btBrowse;
    private JLabelOperator _lblCreatedFile;
    private JTextFieldOperator _txtCreatedFile;
    private JLabelOperator _lblOptions;
    private JRadioButtonOperator _rbJSPFileStandardSyntax;
    private JRadioButtonOperator _rbJSPDocumentXMLSyntax;
    private JCheckBoxOperator _cbCreateAsAJSPSegment;
    private JLabelOperator _lblDescription;
    private JTextAreaOperator _txtDescription;
    
    private static final String JSP_FILE = Bundle.getStringTrimmed(
            "org.netbeans.modules.web.core.Bundle",
            "Templates/JSP_Servlet/JSP.jsp");
    private static final String NEW = Bundle.getStringTrimmed(
            "org.netbeans.modules.project.ui.Bundle",
            "LBL_NewFileWizard_Subtitle");
    
    /**
     * Creates new NewJspFileNameStepOperator that can handle it.
     */
    public NewJspFileNameStepOperator() {
        super(Bundle.getStringTrimmed(
                "org.netbeans.modules.project.ui.Bundle",
                "LBL_NewFileWizard_MessageFormat",
                new Object[] {NEW, JSP_FILE}));
    }

    /** Invokes dialog from main menu "File|New..." and selects Web/JSP template.
     * @return instance of NewJspFileNameStepOperator
     */
    public static NewJspFileNameStepOperator invoke() {
                NewFileWizardOperator wizarOperator = NewFileWizardOperator.invoke();
        wizarOperator.selectCategory(Bundle.getStringTrimmed(
                "org.netbeans.modules.web.core.Bundle",
                "Templates/JSP_Servlet"));
        wizarOperator.selectFileType(Bundle.getStringTrimmed(
                "org.netbeans.modules.web.core.Bundle",
                "Templates/JSP_Servlet/JSP.jsp"));
        wizarOperator.next();
        return new NewJspFileNameStepOperator();
    }
    
    //******************************
    // Subcomponents definition part
    //******************************
    
    /** Tries to find "Name and Location" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblNameAndLocation() {
        if (_lblNameAndLocation==null) {
            _lblNameAndLocation = new JLabelOperator(this, Bundle.getStringTrimmed(
                    "org.netbeans.modules.web.wizards.Bundle",
                    "TITLE_name_location"));
        }
        return _lblNameAndLocation;
    }
    
    /** Tries to find "JSP File Name:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblJSPFileName() {
        if (_lblJSPFileName==null) {
            _lblJSPFileName = new JLabelOperator(this, Bundle.getStringTrimmed(
                    "org.netbeans.modules.web.wizards.Bundle",
                    "LBL_JspName"));
        }
        return _lblJSPFileName;
    }
    
    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtJSPFileName() {
        if (_txtJSPFileName==null) {
            Component comp = lblJSPFileName().getLabelFor();
            if (comp != null) {
                _txtJSPFileName = new JTextFieldOperator((JTextField) comp);
            } else {
                _txtJSPFileName = new JTextFieldOperator(this);
            }
        }
        return _txtJSPFileName;
    }
    
    /** Tries to find "Project:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblProject() {
        if (_lblProject==null) {
            _lblProject = new JLabelOperator(this, Bundle.getStringTrimmed(
                    "org.netbeans.modules.web.wizards.Bundle",
                    "LBL_Project"));
        }
        return _lblProject;
    }
    
    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtProject() {
        if (_txtProject==null) {
            Component comp = lblProject().getLabelFor();
            if (comp != null) {
                _txtProject = new JTextFieldOperator((JTextField) comp);
            } else {
                _txtProject = new JTextFieldOperator(this, 1);
            }
        }
        return _txtProject;
    }
    
    /** Tries to find "Location:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblLocation() {
        if (_lblLocation==null) {
            _lblLocation = new JLabelOperator(this, Bundle.getStringTrimmed(
                    "org.netbeans.modules.web.wizards.Bundle",
                    "LBL_Location"));
        }
        return _lblLocation;
    }
    
    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboLocation() {
        if (_cboLocation==null) {
            Component comp = lblLocation().getLabelFor();
            if (comp != null) {
                _cboLocation = new JComboBoxOperator((JComboBox) comp);
            } else {
                _cboLocation = new JComboBoxOperator(this);
            }
        }
        return _cboLocation;
    }
    
    /** Tries to find "Folder:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblFolder() {
        if (_lblFolder==null) {
            _lblFolder = new JLabelOperator(this, Bundle.getStringTrimmed(
                    "org.netbeans.modules.web.wizards.Bundle",
                    "LBL_Folder"));
        }
        return _lblFolder;
    }
    
    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtFolder() {
        if (_txtFolder==null) {
            Component comp = lblFolder().getLabelFor();
            if (comp != null) {
                _txtFolder = new JTextFieldOperator((JTextField) comp);
            } else {
                _txtFolder = new JTextFieldOperator(this, 2);
            }
        }
        return _txtFolder;
    }
    
    /** Tries to find "Browse..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btBrowse() {
        if (_btBrowse==null) {
            _btBrowse = new JButtonOperator(this, Bundle.getStringTrimmed(
                    "org.netbeans.modules.web.wizards.Bundle",
                    "LBL_Browse"));
        }
        return _btBrowse;
    }
    
    /** Tries to find "Created File:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblCreatedFile() {
        if (_lblCreatedFile==null) {
            _lblCreatedFile = new JLabelOperator(this, Bundle.getStringTrimmed(
                    "org.netbeans.modules.web.wizards.Bundle",
                    "LBL_CreatedFile"));
        }
        return _lblCreatedFile;
    }
    
    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtCreatedFile() {
        if (_txtCreatedFile==null) {
            Component comp = lblCreatedFile().getLabelFor();
            if (comp != null) {
                _txtCreatedFile = new JTextFieldOperator((JTextField) comp);
            } else {
                _txtCreatedFile = new JTextFieldOperator(this, 3);
            }
        }
        return _txtCreatedFile;
    }
    
    /** Tries to find "Options:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblOptions() {
        if (_lblOptions==null) {
            _lblOptions = new JLabelOperator(this, Bundle.getStringTrimmed(
                    "org.netbeans.modules.web.wizards.Bundle",
                    "LBL_Options"));
        }
        return _lblOptions;
    }
    
    /** Tries to find "JSP File (Standard Syntax)" JRadioButton in this dialog.
     * @return JRadioButtonOperator
     */
    public JRadioButtonOperator rbJSPFileStandardSyntax() {
        if (_rbJSPFileStandardSyntax==null) {
            _rbJSPFileStandardSyntax = new JRadioButtonOperator(this,
                    Bundle.getStringTrimmed(
                    "org.netbeans.modules.web.wizards.Bundle",
                    "OPT_JspSyntax"));
        }
        return _rbJSPFileStandardSyntax;
    }
    
    /** Tries to find "JSP Document (XML Syntax)" JRadioButton in this dialog.
     * @return JRadioButtonOperator
     */
    public JRadioButtonOperator rbJSPDocumentXMLSyntax() {
        if (_rbJSPDocumentXMLSyntax==null) {
            _rbJSPDocumentXMLSyntax = new JRadioButtonOperator(this,
                    Bundle.getStringTrimmed(
                    "org.netbeans.modules.web.wizards.Bundle",
                    "OPT_XmlSyntax"));
        }
        return _rbJSPDocumentXMLSyntax;
    }
    
    /** Tries to find "Create as a JSP Segment" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbCreateAsAJSPSegment() {
        if (_cbCreateAsAJSPSegment==null) {
            _cbCreateAsAJSPSegment = new JCheckBoxOperator(this,
                    Bundle.getStringTrimmed(
                    "org.netbeans.modules.web.wizards.Bundle",
                    "OPT_JspSegment"));
        }
        return _cbCreateAsAJSPSegment;
    }
    
    /** Tries to find "Description:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblDescription() {
        if (_lblDescription==null) {
            _lblDescription = new JLabelOperator(this, Bundle.getStringTrimmed(
                    "org.netbeans.modules.web.wizards.Bundle",
                    "LBL_description"));
        }
        return _lblDescription;
    }
    
    /** Tries to find null JTextArea in this dialog.
     * @return JTextAreaOperator
     */
    public JTextAreaOperator txtDescription() {
        if (_txtDescription==null) {
            Component comp = lblDescription().getLabelFor();
            if (comp != null) {
                _txtDescription = new JTextAreaOperator((JTextArea) comp);
            } else {
                _txtDescription = new JTextAreaOperator(this);
            }
        }
        return _txtDescription;
    }
    
    //****************************************
    // Low-level functionality definition part
    //****************************************
    
    /** gets text for txtJSPFileName
     * @return String text
     */
    public String getJSPFileName() {
        return txtJSPFileName().getText();
    }
    
    /** sets text for txtJSPFileName
     * @param text String text
     */
    public void setJSPFileName(String text) {
        txtJSPFileName().setText(text);
    }
    
    /** types text for txtJSPFileName
     * @param text String text
     */
    public void typeJSPFileName(String text) {
        txtJSPFileName().typeText(text);
    }
    
    /** gets text for txtProject
     * @return String text
     */
    public String getProject() {
        return txtProject().getText();
    }
    
    /** sets text for txtProject
     * @param text String text
     */
    public void setProject(String text) {
        txtProject().setText(text);
    }
    
    /** types text for txtProject
     * @param text String text
     */
    public void typeProject(String text) {
        txtProject().typeText(text);
    }
    
    /** returns selected item for cboLocation
     * @return String item
     */
    public String getSelectedLocation() {
        return cboLocation().getSelectedItem().toString();
    }
    
    /** selects item for cboLocation
     * @param item String item
     */
    public void selectLocation(String item) {
        cboLocation().selectItem(item);
    }
    
    /** gets text for txtFolder
     * @return String text
     */
    public String getFolder() {
        return txtFolder().getText();
    }
    
    /** sets text for txtFolder
     * @param text String text
     */
    public void setFolder(String text) {
        txtFolder().setText(text);
    }
    
    /** types text for txtFolder
     * @param text String text
     */
    public void typeFolder(String text) {
        txtFolder().typeText(text);
    }
    
    /** clicks on "Browse..." JButton
     */
    public void browse() {
        btBrowse().push();
    }
    
    /** gets text for txtCreatedFile
     * @return String text
     */
    public String getCreatedFile() {
        return txtCreatedFile().getText();
    }
    
    /** sets text for txtCreatedFile
     * @param text String text
     */
    public void setCreatedFile(String text) {
        txtCreatedFile().setText(text);
    }
    
    /** types text for txtCreatedFile
     * @param text String text
     */
    public void typeCreatedFile(String text) {
        txtCreatedFile().typeText(text);
    }
    
    /** clicks on "JSP File (Standard Syntax)" JRadioButton
     */
    public void jSPFileStandardSyntax() {
        rbJSPFileStandardSyntax().push();
    }
    
    /** clicks on "JSP Document (XML Syntax)" JRadioButton
     */
    public void jSPDocumentXMLSyntax() {
        rbJSPDocumentXMLSyntax().push();
    }
    
    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkCreateAsAJSPSegment(boolean state) {
        if (cbCreateAsAJSPSegment().isSelected()!=state) {
            cbCreateAsAJSPSegment().push();
        }
    }
    
    /** gets text for txtDescription
     * @return String text
     */
    public String getDescription() {
        return txtDescription().getText();
    }
    
    /** sets text for txtDescription
     * @param text String text
     */
    public void setDescription(String text) {
        txtDescription().setText(text);
    }
    
    /** types text for txtDescription
     * @param text String text
     */
    public void typeDescription(String text) {
        txtDescription().typeText(text);
    }
    
    //*****************************************
    // High-level functionality definition part
    //*****************************************
    
    /**
     * Performs verification of NewJspFileNameStepOperator by accessing all its components.
     */
    @Override
    public void verify() {
        lblNameAndLocation();
        lblJSPFileName();
        txtJSPFileName();
        lblProject();
        txtProject();
        lblLocation();
        cboLocation();
        lblFolder();
        txtFolder();
        btBrowse();
        lblCreatedFile();
        txtCreatedFile();
        lblOptions();
        rbJSPFileStandardSyntax();
        rbJSPDocumentXMLSyntax();
        cbCreateAsAJSPSegment();
        lblDescription();
        txtDescription();
    }
}

