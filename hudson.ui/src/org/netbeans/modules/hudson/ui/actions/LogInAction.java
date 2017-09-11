/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
