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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import javax.swing.JTextField;

/**
 * Handle "Name And Location" panel of the New Web Project wizard.
 * Components on the panel differs according to type of project selected.<br><br>
 * <u>Web Application</u><br>
 * <ol>
 * <li>Label and TextField Project Name: <code>txtProjectName().setText()</code>
 * <li>Label and TextField Project Location: <code>txtProjectLocation().setText()</code>
 * <li>Button for browsing Project Location: <code>btBrowseLocation().pushNoBlock()</code>
 * <li>Label and TextField Project Folder: <code>txtProjectFolder().getText()</code>
 * <li>CheckBox Set as Main Project: <code>cbSetAsMainProject().setSelected(true)</code>
 * </ol>
 * <u>Web Project with Existing Sources</u><br>
 * <ol>
 * <li>Label and TextField Location: <code>txtProjectLocation().setText()</code>
 * <li>Button for browsing Location: <code>btBrowseLocation().pushNoBlock()</code>
 * <li>Button for browsing Project Folder: <code>btBrowseFolder().pushNoBlock()</code>
 * <ol>
 * @author ms113234
 */
public class NewWebProjectNameLocationStepOperator extends NewProjectWizardOperator {
    
    /** Components operators. */
    //Web Application
    private JLabelOperator      _lblProjectName;
    private JTextFieldOperator  _txtProjectName;
    private JLabelOperator      _lblProjectLocation;
    private JTextFieldOperator  _txtProjectLocation;
    private JButtonOperator     _btBrowseProjectLocation;
    private JLabelOperator      _lblProjectFolder;
    private JTextFieldOperator  _txtProjectFolder;
    private JCheckBoxOperator   _cbSetAsMainProject;
    //Web Project With Existing Sources
    private JButtonOperator     _btBrowseFolder;
    private JLabelOperator      _lblLocation;
    private JTextFieldOperator  _txtLocation;
    
    
    /** Returns operator for label Project Name
     * @return JLabelOperator
     */
    public JLabelOperator lblProjectName() {
        if(_lblProjectName == null) {
            _lblProjectName = new JLabelOperator(this, "Project Name:");
        }
        return _lblProjectName;
    }
    
    
    /** Returns operator of project name textfield
     * @return JTextOperator
     */
    public JTextFieldOperator txtProjectName() {
        if(_txtProjectName == null) {
            if ( lblProjectName().getLabelFor()!=null ) {
                _txtProjectName = new JTextFieldOperator(
                        (JTextField)lblProjectName().getLabelFor());
            }
        }
        return _txtProjectName;
    }
    
    
    /** Sets given name in text field Project Name.
     * @param name project name
     */
    public void setProjectName(String name) {
        txtProjectName().setText(name);
    }
    
    
    /** Returns operator for label Project Location
     * @return JLabelOperator
     */
    public JLabelOperator lblProjectLocation() {
        if(_lblProjectLocation == null) {
            _lblProjectLocation = new JLabelOperator(this, "Project Location:");
        }
        return _lblProjectLocation;
    }
    
    
    /** Returns operator of project location text field
     * @return JTextOperator
     */
    public JTextFieldOperator txtProjectLocation() {
        if(_txtProjectLocation == null) {
            if (lblProjectLocation().getLabelFor()!=null) {
                _txtProjectLocation = new JTextFieldOperator(
                        (JTextField)lblProjectLocation().getLabelFor());
            }
        }
        return _txtProjectLocation;
    }
    
    /** Sets given project location
    /** Returns operator for browse project location button
     * @return JButtonOperator
     */
    public JButtonOperator btBrowseProjectLocation() {
        if ( _btBrowseProjectLocation==null ) {
            _btBrowseProjectLocation = new JButtonOperator(this, "Browse...");
        }
        return _btBrowseProjectLocation;
    }
    
    /** Sets given location in text field Project Location.
     * @param location project location
     */
    public void setProjectLocation(String location) {
        txtProjectLocation().setText(location);
    }
    
    /** Returns operator for label Location when creating project with
     *  existing sources.
     * @return JLabelOperator
     */
    public JLabelOperator lblLocation() {
        if(_lblLocation == null) {
            _lblLocation = new JLabelOperator(this, "Location:");
        }
        return _lblLocation;
    }
    
    
    /** Returns operator of project location text field
     * @return JTextOperator
     */
    public JTextFieldOperator txtLocation() {
        if(_txtLocation == null) {
            if (lblLocation().getLabelFor()!=null) {
                _txtLocation = new JTextFieldOperator(
                        (JTextField)lblLocation().getLabelFor());
            }
        }
        return _txtLocation;
    }
    
    
    /** Returns operator for browse project location button
     * @return JButtonOperator
     */
    public JButtonOperator btBrowseLocation() {
        if ( _btBrowseProjectLocation==null ) {
            _btBrowseProjectLocation = new JButtonOperator(this, "Browse...");
        }
        return _btBrowseProjectLocation;
    }
    
    /** Sets given location in text field Location.
     * @param location Project With Existing Sources location
     */
    public void setLocation(String location) {
        txtLocation().setText(location);
    }
    
    /** Returns operator for label Project Folder
     * @return JLabelOperator
     */
    public JLabelOperator lblProjectFolder() {
        if(_lblProjectFolder == null) {
            _lblProjectFolder = new JLabelOperator(this, "Project Folder:");
        }
        return _lblProjectFolder;
    }
    
    
    /** Returns operator of project folder textfield
     * @return JTextOperator
     */
    public JTextFieldOperator txtProjectFolder() {
        if(_txtProjectFolder == null) {
            if ( lblProjectFolder().getLabelFor()!=null ) {
                _txtProjectFolder = new JTextFieldOperator(
                        (JTextField)lblProjectFolder().getLabelFor());
            }
        }
        return _txtProjectFolder;
    }
    
    /** Returns operator for browse project folder button
     * @return JButtonOperator
     */
    public JButtonOperator btBrowseFolder() {
        if ( _btBrowseFolder==null ) {
            _btBrowseFolder = new JButtonOperator(this, "Browse:", 1);
        }
        return _btBrowseFolder;
    }
    
    /** Performs verification by accessing all sub-components */
    @Override
    public void verify() {
        /* not used because this operator is intended both for web application
         * and web application with existing sources.
        lblProjectName();
        txtProjectName();
        lblProjectLocation();
        txtProjectLocation();
        lblProjectFolder();
        txtProjectFolder();
        btBrowseLocation();
        cbSetAsMainProject();
        btBrowseFolder();
         */
    }
}
