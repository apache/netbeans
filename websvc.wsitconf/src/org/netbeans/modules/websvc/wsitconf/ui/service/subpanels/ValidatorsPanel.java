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

import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.ProprietarySecurityPolicyModelHelper;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.Validator;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;

import javax.swing.*;
import java.util.Set;
import org.netbeans.modules.websvc.wsitconf.ui.ClassDialog;
import org.netbeans.modules.websvc.wsitconf.ui.ComboConstants;
import org.netbeans.modules.websvc.wsitmodelext.versioning.ConfigVersion;

/**
 *
 * @author Martin Grebac
 */
public class ValidatorsPanel extends JPanel {

    private WSDLComponent comp;

    private Project p;
    
    private boolean inSync = false;
    private String profile;
    
    private ConfigVersion cfgVersion = null;
        
    public ValidatorsPanel(WSDLComponent comp, Project p, String profile, ConfigVersion cfgVersion) {
        super();
        this.comp = comp;
        this.p = p;
        this.profile = profile;
        this.cfgVersion = cfgVersion;
        
        initComponents();

        /* issue 232988: the background color issues with dark metal L&F
        usernameValidatorTextField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        usernameValidatorLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        timestampValidatorTextField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        timestampValidatorLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        certificateValidatorTextField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        certificateValidatorLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        samlValidatorTextField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        samlValidatorLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        */
        
        sync();
    }

    private String getValidator(String type) {
        if (Validator.USERNAME_VALIDATOR.equals(type)) return usernameValidatorTextField.getText();
        if (Validator.TIMESTAMP_VALIDATOR.equals(type)) return timestampValidatorTextField.getText();
        if (Validator.CERTIFICATE_VALIDATOR.equals(type)) return certificateValidatorTextField.getText();
        if (Validator.SAML_VALIDATOR.equals(type)) return samlValidatorTextField.getText();
        return null;
    }

    private void setValidator(String type, String validator) {
        if (Validator.USERNAME_VALIDATOR.equals(type)) this.usernameValidatorTextField.setText(validator);
        if (Validator.TIMESTAMP_VALIDATOR.equals(type)) this.timestampValidatorTextField.setText(validator);
        if (Validator.CERTIFICATE_VALIDATOR.equals(type)) this.certificateValidatorTextField.setText(validator);
        if (Validator.SAML_VALIDATOR.equals(type)) this.samlValidatorTextField.setText(validator);
    }
    
    private void sync() {
        inSync = true;
                
        String usernameValidator = ProprietarySecurityPolicyModelHelper.getValidator(comp, Validator.USERNAME_VALIDATOR);
        if (usernameValidator != null) {
            setValidator(Validator.USERNAME_VALIDATOR, usernameValidator);
        }
        String timestampValidator = ProprietarySecurityPolicyModelHelper.getValidator(comp, Validator.TIMESTAMP_VALIDATOR);
        if (timestampValidator != null) {
            setValidator(Validator.TIMESTAMP_VALIDATOR, timestampValidator);
        }
        String certificateValidator = ProprietarySecurityPolicyModelHelper.getValidator(comp, Validator.CERTIFICATE_VALIDATOR);
        if (certificateValidator != null) {
            setValidator(Validator.CERTIFICATE_VALIDATOR, certificateValidator);
        }
        String samlValidator = ProprietarySecurityPolicyModelHelper.getValidator(comp, Validator.SAML_VALIDATOR);
        if (samlValidator != null) {
            setValidator(Validator.SAML_VALIDATOR, samlValidator);
        }
        
        enableDisable();
        
        inSync = false;
    }
    
    private void enableDisable() {

        boolean samlRequired = true;
        boolean userRequired = true;
        boolean certRequired = true;
        boolean timeRequired = true;
        if (ComboConstants.PROF_USERNAME.equals(profile)) {
             samlRequired = false;
             certRequired = false;
             timeRequired = false;
        }
        if (ComboConstants.PROF_MUTUALCERT.equals(profile) ||
            ComboConstants.PROF_ENDORSCERT.equals(profile)) {
             samlRequired = false;
             userRequired = false;
             timeRequired = false;
        }
        if (ComboConstants.PROF_MSGAUTHSSL.equals(profile)) {
             //TODO - depends on username  or certificate token in the profile
             samlRequired = false;
             timeRequired = false;
        }
        if (ComboConstants.PROF_SAMLSSL.equals(profile) || 
            ComboConstants.PROF_SAMLHOLDER.equals(profile) ||
            ComboConstants.PROF_SAMLSENDER.equals(profile)) {
             certRequired = false;
             userRequired = false;
             timeRequired = false;
        }
//        if (ComboConstants.PROF_STSISSUED.equals(profile) ||
//            ComboConstants.PROF_STSISSUEDCERT.equals(profile) ||
//            ComboConstants.PROF_STSISSUEDSUPPORTING.equals(profile) ||
//            ComboConstants.PROF_STSISSUEDENDORSE.equals(profile)) {
//             certRequired = false;
//             userRequired = false;
//             timeRequired = false;
//        }
        
        samlValidatorButton.setEnabled(samlRequired);
        samlValidatorLabel.setEnabled(samlRequired);
        samlValidatorTextField.setEnabled(samlRequired);
        usernameValidatorButton.setEnabled(userRequired);
        usernameValidatorLabel.setEnabled(userRequired);
        usernameValidatorTextField.setEnabled(userRequired);
        timestampValidatorButton.setEnabled(timeRequired);
        timestampValidatorLabel.setEnabled(timeRequired);
        timestampValidatorTextField.setEnabled(timeRequired);
        certificateValidatorButton.setEnabled(certRequired);
        certificateValidatorLabel.setEnabled(certRequired);
        certificateValidatorTextField.setEnabled(certRequired);
    }
    
