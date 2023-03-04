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

package org.netbeans.modules.websvc.wsitconf.ui.client.subpanels;

import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.ProprietarySecurityPolicyModelHelper;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.Validator;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;

import javax.swing.*;
import java.util.Set;
import org.netbeans.modules.websvc.wsitconf.ui.ClassDialog;

/**
 *
 * @author Martin Grebac
 */
public class ValidatorsPanel extends JPanel {

    private WSDLComponent comp;

    private Project p;
    
    private boolean inSync = false;
    
    public ValidatorsPanel(WSDLComponent comp, Project p) {
        super();
        this.comp = comp;
        this.p = p;
        
        initComponents();
        
        /* issue 232988: the background color issues with dark metal L&F
        timestampValidatorTextField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        timestampValidatorLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        certificateValidatorTextField.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        certificateValidatorLabel.setBackground(SectionVisualTheme.getDocumentBackgroundColor());
        */
        
        sync();
    }

    private String getValidator(String type) {
        if (Validator.TIMESTAMP_VALIDATOR.equals(type)) return timestampValidatorTextField.getText();
        if (Validator.CERTIFICATE_VALIDATOR.equals(type)) return certificateValidatorTextField.getText();
        return null;
    }

    private void setValidator(String type, String validator) {
        if (Validator.TIMESTAMP_VALIDATOR.equals(type)) this.timestampValidatorTextField.setText(validator);
        if (Validator.CERTIFICATE_VALIDATOR.equals(type)) this.certificateValidatorTextField.setText(validator);
    }
    
    private void sync() {
        inSync = true;
                
        String timestampValidator = ProprietarySecurityPolicyModelHelper.getValidator(comp, Validator.TIMESTAMP_VALIDATOR);
        if (timestampValidator != null) {
            setValidator(Validator.TIMESTAMP_VALIDATOR, timestampValidator);
        }
        String certificateValidator = ProprietarySecurityPolicyModelHelper.getValidator(comp, Validator.CERTIFICATE_VALIDATOR);
        if (certificateValidator != null) {
            setValidator(Validator.CERTIFICATE_VALIDATOR, certificateValidator);
        }
        
        enableDisable();
        
        inSync = false;
    }
    
    private void enableDisable() {

        boolean certRequired = true;
        boolean timeRequired = true;
//        if (ComboConstants.PROF_USERNAME.equals(profile)) {
//             certRequired = false;
//             timeRequired = false;
//        }
//        if (ComboConstants.PROF_MUTUALCERT.equals(profile) ||
//            ComboConstants.PROF_ENDORSCERT.equals(profile)) {
//             timeRequired = false;
//        }
//        if (ComboConstants.PROF_MSGAUTHSSL.equals(profile)) {
//             //TODO - depends on username  or certificate token in the profile
//             timeRequired = false;
//        }
//        if (ComboConstants.PROF_SAMLSSL.equals(profile) ||
//            ComboConstants.PROF_SAMLHOLDER.equals(profile) ||
//            ComboConstants.PROF_SAMLSENDER.equals(profile)) {
//             certRequired = false;
//             timeRequired = false;
//        }
        
        timestampValidatorButton.setEnabled(timeRequired);
        timestampValidatorLabel.setEnabled(timeRequired);
        timestampValidatorTextField.setEnabled(timeRequired);
        certificateValidatorButton.setEnabled(certRequired);
        certificateValidatorLabel.setEnabled(certRequired);
        certificateValidatorTextField.setEnabled(certRequired);
    }
    
    public void storeState() {
        String certV = getValidator(Validator.CERTIFICATE_VALIDATOR);
        if ((certV == null) || (certV.length() == 0)) {
            ProprietarySecurityPolicyModelHelper.setValidator(comp, Validator.CERTIFICATE_VALIDATOR, null, true);
        } else {
            ProprietarySecurityPolicyModelHelper.setValidator(comp, Validator.CERTIFICATE_VALIDATOR, certV, true);
        }
        
        String timeV = getValidator(Validator.TIMESTAMP_VALIDATOR);
        if ((timeV == null) || (timeV.length() == 0)) {
            ProprietarySecurityPolicyModelHelper.setValidator(comp, Validator.TIMESTAMP_VALIDATOR, null, true);
        } else {
            ProprietarySecurityPolicyModelHelper.setValidator(comp, Validator.TIMESTAMP_VALIDATOR, timeV, true);
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
        timestampValidatorLabel = new javax.swing.JLabel();
        certificateValidatorLabel = new javax.swing.JLabel();
        timestampValidatorTextField = new javax.swing.JTextField();
        certificateValidatorTextField = new javax.swing.JTextField();
        timestampValidatorButton = new javax.swing.JButton();
        certificateValidatorButton = new javax.swing.JButton();

        jToggleButton1.setText("jToggleButton1");

        timestampValidatorLabel.setText(org.openide.util.NbBundle.getMessage(ValidatorsPanel.class, "LBL_ValidatorPanel_TimestampVLabel")); // NOI18N

        certificateValidatorLabel.setText(org.openide.util.NbBundle.getMessage(ValidatorsPanel.class, "LBL_ValidatorPanel_CertificateVLabel")); // NOI18N

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

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(timestampValidatorLabel)
                    .addComponent(certificateValidatorLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(certificateValidatorTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 305, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(timestampValidatorTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 308, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(certificateValidatorButton)
                    .addComponent(timestampValidatorButton))
                .addGap(3, 3, 3)
                .addComponent(jToggleButton1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {certificateValidatorTextField, timestampValidatorTextField});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(69, 69, 69)
                .addComponent(jToggleButton1))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(26, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(timestampValidatorLabel)
                    .addComponent(timestampValidatorButton)
                    .addComponent(timestampValidatorTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(certificateValidatorLabel)
                    .addComponent(certificateValidatorButton)
                    .addComponent(certificateValidatorTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

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
        
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton certificateValidatorButton;
    private javax.swing.JLabel certificateValidatorLabel;
    private javax.swing.JTextField certificateValidatorTextField;
    private javax.swing.JToggleButton jToggleButton1;
    private javax.swing.JButton timestampValidatorButton;
    private javax.swing.JLabel timestampValidatorLabel;
    private javax.swing.JTextField timestampValidatorTextField;
    // End of variables declaration//GEN-END:variables
    
}
