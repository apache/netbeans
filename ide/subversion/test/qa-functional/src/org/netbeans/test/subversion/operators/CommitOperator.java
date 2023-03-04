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

import java.awt.Rectangle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JPopupMenuOperator;
import org.netbeans.jemmy.operators.JTableOperator;
import org.netbeans.jemmy.operators.JTextAreaOperator;
import org.netbeans.test.subversion.operators.actions.CommitAction;


/** Class implementing all necessary methods for handling "Commit" dialog.
 * <br>
 * Usage:<br>
 * <pre>
 *      new CommitAction().perform(node);
 *      CommitOperator co = new CommitOperator();
 *      co.setCommitMessage("Commit message.");
 *      co.selectCommitAction("MyFile", "Exclude from Commit");
 *      co.commit();
 * </pre>
 *
 * @see VersioningOperator
 * @see org.netbeans.jellytools.modules.javacvs.actions.CommitAction
 * @see org.netbeans.jellytools.modules.javacvs.actions.ShowChangesAction
 *
 */
public class CommitOperator extends NbDialogOperator {

    /** Waits for "Commit" dialog. It can have title "Commit - <object>" 
     * or "Commit files" if there is no file to commit.
     */
    public CommitOperator() {
        super("Commit");
    }

    /** Selects nodes and call commit action on them.
     * @param nodes an array of nodes
     * @return CommitOperator instance
     */
    public static CommitOperator invoke(Node[] nodes) {
        new CommitAction().perform(nodes);
        return new CommitOperator();
    }
    
    /** Selects node and call commit action on it.
     * @param node node to be selected
     * @return CommitOperator instance
     */
    public static CommitOperator invoke(Node node) {
        return invoke(new Node[] {node});
    }
    
    private JLabelOperator _lblCommitMessage;
    private JTextAreaOperator _txtCommitMessage;
    private JLabelOperator _lblFilesToCommit;
    private JTableOperator _tabFiles;
    private JButtonOperator _btCommit;
    private JButtonOperator _btCancel;
    private JButtonOperator _btHelp;

    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find "Commit Message:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblCommitMessage() {
        if (_lblCommitMessage==null) {
            _lblCommitMessage = new JLabelOperator(this, "Commit Message");
        }
        return _lblCommitMessage;
    }

    /** Tries to find "Commit Message:" TextArea in this dialog.
     * @return JTextAreaOperator
     */
    public JTextAreaOperator txtCommitMessage() {
        if (_txtCommitMessage==null) {
            _txtCommitMessage = new JTextAreaOperator(this);
        }
        return _txtCommitMessage;
    }

    /** Tries to find "Files to Commit:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblFilesToCommit() {
        if (_lblFilesToCommit==null) {
            _lblFilesToCommit = new JLabelOperator(this, "Files to Commit");
        }
        return _lblFilesToCommit;
    }

    /** Tries to find files JTable in this dialog.
     * @return JTableOperator
     */
    public JTableOperator tabFiles() {
        if (_tabFiles==null) {
            _tabFiles = new JTableOperator(this);
        }
        return _tabFiles;
    }

    /** Tries to find "Commit" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btCommit() {
        if (_btCommit==null) {
            _btCommit = new JButtonOperator(this, "Commit");
        }
        return _btCommit;
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

    /** gets text for txtCommitMessage
     * @return String text
     */
    public String getCommitMessage() {
        return txtCommitMessage().getText();
    }

    /** sets text for txtCommitMessage
     * @param text String text
     */
    public void setCommitMessage(String text) {
        txtCommitMessage().clearText();
        txtCommitMessage().typeText(text);
    }

    /** clicks on "Commit" JButton
     */
    public void commit() {
        btCommit().push();
    }

    /** Selects specified commit action for given row.
     * @param rowIndex index of row to be selected
     * @param action name of action to be selected
     */
    public void selectCommitAction(int rowIndex, String action) {
        Rectangle rec = tabFiles().getCellRect(rowIndex, 0, true);
        tabFiles().clickForPopup(rec.x + rec.width / 2, rec.y + rec.height / 2);
        JPopupMenuOperator pmo = new JPopupMenuOperator();
        pmo.pushMenu(action);
    }

    /** Selects specified commit action for given row.
     * @param filename name of file to be selected
     * @param action name of action to be selected
     */
    public void selectCommitAction(String filename, String action) {
        selectCommitAction(tabFiles().findCellRow(filename), action);
    }

    //*****************************************
    // High-level functionality definition part
    //*****************************************

    /** Performs verification of CommitOperator by accessing all its components.
     */
    public void verify() {
        lblCommitMessage();
        txtCommitMessage();
//        lblFilesToCommit();
        tabFiles();
        btCommit();
        btCancel();
        btHelp();
    }
}
