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
import org.netbeans.jellytools.TreeTableOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.*;

/** Class implementing all necessary methods for handling "RepositoryBrowserOperator" NbDialog.
 *
 * @author peter
 * @version 1.0
 */
public class RepositoryBrowserOperator extends NbDialogOperator {

    /** Creates new RepositoryBrowserOperator that can handle it.
     */
    public RepositoryBrowserOperator() {
        super("Browse Repository");
    }

    private JTableOperator _table;
    private JLabelOperator _lblSpecifyFolderToCheckout;
    private JButtonOperator _btOK;
    private JButtonOperator _btCancel;
    private JButtonOperator _btHelp;


    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find null TreeView$ExplorerTree in this dialog.
     * @return JTreeOperator
     */
    public JTableOperator table() {
        if (_table == null) {
            _table = new JTableOperator(this);
        }
        return _table;
    }

    /** Tries to find "Specify Folder to checkout:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblSpecifyFolderToCheckout() {
        if (_lblSpecifyFolderToCheckout==null) {
            _lblSpecifyFolderToCheckout = new JLabelOperator(this);
        }
        return _lblSpecifyFolderToCheckout;
    }
    
    //****************************************
    // Low-level functionality definition part
    //****************************************

    /** Performs popup menu on specified row.
     * @param row row number to be selected (starts from 0)
     * @param popupPath popup menu path
     */
    public void performPopup(int row, String popupPath) {
        table().selectCell(row, 0);
        JPopupMenuOperator popup = new JPopupMenuOperator(table().callPopupOnCell(row, 0));
        popup.pushMenu(popupPath);
    }

    /** Performs popup menu on specified file.
     * @param filename name of file to be selected
     * @param popupPath popup menu path
     */
    public void performPopup(String filename, String popupPath) {
        performPopup(table().findCellRow(filename), popupPath);
    }

    /** Performs popup menu on specified row and no block further execution.
     * @param row row number to be selected (starts from 0)
     * @param popupPath popup menu path
     */
    public void performPopupNoBlock(int row, String popupPath) {
        table().selectCell(row, 0);
        JPopupMenuOperator popup = new JPopupMenuOperator(table().callPopupOnCell(row, 0));
        popup.pushMenuNoBlock(popupPath);
    }

    /** Performs popup menu on specified file and no block further execution.
     * @param filename name of file to be selected
     * @param popupPath popup menu path
     */
    public void performPopupNoBlock(String filename, String popupPath) {
        performPopupNoBlock(table().findCellRow(filename), popupPath);
    }
    
    /** Tries to find "OK" JButton in this dialog.
     * @return JButtonOperator
     */
    @Override
    public JButtonOperator btOK() {
        if (_btOK==null) {
            _btOK = new JButtonOperator(this, "OK");
        }
        return _btOK;
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

    /** clicks on "OK" JButton
     */
    @Override
    public void ok() {
        btOK().push();
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

    /** Performs verification of RepositoryBrowserOperator by accessing all its components.
     */
    public void verify() {
        table();
        lblSpecifyFolderToCheckout();
        btOK();
        btCancel();
        btHelp();
    }
}

