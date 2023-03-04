/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.test.git.operators;

import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.*;
import org.netbeans.test.git.operators.actions.RevertAction;

/**
 * Class implementing all necessary methods for handling "Revert Modifications"
 * NbDialog.
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

    /**
     * Selects nodes and call revert action on them.
     *
     * @param nodes an array of nodes
     * @return RevertModificationsOperator instance
     */
    public static RevertModificationsOperator invoke(Node[] nodes) {
        new RevertAction().perform(nodes);
        return new RevertModificationsOperator();
    }

    /**
     * Selects node and call switch action on it.
     *
     * @param node node to be selected
     * @return SwitchOperator instance
     */
    public static RevertModificationsOperator invoke(Node node) {
        return invoke(new Node[]{node});
    }

    private JLabelOperator _lblChooseRevision;
    private JComboBoxOperator _cboJComboBox;
    private JButtonOperator _btRevert;
    private JButtonOperator _btCancel;
    private JButtonOperator _btHelp;

    //******************************
    // Subcomponents definition part
    //******************************
    /**
     * Tries to find "Choose from modified revisions:" JLabel in this dialog.
     *
     * @return JLabelOperator
     */
    public JLabelOperator lblChooseRevision() {
        if (_lblChooseRevision == null) {
            _lblChooseRevision = new JLabelOperator(this, "Choose");
        }
        return _lblChooseRevision;
    }

    /**
     * Tries to find JLabel in this dialog.
     *
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboJComboBox() {
        if (_cboJComboBox == null) {
            _cboJComboBox = new JComboBoxOperator(this);
        }
        return _cboJComboBox;
    }

    /**
     * Tries to find "Revert" JButton in this dialog.
     *
     * @return JButtonOperator
     */
    public JButtonOperator btRevert() {
        if (_btRevert == null) {
            _btRevert = new JButtonOperator(this, "Revert");
        }
        return _btRevert;
    }

    /**
     * Tries to find "Cancel" JButton in this dialog.
     *
     * @return JButtonOperator
     */
    public JButtonOperator btCancel() {
        if (_btCancel == null) {
            _btCancel = new JButtonOperator(this, "Cancel");
        }
        return _btCancel;
    }

    /**
     * Tries to find "Help" JButton in this dialog.
     *
     * @return JButtonOperator
     */
    public JButtonOperator btHelp() {
        if (_btHelp == null) {
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

    /**
     * clicks on "Revert" JButton
     */
    public void revert() {
        btRevert().push();
    }

    /**
     * clicks on "Cancel" JButton
     */
    public void cancel() {
        btCancel().push();
    }

    /**
     * clicks on "Help" JButton
     */
    public void help() {
        btHelp().push();
    }

    //*****************************************
    // High-level functionality definition part
    //*****************************************
    /**
     * Performs verification of RevertModificationsOperator by accessing all its
     * components.
     */
    public void verify() {
        lblChooseRevision();
        cboJComboBox();
        btRevert();
        btCancel();
        btHelp();
    }
}
