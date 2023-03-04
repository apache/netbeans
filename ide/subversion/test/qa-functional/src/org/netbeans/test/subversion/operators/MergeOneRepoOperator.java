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

import org.netbeans.jemmy.operators.*;

/** Class implementing all necessary methods for handling "Merge AnagramGame to..." NbDialog.
 *
 * @author peter
 * @version 1.0
 */
public class MergeOneRepoOperator extends JDialogOperator {

    /**
     * Creates new MergeOneRepoOperator that can handle it.
     */
    public MergeOneRepoOperator() {
        super("Merge");
    }

    private JLabelOperator _lblEndingWithRevision;
    private JLabelOperator _lblStartingWithRevision;
    private JLabelOperator _lblFirstRepositoryFolder;
    private JLabelOperator _lblEmptyMeansRepositoryHEAD;
    private JLabelOperator _lblEmptyMeansRepositoryHEAD2;
    private JTextFieldOperator _txtStartRevision;
    private JButtonOperator _btSearch;
    private JTextFieldOperator _txtEndRevision;
    private JButtonOperator _btSearch2;
    private JComboBoxOperator _cboMergeFrom;
    private JButtonOperator _btBrowse;
    private JLabelOperator _lblMergeIntoLocalFolderChangesFromOneRepositoryFolder;
    private JLabelOperator _lblMergeFrom;
    private JComboBoxOperator _cboRepository;
    private JLabelOperator _lblJLabel;
    private JTextFieldOperator _txtRepostiryFolder;
    private JTextFieldOperator _txtCurrentFolder;
    private JLabelOperator _lblPreview;
    private JButtonOperator _btMerge;
    private JButtonOperator _btCancel;
    private JButtonOperator _btHelp;


    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find "Ending with Revision:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblEndingWithRevision() {
        if (_lblEndingWithRevision==null) {
            _lblEndingWithRevision = new JLabelOperator(this, "Ending");
        }
        return _lblEndingWithRevision;
    }

    /** Tries to find "Starting with Revision:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblStartingWithRevision() {
        if (_lblStartingWithRevision==null) {
            _lblStartingWithRevision = new JLabelOperator(this, "Starting");
        }
        return _lblStartingWithRevision;
    }

    /** Tries to find "First Repository Folder:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblFirstRepositoryFolder() {
        if (_lblFirstRepositoryFolder==null) {
            _lblFirstRepositoryFolder = new JLabelOperator(this, "Repository Folder");
        }
        return _lblFirstRepositoryFolder;
    }

    /** Tries to find "(empty means repository HEAD)" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblEmptyMeansRepositoryHEAD() {
        if (_lblEmptyMeansRepositoryHEAD==null) {
            _lblEmptyMeansRepositoryHEAD = new JLabelOperator(this, "(empty means repository HEAD)");
        }
        return _lblEmptyMeansRepositoryHEAD;
    }

    /** Tries to find "(empty means repository HEAD)" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblEmptyMeansRepositoryHEAD2() {
        if (_lblEmptyMeansRepositoryHEAD2==null) {
            _lblEmptyMeansRepositoryHEAD2 = new JLabelOperator(this, "(empty means repository HEAD)", 1);
        }
        return _lblEmptyMeansRepositoryHEAD2;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtStartReivison() {
        if (_txtStartRevision==null) {
            _txtStartRevision = new JTextFieldOperator(this);
        }
        return _txtStartRevision;
    }

    /** Tries to find "Search..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btSearch() {
        if (_btSearch==null) {
            _btSearch = new JButtonOperator(this, "Search...");
        }
        return _btSearch;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtEndRevision() {
        if (_txtEndRevision==null) {
            _txtEndRevision = new JTextFieldOperator(this, 1);
        }
        return _txtEndRevision;
    }

    /** Tries to find "Search..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btSearch2() {
        if (_btSearch2==null) {
            _btSearch2 = new JButtonOperator(this, "Search...", 1);
        }
        return _btSearch2;
    }

    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboMergeFrom() {
        if (_cboMergeFrom==null) {
            _cboMergeFrom = new JComboBoxOperator(this, 1);
        }
        return _cboMergeFrom;
    }

    /** Tries to find "Browse..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btBrowse() {
        if (_btBrowse==null) {
            _btBrowse = new JButtonOperator(this, "Browse");
        }
        return _btBrowse;
    }

    /** Tries to find "Merge into local folder changes from one repository folder." JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblMergeIntoLocalFolderChangesFromOneRepositoryFolder() {
        if (_lblMergeIntoLocalFolderChangesFromOneRepositoryFolder==null) {
            _lblMergeIntoLocalFolderChangesFromOneRepositoryFolder = new JLabelOperator(this, "Merge into local folder changes from one repository folder.");
        }
        return _lblMergeIntoLocalFolderChangesFromOneRepositoryFolder;
    }

    /** Tries to find "Merge from:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblMergeFrom() {
        if (_lblMergeFrom==null) {
            _lblMergeFrom = new JLabelOperator(this, "Merge from:");
        }
        return _lblMergeFrom;
    }

    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboRepository() {
        if (_cboRepository==null) {
            _cboRepository = new JComboBoxOperator(this);
        }
        return _cboRepository;
    }

    /** Tries to find null JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblJLabel() {
        if (_lblJLabel==null) {
            _lblJLabel = new JLabelOperator(this, 7);
        }
        return _lblJLabel;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtRepostiryFolder() {
        if (_txtRepostiryFolder==null) {
            _txtRepostiryFolder = new JTextFieldOperator(this, 3);
        }
        return _txtRepostiryFolder;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtCurrentFolder() {
        if (_txtCurrentFolder==null) {
            _txtCurrentFolder = new JTextFieldOperator(this, 4);
        }
        return _txtCurrentFolder;
    }

    /** Tries to find "Preview:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblPreview() {
        if (_lblPreview==null) {
            _lblPreview = new JLabelOperator(this, "Preview:");
        }
        return _lblPreview;
    }

    /** Tries to find "Merge" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btMerge() {
        if (_btMerge==null) {
            _btMerge = new JButtonOperator(this, "Merge");
        }
        return _btMerge;
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
     * gets text for txtStartReivison
     * 
     * @return String text
     */
    public String getStartRevision() {
        return txtStartReivison().getText();
    }

    /**
     * sets text for txtStartReivison
     * 
     * @param text String text
     */
    public void setStartRevision(String text) {
        txtStartReivison().clearText();
        txtStartReivison().typeText(text);
    }

    /** clicks on "Search..." JButton
     */
    public void search() {
        btSearch().push();
    }

    /**
     * gets text for txtEndRevision
     * 
     * @return String text
     */
    public String getEndRevision() {
        return txtEndRevision().getText();
    }

    /**
     * sets text for txtEndRevision
     * 
     * @param text String text
     */
    public void setEndRevision(String text) {
        txtEndRevision().clearText();
        txtEndRevision().typeText(text);
    }

    /** clicks on "Search..." JButton
     */
    public void search2() {
        btSearch2().push();
    }

    /**
     * returns selected item for cboMergeFrom
     * 
     * @return String item
     */
    public String getSelectedMergeFrom() {
        return cboMergeFrom().getSelectedItem().toString();
    }

    /**
     * selects item for cboMergeFrom
     * 
     * @param item String item
     */
    public void selectMergeFrom(String item) {
        cboMergeFrom().selectItem(item);
    }

    /**
     * types text for cboMergeFrom
     * 
     * @param text String text
     */
    public void setMergeFrom(String text) {
        cboMergeFrom().clearText();
        cboMergeFrom().typeText(text);
    }

    /** clicks on "Browse..." JButton
     */
    public RepositoryBrowserOperator browseRepository() {
        btBrowse().pushNoBlock();
        return new RepositoryBrowserOperator();
    }

    /**
     * returns selected item for cboRepository
     * 
     * @return String item
     */
    public String getSelectedRepository() {
        return cboRepository().getSelectedItem().toString();
    }

    public String getRepositoryFolder() {
        return cboRepository().getEditor().getItem().toString();
    }
    /**
     * selects item for cboRepository
     * 
     * @param item String item
     */
    public void selectRepositoryFolder(String item) {
        cboRepository().selectItem(item);
    }
    
    public void setRepositoryFolder(String text) {
        cboRepository().clearText();
        cboRepository().typeText(text);
    }

    /**
     * gets text for txtRepostiryFolder
     * 
     * @return String text
     */
    public String getTxtRepositoryFolder() {
        return txtRepostiryFolder().getText();
    }

    /**
     * gets text for txtCurrentFolder
     * 
     * @return String text
     */
    public String getTxtCurrentFolder() {
        return txtCurrentFolder().getText();
    }

    /** clicks on "Merge" JButton
     */
    public void merge() {
        btMerge().push();
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
     * Performs verification of MergeOneRepoOperator by accessing all its components.
     */
    public void verify() {
        lblEndingWithRevision();
        lblFirstRepositoryFolder();
        lblEmptyMeansRepositoryHEAD();
        lblEmptyMeansRepositoryHEAD2();
        txtStartReivison();
        btSearch();
        txtEndRevision();
        btSearch2();
        cboMergeFrom();
        btBrowse();
        lblMergeIntoLocalFolderChangesFromOneRepositoryFolder();
        lblMergeFrom();
        cboRepository();
        lblJLabel();
        txtRepostiryFolder();
        txtCurrentFolder();
        lblPreview();
        btMerge();
        btCancel();
        btHelp();
    }
}

