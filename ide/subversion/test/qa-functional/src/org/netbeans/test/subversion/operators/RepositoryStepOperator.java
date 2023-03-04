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
 * CheckoutWizardOperator.java
 *
 * Created on 19/04/06 13:24
 */
package org.netbeans.test.subversion.operators;

import javax.swing.JPanel;
import org.netbeans.jellytools.WizardOperator;
import org.netbeans.jemmy.operators.*;

/**
 * Class implementing all necessary methods for handling "CheckoutWizardOperator" NbDialog.
 *
 *
 * @author peter
 * @version 1.0
 */
public class RepositoryStepOperator extends WizardOperator {

    public static final String ITEM_FILE = "file:///";
    public static final String ITEM_HTTP = "http://";
    public static final String ITEM_HTTPS = "https://";
    public static final String ITEM_SVN = "svn://";
    public static final String ITEM_SVNSSH = "svn+ssh://";
    
    /**
     * Creates new CheckoutWizardOperator that can handle it.
     */
    public RepositoryStepOperator() {
        super(""); //NO I18N
        //Repository
        stepsWaitSelectedValue("Repository");
    }
    private JLabelOperator _lblSteps;
    private JListOperator _lstSteps;
    private JLabelOperator _lblRepository;
    private JButtonOperator _btProxyConfiguration;
    private JLabelOperator _lblUseExternal;
    private JLabelOperator _lblTunnelCommand;
    private JTextFieldOperator _txtTunnelCommand;
    private JLabelOperator _lblPassword;
    private JLabelOperator _lblUser;
    private JLabelOperator _lblRepositoryURL;
    private JTextFieldOperator _txtUser;
    private JPasswordFieldOperator _txtPassword;
    private JLabelOperator _lblLeaveBlankForAnonymousAccess;
    private JComboBoxOperator _cboRepositoryURL;
    private JLabelOperator _lblSpecifySubversionRepositoryLocation;
    private JLabelOperator _lblWizardDescriptor$FixedHeightLabel;
    private JButtonOperator _btStop;
    private JButtonOperator _btBack;
    private JButtonOperator _btNext;
    private JButtonOperator _btFinish;
    private JButtonOperator _btCancel;
    private JButtonOperator _btHelp;
    private JTextPaneOperator _txtPaneWarning;
    
    
    //******************************
    // Subcomponents definition part
    //******************************
    /** Tries to find "Steps" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblSteps() {
        if (_lblSteps == null) {
            _lblSteps = new JLabelOperator(this, "Steps");
        }
        return _lblSteps;
    }

    /** Tries to find null JList in this dialog.
     * @return JListOperator
     */
    @Override
    public JListOperator lstSteps() {
        if (_lstSteps == null) {
            _lstSteps = new JListOperator(this);
        }
        return _lstSteps;
    }

    /** Tries to find "Repository" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblRepository() {
        if (_lblRepository == null) {
            _lblRepository = new JLabelOperator(this, "Repository");
        }
        return _lblRepository;
    }

    /** Tries to find "Proxy Configuration..." JButton in this dialog.
     * @return JButtonOperator
     */
public JButtonOperator btProxyConfiguration() {
        if (_btProxyConfiguration == null) {
            _btProxyConfiguration = new JButtonOperator(this, "Proxy Configuration...");
        }
        return _btProxyConfiguration;
    }
    
    /** Tries to find "Use External Tunnel" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblUseExternal() {
        if (_lblUseExternal == null) {
            _lblUseExternal = new JLabelOperator(this, "Use External");
        }
        return _lblUseExternal;
    }
    
    /** Tries to find "Tunnel Command" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblTunnelCommand() {
        if (_lblTunnelCommand == null) {
            _lblTunnelCommand = new JLabelOperator(this, "Tunnel Command");
        }
        return _lblTunnelCommand;
    }

    /** Tries to find "Password:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblPassword() {
        if (_lblPassword == null) {
            _lblPassword = new JLabelOperator(this, "Password:");
        }
        return _lblPassword;
    }

    /** Tries to find "User:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblUser() {
        if (_lblUser == null) {
            _lblUser = new JLabelOperator(this, "User:");
        }
        return _lblUser;
    }

    /** Tries to find "Repository URL:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblRepositoryURL() {
        if (_lblRepositoryURL == null) {
            _lblRepositoryURL = new JLabelOperator(this, "Repository URL:");
        }
        return _lblRepositoryURL;
    }

    /** Tries to find "Tunnel Command" JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtTunnelCommand() {
        if (_txtTunnelCommand == null) {
            _txtTunnelCommand = new JTextFieldOperator(this, 1);
        }
        return _txtTunnelCommand;
    }
    
    /** Tries to find null JTextField in this dialog.
     * @return JTextFieldOperator
     */
    public JTextFieldOperator txtUser() {
        if (_txtUser == null) {
            _txtUser = new JTextFieldOperator(this);
        }
        return _txtUser;
    }

