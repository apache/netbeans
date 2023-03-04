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
 * SAXDocumentHandlerWizard.java
 *
 * Created on 8/5/02 4:14 PM
 */
package org.netbeans.jellytools.modules.xml.saxwizard;

import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jemmy.operators.*;

/** Class implementing all necessary methods for handling "SAX Document Handler Wizard" NbDialog.
 *
 * @author ms113234
 * @version 1.0
 */
public class SAXDocumentHandlerWizardPage1 extends WizardOperator {

    /** Creates new SAXDocumentHandlerWizard that can handle it.
     */
    public SAXDocumentHandlerWizardPage1() {
        super("SAX Document Handler Wizard");
    }

    private JLabelOperator _lblSteps;
    private JLabelOperator _lbl1Of4APIVersions;
    private JTextAreaOperator _txtJTextArea;
    private JLabelOperator _lblJAXPVersion;
    private JComboBoxOperator _cboJAXPVersion;
    public static final String ITEM_JAXP10 = "JAXP 1.0"; 
    public static final String ITEM_JAXP11 = "JAXP 1.1"; 
    private JLabelOperator _lblSAXParserVersion;
    private JComboBoxOperator _cboSAXParserVersion;
    public static final String ITEM_SAX10 = "SAX 1.0"; 
    public static final String ITEM_SAX20 = "SAX 2.0"; 
    private JCheckBoxOperator _cbPropagateSAXEventsToGeneratedHandler;
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

    /** Tries to find "1 of 4 - API Versions" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lbl1Of4APIVersions() {
        if (_lbl1Of4APIVersions==null) {
            _lbl1Of4APIVersions = new JLabelOperator(this, "1 of 4 - API Versions");
        }
        return _lbl1Of4APIVersions;
    }

    /** Tries to find null JTextArea in this dialog.
     * @return JTextAreaOperator
     */
    public JTextAreaOperator txtJTextArea() {
        if (_txtJTextArea==null) {
            _txtJTextArea = new JTextAreaOperator(this);
        }
        return _txtJTextArea;
    }

    /** Tries to find "JAXP Version:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblJAXPVersion() {
        if (_lblJAXPVersion==null) {
            _lblJAXPVersion = new JLabelOperator(this, "JAXP Version:");
        }
        return _lblJAXPVersion;
    }

    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboJAXPVersion() {
        if (_cboJAXPVersion==null) {
            _cboJAXPVersion = new JComboBoxOperator(this);
        }
        return _cboJAXPVersion;
    }

    /** Tries to find "SAX Parser Version:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblSAXParserVersion() {
        if (_lblSAXParserVersion==null) {
            _lblSAXParserVersion = new JLabelOperator(this, "SAX Parser Version:");
        }
        return _lblSAXParserVersion;
    }

    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboSAXParserVersion() {
        if (_cboSAXParserVersion==null) {
            _cboSAXParserVersion = new JComboBoxOperator(this, 1);
        }
        return _cboSAXParserVersion;
    }

    /** Tries to find " Propagate SAX Events to Generated Handler" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbPropagateSAXEventsToGeneratedHandler() {
        if (_cbPropagateSAXEventsToGeneratedHandler==null) {
            _cbPropagateSAXEventsToGeneratedHandler = new JCheckBoxOperator(this, " Propagate SAX Events to Generated Handler");
        }
        return _cbPropagateSAXEventsToGeneratedHandler;
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

    /** gets text for txtJTextArea
     * @return String text
     */
    public String getJTextArea() {
        return txtJTextArea().getText();
    }

    /** sets text for txtJTextArea
     * @param text String text
     */
    public void setJTextArea(String text) {
        txtJTextArea().setText(text);
    }

    /** types text for txtJTextArea
     * @param text String text
     */
    public void typeJTextArea(String text) {
        txtJTextArea().typeText(text);
    }

    /** returns selected item for cboJAXPVersion
     * @return String item
     */
    public String getSelectedJAXPVersion() {
        return cboJAXPVersion().getSelectedItem().toString();
    }

    /** selects item for cboJAXPVersion
     * @param item String item
     */
    public void selectJAXPVersion(String item) {
        cboJAXPVersion().selectItem(item);
    }

    /** types text for cboJAXPVersion
     * @param text String text
     */
    public void typeJAXPVersion(String text) {
        cboJAXPVersion().typeText(text);
    }

    /** returns selected item for cboSAXParserVersion
     * @return String item
     */
    public String getSelectedSAXParserVersion() {
        return cboSAXParserVersion().getSelectedItem().toString();
    }

    /** selects item for cboSAXParserVersion
     * @param item String item
     */
    public void selectSAXParserVersion(String item) {
        cboSAXParserVersion().selectItem(item);
    }

    /** types text for cboSAXParserVersion
     * @param text String text
     */
    public void typeSAXParserVersion(String text) {
        cboSAXParserVersion().typeText(text);
    }

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkPropagateSAXEventsToGeneratedHandler(boolean state) {
        if (cbPropagateSAXEventsToGeneratedHandler().isSelected()!=state) {
            cbPropagateSAXEventsToGeneratedHandler().push();
        }
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

    /** Performs verification of SAXDocumentHandlerWizard by accessing all its components.
     */
    public void verify() {
        lblSteps();
        lbl1Of4APIVersions();
        txtJTextArea();
        lblJAXPVersion();
        cboJAXPVersion();
        lblSAXParserVersion();
        cboSAXParserVersion();
        cbPropagateSAXEventsToGeneratedHandler();
        btCancel();
        btHelp();
    }

    /** Performs simple test of SAXDocumentHandlerWizard
    * @param args the command line arguments
    */
    public static void main(String args[]) {
        new SAXDocumentHandlerWizardPage1().verify();
        System.out.println("SAXDocumentHandlerWizardPage1 verification finished.");
    }
}

