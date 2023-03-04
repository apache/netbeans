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
