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
package org.netbeans.qa.form.jda;

import org.netbeans.jemmy.operators.*;

/** Class implementing all necessary methods for handling "Choose a Class" NbDialog.
 *
 * @author Jiri Vagner
 */
public class ChooseClassOperator extends JDialogOperator {

    /** Creates new ChooseAClass that can handle it.
     */
    public ChooseClassOperator() {
        super("Choose a Class"); // NOI18N
    }

    private JLabelOperator _lblFolderName;
    private JTextFieldOperator _txtFolderName;
    private JTreeOperator _treeTreeView;
    private JButtonOperator _btCreateNew;
    private JButtonOperator _btOK;
    private JButtonOperator _btCancel;


    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find "Folder Name:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblFolderName() {
        if (_lblFolderName==null) {
            _lblFolderName = new JLabelOperator(this, "Folder Name:"); // NOI18N
        }
        return _lblFolderName;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtFolderName() {
        if (_txtFolderName==null) {
            _txtFolderName = new JTextFieldOperator(this);
        }
        return _txtFolderName;
    }

    /** Tries to find null TreeView$ExplorerTree in this dialog.
     * @return JTreeOperator
     */
    public JTreeOperator treeTreeView() {
        if (_treeTreeView==null) {
            _treeTreeView = new JTreeOperator(this);
        }
        return _treeTreeView;
    }

    /** Tries to find "Create New" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btCreateNew() {
        if (_btCreateNew==null) {
            _btCreateNew = new JButtonOperator(this, "Create New"); // NOI18N
        }
        return _btCreateNew;
    }

    /** Tries to find "OK" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btOK() {
        if (_btOK==null) {
            _btOK = new JButtonOperator(this, "OK"); // NOI18N
        }
        return _btOK;
    }

    /** Tries to find "Cancel" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btCancel() {
        if (_btCancel==null) {
            _btCancel = new JButtonOperator(this, "Cancel"); // NOI18N
        }
        return _btCancel;
    }


    //****************************************
    // Low-level functionality definition part
    //****************************************

    /** gets text for txtFolderName
     * @return String text
     */
    public String getFolderName() {
        return txtFolderName().getText();
    }

    /** sets text for txtFolderName
     * @param text String text
     */
    public void setFolderName(String text) {
        txtFolderName().setText(text);
    }

    /** types text for txtFolderName
     * @param text String text
     */
    public void typeFolderName(String text) {
        txtFolderName().typeText(text);
    }

    /** clicks on "Create New" JButton
     */
    public void createNew() {
        btCreateNew().push();
    }

    /** clicks on "OK" JButton
     */
    public void ok() {
        btOK().push();
    }

    /** clicks on "Cancel" JButton
     */
    public void cancel() {
        btCancel().push();
    }
}

