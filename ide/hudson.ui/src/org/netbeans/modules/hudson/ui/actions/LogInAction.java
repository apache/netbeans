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
package org.netbeans.modules.hudson.ui.actions;

import java.awt.event.ActionEvent;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import org.netbeans.modules.hudson.api.ConnectionBuilder;
import org.netbeans.modules.hudson.api.HudsonInstance;
import org.netbeans.modules.hudson.spi.ConnectionAuthenticator;
import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;

@ActionID(category="Team", id="org.netbeans.modules.hudson.ui.actions.LogInAction")
@ActionRegistration(displayName="#CTL_LogInAction")
@ActionReference(path=HudsonInstance.ACTION_PATH, position=550)
@Messages("CTL_LogInAction=Log In...")
public final class LogInAction extends AbstractAction {

    private static final Logger LOG = Logger.getLogger(LogInAction.class.getName());
    private static final RequestProcessor RP = new RequestProcessor(LogInAction.class);

    private final HudsonInstance instance;
    
    public LogInAction(HudsonInstance instance) {
        super(Bundle.CTL_LogInAction());
        this.instance = instance;
    }

    public @Override void actionPerformed(ActionEvent ev) {
        if (instance.isForbidden()) {
            // This will automatically prompt for login.
            instance.synchronize(true);
            return;
        }
        RP.post(new Runnable() {
            @Override public void run() {
                try {
                    URLConnection conn = new ConnectionBuilder().instance(instance).url(instance.getUrl()).connection();
                    for (ConnectionAuthenticator authenticator : Lookup.getDefault().lookupAll(ConnectionAuthenticator.class)) {
                        URLConnection retry = authenticator.forbidden(conn, new URL(instance.getUrl()));
                        if (retry != null) {
                            // XXX try opening user/$user/configure, which lets you know if you are really authenticated
                            // otherwise failed login gets here but there is no apparent way to tell (at least not in ServletConnectionAuthenticator)
                            instance.synchronize(false);
                            return;
                        }
                    }
                } catch (IOException x) {
                    LOG.log(Level.INFO, null, x);
                }
            }
        });
    }

}
