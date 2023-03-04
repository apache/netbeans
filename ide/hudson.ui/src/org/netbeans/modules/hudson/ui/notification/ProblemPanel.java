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

package org.netbeans.modules.hudson.ui.notification;

import java.awt.Cursor;
import javax.swing.JPanel;

class ProblemPanel extends JPanel {

    private final ProblemNotification notification;

    public ProblemPanel(ProblemNotification notification) {
        this.notification = notification;
        // UI roughly copied from org.netbeans.core.ui.notifications.NotificationDisplayerImpl
        // XXX could add links to show changes, etc.
        initComponents();
        showFailure.setText("<html><a href=\"#\">" + notification.showFailureText()); //NOI18N
        // XXX #171445: not available from form editor
        showFailure.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        ignore.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        showFailure = new javax.swing.JButton();
        ignore = new javax.swing.JButton();

        setOpaque(false);

        showFailure.setText("<html><u>Show [something]"); // NOI18N
        showFailure.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        showFailure.setBorderPainted(false);
        showFailure.setContentAreaFilled(false);
        showFailure.setFocusPainted(false);
        showFailure.setFocusable(false);
        showFailure.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        showFailure.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showFailureActionPerformed(evt);
            }
        });

        ignore.setText(org.openide.util.NbBundle.getMessage(ProblemPanel.class, "ProblemPanel.ignore.text")); // NOI18N
        ignore.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        ignore.setBorderPainted(false);
        ignore.setContentAreaFilled(false);
        ignore.setFocusPainted(false);
        ignore.setFocusable(false);
        ignore.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        ignore.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ignoreActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(showFailure, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(ignore, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(showFailure, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ignore, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void showFailureActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showFailureActionPerformed
        notification.showFailure();
    }//GEN-LAST:event_showFailureActionPerformed

    private void ignoreActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ignoreActionPerformed
        notification.ignore();
    }//GEN-LAST:event_ignoreActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton ignore;
    private javax.swing.JButton showFailure;
    // End of variables declaration//GEN-END:variables

}
