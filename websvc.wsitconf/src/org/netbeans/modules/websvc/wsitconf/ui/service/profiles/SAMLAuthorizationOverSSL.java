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
import org.netbeans.modules.websvc.wsitconf.ui.ComboConstants;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.AlgoSuiteModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.SecurityPolicyModelHelper;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.SecurityTokensModelHelper;
import org.netbeans.modules.websvc.wsitmodelext.security.WssElement;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.HttpsToken;
import org.netbeans.modules.websvc.wsitmodelext.security.tokens.TransportToken;
import org.netbeans.modules.xml.wsdl.model.Binding;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;

/**
 *
 * @author  Martin Grebac
 */
public class SAMLAuthorizationOverSSL extends ProfileBaseForm {

    /**
     * Creates new form MessageAuthentication
     */
    public SAMLAuthorizationOverSSL(WSDLComponent comp, SecurityProfile secProfile) {
        super(comp, secProfile);
        initComponents();

        inSync = true;
        fillSamlCombo(samlVersionCombo);
        fillWssCombo(wssVersionCombo);
        fillLayoutCombo(layoutCombo);
        fillAlgoSuiteCombo(algoSuiteCombo);
        inSync = false;
        
        sync();
    }
    
    protected void sync() {
        inSync = true;

        WSDLComponent secBinding = SecurityPolicyModelHelper.getSecurityBindingTypeElement(comp);        

        if (comp instanceof Binding) {
            WSDLComponent tokenKind = SecurityTokensModelHelper.getSupportingToken(comp, SecurityTokensModelHelper.SIGNED_SUPPORTING);
            WSDLComponent token = SecurityTokensModelHelper.getTokenTypeElement(tokenKind);
            String samlVersion = SecurityTokensModelHelper.getTokenProfileVersion(token);
            setCombo(samlVersionCombo, samlVersion);
        }
        
        setCombo(wssVersionCombo, SecurityPolicyModelHelper.isWss11(comp));
        setCombo(algoSuiteCombo, AlgoSuiteModelHelper.getAlgorithmSuite(secBinding));
        setCombo(layoutCombo, SecurityPolicyModelHelper.getMessageLayout(comp));
        setChBox(reqSigConfChBox, SecurityPolicyModelHelper.isRequireSignatureConfirmation(comp));

        enableDisable();
        
        inSync = false;
    }

    protected void enableDisable() {
        boolean wss11 = ComboConstants.WSS11.equals(wssVersionCombo.getSelectedItem());
        reqSigConfChBox.setEnabled(wss11);
    }
    
