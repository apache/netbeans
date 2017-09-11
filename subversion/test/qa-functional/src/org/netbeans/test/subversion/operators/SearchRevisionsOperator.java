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
import org.netbeans.jemmy.operators.*;

/** Class implementing all necessary methods for handling "Search Revisions" NbDialog.
 *
 * @author pvcs
 * @version 1.0
 */
public class SearchRevisionsOperator extends NbDialogOperator {

    /** Creates new SearchRevisions that can handle it.
     */
    public SearchRevisionsOperator() {
        super("Search Revisions");
    }

    private JListOperator _lstJList;
    private JButtonOperator _btMetalScrollButton;
    private JButtonOperator _btMetalScrollButton2;
    private JLabelOperator _lblListLogEntriesFrom;
    private JTextFieldOperator _txtFrom;
    private JLabelOperator _lblYYYYMMDDEmptyMeansAll;
    private JButtonOperator _btList;
    private JButtonOperator _btOK;
    private JButtonOperator _btCancel;
    private JButtonOperator _btHelp;


    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find null JList in this dialog.
     * @return JListOperator
     */
    public JListOperator lstJList() {
        if (_lstJList==null) {
            _lstJList = new JListOperator(this);
        }
        return _lstJList;
    }

    /** Tries to find null MetalScrollButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btMetalScrollButton() {
        if (_btMetalScrollButton==null) {
            _btMetalScrollButton = new JButtonOperator(this);
        }
        return _btMetalScrollButton;
    }

    /** Tries to find null MetalScrollButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btMetalScrollButton2() {
        if (_btMetalScrollButton2==null) {
            _btMetalScrollButton2 = new JButtonOperator(this, 1);
        }
        return _btMetalScrollButton2;
    }

    /** Tries to find "List log entries from: " JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblListLogEntriesFrom() {
        if (_lblListLogEntriesFrom==null) {
            _lblListLogEntriesFrom = new JLabelOperator(this, "List log entries from:");
        }
        return _lblListLogEntriesFrom;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtFrom() {
        if (_txtFrom==null) {
            _txtFrom = new JTextFieldOperator(this);
        }
        return _txtFrom;
    }

    /** Tries to find "(YYYY-MM-DD - empty means all)" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblYYYYMMDDEmptyMeansAll() {
        if (_lblYYYYMMDDEmptyMeansAll==null) {
            _lblYYYYMMDDEmptyMeansAll = new JLabelOperator(this, "(YYYY-MM-DD - empty means all)");
        }
        return _lblYYYYMMDDEmptyMeansAll;
    }

    /** Tries to find "List" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btList() {
        if (_btList==null) {
            _btList = new JButtonOperator(this, "List");
        }
        return _btList;
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

    public void selectListItem(int index) {
        lstJList().setSelectedIndex(index);
    }
    
    public void selectListItem(Object item) {
        lstJList().setSelectedValue(item, true);
    }
    
    public Object getSelectItem() {
        return lstJList().getSelectedValue();
    }
    
    /** clicks on null MetalScrollButton
     */
    public void metalScrollButton() {
        btMetalScrollButton().push();
    }

    /** clicks on null MetalScrollButton
     */
    public void metalScrollButton2() {
        btMetalScrollButton2().push();
    }

    /** gets text for txtJTextField
     * @return String text
     */
    public String getFrom() {
        return txtFrom().getText();
    }

    /** sets text for txtJTextField
     * @param text String text
     */
    public void setFrom(String text) {
        txtFrom().setText(text);
    }

    /** clicks on "List" JButton
     */
    @Override
    public void list() {
        btList().push();
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

    /** Performs verification of SearchRevisions by accessing all its components.
     */
    public void verify() {
        lstJList();
        btMetalScrollButton();
        btMetalScrollButton2();
        lblListLogEntriesFrom();
        txtFrom();
        lblYYYYMMDDEmptyMeansAll();
        btList();
        btOK();
        btCancel();
        btHelp();
    }
}

