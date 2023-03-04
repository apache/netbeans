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

package org.netbeans.modules.websvc.wsitconf.ui.service.subpanels;

import java.text.NumberFormat;
import java.util.Set;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import javax.swing.GroupLayout;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.wsitconf.ui.ClassDialog;
import org.netbeans.modules.websvc.wsitmodelext.versioning.ConfigVersion;
import org.netbeans.modules.websvc.wsitconf.wsdlmodelext.ProprietarySecurityPolicyModelHelper;
import org.netbeans.modules.websvc.wsitmodelext.security.proprietary.service.STSConfiguration;
import org.netbeans.modules.xml.wsdl.model.Binding;

/**
 *
 * @author Martin Grebac
 */
public class STSConfigServicePanel extends JPanel {
    
    private Binding binding;
    private Project project;

    private boolean inSync = false;
    
    private DefaultFormatterFactory lifeTimeDff = null;
    
    private ConfigVersion cfgVersion = null;
    
    /**
     * Creates new form STSConfigServicePanel
     */
    public STSConfigServicePanel( Project p, Binding binding, ConfigVersion cfgVersion) {
        this.project = p;
        this.binding = binding;
        this.cfgVersion = cfgVersion;

        lifeTimeDff = new DefaultFormatterFactory();
        NumberFormat lifetimeFormat = NumberFormat.getIntegerInstance();
        lifetimeFormat.setGroupingUsed(false);
        lifetimeFormat.setParseIntegerOnly(true);
        lifetimeFormat.setMaximumFractionDigits(0);
        NumberFormatter lifetimeFormatter = new NumberFormatter(lifetimeFormat);
        lifetimeFormatter.setCommitsOnValidEdit(true);
        lifetimeFormatter.setMinimum(0);
        lifeTimeDff.setDefaultFormatter(lifetimeFormatter);

        initComponents();

        inSync = true;
        ServiceProvidersTablePanel.ServiceProvidersTableModel tablemodel = new ServiceProvidersTablePanel.ServiceProvidersTableModel();
        this.remove(serviceProvidersPanel);
        
        STSConfiguration stsConfig = ProprietarySecurityPolicyModelHelper.getSTSConfiguration(binding);
        if (stsConfig == null) {
            stsConfig = ProprietarySecurityPolicyModelHelper.createSTSConfiguration(binding);
        }
        serviceProvidersPanel = new ServiceProvidersTablePanel(tablemodel, stsConfig, cfgVersion);
        ((ServiceProvidersTablePanel)serviceProvidersPanel).populateModel();
        inSync = false;

        sync();
        
    }

    private void sync() {
        inSync = true;

        String lifeTime = ProprietarySecurityPolicyModelHelper.getSTSLifeTime(binding);
        if (lifeTime == null) { // no setup exists yet - set the default
            setLifeTime(ProprietarySecurityPolicyModelHelper.DEFAULT_LIFETIME);
            ProprietarySecurityPolicyModelHelper.setSTSLifeTime(binding, 
                    ProprietarySecurityPolicyModelHelper.DEFAULT_LIFETIME);
        } else {
            setLifeTime(lifeTime);
        } 

        boolean encryptKey = ProprietarySecurityPolicyModelHelper.getSTSEncryptKey(binding);
        setChBox(encryptKeyChBox, encryptKey);

        boolean encryptToken = ProprietarySecurityPolicyModelHelper.getSTSEncryptToken(binding);
        setChBox(encryptTokenChBox, encryptToken);
        
        String issuer = ProprietarySecurityPolicyModelHelper.getSTSIssuer(binding);
        if (issuer != null) {
            setIssuer(issuer);
        } 
        
        String cclass = ProprietarySecurityPolicyModelHelper.getSTSContractClass(binding);
        if (cclass == null) { // no setup exists yet - set the default
            setContractClass(ProprietarySecurityPolicyModelHelper.DEFAULT_CONTRACT_CLASS);
            ProprietarySecurityPolicyModelHelper.setSTSContractClass(binding, 
                    ProprietarySecurityPolicyModelHelper.DEFAULT_CONTRACT_CLASS);
        } else {
            setContractClass(cclass);
        } 
        
        refreshPanels();
        
        inSync = false;
    }
    
