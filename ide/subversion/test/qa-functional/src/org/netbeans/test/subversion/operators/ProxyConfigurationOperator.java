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

/** Class implementing all necessary methods for handling "Proxy Configuration" NbDialog.
 *
 * @author peter
 * @version 1.0
 */
public class ProxyConfigurationOperator extends NbDialogOperator {

    /**
     * Creates new ProxyConfigurationOperator that can handle it.
     */
    public ProxyConfigurationOperator() {
        super("Options");
    }

    private JRadioButtonOperator _rbUseSystemProxySettings;
    private JRadioButtonOperator _rbNoProxyDirectConnection;
    private JRadioButtonOperator _rbHTTPProxy;
    //private JRadioButtonOperator _rbSOCKSProxy;
    private JLabelOperator _lblProxyHost;
    private JTextFieldOperator _txtProxyHost;
    private JLabelOperator _lblPort;
    private JTextFieldOperator _txtPort;
    private JCheckBoxOperator _cbProxyServerRequiresLogin;
    private JLabelOperator _lblName;
    private JTextFieldOperator _txtName;
    private JLabelOperator _lblPassword;
    private JPasswordFieldOperator _txtPassword;
    private JButtonOperator _btOK;
    private JButtonOperator _btCancel;
    private JButtonOperator _btHelp;


    //******************************
    // Subcomponents definition part
    //******************************

    /** Tries to find "Use System Proxy Settings" JRadioButton in this dialog.
     * @return JRadioButtonOperator
     */
    public JRadioButtonOperator rbUseSystemProxySettings() {
        if (_rbUseSystemProxySettings==null) {
            _rbUseSystemProxySettings = new JRadioButtonOperator(this, "Use System Proxy Settings");
        }
        return _rbUseSystemProxySettings;
    }

    /** Tries to find "No Proxy (direct connection)" JRadioButton in this dialog.
     * @return JRadioButtonOperator
     */
    public JRadioButtonOperator rbNoProxyDirectConnection() {
        if (_rbNoProxyDirectConnection==null) {
            _rbNoProxyDirectConnection = new JRadioButtonOperator(this, "No Proxy");
        }
        return _rbNoProxyDirectConnection;
    }

    /** Tries to find "HTTP Proxy" JRadioButton in this dialog.
     * @return JRadioButtonOperator HTTP Proxy
     */
    public JRadioButtonOperator rbHTTPProxy() {
        if (_rbHTTPProxy==null) {
            _rbHTTPProxy = new JRadioButtonOperator(this, "Manual Proxy Settings");
        }
        return _rbHTTPProxy;
    }

    /** Tries to find "Proxy Host:" JLabel in this dialog.
     * @return JLabelOperator Proxy Host
     */
    public JLabelOperator lblProxyHost() {
        if (_lblProxyHost==null) {
            _lblProxyHost = new JLabelOperator(this, "HTTP Proxy:");
        }
        return _lblProxyHost;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtProxyHost() {
        if (_txtProxyHost==null) {
            _txtProxyHost = new JTextFieldOperator(this);
        }
        return _txtProxyHost;
    }

    /** Tries to find "Port:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblPort() {
        if (_lblPort==null) {
            _lblPort = new JLabelOperator(this, "Port:");
        }
        return _lblPort;
    }

    /** Tries to find null JFormattedTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtPort() {
        if (_txtPort==null) {
            _txtPort = new JTextFieldOperator(this, 1);
        }
        return _txtPort;
    }

    /** Tries to find "Proxy Server Requires Login" JCheckBox in this dialog.
     * @return JCheckBoxOperator
     */
    public JCheckBoxOperator cbProxyServerRequiresLogin() {
        if (_cbProxyServerRequiresLogin==null) {
            _cbProxyServerRequiresLogin = new JCheckBoxOperator(this, "Proxy Server Requires Login");
        }
        return _cbProxyServerRequiresLogin;
    }

    /** Tries to find "Name:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblName() {
        if (_lblName==null) {
            _lblName = new JLabelOperator(this, "Name:");
        }
        return _lblName;
    }

    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtName() {
        if (_txtName==null) {
            _txtName = new JTextFieldOperator(this, 2);
        }
        return _txtName;
    }

    /** Tries to find "Password:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblPassword() {
        if (_lblPassword==null) {
            _lblPassword = new JLabelOperator(this, "Password:");
        }
        return _lblPassword;
    }

    /** Tries to find null JPasswordField in this dialog.
     * @return JPasswordFieldOperator
     */
    public JPasswordFieldOperator txtPassword() {
        if (_txtPassword==null) {
            _txtPassword = new JPasswordFieldOperator(this);
        }
        return _txtPassword;
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

    /** clicks on "Use System Proxy Settings" JRadioButton
     */
    public void useSystemProxySettings() {
        rbUseSystemProxySettings().push();
    }

    /** clicks on "No Proxy (direct connection)" JRadioButton
     */
    public void noProxyDirectConnection() {
        rbNoProxyDirectConnection().push();
    }

    /** clicks on "HTTP Proxy" JRadioButton
     */
    public void hTTPProxy() {
        rbHTTPProxy().push();
    }

    /** gets text for txtProxyHost
     * @return String text
     */
    public String getProxyHost() {
        return txtProxyHost().getText();
    }

    /** sets text for txtProxyHost
     * @param text String text
     */
    public void setProxyHost(String text) {
        txtProxyHost().setText(text);
    }

    /** types text for txtProxyHost
     * @param text String text
     */
    public void typeProxyHost(String text) {
        txtProxyHost().typeText(text);
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

    /** checks or unchecks given JCheckBox
     * @param state boolean requested state
     */
    public void checkProxyServerRequiresLogin(boolean state) {
        if (cbProxyServerRequiresLogin().isSelected()!=state) {
            cbProxyServerRequiresLogin().push();
        }
    }

    /** gets text for txtName
     * @return String text
     */
    @Override
    public String getName() {
        return txtName().getText();
    }

    /** sets text for txtName
     * @param text String text
     */
    @Override
    public void setName(String text) {
        txtName().setText(text);
    }

    /** types text for txtName
     * @param text String text
     */
    public void typeName(String text) {
        txtName().typeText(text);
    }

    /** sets text for txtPassword
     * @param text String text
     */
    public void setPassword(String text) {
        txtPassword().setText(text);
    }

    /** types text for txtPassword
     * @param text String text
     */
    public void typePassword(String text) {
        txtPassword().typeText(text);
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

    /**
     * Performs verification of ProxyConfigurationOperator by accessing all its components.
     */
    public void verify() {
        rbUseSystemProxySettings();
        rbNoProxyDirectConnection();
        rbHTTPProxy();
        lblProxyHost();
        txtProxyHost();
        lblPort();
        txtPort();
//        cbProxyServerRequiresLogin();
//        lblName();
//        txtName();
//        lblPassword();
//        txtPassword();
        btOK();
        btCancel();
        btHelp();
    }
}

