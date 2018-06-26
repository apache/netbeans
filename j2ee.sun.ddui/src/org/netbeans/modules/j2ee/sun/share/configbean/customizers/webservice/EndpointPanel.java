/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.modules.j2ee.sun.share.configbean.customizers.webservice;

import java.util.ResourceBundle;
import javax.enterprise.deploy.shared.ModuleType;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.sun.dd.api.ASDDVersion;
import org.netbeans.modules.j2ee.sun.dd.api.CommonDDBean;
import org.netbeans.modules.j2ee.sun.dd.api.VersionNotSupportedException;
import org.netbeans.modules.j2ee.sun.dd.api.common.LoginConfig;
import org.netbeans.modules.j2ee.sun.dd.api.common.MessageSecurityBinding;
import org.netbeans.modules.j2ee.sun.dd.api.common.WebserviceEndpoint;
import org.netbeans.modules.j2ee.sun.ddloaders.SunDescriptorDataObject;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.BaseSectionNodeInnerPanel;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.DDTextFieldEditorModel;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.MappingComboBoxHelper;
import org.netbeans.modules.j2ee.sun.ddloaders.multiview.webservice.EndpointNode;
import org.netbeans.modules.j2ee.sun.share.configbean.Utils;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.TextMapping;
import org.netbeans.modules.xml.multiview.ItemCheckBoxHelper;
import org.netbeans.modules.xml.multiview.ItemEditorHelper;
import org.netbeans.modules.xml.multiview.XmlMultiViewDataSynchronizer;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.openide.ErrorManager;
import org.openide.util.NbBundle;


/**
 *
 * @author Peter Williams
 */
public class EndpointPanel extends BaseSectionNodeInnerPanel {

    private static final int SECURITY_NONE = 0; // No security settings
    private static final int SECURITY_AUTHENTICATION = 1; // login-config/authentication is set
    private static final int SECURITY_MESSAGE = 2; // message level security is set.
    
    private final ResourceBundle webserviceBundle = NbBundle.getBundle(
       "org.netbeans.modules.j2ee.sun.share.configbean.customizers.webservice.Bundle"); // NOI18N

    /** xml <--> ui mapping for authorization method combo box */
    private final TextMapping [] authMethodTypes = {
        new TextMapping("", ""), // NOI18N
        new TextMapping("BASIC", webserviceBundle.getString("AUTHORIZATION_Basic")),	// NOI18N
        new TextMapping("CLIENT-CERT", webserviceBundle.getString("AUTHORIZATION_ClientCert")),	// NOI18N
    };

    /** xml <--> ui mapping for transport guarantee combo box */
    private final TextMapping [] transportTypes = {
        new TextMapping("", ""), // NOI18N
        new TextMapping("NONE", webserviceBundle.getString("TRANSPORT_None")),	// NOI18N
        new TextMapping("INTEGRAL", webserviceBundle.getString("TRANSPORT_Integral")),	// NOI18N
        new TextMapping("CONFIDENTIAL", webserviceBundle.getString("TRANSPORT_Confidential")),	// NOI18N
    };
    
    // data model & version
    private EndpointNode endpointNode;
    private boolean isWebApp;

    // Data storage for when these types are not selected.
    private WebserviceEndpoint endpoint;
    private LoginConfig savedLoginConfig;
    private MessageSecurityBinding savedMessageBinding;
    
    // authorization method combo box model
    private DefaultComboBoxModel authMethodModel;

    // transport guarantee combo box model
    private DefaultComboBoxModel transportGuaranteeModel;

    // setup flag, disables listeners until addNotify()
    private boolean setup;

    // Required to reuse the old radio button code.
    private XmlMultiViewDataSynchronizer synchronizer;
    
    public EndpointPanel(SectionNodeView sectionNodeView, final EndpointNode endpointNode, 
            final ASDDVersion version) {
        super(sectionNodeView, version);
        this.endpointNode = endpointNode;
        this.setup = true;
        this.endpoint = (WebserviceEndpoint) endpointNode.getBinding().getSunBean();

        initComponents();
        initUserComponents(sectionNodeView);
    }
    
