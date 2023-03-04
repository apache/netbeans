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

package org.netbeans.modules.j2ee.ddloaders.web.multiview;

import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.DialogDescriptor;
import org.openide.NotificationLineSupport;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * @author Petr Slechta
 */
class OrderingItemPanel extends JPanel {

    public static final String OTHERS = "<others>"; // NOI18N
    private DialogDescriptor desc;

    public OrderingItemPanel(String item) {
        initComponents();
        if (item != null && !item.equals(OTHERS)) {
            tfNameRef.setText(item);
            rbName.setSelected(true);
        }
        else {
            tfNameRef.setEnabled(false);
            rbOthers.setSelected(true);
        }
        tfNameRef.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
                check();
            }
            public void insertUpdate(DocumentEvent e) {
                check();
            }
            public void removeUpdate(DocumentEvent e) {
                check();
            }
        });
    }

    void setDlgSupport(DialogDescriptor desc) {
        this.desc = desc;
    }

    private void check() {
        if (desc == null)
            return;

        NotificationLineSupport supp = desc.getNotificationLineSupport();
        if (rbName.isSelected()) {
            String s = tfNameRef.getText();
            if (s == null || s.length() < 1) {
                supp.setInformationMessage(NbBundle.getMessage(OrderingItemPanel.class, "ERR_NO_NAME"));
                desc.setValid(false);
                return;
            }
            if (!Utilities.isJavaIdentifier(s)) {
                supp.setErrorMessage(NbBundle.getMessage(OrderingItemPanel.class, "ERR_WRONG_NAME"));
                desc.setValid(false);
                return;
            }
        }
        supp.clearMessages();
        desc.setValid(true);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        tfNameRef = new javax.swing.JTextField();
        rbName = new javax.swing.JRadioButton();
        rbOthers = new javax.swing.JRadioButton();

        setMinimumSize(new java.awt.Dimension(250, 150));

        tfNameRef.setColumns(20);

        buttonGroup1.add(rbName);
        org.openide.awt.Mnemonics.setLocalizedText(rbName, org.openide.util.NbBundle.getMessage(OrderingItemPanel.class, "RB_Name")); // NOI18N
        rbName.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                rbNameStateChanged(evt);
            }
        });

        buttonGroup1.add(rbOthers);
        org.openide.awt.Mnemonics.setLocalizedText(rbOthers, org.openide.util.NbBundle.getMessage(OrderingItemPanel.class, "RB_Others")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(rbName)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(tfNameRef, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(rbOthers))
                .addContainerGap(113, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rbName)
                    .addComponent(tfNameRef, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(rbOthers)
                .addContainerGap(87, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void rbNameStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_rbNameStateChanged
        tfNameRef.setEnabled(rbName.isSelected());
        check();
    }//GEN-LAST:event_rbNameStateChanged

    public String getResult() {
        if (rbName.isSelected()) {
            String res = tfNameRef.getText().trim();
            return res.length() < 1 ? null : res;
        }
        else
            return OTHERS;
    }

    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JRadioButton rbName;
    private javax.swing.JRadioButton rbOthers;
    private javax.swing.JTextField tfNameRef;
    // End of variables declaration//GEN-END:variables
 
}
