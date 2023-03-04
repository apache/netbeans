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

package org.netbeans.modules.websvc.wsitconf.ui.client;

import java.awt.Component;
import java.awt.Dialog;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.wsitconf.ui.ClassDialog;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.CallbackHandler;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.ProprietarySecurityPolicyModelHelper;
import org.netbeans.modules.xml.multiview.ui.SectionInnerPanel;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.netbeans.modules.websvc.api.jaxws.project.config.JaxWsModel;
import org.netbeans.modules.websvc.jaxws.light.api.JAXWSLightSupport;
import org.netbeans.modules.websvc.wsitconf.spi.SecurityCheckerRegistry;
import org.netbeans.modules.websvc.wsitconf.spi.WsitProvider;
import org.netbeans.modules.websvc.wsitconf.ui.ComboConstants;
import org.netbeans.modules.websvc.wsitconf.ui.client.subpanels.DynamicCredsPanel;
import org.netbeans.modules.websvc.wsitconf.ui.client.subpanels.KerberosConfigPanel;
import org.netbeans.modules.websvc.wsitconf.ui.client.subpanels.StaticCredsPanel;
import org.netbeans.modules.websvc.wsitconf.ui.client.subpanels.ValidatorsPanel;
import org.netbeans.modules.websvc.wsitconf.ui.service.BindingPanel;
import org.netbeans.modules.websvc.wsitconf.ui.service.subpanels.KeystorePanel;
import org.netbeans.modules.websvc.wsitconf.ui.service.subpanels.TruststorePanel;
import org.netbeans.modules.websvc.wsitconf.util.DefaultSettings;
import org.netbeans.modules.websvc.wsitconf.util.Util;
import org.netbeans.modules.websvc.wsitmodelext.versioning.ConfigVersion;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.PolicyModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.ProfilesModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.SecurityTokensModelHelper;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.X509Token;
import org.netbeans.modules.xml.multiview.ui.NodeSectionPanel;
import org.netbeans.modules.xml.wsdl.model.BindingFault;
import org.netbeans.modules.xml.wsdl.model.BindingInput;
import org.netbeans.modules.xml.wsdl.model.BindingOperation;
import org.netbeans.modules.xml.wsdl.model.BindingOutput;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;

/**
 *
 * @author Martin Grebac
 */
public class CallbackPanel extends SectionInnerPanel {

    private Node node;
    private Binding binding;

    private boolean inSync = false;

    private Project project;

    private SectionView view;
    private JaxWsModel jaxwsmodel;
    private WSDLModel serviceModel;

    private WsitProvider wsitProvider;
    private boolean jsr109 = false;
    
    private String profile;

    private DefaultFormatterFactory tstampff = null;

    private ConfigVersion cfgVersion = null;
    
    public CallbackPanel(SectionView view, Node node, Binding binding, JaxWsModel jaxWsModel, WSDLModel serviceModel) {
        super(view);
        this.view = view;
        this.node = node;
        this.binding = binding;
        this.jaxwsmodel = jaxWsModel;
        this.serviceModel = serviceModel;

        cfgVersion = PolicyModelHelper.getConfigVersion(binding);

        FileObject fo = node.getLookup().lookup(FileObject.class);
        if (fo == null) {
            JAXWSLightSupport support = node.getLookup().lookup(JAXWSLightSupport.class);
            if (support != null) {
                fo = support.getWsdlFolder(false);
            }
        }
        if (fo != null) {
            project = FileOwnerQuery.getOwner(fo);
        } else {
            throw new IllegalArgumentException("Cannot find corresponding project: " + node);
        }

        this.wsitProvider = project.getLookup().lookup(WsitProvider.class);
        if (wsitProvider != null) {
            jsr109 = wsitProvider.isJsr109Project();
        }

        tstampff = new DefaultFormatterFactory();
        NumberFormat timestampFormat = NumberFormat.getIntegerInstance();
        timestampFormat.setGroupingUsed(false);
        timestampFormat.setParseIntegerOnly(true);
        timestampFormat.setMaximumIntegerDigits(8);
        timestampFormat.setMaximumFractionDigits(0);
        NumberFormatter timestampFormatter = new NumberFormatter(timestampFormat);
        timestampFormatter.setCommitsOnValidEdit(true);
        timestampFormatter.setMinimum(0);
        timestampFormatter.setMaximum(99999999);
        tstampff.setDefaultFormatter(timestampFormatter);

        initComponents();
        /* issue 232988: the background color issues with dark metal L&F
        samlHandlerField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        samlHandlerLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        iterationLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        iterationField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        devDefaultsChBox.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        cbTimestampLbl.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        cbTimestampField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        */
        cbTimestampField.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);

