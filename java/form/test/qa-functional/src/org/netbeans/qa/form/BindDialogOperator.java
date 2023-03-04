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

package org.netbeans.qa.form;

import org.netbeans.jemmy.operators.*;

/**
 * Class implementing all necessary methods for handling "Bind" NbDialog.
 *
 * @author Jiri Vagner
 */
public class BindDialogOperator extends JDialogOperator {
    
    private JButtonOperator _btCancel;
    private JButtonOperator _btOk;    
    private JTabbedPaneOperator _tbdPane;
    private JComboBoxOperator _cboBindSource;
    private JComboBoxOperator _cboBindExpression;
    private JComboBoxOperator _cboUpdateMode;
    private JComboBoxOperator _cboConverter;    
    private JComboBoxOperator _cboValidator;
    private JTextFieldOperator _txtBindExpression;
    private JCheckBoxOperator _chbNullValue;
    private JCheckBoxOperator _chbIncompletePathValue;    
    
    public static String READ_WRITE_UPDATE_MODE = "(read/write)";  // NOI18N
    public static String READ_ONLY_UPDATE_MODE = "(read-only)";  // NOI18N
    public static String READ_ONCE_UPDATE_MODE = "(read once)";  // NOI18N

    /**
     * Creates new instance using default name
     */
    public BindDialogOperator() {
        super("Bind"); // NOI18N
    }
    
    /**
     * Creates new instance using dialog name
     */
    public BindDialogOperator(String name) {
        super(name);
    }
    

    /** Tries to find JTextFieldOperator in this dialog.
     * @return JTextFieldOperator
     */
    private JTextFieldOperator txtBindExpression() {
        if (_txtBindExpression==null) {
            _txtBindExpression = new JTextFieldOperator(cboBindExpression());
        }
        return _txtBindExpression;
    }
    
    /** Tries to find JComboBoxOperator in this dialog.
     * @return JComboBoxOperator
     */
    private JComboBoxOperator cboBindSource() {
        if (_cboBindSource==null) {
            _cboBindSource = new JComboBoxOperator(tbdPane(),0);
        }
        return _cboBindSource;
    }

    
    /** Tries to find JComboBoxOperator in Advanced tab.
     * @return JComboBoxOperator
     */
    private JComboBoxOperator cboUpdateMode() {
        if (_cboUpdateMode ==null) {
            _cboUpdateMode = new JComboBoxOperator(tbdPane(),0);
        }
        return _cboUpdateMode;
    }
    
    /** Tries to find JComboBoxOperator in Advanced tab.
     * @return JComboBoxOperator
     */
    private JComboBoxOperator cboConverter() {
        if (_cboConverter ==null) {
            _cboConverter = new JComboBoxOperator(tbdPane(),1);
        }
        return _cboConverter;
    }

    /** Tries to find JComboBoxOperator in Advanced tab.
     * @return JComboBoxOperator
     */
    private JComboBoxOperator cboValidator() {
        if (_cboValidator ==null) {
            _cboValidator = new JComboBoxOperator(tbdPane(),2);
        }
        return _cboValidator;
    }
    
    /** Tries to find JComboBoxOperator in this dialog.
     * @return JComboBoxOperator
     */
    private JComboBoxOperator cboBindExpression() {
        if (_cboBindExpression==null) {
            _cboBindExpression = new JComboBoxOperator(tbdPane(),1);
        }
        return _cboBindExpression;
    }
    
    /** Tries to find JTabbedPaneOperator in this dialog.
     * @return JTabbedPaneOperator
     */
    public JTabbedPaneOperator tbdPane() {
        if (_tbdPane==null) {
            _tbdPane = new JTabbedPaneOperator(this);
        }
        return _tbdPane;
    }

    /** Tries to find JButtonOperator in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btCancel() {
        if (_btCancel==null) {
            _btCancel = new JButtonOperator(this, "Cancel");  // NOI18N
        }
        return _btCancel;
    }

    /** Tries to find JButtonOperator in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btOk() {
        if (_btOk==null) {
            _btOk = new JButtonOperator(this, "Ok"); // NOI18N
        }
        return _btOk;
    }

    /** Tries to find JCheckBoxOperator in Advanced tab.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator chbNullValue() {
        if (_chbNullValue == null) {
            _chbNullValue = new JCheckBoxOperator(tbdPane(), "Null"); // NOI18N
        }
        return _chbNullValue;
    }

    /** Tries to find JCheckBoxOperator in Advanced tab.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator chbIncompletePathValue() {
        if (_chbIncompletePathValue == null) {
            _chbIncompletePathValue = new JCheckBoxOperator(tbdPane(), "Incomplete"); // NOI18N
        }
        return _chbIncompletePathValue;
    }
    
    /** Clicks Cancel button */
    public void cancel() {
        btCancel().push();
    }