    private void initUserComponents(SectionNodeView sectionNodeView) {
        SunDescriptorDataObject dataObject = (SunDescriptorDataObject) sectionNodeView.getDataObject();
        this.synchronizer = dataObject.getModelSynchronizer();
        this.isWebApp = J2eeModule.Type.WAR.equals(dataObject.getModuleType());

        // Setup authorization method combobox
        authMethodModel = new DefaultComboBoxModel();
        for(int i = 0; i < authMethodTypes.length; i++) {
            authMethodModel.addElement(authMethodTypes[i]);
        }
        jCbxAuthentication.setModel(authMethodModel);		

        // Setup transport guarantee combobox
        transportGuaranteeModel = new DefaultComboBoxModel();
        for(int i = 0; i < transportTypes.length; i++) {
            transportGuaranteeModel.addElement(transportTypes[i]);
        }
        jCbxTransportGuarantee.setModel(transportGuaranteeModel);		

        handleUIVisibility();

        addRefreshable(new ItemEditorHelper(jTxtName, new EndpointTextFieldEditorModel(synchronizer, WebserviceEndpoint.PORT_COMPONENT_NAME)));
        addRefreshable(new ItemEditorHelper(jTxtEndpointAddressURI, new EndpointTextFieldEditorModel(synchronizer, WebserviceEndpoint.ENDPOINT_ADDRESS_URI)));
        if(!isWebApp) {
            addRefreshable(new AuthMethodComboBoxHelper(synchronizer, jCbxAuthentication));
            if(as90FeaturesVisible) {
                addRefreshable(new ItemEditorHelper(jTxtRealm, new RealmTextFieldEditorModel(synchronizer)));
            }
        }
        addRefreshable(new TransportComboBoxHelper(synchronizer, jCbxTransportGuarantee));
        if(as90FeaturesVisible) {
            addRefreshable(new DebugEnabledCheckboxHelper(synchronizer, jChkDebugEnabled));
        }

        jTxtName.setEditable(!endpointNode.getBinding().isBound());
    }
    
