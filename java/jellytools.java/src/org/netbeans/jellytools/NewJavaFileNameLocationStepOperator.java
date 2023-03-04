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

import java.awt.Component;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import javax.swing.JTextField;
import org.netbeans.jemmy.ComponentChooser;

/**
 * Handle "Name And Location" panel of the New File wizard.
 * Components on the panel differs according to type of Object selected.
 * This one contains only basic components.<br>
 * Usage:
 * <pre>
 *      NewFileWizardOperator wop = NewFileWizardOperator.invoke();
 *      wop.selectCategory("Java Classes");
 *      wop.selectFileType("Java Class");
 *      wop.next();
 *      NewFileNameLocationStepOperator op = new NewFileNameLocationStepOperator();
 *      op.selectLocation("Source Packages");
 *      op.selectPackage("org.netbeans.jellytools");
 * </pre>
 *
 * @author tb115823, Jiri.Skrivanek@sun.com
 */
public class NewJavaFileNameLocationStepOperator extends NewFileWizardOperator {
    
    /** Components operators. */
    private JLabelOperator      _lblObjectName;
    private JTextFieldOperator  _txtObjectName;
    private JLabelOperator      _lblProject;
    private JTextFieldOperator  _txtProject;
    private JLabelOperator      _lblCreatedFile;
    private JTextFieldOperator  _txtCreatedFile;
    private JLabelOperator      _lblPackage;
    private JComboBoxOperator   _cboPackage;
    private JLabelOperator      _lblLocation;
    private JComboBoxOperator   _cboLocation;
    
    /** Waits for wizard with New title.  */
    public NewJavaFileNameLocationStepOperator() {
        super();
    }
    
    /** Waits for wizard with given title.
     * @param title title of wizard
     */
    public NewJavaFileNameLocationStepOperator(String title) {
        super(title);
    }
    
    /** Returns operator for first label with "Name"
     * @return JLabelOperator
     */
    public JLabelOperator lblObjectName() {
        if(_lblObjectName == null) {
            final String nameLabel = Bundle.getString("org.netbeans.modules.properties.Bundle", "PROP_name");
            final String nameAndLocationLabel = Bundle.getStringTrimmed("org.netbeans.modules.java.project.Bundle", "LBL_JavaTargetChooserPanelGUI_Name");
            _lblObjectName = new JLabelOperator(this, new JLabelOperator.JLabelFinder(new ComponentChooser() {
                public boolean checkComponent(Component comp) {
                    JLabel jLabel = (JLabel)comp;
                    String text = jLabel.getText();
                    if(text == null || nameAndLocationLabel.equals(text)) {
                        return false;
                    } else if(text.indexOf(nameLabel) > -1 && (jLabel.getLabelFor() == null || jLabel.getLabelFor() instanceof JTextField)) {
                        return true;
                    }
                    return false;
                }
                public String getDescription() {
                    return "JLabel containing Name and associated with text field";
                }
            }));
        }
        return _lblObjectName;
    }
    
    
    /** Returns operator of text field bind to lblObjectName
     * @return JTextOperator
     */
    public JTextFieldOperator txtObjectName() {
        if( _txtObjectName==null ) {
            if ( lblObjectName().getLabelFor()!=null ) {
                _txtObjectName = new JTextFieldOperator((JTextField)lblObjectName().getLabelFor());
            } else {
                _txtObjectName = new JTextFieldOperator(this,0);
            }
        }
        return _txtObjectName;
    }
    
    /** Returns operator for first label with "Project"
     * @return JLabelOperator
     */
    public JLabelOperator lblProject() {
        if(_lblProject == null) {
            _lblProject = new JLabelOperator(this, Bundle.getStringTrimmed("org.netbeans.modules.project.ui.Bundle",
                    "LBL_TemplateChooserPanelGUI_jLabel1"));
        }
        return _lblProject;
    }
    
    
    /** Returns operator of text field bind to lblProject
     * @return JTextOperator
     */
    public JTextFieldOperator txtProject() {
        if( _txtProject==null ) {
            if ( lblProject().getLabelFor()!=null ) {
                _txtProject = new JTextFieldOperator((JTextField)lblProject().getLabelFor());
            } else {
                _txtProject = new JTextFieldOperator(this,1);
            }
        }
        return _txtProject;
    }
    
