/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of the
 * License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing the
 * software, include this License Header Notice in each file and include the
 * License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by Oracle
 * in the GPL Version 2 section of the License file that accompanied this code.
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or only
 * the GPL Version 2, indicate your decision by adding "[Contributor] elects to
 * include this software in this distribution under the [CDDL or GPL Version 2]
 * license." If you do not indicate a single choice of license, a recipient has
 * the option to distribute your version of this file under either the CDDL, the
 * GPL Version 2 or to extend the choice of license to its licensees as provided
 * above. However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is made
 * subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

package org.netbeans.modules.hudson.ui;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import org.apache.commons.net.util.Base64;
import org.netbeans.api.keyring.Keyring;
import org.netbeans.modules.hudson.api.HudsonManager;
import org.netbeans.modules.hudson.api.HudsonVersion;
import org.netbeans.modules.hudson.api.Utilities;
import org.netbeans.modules.hudson.spi.ConnectionAuthenticator;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.HtmlBrowser;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.ServiceProvider;

/**
 * Supplies HTTP BASIC authentication using an API token.
 * Useful for servers using special authentication modes such as OpenID.
 * Currently works only in Jenkins 1.426+.
 * @see <a href="https://wiki.jenkins-ci.org/display/JENKINS/Authenticating+scripted+clients">Authenticating scripted clients</a>
 */
public class APITokenConnectionAuthenticator extends JPanel {

    private static final Logger LOG = Logger.getLogger(APITokenConnectionAuthenticator.class.getName());

    /**
     * Map from home URL to encoded username:password.
     * @see <a href="http://stackoverflow.com/questions/496651/connecting-to-remote-url-which-requires-authentication-using-java/5137446#5137446">technique</a>
     */
    private static final Map</*URL*/String,String> BASIC_AUTH = new HashMap<String,String>();

    @ServiceProvider(service=ConnectionAuthenticator.class, position=200)
    public static final class Impl implements ConnectionAuthenticator {

        @Override public void prepareRequest(URLConnection conn, URL home) {
            String auth = BASIC_AUTH.get(home.toString());
            if (auth != null) {
                LOG.log(Level.FINER, "have basic auth for {0}", home);
                conn.setRequestProperty("Authorization", "Basic " + auth);
            }
        }

        @Messages({"# {0} - server location", "# {1} - user name", "APITokenConnectionAuthenticator.password_description=API token for {1} on {0}"})
        @org.netbeans.api.annotations.common.SuppressWarnings("DM_DEFAULT_ENCODING")
        @Override public URLConnection forbidden(URLConnection conn, URL home) {
            String version = conn.getHeaderField("X-Jenkins");
            if (version == null) {
                if (conn.getHeaderField("X-Hudson") == null) {
                    LOG.log(Level.FINE, "neither Hudson nor Jenkins headers on {0}, assuming might be Jenkins", home);
                } else {
                    LOG.log(Level.FINE, "disabled on non-Jenkins server {0}", home);
                    return null;
                }
            } else if (new HudsonVersion(version).compareTo(new HudsonVersion("1.426")) < 0) {
                LOG.log(Level.FINE, "disabled on old ({0}) Jenkins server {1}", new Object[] {version, home});
                return null;
            } else {
                LOG.log(Level.FINE, "enabled on {0}", home);
            }
            APITokenConnectionAuthenticator panel = new APITokenConnectionAuthenticator();
            String server = HudsonManager.simplifyServerLocation(home.toString(), true);
            String key = "tok." + server;
            String username = FormLogin.loginPrefs().get(server, null);
            if (username != null) {
                panel.userField.setText(username);
                char[] savedToken = Keyring.read(key);
                if (savedToken != null) {
                    panel.tokField.setText(new String(savedToken));
                }
            }
            panel.locationField.setText(home.toString());
            DialogDescriptor dd = new DialogDescriptor(panel, Bundle.FormLogin_log_in());
            if (DialogDisplayer.getDefault().notify(dd) != NotifyDescriptor.OK_OPTION) {
                return null;
            }
            username = panel.userField.getText();
            LOG.log(Level.FINE, "trying token for {0} on {1}", new Object[] {username, home});
            FormLogin.loginPrefs().put(server, username);
            String token = new String(panel.tokField.getPassword());
            panel.tokField.setText("");
            Keyring.save(key, token.toCharArray(), Bundle.APITokenConnectionAuthenticator_password_description(home, username));
            BASIC_AUTH.put(home.toString(), new Base64(0).encodeToString((username + ':' + token).getBytes()).trim());
            try {
                return conn.getURL().openConnection();
            } catch (IOException x) {
                LOG.log(Level.FINE, null, x);
                return null;
            }
        }

    }

    private APITokenConnectionAuthenticator() {
        initComponents();
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        locationLabel = new javax.swing.JLabel();
        locationField = new javax.swing.JTextField();
        userLabel = new javax.swing.JLabel();
        userField = new javax.swing.JTextField();
        tokLabel = new javax.swing.JLabel();
        tokField = new javax.swing.JPasswordField();
        tokButton = new javax.swing.JButton();

        locationLabel.setLabelFor(locationField);
        org.openide.awt.Mnemonics.setLocalizedText(locationLabel, org.openide.util.NbBundle.getMessage(APITokenConnectionAuthenticator.class, "APITokenConnectionAuthenticator.locationLabel.text")); // NOI18N

        locationField.setEditable(false);

        userLabel.setLabelFor(userField);
        org.openide.awt.Mnemonics.setLocalizedText(userLabel, org.openide.util.NbBundle.getMessage(APITokenConnectionAuthenticator.class, "APITokenConnectionAuthenticator.userLabel.text")); // NOI18N

        tokLabel.setLabelFor(tokField);
        org.openide.awt.Mnemonics.setLocalizedText(tokLabel, org.openide.util.NbBundle.getMessage(APITokenConnectionAuthenticator.class, "APITokenConnectionAuthenticator.tokLabel.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(tokButton, NbBundle.getMessage(APITokenConnectionAuthenticator.class, "APITokenConnectionAuthenticator.tokButton.text")); // NOI18N
        tokButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tokButtonActionPerformed(evt);
            }
        });

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
                        .addComponent(locationField, javax.swing.GroupLayout.DEFAULT_SIZE, 279, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(tokLabel)
                            .addComponent(userLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(userField, javax.swing.GroupLayout.DEFAULT_SIZE, 277, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(tokField)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(tokButton)))))
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
                    .addComponent(tokLabel)
                    .addComponent(tokField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(tokButton))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void tokButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tokButtonActionPerformed
        try {
            HtmlBrowser.URLDisplayer.getDefault().showURLExternal(new URL(locationField.getText() + "user/" + Utilities.uriEncode(userField.getText()) + "/configure"));
        } catch (MalformedURLException x) {
            LOG.log(Level.INFO, null, x);
        }
    }//GEN-LAST:event_tokButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField locationField;
    private javax.swing.JLabel locationLabel;
    private javax.swing.JButton tokButton;
    private javax.swing.JPasswordField tokField;
    private javax.swing.JLabel tokLabel;
    private javax.swing.JTextField userField;
    private javax.swing.JLabel userLabel;
    // End of variables declaration//GEN-END:variables

    public @Override void addNotify() {
        super.addNotify();
        ((userField.getText().length() > 0) ? tokField : userField).requestFocus();
    }

}
