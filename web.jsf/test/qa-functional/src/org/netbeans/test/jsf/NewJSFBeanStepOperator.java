/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of
 * the License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include
 * the License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by
 * Oracle in the GPL Version 2 section of the License file that accompanied
 * this code. If applicable, add the following below the License Header, with
 * the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license." If you do not indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to its
 * licensees as provided above. However, if you add GPL Version 2 code and
 * therefore, elected the GPL Version 2 license, then the option applies only
 * if the new code is made subject to such option by the copyright holder.
 */
package org.netbeans.test.jsf;

import javax.swing.JComboBox;
import javax.swing.JTextField;
import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTextAreaOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

/**
 * Class implementing all necessary methods for handling "New JSF Managed Bean"
 * NbDialog.
 *
 * @author luke
 */
public class NewJSFBeanStepOperator extends WizardOperator {

    /**
     * Creates new NewJSFManagedBean that can handle it.
     */
    public NewJSFBeanStepOperator() {
        super("New JSF Managed Bean");
        checkPanel("Name and Location");
    }
    private JLabelOperator _lblNameAndLocation;
    private JLabelOperator _lblClassName;
    private JTextFieldOperator _txtClassName;
    private JLabelOperator _lblProject;
    private JTextFieldOperator _txtProject;
    private JLabelOperator _lblLocation;
    private JComboBoxOperator _cboLocation;
    private JLabelOperator _lblPackage;
    private JComboBoxOperator _cboPackage;
    private JLabelOperator _lblCreatedFile;
    private JTextFieldOperator _txtCreatedFile;
    private JLabelOperator _lblConfigurationFile;
    private JComboBoxOperator _cboConfigurationFile;
    private JLabelOperator _lblScope;
    private JComboBoxOperator _cboScope;
    private JLabelOperator _lblBeanDescription;
    private JTextAreaOperator _txtBeanDescription;

    //******************************
    // Subcomponents definition part
    //******************************
    /** Tries to find "Name and Location" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblNameAndLocation() {
        if (_lblNameAndLocation == null) {
            _lblNameAndLocation = new JLabelOperator(this, "Name and Location");
        }
        return _lblNameAndLocation;
    }

    /** Tries to find "Class Name:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblClassName() {
        if (_lblClassName == null) {
            _lblClassName = new JLabelOperator(this, "Class Name:");
        }
        return _lblClassName;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtClassName() {
        if (_txtClassName == null) {
            _txtClassName = new JTextFieldOperator((JTextField) lblClassName().getLabelFor());
        }
        return _txtClassName;
    }

    /** Tries to find "Project:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblProject() {
        if (_lblProject == null) {
            _lblProject = new JLabelOperator(this, "Project:");
        }
        return _lblProject;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtProject() {
        if (_txtProject == null) {
            _txtProject = new JTextFieldOperator((JTextField) lblProject().getLabelFor());
        }
        return _txtProject;
    }

    /** Tries to find "Location:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblLocation() {
        if (_lblLocation == null) {
            _lblLocation = new JLabelOperator(this, "Location:");
        }
        return _lblLocation;
    }

    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboLocation() {
        if (_cboLocation == null) {
            _cboLocation = new JComboBoxOperator((JComboBox) lblLocation().getLabelFor());
        }
        return _cboLocation;
    }

    /** Tries to find "Package:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblPackage() {
        if (_lblPackage == null) {
            _lblPackage = new JLabelOperator(this, "Package:");
        }
        return _lblPackage;
    }

    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboPackage() {
        if (_cboPackage == null) {
            _cboPackage = new JComboBoxOperator((JComboBox) lblPackage().getLabelFor());
        }
        return _cboPackage;
    }

    /** Tries to find "Created File:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblCreatedFile() {
        if (_lblCreatedFile == null) {
            _lblCreatedFile = new JLabelOperator(this, "Created File:");
        }
        return _lblCreatedFile;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtCreatedFile() {
        if (_txtCreatedFile == null) {
            _txtCreatedFile = new JTextFieldOperator((JTextField) lblCreatedFile().getLabelFor());
        }
        return _txtCreatedFile;
    }

    /** Tries to find "Configuration File:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblConfigurationFile() {
        if (_lblConfigurationFile == null) {
            _lblConfigurationFile = new JLabelOperator(this, "Configuration File:");
        }
        return _lblConfigurationFile;
    }

    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboConfigurationFile() {
        if (_cboConfigurationFile == null) {
            _cboConfigurationFile = new JComboBoxOperator((JComboBox) lblConfigurationFile().getLabelFor());
        }
        return _cboConfigurationFile;
    }

    /** Tries to find "Scope:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblScope() {
        if (_lblScope == null) {
            _lblScope = new JLabelOperator(this, "Scope:");
        }
        return _lblScope;
    }

    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboScope() {
        if (_cboScope == null) {
            _cboScope = new JComboBoxOperator((JComboBox) lblScope().getLabelFor());
        }
        return _cboScope;
    }

    /** Tries to find "Bean Description:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblBeanDescription() {
        if (_lblBeanDescription == null) {
            _lblBeanDescription = new JLabelOperator(this, "Bean Description:");
        }
        return _lblBeanDescription;
    }

    /** Tries to find null JTextArea in this dialog.
     * @return JTextAreaOperator
     */
    public JTextAreaOperator txtBeanDescription() {
        if (_txtBeanDescription == null) {
            _txtBeanDescription = new JTextAreaOperator(this);
        }
        return _txtBeanDescription;
    }