    /** Returns operator for label with "Created File:"
     * @return JLabelOperator
     */
    public JLabelOperator lblCreatedFile() {
        if(_lblCreatedFile == null) {
            _lblCreatedFile = new JLabelOperator(this, Bundle.getStringTrimmed("org.netbeans.modules.java.project.Bundle","LBL_JavaTargetChooserPanelGUI_CreatedFile_Label"));
        }
        return _lblCreatedFile;
    }
    
    /** Returns operator of text field bind to lblCreatedFile
     * @return JTextOperator
     */
    public JTextFieldOperator txtCreatedFile() {
        if( _txtCreatedFile==null ) {
            if ( lblCreatedFile().getLabelFor()!=null ) {
                _txtCreatedFile = new JTextFieldOperator((JTextField)lblCreatedFile().getLabelFor());
            } else {
                _txtCreatedFile = new JTextFieldOperator(this,3);
            }
        }
        return _txtCreatedFile;
    }
    
    /** Returns operator of label "Location:"
     * @return JLabelOperator
     */
    public JLabelOperator lblLocation() {
        if(_lblLocation == null) {
            _lblLocation = new JLabelOperator(this,
                    Bundle.getStringTrimmed("org.netbeans.modules.java.project.Bundle", "LBL_JavaTargetChooserPanelGUI_jLabel1"));
        }
        return _lblLocation;
    }
    
    /** Returns operator for combo box Location:
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboLocation() {
        if ( _cboLocation==null ) {
            _cboLocation = new JComboBoxOperator((JComboBox)lblLocation().getLabelFor());
        }
        return _cboLocation;
    }
    
    /** Returns operator of label "Package:"
     * @return JLabelOperator
     */
    public JLabelOperator lblPackage() {
        if(_lblPackage == null) {
            _lblPackage = new JLabelOperator(this,
                    Bundle.getStringTrimmed("org.netbeans.modules.java.project.Bundle", "LBL_JavaTargetChooserPanelGUI_jLabel2"));
        }
        return _lblPackage;
    }
    
    /** returns operator for combo box Package:
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboPackage() {
        if ( _cboPackage==null ) {
            _cboPackage = new JComboBoxOperator((JComboBox)lblPackage().getLabelFor());
        }
        return _cboPackage;
    }
    
    /** Selects given package in combo box Package.
     * @param packageName name of package to be selected
     */
    public void selectPackage(String packageName) {
        new EventTool().waitNoEvent(500);
        cboPackage().selectItem(packageName);
    }
    
    /** Type given package in combo box Package.
     * @param packageName name of package
     */
    public void setPackage(String packageName) {
        new EventTool().waitNoEvent(500);
        cboPackage().clearText();
        cboPackage().typeText(packageName);
    }
    
    /** Sets given object name in the text field.
     * @param objectName name of object
     */
    public void setObjectName(String objectName) {
        txtObjectName().setText(objectName);
    }
    
    /** Selects Source Packages in combo box Location:.
     * Cannot set location directly by string because combo box has a model
     * with objects and not visible strings.
     */
    public void selectSourcePackagesLocation() {
        cboLocation().selectItem(0);
    }
    
    /** Selects Test Packages in combo box Location:
     * Cannot set location directly by string because combo box has a model
     * with objects and not visible strings.
     */
    public void selectTestPackagesLocation() {
        cboLocation().selectItem(1);
    }
    
    /** Performs verification by accessing all sub-components */
    public void verify() {
        lblObjectName();
        txtObjectName();
        lblCreatedFile();
        txtCreatedFile();
        cboLocation();
        cboPackage();
    }
}
