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

import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import javax.swing.JTextField;


/**
 * Handle "Name And Location" panel of the New Project wizard (CND).
 * Components on the panel differs according to type of project selected. 
 */
public class NewCNDProjectNameLocationStepOperator extends NewProjectWizardOperator {
    
    /** Components operators. */
    //C/C++ Dynamic/Static Library
    private JLabelOperator      _lblProjectName;
    private JTextFieldOperator  _txtProjectName;
    private JLabelOperator      _lblProjectLocation;
    private JTextFieldOperator  _txtProjectLocation;
    private JLabelOperator      _lblProjectFolder;
    private JTextFieldOperator  _txtProjectFolder;
    private JButtonOperator     _btBrowseLocation;
    private JCheckBoxOperator   _cbSetAsMainProject;
    //C/C++ Application
    private JCheckBoxOperator   _cbCreateMainFile;
    private JTextFieldOperator  _txtCreateMainFile;

    //TODO: add functionality for C/C++ project from existing code (or create a new operator)
    
    /** Returns operator for label Project Location
     * @return JLabelOperator
     */
    public JLabelOperator lblProjectLocation() {
        if(_lblProjectLocation == null) {
            _lblProjectLocation = new JLabelOperator(this,
                                    Bundle.getStringTrimmed("org.netbeans.modules.cnd.makeproject.ui.wizards.Bundle",
                                    "LBL_NWP1_ProjectLocation_Label"));
        }
        return _lblProjectLocation;
    }

    
    /** Returns operator of project location text field
     * @return JTextOperator
     */
    public JTextFieldOperator txtProjectLocation() {
        if(_txtProjectLocation == null) {
            if ( lblProjectLocation().getLabelFor()!=null ) {
                _txtProjectLocation = new JTextFieldOperator((JTextField)lblProjectLocation().getLabelFor());
            }
        }
        return _txtProjectLocation;
    }
    
    
    /** Returns operator for label Project Name
     * @return JLabelOperator
     */
    public JLabelOperator lblProjectName() {
        if(_lblProjectName == null) {
            _lblProjectName = new JLabelOperator(this,
                                    Bundle.getStringTrimmed("org.netbeans.modules.cnd.makeproject.ui.wizards.Bundle",
                                    "LBL_NWP1_ProjectName_Label"));
        }
        return _lblProjectName;
    }

    
    /** Returns operator of project name textfield
     * @return JTextOperator
     */
    public JTextFieldOperator txtProjectName() {
        if(_txtProjectName == null) {
            if ( lblProjectName().getLabelFor()!=null ) {
                _txtProjectName = new JTextFieldOperator((JTextField)lblProjectName().getLabelFor());
            }
        }
        return _txtProjectName;
    }
    
    
    /** Returns operator for label Project Folder
     * @return JLabelOperator
     */
    public JLabelOperator lblProjectFolder() {
        if(_lblProjectFolder == null) {
            _lblProjectFolder = new JLabelOperator(this,
                                    Bundle.getStringTrimmed("org.netbeans.modules.cnd.makeproject.ui.wizards.Bundle",
                                    "LBL_NWP1_CreatedProjectFolder_Lablel"));
        }
        return _lblProjectFolder;
    }

    
    /** Returns operator of project folder textfield
     * @return JTextOperator
     */
    public JTextFieldOperator txtProjectFolder() {
        if(_txtProjectFolder == null) {
            if ( lblProjectFolder().getLabelFor()!=null ) {
                _txtProjectFolder = new JTextFieldOperator((JTextField)lblProjectFolder().getLabelFor());
            }    
        }
        return _txtProjectFolder;
    }
    
    
    /** Returns operator for browse project location button
     * @return JButtonOperator
     */
    public JButtonOperator btBrowseProjectLocation() {
        if ( _btBrowseLocation==null ) {
             _btBrowseLocation = new JButtonOperator(this, Bundle.getStringTrimmed("org.netbeans.modules.cnd.makeproject.ui.wizards.Bundle",
                     "LBL_NWP1_BrowseLocation_Button"));
        }
        return _btBrowseLocation;
    }
    
    
    /** Returns operator for checkbox 'Set as Main Project'
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbSetAsMainProject() {
        if ( _cbSetAsMainProject==null ) {
            _cbSetAsMainProject = new JCheckBoxOperator(this,
                                        Bundle.getStringTrimmed("org.netbeans.modules.cnd.makeproject.ui.wizards.Bundle",
                                        "LBL_createMainfile"));
        }
        return _cbSetAsMainProject;    
    }
    
    
    /** Returns operator for checkbox 'Create Main File'
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbCreateMainFile() {
        if ( _cbCreateMainFile==null ) {
            _cbCreateMainFile = new JCheckBoxOperator(this,
                                        Bundle.getStringTrimmed("org.netbeans.modules.cnd.makeproject.ui.wizards.Bundle",
                                        "LBL_createMainfile"));
        }
        return _cbCreateMainFile;
    }
    
    
    /** Returns operator for text field 'Create Main File'
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtCreateMainFile() {
        if ( _txtCreateMainFile==null ) {
            _txtCreateMainFile = new JTextFieldOperator(this, 4);
        }
        return _txtCreateMainFile;
    }
        
    /** Performs verification by accessing all sub-components */    
    @Override
    public void verify() {
        //TODO fill this
    
    }
}
