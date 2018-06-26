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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
