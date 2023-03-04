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

package org.netbeans.modules.websvc.wsitconf.ui.service.profiles;

import org.netbeans.modules.websvc.wsitconf.spi.SecurityProfile;
import org.netbeans.modules.websvc.wsitconf.spi.features.SecureConversationFeature;
import org.netbeans.modules.websvc.wsitconf.ui.ComboConstants;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.AlgoSuiteModelHelper;
import org.netbeans.modules.websvc.wsitmodelext.versioning.ConfigVersion;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.PolicyModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.SecurityPolicyModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.SecurityTokensModelHelper;
import org.netbeans.modules.websvc.wsitmodelext.policy.Policy;
import org.netbeans.modules.websvc.wsitmodelext.security.BootstrapPolicy;
import org.netbeans.modules.websvc.wsitmodelext.security.WssElement;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.SecureConversationToken;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;

/**
 *
 * @author  Martin Grebac
 */
public class MessageAuthentication extends ProfileBaseForm {

    /**
     * Creates new form MessageAuthentication
     */
    public MessageAuthentication(WSDLComponent comp, SecurityProfile secProfile) {
        super(comp, secProfile);
        initComponents();

        inSync = true;
        supportTokenCombo.removeAllItems();
        supportTokenCombo.addItem(ComboConstants.X509);
        supportTokenCombo.addItem(ComboConstants.USERNAME);

        fillWssCombo(wssVersionCombo);
        fillLayoutCombo(layoutCombo);
        fillAlgoSuiteCombo(algoSuiteCombo);
        
        inSync = false;
        
        sync();
    }
    
    protected void sync() {
        inSync = true;

        WSDLComponent secBinding = null;

        WSDLComponent endToken = SecurityTokensModelHelper.getSupportingToken(comp, SecurityTokensModelHelper.ENDORSING);
        WSDLComponent secConvT = SecurityTokensModelHelper.getTokenElement(endToken, SecureConversationToken.class);
        boolean secConv = (secConvT instanceof SecureConversationToken);
        
        if (secConv) {
            WSDLComponent bootPolicy = SecurityTokensModelHelper.getTokenElement(secConvT, BootstrapPolicy.class);
            secBinding = SecurityPolicyModelHelper.getSecurityBindingTypeElement(bootPolicy);
            Policy p = (Policy) secBinding.getParent();
            setChBox(secConvChBox, true);
            setChBox(derivedKeysSecConvChBox, SecurityPolicyModelHelper.isRequireDerivedKeys(secConvT));
            setCombo(wssVersionCombo, SecurityPolicyModelHelper.isWss11(p));
            setChBox(reqSigConfChBox, SecurityPolicyModelHelper.isRequireSignatureConfirmation(p));
            p = PolicyModelHelper.getTopLevelElement(bootPolicy, Policy.class,false);
            WSDLComponent tokenKind = SecurityTokensModelHelper.getSupportingToken(p, SecurityTokensModelHelper.ENDORSING);
            if (tokenKind == null) {
                if (ConfigVersion.CONFIG_1_0.equals(cfgVersion)) {
                    tokenKind = SecurityTokensModelHelper.getSupportingToken(p, SecurityTokensModelHelper.SIGNED_SUPPORTING);
                } else {
                    tokenKind = SecurityTokensModelHelper.getSupportingToken(p, SecurityTokensModelHelper.SIGNED_ENCRYPTED);
                }
            }
            String tokenType = SecurityTokensModelHelper.getTokenType(tokenKind);
            setCombo(supportTokenCombo, tokenType);
        } else {
            secBinding = SecurityPolicyModelHelper.getSecurityBindingTypeElement(comp);
            setChBox(secConvChBox, false);
            setChBox(derivedKeysSecConvChBox, false);
            setCombo(wssVersionCombo, SecurityPolicyModelHelper.isWss11(comp));
            setChBox(reqSigConfChBox, SecurityPolicyModelHelper.isRequireSignatureConfirmation(comp));
            WSDLComponent tokenKind = SecurityTokensModelHelper.getSupportingToken(comp, SecurityTokensModelHelper.ENDORSING);
            if (tokenKind == null) {
                if (ConfigVersion.CONFIG_1_0.equals(cfgVersion)) {
                    tokenKind = SecurityTokensModelHelper.getSupportingToken(comp, SecurityTokensModelHelper.SIGNED_SUPPORTING);
                } else {
                    tokenKind = SecurityTokensModelHelper.getSupportingToken(comp, SecurityTokensModelHelper.SIGNED_ENCRYPTED);
                }
            }
            String tokenType = SecurityTokensModelHelper.getTokenType(tokenKind);
            setCombo(supportTokenCombo, tokenType);
        }

        setCombo(algoSuiteCombo, AlgoSuiteModelHelper.getAlgorithmSuite(secBinding));
        setCombo(layoutCombo, SecurityPolicyModelHelper.getMessageLayout(secBinding));

        enableDisable();

        inSync = false;
    }

