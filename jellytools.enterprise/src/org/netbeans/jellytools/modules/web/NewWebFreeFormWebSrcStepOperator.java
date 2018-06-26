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

package org.netbeans.jellytools.modules.web;

import javax.swing.JComboBox;
import javax.swing.JTextField;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jemmy.operators.*;


/** Class implementing all necessary methods for handling "New Web Application
 * with Existing Ant Script - Web Sources" wizard step.
 *
 * @author Martin.Schovanek@sun.com
 * @version 1.0
 */
public class NewWebFreeFormWebSrcStepOperator extends WizardOperator {

    /** Creates new NewWebFreeFormWebSrcStepOperator that can handle it.
     */
    public NewWebFreeFormWebSrcStepOperator() {
        super(Helper.freeFormWizardTitle());
    }
    
    private JLabelOperator _lblWebPagesFolder;
    private JTextFieldOperator _txtWebPagesFolder;
    private JButtonOperator _btBrowse;
    private JLabelOperator _lblContextPath;
    private JTextFieldOperator _txtContextPath;
    private JLabelOperator _lblJ2EESpecificationLevel;
    private JComboBoxOperator _cboJ2EESpecificationLevel;
    public static final String ITEM_J2EE14 = Bundle.getStringTrimmed(
            "org.netbeans.modules.web.freeform.ui.Bundle",
            "TXT_J2EESpecLevel_0");
    public static final String ITEM_J2EE13 = Bundle.getStringTrimmed(
            "org.netbeans.modules.web.freeform.ui.Bundle",
            "TXT_J2EESpecLevel_1");
    
    
    //******************************
    // Subcomponents definition part
    //******************************
    
    /** Tries to find "Web Pages Folder:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblWebPagesFolder() {
        if (_lblWebPagesFolder==null) {
            String webPagesFolder = Bundle.getStringTrimmed(
                    "org.netbeans.modules.web.freeform.ui.Bundle",
                    "LBL_WebPagesPanel_WebPagesLocation_Label");
            _lblWebPagesFolder = new JLabelOperator(this, webPagesFolder);
        }
        return _lblWebPagesFolder;
    }
    
    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtWebPagesFolder() {
        if (_txtWebPagesFolder==null) {
            if (lblWebPagesFolder().getLabelFor()!=null) {
                _txtWebPagesFolder = new JTextFieldOperator(
                        (JTextField) lblWebPagesFolder().getLabelFor());
            } else {
                _txtWebPagesFolder = new JTextFieldOperator(this);
            }
        }
        return _txtWebPagesFolder;
    }
    
    /** Tries to find "Browse..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btBrowse() {
        if (_btBrowse==null) {
            String browse = Bundle.getStringTrimmed(
                    "org.netbeans.modules.web.freeform.ui.Bundle",
                    "BTN_BasicProjectInfoPanel_browseAntScript");
            _btBrowse = new JButtonOperator(this, browse);
        }
        return _btBrowse;
    }
    
    /** Tries to find "Context Path:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblContextPath() {
        if (_lblContextPath==null) {
            String contextPath = Bundle.getStringTrimmed(
                    "org.netbeans.modules.web.freeform.ui.Bundle",
                    "LBL_WebPagesPanel_ContextPath_Label");
            _lblContextPath = new JLabelOperator(this, contextPath);
        }
        return _lblContextPath;
    }
    
    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtContextPath() {
        if (_txtContextPath==null) {
            if (lblContextPath().getLabelFor()!=null) {
                _txtContextPath = new JTextFieldOperator(
                        (JTextField) lblContextPath().getLabelFor());
            } else {
                _txtContextPath = new JTextFieldOperator(this, 1);
            }
        }
        return _txtContextPath;
    }
    
    /** Tries to find "J2EE Specification Level:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblJ2EESpecificationLevel() {
        if (_lblJ2EESpecificationLevel==null) {
            String j2eeLevel = Bundle.getStringTrimmed(
                    "org.netbeans.modules.web.freeform.ui.Bundle",
                    "LBL_WebPagesPanel_J2EESpecLevel_Label");
            _lblJ2EESpecificationLevel = new JLabelOperator(this, j2eeLevel);
        }
        return _lblJ2EESpecificationLevel;
    }
    
    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboJ2EESpecificationLevel() {
        if (_cboJ2EESpecificationLevel==null) {
            if (lblJ2EESpecificationLevel().getLabelFor()!=null) {
                _cboJ2EESpecificationLevel = new JComboBoxOperator(
                        (JComboBox) lblJ2EESpecificationLevel().getLabelFor());
            } else {
                _cboJ2EESpecificationLevel = new JComboBoxOperator(this);
            }
        }
        return _cboJ2EESpecificationLevel;
    }
    
    
    //****************************************
    // Low-level functionality definition part
    //****************************************
    
    /** gets text for txtWebPagesFolder
     * @return String text
     */
    public String getWebPagesFolder() {
        return txtWebPagesFolder().getText();
    }
    
    /** sets text for txtWebPagesFolder
     * @param text String text
     */
    public void setWebPagesFolder(String text) {
        txtWebPagesFolder().setText(text);
    }
    
    /** types text for txtWebPagesFolder
     * @param text String text
     */
    public void typeWebPagesFolder(String text) {
        txtWebPagesFolder().typeText(text);
    }
    
    /** clicks on "Browse..." JButton
     */
    public void browse() {
        btBrowse().push();
    }
    
    /** gets text for txtContextPath
     * @return String text
     */
    public String getContextPath() {
        return txtContextPath().getText();
    }
    
    /** sets text for txtContextPath
     * @param text String text
     */
    public void setContextPath(String text) {
        txtContextPath().setText(text);
    }
    
    /** types text for txtContextPath
     * @param text String text
     */
    public void typeContextPath(String text) {
        txtContextPath().typeText(text);
    }
    
    /** returns selected item for cboJ2EESpecificationLevel
     * @return String item
     */
    public String getSelectedJ2EESpecificationLevel() {
        return cboJ2EESpecificationLevel().getSelectedItem().toString();
    }
    
    /** selects item for cboJ2EESpecificationLevel
     * @param item String item
     */
    public void selectJ2EESpecificationLevel(String item) {
        cboJ2EESpecificationLevel().selectItem(item);
    }
    
    
    //*****************************************
    // High-level functionality definition part
    //*****************************************
    
    /** Performs verification of NewWebFreeFormWebSrcStepOperator by accessing all its components.
     */
    public void verify() {
        lblWebPagesFolder();
        txtWebPagesFolder();
        btBrowse();
        lblContextPath();
        txtContextPath();
        lblJ2EESpecificationLevel();
        cboJ2EESpecificationLevel();
    }
}
