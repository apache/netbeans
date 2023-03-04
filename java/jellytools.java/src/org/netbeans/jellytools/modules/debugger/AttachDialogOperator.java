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
package org.netbeans.jellytools.modules.debugger;

import javax.swing.JComboBox;
import javax.swing.JTextField;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.NbDialogOperator;
import org.netbeans.jellytools.modules.debugger.actions.AttachDebuggerAction;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JLabelOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.jemmy.operators.Operator;

/** Class implementing all necessary methods for handling "Attach" Dialog.
 *
 * @author Jiri.Skrivanek@sun.com
 */
public class AttachDialogOperator extends NbDialogOperator {

    private JLabelOperator _lblDebugger;
    private JComboBoxOperator _cboDebugger;
    private JLabelOperator _lblConnector;
    private JComboBoxOperator _cboConnector;
    /** SocketAttach (Attaches by socket to other VMs) item. */
    public static final String ITEM_SOCKET_ATTACH = "SocketAttach (Attaches by socket to other VMs)";  //NOI18N
    /** SharedMemoryAttach (Attaches by shared memory to other VMs) item. */
    public static final String ITEM_SHARED_MEMORY_ATTACH = "SharedMemoryAttach (Attaches by shared memory to other VMs)";  //NOI18N
    /** SocketListen (Accepts socket connections initiated by other VMs) item. */
    public static final String ITEM_SOCKET_LISTEN = "SocketListen (Accepts socket connections initiated by other VMs)";  //NOI18N
    /** SharedMemoryListen (Accepts shared memory connections initiated by other VMs) item. */
    public static final String ITEM_SHARED_MEMORY_LISTEN = "SharedMemoryListen (Accepts shared memory connections initiated by other VMs)";  //NOI18N
    private JLabelOperator _lblTransport;
    private JTextFieldOperator _txtTransport;
    private JLabelOperator _lblName;
    private JTextFieldOperator _txtName;
    private JLabelOperator _lblTimeoutMs;
    private JTextFieldOperator _txtTimeoutMs;
    private JLabelOperator _lblPort;
    private JTextFieldOperator _txtPort;
    private JLabelOperator _lblHost;
    private JTextFieldOperator _txtHost;

    /** Waits for dialog with Attach title and creates AttachDialogOperator. */
    public AttachDialogOperator() {
        super(Bundle.getString("org.netbeans.modules.debugger.ui.actions.Bundle",
                               "CTL_Connect_to_running_process"));
    }