    public void setValue(javax.swing.JComponent source) {

        if (inSync) return;
            
        WSDLComponent secBinding = null;
        WSDLComponent topSecBinding = SecurityPolicyModelHelper.getSecurityBindingTypeElement(comp);        
        
        WSDLComponent endToken = SecurityTokensModelHelper.getSupportingToken(comp, SecurityTokensModelHelper.ENDORSING);
        WSDLComponent secConvT = SecurityTokensModelHelper.getTokenElement(endToken, SecureConversationToken.class);
        boolean secConv = (secConvT instanceof SecureConversationToken);

        ConfigVersion configVersion = PolicyModelHelper.getConfigVersion(comp);
        SecurityPolicyModelHelper spmh = SecurityPolicyModelHelper.getInstance(configVersion);

        if (source.equals(secConvChBox)) {
            ((SecureConversationFeature)secProfile).enableSecureConversation(comp, secConvChBox.isSelected());
            if (secConvChBox.isSelected()) {
                endToken = SecurityTokensModelHelper.getSupportingToken(comp, SecurityTokensModelHelper.ENDORSING);
                secConvT = SecurityTokensModelHelper.getTokenElement(endToken, SecureConversationToken.class);
                spmh.enableRequireDerivedKeys(secConvT, true);
            }
            sync();
        }
        
        SecurityTokensModelHelper stmh = SecurityTokensModelHelper.getInstance(configVersion);
        AlgoSuiteModelHelper apmh = AlgoSuiteModelHelper.getInstance(configVersion);

        if (secConv) {
            WSDLComponent bootPolicy = SecurityTokensModelHelper.getTokenElement(secConvT, BootstrapPolicy.class);
            secBinding = SecurityPolicyModelHelper.getSecurityBindingTypeElement(bootPolicy);
            Policy p = (Policy) secBinding.getParent();
            if (source.equals(derivedKeysSecConvChBox)) {
                spmh.enableRequireDerivedKeys(secConvT, derivedKeysSecConvChBox.isSelected());
            }
            if (source.equals(wssVersionCombo)) {
                boolean wss11 = ComboConstants.WSS11.equals(wssVersionCombo.getSelectedItem());
                WssElement wss = spmh.enableWss(p, wss11);
                if (wss11) {
                        spmh.enableRequireSignatureConfirmation(
                            SecurityPolicyModelHelper.getWss11(p), reqSigConfChBox.isSelected());
                }
//                spmh.enableMustSupportRefKeyIdentifier(wss, true);
            }
            p = PolicyModelHelper.getTopLevelElement(bootPolicy, Policy.class,false);
            if (source.equals(supportTokenCombo)) {
                if (supportTokenCombo.getSelectedItem().equals(ComboConstants.X509)) {
                    SecurityTokensModelHelper.removeSupportingTokens(p);
                    stmh.setSupportingTokens(p,
                            (String)supportTokenCombo.getSelectedItem(),
                            SecurityTokensModelHelper.ENDORSING);
                } else {
                    SecurityTokensModelHelper.removeSupportingTokens(p);
                    if (ConfigVersion.CONFIG_1_0.equals(cfgVersion)) {
                        stmh.setSupportingTokens(p,
                                (String)supportTokenCombo.getSelectedItem(),
                                SecurityTokensModelHelper.SIGNED_SUPPORTING);
                    } else {
                        stmh.setSupportingTokens(p,
                                (String)supportTokenCombo.getSelectedItem(),
                                SecurityTokensModelHelper.SIGNED_ENCRYPTED);
                    }
                }
            }
        } else {
            secBinding = SecurityPolicyModelHelper.getSecurityBindingTypeElement(comp);
            if (source.equals(reqSigConfChBox)) {
                spmh.enableRequireSignatureConfirmation(
                        SecurityPolicyModelHelper.getWss11(comp), reqSigConfChBox.isSelected());
            }
            if (source.equals(wssVersionCombo)) {
                boolean wss11 = ComboConstants.WSS11.equals(wssVersionCombo.getSelectedItem());
                WssElement wss = spmh.enableWss(comp, wss11);
                if (wss11) {
                    spmh.enableRequireSignatureConfirmation(
                            SecurityPolicyModelHelper.getWss11(comp), reqSigConfChBox.isSelected());
                }
//                spmh.enableMustSupportRefKeyIdentifier(wss, true);
            }
            if (source.equals(supportTokenCombo)) {
                if (supportTokenCombo.getSelectedItem().equals(ComboConstants.X509)) {
                    SecurityTokensModelHelper.removeSupportingTokens(comp);
                    stmh.setSupportingTokens(comp,
                            (String)supportTokenCombo.getSelectedItem(),
                            SecurityTokensModelHelper.ENDORSING);
                } else {
                    SecurityTokensModelHelper.removeSupportingTokens(comp);
                    if (ConfigVersion.CONFIG_1_0.equals(cfgVersion)) {
                        stmh.setSupportingTokens(comp,
                                (String)supportTokenCombo.getSelectedItem(),
                                SecurityTokensModelHelper.SIGNED_SUPPORTING);
                    } else {
                        stmh.setSupportingTokens(comp,
                                (String)supportTokenCombo.getSelectedItem(),
                                SecurityTokensModelHelper.SIGNED_ENCRYPTED);
                    }
                }
            }
        }

        if (source.equals(layoutCombo)) {
            spmh.setLayout(secBinding, (String) layoutCombo.getSelectedItem());
            if (secConv) {
                spmh.setLayout(topSecBinding, (String) layoutCombo.getSelectedItem());
            }
        }
        if (source.equals(algoSuiteCombo)) {
            apmh.setAlgorithmSuite(secBinding, (String) algoSuiteCombo.getSelectedItem());
            if (secConv) {
                apmh.setAlgorithmSuite(topSecBinding, (String) algoSuiteCombo.getSelectedItem());
            }
        }
        
        enableDisable();
    }

