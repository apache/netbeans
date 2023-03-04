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

