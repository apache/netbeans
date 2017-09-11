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

import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.*;
import org.netbeans.test.subversion.operators.actions.RevertAction;

/** Class implementing all necessary methods for handling "Revert Modifications" NbDialog.
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
    
    /** Selects nodes and call revert action on them.
     * @param nodes an array of nodes
     * @return RevertModificationsOperator instance
     */
    public static RevertModificationsOperator invoke(Node[] nodes) {
        new RevertAction().perform(nodes);
        return new RevertModificationsOperator();
    }
    
    /** Selects node and call switch action on it.
     * @param node node to be selected
     * @return SwitchOperator instance
     */
    public static RevertModificationsOperator invoke(Node node) {
        return invoke(new Node[] {node});
    }

    private JLabelOperator _lblStartWithRevision;
    private JLabelOperator _lblEndWithRevision;
    private JTextFieldOperator _txtStartRevision;
    private JTextFieldOperator _txtEndRevision;
    private JTextFieldOperator _txtRevision;
    private JLabelOperator _lblEmptyMeansRepositoryHEAD;
    private JButtonOperator _btSearch;
    private JButtonOperator _btSearch2;
    private JButtonOperator _btSearch3;
    private JLabelOperator _lblRevertModificationsFrom;
    private JRadioButtonOperator _rbPreviousCommits;
    private JRadioButtonOperator _rbLocalChanges;
    private JRadioButtonOperator _rbSingleCommit;
    private JButtonOperator _btRevert;
    private JButtonOperator _btCancel;
    private JButtonOperator _btHelp;


    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find "Start with Revision:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblStartWithRevision() {
        if (_lblStartWithRevision==null) {
            _lblStartWithRevision = new JLabelOperator(this, "Starting");
        }
        return _lblStartWithRevision;
    }

    /** Tries to find "End with Revision:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblEndWithRevision() {
        if (_lblEndWithRevision==null) {
            _lblEndWithRevision = new JLabelOperator(this, "Ending");
        }
        return _lblEndWithRevision;
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

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtEndRevision() {
        if (_txtEndRevision==null) {
            _txtEndRevision = new JTextFieldOperator(this,1);
        }
        return _txtEndRevision;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtRevision() {
        if (_txtRevision==null) {
            _txtRevision = new JTextFieldOperator(this, 0);
        }
        return _txtRevision;
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

    /** Tries to find "Search..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btSearch() {
        if (_btSearch==null) {
            _btSearch = new JButtonOperator(this, "Search...");
        }
        return _btSearch;
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

    /** Tries to find "Search..." JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btSearch3() {
        if (_btSearch3==null) {
            _btSearch3 = new JButtonOperator(this, "Search...", 2);
        }
        return _btSearch3;
    }
    
    /** Tries to find "Previous Commit(s)" JRadioButton in this dialog.
     * @return JRadioButtonOperator
     */
    public JRadioButtonOperator rbPreviousCommits() {
        if (_rbPreviousCommits==null) {
            _rbPreviousCommits = new JRadioButtonOperator(this, "Revert Modifications from Previous Commits");
        }
        return _rbPreviousCommits;
    }
    
    /** Tries to find "Single Commit" JRadioButton in this dialog.
     * @return JRadioButtonOperator
     */
    public JRadioButtonOperator rbSingleCommit() {
        if (_rbSingleCommit==null) {
            _rbSingleCommit = new JRadioButtonOperator(this, "Revert Modifications from Single Commit");
        }
        return _rbSingleCommit;
    }

    /** Tries to find "Local Changes" JRadioButton in this dialog.
     * @return JRadioButtonOperator
     */
    public JRadioButtonOperator rbLocalChanges() {
        if (_rbLocalChanges==null) {
            _rbLocalChanges = new JRadioButtonOperator(this, "Revert Local Changes");
        }
        return _rbLocalChanges;
    }

    /** Tries to find "Revert" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btRevert() {
        if (_btRevert==null) {
            _btRevert = new JButtonOperator(this, "Revert");
        }
        return _btRevert;
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
     * gets text for txtStartRevision
     * 
     * @return String text
     */
    public String getTxtStartRevision() {
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

    /**
     * gets text for txtEndRevision
     * 
     * @return String text
     */
    public String getEndRevision() {
        return txtEndRevision().getText();
    }

    /**
     * gets text for txtRevision
     * 
     * @return String text
     */
    public String getRevision() {
        return txtRevision().getText();
    }
    
    /**
     * sets text for txtRevision
     * 
     * @param text String text
     */
    public void setRevision(String text) {
        txtRevision().clearText();
        txtRevision().typeText(text);
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
    public void search() {
        btSearch().push();
    }

    /** clicks on "Search..." JButton
     */
    public void search2() {
        btSearch2().push();
    }

    /** clicks on "Previous Commit(s)" JRadioButton
     */
    public void previousCommits() {
        rbPreviousCommits().push();
    }

    /** clicks on "Local Changes" JRadioButton
     */
    public void localChanges() {
        rbLocalChanges().push();
    }

    /** clicks on "Revert" JButton
     */
    public void revert() {
        btRevert().push();
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
     * Performs verification of RevertModificationsOperator by accessing all its components.
     */
    public void verify() {
        lblStartWithRevision();
        lblEndWithRevision();
        txtStartRevision();
        txtEndRevision();
       // lblEmptyMeansRepositoryHEAD();
        btSearch();
        btSearch2();
        rbPreviousCommits();
        rbLocalChanges();
        btRevert();
        btCancel();
        btHelp();
    }
}