    public void handleUIVisibility() {
        handleAS90FieldVisibility(as90FeaturesVisible);

        // Relies on version field initialization from handleAS90FieldVisibility(), above
        showSecurityUI(isWebApp, !isWebApp);

        if(as90FeaturesVisible) {
            boolean debugEnabled;
            try {
                debugEnabled = Utils.booleanValueOf(endpoint.getDebuggingEnabled());
            } catch (VersionNotSupportedException ex) {
                debugEnabled = false;
            }
            jChkDebugEnabled.setSelected(debugEnabled);
        }

        // security (all)
        boolean authenticationEnabled = false;
        boolean messageSecurityEnabled = false;
        
        // Backup security data (usability - in case the user is playing with the radio buttons.)
        savedLoginConfig = endpoint.getLoginConfig();
        if(savedLoginConfig != null) {
            authenticationEnabled = true;
        } else {
            savedLoginConfig = endpoint.newLoginConfig();
        }

        try {
            savedMessageBinding = endpoint.getMessageSecurityBinding();
            if(savedMessageBinding != null) {
                messageSecurityEnabled = true;
            } else {
                savedMessageBinding = endpoint.newMessageSecurityBinding();
            }
        } catch (VersionNotSupportedException ex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            savedMessageBinding = null;
        }

        // security (web)
        if(isWebApp) {
            jChkEnableMsgSecurity.setSelected(messageSecurityEnabled);
            enableMessageSecurityUI(messageSecurityEnabled);
        }
        // security (ejb)
        else if(!isWebApp) {
            if(messageSecurityEnabled) {
                jRBnMessageSecurity.setSelected(true);
                jCbxAuthentication.setSelectedItem(getAuthorizationMethodMapping(""));
                jTxtRealm.setText("");
            } else if(authenticationEnabled) {
                jRBnLoginConfig.setSelected(true);
                String authMethod = savedLoginConfig.getAuthMethod();
                jCbxAuthentication.setSelectedItem(getAuthorizationMethodMapping(authMethod));

                if(as90FeaturesVisible) {
                    try {
                        String realm = savedLoginConfig.getRealm();
                        jTxtRealm.setText(realm);
                    } catch(VersionNotSupportedException ex) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                    }
                }
            } else {
                jRBnNoSecurity.setSelected(true);
                jCbxAuthentication.setSelectedItem(getAuthorizationMethodMapping(""));
                jTxtRealm.setText("");
            }

            enableMessageSecurityUI(messageSecurityEnabled);
            enableAuthenticationUI(authenticationEnabled);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        bgSecurity = new javax.swing.ButtonGroup();
        jLblName = new javax.swing.JLabel();
        jTxtName = new javax.swing.JTextField();
        jLblEndpointAddressURI = new javax.swing.JLabel();
        jTxtEndpointAddressURI = new javax.swing.JTextField();
        jLblTransportGuarantee = new javax.swing.JLabel();
        jCbxTransportGuarantee = new javax.swing.JComboBox();
        jLblDebugEnabled = new javax.swing.JLabel();
        jChkDebugEnabled = new javax.swing.JCheckBox();
        jLblSecuritySettings = new javax.swing.JLabel();
        jRBnNoSecurity = new javax.swing.JRadioButton();
        jRBnMessageSecurity = new javax.swing.JRadioButton();
        jLblEnableMsgSecurity = new javax.swing.JLabel();
        jChkEnableMsgSecurity = new javax.swing.JCheckBox();
        jBtnEditBindings = new javax.swing.JButton();
        jRBnLoginConfig = new javax.swing.JRadioButton();
        jLblRealm = new javax.swing.JLabel();
        jTxtRealm = new javax.swing.JTextField();
        jLblAuthentication = new javax.swing.JLabel();
        jCbxAuthentication = new javax.swing.JComboBox();

        setAlignmentX(LEFT_ALIGNMENT);
        setOpaque(false);
        setLayout(new java.awt.GridBagLayout());

        jLblName.setLabelFor(jTxtName);
        jLblName.setText(webserviceBundle. getString("LBL_PortComponentName_1")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(jLblName, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 5);
        add(jTxtName, gridBagConstraints);
        jTxtName.getAccessibleContext().setAccessibleName(webserviceBundle. getString("ACSN_PortComponentName")); // NOI18N
        jTxtName.getAccessibleContext().setAccessibleDescription(webserviceBundle. getString("ACSD_PortComponentName")); // NOI18N

        jLblEndpointAddressURI.setLabelFor(jTxtEndpointAddressURI);
        jLblEndpointAddressURI.setText(webserviceBundle. getString("LBL_EndpointAddressURI_1")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(jLblEndpointAddressURI, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 5);
        add(jTxtEndpointAddressURI, gridBagConstraints);
        jTxtEndpointAddressURI.getAccessibleContext().setAccessibleName(webserviceBundle. getString("ACSN_EndpointAddressURI")); // NOI18N
        jTxtEndpointAddressURI.getAccessibleContext().setAccessibleDescription(webserviceBundle. getString("ACSD_EndpointAddressURI")); // NOI18N

        jLblTransportGuarantee.setLabelFor(jCbxTransportGuarantee);
        jLblTransportGuarantee.setText(webserviceBundle. getString("LBL_TransportGuarantee_1")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(jLblTransportGuarantee, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.ipadx = 20;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 5);
        add(jCbxTransportGuarantee, gridBagConstraints);
        jCbxTransportGuarantee.getAccessibleContext().setAccessibleName(webserviceBundle. getString("ACSN_TransportGuarantee")); // NOI18N
        jCbxTransportGuarantee.getAccessibleContext().setAccessibleDescription(webserviceBundle. getString("ACSD_TransportGuarantee")); // NOI18N

        jLblDebugEnabled.setLabelFor(jChkDebugEnabled);
        jLblDebugEnabled.setText(webserviceBundle. getString("LBL_DebugEnabled_1")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 0);
        add(jLblDebugEnabled, gridBagConstraints);

        jChkDebugEnabled.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jChkDebugEnabled.setOpaque(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 0, 5);
        add(jChkDebugEnabled, gridBagConstraints);
        jChkDebugEnabled.getAccessibleContext().setAccessibleName(webserviceBundle. getString("ACSN_DebugEnabled")); // NOI18N
        jChkDebugEnabled.getAccessibleContext().setAccessibleDescription(webserviceBundle. getString("ACSD_DebugEnabled")); // NOI18N

        jLblSecuritySettings.setText(webserviceBundle. getString("LBL_SecuritySettings")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 6, 0, 0);
        add(jLblSecuritySettings, gridBagConstraints);

        bgSecurity.add(jRBnNoSecurity);
        jRBnNoSecurity.setText(webserviceBundle. getString("LBL_NoSecurity")); // NOI18N
        jRBnNoSecurity.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jRBnNoSecurity.setOpaque(false);
        jRBnNoSecurity.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRBnNoSecurityActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 0, 0);
        add(jRBnNoSecurity, gridBagConstraints);
        jRBnNoSecurity.getAccessibleContext().setAccessibleName(webserviceBundle. getString("ACSN_NoSecurity")); // NOI18N
        jRBnNoSecurity.getAccessibleContext().setAccessibleDescription(webserviceBundle. getString("ACSD_NoSecurity")); // NOI18N

        bgSecurity.add(jRBnMessageSecurity);
        jRBnMessageSecurity.setText(webserviceBundle. getString("LBL_EnableMsgSecurity")); // NOI18N
        jRBnMessageSecurity.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jRBnMessageSecurity.setOpaque(false);
        jRBnMessageSecurity.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRBnMessageSecurityActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 12, 5, 0);
        add(jRBnMessageSecurity, gridBagConstraints);
        jRBnMessageSecurity.getAccessibleContext().setAccessibleName(webserviceBundle. getString("ACSN_MessageSecurity")); // NOI18N
        jRBnMessageSecurity.getAccessibleContext().setAccessibleDescription(webserviceBundle. getString("ACSD_MessageSecurity")); // NOI18N

        jLblEnableMsgSecurity.setLabelFor(jChkEnableMsgSecurity);
        jLblEnableMsgSecurity.setText(webserviceBundle. getString("LBL_EnableMsgSecurity_1")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 5, 0);
        add(jLblEnableMsgSecurity, gridBagConstraints);

        jChkEnableMsgSecurity.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jChkEnableMsgSecurity.setOpaque(false);
        jChkEnableMsgSecurity.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jChkEnableMsgSecurityItemStateChanged(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 5, 12);
        add(jChkEnableMsgSecurity, gridBagConstraints);
        jChkEnableMsgSecurity.getAccessibleContext().setAccessibleName(webserviceBundle. getString("ACSN_EnableMsgSecurity")); // NOI18N
        jChkEnableMsgSecurity.getAccessibleContext().setAccessibleDescription(webserviceBundle. getString("ACSD_EnableMsgSecurity")); // NOI18N

        jBtnEditBindings.setText(webserviceBundle. getString("LBL_EditMsgSecBindings")); // NOI18N
        jBtnEditBindings.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jBtnEditBindingsActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 5, 5);
        add(jBtnEditBindings, gridBagConstraints);
        jBtnEditBindings.getAccessibleContext().setAccessibleName(webserviceBundle. getString("ACSN_EditMsgSecBindings")); // NOI18N
        jBtnEditBindings.getAccessibleContext().setAccessibleDescription(webserviceBundle. getString("ACSD_EditMsgSecBindings")); // NOI18N

        bgSecurity.add(jRBnLoginConfig);
        jRBnLoginConfig.setText(webserviceBundle. getString("LBL_LoginConfiguration")); // NOI18N
        jRBnLoginConfig.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jRBnLoginConfig.setOpaque(false);
        jRBnLoginConfig.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRBnLoginConfigActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(1, 12, 0, 0);
        add(jRBnLoginConfig, gridBagConstraints);
        jRBnLoginConfig.getAccessibleContext().setAccessibleName(webserviceBundle. getString("ACSN_LoginConfiguration")); // NOI18N
        jRBnLoginConfig.getAccessibleContext().setAccessibleDescription(webserviceBundle. getString("ACSD_LoginConfiguration")); // NOI18N

        jLblRealm.setLabelFor(jTxtRealm);
        jLblRealm.setText(webserviceBundle. getString("LBL_Realm_1")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(1, 6, 0, 0);
        add(jLblRealm, gridBagConstraints);

        jTxtRealm.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTxtRealmKeyReleased(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(1, 6, 0, 5);
        add(jTxtRealm, gridBagConstraints);
        jTxtRealm.getAccessibleContext().setAccessibleName(webserviceBundle. getString("ACSN_Realm")); // NOI18N
        jTxtRealm.getAccessibleContext().setAccessibleDescription(webserviceBundle. getString("ACSD_Realm")); // NOI18N

        jLblAuthentication.setLabelFor(jCbxAuthentication);
        jLblAuthentication.setText(webserviceBundle. getString("LBL_AuthenticationMethod_1")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 5, 0);
        add(jLblAuthentication, gridBagConstraints);

        jCbxAuthentication.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCbxAuthenticationActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.ipadx = 20;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(6, 6, 5, 5);
        add(jCbxAuthentication, gridBagConstraints);
        jCbxAuthentication.getAccessibleContext().setAccessibleName(webserviceBundle. getString("ACSN_AuthenticationMethod")); // NOI18N
        jCbxAuthentication.getAccessibleContext().setAccessibleDescription(webserviceBundle. getString("ACSD_AuthenticationMethod")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void jRBnLoginConfigActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRBnLoginConfigActionPerformed
        if(!setup) {
            try {
                startUIChange();
                endpoint.setLoginConfig((LoginConfig) savedLoginConfig.clone());

                String authMethod = savedLoginConfig.getAuthMethod();
                jCbxAuthentication.setSelectedItem(getAuthorizationMethodMapping(authMethod));
                if(as90FeaturesVisible) {
                    try {                
                        String realm = savedLoginConfig.getRealm();
                        jTxtRealm.setText(realm);
                    } catch(VersionNotSupportedException ex) {
                        ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                    }
                }

                enableMessageSecurityUI(false);
                enableAuthenticationUI(true);
            } finally {
                endUIChange();
                synchronizer.requestUpdateData();
            }
        }
    }//GEN-LAST:event_jRBnLoginConfigActionPerformed

    private void jRBnMessageSecurityActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRBnMessageSecurityActionPerformed
        if(!setup) {
            try {
                startUIChange();
                try {
//                    endpoint.setLoginConfig(null);
                    endpoint.setMessageSecurityBinding((MessageSecurityBinding) savedMessageBinding.clone());
                } catch (VersionNotSupportedException ex) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                }

                jCbxAuthentication.setSelectedItem(getAuthorizationMethodMapping("")); // NOI18N
                jTxtRealm.setText(""); // NOI18N

                enableMessageSecurityUI(true);
                enableAuthenticationUI(false);
            } finally {
                endUIChange();
                synchronizer.requestUpdateData();
            }
        }
    }//GEN-LAST:event_jRBnMessageSecurityActionPerformed

    private void jRBnNoSecurityActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRBnNoSecurityActionPerformed
        if(!setup) {
            try {
                startUIChange();
                endpoint.setLoginConfig(null);
                try {
                    endpoint.setMessageSecurityBinding(null);
                } catch (VersionNotSupportedException ex) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                }

                jCbxAuthentication.setSelectedItem(getAuthorizationMethodMapping("")); // NOI18N
                jTxtRealm.setText(""); // NOI18N

                enableMessageSecurityUI(false);
                enableAuthenticationUI(false);
            } finally {
                endUIChange();
                synchronizer.requestUpdateData();
            }
        }
    }//GEN-LAST:event_jRBnNoSecurityActionPerformed

    private void jTxtRealmKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTxtRealmKeyReleased
        if(!setup && jRBnLoginConfig.isSelected() && jRBnLoginConfig.isVisible()) {
            String newRealm = jTxtRealm.getText();
            if(newRealm != null) {
                newRealm = newRealm.trim();
            }
            try {
                String oldRealm = savedLoginConfig.getRealm();
                if(!Utils.strEquivalent(newRealm, oldRealm)) {
                    try {
                        startUIChange();
                        savedLoginConfig.setRealm(newRealm);
                        LoginConfig lc = endpoint.getLoginConfig();
                        if(lc != null) {
                            lc.setRealm(newRealm);
                        }
                    } finally {
                        endUIChange();
                        synchronizer.requestUpdateData();
                    }
                }
            } catch(VersionNotSupportedException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }
    }//GEN-LAST:event_jTxtRealmKeyReleased

    private void jCbxAuthenticationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCbxAuthenticationActionPerformed
        if(!setup && jRBnLoginConfig.isSelected() && jRBnLoginConfig.isVisible()) {
            TextMapping authMapping = (TextMapping) authMethodModel.getSelectedItem();
            String oldAuthMethod = savedLoginConfig.getAuthMethod();
            String newAuthMethod = authMapping.getXMLString();
            if(!Utils.strEquals(newAuthMethod, oldAuthMethod)) {
                try {
                    startUIChange();
                    savedLoginConfig.setAuthMethod(newAuthMethod);
                    LoginConfig lc = endpoint.getLoginConfig();
                    if(lc != null) {
                        lc.setAuthMethod(newAuthMethod);
                    }
                } finally {
                    endUIChange();
                    synchronizer.requestUpdateData();
                }
            }
        }
    }//GEN-LAST:event_jCbxAuthenticationActionPerformed

    private void jChkEnableMsgSecurityItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jChkEnableMsgSecurityItemStateChanged
        if(!setup) {
            boolean hasMessageSecurity = Utils.interpretCheckboxState(evt);
            
            try {
                startUIChange();
                MessageSecurityBinding newBinding = null;
                if(hasMessageSecurity) {
                    if(savedMessageBinding != null) {
                        newBinding = (MessageSecurityBinding) savedMessageBinding.clone();
                        endpoint.setMessageSecurityBinding(newBinding);
                    }
                } else {
                    savedMessageBinding = endpoint.getMessageSecurityBinding();
                    endpoint.setMessageSecurityBinding(null);
                }
            } catch (VersionNotSupportedException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            } finally {
                endUIChange();
                synchronizer.requestUpdateData();
            }
            
            enableMessageSecurityUI(hasMessageSecurity);
        }
    }//GEN-LAST:event_jChkEnableMsgSecurityItemStateChanged

    private void jBtnEditBindingsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jBtnEditBindingsActionPerformed
        if(!setup) {
            try {
                startUIChange();
                String asCloneVersion = isWebApp ? version.getWebAppVersionAsString() : version.getEjbJarVersionAsString();
                MessageSecurityBinding binding = endpoint.getMessageSecurityBinding();
                if(binding == null) {
                    binding = endpoint.newMessageSecurityBinding();
                    endpoint.setMessageSecurityBinding(binding);
                }

                EditBindingMultiview.editMessageSecurityBinding(this, false, binding, version, asCloneVersion);
            } catch (VersionNotSupportedException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            } finally {
                endUIChange();
                synchronizer.requestUpdateData();
            }
        }
    }//GEN-LAST:event_jBtnEditBindingsActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup bgSecurity;
    private javax.swing.JButton jBtnEditBindings;
    private javax.swing.JComboBox jCbxAuthentication;
    private javax.swing.JComboBox jCbxTransportGuarantee;
    private javax.swing.JCheckBox jChkDebugEnabled;
    private javax.swing.JCheckBox jChkEnableMsgSecurity;
    private javax.swing.JLabel jLblAuthentication;
    private javax.swing.JLabel jLblDebugEnabled;
    private javax.swing.JLabel jLblEnableMsgSecurity;
    private javax.swing.JLabel jLblEndpointAddressURI;
    private javax.swing.JLabel jLblName;
    private javax.swing.JLabel jLblRealm;
    private javax.swing.JLabel jLblSecuritySettings;
    private javax.swing.JLabel jLblTransportGuarantee;
    private javax.swing.JRadioButton jRBnLoginConfig;
    private javax.swing.JRadioButton jRBnMessageSecurity;
    private javax.swing.JRadioButton jRBnNoSecurity;
    private javax.swing.JTextField jTxtEndpointAddressURI;
    private javax.swing.JTextField jTxtName;
    private javax.swing.JTextField jTxtRealm;
    // End of variables declaration//GEN-END:variables
    
    @Override
    public void addNotify() {
        super.addNotify();
        setup = false;
    }
    
    private void handleAS90FieldVisibility(boolean visible) {
        jLblDebugEnabled.setVisible(visible);
        jChkDebugEnabled.setVisible(visible);
    }
    
    /** This method displays the correct security related UI based on whether the
     *  host is a web module or an ejb jar.
     */
    private void showSecurityUI(boolean showForWebApp, boolean showForEjbJar) {
        // Show web app security fields.
        jLblEnableMsgSecurity.setVisible(showForWebApp);
        jChkEnableMsgSecurity.setVisible(showForWebApp);
        
        // Hide ejb-jar security fields.
        jLblSecuritySettings.setVisible(showForEjbJar);
        jRBnNoSecurity.setVisible(showForEjbJar);
        jRBnMessageSecurity.setVisible(showForEjbJar);
        jRBnLoginConfig.setVisible(showForEjbJar);
        jLblRealm.setVisible(showForEjbJar && as90FeaturesVisible);
        jTxtRealm.setVisible(showForEjbJar && as90FeaturesVisible);
        jLblAuthentication.setVisible(showForEjbJar);
        jCbxAuthentication.setVisible(showForEjbJar);
        
        // This button is shown for both, but if both are false, then we want to hide it
        jBtnEditBindings.setVisible(showForWebApp || showForEjbJar);
    }

    private void enableMessageSecurityUI(boolean enable) {
        jBtnEditBindings.setEnabled(enable);
    }
    
    private void enableAuthenticationUI(boolean enable) {
        jLblRealm.setEnabled(enable);
        jTxtRealm.setEnabled(enable);
        jLblAuthentication.setEnabled(enable);
        jCbxAuthentication.setEnabled(enable);
    }
  
    private TextMapping getAuthorizationMethodMapping(String xmlKey) {
        TextMapping result = authMethodTypes[0]; // Default to BLANK
        if(xmlKey == null) {
            xmlKey = ""; // NOI18N
        }
        for(int i = 0; i < authMethodTypes.length; i++) {
            if(authMethodTypes[i].getXMLString().compareTo(xmlKey) == 0) {
                result = authMethodTypes[i];
                break;
            }
        }

        return result;
    }

    private TextMapping getTransportGuaranteeMapping(String xmlKey) {
        TextMapping result = transportTypes[0]; // Default to BLANK
        if(xmlKey == null) {
            xmlKey = ""; // NOI18N
        }
        for(int i = 0; i < transportTypes.length; i++) {
            if(transportTypes[i].getXMLString().compareTo(xmlKey) == 0) {
                result = transportTypes[i];
                break;
            }
        }

        return result;
    }

    @Override
    protected void endUIChange() {
        // If this was a virtual bean, commit it to the graph.
        if(endpointNode.addVirtualBean()) {
            // update if necessary
        }
        
        super.endUIChange();
    }

