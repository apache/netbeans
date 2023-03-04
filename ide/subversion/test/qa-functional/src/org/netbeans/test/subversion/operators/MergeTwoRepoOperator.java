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
public class MergeTwoRepoOperator extends JDialogOperator {

    /**
     * Creates new MergeTwoRepoOperator that can handle it.
     */
    public MergeTwoRepoOperator() {
        super("Merge");
    }

    private JLabelOperator _lblSecondRepositoryFolder;
    private JLabelOperator _lblStartingWithRevision;
    private JLabelOperator _lblEndingWithRevision;
    private JLabelOperator _lblFirstRepositoryFolder;
    private JLabelOperator _lblEmptyMeansRepositoryHEAD;
    private JComboBoxOperator _cboMergeFrom;
    private JComboBoxOperator _cboRepository1;
    private JTextFieldOperator _txtStartRevision;
    private JButtonOperator _btSearch;
    private JTextFieldOperator _txtEndRevision;
    private JButtonOperator _btSearch2;
    private JLabelOperator _lblEmptyMeansRepositoryHEAD2;
    private JButtonOperator _btBrowse1;
    private JButtonOperator _btBrowse2;
    private JLabelOperator _lblMergeIntoLocalFolderChangesBetweenTwoRepositoryFolders;
    private JLabelOperator _lblMergeFrom;
    private JComboBoxOperator _cboRepository2;
    private JLabelOperator _lblJLabel;
    private JTextFieldOperator _txtRepositoryFolder1;
    private JTextFieldOperator _txtCurrentFolder;
    private JTextFieldOperator _txtRepositoryFolder2;
    private JLabelOperator _lblPreview;
    private JButtonOperator _btMerge;
    private JButtonOperator _btCancel;
    private JButtonOperator _btHelp;


    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find "Second Repository Folder:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblSecondRepositoryFolder() {
        if (_lblSecondRepositoryFolder==null) {
            _lblSecondRepositoryFolder = new JLabelOperator(this, "Second Repository Folder");
        }
        return _lblSecondRepositoryFolder;
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

    /** Tries to find "Ending with Revision:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblEndingWithRevision() {
        if (_lblEndingWithRevision==null) {
            _lblEndingWithRevision = new JLabelOperator(this, "Ending");
        }
        return _lblEndingWithRevision;
    }

    /** Tries to find "First Repository Folder:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblFirstRepositoryFolder() {
        if (_lblFirstRepositoryFolder==null) {
            _lblFirstRepositoryFolder = new JLabelOperator(this, "First Repository Folder");
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

    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboMergeFrom() {
        if (_cboMergeFrom==null) {
            _cboMergeFrom = new JComboBoxOperator(this, 2);
        }
        return _cboMergeFrom;
    }

    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboRepository1() {
        if (_cboRepository1==null) {
            _cboRepository1 = new JComboBoxOperator(this, 1);
        }
        return _cboRepository1;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtStartRevision() {
        if (_txtStartRevision==null) {
            _txtStartRevision = new JTextFieldOperator(this, 2);
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
            _txtEndRevision = new JTextFieldOperator(this, 3);
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

    /** Tries to find "(empty means repository HEAD)" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblEmptyMeansRepositoryHEAD2() {
        if (_lblEmptyMeansRepositoryHEAD2==null) {
            _lblEmptyMeansRepositoryHEAD2 = new JLabelOperator(this, "(empty means repository HEAD)", 1);
        }
        return _lblEmptyMeansRepositoryHEAD2;
    }

    /** Tries to find "Browse..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btBrowseRepositoryFolder1() {
        if (_btBrowse1==null) {
            _btBrowse1 = new JButtonOperator(this, "Browse", 1);
        }
        return _btBrowse1;
    }

    /** Tries to find "Browse..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btBrowseRepositoryFolder2() {
        if (_btBrowse2==null) {
            _btBrowse2 = new JButtonOperator(this, "Browse");
        }
        return _btBrowse2;
    }

    /** Tries to find "Merge into local folder changes between two repository folders." JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblMergeIntoLocalFolderChangesBetweenTwoRepositoryFolders() {
        if (_lblMergeIntoLocalFolderChangesBetweenTwoRepositoryFolders==null) {
            _lblMergeIntoLocalFolderChangesBetweenTwoRepositoryFolders = new JLabelOperator(this, "Merge into local folder changes between two repository folders.");
        }
        return _lblMergeIntoLocalFolderChangesBetweenTwoRepositoryFolders;
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
    public JComboBoxOperator cboRepository2() {
        if (_cboRepository2==null) {
            _cboRepository2 = new JComboBoxOperator(this);
        }
        return _cboRepository2;
    }

    /** Tries to find null JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblJLabel() {
        if (_lblJLabel==null) {
            _lblJLabel = new JLabelOperator(this, 8);
        }
        return _lblJLabel;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtRepositoryFolder1() {
        if (_txtRepositoryFolder1==null) {
            _txtRepositoryFolder1 = new JTextFieldOperator(this, 4);
        }
        return _txtRepositoryFolder1;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtCurrentFolder() {
        if (_txtCurrentFolder==null) {
            _txtCurrentFolder = new JTextFieldOperator(this, 5);
        }
        return _txtCurrentFolder;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtRepositoryFolder2() {
        if (_txtRepositoryFolder2==null) {
            _txtRepositoryFolder2 = new JTextFieldOperator(this, 6);
        }
        return _txtRepositoryFolder2;
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

    /**
     * returns selected item for cboRepository1
     * 
     * @return String item
     */
    public String getSelectedRepositoryFolder1() {
        return cboRepository1().getSelectedItem().toString();
    }

