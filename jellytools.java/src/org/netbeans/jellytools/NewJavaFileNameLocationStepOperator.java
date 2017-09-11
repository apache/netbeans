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
