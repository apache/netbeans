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

import javax.swing.JTextField;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jemmy.operators.*;

/**
 * Class implementing all necessary methods for handling "Add Managed Bean"
 * Dialog.
 *
 */
public class AddManagedBeanOperator extends NbDialogOperator {

    private JLabelOperator _lblBeanClass;
    private JTextFieldOperator _txtBeanClass;
    private JButtonOperator _btBrowse;
    private JLabelOperator _lblScope;
    private JComboBoxOperator _cboScope;
    private JLabelOperator _lblBeanDescription;
    private JTextAreaOperator _txtBeanDescription;
    private JLabelOperator _lblBeanName;
    private JTextFieldOperator _txtBeanName;
    private JButtonOperator _btAdd;

    /**
     * Creates new AddManagedBeanOperator that can handle it.
     */
    public AddManagedBeanOperator() {
        super("Add Managed Bean");
    }

    //******************************
    // Subcomponents definition part
    //******************************
    /** Tries to find "Bean Class:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblBeanClass() {
        if (_lblBeanClass == null) {
            _lblBeanClass = new JLabelOperator(this, "Bean Class:");
        }
        return _lblBeanClass;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtBeanClass() {
        if (_txtBeanClass == null) {
            _txtBeanClass = new JTextFieldOperator((JTextField) lblBeanClass().getLabelFor());
        }
        return _txtBeanClass;
    }

    /** Tries to find "Browse..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btBrowse() {
        if (_btBrowse == null) {
            _btBrowse = new JButtonOperator(this, "Browse...");
        }
        return _btBrowse;
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
            _cboScope = new JComboBoxOperator(this);
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

    /** Tries to find "Bean Name:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblBeanName() {
        if (_lblBeanName == null) {
            _lblBeanName = new JLabelOperator(this, "Bean Name:");
        }
        return _lblBeanName;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtBeanName() {
        if (_txtBeanName == null) {
            _txtBeanName = new JTextFieldOperator((JTextField) lblBeanName().getLabelFor());
        }
        return _txtBeanName;
    }

    /** Tries to find "Add" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btAdd() {
        if (_btAdd == null) {
            _btAdd = new JButtonOperator(this, "Add");
        }
        return _btAdd;
    }

    //****************************************
    // Low-level functionality definition part
    //****************************************
    /** gets text for txtBeanClass
     * @return String text
     */
    public String getBeanClass() {
        return txtBeanClass().getText();
    }

    /** sets text for txtBeanClass
     * @param text String text
     */
    public void setBeanClass(String text) {
        txtBeanClass().setText(text);
    }

    /** types text for txtBeanClass
     * @param text String text
     */
    public void typeBeanClass(String text) {
        txtBeanClass().typeText(text);
    }

    /** clicks on "Browse..." JButton
     */
    public void browse() {
        btBrowse().push();
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

    /** gets text for txtBeanName
     * @return String text
     */
    public String getBeanName() {
        return txtBeanName().getText();
    }

    /** sets text for txtBeanName
     * @param text String text
     */
    public void setBeanName(String text) {
        txtBeanName().setText(text);
    }

    /** types text for txtBeanName
     * @param text String text
     */
    public void typeBeanName(String text) {
        txtBeanName().typeText(text);
    }

    /** clicks on "Add" JButton
     */
    public void add() {
        btAdd().push();
    }

    //*****************************************
    // High-level functionality definition part
    //*****************************************
    /** Performs verification of AddManagedBeanOperator by accessing all its components.
     */
    public void verify() {
        lblBeanClass();
        txtBeanClass();
        btBrowse();
        lblScope();
        cboScope();
        lblBeanDescription();
        txtBeanDescription();
        lblBeanName();
        txtBeanName();
        btAdd();
    }
}
