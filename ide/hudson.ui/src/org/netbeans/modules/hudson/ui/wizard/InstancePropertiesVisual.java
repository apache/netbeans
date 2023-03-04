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

package org.netbeans.modules.hudson.ui.wizard;

import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.modules.hudson.api.HudsonManager;
import org.openide.NotificationLineSupport;
import org.openide.util.NbBundle;

class InstancePropertiesVisual extends JPanel {
    
    public InstancePropertiesVisual() {
        initComponents();
        DocumentListener l = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                check();
            }
            public void removeUpdate(DocumentEvent e) {
                check();
            }
            public void changedUpdate(DocumentEvent e) {}
        };
        nameTxt.getDocument().addDocumentListener(l);
        urlTxt.getDocument().addDocumentListener(l);
        checkProgress.setVisible(false);
    }

    private NotificationLineSupport msgs;
    private JButton addButton;

    void init(NotificationLineSupport msgs, JButton addButton) {
        assert msgs != null;
        this.msgs = msgs;
        this.addButton = addButton;
        check();
    }

    void showChecking() {
        checkProgress.setVisible(true);
        nameTxt.setEnabled(false);
        urlTxt.setEnabled(false);
        autoSyncCheckBox.setEnabled(false);
        autoSyncSpinner.setEnabled(false);
        proxyButton.setEnabled(false);
    }

    void checkFailed(String explanation) {
        msgs.setErrorMessage(explanation);
        checkProgress.setVisible(false);
        nameTxt.setEnabled(true);
        urlTxt.setEnabled(true);
        autoSyncCheckBox.setEnabled(true);
        autoSyncSpinner.setEnabled(autoSyncCheckBox.isSelected());
        proxyButton.setEnabled(true);
        urlTxt.requestFocusInWindow();
    }

    String getDisplayName() {
        return nameTxt.getText().trim();
    }
    
    String getUrl() {
        return urlTxt.getText().trim();
    }
    
    int getSyncTime() {
        return autoSyncCheckBox.isSelected() ? (Integer) autoSyncSpinner.getValue() : 0;
    }
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        nameLabel = new javax.swing.JLabel();
        nameTxt = new javax.swing.JTextField();
        urlLabel = new javax.swing.JLabel();
        urlTxt = new javax.swing.JTextField();
        autoSyncCheckBox = new javax.swing.JCheckBox();
        autoSyncSpinner = new javax.swing.JSpinner();
        autoSyncLabel = new javax.swing.JLabel();
        proxyButton = new javax.swing.JButton();
        checkProgress = new javax.swing.JProgressBar();

        nameLabel.setLabelFor(nameTxt);
        org.openide.awt.Mnemonics.setLocalizedText(nameLabel, org.openide.util.NbBundle.getMessage(InstancePropertiesVisual.class, "LBL_Name")); // NOI18N

        urlLabel.setLabelFor(urlTxt);
        org.openide.awt.Mnemonics.setLocalizedText(urlLabel, org.openide.util.NbBundle.getMessage(InstancePropertiesVisual.class, "LBL_Url")); // NOI18N

        urlTxt.setText(org.openide.util.NbBundle.getMessage(InstancePropertiesVisual.class, "InstancePropertiesVisual.urlTxt.text")); // NOI18N

        autoSyncCheckBox.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(autoSyncCheckBox, org.openide.util.NbBundle.getMessage(InstancePropertiesVisual.class, "LBL_AutoSync")); // NOI18N
        autoSyncCheckBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        autoSyncCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                autoSyncCheckBoxActionPerformed(evt);
            }
        });

        autoSyncSpinner.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(5), Integer.valueOf(1), null, Integer.valueOf(1)));
        autoSyncSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                autoSyncSpinnerStateChanged(evt);
            }
        });

        autoSyncLabel.setLabelFor(autoSyncSpinner);
        org.openide.awt.Mnemonics.setLocalizedText(autoSyncLabel, org.openide.util.NbBundle.getMessage(InstancePropertiesVisual.class, "LBL_AutoSyncMinutes")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(proxyButton, org.openide.util.NbBundle.getMessage(InstancePropertiesVisual.class, "LBL_Proxy")); // NOI18N
        proxyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                proxyButtonActionPerformed(evt);
            }
        });

        checkProgress.setIndeterminate(true);
        checkProgress.setString(org.openide.util.NbBundle.getMessage(InstancePropertiesVisual.class, "InstancePropertiesVisual.checkProgress.string")); // NOI18N
        checkProgress.setStringPainted(true);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(checkProgress, javax.swing.GroupLayout.DEFAULT_SIZE, 439, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(autoSyncCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(autoSyncSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(autoSyncLabel))
                    .addComponent(proxyButton)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(nameLabel)
                            .addComponent(urlLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(urlTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 382, Short.MAX_VALUE)
                            .addComponent(nameTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 382, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nameLabel)
                    .addComponent(nameTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(urlLabel)
                    .addComponent(urlTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(autoSyncCheckBox)
                    .addComponent(autoSyncSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(autoSyncLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(proxyButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(checkProgress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        nameTxt.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InstancePropertiesVisual.class, "InstancePropertiesVisual.nameTxt.AccessibleContext.accessibleDescription")); // NOI18N
        urlTxt.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InstancePropertiesVisual.class, "InstancePropertiesVisual.urlTxt.AccessibleContext.accessibleDescription")); // NOI18N
        autoSyncCheckBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InstancePropertiesVisual.class, "InstancePropertiesVisual.autoSyncCheckBox.AccessibleContext.accessibleDescription")); // NOI18N
        autoSyncSpinner.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InstancePropertiesVisual.class, "InstancePropertiesVisual.autoSyncSpinner.AccessibleContext.accessibleDescription")); // NOI18N
        proxyButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InstancePropertiesVisual.class, "InstancePropertiesVisual.proxyButton.AccessibleContext.accessibleDescription")); // NOI18N

        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(InstancePropertiesVisual.class, "InstancePropertiesVisual.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
    
private void proxyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_proxyButtonActionPerformed
    OptionsDisplayer.getDefault().open("General"); // NOI18N
}//GEN-LAST:event_proxyButtonActionPerformed