    //****************************************
    // Low-level functionality definition part
    //****************************************
    /** gets text for txtClassName
     * @return String text
     */
    public String getClassName() {
        return txtClassName().getText();
    }

    /** sets text for txtClassName
     * @param text String text
     */
    public void setClassName(String text) {
        txtClassName().setText(text);
    }

    /** types text for txtClassName
     * @param text String text
     */
    public void typeClassName(String text) {
        txtClassName().typeText(text);
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

    /** returns selected item for cboPackage
     * @return String item
     */
    public String getSelectedPackage() {
        return cboPackage().getSelectedItem().toString();
    }

    /** selects item for cboPackage
     * @param item String item
     */
    public void selectPackage(String item) {
        cboPackage().selectItem(item);
    }

    /** types text for cboPackage
     * @param text String text
     */
    public void typePackage(String text) {
        cboPackage().typeText(text);
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

    /** returns selected item for cboConfigurationFile
     * @return String item
     */
    public String getSelectedConfigurationFile() {
        return cboConfigurationFile().getSelectedItem().toString();
    }

    /** selects item for cboConfigurationFile
     * @param item String item
     */
    public void selectConfigurationFile(String item) {
        cboConfigurationFile().selectItem(item);
    }

    /** returns selected item for cboScope
     * @return String item
     */
    public String getSelectedScope() {
        return cboScope().getSelectedItem().toString();
    }

    /** selects item for cboScope
     * @param item String item
     */
    public void selectScope(String item) {
        cboScope().selectItem(item);
    }

    /** gets text for txtBeanDescription
     * @return String text
     */
    public String getBeanDescription() {
        return txtBeanDescription().getText();
    }

    /** sets text for txtBeanDescription
     * @param text String text
     */
    public void setBeanDescription(String text) {
        txtBeanDescription().setText(text);
    }

    /** types text for txtBeanDescription
     * @param text String text
     */
    public void typeBeanDescription(String text) {
        txtBeanDescription().typeText(text);
    }

    //*****************************************
    // High-level functionality definition part
    //*****************************************
    /** Performs verification of NewJSFManagedBean by accessing all its components.
     */
    @Override
    public void verify() {
        lblNameAndLocation();
        lblClassName();
        txtClassName();
        lblProject();
        txtProject();
        lblLocation();
        cboLocation();
        lblPackage();
        cboPackage();
        lblCreatedFile();
        txtCreatedFile();
        lblConfigurationFile();
        cboConfigurationFile();
        lblScope();
        cboScope();
        lblBeanDescription();
        txtBeanDescription();
        btBack();
        btNext();
        btFinish();
        btCancel();
        btHelp();
    }
}
