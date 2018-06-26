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
 * Software is Sun Microsystems, Inc. Portions Copyright 2006 Sun
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

package org.netbeans.modules.websvc.wsitconf.ui.service.profiles;

import org.netbeans.modules.websvc.wsitconf.spi.SecurityProfile;
import org.netbeans.modules.websvc.wsitconf.spi.features.SecureConversationFeature;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.AlgoSuiteModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.PolicyModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.SecurityPolicyModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.SecurityTokensModelHelper;
import org.netbeans.modules.websvc.wsitmodelext.policy.Policy;
import org.netbeans.modules.websvc.wsitmodelext.security.BootstrapPolicy;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.InitiatorToken;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.ProtectionToken;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.RecipientToken;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.SecureConversationToken;
import org.netbeans.modules.websvc.wsitmodelext.versioning.ConfigVersion;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;

/**
 *
 * @author  Martin Grebac
 */
public class SenderVouches extends ProfileBaseForm {

    private int suppTokenType = SecurityTokensModelHelper.SIGNED_ENCRYPTED;

    /**
     * Creates new form SenderVouches
     */
    public SenderVouches(WSDLComponent comp, SecurityProfile secProfile) {
        super(comp, secProfile);
        initComponents();

        if (ConfigVersion.CONFIG_1_0.equals(cfgVersion)) {
            suppTokenType = SecurityTokensModelHelper.SIGNED_SUPPORTING;
        }

        inSync = true;
        fillLayoutCombo(layoutCombo);
        fillSamlCombo(samlVersionCombo);
        fillAlgoSuiteCombo(algoSuiteCombo);
        inSync = false;
        
        sync();
    }
    
    protected void sync() {
        inSync = true;
        
        WSDLComponent secBinding = null;
        WSDLComponent bootPolicy = null;
        WSDLComponent topSecBinding = SecurityPolicyModelHelper.getSecurityBindingTypeElement(comp);
        WSDLComponent protTokenKind = SecurityTokensModelHelper.getTokenElement(topSecBinding, ProtectionToken.class);
        WSDLComponent protToken = SecurityTokensModelHelper.getTokenTypeElement(protTokenKind);
        
        boolean secConv = (protToken instanceof SecureConversationToken);
        
        if (secConv) {
            bootPolicy = SecurityTokensModelHelper.getTokenElement(protToken, BootstrapPolicy.class);
            secBinding = SecurityPolicyModelHelper.getSecurityBindingTypeElement(bootPolicy);
            setChBox(secConvChBox, true);
            setChBox(derivedKeysChBox, SecurityPolicyModelHelper.isRequireDerivedKeys(protToken));
            setChBox(encryptSignatureChBox, SecurityPolicyModelHelper.isEncryptSignature(bootPolicy));
            setChBox(encryptOrderChBox, SecurityPolicyModelHelper.isEncryptBeforeSigning(bootPolicy));
        } else {
            secBinding = SecurityPolicyModelHelper.getSecurityBindingTypeElement(comp);
            setChBox(secConvChBox, false);
            setChBox(derivedKeysChBox, false);
            setChBox(encryptSignatureChBox, SecurityPolicyModelHelper.isEncryptSignature(comp));
            setChBox(encryptOrderChBox, SecurityPolicyModelHelper.isEncryptBeforeSigning(comp));
        }

        String samlVersion = null;
        WSDLComponent tokenKind = null;
        if (secConv) {
            Policy pp = PolicyModelHelper.getTopLevelElement(bootPolicy, Policy.class,false);
            tokenKind = SecurityTokensModelHelper.getSupportingToken(pp, suppTokenType);
        } else {
            tokenKind = SecurityTokensModelHelper.getSupportingToken(comp, suppTokenType);
        }
        WSDLComponent token = SecurityTokensModelHelper.getTokenTypeElement(tokenKind);
        samlVersion = SecurityTokensModelHelper.getTokenProfileVersion(token);
        setCombo(samlVersionCombo, samlVersion);
            
        tokenKind = SecurityTokensModelHelper.getTokenElement(secBinding, RecipientToken.class);
        token = SecurityTokensModelHelper.getTokenTypeElement(tokenKind);
        setChBox(reqDerivedKeys, SecurityPolicyModelHelper.isRequireDerivedKeys(token));

        setCombo(algoSuiteCombo, AlgoSuiteModelHelper.getAlgorithmSuite(secBinding));
        setCombo(layoutCombo, SecurityPolicyModelHelper.getMessageLayout(secBinding));

        enableDisable();
        
        inSync = false;
    }