    public void setValue(javax.swing.JComponent source) {

        if (inSync) return;
            
        WSDLComponent secBinding = SecurityPolicyModelHelper.getSecurityBindingTypeElement(comp);        

        SecurityPolicyModelHelper spmh = SecurityPolicyModelHelper.getInstance(cfgVersion);
        SecurityTokensModelHelper stmh = SecurityTokensModelHelper.getInstance(cfgVersion);
        AlgoSuiteModelHelper asmh = AlgoSuiteModelHelper.getInstance(cfgVersion);
        if (source.equals(reqClientCertChBox)) {
            WSDLComponent tokenKind = SecurityTokensModelHelper.getTokenElement(secBinding, TransportToken.class);
            HttpsToken token = (HttpsToken) SecurityTokensModelHelper.getTokenTypeElement(tokenKind);
            SecurityTokensModelHelper.setRequireClientCertificate(token, reqClientCertChBox.isSelected());
        }
        if (source.equals(reqSigConfChBox)) {
            spmh.enableRequireSignatureConfirmation(SecurityPolicyModelHelper.getWss11(comp), reqSigConfChBox.isSelected());
        }
        if (source.equals(layoutCombo)) {
            spmh.setLayout(secBinding, (String) layoutCombo.getSelectedItem());
        }
        if (source.equals(algoSuiteCombo)) {
            asmh.setAlgorithmSuite(secBinding, (String) algoSuiteCombo.getSelectedItem());
        }
        if (source.equals(wssVersionCombo)) {
            boolean wss11 = ComboConstants.WSS11.equals(wssVersionCombo.getSelectedItem());
            WssElement wss = spmh.enableWss(comp, wss11);
            if (wss11) {
                spmh.enableRequireSignatureConfirmation(SecurityPolicyModelHelper.getWss11(comp), reqSigConfChBox.isSelected());
            }
//            spmh.enableMustSupportRefKeyIdentifier(wss, true);
        }
        if (source.equals(samlVersionCombo)) {            
            if (comp instanceof Binding) {
                WSDLComponent tokenKind = SecurityTokensModelHelper.getSupportingToken(comp, 
                                            SecurityTokensModelHelper.SIGNED_SUPPORTING);
                WSDLComponent token = SecurityTokensModelHelper.getTokenTypeElement(tokenKind);
                stmh.setTokenProfileVersion(token, (String) samlVersionCombo.getSelectedItem());
            }
        }
        enableDisable();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        samlVersionLabel = new javax.swing.JLabel();
        samlVersionCombo = new javax.swing.JComboBox();
        reqSigConfChBox = new javax.swing.JCheckBox();
        reqClientCertChBox = new javax.swing.JCheckBox();
        wssVersionLabel = new javax.swing.JLabel();
        wssVersionCombo = new javax.swing.JComboBox();
        algoSuiteLabel = new javax.swing.JLabel();
        algoSuiteCombo = new javax.swing.JComboBox();
        layoutLabel = new javax.swing.JLabel();
        layoutCombo = new javax.swing.JComboBox();

        samlVersionLabel.setLabelFor(samlVersionCombo);
        org.openide.awt.Mnemonics.setLocalizedText(samlVersionLabel, org.openide.util.NbBundle.getMessage(SAMLAuthorizationOverSSL.class, "LBL_SamlVersion")); // NOI18N

        samlVersionCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                samlVersionComboActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(reqSigConfChBox, org.openide.util.NbBundle.getMessage(SAMLAuthorizationOverSSL.class, "LBL_RequireSigConfirmation")); // NOI18N
        reqSigConfChBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        reqSigConfChBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        reqSigConfChBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reqSigConfChBoxActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(reqClientCertChBox, org.openide.util.NbBundle.getMessage(SAMLAuthorizationOverSSL.class, "LBL_RequireClientCertificate")); // NOI18N
        reqClientCertChBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        reqClientCertChBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        reqClientCertChBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reqClientCertChBoxActionPerformed(evt);
            }
        });

        wssVersionLabel.setLabelFor(wssVersionCombo);
        org.openide.awt.Mnemonics.setLocalizedText(wssVersionLabel, org.openide.util.NbBundle.getMessage(SAMLAuthorizationOverSSL.class, "LBL_WSSVersionLabel")); // NOI18N

        wssVersionCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                wssVersionComboActionPerformed(evt);
            }
        });

        algoSuiteLabel.setLabelFor(algoSuiteCombo);
        org.openide.awt.Mnemonics.setLocalizedText(algoSuiteLabel, org.openide.util.NbBundle.getMessage(SAMLAuthorizationOverSSL.class, "LBL_AlgoSuiteLabel")); // NOI18N

        algoSuiteCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                algoSuiteComboActionPerformed(evt);
            }
        });

        layoutLabel.setLabelFor(layoutCombo);
        org.openide.awt.Mnemonics.setLocalizedText(layoutLabel, org.openide.util.NbBundle.getMessage(SAMLAuthorizationOverSSL.class, "LBL_LayoutLabel")); // NOI18N

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
                    .addComponent(reqClientCertChBox)
                    .addComponent(reqSigConfChBox)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(samlVersionLabel)
                            .addComponent(wssVersionLabel)
                            .addComponent(algoSuiteLabel)
                            .addComponent(layoutLabel))
                        .addGap(12, 12, 12)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(samlVersionCombo, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(wssVersionCombo, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(algoSuiteCombo, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(layoutCombo, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {algoSuiteCombo, layoutCombo, samlVersionCombo, wssVersionCombo});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(samlVersionLabel)
                    .addComponent(samlVersionCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                .addComponent(reqClientCertChBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(reqSigConfChBox)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {algoSuiteCombo, layoutCombo, samlVersionCombo, wssVersionCombo});

    }// </editor-fold>//GEN-END:initComponents

    private void reqSigConfChBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reqSigConfChBoxActionPerformed
        setValue(reqSigConfChBox);
    }//GEN-LAST:event_reqSigConfChBoxActionPerformed

    private void reqClientCertChBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reqClientCertChBoxActionPerformed
        setValue(reqClientCertChBox);
    }//GEN-LAST:event_reqClientCertChBoxActionPerformed

    private void wssVersionComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_wssVersionComboActionPerformed
        setValue(wssVersionCombo);
    }//GEN-LAST:event_wssVersionComboActionPerformed

    private void samlVersionComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_samlVersionComboActionPerformed
        setValue(samlVersionCombo);
    }//GEN-LAST:event_samlVersionComboActionPerformed

    private void layoutComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_layoutComboActionPerformed
        setValue(layoutCombo);
    }//GEN-LAST:event_layoutComboActionPerformed

    private void algoSuiteComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_algoSuiteComboActionPerformed
        setValue(algoSuiteCombo);
    }//GEN-LAST:event_algoSuiteComboActionPerformed
            
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox algoSuiteCombo;
    private javax.swing.JLabel algoSuiteLabel;
    private javax.swing.JComboBox layoutCombo;
    private javax.swing.JLabel layoutLabel;
    private javax.swing.JCheckBox reqClientCertChBox;
    private javax.swing.JCheckBox reqSigConfChBox;
    private javax.swing.JComboBox samlVersionCombo;
    private javax.swing.JLabel samlVersionLabel;
    private javax.swing.JComboBox wssVersionCombo;
    private javax.swing.JLabel wssVersionLabel;
    // End of variables declaration//GEN-END:variables
    
}