    /** Clicks OK button */
    public void ok() {
        btOk().push();
    }

    /** Clicks Null Value checkbox */
    public void selectNullValue() {
        if (!chbNullValue().isSelected())
            chbNullValue().clickMouse();
    }

    /** Unselect Incomplete Path Value checkbox */
    public void unselectNullValue() {
        if (chbNullValue().isSelected())
            chbNullValue().clickMouse();
    }
    
    /** Select Incomplete Path Value checkbox */
    public void selectIncompletePathValue() {
        if (!chbIncompletePathValue().isSelected())
            chbIncompletePathValue().clickMouse();
    }
    
    /** Unselect Incomplete Path Value checkbox */
    public void unselectIncompletePathValue() {
        if (chbIncompletePathValue().isSelected())
            chbIncompletePathValue().clickMouse();
    }
    
    /** Set Null Value text */
    public void setNullValueText(String text) {
        // TODO: remove this ID and get button better way        
        new JButtonOperator(tbdPane(), 4).pushNoBlock();
        setTextIntoValueDialog(text);        
    }

    /** Get Null Value text */
    public String getNullValueText() {
        // TODO: remove this ID and get button better way        
        new JButtonOperator(tbdPane(), 4).pushNoBlock();
        return getTextIntoValueDialog();
    }
    
    /** Set Incomplete Path Value text */
    public void setIncompletePathValueText(String text) {
        // TODO: remove this ID and get button better way
        new JButtonOperator(tbdPane(), 4).pushNoBlock();
        setTextIntoValueDialog(text);
    }

    /** get Incomplete Path Value text */
    public String getIncompletePathValueText() {
        // TODO: remove this ID and get button better way        
        new JButtonOperator(tbdPane(), 5).pushNoBlock();
        return getTextIntoValueDialog();
    }
    
    private void setTextIntoValueDialog(String text) {
        JDialogOperator dialog = new JDialogOperator("Value");  // NOI18N
        new JComboBoxOperator(dialog).selectItem("Plain text");  // NOI18N
        new JTextAreaOperator(dialog).setText(text);
        new JCheckBoxOperator(dialog).setSelected(true);
        new JButtonOperator(dialog, "OK").push();  // NOI18N
    }

    private String getTextIntoValueDialog() {
        JDialogOperator dialog = new JDialogOperator("Value");  // NOI18N
        String result = (new JTextAreaOperator(dialog)).getText();
        new JButtonOperator(dialog, "Cancel").push();  // NOI18N
        return result;
    }
    
    public void selectAdvancedTab() {
        tbdPane().selectPage(1);
    }
    
    public void selectBindingTab() {
        tbdPane().selectPage(0);
    }
    
    /** selects binding source */
    public void selectBindSource(String item) {
        cboBindSource().selectItem(item);
    }

    /** selects update mode */
    public void selectUpdateMode(String item) {
        cboUpdateMode().selectItem(item);
    }

    /** selects update mode */
    public void selectReadWriteUpdateMode() {
        cboUpdateMode().selectItem(READ_WRITE_UPDATE_MODE);
    }

    /** selects update mode */
    public void selectReadOnlyUpdateMode() {
        cboUpdateMode().selectItem(READ_ONLY_UPDATE_MODE);
    }

    /** selects update mode */
    public void selectReadOnceUpdateMode() {
        cboUpdateMode().selectItem(READ_ONCE_UPDATE_MODE);
    }

    /** selects converter from list */
    public void selectConverter(String converterName) {
        cboConverter().selectItem(converterName);        
    }

    /** gets selected converter */
    public String getSelectedConverter() {
        return cboConverter().getSelectedItem().toString();        
    }

    /** selects validator from list */
    public void selectValidator(String validatorName) {
        cboValidator().selectItem(validatorName);        
    }

    /** gets selected validator */
    public String getValidator() {
        return cboValidator().getSelectedItem().toString();        
    }
    
    /** gets selected binding source
     * @return String text
     */    
    public String getSelectedBindSource() {
        return cboBindSource().getSelectedItem().toString();
    }

    /** gets selected update mode
     * @return String text
     */    
    public String getSelectedUpdateMode() {
        return cboUpdateMode().getSelectedItem().toString();
    }
    
    /** sets binding expression */    
    public void setBindExpression(String text) {
        txtBindExpression().getFocus();
        txtBindExpression().clearText();
        txtBindExpression().typeText(text);
        btOk().getFocus();
    }
    
    /** returns binding expression
     * @return String text
     */
    public String getBindExpression() {
        return txtBindExpression().getText();
    }
}
