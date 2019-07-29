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
package org.netbeans.modules.payara.common;

import java.awt.Font;
import java.awt.FontMetrics;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.NetworkSettings;

/** Global password protected sites Authenticator for IDE
 *
 * @author  Ludo, Petr Hrebejk, vkraemer
 */
public class AdminAuthenticator extends java.net.Authenticator {

    private boolean displayed = false;
    private static final long TIMEOUT = 3000;
    private static long lastTry = 0;

    public AdminAuthenticator() {
        Preferences proxySettingsNode = NbPreferences.root().node("/org/netbeans/core"); //NOI18N
        assert proxySettingsNode != null;
    }

    @Override
    protected java.net.PasswordAuthentication getPasswordAuthentication() {
        Logger.getLogger("payara").log(Level.FINER, "AdminAuthenticator.getPasswordAuthentication() with prompt {0}", this.getRequestingPrompt()); //NOI18N

        String title = getRequestingPrompt();
        // if the request is coming from a proxy and the IDE has proxy auth values
        // send them straight back...
        if (RequestorType.PROXY == getRequestorType() && ProxySettings.useAuthentication()) {
            Logger.getLogger("payara").log(Level.FINER, "Username set to {0} while request {1}", new Object[]{ProxySettings.getAuthenticationUsername(), this.getRequestingURL()}); //NOI18N
            return new java.net.PasswordAuthentication(ProxySettings.getAuthenticationUsername(), ProxySettings.getAuthenticationPassword());
        } else {
            // if we haven't been called in the last three seconds...
            //
            if (System.currentTimeMillis() - lastTry > TIMEOUT) {

                // if anything other than the GF AS is asking for authentication
                // make it possible to see the dialog
                if (!"admin-realm".equals(title)) {  // NOI18N
                    displayed = false;
                }
                if (!displayed && !NetworkSettings.isAuthenticationDialogSuppressed()) {
                    // try to prevent the dialog from popping up too often... since the
                    // plugin  sends a bunch of requests to the AS one after the other to try to
                    // populate its node tree.
                    //
                    displayed = true;

                    ResourceBundle bundle = NbBundle.getBundle(AdminAuthenticator.class);
                    String dialogTitle = getRequestingHost();
                    if (null == dialogTitle) {
                        dialogTitle = bundle.getString("CTL_PasswordProtected");
                    }
                    String name = ""; // NOI18N

                    PasswordPanel passwordPanel = new PasswordPanel(name);
                    String url = getRequestingURL().toString();
                    if (null == url || !url.contains("/__asadmin/")) { // NOI18N
                        passwordPanel.setPrompt(title);
                    } else {
                        dialogTitle = bundle.getString("CTL_PasswordProtected");
                        passwordPanel.setPrompt(NbBundle.getMessage(AdminAuthenticator.class,
                                "PROMPT_PAYARA_AUTH", getRequestingHost(),
                                Integer.toString(getRequestingPort())));
                    }
                    DialogDescriptor dd = new DialogDescriptor(passwordPanel, dialogTitle);
                    java.awt.Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
                    dialog.setVisible(true);

                    if (dd.getValue().equals(NotifyDescriptor.OK_OPTION)) {
                        lastTry = System.currentTimeMillis();
                        return new java.net.PasswordAuthentication(passwordPanel.getUsername(), passwordPanel.getPassword());
                    } else {
                        // cancelled... so just update the delay stamp.
                        lastTry = System.currentTimeMillis();
                    }
                } // fi !displayed
                } // fi time check...
            }

        Logger.getLogger("payara").log(Level.WARNING, "No authentication set while requesting {0}", this.getRequestingURL()); //NOI18N
        return null;
    }

    /** Inner class for JPanel with Username & Password fields */
    static class PasswordPanel extends javax.swing.JPanel {

        private static final int DEFAULT_WIDTH = 200;
        private static final int DEFAULT_HEIGHT = 0;
        /** Generated serialVersionUID */
        static final long serialVersionUID = 1555749205340031767L;
        ResourceBundle bundle = org.openide.util.NbBundle.getBundle(AdminAuthenticator.class);

        /** Creates new form PasswordPanel */
        public PasswordPanel(String userName) {
            initComponents();
            usernameField.setText(userName);
            usernameField.setSelectionStart(0);
            usernameField.setSelectionEnd(userName.length());
            usernameField.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_UserNameField"));
            passwordField.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_PasswordField"));
        }