    protected void enableDisable() {
        boolean secConvEnabled = secConvChBox.isSelected();
        derivedKeysSecConvChBox.setEnabled(secConvEnabled);
        reqSigConfChBox.setEnabled(!secConvEnabled);
        
        boolean wss11 = ComboConstants.WSS11.equals(wssVersionCombo.getSelectedItem());
        reqSigConfChBox.setEnabled(wss11);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        supportTokenLabel = new javax.swing.JLabel();
        supportTokenCombo = new javax.swing.JComboBox();
        secConvChBox = new javax.swing.JCheckBox();
        reqSigConfChBox = new javax.swing.JCheckBox();
        derivedKeysSecConvChBox = new javax.swing.JCheckBox();
        wssVersionLabel = new javax.swing.JLabel();
        wssVersionCombo = new javax.swing.JComboBox();
        algoSuiteLabel = new javax.swing.JLabel();
        algoSuiteCombo = new javax.swing.JComboBox();
        layoutLabel = new javax.swing.JLabel();
        layoutCombo = new javax.swing.JComboBox();

        supportTokenLabel.setLabelFor(supportTokenCombo);
        org.openide.awt.Mnemonics.setLocalizedText(supportTokenLabel, org.openide.util.NbBundle.getMessage(MessageAuthentication.class, "LBL_AuthToken")); // NOI18N

        supportTokenCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                supportTokenComboActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(secConvChBox, org.openide.util.NbBundle.getMessage(MessageAuthentication.class, "LBL_SecConvLabel")); // NOI18N
        secConvChBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        secConvChBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        secConvChBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                secConvChBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(reqSigConfChBox, org.openide.util.NbBundle.getMessage(MessageAuthentication.class, "LBL_RequireSigConfirmation")); // NOI18N
        reqSigConfChBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        reqSigConfChBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        reqSigConfChBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reqSigConfChBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(derivedKeysSecConvChBox, org.openide.util.NbBundle.getMessage(MessageAuthentication.class, "LBL_RequireDerivedKeysForSecConv")); // NOI18N
        derivedKeysSecConvChBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        derivedKeysSecConvChBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        derivedKeysSecConvChBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                derivedKeysSecConvChBoxActionPerformed(evt);
            }
        });

        wssVersionLabel.setLabelFor(wssVersionCombo);
        org.openide.awt.Mnemonics.setLocalizedText(wssVersionLabel, org.openide.util.NbBundle.getMessage(MessageAuthentication.class, "LBL_WSSVersionLabel")); // NOI18N

        wssVersionCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                wssVersionComboActionPerformed(evt);
            }
        });

        algoSuiteLabel.setLabelFor(algoSuiteCombo);
        org.openide.awt.Mnemonics.setLocalizedText(algoSuiteLabel, org.openide.util.NbBundle.getMessage(MessageAuthentication.class, "LBL_AlgoSuiteLabel")); // NOI18N

        algoSuiteCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                algoSuiteComboActionPerformed(evt);
            }
        });

        layoutLabel.setLabelFor(layoutCombo);
        org.openide.awt.Mnemonics.setLocalizedText(layoutLabel, org.openide.util.NbBundle.getMessage(MessageAuthentication.class, "LBL_LayoutLabel")); // NOI18N

        layoutCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                layoutComboActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(reqSigConfChBox)
                    .addComponent(secConvChBox)
                    .addComponent(derivedKeysSecConvChBox)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(layoutLabel)
                            .addComponent(supportTokenLabel)
                            .addComponent(wssVersionLabel)
                            .addComponent(algoSuiteLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(supportTokenCombo, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(wssVersionCombo, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(algoSuiteCombo, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(layoutCombo, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(33, 33, 33))
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {algoSuiteCombo, layoutCombo, supportTokenCombo, wssVersionCombo});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(supportTokenLabel)
                    .addComponent(supportTokenCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(wssVersionLabel)
                    .addComponent(wssVersionCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(algoSuiteLabel)
                    .addComponent(algoSuiteCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(layoutLabel)
                    .addComponent(layoutCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(secConvChBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(derivedKeysSecConvChBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(reqSigConfChBox)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {layoutCombo, supportTokenCombo, wssVersionCombo});

    }// </editor-fold>//GEN-END:initComponents

    private void reqSigConfChBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reqSigConfChBoxActionPerformed
         setValue(reqSigConfChBox);
    }//GEN-LAST:event_reqSigConfChBoxActionPerformed

    private void derivedKeysSecConvChBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_derivedKeysSecConvChBoxActionPerformed
         setValue(derivedKeysSecConvChBox);
    }//GEN-LAST:event_derivedKeysSecConvChBoxActionPerformed

    private void secConvChBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_secConvChBoxActionPerformed
        setValue(secConvChBox);
    }//GEN-LAST:event_secConvChBoxActionPerformed

    private void wssVersionComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_wssVersionComboActionPerformed
        setValue(wssVersionCombo);
    }//GEN-LAST:event_wssVersionComboActionPerformed

    private void supportTokenComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_supportTokenComboActionPerformed
        setValue(supportTokenCombo);
    }//GEN-LAST:event_supportTokenComboActionPerformed

    private void layoutComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_layoutComboActionPerformed
        setValue(layoutCombo);
    }//GEN-LAST:event_layoutComboActionPerformed

    private void algoSuiteComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_algoSuiteComboActionPerformed
        setValue(algoSuiteCombo);
    }//GEN-LAST:event_algoSuiteComboActionPerformed
            
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox algoSuiteCombo;
    private javax.swing.JLabel algoSuiteLabel;
    private javax.swing.JCheckBox derivedKeysSecConvChBox;
    private javax.swing.JComboBox layoutCombo;
    private javax.swing.JLabel layoutLabel;
    private javax.swing.JCheckBox reqSigConfChBox;
    private javax.swing.JCheckBox secConvChBox;
    private javax.swing.JComboBox supportTokenCombo;
    private javax.swing.JLabel supportTokenLabel;
    private javax.swing.JComboBox wssVersionCombo;
    private javax.swing.JLabel wssVersionLabel;
    // End of variables declaration//GEN-END:variables
    
}
