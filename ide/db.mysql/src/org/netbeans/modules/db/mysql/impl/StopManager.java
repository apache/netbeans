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

package org.netbeans.modules.db.mysql.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.db.mysql.DatabaseServer;
import org.netbeans.modules.db.mysql.ui.PropertiesDialog;
import org.netbeans.modules.db.mysql.ui.PropertiesDialog.Tab;
import org.netbeans.modules.db.mysql.util.Utils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Mutex;
import org.openide.util.Mutex.Action;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Manages starting of a server, including posting messages if start fails
 * and allowing users to edit properties or keep waiting as needed.
 *
 * This is a thread-safe class
 * 
 * @author David Van Couvering
 */
public final class StopManager {
    private static final StopManager DEFAULT = new StopManager();
    private static final Logger LOGGER = Logger.getLogger(StopManager.class.getName());

    private final PropertyChangeListener listener = new StopPropertyChangeListener();
    private final AtomicBoolean isStopping = new AtomicBoolean(false);
    private final AtomicBoolean stopRequested = new AtomicBoolean(false);

    private volatile DatabaseServer server;

    private StopManager() {
    }

    public static StopManager getDefault() {
        return DEFAULT;
    }

    public PropertyChangeListener getStopListener() {
        return listener;
    }

    public boolean isStopRequested() {
        return stopRequested.get();
    }

    private void disconnectConnections() {
        // Disconnect any connections that are for this database
        DatabaseConnection[] connections = ConnectionManager.getDefault().getConnections();
        String url = server.getURL();
        for (DatabaseConnection dbconn : connections) {
            if (MySQLOptions.getDriverClass().equals(dbconn.getDriverClass()) &&
                   dbconn.getDatabaseURL().contains(url) ) {
                ConnectionManager.getDefault().disconnect(dbconn);
            }
        }
    }

    public void stop(final DatabaseServer server) {
        this.server = server;

        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                try {
                    boolean stopping = isStopping.getAndSet(true);
                    if (stopping) {
                        LOGGER.log(Level.FINE, "Server is already stopping");
                        return;
                    }
                    StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(StopManager.class, "MSG_StoppingMySQL"));

                    stopRequested.set(true);
                    server.stop();

                    disconnectAndWaitForStop();
                } catch (DatabaseException dbe) {
                    Utils.displayError(Utils.getMessage("MSG_UnableToStopServer"), dbe);
                } finally {
                    isStopping.set(false);
                }
            }
            
        });
    }

    private void disconnectAndWaitForStop() {
        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                ProgressHandle handle = ProgressHandle.createHandle(
                        NbBundle.getMessage(StopManager.class, "MSG_WaitingForServerToStop"));
                handle.start();
                handle.switchToIndeterminate();

                try {
                    for ( ; ; ) {
                        if (waitForStop()) {
                            stopRequested.set(false);
                            return;
                        }

                        boolean keepTrying = displayServerRunning();
                        if (! keepTrying) {
                            break;
                        }
                    }
                } finally {
                    handle.finish();
                }
            }
        });
    }
    private boolean displayServerRunning() {
        JButton cancelButton = new JButton();
        Mnemonics.setLocalizedText(cancelButton, NbBundle.getMessage(StopManager.class, "StopManager.CancelButton")); // NOI18N
        cancelButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(StopManager.class, "StopManager.CancelButtonA11yDesc")); //NOI18N

        JButton keepWaitingButton = new JButton();
        Mnemonics.setLocalizedText(keepWaitingButton, NbBundle.getMessage(StopManager.class, "StopManager.KeepWaitingButton")); // NOI18N
        keepWaitingButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(StopManager.class, "StopManager.KeepWaitingButtonA11yDesc")); //NOI18N

        JButton propsButton = new JButton();
        Mnemonics.setLocalizedText(propsButton, NbBundle.getMessage(StopManager.class, "StopManager.PropsButton")); // NOI18N
        propsButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(StopManager.class, "StopManager.PropsButtonA11yDesc")); //NOI18N

        String message = NbBundle.getMessage(StopManager.class, "MSG_ServerStillRunning");
        final NotifyDescriptor ndesc = new NotifyDescriptor(message,
                NbBundle.getMessage(StopManager.class, "StopManager.ServerStillRunningTitle"),
                NotifyDescriptor.YES_NO_CANCEL_OPTION,
                NotifyDescriptor.QUESTION_MESSAGE,
                new Object[] {keepWaitingButton, propsButton, cancelButton},
                NotifyDescriptor.CANCEL_OPTION); //NOI18N

        Object ret = Mutex.EVENT.readAccess(new Action<Object>() {
            @Override
            public Object run() {
                return DialogDisplayer.getDefault().notify(ndesc);
            }

        });

        if (cancelButton.equals(ret)) {
            stopRequested.set(false);
            return false;
        } else if (keepWaitingButton.equals(ret)) {
            return true;
        } else {
            displayAdminProperties(server);
            return false;
        }
    }

    private void displayAdminProperties(final DatabaseServer server)  {
        Mutex.EVENT.postReadRequest(new Runnable() {
            @Override
            public void run() {
                PropertiesDialog dlg = new PropertiesDialog(server);
                dlg.displayDialog(Tab.ADMIN);
            }
        });
    }

    @SuppressWarnings("SleepWhileInLoop")
    private boolean waitForStop() {
        int tries = 0;
        while (tries <= 10) {
            tries++;
            
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                LOGGER.log(Level.INFO, "Interrupted waiting for server to stop", ie);
                Thread.currentThread().interrupt();
                return true;
            }

            try {
                server.reconnect();
            } catch (DatabaseException dbe) {
                server.disconnect();
                disconnectConnections();
                return true;
            } catch (TimeoutException te) {
                LOGGER.log(Level.INFO, te.getMessage(), te);

                // If it's hanging, it's effectively down
                server.disconnect();
                disconnectConnections();
                return true;
            }
        }

        return false;
    }

    private class StopPropertyChangeListener implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            final DatabaseServer server = (DatabaseServer)evt.getSource();
            if ((MySQLOptions.PROP_STOP_ARGS.equals(evt.getPropertyName()) ||
                    MySQLOptions.PROP_STOP_PATH.equals(evt.getPropertyName())) && stopRequested.get()) {
                stop(server);
            }
        }
    }
}
