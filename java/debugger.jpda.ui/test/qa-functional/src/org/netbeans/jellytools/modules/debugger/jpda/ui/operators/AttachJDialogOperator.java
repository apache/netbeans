/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.jellytools.modules.debugger.jpda.ui.operators;

import org.netbeans.jemmy.operators.*;

/** Class implementing all necessary methods for handling "Attach" NbDialog.
 *
 * @author mg105252
 * @version 1.0
 */
public class AttachJDialogOperator extends JDialogOperator {

    /** Creates new AttachJDialogOperator that can handle it.
     */
    public AttachJDialogOperator() {
        super("Attach");
    }

    private JComboBoxOperator _cboDebugger;
    private JComboBoxOperator _cboConnector;
//    public static final String ITEM_DEFAULTDEBUGGERJPDA = "Default Debugger (JPDA)"; 
    private JTextFieldOperator _txtTransport;
    private JTextFieldOperator _txtHost;
    private JTextFieldOperator _txtPort;
    private JButtonOperator _btOK;
    private JButtonOperator _btCancel;
    private JButtonOperator _btHelp;


    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboDebugger() {
        if (_cboDebugger==null) {
            _cboDebugger = new JComboBoxOperator(this);
        }
        return _cboDebugger;
    }

    public JComboBoxOperator cboConnector() {
        if (_cboConnector==null) {
            _cboConnector = new JComboBoxOperator(this,1);
        }
        return _cboConnector;
    }
    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtTransport() {
        if (_txtTransport==null) {
            _txtTransport = new JTextFieldOperator(this);
        }
        return _txtTransport;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtHost() {
        if (_txtHost==null) {
            _txtHost = new JTextFieldOperator(this, 1);
        }
        return _txtHost;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtPort() {
        if (_txtPort==null) {
            _txtPort = new JTextFieldOperator(this, 2);
        }
        return _txtPort;
    }

    /** Tries to find "OK" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btOK() {
        if (_btOK==null) {
            _btOK = new JButtonOperator(this, "OK");
        }
        return _btOK;
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

    /** returns selected item for cboDebugger
     * @return String item
     */
    public String getSelectedDebugger() {
        return cboDebugger().getSelectedItem().toString();
    }

    /** selects item for cboDebugger
     * @param item String item
     */
    public void selectDebugger(String item) {
        cboDebugger().selectItem(item);
    }

    public void selectConnector(String item) {
        cboConnector().selectItem(item);
    }

    public void selectConnector(int item) {
        cboConnector().selectItem(1);
    }
    /** types text for cboDebugger
     * @param text String text
     */
    public void typeDebugger(String text) {
        cboDebugger().typeText(text);
    }

    /** gets text for txtTransport
     * @return String text
     */
    public String getTransport() {
        return txtTransport().getText();
    }

    /** sets text for txtTransport
     * @param text String text
     */
    public void setTransport(String text) {
        txtTransport().setText(text);
    }

    /** types text for txtTransport
     * @param text String text
     */
    public void typeTransport(String text) {
        txtTransport().typeText(text);
    }

    /** gets text for txtHost
     * @return String text
     */
    public String getHost() {
        return txtHost().getText();
    }

    /** sets text for txtHost
     * @param text String text
     */
    public void setHost(String text) {
        txtHost().setText(text);
    }

    /** types text for txtHost
     * @param text String text
     */
    public void typeHost(String text) {
        txtHost().typeText(text);
    }

    /** gets text for txtPort
     * @return String text
     */
    public String getPort() {
        return txtPort().getText();
    }

    /** sets text for txtPort
     * @param text String text
     */
    public void setPort(String text) {
        txtPort().setText(text);
    }

    /** types text for txtPort
     * @param text String text
     */
    public void typePort(String text) {
        txtPort().typeText(text);
    }

    /** clicks on "OK" JButton
     */
    public void oK() {
        btOK().push();
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

    /** Performs verification of AttachJDialogOperator by accessing all its components.
     */
    public void verify() {
        cboDebugger();
        txtTransport();
        txtHost();
        txtPort();
        btOK();
        btCancel();
        btHelp();
    }

    /** Performs simple test of AttachJDialogOperator
    * @param args the command line arguments
    */
    public static void main(String args[]) {
//        new AttachJDialogOperator().verify();
        System.out.println("AttachJDialogOperator verification finished.");
        java.util.Enumeration enumeration =  System.getProperties().keys();
        String property;
        while (enumeration.hasMoreElements()) {
            property = (String) enumeration.nextElement();
            System.out.println(property + "\t" + System.getProperty(property) );
        }
        
        
    }
}

