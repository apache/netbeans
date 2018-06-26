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

package org.netbeans.modules.websvc.wsitconf.ui.client.subpanels;

import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.ProprietarySecurityPolicyModelHelper;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import javax.swing.*;
import org.netbeans.api.project.Project;

/**
 *
 * @author Martin Grebac
 */
public class KerberosConfigPanel extends JPanel {

    private WSDLComponent comp;
    private Project project = null;    
    private boolean inSync = false;
    
    public KerberosConfigPanel(WSDLComponent comp, Project p) {
        super();
        this.comp = comp;
        this.project = p;
        
        initComponents();

        /* issue 232988: the background color issues with dark metal L&F
        loginModuleCombo.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        loginModuleLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        servicePrincipalLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        servicePrincipalField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        credDelegationChBox.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        */

        sync();
    }

    private String getLoginModule() {
        return (String) this.loginModuleCombo.getSelectedItem();
    }

    private void setLoginModule(String alias) {
        this.loginModuleCombo.setSelectedItem(alias);
    }

    private String getServicePrincipal() {
        return String.valueOf(this.servicePrincipalField.getText());
    }

    private void setServicePrincipal(String principal) {
        this.servicePrincipalField.setText(principal);
    }
    
    String servicePrincipal = null;
    String loginModule = null;
    boolean credentialDelegation = false;
    
    public void sync() {
        inSync = true;

        servicePrincipal = ProprietarySecurityPolicyModelHelper.getServicePrincipal(comp);
        if (servicePrincipal != null) {
            setServicePrincipal(servicePrincipal);
        }

        loginModule = ProprietarySecurityPolicyModelHelper.getLoginModule(comp);
        if (loginModule != null) {
            setLoginModule(loginModule);
        }

        credentialDelegation = ProprietarySecurityPolicyModelHelper.isCredentialDelegation(comp);
        setChBox(credDelegationChBox, credentialDelegation);

        //        enableDisable();

        inSync = false;
    }

//    private void enableDisable() {
//        boolean gf = Util.isGlassfish(project);
//        keyPasswordField.setEnabled(!gf);
//        keyPasswordLabel.setEnabled(!gf);
//    }
        
    public void storeState() {
        loginModule = getLoginModule();
        if ((loginModule == null) || (loginModule.length() == 0)) {
            ProprietarySecurityPolicyModelHelper.setLoginModule(comp, null, true);
        } else {
            ProprietarySecurityPolicyModelHelper.setLoginModule(comp, loginModule, true);
        }

        servicePrincipal = getServicePrincipal();
        if ((servicePrincipal == null) || (servicePrincipal.length() == 0)) {
            ProprietarySecurityPolicyModelHelper.setServicePrincipal(comp, null, true);
        } else {
            ProprietarySecurityPolicyModelHelper.setServicePrincipal(comp, servicePrincipal, true);
        }
        
        credentialDelegation = getChBox(credDelegationChBox);
        ProprietarySecurityPolicyModelHelper.setCredentialDelegation(comp, credentialDelegation, true);        
    }
    
    public Boolean getChBox(JCheckBox chBox) {
        if (chBox.isSelected()) {
            return Boolean.TRUE;
        }
        return Boolean.FALSE;
    }
    
    private void setChBox(JCheckBox chBox, Boolean enable) {
        if (enable == null) {
            chBox.setSelected(false);
        } else {
            chBox.setSelected(enable);
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        servicePrincipalLabel = new javax.swing.JLabel();
        loginModuleLabel = new javax.swing.JLabel();
        loginModuleCombo = new javax.swing.JComboBox();
        credDelegationChBox = new javax.swing.JCheckBox();
        servicePrincipalField = new javax.swing.JTextField();

        org.openide.awt.Mnemonics.setLocalizedText(servicePrincipalLabel, org.openide.util.NbBundle.getMessage(KerberosConfigPanel.class, "LBL_KerberosConfigPanel_ServicePrincipal")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(loginModuleLabel, org.openide.util.NbBundle.getMessage(KerberosConfigPanel.class, "LBL_KerberosConfigPanel_LoginModule")); // NOI18N

        loginModuleCombo.setEditable(true);

        org.openide.awt.Mnemonics.setLocalizedText(credDelegationChBox, org.openide.util.NbBundle.getMessage(KerberosConfigPanel.class, "LBL_KerberosPanel_CredentialDelegation")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(loginModuleLabel)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(servicePrincipalLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(servicePrincipalField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(loginModuleCombo, 0, 171, Short.MAX_VALUE)))
                    .addComponent(credDelegationChBox))
                .addContainerGap())
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {loginModuleCombo, servicePrincipalField});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(13, 13, 13)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(loginModuleLabel)
                    .addComponent(loginModuleCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(servicePrincipalLabel)
                    .addComponent(servicePrincipalField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(credDelegationChBox)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        layout.linkSize(javax.swing.SwingConstants.VERTICAL, new java.awt.Component[] {loginModuleCombo, servicePrincipalField});

    }// </editor-fold>//GEN-END:initComponents
        
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox credDelegationChBox;
    private javax.swing.JComboBox loginModuleCombo;
    private javax.swing.JLabel loginModuleLabel;
    private javax.swing.JTextField servicePrincipalField;
    private javax.swing.JLabel servicePrincipalLabel;
    // End of variables declaration//GEN-END:variables
    
}
