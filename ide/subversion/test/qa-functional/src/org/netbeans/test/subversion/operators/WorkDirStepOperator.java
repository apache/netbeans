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
/*
 * WorkDirStepOperator.java
 *
 * Created on 19/04/06 13:25
 */
package org.netbeans.test.subversion.operators;

import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jemmy.operators.*;

/**
 * Class implementing all necessary methods for handling "WorkDirStepOperator" NbDialog.
 *
 *
 * @author peter
 * @version 1.0
 */
public class WorkDirStepOperator extends WizardOperator {

    /**
     * Creates new WorkDirStepOperator that can handle it.
     */
    public WorkDirStepOperator() {
        super("");
        stepsWaitSelectedValue("Folders to Checkout");
    }

    private JLabelOperator _lblSteps;
    private JListOperator _lstSteps;
    private JLabelOperator _lblWorkdir;
    private JLabelOperator _lblSpecifyTheFoldersToCheckoutFromSubversionRepository;
    private JLabelOperator _lblRepositoryRevision;
    private JLabelOperator _lblRepositoryFolders;
    private JTextFieldOperator repositoryFolder;
    private JButtonOperator _btSearch;
    private JLabelOperator _lblLocalSubversionWorkingCopy;
    private JTextFieldOperator repositoryRevision;
    private JLabelOperator _lblEmptyMeansRepositoryHEAD;
    private JTextFieldOperator localFolder;
    private JButtonOperator _btBrowseRepository;
    private JButtonOperator _btBrowseLocalFolder;
    private JLabelOperator _lblLocalFolder;
    private JLabelOperator _lblSpecifyTheLocalFolderToCheckoutFoldersInto;
    private JLabelOperator _lblWizardDescriptor$FixedHeightLabel;
    private JButtonOperator _btBack;
    private JButtonOperator _btNext;
    private JButtonOperator _btFinish;
    private JButtonOperator _btCancel;
    private JButtonOperator _btHelp;
    private JCheckBoxOperator _cbCheckoutContentOnly;


    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find "Steps" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblSteps() {
        if (_lblSteps==null) {
            _lblSteps = new JLabelOperator(this, "Steps");
        }
        return _lblSteps;
    }

    /** Tries to find "Revert Local Changes" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbCheckoutContentOnly() {
        if (_cbCheckoutContentOnly==null) {
            _cbCheckoutContentOnly = new JCheckBoxOperator(this, "Checkout only");
        }
        return _cbCheckoutContentOnly;
    }
    
    /** Tries to find null JList in this dialog.
     * @return JListOperator
     */
    @Override
    public JListOperator lstSteps() {
        if (_lstSteps==null) {
            _lstSteps = new JListOperator(this);
        }
        return _lstSteps;
    }

    /** Tries to find "Specify the folder(s) to checkout from Subversion repository." JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblSpecifyTheFoldersToCheckoutFromSubversionRepository() {
        if (_lblSpecifyTheFoldersToCheckoutFromSubversionRepository==null) {
            _lblSpecifyTheFoldersToCheckoutFromSubversionRepository = new JLabelOperator(this, "Specify the folder(s) to checkout from Subversion repository.");
        }
        return _lblSpecifyTheFoldersToCheckoutFromSubversionRepository;
    }

    /** Tries to find "Repository Revision:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblRepositoryRevision() {
        if (_lblRepositoryRevision==null) {
            _lblRepositoryRevision = new JLabelOperator(this, "Repository Revision:");
        }
        return _lblRepositoryRevision;
    }

    /** Tries to find "Repository Folder(s):" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblRepositoryFolders() {
        if (_lblRepositoryFolders==null) {
            _lblRepositoryFolders = new JLabelOperator(this, "Repository Folder(s):");
        }
        return _lblRepositoryFolders;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtRepositoryFolder() {
        if (repositoryFolder==null) {
            repositoryFolder = new JTextFieldOperator(this);
        }
        return repositoryFolder;
    }
    
    public void setRepositoryFolder(String text) {
        txtRepositoryFolder().clearText();
        txtRepositoryFolder().typeText(text);
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

    /** Tries to find "(local Subversion working copy) " JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblLocalSubversionWorkingCopy() {
        if (_lblLocalSubversionWorkingCopy==null) {
            _lblLocalSubversionWorkingCopy = new JLabelOperator(this, "(local Subversion working copy)");
        }
        return _lblLocalSubversionWorkingCopy;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtRepositoryRevision() {
        if (repositoryRevision==null) {
            repositoryRevision = new JTextFieldOperator(this, 1);
        }
        return repositoryRevision;
    }
    
    public void setRepositoryRevision(String text) {
        txtRepositoryRevision().clearText();
        txtRepositoryRevision().typeText(text);
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

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtLocalFolder() {
        if (localFolder==null) {
            localFolder = new JTextFieldOperator(this, 2);
        }
        return localFolder;
    }
    
    public void setLocalFolder(String text) {
        txtLocalFolder().clearText();
        txtLocalFolder().typeText(text);
    }

    /** Tries to find "Browse..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btBrowseRepository() {
        if (_btBrowseRepository==null) {
            _btBrowseRepository = new JButtonOperator(this, "Browse");
        }
        return _btBrowseRepository;
    }

    /** Tries to find "Browse..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btBrowseLocalFolder() {
        if (_btBrowseLocalFolder==null) {
            _btBrowseLocalFolder = new JButtonOperator(this, "Browse", 1);
        }
        return _btBrowseLocalFolder;
    }

    /** Tries to find "Local Folder:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblLocalFolder() {
        if (_lblLocalFolder==null) {
            _lblLocalFolder = new JLabelOperator(this, "Local Folder:");
        }
        return _lblLocalFolder;
    }

    /** Tries to find "Specify the local folder to checkout folders into." JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblSpecifyTheLocalFolderToCheckoutFoldersInto() {
        if (_lblSpecifyTheLocalFolderToCheckoutFoldersInto==null) {
            _lblSpecifyTheLocalFolderToCheckoutFoldersInto = new JLabelOperator(this, "Specify the local folder to checkout folders into.");
        }
        return _lblSpecifyTheLocalFolderToCheckoutFoldersInto;
    }

    /** Tries to find " " WizardDescriptor$FixedHeightLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblWizardDescriptor$FixedHeightLabel() {
        if (_lblWizardDescriptor$FixedHeightLabel==null) {
            _lblWizardDescriptor$FixedHeightLabel = new JLabelOperator(this, " ", 7);
        }
        return _lblWizardDescriptor$FixedHeightLabel;
    }

    /** Tries to find "< Back" JButton in this dialog.
     * @return JButtonOperator
     */
    @Override
    public JButtonOperator btBack() {
        if (_btBack==null) {
            _btBack = new JButtonOperator(this, "< Back");
        }
        return _btBack;
    }

