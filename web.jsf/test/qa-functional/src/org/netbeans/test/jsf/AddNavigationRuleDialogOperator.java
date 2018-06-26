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

import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTextAreaOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;

/**
 * Class implementing all necessary methods for handling "Add Navigation Rule"
 * NbDialog.
 *
 * @author luke
 */
public class AddNavigationRuleDialogOperator extends NbDialogOperator {

    private JLabelOperator _lblRuleFromView;
    private JTextFieldOperator _txtRuleFromView;
    private JButtonOperator _btBrowse;
    private JLabelOperator _lblRuleDescription;
    private JTextAreaOperator _txtRuleDescription;
    private JButtonOperator _btAdd;

    /** Creates new AddNavigationRule that can handle it.
     */
    public AddNavigationRuleDialogOperator() {
        super("Add Navigation Rule");
    }

    //******************************
    // Subcomponents definition part
    //******************************
    /** Tries to find "Rule from View:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblRuleFromView() {
        if (_lblRuleFromView == null) {
            _lblRuleFromView = new JLabelOperator(this, "Rule from View:");
        }
        return _lblRuleFromView;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtRuleFromView() {
        if (_txtRuleFromView == null) {
            _txtRuleFromView = new JTextFieldOperator(this);
        }
        return _txtRuleFromView;
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

    /** Tries to find "Rule Description:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblRuleDescription() {
        if (_lblRuleDescription == null) {
            _lblRuleDescription = new JLabelOperator(this, "Rule Description:");
        }
        return _lblRuleDescription;
    }

    /** Tries to find null JTextArea in this dialog.
     * @return JTextAreaOperator
     */
    public JTextAreaOperator txtRuleDescription() {
        if (_txtRuleDescription == null) {
            _txtRuleDescription = new JTextAreaOperator(this);
        }
        return _txtRuleDescription;
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
    /** gets text for txtRuleFromView
     * @return String text
     */
    public String getRuleFromView() {
        return txtRuleFromView().getText();
    }

    /** sets text for txtRuleFromView
     * @param text String text
     */
    public void setRuleFromView(String text) {
        txtRuleFromView().setText(text);
    }

    /** types text for txtRuleFromView
     * @param text String text
     */
    public void typeRuleFromView(String text) {
        txtRuleFromView().typeText(text);
    }

    /** clicks on "Browse..." JButton
     */
    public void browse() {
        btBrowse().push();
    }

    /** gets text for txtRuleDescription
     * @return String text
     */
    public String getRuleDescription() {
        return txtRuleDescription().getText();
    }

    /** sets text for txtRuleDescription
     * @param text String text
     */
    public void setRuleDescription(String text) {
        txtRuleDescription().setText(text);
    }

    /** types text for txtRuleDescription
     * @param text String text
     */
    public void typeRuleDescription(String text) {
        txtRuleDescription().typeText(text);
    }

    /** clicks on "Add" JButton
     */
    public void add() {
        btAdd().push();
    }

    //*****************************************
    // High-level functionality definition part
    //*****************************************
    /** Performs verification of AddNavigationRule by accessing all its components.
     */
    public void verify() {
        lblRuleFromView();
        txtRuleFromView();
        btBrowse();
        lblRuleDescription();
        txtRuleDescription();
        btAdd();
        btCancel();
        btHelp();
    }
}