        inSync = true;
        credTypeCombo.removeAllItems();
        credTypeCombo.addItem(ComboConstants.STATIC);
        credTypeCombo.addItem(ComboConstants.DYNAMIC);
        inSync = false;

        addImmediateModifier(samlHandlerField);
        addImmediateModifier(credTypeCombo);
        addImmediateModifier(devDefaultsChBox);
        addImmediateModifier(cbTimestampField);
        addImmediateModifier(iterationField);
        
        sync();
    }

    public void sync() {
        inSync = true;

        Binding serviceBinding = PolicyModelHelper.getBinding(serviceModel, binding.getName());
        profile = ProfilesModelHelper.getWSITSecurityProfile(serviceBinding);

        boolean defaultsSupported = ProfilesModelHelper.isClientDefaultSetupSupported(profile);
        boolean defaults = false;
        if (defaultsSupported) {
            defaults = ProfilesModelHelper.isClientDefaultSetupUsed(profile, binding, serviceBinding, project);
        }
        setChBox(devDefaultsChBox, defaults);
        
        String samlCallback = ProprietarySecurityPolicyModelHelper.getCallbackHandler(binding, CallbackHandler.SAML_CBHANDLER);
        if (samlCallback != null) {
            setCallbackHandler(samlCallback);
        }

        String usernameCBH = ProprietarySecurityPolicyModelHelper.getCallbackHandler(binding, CallbackHandler.USERNAME_CBHANDLER);
        if ((usernameCBH != null) && (usernameCBH.length() > 0)) {
            setCredType(ComboConstants.DYNAMIC, defaults);
            credTypeCombo.setSelectedItem(ComboConstants.DYNAMIC);
        } else {
            setCredType(ComboConstants.STATIC, defaults);
            credTypeCombo.setSelectedItem(ComboConstants.STATIC);
        }

        String tsTimeout = ProprietarySecurityPolicyModelHelper.getHandlerTimestampTimeout(binding);
        if (tsTimeout == null) { // no setup exists yet - set the default
            cbTimestampField.setText(ProprietarySecurityPolicyModelHelper.DEFAULT_HANDLER_TIMESTAMP_TIMEOUT);
        } else {
            cbTimestampField.setText(tsTimeout);
        } 
        
        String iterations = ProprietarySecurityPolicyModelHelper.getHandlerIterations(binding);
        if (iterations == null) { // no setup exists yet - set the default
            iterationField.setText(ProprietarySecurityPolicyModelHelper.DEFAULT_ITERATIONS);
        } else {
            iterationField.setText(iterations);
        }

        enableDisable();

        inSync = false;
    }

    private void setChBox(JCheckBox chBox, Boolean enable) {
        if (enable == null) {
            chBox.setSelected(false);
        } else {
            chBox.setSelected(enable);
        }
    }

    private JPanel getPanel(String type, boolean defaults) {
        boolean amSec = SecurityCheckerRegistry.getDefault().isNonWsitSecurityEnabled(node, jaxwsmodel);

        if (ComboConstants.DYNAMIC.equals(type)) {
            return new DynamicCredsPanel(binding, project, !amSec && !defaults);
        }
        return new StaticCredsPanel(binding, !amSec && !defaults);
    }

    private void setCredType(String credType, boolean defaults) {
        this.remove(credPanel);
        credPanel = getPanel(credType, defaults);

        boolean active = true;
        if (view != null) {
            NodeSectionPanel panel = view.getActivePanel();
            active = (panel == null) ? false : panel.isActive();
        }
        /* issue 232988: the background color issues with dark metal L&F
        Color c = active ? SectionVisualTheme.getSectionActiveBackgroundColor() : SectionVisualTheme.getDocumentBackgroundColor();
        credPanel.setBackground(c);
        */
        refreshLayout();
    }

    @Override
    public void setValue(javax.swing.JComponent source, Object value) {
        if (inSync) {
            return;
        }

        Util.checkMetroLibrary(project);

        if (source.equals(credTypeCombo)) {
            ProprietarySecurityPolicyModelHelper.setCallbackHandler(binding, CallbackHandler.USERNAME_CBHANDLER, null, null, true);
            ProprietarySecurityPolicyModelHelper.setCallbackHandler(binding, CallbackHandler.PASSWORD_CBHANDLER, null, null, true);
            setCredType((String) credTypeCombo.getSelectedItem(), devDefaultsChBox.isSelected());
        }

        if (source.equals(samlHandlerField)) {
            String classname = getCallbackHandler();
            if ((classname != null) && (classname.length() == 0)) {
                classname = null;
            }
            ProprietarySecurityPolicyModelHelper.setCallbackHandler(binding, CallbackHandler.SAML_CBHANDLER, classname, null, true);
            return;
        }

        if (source.equals(iterationField)) {
            Long iterations = (Long)iterationField.getValue();
            if ((iterations == null) || iterations.toString().length() == 0) {
                ProprietarySecurityPolicyModelHelper.setHandlerIterations(binding, null, true);
            } else {
                ProprietarySecurityPolicyModelHelper.setHandlerIterations(binding, iterations.toString(), true);
            }
        }

        if (source.equals(cbTimestampField)) {
            String timeout = ((Integer) cbTimestampField.getValue()).toString();
            if ((timeout == null) || (timeout.length() == 0) || (ProprietarySecurityPolicyModelHelper.DEFAULT_HANDLER_TIMESTAMP_TIMEOUT.equals(timeout.toString()))) {
                ProprietarySecurityPolicyModelHelper.setHandlerTimestampTimeout(binding, null, true);
            } else {
                ProprietarySecurityPolicyModelHelper.setHandlerTimestampTimeout(binding, timeout, true);
            }
        }
        
        if (source.equals(devDefaultsChBox)) {
            if (devDefaultsChBox.isSelected()) {
                DefaultSettings.fillDefaults(project, true,true);
                Binding serviceBinding = PolicyModelHelper.getBinding(serviceModel, binding.getName());
                ProfilesModelHelper.setClientDefaults(profile, binding, serviceBinding, project);
                sync();
                refreshLayout();
                ((PanelEnabler)credPanel).enablePanel(false);
                credPanel.revalidate();
                credPanel.repaint();
            } else {
                ((PanelEnabler)credPanel).enablePanel(true);
            }
        }

        enableDisable();
    }

    @Override
    public void documentChanged(javax.swing.text.JTextComponent comp, String value) {
        enableDisable();
    }

    @Override
    public void rollbackValue(javax.swing.text.JTextComponent source) {
    }

    @Override
    protected void endUIChange() {
    }

    public void linkButtonPressed(Object ddBean, String ddProperty) {
    }

    public javax.swing.JComponent getErrorComponent(String errorId) {
        return null;
    }

    private void enableDisable() {

        boolean amSec = SecurityCheckerRegistry.getDefault().isNonWsitSecurityEnabled(node, jaxwsmodel);
        boolean samlRequired = true;
        boolean authRequired = true;


        if (!amSec) {
            devDefaultsChBox.setEnabled(true);
            boolean defaults = devDefaultsChBox.isSelected();

            boolean trustStoreConfigRequired = true;
            boolean keyStoreConfigRequired = true;
            boolean kerberosConfigRequired = false;
            boolean validatorsConfigRequired = (ConfigVersion.CONFIG_1_0 == cfgVersion);
            
            if (ComboConstants.PROF_USERNAME.equals(profile)) {
                    keyStoreConfigRequired = false;
            }
            if (ComboConstants.PROF_MSGAUTHSSL.equals(profile)) {
                    trustStoreConfigRequired = false;
            }
            if (ComboConstants.PROF_KERBEROS.equals(profile)) {
                trustStoreConfigRequired = false;
                keyStoreConfigRequired = false;
                kerberosConfigRequired = true;
            }
            keyStoreButton.setEnabled(keyStoreConfigRequired && !defaults);
            trustStoreButton.setEnabled(trustStoreConfigRequired && !defaults);
            kerberosCfgButton.setEnabled(kerberosConfigRequired && !defaults);
            validatorCfgButton.setEnabled(validatorsConfigRequired && !defaults);

            if (ComboConstants.PROF_USERNAME.equals(profile) || 
                ComboConstants.PROF_STSISSUED.equals(profile) || 
                ComboConstants.PROF_STSISSUEDENDORSE.equals(profile) || 
                ComboConstants.PROF_STSISSUEDCERT.equals(profile) || 
                ComboConstants.PROF_STSISSUEDSUPPORTING.equals(profile) || 
                ComboConstants.PROF_MSGAUTHSSL.equals(profile)) {
                    samlRequired = false;
            }

            if (ComboConstants.PROF_SAMLSSL.equals(profile) || 
                ComboConstants.PROF_SAMLHOLDER.equals(profile) || 
                ComboConstants.PROF_SAMLSENDER.equals(profile)) {
                    authRequired = false;
            }

            credTypeLabel.setEnabled(authRequired && !defaults);
            credTypeCombo.setEnabled(authRequired && !defaults);

            credPanel.setEnabled(authRequired && !defaults);
            Component[] comps = credPanel.getComponents();
            for (Component c : comps) {
                c.setEnabled(authRequired && !defaults);
            }

            samlBrowseButton.setEnabled(samlRequired && !defaults);
            samlHandlerField.setEnabled(samlRequired && !defaults);
            samlHandlerLabel.setEnabled(samlRequired && !defaults);
            
            cbTimestampField.setEnabled(!defaults);
            cbTimestampLbl.setEnabled(!defaults);
            
            iterationField.setEnabled(!defaults);
            iterationLabel.setEnabled(!defaults);
        } else {
            credPanel.setEnabled(false);
            Component[] comps = credPanel.getComponents();
            for (Component c : comps) {
                c.setEnabled(false);
            }
            credTypeCombo.setEnabled(false);
            credTypeLabel.setEnabled(false);
            devDefaultsChBox.setEnabled(false);
            validatorCfgButton.setEnabled(false);
            jSeparator1.setEnabled(false);
            keyStoreButton.setEnabled(false);
            samlBrowseButton.setEnabled(false);
            samlHandlerField.setEnabled(false);
            samlHandlerLabel.setEnabled(false);
            trustStoreButton.setEnabled(false);
        }
        refreshLayout();
    }

    public static boolean isStoreConfigRequired(String profile, boolean trust, Binding binding) {
        ArrayList<WSDLComponent> compsToTry = new ArrayList<WSDLComponent>();
        compsToTry.add(binding);
        Collection<BindingOperation> ops = binding.getBindingOperations();
        for (BindingOperation op : ops) {
            BindingInput bi = op.getBindingInput();
            if (bi != null) {
                compsToTry.add(bi);
            }
            BindingOutput bo = op.getBindingOutput();
            if (bo != null) {
                compsToTry.add(bo);
            }
            Collection<BindingFault> bfs = op.getBindingFaults();
            for (BindingFault bf : bfs) {
                if (bf != null) {
                    compsToTry.add(bf);
                }
            }
        }

        for (WSDLComponent wc : compsToTry) {
            List<WSDLComponent> suppTokens = SecurityTokensModelHelper.getSupportingTokens(wc);
            if (suppTokens != null) {
                for (WSDLComponent suppToken : suppTokens) {
                    WSDLComponent token = SecurityTokensModelHelper.getTokenTypeElement(suppToken);
                    if (token instanceof X509Token) {
                        return true;
                    }
                }
            }
        }

        if ((ComboConstants.PROF_TRANSPORT.equals(profile)) || (ComboConstants.PROF_SAMLSSL.equals(profile))) {
            return false;
        }
        if (!trust) {
            if (ComboConstants.PROF_USERNAME.equals(profile)) {
                return false;
            }
            if (ComboConstants.PROF_MSGAUTHSSL.equals(profile)) {
                // TODO - depends on other config
            }
        } else {
            if (ComboConstants.PROF_MSGAUTHSSL.equals(profile)) {
                return false;
            }
        }
        return true;
    }
    
    private void setCallbackHandler(String classname) {
        this.samlHandlerField.setText(classname);
    }

    private String getCallbackHandler() {
        return samlHandlerField.getText();
    }

    private void refreshLayout() {
        javax.swing.GroupLayout layout = (GroupLayout) this.getLayout();
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(keyStoreButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(trustStoreButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(kerberosCfgButton)
                        .addGap(6, 6, 6)
                        .addComponent(validatorCfgButton))
                    .addComponent(devDefaultsChBox))
                .addGap(62, 62, 62))
            .addGroup(layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(credTypeLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(credTypeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(credPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(cbTimestampLbl)
                                .addComponent(samlHandlerLabel)
                                .addComponent(iterationLabel))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(iterationField, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(samlHandlerField, javax.swing.GroupLayout.PREFERRED_SIZE, 209, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(samlBrowseButton))
                                    .addComponent(cbTimestampField, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                    .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 549, Short.MAX_VALUE))
                .addContainerGap())
        );

        layout.linkSize(SwingConstants.HORIZONTAL, kerberosCfgButton, keyStoreButton, trustStoreButton, validatorCfgButton);

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(devDefaultsChBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(keyStoreButton)
                    .addComponent(trustStoreButton)
                    .addComponent(kerberosCfgButton)
                    .addComponent(validatorCfgButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(credTypeLabel)
                    .addComponent(credTypeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(credPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(samlBrowseButton)
                    .addComponent(samlHandlerField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(samlHandlerLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbTimestampField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbTimestampLbl))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(iterationLabel)
                    .addComponent(iterationField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        layout.linkSize(SwingConstants.VERTICAL, kerberosCfgButton, keyStoreButton, trustStoreButton, validatorCfgButton);

        samlHandlerLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CallbackPanel.class, "LBL_KeyStorePanel_SamlLabel_ACSN")); // NOI18N
        samlHandlerLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CallbackPanel.class, "LBL_KeyStorePanel_SamlLabel_ACSD")); // NOI18N
        samlBrowseButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CallbackPanel.class, "LBL_AuthPanel_SCHBrowseButton_ACSN")); // NOI18N
        samlBrowseButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CallbackPanel.class, "LBL_AuthPanel_SCHBrowseButton_ACSD")); // NOI18N
        credTypeLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CallbackPanel.class, "LBL_KeyStorePanel_AuthTypeLabel_ACSN")); // NOI18N
        credTypeLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CallbackPanel.class, "LBL_KeyStorePanel_AuthTypeLabel_ACSD")); // NOI18N
        keyStoreButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CallbackPanel.class, "LBL_KeyStore_ACSN")); // NOI18N
        keyStoreButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CallbackPanel.class, "LBL_KeyStore_ACSD")); // NOI18N
        trustStoreButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CallbackPanel.class, "LBL_Truststore_ACSN")); // NOI18N
        trustStoreButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CallbackPanel.class, "LBL_Truststore_ACSD")); // NOI18N
        devDefaultsChBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CallbackPanel.class, "LBL_DevDefaults_ACSN")); // NOI18N
        devDefaultsChBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CallbackPanel.class, "LBL_DevDefaults_ACSD")); // NOI18N
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        samlHandlerLabel = new javax.swing.JLabel();
        samlHandlerField = new javax.swing.JTextField();
        samlBrowseButton = new javax.swing.JButton();
        credTypeCombo = new javax.swing.JComboBox();
        credTypeLabel = new javax.swing.JLabel();
        credPanel = new javax.swing.JPanel();
        keyStoreButton = new javax.swing.JButton();
        trustStoreButton = new javax.swing.JButton();
        devDefaultsChBox = new javax.swing.JCheckBox();
        jSeparator1 = new javax.swing.JSeparator();
        cbTimestampLbl = new javax.swing.JLabel();
        cbTimestampField = new javax.swing.JFormattedTextField();
        kerberosCfgButton = new javax.swing.JButton();
        validatorCfgButton = new javax.swing.JButton();
        iterationLabel = new javax.swing.JLabel();
        iterationField = new javax.swing.JFormattedTextField();

        addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                formFocusGained(evt);
            }
        });
        addAncestorListener(new javax.swing.event.AncestorListener() {
            public void ancestorMoved(javax.swing.event.AncestorEvent evt) {
            }
            public void ancestorAdded(javax.swing.event.AncestorEvent evt) {
                formAncestorAdded(evt);
            }
            public void ancestorRemoved(javax.swing.event.AncestorEvent evt) {
            }
        });

        samlHandlerLabel.setLabelFor(samlHandlerField);
        org.openide.awt.Mnemonics.setLocalizedText(samlHandlerLabel, org.openide.util.NbBundle.getMessage(CallbackPanel.class, "LBL_KeyStorePanel_SamlLabel")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(samlBrowseButton, org.openide.util.NbBundle.getMessage(CallbackPanel.class, "LBL_AuthPanel_SCHBrowseButton")); // NOI18N
        samlBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                samlBrowseButtonActionPerformed(evt);
            }
        });

        credTypeCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Static", "Dynamic" }));

        credTypeLabel.setLabelFor(credTypeCombo);
        org.openide.awt.Mnemonics.setLocalizedText(credTypeLabel, org.openide.util.NbBundle.getMessage(CallbackPanel.class, "LBL_KeyStorePanel_AuthTypeLabel")); // NOI18N

        javax.swing.GroupLayout credPanelLayout = new javax.swing.GroupLayout(credPanel);
        credPanel.setLayout(credPanelLayout);
        credPanelLayout.setHorizontalGroup(
            credPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 424, Short.MAX_VALUE)
        );
        credPanelLayout.setVerticalGroup(
            credPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 64, Short.MAX_VALUE)
        );

        org.openide.awt.Mnemonics.setLocalizedText(keyStoreButton, org.openide.util.NbBundle.getMessage(CallbackPanel.class, "LBL_KeyStore")); // NOI18N
        keyStoreButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                keyStoreButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(trustStoreButton, org.openide.util.NbBundle.getMessage(CallbackPanel.class, "LBL_Truststore")); // NOI18N
        trustStoreButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                trustStoreButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(devDefaultsChBox, org.openide.util.NbBundle.getMessage(CallbackPanel.class, "LBL_DevDefaults")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbTimestampLbl, org.openide.util.NbBundle.getMessage(CallbackPanel.class, "LBL_CallbackPanel_Timestamp")); // NOI18N

        cbTimestampField.setFormatterFactory(tstampff);

        org.openide.awt.Mnemonics.setLocalizedText(kerberosCfgButton, org.openide.util.NbBundle.getMessage(CallbackPanel.class, "LBL_KerberosCfg")); // NOI18N
        kerberosCfgButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                kerberosCfgButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(validatorCfgButton, org.openide.util.NbBundle.getMessage(CallbackPanel.class, "LBL_ValidatorsCfg")); // NOI18N
        validatorCfgButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                validatorCfgButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(iterationLabel, "Key Obtention Iterations:");

        iterationField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        iterationField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                iterationFieldActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(keyStoreButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(trustStoreButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(kerberosCfgButton)
                        .addGap(6, 6, 6)
                        .addComponent(validatorCfgButton))
                    .addComponent(devDefaultsChBox))
                .addGap(62, 62, 62))
            .addGroup(layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(credTypeLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(credTypeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(credPanel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(cbTimestampLbl)
                                .addComponent(samlHandlerLabel)
                                .addComponent(iterationLabel))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(iterationField, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(samlHandlerField, javax.swing.GroupLayout.PREFERRED_SIZE, 209, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(samlBrowseButton))
                                    .addComponent(cbTimestampField, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                    .addComponent(jSeparator1, javax.swing.GroupLayout.DEFAULT_SIZE, 572, Short.MAX_VALUE))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {kerberosCfgButton, keyStoreButton, trustStoreButton, validatorCfgButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(devDefaultsChBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(keyStoreButton)
                    .addComponent(trustStoreButton)
                    .addComponent(kerberosCfgButton)
                    .addComponent(validatorCfgButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(credTypeLabel)
                    .addComponent(credTypeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(credPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(samlBrowseButton)
                    .addComponent(samlHandlerField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(samlHandlerLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbTimestampField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cbTimestampLbl))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(iterationLabel)
                    .addComponent(iterationField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {kerberosCfgButton, keyStoreButton, trustStoreButton, validatorCfgButton});

        samlHandlerLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CallbackPanel.class, "LBL_KeyStorePanel_SamlLabel_ACSN")); // NOI18N
        samlHandlerLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CallbackPanel.class, "LBL_KeyStorePanel_SamlLabel_ACSD")); // NOI18N
        samlBrowseButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CallbackPanel.class, "LBL_AuthPanel_SCHBrowseButton_ACSN")); // NOI18N
        samlBrowseButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CallbackPanel.class, "LBL_AuthPanel_SCHBrowseButton_ACSD")); // NOI18N
        credTypeLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CallbackPanel.class, "LBL_KeyStorePanel_AuthTypeLabel_ACSN")); // NOI18N
        credTypeLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CallbackPanel.class, "LBL_KeyStorePanel_AuthTypeLabel_ACSD")); // NOI18N
        keyStoreButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CallbackPanel.class, "LBL_KeyStore_ACSN")); // NOI18N
        keyStoreButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CallbackPanel.class, "LBL_KeyStore_ACSD")); // NOI18N
        trustStoreButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CallbackPanel.class, "LBL_Truststore_ACSN")); // NOI18N
        trustStoreButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CallbackPanel.class, "LBL_Truststore_ACSD")); // NOI18N
        devDefaultsChBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CallbackPanel.class, "LBL_DevDefaults_ACSN")); // NOI18N
        devDefaultsChBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CallbackPanel.class, "LBL_DevDefaults_ACSD")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