    public void setValue(javax.swing.JComponent source) {

        if (inSync) return;
            
        WSDLComponent secBinding = null;
        WSDLComponent bootPolicy = null;
        WSDLComponent topSecBinding = SecurityPolicyModelHelper.getSecurityBindingTypeElement(comp);
        WSDLComponent protTokenKind = SecurityTokensModelHelper.getTokenElement(topSecBinding, ProtectionToken.class);
        WSDLComponent protToken = SecurityTokensModelHelper.getTokenTypeElement(protTokenKind);
        
        boolean secConv = (protToken instanceof SecureConversationToken);
        if (source.equals(secConvChBox)) {
            ((SecureConversationFeature)secProfile).enableSecureConversation(comp, secConvChBox.isSelected());
            sync();
        }
        SecurityPolicyModelHelper spmh = SecurityPolicyModelHelper.getInstance(cfgVersion);
        SecurityTokensModelHelper stmh = SecurityTokensModelHelper.getInstance(cfgVersion);
        AlgoSuiteModelHelper asmh = AlgoSuiteModelHelper.getInstance(cfgVersion);
        if (secConv) {
            bootPolicy = SecurityTokensModelHelper.getTokenElement(protToken, BootstrapPolicy.class);
            secBinding = SecurityPolicyModelHelper.getSecurityBindingTypeElement(bootPolicy);
            if (source.equals(derivedKeysChBox)) {
                spmh.enableRequireDerivedKeys(protToken, derivedKeysChBox.isSelected());
            }
        } else {
            secBinding = SecurityPolicyModelHelper.getSecurityBindingTypeElement(comp);
        }
        
        if (source.equals(encryptSignatureChBox)) {
            spmh.enableEncryptSignature(secBinding, encryptSignatureChBox.isSelected());
            if (secConv) {
                spmh.enableEncryptSignature(topSecBinding, encryptSignatureChBox.isSelected());
            }
        }
        if (source.equals(encryptOrderChBox)) {
            spmh.enableEncryptBeforeSigning(secBinding, encryptOrderChBox.isSelected());
            if (secConv) {
                spmh.enableEncryptBeforeSigning(topSecBinding, encryptOrderChBox.isSelected());
            }
        }
        if (source.equals(layoutCombo)) {
            spmh.setLayout(secBinding, (String) layoutCombo.getSelectedItem());
            if (secConv) {
                spmh.setLayout(topSecBinding, (String) layoutCombo.getSelectedItem());
            }
        }
        if (source.equals(algoSuiteCombo)) {
            asmh.setAlgorithmSuite(secBinding, (String) algoSuiteCombo.getSelectedItem());
            if (secConv) {
                asmh.setAlgorithmSuite(topSecBinding, (String) algoSuiteCombo.getSelectedItem());
            }
        }
        if (source.equals(reqDerivedKeys)) {
            WSDLComponent tokenKind = SecurityTokensModelHelper.getTokenElement(secBinding, RecipientToken.class);
            WSDLComponent token = SecurityTokensModelHelper.getTokenTypeElement(tokenKind);
            spmh.enableRequireDerivedKeys(token, reqDerivedKeys.isSelected());
            tokenKind = SecurityTokensModelHelper.getTokenElement(secBinding, InitiatorToken.class);
            token = SecurityTokensModelHelper.getTokenTypeElement(tokenKind);
            spmh.enableRequireDerivedKeys(token, reqDerivedKeys.isSelected());
            return;
        }
        if (source.equals(samlVersionCombo)) {
            WSDLComponent tokenKind = null;
            if (secConv){
                Policy pp = PolicyModelHelper.getTopLevelElement(bootPolicy, Policy.class,false);
                tokenKind = SecurityTokensModelHelper.getSupportingToken(pp, suppTokenType);
            } else {
                tokenKind = SecurityTokensModelHelper.getSupportingToken(comp, suppTokenType);
            }
            WSDLComponent token = SecurityTokensModelHelper.getTokenTypeElement(tokenKind);
            stmh.setTokenProfileVersion(token, (String) samlVersionCombo.getSelectedItem());
        }
        
        enableDisable();
    }

    protected void enableDisable() {
        boolean secConvEnabled = secConvChBox.isSelected();
        derivedKeysChBox.setEnabled(secConvEnabled);
    }
        
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        secConvChBox = new javax.swing.JCheckBox();
        derivedKeysChBox = new javax.swing.JCheckBox();
        algoSuiteLabel = new javax.swing.JLabel();
        algoSuiteCombo = new javax.swing.JComboBox();
        layoutLabel = new javax.swing.JLabel();
        layoutCombo = new javax.swing.JComboBox();
        encryptSignatureChBox = new javax.swing.JCheckBox();
        reqDerivedKeys = new javax.swing.JCheckBox();
        encryptOrderChBox = new javax.swing.JCheckBox();
        samlVersionCombo = new javax.swing.JComboBox();
        samlVersionLabel = new javax.swing.JLabel();

