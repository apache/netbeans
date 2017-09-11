/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 - 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 - 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.db.mysql.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.modules.db.mysql.DatabaseServer;
import org.netbeans.modules.db.mysql.ui.PropertiesDialog;
import org.netbeans.modules.db.mysql.util.Utils;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author David
 */
public class ConnectManager {
    private static final ConnectManager DEFAULT = new ConnectManager();
    private final PropertyChangeListener listener = new ReconnectPropertyChangeListener();

    private static final Logger LOGGER = Logger.getLogger(ConnectManager.class.getName());

    // Guarded by this
    private boolean reconnecting = false;
    private static final RequestProcessor RP = new RequestProcessor(ConnectManager.class);

    private ConnectManager() {
    }
    
    public static ConnectManager getDefault() {
        return DEFAULT;
    }

    public PropertyChangeListener getReconnectListener() {
        return listener;
    }

    public void reconnect(final DatabaseServer server) {
        RP.post(new Runnable() {
            @Override
            public void run() {
                try {
                    synchronized(ConnectManager.this) {
                        if (reconnecting) {
                            // If we're already in the process of reconnecting, don't try it again
                            // (this can happen for instance if multiple properties have changed, each firing
                            // a property change event).
                            LOGGER.log(Level.FINE, "Already reconnecting to the server");
                            return;
                        }
                        reconnecting = true;
                    }
                    
                    StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(ConnectManager.class, "MSG_ReconnectingToMySQL"));
                    server.reconnect();
                } catch (DatabaseException dbe) {
                    LOGGER.log(Level.INFO, dbe.getMessage(), dbe);
                    
                    boolean displayProperties = Utils.displayYesNoDialog(NbBundle.getMessage(ConnectManager.class,
                            "MSG_ReconnectFailed", dbe.getMessage()));

                    if (displayProperties) {
                        Mutex.EVENT.postReadRequest(new Runnable() {
                            @Override
                            public void run() {
                                PropertiesDialog dialog = new PropertiesDialog(server);
                                boolean ok = dialog.displayDialog();
                                if (ok) {
                                    ConnectManager.getDefault().reconnect(server);
                                }
                            }
                        });
                    }
                } catch (TimeoutException te) {
                    LOGGER.log(Level.INFO, te.getMessage(), te);
                    Utils.displayErrorMessage(te.getMessage());
                } finally {
                    setReconnecting(false);
                }
            }

        });
    }

    private synchronized void setReconnecting(boolean isReconnecting) {
        this.reconnecting = isReconnecting;
    }

    private class ReconnectPropertyChangeListener implements PropertyChangeListener {
        private boolean propertyChangeNeedsReconnect(PropertyChangeEvent evt) {
            String property = evt.getPropertyName();
            if (property.equals(MySQLOptions.PROP_ADMINUSER) ||
                property.equals(MySQLOptions.PROP_HOST)       ||
                property.equals(MySQLOptions.PROP_PORT)) {
                    return true;
            }

            return false;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            final DatabaseServer server = (DatabaseServer)evt.getSource();
            if (propertyChangeNeedsReconnect(evt)) {
                reconnect(server);
            }
        }
    }

}