    /** Opens Attach dialog and returns instance of AttachDialogOperator.
     * @return AttachDialogOperator instance
     */
    public static AttachDialogOperator invoke() {
        new AttachDebuggerAction().perform();
        return new AttachDialogOperator();
    }
    
    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find "Debugger:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblDebugger() {
        if (_lblDebugger==null) {
            _lblDebugger = new JLabelOperator(this, Bundle.getStringTrimmed(
                                "org.netbeans.modules.debugger.ui.actions.Bundle",
                                "CTL_Connect_through"));
        }
        return _lblDebugger;
    }

    /** Tries to find Debugger JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboDebugger() {
        if (_cboDebugger==null) {
            _cboDebugger = new JComboBoxOperator(
                    (JComboBox)lblDebugger().getLabelFor());
        }
        return _cboDebugger;
    }

    /** Tries to find "Connector:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblConnector() {
        if (_lblConnector==null) {
            _lblConnector = new JLabelOperator(this, Bundle.getStringTrimmed(
                                    "org.netbeans.modules.debugger.jpda.ui.Bundle",
                                    "CTL_Connector"));
        }
        return _lblConnector;
    }

    /** Tries to find Connector JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboConnector() {
        if (_cboConnector==null) {
            _cboConnector = new JComboBoxOperator(
                    (JComboBox)lblConnector().getLabelFor());
        }
        return _cboConnector;
    }

    /** Tries to find "Transport:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblTransport() {
        if (_lblTransport==null) {
            _lblTransport = new JLabelOperator(this, Bundle.getStringTrimmed(
                                    "org.netbeans.modules.debugger.jpda.ui.Bundle",
                                    "CTL_Transport"));
        }
        return _lblTransport;
    }

    /** Tries to find Transport JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtTransport() {
        if (_txtTransport==null) {
            _txtTransport = new JTextFieldOperator((JTextField)lblTransport().getLabelFor());
        }
        return _txtTransport;
    }

    /** Tries to find "Name:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblName() {
        if (_lblName==null) {
            _lblName = new JLabelOperator(this, Bundle.getStringTrimmed(
                                    "org.netbeans.modules.debugger.jpda.ui.Bundle",
                                    "CTL_CA_name"));
        }
        return _lblName;
    }

    /** Tries to find Name JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtName() {
        if (_txtName==null) {
            _txtName = new JTextFieldOperator(
                    (JTextField)lblName().getLabelFor());
        }
        return _txtName;
    }

    /** Tries to find "Timeout [ms]:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblTimeout() {
        if (_lblTimeoutMs==null) {
            _lblTimeoutMs = new JLabelOperator(this, Bundle.getStringTrimmed(
                                    "org.netbeans.modules.debugger.jpda.ui.Bundle",
                                    "CTL_CA_timeout"));
        }
        return _lblTimeoutMs;
    }

    /** Tries to find Timeout JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtTimeout() {
        if (_txtTimeoutMs==null) {
            _txtTimeoutMs = new JTextFieldOperator(
                                    (JTextField)lblTimeout().getLabelFor());
        }
        return _txtTimeoutMs;
    }

    /** Tries to find "Host:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblHost() {
        if (_lblHost==null) {
            _lblHost = new JLabelOperator(this, Bundle.getStringTrimmed(
                                    "org.netbeans.modules.debugger.jpda.ui.Bundle",
                                    "CTL_CA_hostname"));
        }
        return _lblHost;
    }

    /** Tries to find Host JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtHost() {
        if (_txtHost==null) {
            _txtHost = new JTextFieldOperator((JTextField)lblHost().getLabelFor());
        }
        return _txtHost;
    }

    /** Tries to find "Port:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblPort() {
        if (_lblPort==null) {
            Operator.StringComparator old = getComparator();
            try {
                // set exact comparator because port vs. trasport clash
                setComparator(new DefaultStringComparator(true, true));
                _lblPort = new JLabelOperator(this, Bundle.getStringTrimmed(
                                   "org.netbeans.modules.debugger.jpda.ui.Bundle",
                                    "CTL_CA_port"));
            } finally {
                setComparator(old);
            }
        }
        return _lblPort;
    }

    /** Tries to find Port JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtPort() {
        if (_txtPort==null) {
            _txtPort = new JTextFieldOperator((JTextField)lblPort().getLabelFor());
        }
        return _txtPort;
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

    /** returns selected item for cboConnector
     * @return String item
     */
    public String getSelectedConnector() {
        return cboConnector().getSelectedItem().toString();
    }

    /** selects item for cboConnector
     * @param item String item
     */
    public void selectConnector(String item) {
        cboConnector().selectItem(item);
    }

    /** gets text for Transport text field
     * @return String text
     */
    public String getTransport() {
        return txtTransport().getText();
    }

    /** gets text for txtName
     * @return String text
     */
    public String getName() {
        return txtName().getText();
    }

    /** sets text for Name text field
     * @param text String text
     */
    public void setName(String text) {
        txtName().clearText();
        txtName().typeText(text);
    }

    /** gets text for Timeout text field
     * @return String text
     */
    public String getTimeout() {
        return txtTimeout().getText();
    }

    /** sets text for txtTimeoutMs
     * @param text String text
     */
    public void setTimeout(String text) {
        txtTimeout().clearText();
        txtTimeout().typeText(text);
    }

    /** gets text for Host text field
     * @return String text
     */
    public String getHost() {
        return txtHost().getText();
    }

    /** sets text for Host
     * @param text String text
     */
    public void setHost(String text) {
        txtHost().clearText();
        txtHost().typeText(text);
    }

    /** gets text for Port text field
     * @return String text
     */
    public String getPort() {
        return txtPort().getText();
    }

    /** sets text for Port
     * @param text String text
     */
    public void setPort(String text) {
        txtPort().clearText();
        txtPort().typeText(text);
    }

    //*****************************************
    // High-level functionality definition part
    //*****************************************

    /** Performs verification of AttachDialogOperator by accessing all its components.
     */
    public void verify() {
        lblDebugger();
        cboDebugger();
        lblConnector();
        cboConnector();
        lblTransport();
        txtTransport();
        lblTimeout();
        txtTimeout();
    }
}

