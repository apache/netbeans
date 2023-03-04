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
import org.netbeans.jellytools.nodes.Node;
import org.netbeans.jemmy.operators.*;
import org.netbeans.test.subversion.operators.actions.SvnPropertiesAction;

/** Class implementing all necessary methods for handling "Svn Properties Editor" NbDialog.
 *
 * @author novakm
 * @version 1.0
 */
public class SvnPropertiesOperator extends NbDialogOperator {

    /** Creates new SvnPropertiesEditor that can handle it.
     */
    public SvnPropertiesOperator() {
        super("Svn Properties Editor");
    }

    private JButtonOperator _btWindowsScrollBarUI$WindowsArrowButton;
    private JButtonOperator _btWindowsScrollBarUI$WindowsArrowButton2;
    private JLabelOperator _lblPropertyValue;
    private JLabelOperator _lblPropertyName;
    private JComboBoxOperator _cboPropertyName;
    public static final String ITEM_SVNEOLSTYLE = "svn:eol-style";
    public static final String ITEM_SVNEXECUTABLE = "svn:executable";
    public static final String ITEM_SVNKEYWORDS = "svn:keywords";
    public static final String ITEM_SVNNEEDSLOCK = "svn:needs-lock";
    public static final String ITEM_SVNMIMETYPE = "svn:mime-type";
    private JButtonOperator _btLoad;
    private JTextAreaOperator _txtPropertyValue;
    private JButtonOperator _btAdd;
    private JCheckBoxOperator _cbRecursively;
    private JButtonOperator _btRemove;
    private JButtonOperator _btRefresh;
    private JLabelOperator _lblSubversionProperties;
    private JButtonOperator _btCancel;
    private JButtonOperator _btHelp;
    private JTableOperator _propTable;


    //******************************
    // Subcomponents definition part
    //******************************
    /** Tries to find null WindowsScrollBarUI$WindowsArrowButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btWindowsScrollBarUI$WindowsArrowButton() {
        if (_btWindowsScrollBarUI$WindowsArrowButton == null) {
            _btWindowsScrollBarUI$WindowsArrowButton = new JButtonOperator(this);
        }
        return _btWindowsScrollBarUI$WindowsArrowButton;
    }

    /** Tries to find null WindowsScrollBarUI$WindowsArrowButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btWindowsScrollBarUI$WindowsArrowButton2() {
        if (_btWindowsScrollBarUI$WindowsArrowButton2 == null) {
            _btWindowsScrollBarUI$WindowsArrowButton2 = new JButtonOperator(this, 1);
        }
        return _btWindowsScrollBarUI$WindowsArrowButton2;
    }

    public static SvnPropertiesOperator invoke(Node node) {
        new SvnPropertiesAction().perform(node);
        return new SvnPropertiesOperator();
    }

    /** Tries to find "Property Value:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblPropertyValue() {
        if (_lblPropertyValue == null) {
            _lblPropertyValue = new JLabelOperator(this, "Property Value:");
        }
        return _lblPropertyValue;
    }

    /** Tries to find "Property Name:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblPropertyName() {
        if (_lblPropertyName == null) {
            _lblPropertyName = new JLabelOperator(this, "Property Name:");
        }
        return _lblPropertyName;
    }

    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboPropertyName() {
        if (_cboPropertyName == null) {
            _cboPropertyName = new JComboBoxOperator(this);
        }
        return _cboPropertyName;
    }

    /** Tries to find "Load" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btLoad() {
        if (_btLoad == null) {
            _btLoad = new JButtonOperator(this, "Load");
        }
        return _btLoad;
    }

    /** Tries to find null JTextArea in this dialog.
     * @return JTextAreaOperator
     */
    public JTextAreaOperator txtPropertyValue() {
        if (_txtPropertyValue == null) {
            _txtPropertyValue = new JTextAreaOperator(this);
        }
        return _txtPropertyValue;
    }

