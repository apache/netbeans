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
package org.netbeans.test.subversion.operators;

import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.*;
import org.netbeans.test.subversion.operators.actions.CopyAction;

/** Class implementing all necessary methods for handling "Copy to..." NbDialog.
 *
 * @author peter
 * @version 1.0
 */
public class CopyToOperator extends NbDialogOperator {

    /**
     * Creates new CopyToOperator that can handle it.
     */
    public CopyToOperator() {
        super("Copy");
    }
    
    /** Selects nodes and call copy action on them.
     * @param nodes an array of nodes
     * @return CopyToOperator instance
     */
    public static CopyToOperator invoke(Node[] nodes) {
        new CopyAction().perform(nodes);
        return new CopyToOperator();
    }
    
    /** Selects node and call copy action on it.
     * @param node node to be selected
     * @return CommitOperator instance
     */
    public static CopyToOperator invoke(Node node) {
        return invoke(new Node[] {node});
    }

    private JTextAreaOperator _txtJTextArea;
    private JRadioButtonOperator _rbLocalFolder;
    private JRadioButtonOperator _rbRemoteFolder;
    private JLabelOperator _lblDescribeTheCopyPurpose;
    private JLabelOperator _lblRepositoryFolder;
    private JComboBoxOperator _cboJComboBox;
    private JButtonOperator _btBrowse;
    private JCheckBoxOperator _cbSwitchToCopy;
    private JCheckBoxOperator _cbSkip;
    private JLabelOperator _lblWarningMessage;
    private JButtonOperator _btCopy;
    private JButtonOperator _btCancel;
    private JButtonOperator _btHelp;


    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find null JTextArea in this dialog.
     * @return JTextAreaOperator
     */
    public JTextAreaOperator txtJTextArea() {
        if (_txtJTextArea==null) {
            _txtJTextArea = new JTextAreaOperator(this);
        }
        return _txtJTextArea;
    }

    /** Tries to find "Describe the copy purpose:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblDescribeTheCopyPurpose() {
        if (_lblDescribeTheCopyPurpose==null) {
            _lblDescribeTheCopyPurpose = new JLabelOperator(this, "Copy Description");
        }
        return _lblDescribeTheCopyPurpose;
    }

    /** Tries to find "Repository Folder:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JRadioButtonOperator rbLocalFolder() {
        if (_rbLocalFolder == null) {
            _rbLocalFolder = new JRadioButtonOperator(this, "Local Folder");
        }
        return _rbLocalFolder;
    }
    
    public JRadioButtonOperator rbRemoteFolder() {
        if (_rbRemoteFolder == null) {
            _rbRemoteFolder = new JRadioButtonOperator(this, "Remote Folder");
        }
        return _rbRemoteFolder;
    }

    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboJComboBox() {
        if (_cboJComboBox==null) {
            _cboJComboBox = new JComboBoxOperator(this);
        }
        return _cboJComboBox;
    }

    /** Tries to find "Browse..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btBrowse() {
        if (_btBrowse==null) {
            _btBrowse = new JButtonOperator(this, "Browse...");
        }
        return _btBrowse;
    }

    /** Tries to find "Switch to Copy" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbSwitchToCopy() {
        if (_cbSwitchToCopy==null) {
            _cbSwitchToCopy = new JCheckBoxOperator(this, "Switch to Copy");
        }
        return _cbSwitchToCopy;
    }
    
    /** Tries to find "Skip" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbSkip() {
        if (_cbSkip==null) {
            _cbSkip = new JCheckBoxOperator(this, "Skip");
        }
        return _cbSkip;
    }

    /** Tries to find "Warning - there are localy modified files!" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblWarningMessage() {
        if (_lblWarningMessage==null) {
            _lblWarningMessage = new JLabelOperator(this, 2);
        }
        return _lblWarningMessage;
    }

    /** Tries to find "Copy" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btCopy() {
        if (_btCopy==null) {
            _btCopy = new JButtonOperator(this, "Copy");
        }
        return _btCopy;
    }

    /** Tries to find "Cancel" JButton in this dialog.
     * @return JButtonOperator
     */
    @Override
    public JButtonOperator btCancel() {
        if (_btCancel==null) {
            _btCancel = new JButtonOperator(this, "Cancel");
        }
        return _btCancel;
    }

    /** Tries to find "Help" JButton in this dialog.
     * @return JButtonOperator
     */
    @Override
    public JButtonOperator btHelp() {
        if (_btHelp==null) {
            _btHelp = new JButtonOperator(this, "Help");
        }
        return _btHelp;
    }


    //****************************************
    // Low-level functionality definition part
    //****************************************

    /** gets text for txtJTextArea
     * @return String text
     */
    public String getCopyPurpose() {
        return txtJTextArea().getText();
    }

    /** sets text for txtJTextArea
     * @param text String text
     */
    public void setCopyPurpose(String text) {
        txtJTextArea().clearText();
        txtJTextArea().typeText(text);
    }

    
    /** returns selected item for cboJComboBox
     * @return String item
     */
    public String getSelectedRepositoryFolder() {
        return cboJComboBox().getSelectedItem().toString();
    }
    
    public String getRepositoryFolder() {
        return cboJComboBox().getEditor().getItem().toString();
    }

    /** selects item for cboJComboBox
     * @param item String item
     */
    public void selectJComboBox(String item) {
        cboJComboBox().selectItem(item);
    }

    /** types text for cboJComboBox
     * @param text String text
     */
    public void setRepositoryFolder(String text) {
        cboJComboBox().clearText();
        cboJComboBox().typeText(text);
    }

    /** clicks on "Browse..." JButton
     */
    public RepositoryBrowserImpOperator browseRepository() {
        btBrowse().pushNoBlock();
        return new RepositoryBrowserImpOperator();
    }

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkSwitchToCopy(boolean state) {
        if (cbSwitchToCopy().isSelected()!=state) {
            cbSwitchToCopy().push();
        }
    }

    /** clicks on "Copy" JButton
     */
    public void copy() {
        btCopy().push();
    }

    /** clicks on "Cancel" JButton
     */
    @Override
    public void cancel() {
        btCancel().push();
    }

    /** clicks on "Help" JButton
     */
    @Override
    public void help() {
        btHelp().push();
    }


    //*****************************************
    // High-level functionality definition part
    //*****************************************

    /**
     * Performs verification of CopyToOperator by accessing all its components.
     */
    public void verify() {
        txtJTextArea();
        lblDescribeTheCopyPurpose();
        rbLocalFolder();
        rbRemoteFolder();
        cboJComboBox();
        btBrowse();
        cbSwitchToCopy();
        cbSkip();
        btCopy();
        btCancel();
        btHelp();
    }
}

