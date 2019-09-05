/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.fish.payara.micro.project.ui;

import org.netbeans.modules.fish.payara.micro.project.VersionRepository;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import static org.netbeans.modules.fish.payara.micro.plugin.Constants.PROP_AUTO_BIND_HTTP;
import static org.netbeans.modules.fish.payara.micro.plugin.Constants.PROP_PAYARA_MICRO_VERSION;
import org.netbeans.modules.fish.payara.micro.project.MicroVersion;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.openide.WizardDescriptor;
import static org.openide.util.NbBundle.getMessage;
import static org.netbeans.modules.fish.payara.micro.plugin.Constants.PROP_CONTEXT_ROOT;

/**
 *
 * @author Gaurav Gupta <gaurav.gupta@payara.fish>
 */
class PayaraMicroPanel extends JPanel {

    PayaraMicroPanel(J2eeModule.Type projectType) {
        initComponents();
        getAccessibleContext().setAccessibleDescription(getName());
    }

    @Override
    public String getName() {
        return getMessage(PayaraMicroPanel.class, "LBL_MavenProjectSettings");
    }

    void readSettings(WizardDescriptor descriptor) {
        String microVersionText = (String) descriptor.getProperty(PROP_PAYARA_MICRO_VERSION);
        if (microVersionText != null) {
            VersionRepository.toMicroVersion(microVersionText)
                    .ifPresent(microVersion -> microVersionCombobox.setSelectedItem(microVersion));
        }
        
        String autoBindHTTP = (String)descriptor.getProperty(PROP_AUTO_BIND_HTTP);
        if(autoBindHTTP == null){
            autoBindHTTP = Boolean.TRUE.toString();
        }
        autoBindHttpCheckBox.setSelected(Boolean.valueOf(autoBindHTTP));

        String contextRoot = (String) descriptor.getProperty(PROP_CONTEXT_ROOT);
        if (contextRoot == null) {
            contextRoot = "/";
        }
        contextRootTextField.setText(contextRoot);
    }

    void storeSettings(WizardDescriptor descriptor) {
        descriptor.putProperty(PROP_PAYARA_MICRO_VERSION, ((MicroVersion)microVersionCombobox.getSelectedItem()).getVersion());
        descriptor.putProperty(PROP_AUTO_BIND_HTTP, String.valueOf(autoBindHttpCheckBox.isSelected()));
        descriptor.putProperty(PROP_CONTEXT_ROOT, String.valueOf(contextRootTextField.getText().trim()));
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        microVersionCombobox = new javax.swing.JComboBox();
        microVersionLabel = new javax.swing.JLabel();
        autoBindHttpLabel = new javax.swing.JLabel();
        autoBindHttpCheckBox = new javax.swing.JCheckBox();
        contextRootLabel = new javax.swing.JLabel();
        contextRootTextField = new javax.swing.JTextField();

        microVersionCombobox.setModel(new DefaultComboBoxModel(VersionRepository.getInstance().getMicroVersion().toArray()));

        org.openide.awt.Mnemonics.setLocalizedText(microVersionLabel, org.openide.util.NbBundle.getMessage(PayaraMicroPanel.class, "PayaraMicroPanel.microVersionLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(autoBindHttpLabel, org.openide.util.NbBundle.getMessage(PayaraMicroPanel.class, "PayaraMicroPanel.autoBindHttpLabel.text")); // NOI18N
        autoBindHttpLabel.setToolTipText(org.openide.util.NbBundle.getMessage(PayaraMicroPanel.class, "TLTP_AUTO_BIND_HTTP")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(autoBindHttpCheckBox, org.openide.util.NbBundle.getMessage(PayaraMicroPanel.class, "PayaraMicroPanel.autoBindHttpCheckBox.text")); // NOI18N
        autoBindHttpCheckBox.setToolTipText(org.openide.util.NbBundle.getMessage(PayaraMicroPanel.class, "TLTP_AUTO_BIND_HTTP")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(contextRootLabel, org.openide.util.NbBundle.getMessage(PayaraMicroPanel.class, "PayaraMicroPanel.contextRootLabel.text")); // NOI18N
        contextRootLabel.setToolTipText(org.openide.util.NbBundle.getMessage(PayaraMicroPanel.class, "PayaraMicroPanel.contextRootLabel.toolTipText")); // NOI18N

        contextRootTextField.setText(org.openide.util.NbBundle.getMessage(PayaraMicroPanel.class, "PayaraMicroPanel.contextRootTextField.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(microVersionLabel)
                    .addComponent(autoBindHttpLabel)
                    .addComponent(contextRootLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(autoBindHttpCheckBox)
                        .addGap(0, 91, Short.MAX_VALUE))
                    .addComponent(microVersionCombobox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(contextRootTextField, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(microVersionLabel)
                    .addComponent(microVersionCombobox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(autoBindHttpCheckBox)
                    .addComponent(autoBindHttpLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(contextRootLabel)
                    .addComponent(contextRootTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(217, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox autoBindHttpCheckBox;
    private javax.swing.JLabel autoBindHttpLabel;
    private javax.swing.JLabel contextRootLabel;
    private javax.swing.JTextField contextRootTextField;
    private javax.swing.JComboBox microVersionCombobox;
    private javax.swing.JLabel microVersionLabel;
    // End of variables declaration//GEN-END:variables

}