    /** Tries to find "Add" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btAdd() {
        if (_btAdd == null) {
            _btAdd = new JButtonOperator(this, "Add");
        }
        return _btAdd;
    }

    /** Tries to find "Recursively" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbRecursively() {
        if (_cbRecursively == null) {
            _cbRecursively = new JCheckBoxOperator(this, "Recursively");
        }
        return _cbRecursively;
    }

    /** Tries to find "Remove" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btRemove() {
        if (_btRemove == null) {
            _btRemove = new JButtonOperator(this, "Remove");
        }
        return _btRemove;
    }

    /** Tries to find "Refresh" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btRefresh() {
        if (_btRefresh == null) {
            _btRefresh = new JButtonOperator(this, "Refresh");
        }
        return _btRefresh;
    }

    /** Tries to find "Subversion Properties:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblSubversionProperties() {
        if (_lblSubversionProperties == null) {
            _lblSubversionProperties = new JLabelOperator(this, "Subversion Properties:");
        }
        return _lblSubversionProperties;
    }

    /** Tries to find "Cancel " JButton in this dialog.
     * @return JButtonOperator
     */
    @Override
    public JButtonOperator btCancel() {
        if (_btCancel == null) {
            _btCancel = new JButtonOperator(this, "Close");
        }
        return _btCancel;
    }

    /** Tries to find "Help" JButton in this dialog.
     * @return JButtonOperator
     */
    @Override
    public JButtonOperator btHelp() {
        if (_btHelp == null) {
            _btHelp = new JButtonOperator(this, "Help");
        }
        return _btHelp;
    }


    //****************************************
    // Low-level functionality definition part
    //****************************************
    /** clicks on null WindowsScrollBarUI$WindowsArrowButton
     */
    public void windowsScrollBarUI$WindowsArrowButton() {
        btWindowsScrollBarUI$WindowsArrowButton().push();
    }

    /** clicks on null WindowsScrollBarUI$WindowsArrowButton
     */
    public void windowsScrollBarUI$WindowsArrowButton2() {
        btWindowsScrollBarUI$WindowsArrowButton2().push();
    }

    /** returns selected item for cboPropertyName
     * @return String item
     */
    public String getSelectedPropertyName() {
        return cboPropertyName().getSelectedItem().toString();
    }

    /** selects item for cboPropertyName
     * @param item String item
     */
    public void selectPropertyName(String item) {
        cboPropertyName().selectItem(item);
    }

    /** types text for cboPropertyName
     * @param text String text
     */
    public void typePropertyName(String text) {
        cboPropertyName().clearText();
        cboPropertyName().typeText(text);
    }

    /** clicks on "Load" JButton
     */
    public void load() {
        btLoad().push();
    }

    /** gets text for txtPropertyValue
     * @return String text
     */
    public String getPropertyValue() {
        return txtPropertyValue().getText();
    }

    /** sets text for txtPropertyValue
     * @param text String text
     */
    public void setPropertyValue(String text) {
        txtPropertyValue().setText(text);
    }

    /** types text for txtPropertyValue
     * @param text String text
     */
    public void typePropertyValue(String text) {
        txtPropertyValue().typeText(text);
    }

    /** clicks on "Add" JButton
     */
    public void add() {
        btAdd().push();
    }

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkRecursively(boolean state) {
        if (cbRecursively().isSelected() != state) {
            cbRecursively().push();
        }
    }
    
    public JTableOperator propertiesTable() {
        if (_propTable==null) {
            _propTable = new JTableOperator(this);
        }
        return _propTable;
    }
    
    /** clicks on "Remove" JButton
     */
    public void remove() {
        btRemove().push();
    }

    /** clicks on "Refresh" JButton
     */
    public void refresh() {
        btRefresh().push();
    }

    /** clicks on "Cancel " JButton
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
    /** Performs verification of SvnPropertiesEditor by accessing all its components.
     */
    public void verify() {
        btWindowsScrollBarUI$WindowsArrowButton();
        btWindowsScrollBarUI$WindowsArrowButton2();
        lblPropertyValue();
        lblPropertyName();
        cboPropertyName();
        btLoad();
        txtPropertyValue();
        btAdd();
        cbRecursively();
        btRemove();
        btRefresh();
        lblSubversionProperties();
        btCancel();
        btHelp();
    }

    /** Performs simple test of SvnPropertiesEditor
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new SvnPropertiesOperator().verify();
        System.out.println("SvnPropertiesEditor verification finished.");
    }
}