    /** Tries to find "Next >" JButton in this dialog.
     * @return JButtonOperator
     */
    @Override
    public JButtonOperator btNext() {
        if (_btNext==null) {
            _btNext = new JButtonOperator(this, "Next >");
        }
        return _btNext;
    }

    /** Tries to find "Finish" JButton in this dialog.
     * @return JButtonOperator
     */
    @Override
    public JButtonOperator btFinish() {
        if (_btFinish==null) {
            _btFinish = new JButtonOperator(this, "Finish");
        }
        return _btFinish;
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

    /**
     * gets text for txtRepositoryFolder
     * 
     * @return String text
     */
    public String getRepositoryFolder() {
        return txtRepositoryFolder().getText();
    }

    /** clicks on "Search..." JButton
     */
    public SearchRevisionsOperator search() {
        btSearch().pushNoBlock();
        return new SearchRevisionsOperator();
    }

    /**
     * gets text for txtRepositoryRevision
     * 
     * @return String text
     */
    public String getRevisionNumber() {
        return txtRepositoryRevision().getText();
    }

    /**
     * gets text for txtLocalFolder
     * 
     * @return String text
     */
    public String getLocalFolder() {
        return txtLocalFolder().getText();
    }

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkCheckoutContentOnly(boolean state) {
        if (cbCheckoutContentOnly().isSelected()!=state) {
            cbCheckoutContentOnly().push();
        }
    }
    
    /** clicks on "< Back" JButton
     */
    @Override
    public void back() {
        btBack().push();
    }

    /** clicks on "Next >" JButton
     */
    @Override
    public void next() {
        btNext().push();
    }

    /** clicks on "Finish" JButton
     */
    @Override
    public void finish() {
        btFinish().push();
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
    
    public RepositoryBrowserOperator browseRepository() {
        btBrowseRepository().pushNoBlock();
        return new RepositoryBrowserOperator();
    }
    
    /** clicks on "Browse..." and returns JFileChooserOperator instance.
     * @return instance of JFileChooserOperator
     */
    public JFileChooserOperator browseLocalFolder() {
        btBrowseLocalFolder().pushNoBlock();
        return new JFileChooserOperator();
    }


    //*****************************************
    // High-level functionality definition part
    //*****************************************

    /**
     * Performs verification of WorkDirStepOperator by accessing all its components.
     */
    @Override
    public void verify() {
        //lblSteps();
        //lstSteps();
        lblSpecifyTheFoldersToCheckoutFromSubversionRepository();
        lblRepositoryRevision();
        lblRepositoryFolders();
        txtRepositoryFolder();
        btSearch();
//        lblLocalSubversionWorkingCopy();
        txtRepositoryRevision();
        //lblEmptyMeansRepositoryHEAD();
        //txtLocalFolder();
        //btBrowseRepository();
        //btBrowseLocalFolder();
        //lblLocalFolder();
        //cbCheckoutContentOnly();
        //lblSpecifyTheLocalFolderToCheckoutFoldersInto();
        //lblWizardDescriptor$FixedHeightLabel();
        btBack();
        btNext();
        btFinish();
        btCancel();
        btHelp();
    }
}

