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
package org.netbeans.modules.payara.common.ui;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.payara.common.PayaraLogger;
import org.netbeans.modules.payara.common.PayaraInstance;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import static org.openide.NotifyDescriptor.CANCEL_OPTION;
import org.openide.util.NbBundle;

/**
 * Input Payara credentials (username and password).
 * <p/>
 * @author Tomas Kraus
 */
public class PayaraCredentials extends CommonPasswordPanel {

    ////////////////////////////////////////////////////////////////////////////
    // Class attributes                                                       //
    ////////////////////////////////////////////////////////////////////////////

    /** Local logger. */
    private static final Logger LOGGER
            = PayaraLogger.get(PayaraCredentials.class);

    ////////////////////////////////////////////////////////////////////////////
    // Static methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Display Payara credentials panel to get Payara credentials.
     * <p/>
     * User name and password is stored in server instance properties.
     * Properties are persisted.
     * <p/>
     * @param instance Payara server instance.
     * @return Value of <code>true</code> when Payara credentials were
     *         updated of <code>false</code> when <code>Cancel</code> button
     *         was selected.
     */
    public static boolean setCredentials(final PayaraInstance instance,
            final String message) {
        String title = NbBundle.getMessage(
                PayaraPassword.class, "PayaraCredentials.title");
        NotifyDescriptor notifyDescriptor = new NotifyDescriptor(
                null, title, NotifyDescriptor.OK_CANCEL_OPTION,
                NotifyDescriptor.PLAIN_MESSAGE, null, null);
        PayaraCredentials panel
                = new PayaraCredentials(notifyDescriptor, instance, message);
        Object button = DialogDisplayer.getDefault().notify(notifyDescriptor);
        if (button == CANCEL_OPTION) {
            return false;
        }
        String userName = panel.getUserName();
        String password = panel.getPassword();
        instance.setAdminUser(userName);
        instance.setAdminPassword(password);
        try {
            PayaraInstance.writeInstanceToFile(instance);
        } catch (IOException ex) {
            LOGGER.log(Level.INFO,
                    "Could not store Payara server attributes", ex);
        }
        return true;
    }

    /**
     * Display Payara credentials panel to get Payara credentials.
     * <p/>
     * User name and password is stored in server instance properties.
     * Properties are persisted.
     * <p/>
     * @param instance Payara server instance.
     * @return Value of <code>true</code> when Payara credentials were
     *         updated of <code>false</code> when <code>Cancel</code> button
     *         was selected.
     */
    public static boolean setCredentials(final PayaraInstance instance) {
        String message = NbBundle.getMessage(
                PayaraPassword.class,
                "PayaraCredentials.message", instance.getDisplayName());
        return setCredentials(instance, message);
    }

    ////////////////////////////////////////////////////////////////////////////
    // Constructors                                                           //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Creates new Payara credentials panel.
     * <p/>
     * @param descriptor User notification descriptor.
     * @param instance   Payara server instance used to search
     *                   for supported platforms.
     * @param message    Message text.
     */
    @SuppressWarnings("LeakingThisInConstructor")
    public PayaraCredentials(final NotifyDescriptor descriptor,
            final PayaraInstance instance, final String message) {
        super(descriptor, instance, message);
        initComponents();
        initFileds(true);
    }

    ////////////////////////////////////////////////////////////////////////////
    // GUI Getters and Setters                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Get password value from form.
     * <p/>
     * @return Password value from form.
     */
    String getPassword() {
        return new String(password.getPassword());
    }

    /**
     * Get user name value from form.
     * <p/>
     * @return User name value from form.
     */
    String getUserName() {
        return userText.getText();
    }

    ////////////////////////////////////////////////////////////////////////////
    // Methods                                                                //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Clear form fields.
     */
    public void clear() {
        this.password.setText("");
        this.userText.setText("");
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        messageLabel = new javax.swing.JLabel();
        userLabel = new javax.swing.JLabel();
        passwordLabel = new javax.swing.JLabel();
        userText = new javax.swing.JTextField();
        password = new javax.swing.JPasswordField();

        setMaximumSize(new java.awt.Dimension(500, 200));
        setMinimumSize(new java.awt.Dimension(500, 150));
        setPreferredSize(new java.awt.Dimension(500, 150));

        org.openide.awt.Mnemonics.setLocalizedText(messageLabel, this.message);

        org.openide.awt.Mnemonics.setLocalizedText(userLabel, this.userLabelText);

        org.openide.awt.Mnemonics.setLocalizedText(passwordLabel, this.passwordLabelText);

        userText.setText(this.instance.getAdminUser());

        password.setText(this.instance.getAdminPassword());

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(messageLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(passwordLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(userLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 150, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(userText, javax.swing.GroupLayout.DEFAULT_SIZE, 314, Short.MAX_VALUE)
                            .addComponent(password))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(messageLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 67, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(userLabel)
                    .addComponent(userText, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(passwordLabel)
                    .addComponent(password, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(15, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel messageLabel;
    private javax.swing.JPasswordField password;
    private javax.swing.JLabel passwordLabel;
    private javax.swing.JLabel userLabel;
    private javax.swing.JTextField userText;
    // End of variables declaration//GEN-END:variables
}
