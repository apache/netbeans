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
package org.netbeans.test.subversion.operators;

import org.netbeans.jemmy.operators.*;

/**
 * Class implementing all necessary methods for handling "FolderToImportStepOperator" NbDialog.
 * 
 * 
 * 
 * @author peter
 * @version 1.0
 */
public class FolderToImportStepOperator extends ImportWizardOperator {

    /**
     * Creates new FolderToImportStepOperator that can handle it.
     */
    public FolderToImportStepOperator() {
        super();
        stepsWaitSelectedValue("Repository folder");
    }

    private JLabelOperator _lblSteps;
    private JListOperator _lstSteps;
    private JLabelOperator _lblRepositoryFolder;
    private JTextAreaOperator _txtImportMessage;
    private JLabelOperator _lblRepositoryFolder2;
    private JTextFieldOperator _txtRepositoryFolder;
    private JButtonOperator _btBrowse;
    private JLabelOperator _lblSpecifyTheRepositoryFolderYouWantToImportIn;
    private JLabelOperator _lblSpecifyTheMessage;
    private JTextPaneOperator _txtPaneImportMessageRequired;
    private JButtonOperator _btStop;
    private JButtonOperator _btBack;
    private JButtonOperator _btNext;
    private JButtonOperator _btFinish;
    private JButtonOperator _btCancel;
    private JButtonOperator _btHelp;


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

    /** Tries to find "Repository folder" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblRepositoryFolder() {
        if (_lblRepositoryFolder==null) {
            _lblRepositoryFolder = new JLabelOperator(this, "Repository folder");
        }
        return _lblRepositoryFolder;
    }

    /** Tries to find null JTextArea in this dialog.
     * @return JTextAreaOperator
     */
    public JTextAreaOperator txtJTextArea() {
        if (_txtImportMessage==null) {
            _txtImportMessage = new JTextAreaOperator(this);
        }
        return _txtImportMessage;
    }

    /** Tries to find "Repository Folder:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblRepositoryFolder2() {
        if (_lblRepositoryFolder2==null) {
            _lblRepositoryFolder2 = new JLabelOperator(this, "Repository Folder:");
        }
        return _lblRepositoryFolder2;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtJTextField() {
        if (_txtRepositoryFolder==null) {
            _txtRepositoryFolder = new JTextFieldOperator(this);
        }
        return _txtRepositoryFolder;
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

    /** Tries to find "Browse..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btBrowse() {
        if (_btBrowse==null) {
            _btBrowse = new JButtonOperator(this, "Browse...");
        }
        return _btBrowse;
    }

    /** Tries to find "Specify the repository folder you want to import in" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblSpecifyTheRepositoryFolderYouWantToImportIn() {
        if (_lblSpecifyTheRepositoryFolderYouWantToImportIn==null) {
            _lblSpecifyTheRepositoryFolderYouWantToImportIn = new JLabelOperator(this, "Specify the repository folder you want to import in");
        }
        return _lblSpecifyTheRepositoryFolderYouWantToImportIn;
    }

    /** Tries to find "Specify the message:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblSpecifyTheMessage() {
        if (_lblSpecifyTheMessage==null) {
            _lblSpecifyTheMessage = new JLabelOperator(this, "Specify the message:");
        }
        return _lblSpecifyTheMessage;
    }

    /**
     * Tries to find "FolderToImportStepOperator message required" WizardDescriptor$FixedHeightLabel in this dialog.
     * 
     * 
     * @return JLabelOperator
     */
    public JTextPaneOperator lblImportMessageRequired() {
        if (_txtPaneImportMessageRequired==null) {
            _txtPaneImportMessageRequired = new JTextPaneOperator(this);    
        }
        return _txtPaneImportMessageRequired;
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

    /** gets text for txtJTextArea
     * @return String text
     */
    public String getImportMessage() {
        return txtJTextArea().getText();
    }

    /** sets text for txtJTextArea
     * @param text String text
     */
    public void setImportMessage(String text) {
        txtJTextArea().clearText();
        txtJTextArea().setText(text);
    }

    /** gets text for txtJTextField
     * @return String text
     */
    public String getRepositoryFolder() {
        return txtJTextField().getText();
    }

    /** sets text for txtJTextField
     * @param text String text
     */
    public void setRepositoryFolder(String text) {
        txtJTextField().setText(text);
    }

    public void browse() {
        btBrowse().push();
    }
    
    public RepositoryBrowserImpOperator browseRepository() {
        btBrowse().pushNoBlock();
        return new RepositoryBrowserImpOperator();
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


    //*****************************************
    // High-level functionality definition part
    //*****************************************

    /**
     * Performs verification of FolderToImportStepOperator by accessing all its components.
     */
    @Override
    public void verify() {
        lblSteps();
        lstSteps();
        lblRepositoryFolder();
        txtJTextArea();
        lblRepositoryFolder2();
        txtJTextField();
        btBrowse();
        lblSpecifyTheRepositoryFolderYouWantToImportIn();
        lblSpecifyTheMessage();
        lblImportMessageRequired();
        btBack();
        btNext();
        btFinish();
        btCancel();
        btHelp();
    }
}

