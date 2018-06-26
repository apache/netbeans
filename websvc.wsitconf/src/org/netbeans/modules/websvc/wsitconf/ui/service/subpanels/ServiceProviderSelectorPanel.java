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

package org.netbeans.modules.websvc.wsitconf.ui.service.subpanels;

import org.netbeans.modules.websvc.wsitconf.ui.ComboConstants;

/**
 *
 * @author  Martin Grebac
 */
public class ServiceProviderSelectorPanel extends javax.swing.JPanel {

    private boolean inSync = false;
    
    /**
     * Creates new form ServiceProviderSelectorPanel
     */
    public ServiceProviderSelectorPanel(String spUrl, String certAlias, String tokenType, String keyType) {
        super();
        
        initComponents();
        
        /* issue 232988: the background color issues with dark metal L&F
        certAliasTextField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        certAliasLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        spUrlLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        spUrlTextField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        tokenTypeCombo.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        tokenTypeLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        tokenTypeCombo.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        tokenTypeLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        */
        
        inSync = true;
        tokenTypeCombo.removeAllItems();
        tokenTypeCombo.addItem(ComboConstants.ISSUED_TOKENTYPE_SAML10);
        tokenTypeCombo.addItem(ComboConstants.ISSUED_TOKENTYPE_SAML11);
        tokenTypeCombo.addItem(ComboConstants.ISSUED_TOKENTYPE_SAML20);
        tokenTypeCombo.setSelectedIndex(1); //saml11
        
        keyTypeCombo.removeAllItems();
        keyTypeCombo.addItem(ComboConstants.ISSUED_KEYTYPE_PUBLIC);
        keyTypeCombo.addItem(ComboConstants.ISSUED_KEYTYPE_SYMMETRIC);
        
        this.setSpUrl(spUrl);
        this.setCertAlias(certAlias);
        this.setTokenType(tokenType);
        this.setKeyType(keyType);

        inSync = false;
    }

    public String getSpUrl() {
        return spUrlTextField.getText();
    }

    public void setSpUrl(String spUrl) {
        this.spUrlTextField.setText(spUrl);
    }

    public String getCertAlias() {
        return certAliasTextField.getText();
    }

    public void setCertAlias(String certAlias) {
        this.certAliasTextField.setText(certAlias);
    }

    public String getTokenType() {
        return (String)tokenTypeCombo.getSelectedItem();
    }

    public void setTokenType(String tokenType) {
        if (tokenType != null) {
            this.tokenTypeCombo.setSelectedItem(tokenType);
        }
    }

    public String getKeyType() {
        return (String)keyTypeCombo.getSelectedItem();
    }

    public void setKeyType(String keyType) {
        if (keyType != null) {
            this.keyTypeCombo.setSelectedItem(keyType);
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        spUrlLabel = new javax.swing.JLabel();
        certAliasLabel = new javax.swing.JLabel();
        tokenTypeLabel = new javax.swing.JLabel();
        spUrlTextField = new javax.swing.JTextField();
        certAliasTextField = new javax.swing.JTextField();
        tokenTypeCombo = new javax.swing.JComboBox();
        keyTypeLabel = new javax.swing.JLabel();
        keyTypeCombo = new javax.swing.JComboBox();

        spUrlLabel.setText(org.openide.util.NbBundle.getMessage(ServiceProviderSelectorPanel.class, "LBL_STSConfig_ProviderURL")); // NOI18N

        certAliasLabel.setText(org.openide.util.NbBundle.getMessage(ServiceProviderSelectorPanel.class, "LBL_STSConfig_Alias")); // NOI18N

        tokenTypeLabel.setText(org.openide.util.NbBundle.getMessage(ServiceProviderSelectorPanel.class, "LBL_STSConfig_TokenType")); // NOI18N

        keyTypeLabel.setText(org.openide.util.NbBundle.getMessage(ServiceProviderSelectorPanel.class, "LBL_STSConfig_KeyType")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(spUrlLabel)
                    .addComponent(certAliasLabel)
                    .addComponent(tokenTypeLabel)
                    .addComponent(keyTypeLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(spUrlTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 288, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(certAliasTextField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 126, Short.MAX_VALUE)
                            .addComponent(keyTypeCombo, javax.swing.GroupLayout.Alignment.LEADING, 0, 126, Short.MAX_VALUE)
                            .addComponent(tokenTypeCombo, javax.swing.GroupLayout.Alignment.LEADING, 0, 126, Short.MAX_VALUE))
                        .addGap(174, 174, 174)))
                .addGap(0, 0, 0))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(spUrlLabel)
                    .addComponent(spUrlTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(certAliasLabel)
                    .addComponent(certAliasTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(tokenTypeLabel)
                    .addComponent(tokenTypeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(keyTypeLabel)
                    .addComponent(keyTypeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
            
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel certAliasLabel;
    private javax.swing.JTextField certAliasTextField;
    private javax.swing.JComboBox keyTypeCombo;
    private javax.swing.JLabel keyTypeLabel;
    private javax.swing.JLabel spUrlLabel;
    private javax.swing.JTextField spUrlTextField;
    private javax.swing.JComboBox tokenTypeCombo;
    private javax.swing.JLabel tokenTypeLabel;
    // End of variables declaration//GEN-END:variables
    
}