    /** Tries to find null JPasswordField in this dialog.
     * @return JPasswordFieldOperator
     */
    public JPasswordFieldOperator txtPassword() {
        if (_txtPassword == null) {
            _txtPassword = new JPasswordFieldOperator(this);
        }
        return _txtPassword;
    }

    /** Tries to find "(leave blank for anonymous access)" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblLeaveBlankForAnonymousAccess() {
        if (_lblLeaveBlankForAnonymousAccess == null) {
            _lblLeaveBlankForAnonymousAccess = new JLabelOperator(this, "(leave blank for anonymous access)");
        }
        return _lblLeaveBlankForAnonymousAccess;
    }

    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboRepositoryURL() {
        if (_cboRepositoryURL == null) {
            _cboRepositoryURL = new JComboBoxOperator(this);
        }
        return _cboRepositoryURL;
    }

    /** Tries to find "Specify Subversion repository location:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblSpecifySubversionRepositoryLocation() {
        if (_lblSpecifySubversionRepositoryLocation == null) {
            _lblSpecifySubversionRepositoryLocation = new JLabelOperator(this, "Specify Subversion repository location:");
        }
        return _lblSpecifySubversionRepositoryLocation;
    }

    /** Tries to find null WizardDescriptor$FixedHeightLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblWizardDescriptor$FixedHeightLabel() {
        if (_lblWizardDescriptor$FixedHeightLabel == null) {
            _lblWizardDescriptor$FixedHeightLabel = new JLabelOperator(this, 7);
        }
        return _lblWizardDescriptor$FixedHeightLabel;
    }
    
    public JTextPaneOperator txtPaneWarning() {
        if (_txtPaneWarning == null) {
            _txtPaneWarning = new JTextPaneOperator(this);
        }
        return _txtPaneWarning;
    }

    /** Tries to find "Stop" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btStop() {
        if (_btStop == null) {
            _btStop = new JButtonOperator(this, "Stop");
        }
        return _btStop;
    }
    
    /** Tries to find "< Back" JButton in this dialog.
     * @return JButtonOperator
     */
    @Override
    public JButtonOperator btBack() {
        if (_btBack == null) {
            _btBack = new JButtonOperator(this, "< Back");
        }
        return _btBack;
    }

    /** Tries to find "Next >" JButton in this dialog.
     * @return JButtonOperator
     */
    @Override
    public JButtonOperator btNext() {
        if (_btNext == null) {
            _btNext = new JButtonOperator(this, "Next >");
        }
        return _btNext;
    }

    /** Tries to find "Finish" JButton in this dialog.
     * @return JButtonOperator
     */
    @Override
    public JButtonOperator btFinish() {
        if (_btFinish == null) {
            _btFinish = new JButtonOperator(this, "Finish");
        }
        return _btFinish;
    }

    /** Tries to find "Cancel" JButton in this dialog.
     * @return JButtonOperator
     */
    @Override
    public JButtonOperator btCancel() {
        if (_btCancel == null) {
            _btCancel = new JButtonOperator(this, "Cancel");
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

    public ProxyConfigurationOperator invokeProxy() {
        btProxyConfiguration().pushNoBlock();
        return new ProxyConfigurationOperator();
    }
    
    //****************************************
    // Low-level functionality definition part
    //****************************************
    /** clicks on "Proxy Configuration..." JButton
     */
    public void proxyConfiguration() {
        btProxyConfiguration().pushNoBlock();
    }

    /** gets text for txtUser
     * @return String text
     */
    public String getUser() {
        return txtUser().getText();
    }

    /** sets text for txtUser
     * @param text String text
     */
    public void setUser(String text) {
        txtUser().setText(text);
    }

    /** types text for txtUser
     * @param text String text
     */
    public void typeUser(String text) {
        txtUser().typeText(text);
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

    /** returns selected item for cboRepositoryURL
     * @return String item
     */
    public String getSelectedRepositoryURL() {
        return cboRepositoryURL().getSelectedItem().toString();
    }

    /** selects item for cboRepositoryURL
     * @param item String item
     */
    public void selectRepositoryURL(String item) {
        cboRepositoryURL().selectItem(item);
    }
    
    public void setRepositoryURL(String url) {
        cboRepositoryURL().getTextField().clearText();
        cboRepositoryURL().getTextField().typeText(url);
    }

    /** types text for cboRepositoryURL
     * @param text String text
     */
    public void typeRepositoryURL(String text) {
        cboRepositoryURL().typeText(text);
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
     * Performs verification of CheckoutWizardOperator by accessing all its components.
     */
    @Override
    public void verify() {
        lblSteps();
        lstSteps();
        lblRepository();
        btProxyConfiguration();
        lblPassword();
        lblUser();
        lblRepositoryURL();
        txtUser();
        txtPassword();
        lblLeaveBlankForAnonymousAccess();
        cboRepositoryURL();
        lblSpecifySubversionRepositoryLocation();
        lblWizardDescriptor$FixedHeightLabel();
        btBack();
        btNext();
        btFinish();
        btCancel();
        btHelp();
    }
}

