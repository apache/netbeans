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
package org.netbeans.core;

import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.NetworkSettings;

/** Global password protected sites Authenticator for IDE
 *
 * @author Jiri Rechtacek
 */
final class NbAuthenticator extends java.net.Authenticator {

    private static final long TIMEOUT = 3000;
    private static long lastTry = 0;

    private NbAuthenticator() {
        Preferences proxySettingsNode = NbPreferences.root().node("/org/netbeans/core"); //NOI18N
        assert proxySettingsNode != null;
    }

    static void install() {
        if (Boolean.valueOf(NbBundle.getMessage(GuiRunLevel.class, "USE_Authentication"))) {
            setDefault(new NbAuthenticator());
        }
    }

    static void install4test() {
        setDefault(new NbAuthenticator());
    }

    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        Logger.getLogger(NbAuthenticator.class.getName()).log(Level.FINER, "Authenticator.getPasswordAuthentication() with prompt " + this.getRequestingPrompt()); //NOI18N

        if (RequestorType.PROXY == getRequestorType() && ProxySettings.useAuthentication()) {
            Logger.getLogger(NbAuthenticator.class.getName()).log(Level.FINER, "Username set to " + ProxySettings.getAuthenticationUsername() + " while request " + this.getRequestingURL()); //NOI18N
            return new java.net.PasswordAuthentication(ProxySettings.getAuthenticationUsername(), ProxySettings.getAuthenticationPassword());
        } else {
            if (System.currentTimeMillis() - lastTry > TIMEOUT) {
                if (getRequestingProtocol().startsWith("SOCKS")&&(ProxySettings.getAuthenticationUsername().length()>0)) { //NOI18N
                    return new java.net.PasswordAuthentication(ProxySettings.getAuthenticationUsername(), ProxySettings.getAuthenticationPassword());
                }
                if (NetworkSettings.isAuthenticationDialogSuppressed()) {
                    return null;
                }
                PasswordAuthentication auth = getAuthenticationFromURL();
                if (auth != null) {
                    return auth;
                }
                NbAuthenticatorPanel ui = new NbAuthenticatorPanel(getRequestingPrompt());
                Object result = DialogDisplayer.getDefault().notify(
                        new DialogDescriptor(ui, NbBundle.getMessage(NbAuthenticator.class, "CTL_Authentication"))); //NOI18N
                if (DialogDescriptor.OK_OPTION == result) {
                    lastTry = 0;
                    return new PasswordAuthentication(ui.getUserName(), ui.getPassword());
                } else {
                    lastTry = System.currentTimeMillis();
                }
            }
        }

        Logger.getLogger(NbAuthenticator.class.getName()).log(Level.WARNING, "No authentication set while requesting " + this.getRequestingURL()); //NOI18N
        return null;
    }

    private PasswordAuthentication getAuthenticationFromURL() {
        URL u = this.getRequestingURL();
        if (u != null) {
            String auth = u.getUserInfo();
            if (auth != null) {
                int i = auth.indexOf(':');
                String user = (i == -1) ? auth : auth.substring(0, i);
                String pwd = (i == -1) ? "" : auth.substring(i + 1);
                return new PasswordAuthentication(user, pwd.toCharArray());
            }
        }
        return null;
    }
}
