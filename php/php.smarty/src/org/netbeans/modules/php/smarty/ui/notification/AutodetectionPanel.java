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

package org.netbeans.modules.php.smarty.ui.notification;

import java.awt.Cursor;
import java.beans.PropertyChangeEvent;
import java.util.prefs.Preferences;
import javax.swing.JPanel;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.smarty.SmartyPhpFrameworkProvider;
import static org.netbeans.modules.php.smarty.SmartyPhpFrameworkProvider.PROP_SMARTY_AVAILABLE;
import org.openide.util.NbBundle;

public class AutodetectionPanel extends JPanel {

    private final PhpModule phpModule;

    @NbBundle.Messages({
        "AutodetectionPanel.msg.autodetect=Smarty templates detected in project {0}. Click to enable its support.",
        "AutodetectionPanel.lbl.ignore=Do not show this again"
    })
    public AutodetectionPanel(PhpModule phpModule) {
        this.phpModule = phpModule;
        // UI roughly copied from org.netbeans.core.ui.notifications.NotificationDisplayerImpl
        initComponents();
        descriptionLabel.setText("<html><a href=\"#\">" + Bundle.AutodetectionPanel_msg_autodetect(phpModule.getDisplayName())); //NOI18N
        descriptionLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        ignoreLabel.setText("<html><a href=\"#\">" + Bundle.AutodetectionPanel_lbl_ignore()); //NOI18N
        ignoreLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        descriptionLabel = new javax.swing.JButton();
        ignoreLabel = new javax.swing.JButton();

        setOpaque(false);

        descriptionLabel.setText("<html><u>There was found ..."); // NOI18N
        descriptionLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        descriptionLabel.setBorderPainted(false);
        descriptionLabel.setContentAreaFilled(false);
        descriptionLabel.setFocusPainted(false);
        descriptionLabel.setFocusable(false);
        descriptionLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        descriptionLabel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                descriptionLabelActionPerformed(evt);
            }
        });

        ignoreLabel.setText(org.openide.util.NbBundle.getMessage(AutodetectionPanel.class, "AutodetectionPanel.ignoreLabel.text")); // NOI18N
        ignoreLabel.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        ignoreLabel.setBorderPainted(false);
        ignoreLabel.setContentAreaFilled(false);
        ignoreLabel.setFocusPainted(false);
        ignoreLabel.setFocusable(false);
        ignoreLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        ignoreLabel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ignoreLabelActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(descriptionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(ignoreLabel)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(descriptionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ignoreLabel))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void descriptionLabelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_descriptionLabelActionPerformed
        Preferences preferences = phpModule.getPreferences(SmartyPhpFrameworkProvider.class, true);
        preferences.putBoolean(PROP_SMARTY_AVAILABLE, true);
        phpModule.notifyPropertyChanged(new PropertyChangeEvent(this, PhpModule.PROPERTY_FRAMEWORKS, null, null));
    }//GEN-LAST:event_descriptionLabelActionPerformed

    private void ignoreLabelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ignoreLabelActionPerformed
        Preferences preferences = phpModule.getPreferences(SmartyPhpFrameworkProvider.class, true);
        preferences.putBoolean(PROP_SMARTY_AVAILABLE, false);
    }//GEN-LAST:event_ignoreLabelActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton descriptionLabel;
    private javax.swing.JButton ignoreLabel;
    // End of variables declaration//GEN-END:variables

}