        @Override
        public java.awt.Dimension getPreferredSize() {
            java.awt.Dimension sup = super.getPreferredSize();
            return new java.awt.Dimension(Math.max(sup.width, DEFAULT_WIDTH), Math.max(sup.height, DEFAULT_HEIGHT));
        }

        /** This method is called from within the constructor to
         * initialize the form.
         * WARNING: Do NOT modify this code. The content of this method is
         * always regenerated by the FormEditor.
         */
        private void initComponents() {
            setLayout(new java.awt.BorderLayout());

            mainPanel = new javax.swing.JPanel();
            mainPanel.setLayout(new java.awt.GridBagLayout());
            java.awt.GridBagConstraints gridBagConstraints1;
            mainPanel.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(12, 12, 0, 11)));

            promptLabel = new javax.swing.JLabel();
            promptLabel.setHorizontalAlignment(0);

            gridBagConstraints1 = new java.awt.GridBagConstraints();
            gridBagConstraints1.gridwidth = 0;
            gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints1.insets = new java.awt.Insets(0, 0, 6, 0);
            mainPanel.add(promptLabel, gridBagConstraints1);

            jLabel1 = new javax.swing.JLabel();
            org.openide.awt.Mnemonics.setLocalizedText(jLabel1,
                    bundle.getString("LAB_AUTH_User_Name")); // NOI18N

            gridBagConstraints1 = new java.awt.GridBagConstraints();
            gridBagConstraints1.insets = new java.awt.Insets(0, 0, 5, 12);
            gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
            mainPanel.add(jLabel1, gridBagConstraints1);

            usernameField = new javax.swing.JTextField();
            Font f = usernameField.getFont();
            FontMetrics fm = usernameField.getFontMetrics(f);
            int unFH = (fm.getHeight()*150)/100;
            usernameField.setMinimumSize(new java.awt.Dimension(70, unFH));
            usernameField.setPreferredSize(new java.awt.Dimension(70, unFH));
            jLabel1.setLabelFor(usernameField);

            gridBagConstraints1 = new java.awt.GridBagConstraints();
            gridBagConstraints1.gridwidth = 0;
            gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints1.insets = new java.awt.Insets(0, 0, 5, 0);
            gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints1.weightx = 1.0;
            mainPanel.add(usernameField, gridBagConstraints1);

            jLabel2 = new javax.swing.JLabel();
            org.openide.awt.Mnemonics.setLocalizedText(jLabel2,
                    bundle.getString("LAB_AUTH_Password")); // NOI18N

            gridBagConstraints1 = new java.awt.GridBagConstraints();
            gridBagConstraints1.insets = new java.awt.Insets(0, 0, 0, 12);
            gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
            mainPanel.add(jLabel2, gridBagConstraints1);

            passwordField = new javax.swing.JPasswordField();
            passwordField.setMinimumSize(new java.awt.Dimension(70, unFH));
            passwordField.setPreferredSize(new java.awt.Dimension(70, unFH));
            jLabel2.setLabelFor(passwordField);

            gridBagConstraints1 = new java.awt.GridBagConstraints();
            gridBagConstraints1.gridwidth = 0;
            gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints1.anchor = java.awt.GridBagConstraints.WEST;
            gridBagConstraints1.weightx = 1.0;
            mainPanel.add(passwordField, gridBagConstraints1);

            add(mainPanel, "Center"); // NOI18N

        }
        // Variables declaration - do not modify//GEN-BEGIN:variables
        private javax.swing.JPanel mainPanel;
        private javax.swing.JLabel promptLabel;
        private javax.swing.JLabel jLabel1;
        private javax.swing.JTextField usernameField;
        private javax.swing.JLabel jLabel2;
        private javax.swing.JPasswordField passwordField;
        // End of variables declaration//GEN-END:variables

        String getUsername() {
            return usernameField.getText();
        }

        char[] getPassword() {
            return passwordField.getPassword();
        }

        String getTPassword() {
            return passwordField.getText();
        }

        void setPrompt(String prompt) {
            if (prompt == null) {
                promptLabel.setVisible(false);
                getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_NbAuthenticatorPasswordPanel"));
            } else {
                promptLabel.setVisible(true);
                promptLabel.setText(prompt);
                getAccessibleContext().setAccessibleDescription(prompt);
            }
        }
    }
}