//    public void setContainerEnabled(Container container, boolean enabled) {
//        Component [] components = container.getComponents();
//        for(int i = 0; i < components.length; i++) {
//            components[i].setEnabled(enabled);
//            if(components[i] instanceof Container) {
//                setContainerEnabled((Container) components[i], enabled);
//            }
//        }
//    }
    
    public String getHelpId() {
        return "AS_CFG_WebserviceEndpoint"; // NOI18N
    }

    // Model class for handling updates to the text fields
    private class EndpointTextFieldEditorModel extends DDTextFieldEditorModel {

        public EndpointTextFieldEditorModel(XmlMultiViewDataSynchronizer synchronizer, String propertyName) {
            super(synchronizer, propertyName);
        }
        
        public EndpointTextFieldEditorModel(XmlMultiViewDataSynchronizer synchronizer, String propertyName, String attributeName) {
            super(synchronizer, propertyName, attributeName);
        }

        protected CommonDDBean getBean() {
            return endpointNode.getBinding().getSunBean();
        }
        
        @Override
        protected void setValue(String value) {
            super.setValue(value);

            // If this was a virtual bean, commit it to the graph.
            if(endpointNode.addVirtualBean()) {
                // update if necessary
            }
        }
    }
    
    private class TransportComboBoxHelper extends MappingComboBoxHelper {
        
        public TransportComboBoxHelper(XmlMultiViewDataSynchronizer synchronizer, JComboBox comboxBox) {
            super(synchronizer, comboxBox);
        }

        public TextMapping getItemValue() {
            WebserviceEndpoint endpoint = (WebserviceEndpoint) endpointNode.getBinding().getSunBean();
            return getTransportGuaranteeMapping(endpoint.getTransportGuarantee());
        }

        public void setItemValue(TextMapping value) {
            WebserviceEndpoint endpoint = (WebserviceEndpoint) endpointNode.getBinding().getSunBean();
            String tg = value.getXMLString();
            endpoint.setTransportGuarantee(Utils.notEmpty(tg) ? tg : null);
            
            // If this was a virtual bean, commit it to the graph.
            if(endpointNode.addVirtualBean()) {
                // update if necessary
            }
        }

    }
    
    private class AuthMethodComboBoxHelper extends MappingComboBoxHelper {
        
        public AuthMethodComboBoxHelper(XmlMultiViewDataSynchronizer synchronizer, JComboBox comboxBox) {
            super(synchronizer, comboxBox);
        }

        public TextMapping getItemValue() {
            WebserviceEndpoint endpoint = (WebserviceEndpoint) endpointNode.getBinding().getSunBean();
            LoginConfig lc = endpoint.getLoginConfig();
            return getAuthorizationMethodMapping(lc != null ? lc.getAuthMethod() : null);
        }

        public void setItemValue(TextMapping value) {
            WebserviceEndpoint endpoint = (WebserviceEndpoint) endpointNode.getBinding().getSunBean();
            LoginConfig lc = endpoint.getLoginConfig();
            if(lc == null) {
                lc = endpoint.newLoginConfig();
                endpoint.setLoginConfig(lc);
            }
            String am = value.getXMLString();
            lc.setAuthMethod(Utils.notEmpty(am) ? am : null);

            // If this was a virtual bean, commit it to the graph.
            if(endpointNode.addVirtualBean()) {
                // update if necessary
            }
        }
        
    }
    
    private class RealmTextFieldEditorModel extends DDTextFieldEditorModel {

        public RealmTextFieldEditorModel(XmlMultiViewDataSynchronizer synchronizer) {
            super(synchronizer, LoginConfig.REALM);
        }
        
        protected CommonDDBean getBean() {
            return getBean(false);
        }
        
        @Override
        protected CommonDDBean getBean(boolean create) {
            WebserviceEndpoint endpoint = (WebserviceEndpoint) endpointNode.getBinding().getSunBean();
            LoginConfig lc = endpoint.getLoginConfig();
            if(create && lc == null) {
                lc = endpoint.newLoginConfig();
                endpoint.setLoginConfig(lc);
            }
            return lc;
        }
        
        @Override
        protected void setValue(String value) {
            super.setValue(value);

            // If this was a virtual bean, commit it to the graph.
            if(endpointNode.addVirtualBean()) {
                // update if necessary
            }
        }
    }    
    
    private class DebugEnabledCheckboxHelper extends ItemCheckBoxHelper {

        public DebugEnabledCheckboxHelper(XmlMultiViewDataSynchronizer synchronizer, JCheckBox component) {
            super(synchronizer, component);
        }

        public boolean getItemValue() {
            try {
                return Utils.booleanValueOf(endpoint.getDebuggingEnabled());
            } catch (VersionNotSupportedException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
            return false;
        }

        public void setItemValue(boolean value) {
            try {
                String newDebugEnabled = value ? "true" : null; // NOI18N
                endpoint.setDebuggingEnabled(newDebugEnabled);
                
                // If this was a virtual bean, commit it to the graph.
                if(endpointNode.addVirtualBean()) {
                    // update if necessary
                }
            } catch (VersionNotSupportedException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }
    }
    
}
