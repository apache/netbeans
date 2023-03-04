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

/** Class implementing all necessary methods for handling "Repository browser - file:///tmp/repo" NbDialog.
 *
 * @author peter
 * @version 1.0
 */
public class RepositoryBrowserImpOperator extends NbDialogOperator {

    /**
     * Creates new RepositoryBrowserImpOperator that can handle it.
     */
    public RepositoryBrowserImpOperator() {
        super("Browse Repository");
    }

    private TreeTableOperator _tree;
    private JLabelOperator _lblSpecifyFolderToCheckout;
    private JButtonOperator _btStop;
    private JButtonOperator _btNewFolder;
    private JButtonOperator _btOK;
    private JButtonOperator _btCancel;
    private JButtonOperator _btHelp;


    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find null TreeView$ExplorerTree in this dialog.
     * @return JTreeOperator
     */
    public TreeTableOperator tree() {
        if (_tree==null) {
            _tree = new TreeTableOperator(this);
        }
        return _tree;
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
    
    /** Tries to find "Stop" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btStop() {
        if (_btStop==null) {
            _btStop = new JButtonOperator(this, "Stop");
        }
        return _btStop;
    }
    
    /** Tries to find "New folder" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btNewFolder() {
        if (_btNewFolder==null) {
            _btNewFolder = new JButtonOperator(this, "New folder");
        }
        return _btNewFolder;
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
    
    /** Selects a folder denoted by path.
     * @param path path to folder without root (e.g. "folder|subfolder")
     */
    public void selectFolder(String path) {
        new Node(tree().tree(), path).select();
    }
    
    /** clicks on "New folder" JButton
     */
    public CreateNewFolderOperator createNewFolder() {
        btNewFolder().pushNoBlock();
        return new CreateNewFolderOperator();
    }

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

    /**
     * Performs verification of RepositoryBrowserImpOperator by accessing all its components.
     */
    public void verify() {
        tree();
        lblSpecifyFolderToCheckout();
        btNewFolder();
        btOK();
        btCancel();
        btHelp();
    }
}

