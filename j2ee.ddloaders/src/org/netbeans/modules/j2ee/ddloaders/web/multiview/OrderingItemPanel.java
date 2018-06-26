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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