        org.openide.awt.Mnemonics.setLocalizedText(secConvChBox, org.openide.util.NbBundle.getMessage(SenderVouches.class, "LBL_SecConvLabel")); // NOI18N
        secConvChBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        secConvChBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        secConvChBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                secConvChBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(derivedKeysChBox, org.openide.util.NbBundle.getMessage(SenderVouches.class, "LBL_RequireDerivedKeysForSecConv")); // NOI18N
        derivedKeysChBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        derivedKeysChBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        derivedKeysChBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                derivedKeysChBoxActionPerformed(evt);
            }
        });

        algoSuiteLabel.setLabelFor(algoSuiteCombo);
        org.openide.awt.Mnemonics.setLocalizedText(algoSuiteLabel, org.openide.util.NbBundle.getMessage(SenderVouches.class, "LBL_AlgoSuiteLabel")); // NOI18N

        algoSuiteCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                algoSuiteComboActionPerformed(evt);
            }
        });

        layoutLabel.setLabelFor(layoutCombo);
        org.openide.awt.Mnemonics.setLocalizedText(layoutLabel, org.openide.util.NbBundle.getMessage(SenderVouches.class, "LBL_LayoutLabel")); // NOI18N

        layoutCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                layoutComboActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(encryptSignatureChBox, org.openide.util.NbBundle.getMessage(SenderVouches.class, "LBL_EncryptSignatureLabel")); // NOI18N
        encryptSignatureChBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        encryptSignatureChBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        encryptSignatureChBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                encryptSignatureChBox(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(reqDerivedKeys, org.openide.util.NbBundle.getMessage(SenderVouches.class, "LBL_RequireDerivedKeys")); // NOI18N
        reqDerivedKeys.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        reqDerivedKeys.setMargin(new java.awt.Insets(0, 0, 0, 0));
        reqDerivedKeys.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reqDerivedKeysActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(encryptOrderChBox, org.openide.util.NbBundle.getMessage(SenderVouches.class, "LBL_EncryptOrderLabel")); // NOI18N
        encryptOrderChBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        encryptOrderChBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        encryptOrderChBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                encryptOrderChBoxActionPerformed(evt);
            }
        });

        samlVersionCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                samlVersionComboActionPerformed(evt);
            }
        });

        samlVersionLabel.setLabelFor(samlVersionCombo);
        org.openide.awt.Mnemonics.setLocalizedText(samlVersionLabel, org.openide.util.NbBundle.getMessage(SenderVouches.class, "LBL_SamlVersion")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(reqDerivedKeys)
                    .addComponent(secConvChBox)
                    .addComponent(derivedKeysChBox)
                    .addComponent(encryptSignatureChBox)
                    .addComponent(encryptOrderChBox)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(layoutLabel)
                            .addComponent(algoSuiteLabel)
                            .addComponent(samlVersionLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(samlVersionCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(algoSuiteCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(layoutCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {algoSuiteCombo, layoutCombo, samlVersionCombo});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(samlVersionLabel)
                    .addComponent(samlVersionCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(algoSuiteLabel)
                    .addComponent(algoSuiteCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(layoutLabel)
                    .addComponent(layoutCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(reqDerivedKeys)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(secConvChBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(derivedKeysChBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(encryptSignatureChBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(encryptOrderChBox)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {algoSuiteCombo, layoutCombo, samlVersionCombo});

    }// </editor-fold>//GEN-END:initComponents

    private void samlVersionComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_samlVersionComboActionPerformed
        setValue(samlVersionCombo);
    }//GEN-LAST:event_samlVersionComboActionPerformed

    private void encryptOrderChBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_encryptOrderChBoxActionPerformed
         setValue(encryptOrderChBox);
    }//GEN-LAST:event_encryptOrderChBoxActionPerformed

    private void reqDerivedKeysActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reqDerivedKeysActionPerformed
         setValue(reqDerivedKeys);
    }//GEN-LAST:event_reqDerivedKeysActionPerformed

    private void derivedKeysChBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_derivedKeysChBoxActionPerformed
         setValue(derivedKeysChBox);
    }//GEN-LAST:event_derivedKeysChBoxActionPerformed

    private void secConvChBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_secConvChBoxActionPerformed
        setValue(secConvChBox);
    }//GEN-LAST:event_secConvChBoxActionPerformed

    private void encryptSignatureChBox(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_encryptSignatureChBox
        setValue(encryptSignatureChBox);
    }//GEN-LAST:event_encryptSignatureChBox

    private void layoutComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_layoutComboActionPerformed
        setValue(layoutCombo);
    }//GEN-LAST:event_layoutComboActionPerformed

    private void algoSuiteComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_algoSuiteComboActionPerformed
        setValue(algoSuiteCombo);
    }//GEN-LAST:event_algoSuiteComboActionPerformed
            
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox algoSuiteCombo;
    private javax.swing.JLabel algoSuiteLabel;
    private javax.swing.JCheckBox derivedKeysChBox;
    private javax.swing.JCheckBox encryptOrderChBox;
    private javax.swing.JCheckBox encryptSignatureChBox;
    private javax.swing.JComboBox layoutCombo;
    private javax.swing.JLabel layoutLabel;
    private javax.swing.JCheckBox reqDerivedKeys;
    private javax.swing.JComboBox samlVersionCombo;
    private javax.swing.JLabel samlVersionLabel;
    private javax.swing.JCheckBox secConvChBox;
    // End of variables declaration//GEN-END:variables
    
}