    public String getRepositoryFolder1() {
        return cboRepository1().getEditor().getItem().toString();
    }
    
    /**
     * selects item for cboRepository1
     * 
     * @param item String item
     */
    public void selectRepositoryFolder1(String item) {
        cboRepository1().selectItem(item);
    }

    /**
     * types text for cboRepository1
     * 
     * @param text String text
     */
    public void setRepositoryFolder1(String text) {
        cboRepository1().clearText();
        cboRepository1().typeText(text);
    }

    /**
     * gets text for txtStartRevision
     * 
     * @return String text
     */
    public String getStartRevision() {
        return txtStartRevision().getText();
    }

    /**
     * sets text for txtStartRevision
     * 
     * @param text String text
     */
    public void setStartRevision(String text) {
        txtStartRevision().clearText();
        txtStartRevision().typeText(text);
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

    /** clicks on "Browse..." JButton
     */
    public RepositoryBrowserOperator browseRepositoryFolder1() {
        btBrowseRepositoryFolder1().pushNoBlock();
        return new RepositoryBrowserOperator();
    }

    /** clicks on "Browse..." JButton
     */
    public RepositoryBrowserOperator browseRepositoryFolder2() {
        btBrowseRepositoryFolder2().pushNoBlock();
        return new RepositoryBrowserOperator();
    }

    /**
     * returns selected item for cboRepository2
     * 
     * @return String item
     */
    public String getSelectedRepositoryFolder2() {
        return cboRepository2().getSelectedItem().toString();
    }

    public String getRepositoryFolder2() {
        return cboRepository2().getEditor().getItem().toString();
    }
    
    /**
     * selects item for cboRepository2
     * 
     * @param item String item
     */
    public void selectRepositoryFolder2(String item) {
        cboRepository2().selectItem(item);
    }
    
    /**
     * types text for cboRepository1
     * 
     * @param text String text
     */
    public void setRepositoryFolder2(String text) {
        cboRepository2().clearText();
        cboRepository2().typeText(text);
    }

    /**
     * gets text for txtRepositoryFolder1
     * 
     * @return String text
     */
    public String getTxtRepositoryFolder1() {
        return txtRepositoryFolder1().getText();
    }

    /**
     * gets text for txtCurrentFolder
     * 
     * @return String text
     */
    public String getTxtCurrentFolder() {
        return txtCurrentFolder().getText();
    }

    /**
     * gets text for txtRepositoryFolder2
     * 
     * @return String text
     */
    public String getTxtRepositoryFolder2() {
        return txtRepositoryFolder2().getText();
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
     * Performs verification of MergeTwoRepoOperator by accessing all its components.
     */
    public void verify() {
        lblSecondRepositoryFolder();
        lblStartingWithRevision();
        lblEndingWithRevision();
        lblFirstRepositoryFolder();
        lblEmptyMeansRepositoryHEAD();
        cboMergeFrom();
        cboRepository1();
        txtStartRevision();
        btSearch();
        txtEndRevision();
        btSearch2();
        lblEmptyMeansRepositoryHEAD2();
        btBrowseRepositoryFolder1();
        btBrowseRepositoryFolder2();
        lblMergeIntoLocalFolderChangesBetweenTwoRepositoryFolders();
        lblMergeFrom();
        cboRepository2();
        lblJLabel();
        txtRepositoryFolder1();
        txtCurrentFolder();
        txtRepositoryFolder2();
        lblPreview();
        btMerge();
        btCancel();
        btHelp();
    }
}