private void autoSyncSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_autoSyncSpinnerStateChanged
    check();
}//GEN-LAST:event_autoSyncSpinnerStateChanged

private void autoSyncCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autoSyncCheckBoxActionPerformed
    autoSyncSpinner.setEnabled(autoSyncCheckBox.isSelected());
    check();
}//GEN-LAST:event_autoSyncCheckBoxActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox autoSyncCheckBox;
    private javax.swing.JLabel autoSyncLabel;
    private javax.swing.JSpinner autoSyncSpinner;
    private javax.swing.JProgressBar checkProgress;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameTxt;
    private javax.swing.JButton proxyButton;
    private javax.swing.JLabel urlLabel;
    private javax.swing.JTextField urlTxt;
    // End of variables declaration//GEN-END:variables
    
    private void check() {
        addButton.setEnabled(false);
        String name = getDisplayName();
        String url = getUrl();
        if (name.length() == 0) {
            msgs.setInformationMessage(NbBundle.getMessage(InstanceDialog.class, "MSG_EmptyName"));
            return;
        }
        if (HudsonManager.getInstanceByName(name) != null) {
            msgs.setErrorMessage(NbBundle.getMessage(InstanceDialog.class, "MSG_ExistName"));
            return;
        }
        if (url.length() == 0 || url.endsWith("//")) {
            msgs.setInformationMessage(NbBundle.getMessage(InstanceDialog.class, "MSG_EmptyUrl"));
            return;
        }
        if (!url.endsWith("/")) { // NOI18N
            msgs.setInformationMessage(NbBundle.getMessage(InstancePropertiesVisual.class, "InstanceDialog.end_with_slash"));
            return;
        }
        try {
            URL u = new URL(url);
            if (!u.getProtocol().matches("https?")) { // NOI18N
                msgs.setErrorMessage(NbBundle.getMessage(InstancePropertiesVisual.class, "InstanceDialog.http_protocol"));
                return;
            }
        } catch (MalformedURLException x) {
            msgs.setErrorMessage(x.getLocalizedMessage());
            return;
        }
        if (HudsonManager.getInstance(url) != null) {
            msgs.setErrorMessage(NbBundle.getMessage(InstanceDialog.class, "MSG_ExistUrl"));
            return;
        }
        msgs.clearMessages();
        addButton.setEnabled(true);
    }

}
