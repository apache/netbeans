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
package org.netbeans.modules.cloud.oracle.actions;

import java.awt.Dialog;
import java.awt.GraphicsEnvironment;
import java.util.Optional;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.cloud.oracle.compartment.CompartmentItem;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.Pair;

/**
 *
 * @author Jan Horvath
 */
@NbBundle.Messages({
    "CreateDB=Create AutonomousDatabase",
    "DatabaseName=Database Name",
    "AdminPassword=Provide password for database user ADMIN",
    "ConfirmAdminPassword=Confirm password for database user ADMIN"
})
public class CreateAutonomousDBDialog extends AbstractPasswordPanel {

    private DialogDescriptor descriptor;

    /**
     * Creates new form CreateAutonomousDBDialog
     */
    public CreateAutonomousDBDialog() {
        initComponents();
        DocumentListener docListener = new PasswordListener();
        jPasswordField1.getDocument().addDocumentListener(docListener);
        jPasswordField2.getDocument().addDocumentListener(docListener);
    }

    static Optional<Pair<String, char[]>> showDialog(CompartmentItem compartment) {
        if (!GraphicsEnvironment.isHeadless()) {
            CreateAutonomousDBDialog dlgPanel = new CreateAutonomousDBDialog();
            DialogDescriptor descriptor = new DialogDescriptor(dlgPanel, Bundle.CreateDB()); //NOI18N
            dlgPanel.setDescriptor(descriptor);
            descriptor.createNotificationLineSupport();
            Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
            dialog.setMinimumSize(dlgPanel.getPreferredSize());
            dialog.setVisible(true);
            if (DialogDescriptor.OK_OPTION == descriptor.getValue()) {
                String dbName = dlgPanel.jTextField1.getText();
                char[] passwd = dlgPanel.jPasswordField1.getPassword();
                return Optional.of(Pair.of(dbName, passwd));
            }
        } else {
            NotifyDescriptor.InputLine inp = new NotifyDescriptor.InputLine(Bundle.DatabaseName(), Bundle.DatabaseName());
            Object selected = DialogDisplayer.getDefault().notify(inp);
            if (DialogDescriptor.OK_OPTION != selected) {
                return Optional.empty();
            }
            String dbName = inp.getInputText();

            inp = new NotifyDescriptor.PasswordLine(Bundle.AdminPassword(), Bundle.AdminPassword());
            selected = DialogDisplayer.getDefault().notify(inp);
            if (DialogDescriptor.OK_OPTION != selected) {
                return Optional.empty();
            }

            String password1 = inp.getInputText();
            if (!checkPasswordLogic(password1.toCharArray(), password1.toCharArray(),
                    (m) -> DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message(m)))) {
                return Optional.empty();
            }

            inp = new NotifyDescriptor.PasswordLine(Bundle.ConfirmAdminPassword(), Bundle.ConfirmAdminPassword());
            selected = DialogDisplayer.getDefault().notify(inp);
            if (DialogDescriptor.OK_OPTION != selected) {
                return Optional.empty();
            }

            String password2 = inp.getInputText();
            if (!checkPasswordLogic(password1.toCharArray(), password2.toCharArray(),
                    (m) -> DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message(m)))) {
                return Optional.empty();
            }
            return Optional.of(Pair.of(dbName, password1.toCharArray()));
        }
        return Optional.empty();
    }

    private void showError(String msg) {
        DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message(msg));
    }

    @Override
    protected void checkPassword() {
        char[] passwd1 = jPasswordField1.getPassword();
        char[] passwd2 = jPasswordField2.getPassword();
        checkPasswordLogic(passwd1, passwd2, (m) -> errorMessage(m));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jPasswordField1 = new javax.swing.JPasswordField();
        jLabel3 = new javax.swing.JLabel();
        jPasswordField2 = new javax.swing.JPasswordField();
        jTextArea1 = new javax.swing.JTextArea();
        jLabel4 = new javax.swing.JLabel();

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(CreateAutonomousDBDialog.class, "CreateAutonomousDBDialog.jLabel1.text")); // NOI18N

        jTextField1.setText(org.openide.util.NbBundle.getMessage(CreateAutonomousDBDialog.class, "CreateAutonomousDBDialog.jTextField1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(CreateAutonomousDBDialog.class, "CreateAutonomousDBDialog.jLabel2.text")); // NOI18N

        jPasswordField1.setText(org.openide.util.NbBundle.getMessage(CreateAutonomousDBDialog.class, "CreateAutonomousDBDialog.jPasswordField1.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(CreateAutonomousDBDialog.class, "CreateAutonomousDBDialog.jLabel3.text")); // NOI18N

        jPasswordField2.setText(org.openide.util.NbBundle.getMessage(CreateAutonomousDBDialog.class, "CreateAutonomousDBDialog.jPasswordField2.text")); // NOI18N

        jTextArea1.setEditable(false);
        jTextArea1.setColumns(20);
        jTextArea1.setLineWrap(true);
        jTextArea1.setRows(5);
        jTextArea1.setText(org.openide.util.NbBundle.getMessage(CreateAutonomousDBDialog.class, "CreateAutonomousDBDialog.jTextArea1.text")); // NOI18N
        jTextArea1.setAutoscrolls(false);
        jTextArea1.setBorder(null);
        jTextArea1.setDragEnabled(false);
        jTextArea1.setFocusTraversalKeysEnabled(false);
        jTextArea1.setFocusable(false);
        jTextArea1.setOpaque(false);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, org.openide.util.NbBundle.getMessage(CreateAutonomousDBDialog.class, "CreateAutonomousDBDialog.jLabel4.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel1)
                            .addComponent(jLabel3)
                            .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextField1)
                            .addComponent(jPasswordField1)
                            .addComponent(jPasswordField2)))
                    .addComponent(jTextArea1))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTextArea1, javax.swing.GroupLayout.PREFERRED_SIZE, 18, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jPasswordField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jPasswordField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JPasswordField jPasswordField1;
    private javax.swing.JPasswordField jPasswordField2;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables

}
