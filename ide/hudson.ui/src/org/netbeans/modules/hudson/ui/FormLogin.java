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

package org.netbeans.modules.hudson.ui;

import java.net.URL;
import java.util.prefs.Preferences;
import javax.swing.JPanel;
import org.netbeans.api.keyring.Keyring;
import org.netbeans.modules.hudson.api.HudsonManager;
import org.netbeans.modules.hudson.spi.PasswordAuthorizer;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;

/**
 * Form-based login impl which keeps username in preferences and authenticates only when requested.
 */
public class FormLogin extends JPanel {

    static Preferences loginPrefs() {
        return NbPreferences.forModule(FormLogin.class).node("authentication"); // NOI18N
    }

    private FormLogin() {
        initComponents();
    }

    @ServiceProvider(service=PasswordAuthorizer.class, position=1000)
    public static class AuthImpl implements PasswordAuthorizer {

        @Messages({
            "FormLogin.log_in=Log in to Jenkins",
            "# {0} - server location", "# {1} - user name", "FormLogin.password_description=Password for {1} on {0}"
        })
        @Override
        public String[] authorize(URL home) {
            FormLogin panel = new FormLogin();
            String server = HudsonManager.simplifyServerLocation(home.toString(), true);
            String username = loginPrefs().get(server, null);
            if (username != null) {
                panel.userField.setText(username);
                char[] savedPassword = Keyring.read(server);
                if (savedPassword != null) {
                    panel.passField.setText(new String(savedPassword));
                }
            }
            panel.locationField.setText(home.toString());
            DialogDescriptor dd = new DialogDescriptor(panel, Bundle.FormLogin_log_in());
            if (DialogDisplayer.getDefault().notify(dd) != NotifyDescriptor.OK_OPTION) {
                return null;
            }
            username = panel.userField.getText();
            loginPrefs().put(server, username);
            String password = new String(panel.passField.getPassword());
            panel.passField.setText("");
            Keyring.save(server, password.toCharArray(), Bundle.FormLogin_password_description(home, username));
            return new String[] {username, password};
        }
    }

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        locationLabel = new javax.swing.JLabel();
        locationField = new javax.swing.JTextField();
        userLabel = new javax.swing.JLabel();
        userField = new javax.swing.JTextField();
        passLabel = new javax.swing.JLabel();
        passField = new javax.swing.JPasswordField();

        org.openide.awt.Mnemonics.setLocalizedText(locationLabel, org.openide.util.NbBundle.getMessage(FormLogin.class, "FormLogin.locationLabel.text")); // NOI18N

        locationField.setEditable(false);

        org.openide.awt.Mnemonics.setLocalizedText(userLabel, org.openide.util.NbBundle.getMessage(FormLogin.class, "FormLogin.userLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(passLabel, org.openide.util.NbBundle.getMessage(FormLogin.class, "FormLogin.passLabel.text")); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(locationLabel)
                        .addGap(36, 36, 36)
                        .addComponent(locationField, javax.swing.GroupLayout.DEFAULT_SIZE, 301, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(passLabel)
                            .addComponent(userLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(passField, javax.swing.GroupLayout.DEFAULT_SIZE, 299, Short.MAX_VALUE)
                            .addComponent(userField, javax.swing.GroupLayout.DEFAULT_SIZE, 299, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(locationLabel)
                    .addComponent(locationField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(userField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(userLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(passLabel)
                    .addComponent(passField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField locationField;
    private javax.swing.JLabel locationLabel;
    private javax.swing.JPasswordField passField;
    private javax.swing.JLabel passLabel;
    private javax.swing.JTextField userField;
    private javax.swing.JLabel userLabel;
    // End of variables declaration//GEN-END:variables

    public @Override void addNotify() {
        super.addNotify();
        ((userField.getText().length() > 0) ? passField : userField).requestFocus();
    }

}
