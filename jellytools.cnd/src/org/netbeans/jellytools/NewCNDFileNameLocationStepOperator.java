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
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;

/**
 * Handle "Project Name And Location" panel of the New File wizard (CND).
 * Components on the panel differs according to type of Object selected.
 * This one contains only basic components.<br>
 * Usage:
 * <pre>
 *      NewFileWizardOperator wop = NewFileWizardOperator.invoke();
 *      wop.selectCategory("C++");
 *      wop.selectFileType("C++ Source File");
 *      wop.next();
 *      NewCNDFileNameLocationStepOperator op = new NewCNDFileNameLocationStepOperator();
 *      op.selectExtension("cpp");
 * </pre>
 *
 */
public class NewCNDFileNameLocationStepOperator extends NewFileWizardOperator {
    
    /** Components operators. */
    private JLabelOperator      _lblObjectName;
    private JTextFieldOperator  _txtObjectName;
    private JLabelOperator      _lblExtension;
    private JComboBoxOperator   _cboExtension;
    private JCheckBoxOperator   _cbSetExtensionAsDefault;
    private JLabelOperator      _lblProject;
    private JTextFieldOperator  _txtProject;
    private JLabelOperator      _lblFolder;
    private JTextFieldOperator  _txtFolder;
    private JButtonOperator     _btnBrowse;
    private JLabelOperator      _lblCreatedFile;
    private JTextFieldOperator  _txtCreatedFile;
    
    /** Waits for wizard with New title.  */
    public NewCNDFileNameLocationStepOperator() {
        super();
    }
    
    /** Waits for wizard with given title.
     * @param title title of wizard
     */
    public NewCNDFileNameLocationStepOperator(String title) {
        super(title);
    }
    
    /** Returns operator for first label with "Name"
     * @return JLabelOperator
     */
    public JLabelOperator lblObjectName() {
        if(_lblObjectName == null) {
            final String nameLabel = Bundle.getString("org.netbeans.modules.properties.Bundle", "PROP_name");
            final String nameAndLocationLabel = Bundle.getStringTrimmed("org.netbeans.modules.cnd.editor.filecreation.Bundle", "LBL_SimpleTargetChooserPanel_Name");
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
       
    /** Returns operator for label "Extension:"
     * @return JLabelOperator
     */
    public JLabelOperator lblExtension()
    {
        if( _lblExtension == null ) {
            _lblExtension = new JLabelOperator(this, Bundle.getStringTrimmed("org.netbeans.modules.cnd.editor.filecreation.Bundle",
                    "LBL_TargetChooser_Extension_Label"));
        }
        return _lblExtension;
    }

    /** Returns operator for "Extension:" combo box
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboExtension()
    {
        if( _cboExtension == null ) {
              _cboExtension = new JComboBoxOperator((JComboBox)lblExtension().getLabelFor());
        }
        return _cboExtension;
    }

    /** Returns operator for the "Set this Extension as Default" checkbox
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbSetExtensionAsDefault()
    {
        if( _cbSetExtensionAsDefault == null ) {
              _cbSetExtensionAsDefault = new JCheckBoxOperator(this, Bundle.getStringTrimmed("org.netbeans.modules.cnd.editor.filecreation.Bundle",
                      "ACSD_SetAsDefaultCheckBox"));
        }
        return _cbSetExtensionAsDefault;
    }
    
    /** Returns operator for first label with "Project"
     * @return JLabelOperator
     */
    public JLabelOperator lblProject() {
        if(_lblProject == null) {
            _lblProject = new JLabelOperator(this, Bundle.getStringTrimmed("org.netbeans.modules.cnd.editor.filecreation.Bundle",
                    "LBL_TargetChooser_Project_Label"));
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
    
     /** Returns operator of label "Folder:"
     * @return JLabelOperator
     */
    public JLabelOperator lblFolder() {
        if(_lblFolder == null) {
            _lblFolder = new JLabelOperator(this,
                    Bundle.getStringTrimmed("org.netbeans.modules.cnd.editor.filecreation.Bundle",
                    "LBL_TargetChooser_Folder_Label"));
        }
        return _lblFolder;
    }
    
    /** Returns operator for combo box Folder:
     * @return JComboBoxOperator
     */
    public JTextFieldOperator txtFolder() {
        if ( _txtFolder == null ) {
            _txtFolder = new JTextFieldOperator((JTextField)lblFolder().getLabelFor());
        }
        return _txtFolder;
    }

    /** Returns operator for the "Browse..." button
     * @return JButtonOperator
     */
    public JButtonOperator btnBrowse()
    {
        if ( _btnBrowse == null ) {
            _btnBrowse = new JButtonOperator(this, Bundle.getStringTrimmed("org.netbeans.modules.cnd.editor.filecreation.Bundle",
                    "LBL_TargetChooser_Browse_Button"));
        }
        return _btnBrowse;
    }


    /** Returns operator for label with "Created File:"
     * @return JLabelOperator
     */
    public JLabelOperator lblCreatedFile() {
        if(_lblCreatedFile == null) {
            _lblCreatedFile = new JLabelOperator(this, Bundle.getStringTrimmed("org.netbeans.modules.cnd.editor.filecreation.Bundle",
                    "LBL_TargetChooser_CreatedFile_Label"));
        }
        return _lblCreatedFile;
    }
    
    /** Returns operator of text field bind to lblCreatedFile
     * @return JTextOperator
     */
    public JTextFieldOperator txtCreatedFile() {
        if( _txtCreatedFile==null ) {            
                _txtCreatedFile = new JTextFieldOperator((JTextField)lblCreatedFile().getLabelFor());            
        }
        return _txtCreatedFile;
    }
  
    
    /** Selects given extension in combo box Package.
     * @param packageName name of package to be selected
     */
    public void selectExtension(String extension) {
        new EventTool().waitNoEvent(500);
        cboExtension().selectItem(extension);
    }
    
    /** Type given extension in combo box Extension.
     * @param packageName name of package
     */
    public void setExtension(String extension) {
        new EventTool().waitNoEvent(500);
        cboExtension().clearText();
        cboExtension().typeText(extension);
    }
    
    /** Sets given object name in the text field.
     * @param objectName name of object
     */
    public void setObjectName(String objectName) {
        txtObjectName().setText(objectName);
    }
       
    /** Performs verification by accessing all sub-components */
    public void verify() {
        lblObjectName();
        txtObjectName();
        lblCreatedFile();
        txtCreatedFile();
        cboExtension();
        txtProject();
        txtFolder();
        btnBrowse();
        cbSetExtensionAsDefault();
    }
}