private void trustStoreButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_trustStoreButtonActionPerformed
    TruststorePanel storePanel = new TruststorePanel(binding, project, jsr109, profile, true, cfgVersion);
    DialogDescriptor dlgDesc = new DialogDescriptor(storePanel, 
            NbBundle.getMessage(BindingPanel.class, "LBL_Truststore_Panel_Title")); //NOI18N
    Dialog dlg = DialogDisplayer.getDefault().createDialog(dlgDesc);

    dlg.setVisible(true); 
    if (dlgDesc.getValue() == DialogDescriptor.OK_OPTION) {
        storePanel.storeState();
    }
}//GEN-LAST:event_trustStoreButtonActionPerformed

private void keyStoreButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_keyStoreButtonActionPerformed
    KeystorePanel storePanel = new KeystorePanel(binding, project, jsr109, true, cfgVersion);
    DialogDescriptor dlgDesc = new DialogDescriptor(storePanel, 
            NbBundle.getMessage(BindingPanel.class, "LBL_Keystore_Panel_Title")); //NOI18N
    Dialog dlg = DialogDisplayer.getDefault().createDialog(dlgDesc);

    dlg.setVisible(true); 

    if (dlgDesc.getValue() == DialogDescriptor.OK_OPTION) {
        storePanel.storeState();
    }
}//GEN-LAST:event_keyStoreButtonActionPerformed

