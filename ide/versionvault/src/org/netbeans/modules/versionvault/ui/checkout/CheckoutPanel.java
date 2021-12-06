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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

/*
 * Copyright 2021 HCL America, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package org.netbeans.modules.versionvault.ui.checkout;

import org.netbeans.modules.versioning.util.Utils;
import org.netbeans.modules.versioning.util.StringSelector;
import org.netbeans.modules.versionvault.ClearcaseModuleConfig;
import org.openide.util.NbBundle;

import java.awt.Cursor;
import java.util.*;

/**
 * Checkout confirmation panel.
 *
 * @author Maros Sandor
 */
class CheckoutPanel extends javax.swing.JPanel {
        
    private static final String CHECKOUT_RESERVED = "checkout.reserved";
    private static final String CHECKOUT_UNRESERVED_NON_MASTER = "checkout.unreserved.nonmaster";
    private static final String CHECKOUT_RECURSIVE = "checkout.recursive";

    private boolean nonMaster;

    /** Creates new form CheckoutPanel */
    public CheckoutPanel() {
        initComponents();
    }

    public void addNotify() {
        super.addNotify();
        cbReserved.setSelected(ClearcaseModuleConfig.getPreferences().getBoolean(CHECKOUT_RESERVED, true));        
        cbRecursive.setSelected(ClearcaseModuleConfig.getPreferences().getBoolean(CHECKOUT_RECURSIVE, false));
        nonMaster = ClearcaseModuleConfig.getPreferences().getBoolean(CHECKOUT_UNRESERVED_NON_MASTER, false);
        cbNonMaster.setSelected(nonMaster);
        setNonMasterEnabled();
        bRecentMessages.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        List<String> messages = Utils.getStringList(ClearcaseModuleConfig.getPreferences(), CheckoutAction.RECENT_CHECKOUT_MESSAGES);
        if (messages.size() > 0) {
            taMessage.setText(messages.get(0));
        }
        taMessage.selectAll();
        taMessage.requestFocus();
    }

    public void removeNotify() {
        ClearcaseModuleConfig.getPreferences().putBoolean(CHECKOUT_RESERVED, cbReserved.isSelected());
        ClearcaseModuleConfig.getPreferences().putBoolean(CHECKOUT_RECURSIVE, cbRecursive.isSelected());
        ClearcaseModuleConfig.getPreferences().putBoolean(CHECKOUT_UNRESERVED_NON_MASTER, cbNonMaster.isSelected());
        super.removeNotify();
    }

    private void setNonMasterEnabled() {
        if(cbReserved.isSelected()) {
            cbNonMaster.setEnabled(false);
            nonMaster = cbNonMaster.isSelected();
            cbNonMaster.setSelected(false);
        } else {
            cbNonMaster.setEnabled(true);
            cbNonMaster.setSelected(nonMaster);
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        taMessage = new javax.swing.JTextArea();
        cbReserved = new javax.swing.JCheckBox();
        bRecentMessages = new javax.swing.JButton();
        cbNonMaster = new javax.swing.JCheckBox();

        jLabel1.setLabelFor(taMessage);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(CheckoutPanel.class, "CheckoutPanel.jLabel1.text")); // NOI18N

        taMessage.setColumns(20);
        taMessage.setRows(5);
        taMessage.setToolTipText(org.openide.util.NbBundle.getMessage(CheckoutPanel.class, "CheckoutPanel.taMessage.toolTipText")); // NOI18N
        taMessage.setWrapStyleWord(true);
        jScrollPane1.setViewportView(taMessage);

        org.openide.awt.Mnemonics.setLocalizedText(cbReserved, org.openide.util.NbBundle.getMessage(CheckoutPanel.class, "CheckoutPanel.cbReserved.text")); // NOI18N
        cbReserved.setToolTipText(org.openide.util.NbBundle.getMessage(CheckoutPanel.class, "CheckoutPanel.cbReserved.toolTipText")); // NOI18N
        cbReserved.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbReservedActionPerformed(evt);
            }
        });

        bRecentMessages.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/versionvault/resources/icons/recent_messages.png"))); // NOI18N
        bRecentMessages.setToolTipText(org.openide.util.NbBundle.getMessage(CheckoutPanel.class, "CheckoutPanel.bRecentMessages.toolTipText")); // NOI18N
        bRecentMessages.setBorderPainted(false);
        bRecentMessages.setIconTextGap(0);
        bRecentMessages.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bRecentMessagesActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(cbRecursive, org.openide.util.NbBundle.getMessage(CheckoutPanel.class, "CheckoutPanel.cbRecursive.text")); // NOI18N
        cbRecursive.setToolTipText(org.openide.util.NbBundle.getMessage(CheckoutPanel.class, "CheckoutPanel.cbRecursive.toolTipText")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(cbNonMaster, org.openide.util.NbBundle.getMessage(CheckoutPanel.class, "CheckoutPanel.cbNonMaster.text")); // NOI18N
        cbNonMaster.setToolTipText(org.openide.util.NbBundle.getMessage(CheckoutPanel.class, "CheckoutPanel.cbNonMaster.toolTipText")); // NOI18N
        cbNonMaster.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cbNonMasterActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(cbRecursive)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 315, Short.MAX_VALUE)
                        .addComponent(bRecentMessages))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 505, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(cbReserved)
                        .addGap(18, 18, 18)
                        .addComponent(cbNonMaster)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jLabel1)
                    .addComponent(bRecentMessages))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 127, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cbReserved)
                    .addComponent(cbNonMaster))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(cbRecursive)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void cbReservedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbReservedActionPerformed
        setNonMasterEnabled();
    }//GEN-LAST:event_cbReservedActionPerformed

    private void bRecentMessagesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bRecentMessagesActionPerformed
        String message = StringSelector.select(NbBundle.getMessage(CheckoutPanel.class, "CTL_RecentMessages_Prompt"),   // NOI18N
                                               NbBundle.getMessage(CheckoutPanel.class, "CTL_RecentMessages_Title"),   // NOI18N
            Utils.getStringList(ClearcaseModuleConfig.getPreferences(), CheckoutAction.RECENT_CHECKOUT_MESSAGES));
        if (message != null) {
            taMessage.replaceSelection(message);
        }
    }//GEN-LAST:event_bRecentMessagesActionPerformed

    private void cbNonMasterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cbNonMasterActionPerformed
        
    }//GEN-LAST:event_cbNonMasterActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    javax.swing.JButton bRecentMessages;
    javax.swing.JCheckBox cbNonMaster;
    final javax.swing.JCheckBox cbRecursive = new javax.swing.JCheckBox();
    javax.swing.JCheckBox cbReserved;
    javax.swing.JLabel jLabel1;
    javax.swing.JScrollPane jScrollPane1;
    javax.swing.JTextArea taMessage;
    // End of variables declaration//GEN-END:variables

}