    private void setLifeTime(String time) {
        this.lifeTimeTextField.setText(time);
    }

    private void setIssuer(String issuer) {
        this.issuerField.setText(issuer);
    }
    
    private void setChBox(JCheckBox chBox, Boolean enable) {
        if (enable == null) {
            chBox.setSelected(false);
        } else {
            chBox.setSelected(enable);
        }
    }
    
    private void setContractClass(String classname) {
        this.contractTextField.setText(classname);
    }

    private void refreshPanels() {
        updateLayout();
    }
    
    private void updateLayout() {
        GroupLayout layout = (GroupLayout)this.getLayout();
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(serviceProvidersPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(2, 2, 2)
                                .addComponent(lifeTimeLabel))
                            .addComponent(contractLabel)
                            .addComponent(issuerLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lifeTimeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(contractTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 353, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(contractButton))
                            .addComponent(issuerField, javax.swing.GroupLayout.DEFAULT_SIZE, 438, Short.MAX_VALUE))
                        .addGap(10, 10, 10))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(encryptKeyChBox)
                        .addContainerGap(493, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(encryptTokenChBox)
                        .addContainerGap(483, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(issuerLabel)
                    .addComponent(issuerField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(contractLabel)
                    .addComponent(contractButton)
                    .addComponent(contractTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(8, 8, 8)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lifeTimeLabel)
                    .addComponent(lifeTimeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(encryptKeyChBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(encryptTokenChBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(serviceProvidersPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        serviceProvidersPanel = new javax.swing.JPanel();
        lifeTimeLabel = new javax.swing.JLabel();
        contractLabel = new javax.swing.JLabel();
        contractTextField = new javax.swing.JTextField();
        contractButton = new javax.swing.JButton();
        issuerLabel = new javax.swing.JLabel();
        issuerField = new javax.swing.JTextField();
        encryptKeyChBox = new javax.swing.JCheckBox();
        encryptTokenChBox = new javax.swing.JCheckBox();
        lifeTimeTextField = new javax.swing.JFormattedTextField();

        serviceProvidersPanel.setAutoscrolls(true);

        javax.swing.GroupLayout serviceProvidersPanelLayout = new javax.swing.GroupLayout(serviceProvidersPanel);
        serviceProvidersPanel.setLayout(serviceProvidersPanelLayout);
        serviceProvidersPanelLayout.setHorizontalGroup(
            serviceProvidersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 708, Short.MAX_VALUE)
        );
        serviceProvidersPanelLayout.setVerticalGroup(
            serviceProvidersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 90, Short.MAX_VALUE)
        );

        lifeTimeLabel.setText(org.openide.util.NbBundle.getMessage(STSConfigServicePanel.class, "LBL_STSConfig_Lifetime")); // NOI18N

        contractLabel.setText(org.openide.util.NbBundle.getMessage(STSConfigServicePanel.class, "LBL_STSConfig_Contract")); // NOI18N

        contractTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                contractTextFieldKeyReleased(evt);
            }
        });

        contractButton.setText(org.openide.util.NbBundle.getMessage(STSConfigServicePanel.class, "LBL_STSConfig_Browse")); // NOI18N
        contractButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                contractButtonActionPerformed(evt);
            }
        });

        issuerLabel.setText(org.openide.util.NbBundle.getMessage(STSConfigServicePanel.class, "LBL_STSConfig_Issuer")); // NOI18N

        issuerField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                issuerFieldKeyReleased(evt);
            }
        });

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/websvc/wsitconf/ui/service/subpanels/Bundle"); // NOI18N
        encryptKeyChBox.setText(bundle.getString("LBL_STSConfig_EncryptKey")); // NOI18N
        encryptKeyChBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        encryptKeyChBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        encryptKeyChBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                encryptKeyChBoxActionPerformed(evt);
            }
        });

        encryptTokenChBox.setText(bundle.getString("LBL_STSConfig_EncryptToken")); // NOI18N
        encryptTokenChBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        encryptTokenChBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        encryptTokenChBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                encryptTokenChBoxActionPerformed(evt);
            }
        });

        lifeTimeTextField.setFormatterFactory(lifeTimeDff);
        lifeTimeTextField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                lifeTimeTextFieldKeyReleased(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(serviceProvidersPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(2, 2, 2)
                                .addComponent(lifeTimeLabel))
                            .addComponent(contractLabel)
                            .addComponent(issuerLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(issuerField, javax.swing.GroupLayout.DEFAULT_SIZE, 458, Short.MAX_VALUE)
                            .addComponent(lifeTimeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(contractTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 353, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(contractButton)))
                        .addGap(10, 10, 10))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(encryptKeyChBox)
                        .addContainerGap(554, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(encryptTokenChBox)
                        .addContainerGap(535, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(issuerLabel)
                    .addComponent(issuerField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(contractLabel)
                    .addComponent(contractButton)
                    .addComponent(contractTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(8, 8, 8)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lifeTimeLabel)
                    .addComponent(lifeTimeTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(encryptKeyChBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(encryptTokenChBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(serviceProvidersPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void lifeTimeTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_lifeTimeTextFieldKeyReleased
        Object o = lifeTimeTextField.getValue();
        if (o instanceof Integer) {
            String ltime = o.toString();
            ProprietarySecurityPolicyModelHelper.setSTSLifeTime(binding, ltime);
        }
}//GEN-LAST:event_lifeTimeTextFieldKeyReleased

    private void encryptTokenChBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_encryptTokenChBoxActionPerformed
        ProprietarySecurityPolicyModelHelper.setSTSEncryptToken(binding, encryptTokenChBox.isSelected());
    }//GEN-LAST:event_encryptTokenChBoxActionPerformed

    private void encryptKeyChBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_encryptKeyChBoxActionPerformed
        ProprietarySecurityPolicyModelHelper.setSTSEncryptKey(binding, encryptKeyChBox.isSelected());
    }//GEN-LAST:event_encryptKeyChBoxActionPerformed

    private void contractTextFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_contractTextFieldKeyReleased
        String c = contractTextField.getText();
        ProprietarySecurityPolicyModelHelper.setSTSContractClass(binding, c);
    }//GEN-LAST:event_contractTextFieldKeyReleased

    private void issuerFieldKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_issuerFieldKeyReleased
        String issuer = issuerField.getText();
        ProprietarySecurityPolicyModelHelper.setSTSIssuer(binding, issuer);
    }//GEN-LAST:event_issuerFieldKeyReleased

    private void contractButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_contractButtonActionPerformed
        if (project != null) {
            ClassDialog classDialog = new ClassDialog(project, "com.sun.xml.ws.trust.WSTrustContract"); //NOI18N
            classDialog.show();
            if (classDialog.okButtonPressed()) {
                Set<String> selectedClasses = classDialog.getSelectedClasses();
                for (String selectedClass : selectedClasses) {
                    setContractClass(selectedClass);
                    ProprietarySecurityPolicyModelHelper.setSTSContractClass(binding, selectedClass);
                    break;
                }
            }
        }
    }//GEN-LAST:event_contractButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton contractButton;
    private javax.swing.JLabel contractLabel;
    private javax.swing.JTextField contractTextField;
    private javax.swing.JCheckBox encryptKeyChBox;
    private javax.swing.JCheckBox encryptTokenChBox;
    private javax.swing.JTextField issuerField;
    private javax.swing.JLabel issuerLabel;
    private javax.swing.JLabel lifeTimeLabel;
    private javax.swing.JFormattedTextField lifeTimeTextField;
    private javax.swing.JPanel serviceProvidersPanel;
    // End of variables declaration//GEN-END:variables
    
}
