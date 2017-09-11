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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2010 Sun
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

package org.netbeans.modules.derby.ui;

import java.io.File;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.derby.DerbyOptions;
import org.netbeans.modules.derby.api.DerbyDatabases;
import org.openide.DialogDescriptor;
import org.openide.util.NbBundle;

public class CreateSampleDatabasePanel extends javax.swing.JPanel {

    private File derbySystemHome;
    private DialogDescriptor descriptor;

    private DocumentListener docListener = new DocumentListener() {
        
        @Override
        public void removeUpdate(javax.swing.event.DocumentEvent e) {
            validateInput();
        }

        @Override
        public void insertUpdate(javax.swing.event.DocumentEvent e) {
            validateInput();
        }

        @Override
        public void changedUpdate(javax.swing.event.DocumentEvent e) {
            validateInput();
        }
    };
    
    public CreateSampleDatabasePanel(String derbySystemHome) {
        this.derbySystemHome = new File(derbySystemHome);
        initComponents();
        databaseNameTextField.getDocument().addDocumentListener(docListener);
        updateLocation();
    }
    
    public void setDialogDescriptor(DialogDescriptor descriptor) {
        this.descriptor = descriptor;
        validateInput();
    }

    public String getDatabaseName() {
        return databaseNameTextField.getText().trim();
    }

    public void setIntroduction() {
        String info = NbBundle.getMessage(CreateSampleDatabasePanel.class, "INFO_DatabaseNameEmpty");
        descriptor.getNotificationLineSupport().setInformationMessage(info);
        descriptor.setValid(false);
    }

    private void validateInput() {
        if (descriptor == null) {
            return;
        }
        
        String error = null;
        String warning = null;
        String info = null;
        
        String databaseName = getDatabaseName();
        int illegalChar = DerbyDatabases.getFirstIllegalCharacter(databaseName);
        // workaround for issue 69265
        int unsupportedChar = getFirstUnsupportedCharacter(databaseName);
        
        if (databaseName.length() <= 0) { // NOI18N
            warning = NbBundle.getMessage(CreateSampleDatabasePanel.class, "ERR_DatabaseNameEmpty");
        } else if (illegalChar >= 0) {
            error = NbBundle.getMessage(CreateSampleDatabasePanel.class, "ERR_DatabaseNameIllegalChar", new Character((char)illegalChar));
        } else if (databaseName.length() > 0 && new File(derbySystemHome, databaseName).exists()) { // NOI18N
            error = NbBundle.getMessage(CreateSampleDatabasePanel.class, "ERR_DatabaseDirectoryExists", databaseName);
        } else if (unsupportedChar >= 0) {
            error = NbBundle.getMessage(CreateSampleDatabasePanel.class, "ERR_DatabaseNameUnsupportedChar", new Character((char)unsupportedChar));
        }
        
        if (error != null) {
            descriptor.getNotificationLineSupport().setErrorMessage(error);
            descriptor.setValid(false);
        } else if (warning != null) {
            descriptor.getNotificationLineSupport().setWarningMessage(warning);
            descriptor.setValid(false);
        } else if (info != null) {
            descriptor.getNotificationLineSupport().setInformationMessage(info);
            descriptor.setValid(true);
        } else {
            descriptor.getNotificationLineSupport().clearMessages();
            descriptor.setValid(true);
        }
    }
    
    static boolean isSql92Identifier(String s) {
        if (s == null || s.isEmpty()) {
            // empty value
            return false;
        } else if (s.matches("[\\w&&[^\\d_]](_|\\d|\\w)*")) { //NOI18N
            // ordinary identifier
            return true;
        } else if (s.matches("\\\".+\\\"")) { //NOI18N
            boolean unpairedQuite = false;
            // check all quotes are doubled
            for (int i = 1; i < s.length() - 1; i++) {
                if (s.charAt(i) == '"') {
                    unpairedQuite = !unpairedQuite;
                } else if (unpairedQuite) {
                    // invalid delemited identifier
                    return false;
                }
            }
            // delemited identifier
            return !unpairedQuite;
        } else {
            // other cases, not a identifier
            return false;
        }
    }

    private void updateLocation() {
        databaseLocationValueLabel.setText(derbySystemHome.getAbsolutePath());
    }
    
    private int getFirstUnsupportedCharacter(String databaseName) {
        for (int i = 0; i < databaseName.length(); i++) {
            char ch = databaseName.charAt(i);
            if (ch < '\u0020' || ch > '\u00ff') {
                return (int)ch;
            }
        }
        return -1;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        databaseNameLabel = new javax.swing.JLabel();
        databaseNameTextField = new javax.swing.JTextField();
        databaseLocationLabel = new javax.swing.JLabel();
        propertiesButton = new javax.swing.JButton();
        databaseLocationValueLabel = new javax.swing.JLabel();

        databaseNameLabel.setLabelFor(databaseNameTextField);
        org.openide.awt.Mnemonics.setLocalizedText(databaseNameLabel, org.openide.util.NbBundle.getMessage(CreateSampleDatabasePanel.class, "LBL_DatabaseName")); // NOI18N

        databaseLocationLabel.setLabelFor(databaseLocationValueLabel);
        org.openide.awt.Mnemonics.setLocalizedText(databaseLocationLabel, org.openide.util.NbBundle.getMessage(CreateSampleDatabasePanel.class, "LBL_DatabaseLocation")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(propertiesButton, org.openide.util.NbBundle.getMessage(CreateSampleDatabasePanel.class, "LBL_Properties")); // NOI18N
        propertiesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                propertiesButtonActionPerformed(evt);
            }
        });

        databaseLocationValueLabel.setToolTipText(derbySystemHome.getAbsolutePath());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(databaseNameLabel)
                    .addComponent(databaseLocationLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(databaseNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 346, Short.MAX_VALUE)
                    .addComponent(databaseLocationValueLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(propertiesButton)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(databaseNameLabel)
                    .addComponent(databaseNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(databaseLocationValueLabel)
                    .addComponent(databaseLocationLabel)
                    .addComponent(propertiesButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        databaseNameTextField.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CreateSampleDatabasePanel.class, "ACSD_CreateDatabasePanel_databaseNameTextField")); // NOI18N
        propertiesButton.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CreateSampleDatabasePanel.class, "ACSN_CreateDatabasePanel_propertiesButton")); // NOI18N
        propertiesButton.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CreateSampleDatabasePanel.class, "ACSD_CreateDatabasePanel_propertiesButton")); // NOI18N
        databaseLocationValueLabel.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(CreateSampleDatabasePanel.class, "ACSN_CreateDatabasePanel_databaseLocationValueLabel")); // NOI18N
        databaseLocationValueLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(CreateSampleDatabasePanel.class, "ACSD_CreateDatabasePanel_databaseLocationValueLabel")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents

    private void propertiesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_propertiesButtonActionPerformed
        DerbyPropertiesPanel.showDerbyProperties();
        String newLocation = DerbyOptions.getDefault().getSystemHome();
        databaseLocationValueLabel.setText(newLocation);
        databaseLocationValueLabel.setToolTipText(newLocation);
}//GEN-LAST:event_propertiesButtonActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    public javax.swing.JLabel databaseLocationLabel;
    public javax.swing.JLabel databaseLocationValueLabel;
    public javax.swing.JLabel databaseNameLabel;
    public javax.swing.JTextField databaseNameTextField;
    public javax.swing.JButton propertiesButton;
    // End of variables declaration//GEN-END:variables
    
}