    public void storeState() {
        String usernameV = getValidator(Validator.USERNAME_VALIDATOR);
        if ((usernameV == null) || (usernameV.length() == 0)) {
            ProprietarySecurityPolicyModelHelper.setValidator(comp, Validator.USERNAME_VALIDATOR, null, false);
        } else {
            ProprietarySecurityPolicyModelHelper.setValidator(comp, Validator.USERNAME_VALIDATOR, usernameV, false);
        }
        
        String certV = getValidator(Validator.CERTIFICATE_VALIDATOR);
        if ((certV == null) || (certV.length() == 0)) {
            ProprietarySecurityPolicyModelHelper.setValidator(comp, Validator.CERTIFICATE_VALIDATOR, null, false);
        } else {
            ProprietarySecurityPolicyModelHelper.setValidator(comp, Validator.CERTIFICATE_VALIDATOR, certV, false);
        }
        
        String timeV = getValidator(Validator.TIMESTAMP_VALIDATOR);
        if ((timeV == null) || (timeV.length() == 0)) {
            ProprietarySecurityPolicyModelHelper.setValidator(comp, Validator.TIMESTAMP_VALIDATOR, null, false);
        } else {
            ProprietarySecurityPolicyModelHelper.setValidator(comp, Validator.TIMESTAMP_VALIDATOR, timeV, false);
        }
        
        String samlV = getValidator(Validator.SAML_VALIDATOR);
        if ((samlV == null) || (samlV.length() == 0)) {
            ProprietarySecurityPolicyModelHelper.setValidator(comp, Validator.SAML_VALIDATOR, null, false);
        } else {
            ProprietarySecurityPolicyModelHelper.setValidator(comp, Validator.SAML_VALIDATOR, samlV, false);
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jToggleButton1 = new javax.swing.JToggleButton();
        usernameValidatorLabel = new javax.swing.JLabel();
        timestampValidatorLabel = new javax.swing.JLabel();
        certificateValidatorLabel = new javax.swing.JLabel();
        samlValidatorLabel = new javax.swing.JLabel();
        usernameValidatorTextField = new javax.swing.JTextField();
        timestampValidatorTextField = new javax.swing.JTextField();
        certificateValidatorTextField = new javax.swing.JTextField();
        samlValidatorTextField = new javax.swing.JTextField();
        usernameValidatorButton = new javax.swing.JButton();
        timestampValidatorButton = new javax.swing.JButton();
        certificateValidatorButton = new javax.swing.JButton();
        samlValidatorButton = new javax.swing.JButton();

        jToggleButton1.setText("jToggleButton1");

        usernameValidatorLabel.setText(org.openide.util.NbBundle.getMessage(ValidatorsPanel.class, "LBL_ValidatorPanel_UsernameVLabel")); // NOI18N

        timestampValidatorLabel.setText(org.openide.util.NbBundle.getMessage(ValidatorsPanel.class, "LBL_ValidatorPanel_TimestampVLabel")); // NOI18N

        certificateValidatorLabel.setText(org.openide.util.NbBundle.getMessage(ValidatorsPanel.class, "LBL_ValidatorPanel_CertificateVLabel")); // NOI18N

        samlValidatorLabel.setText(org.openide.util.NbBundle.getMessage(ValidatorsPanel.class, "LBL_ValidatorPanel_SAMLVLabel")); // NOI18N

        usernameValidatorButton.setText(org.openide.util.NbBundle.getMessage(ValidatorsPanel.class, "LBL_ValidatorPanel_Browse")); // NOI18N
        usernameValidatorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                usernameValidatorButtonActionPerformed(evt);
            }
        });

        timestampValidatorButton.setText(org.openide.util.NbBundle.getMessage(ValidatorsPanel.class, "LBL_ValidatorPanel_Browse")); // NOI18N
        timestampValidatorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                timestampValidatorButtonActionPerformed(evt);
            }
        });

        certificateValidatorButton.setText(org.openide.util.NbBundle.getMessage(ValidatorsPanel.class, "LBL_ValidatorPanel_Browse")); // NOI18N
        certificateValidatorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                certificateValidatorButtonActionPerformed(evt);
            }
        });

        samlValidatorButton.setText(org.openide.util.NbBundle.getMessage(ValidatorsPanel.class, "LBL_ValidatorPanel_Browse")); // NOI18N
        samlValidatorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                samlValidatorButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(timestampValidatorLabel)
                    .addComponent(certificateValidatorLabel)
                    .addComponent(samlValidatorLabel)
                    .addComponent(usernameValidatorLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(samlValidatorTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 296, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(certificateValidatorTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 305, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(timestampValidatorTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 308, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(usernameValidatorTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 308, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(samlValidatorButton)
                    .addComponent(certificateValidatorButton)
                    .addComponent(timestampValidatorButton)
                    .addComponent(usernameValidatorButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToggleButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {certificateValidatorTextField, samlValidatorTextField, timestampValidatorTextField, usernameValidatorTextField});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(69, 69, 69)
                        .addComponent(jToggleButton1))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(usernameValidatorLabel)
                            .addComponent(usernameValidatorButton)
                            .addComponent(usernameValidatorTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(timestampValidatorLabel)
                            .addComponent(timestampValidatorButton)
                            .addComponent(timestampValidatorTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(certificateValidatorLabel)
                            .addComponent(certificateValidatorButton)
                            .addComponent(certificateValidatorTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(samlValidatorLabel)
                            .addComponent(samlValidatorButton)
                            .addComponent(samlValidatorTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void samlValidatorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_samlValidatorButtonActionPerformed
        if (p != null) {
            ClassDialog classDialog = new ClassDialog(p, "com.sun.xml.wss.impl.callback.SAMLAssertionValidator"); //NOI18N
            classDialog.show();
            if (classDialog.okButtonPressed()) {
                Set<String> selectedClasses = classDialog.getSelectedClasses();
                for (String selectedClass : selectedClasses) {
                    setValidator(Validator.SAML_VALIDATOR, selectedClass);
                    break;
                }
            }
        }
    }//GEN-LAST:event_samlValidatorButtonActionPerformed

    private void certificateValidatorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_certificateValidatorButtonActionPerformed
        if (p != null) {
            ClassDialog classDialog = new ClassDialog(p, "com.sun.xml.wss.impl.callback.CertificateValidationCallback.CertificateValidator"); //NOI18N
            classDialog.show();
            if (classDialog.okButtonPressed()) {
                Set<String> selectedClasses = classDialog.getSelectedClasses();
                for (String selectedClass : selectedClasses) {
                    setValidator(Validator.CERTIFICATE_VALIDATOR, selectedClass);
                    break;
                }
            }
        }
    }//GEN-LAST:event_certificateValidatorButtonActionPerformed

    private void timestampValidatorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_timestampValidatorButtonActionPerformed
        if (p != null) {
            ClassDialog classDialog = new ClassDialog(p, "com.sun.xml.wss.impl.callback.TimestampValidationCallback.TimestampValidator"); //NOI18N
            classDialog.show();
            if (classDialog.okButtonPressed()) {
                Set<String> selectedClasses = classDialog.getSelectedClasses();
                for (String selectedClass : selectedClasses) {
                    setValidator(Validator.TIMESTAMP_VALIDATOR, selectedClass);
                    break;
                }
            }
        }
    }//GEN-LAST:event_timestampValidatorButtonActionPerformed

    private void usernameValidatorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_usernameValidatorButtonActionPerformed
        if (p != null) {
            ClassDialog classDialog = new ClassDialog(p, "com.sun.xml.wss.impl.callback.PasswordValidationCallback.PasswordValidator"); //NOI18N
            classDialog.show();
            if (classDialog.okButtonPressed()) {
                Set<String> selectedClasses = classDialog.getSelectedClasses();
                for (String selectedClass : selectedClasses) {
                    setValidator(Validator.USERNAME_VALIDATOR, selectedClass);
                    break;
                }
            }
        }
    }//GEN-LAST:event_usernameValidatorButtonActionPerformed
        
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton certificateValidatorButton;
    private javax.swing.JLabel certificateValidatorLabel;
    private javax.swing.JTextField certificateValidatorTextField;
    private javax.swing.JToggleButton jToggleButton1;
    private javax.swing.JButton samlValidatorButton;
    private javax.swing.JLabel samlValidatorLabel;
    private javax.swing.JTextField samlValidatorTextField;
    private javax.swing.JButton timestampValidatorButton;
    private javax.swing.JLabel timestampValidatorLabel;
    private javax.swing.JTextField timestampValidatorTextField;
    private javax.swing.JButton usernameValidatorButton;
    private javax.swing.JLabel usernameValidatorLabel;
    private javax.swing.JTextField usernameValidatorTextField;
    // End of variables declaration//GEN-END:variables
    
}
