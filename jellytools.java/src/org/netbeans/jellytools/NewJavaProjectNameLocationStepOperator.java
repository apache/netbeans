/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.jellytools;

import javax.swing.JTextField;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

/**
 * Handle "Name And Location" panel of the New Project wizard.
 * Components on the panel differs according to type of project selected.<br><br>
 * <u>Java Application</u><br>
 * <ol>
 * <li>Label and TextField Project Name: <code>txtProjectName().setText()</code>
 * <li>Label and TextField Project Location: <code>txtProjectLocation().setText()</code>
 * <li>Label and TextField Project Folder: <code>txtProjectFolder().getText()</code>
 * <li>Button for browsing Project Location: <code>btBrowseProjectLocation().pushNoBlock()</code>
 * <li>CheckBox Set as Main Project: <code>cbSetAsMainProject().setSelected(true)</code>
 * <li>CheckBox Create Main Class: <code>cbCreateMainClass().setSelected(true)</code>
 * </ol>
 * <u>Java Class Library</u><br>
 * <ol>
 * <li>Label and TextField Project Name: <code>txtProjectName().setText()</code>
 * <li>Label and TextField Project Location: <code>txtProjectLocation().setText()</code>
 * <li>Label and TextField Project Folder: <code>txtProjectFolder().getText()</code>
 * <li>Button for browsing Project Location: <code>btBrowseProjectLocation().pushNoBlock()</code>
 * </ol>
 * <u>Java Project With Existing Ant script</u><br>
 * <ol>
 * <li>Label and TextField Location: <code>txtLocation().setText()</code>
 * <li>Label and TextField Build Script: <code>txtBuildScript().setText()</code>
 * <li>Label and TextField Project Name: <code>txtProjectName().setText()</code>
 * <li>Label and TextField Project Folder: <code>txtProjectFolder().setText()</code>
 * <li>Button Browse... for browsing Location <code>btBrowseLocation().pushNoBlock()</code>
 * <li>Button Browse... for browsing Build Script <code>btBrowseBuildScript().pushNoBlock()</code>
 * <li>Button Browse... for browsing Project Folder <code>btBrowseProjectFolder().pushNoBlock()</code>
 * <li>CheckBox Set as Main Project <code>cbSetAsMainProject().setSelected(true)</code> 
 * </ol>
 * <u>Java project With Existing Sources</u><br>
 * <ol>
 * <li>Label and TextField Project Name: <code>txtProjectName().setText()</code>
 * <li>Label and TextField Project Folder: <code>txtProjectFolder().setText()</code>
 * <li>Button for browsing Project location: <code>btBrowseProjectLocation().pushNoBlock()</code>
 * </ol>
 * 
 */
public class NewJavaProjectNameLocationStepOperator extends NewProjectWizardOperator {
    
    /** Components operators. */
    //Java Class Library
    private JLabelOperator      _lblProjectName;
    private JTextFieldOperator  _txtProjectName;
    private JLabelOperator      _lblProjectLocation;
    private JTextFieldOperator  _txtProjectLocation;
    private JLabelOperator      _lblProjectFolder;
    private JTextFieldOperator  _txtProjectFolder;
    private JButtonOperator     _btBrowseLocation;
    //Java Application
    private JCheckBoxOperator   _cbCreateMainClass;
    private JTextFieldOperator  _txtCreateMainClass;
    //Java Project With Existing Ant script
    private JLabelOperator      _lblLocation;
    private JTextFieldOperator  _txtLocation;
    private JLabelOperator      _lblBuildScript;
    private JTextFieldOperator  _txtBuildScript;
    private JButtonOperator     _btBrowseBuildScript;
    private JButtonOperator     _btBrowseProjectFolder;
    
