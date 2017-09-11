/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.test.mercurial.operators;

import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.*;
import org.netbeans.test.mercurial.operators.actions.RevertAction;

/** Class implementing all necessary methods for handling "Revert Modifications" NbDialog.
 *
 * @author peter
 * @version 1.0
 */
public class RevertModificationsOperator extends NbDialogOperator {

    /**
     * Creates new RevertModificationsOperator that can handle it.
     */
    public RevertModificationsOperator() {
        super("Revert Modifications");
    }
    
    /** Selects nodes and call revert action on them.
     * @param nodes an array of nodes
     * @return RevertModificationsOperator instance
     */
    public static RevertModificationsOperator invoke(Node[] nodes) {
        new RevertAction().perform(nodes);
        return new RevertModificationsOperator();
    }
    
    /** Selects node and call switch action on it.
     * @param node node to be selected
     * @return SwitchOperator instance
     */
    public static RevertModificationsOperator invoke(Node node) {
        return invoke(new Node[] {node});
    }

    private JLabelOperator _lblChooseRevision;
    private JComboBoxOperator _cboJComboBox;
    private JButtonOperator _btRevert;
    private JButtonOperator _btCancel;
    private JButtonOperator _btHelp;


    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find "Choose from modified revisions:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblChooseRevision() {
        if (_lblChooseRevision==null) {
            _lblChooseRevision = new JLabelOperator(this, "Choose");
        }
        return _lblChooseRevision;
    }

    /** Tries to find JLabel in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboJComboBox() {
        if (_cboJComboBox==null) {
            _cboJComboBox = new JComboBoxOperator(this);
        }
        return _cboJComboBox;
    }
    /** Tries to find "Revert" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btRevert() {
        if (_btRevert==null) {
            _btRevert = new JButtonOperator(this, "Revert");
        }
        return _btRevert;
    }

    /** Tries to find "Cancel" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btCancel() {
        if (_btCancel==null) {
            _btCancel = new JButtonOperator(this, "Cancel");
        }
        return _btCancel;
    }

    /** Tries to find "Help" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btHelp() {
        if (_btHelp==null) {
            _btHelp = new JButtonOperator(this, "Help");
        }
        return _btHelp;
    }


    //****************************************
    // Low-level functionality definition part
    //****************************************

    /**
     * returns selected item for cboJComboBox
     *
     * @return String item
     */
    public String getSelectedJComboBox() {
        return cboJComboBox().getSelectedItem().toString();
    }

    /**
     * selects item for cboJComboBox
     *
     * @param item String item
     */
    public void selectJComboBox(String item) {
        cboJComboBox().selectItem(item);
    }

    /** clicks on "Revert" JButton
     */
    public void revert() {
        btRevert().push();
    }

    /** clicks on "Cancel" JButton
     */
    public void cancel() {
        btCancel().push();
    }

    /** clicks on "Help" JButton
     */
    public void help() {
        btHelp().push();
    }


    //*****************************************
    // High-level functionality definition part
    //*****************************************

    /**
     * Performs verification of RevertModificationsOperator by accessing all its components.
     */
    public void verify() {
        lblChooseRevision();
        cboJComboBox();
        btRevert();
        btCancel();
        btHelp();
    }
}