private void formFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_formFocusGained
    enableDisable();
}//GEN-LAST:event_formFocusGained

private void formAncestorAdded(javax.swing.event.AncestorEvent evt) {//GEN-FIRST:event_formAncestorAdded
    enableDisable();
}//GEN-LAST:event_formAncestorAdded
    
    private void samlBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_samlBrowseButtonActionPerformed
        if (project != null) {
            ClassDialog classDialog = new ClassDialog(project, "javax.security.auth.callback.CallbackHandler"); //NOI18N
            classDialog.show();
            if (classDialog.okButtonPressed()) {
                Set<String> selectedClasses = classDialog.getSelectedClasses();
                for (String selectedClass : selectedClasses) {
                    setCallbackHandler(selectedClass);
                    ProprietarySecurityPolicyModelHelper.setCallbackHandler(binding, CallbackHandler.SAML_CBHANDLER, selectedClass, null, true);
                    break;
                }
            }
        }
    }//GEN-LAST:event_samlBrowseButtonActionPerformed

    private void kerberosCfgButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_kerberosCfgButtonActionPerformed
        KerberosConfigPanel panel = new KerberosConfigPanel(binding, project);
        DialogDescriptor dlgDesc = new DialogDescriptor(panel, 
                NbBundle.getMessage(BindingPanel.class, "LBL_KerberosConfig_Panel_Title")); //NOI18N
        Dialog dlg = DialogDisplayer.getDefault().createDialog(dlgDesc);

        dlg.setVisible(true); 

        if (dlgDesc.getValue() == DialogDescriptor.OK_OPTION) {
            panel.storeState();
        }
}//GEN-LAST:event_kerberosCfgButtonActionPerformed

    private void validatorCfgButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_validatorCfgButtonActionPerformed
        ValidatorsPanel panel = new ValidatorsPanel(binding, project);
        DialogDescriptor dlgDesc = new DialogDescriptor(panel,
                NbBundle.getMessage(BindingPanel.class, "LBL_KerberosConfig_Panel_Title")); //NOI18N
        Dialog dlg = DialogDisplayer.getDefault().createDialog(dlgDesc);

        dlg.setVisible(true);

        if (dlgDesc.getValue() == DialogDescriptor.OK_OPTION) {
            panel.storeState();
        }
}//GEN-LAST:event_validatorCfgButtonActionPerformed

    private void iterationFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_iterationFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_iterationFieldActionPerformed
        
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JFormattedTextField cbTimestampField;
    private javax.swing.JLabel cbTimestampLbl;
    private javax.swing.JPanel credPanel;
    private javax.swing.JComboBox credTypeCombo;
    private javax.swing.JLabel credTypeLabel;
    private javax.swing.JCheckBox devDefaultsChBox;
    private javax.swing.JFormattedTextField iterationField;
    private javax.swing.JLabel iterationLabel;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JButton kerberosCfgButton;
    private javax.swing.JButton keyStoreButton;
    private javax.swing.JButton samlBrowseButton;
    private javax.swing.JTextField samlHandlerField;
    private javax.swing.JLabel samlHandlerLabel;
    private javax.swing.JButton trustStoreButton;
    private javax.swing.JButton validatorCfgButton;
    // End of variables declaration//GEN-END:variables
    
}