    /** Returns operator for label Project Location
     * @return JLabelOperator
     */
    public JLabelOperator lblProjectLocation() {
        if(_lblProjectLocation == null) {
            _lblProjectLocation = new JLabelOperator(this,
                                    Bundle.getStringTrimmed("org.netbeans.modules.java.j2seproject.ui.wizards.Bundle",
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
                                    Bundle.getStringTrimmed("org.netbeans.modules.java.j2seproject.ui.wizards.Bundle",
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
                                    Bundle.getStringTrimmed("org.netbeans.modules.java.j2seproject.ui.wizards.Bundle",
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
             _btBrowseLocation = new JButtonOperator(this,Bundle.getStringTrimmed("org.netbeans.modules.java.j2seproject.ui.wizards.Bundle",
                                        "LBL_NWP1_BrowseLocation_Button"));
        }
        return _btBrowseLocation;
    }
    
    
    /** Returns operator for browse location button in Java Project with existing 
     * Ant script wizard.
     * @return JButtonOperator
     */
    public JButtonOperator btBrowseLocation() {
        if ( _btBrowseLocation==null ) {
            _btBrowseLocation = new JButtonOperator(
                                        this,
                                        Bundle.getStringTrimmed("org.netbeans.modules.ant.freeform.ui.Bundle",
                                                                "BTN_BasicProjectInfoPanel_browseProjectLocation"), 
                                        2);
        }
        return _btBrowseLocation;
    }
    
    /** Returns operator for checkbox 'Create Main Class'
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbCreateMainClass() {
        if ( _cbCreateMainClass==null ) {
            _cbCreateMainClass = new JCheckBoxOperator(this,
                                        Bundle.getStringTrimmed("org.netbeans.modules.java.j2seproject.ui.wizards.Bundle",
                                                                "LBL_createMainCheckBox"));
        }
        return _cbCreateMainClass;    
    }
    
    
    /** Returns operator for text field 'Create Main Class'
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtCreateMainClass() {
        if ( _txtCreateMainClass==null ) {
            _txtCreateMainClass = new JTextFieldOperator(this, 3);
        }
        return _txtCreateMainClass;
    }
    
    
    /** Returns operator for label 'Location:' in Java Project with existing 
     * Ant script wizard.
     * @return JLabelOperator
     */
    public JLabelOperator lblLocation() {
        if(_lblLocation == null) {
           _lblLocation = new JLabelOperator(this,
                                    Bundle.getStringTrimmed("org.netbeans.modules.ant.freeform.ui.Bundle",
                                                            "LBL_BasicProjectInfoPanel_jLabel6"));
        }
        return _lblLocation;
    }
    
    /** Returns operator of Location: text field in Java Project with existing 
     * Ant script wizard.
     * @return JTextOperator
     */
    public JTextFieldOperator txtLocation() {
        if(_txtLocation == null) {
            if ( lblLocation().getLabelFor()!=null ) {
                _txtLocation = new JTextFieldOperator((JTextField)lblLocation().getLabelFor());
            }
        }
        return _txtLocation;
    }
    
    
    /** Returns operator for label 'Build Script:' in Java Project with existing 
     * Ant script wizard.
     * @return JLabelOperator
     */
    public JLabelOperator lblBuildScript() {
        if(_lblBuildScript == null) {
            _lblBuildScript = new JLabelOperator(this,
                                    Bundle.getStringTrimmed("org.netbeans.modules.ant.freeform.ui.Bundle",
                                                            "LBL_BasicProjectInfoPanel_jLabel2"));
        }
        return _lblBuildScript;
    }
    
    /** Returns operator of 'Build Script:' text field in Java Project with existing 
     * Ant script wizard.
     * @return JTextOperator
     */
    public JTextFieldOperator txtBuildScript() {
        if(_txtBuildScript == null) {
            if (  lblBuildScript().getLabelFor()!=null ) {
                _txtBuildScript = new JTextFieldOperator((JTextField)lblBuildScript().getLabelFor());
            }    
        }
        return _txtBuildScript;
    }
    
    
    /** Returns operator for browse Build Script button in Java Project with existing 
     * Ant script wizard.
     * @return JButtonOperator
     */
    public JButtonOperator btBrowseBuildScript() {
        if ( _btBrowseBuildScript==null ) {
            _btBrowseBuildScript = new JButtonOperator(
                                this,
                                Bundle.getStringTrimmed("org.netbeans.modules.ant.freeform.ui.Bundle",
                                                        "BTN_BasicProjectInfoPanel_browseAntScript"), 
                                0);
        }
        return _btBrowseBuildScript;
    }
    
    
    /** Returns operator for browse Project Folder button in Java Project with existing 
     * Ant script wizard.
     * @return JButtonOperator
     */
    public JButtonOperator btBrowseProjectFolder() {
        if ( _btBrowseProjectFolder==null ) {
            _btBrowseProjectFolder = new JButtonOperator(
                                            this, 
                                            Bundle.getStringTrimmed("org.netbeans.modules.ant.freeform.ui.Bundle",
                                                                    "BTN_BasicProjectInfoPanel_browseProjectFolder"), 
                                            1);
        }
        return _btBrowseProjectFolder;
    }
    
    /** Performs verification by accessing all sub-components */    
    @Override
    public void verify() {
        /*
        lblProjectName();
        txtProjectName();
        lblProjectLocation();
        txtProjectLocation();
        lblProjectFolder();
        txtProjectFolder();
        btBrowseLocation();
        cbSetAsMainProject();
        cbCreateMainClass();
        txtCreateMainClass();
        lblLocation();
        lblBuildScript();
        txtLocation();
        txtBuildScript();
        btBrowseBuildScript();
        btBrowseProjectFolder();
        */
    }
}
