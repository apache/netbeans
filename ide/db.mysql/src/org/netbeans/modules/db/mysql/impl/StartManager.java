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
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.modules.db.mysql.DatabaseServer;
import org.netbeans.modules.db.mysql.ui.PropertiesDialog;
import org.netbeans.modules.db.mysql.util.DatabaseUtils;
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
 * @author David Van Couvering
 */
public final class StartManager {
    private static final StartManager DEFAULT = new StartManager();
    private static final Logger LOGGER = Logger.getLogger(StartManager.class.getName());

    private final PropertyChangeListener listener = new StartPropertyChangeListener();
    private final AtomicBoolean isStarting = new AtomicBoolean(false);
    private final AtomicBoolean startRequested = new AtomicBoolean(false);
    private volatile DatabaseServer server;
    private volatile String errorMessage;
    private volatile boolean waitOnUsersInput;
    private volatile boolean stopWaiting;

    private StartManager() {
    }

    public static StartManager getDefault() {
        return DEFAULT;
    }

    public PropertyChangeListener getStartListener() {
        return listener;
    }

    public boolean isStartRequested() {
        return startRequested.get();
    }

    private synchronized void setIsStarting(boolean isStarting) {
        this.isStarting.set(isStarting);
    }

    public void start(final DatabaseServer server) {
        this.server = server;

        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                try {
                    startRequested.set(true);
                    boolean starting = isStarting.getAndSet(true);
                    if (starting) {
                        LOGGER.log(Level.FINE, "Server is already starting");
                        return;
                    }
                    StatusDisplayer.getDefault().setStatusText(NbBundle.getMessage(StartManager.class, "MSG_StartingMySQL"));

                    server.start();

                    waitForStartAndConnect();
                } catch (DatabaseException dbe) {
                    Utils.displayError(Utils.getMessage("MSG_UnableToStartServer"), dbe);
                } finally {
                    setIsStarting(false);
                }
            }
            
        });
    }

    private void waitForStartAndConnect() {
        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                ProgressHandle handle = ProgressHandle.createHandle(
                        NbBundle.getMessage(StartManager.class, "MSG_WaitingForServerToStart"));
                handle.start();
                handle.switchToIndeterminate();
                waitOnUsersInput = false;
                stopWaiting = false;

                try {
                    for ( ; ; ) {
                        try {
                            if (waitForStart()) {
                                startRequested.set(false);
                                return;
                            } else {
                                if (stopWaiting) {
                                    break;
                                }
                            }
                        } catch (DatabaseException dbe) {
                            // probably invalid/missing password
                            assert ! DatabaseUtils.isCommunicationsException(dbe) : "Only other than an CommunicationsException excepted, was: " + dbe;
                            if (displayAdminProperties(server, dbe)) {
                                continue;
                            } else {
                                break;
                            }
                        }

                        if (! waitOnUsersInput) {
                            displayServerNotRunning();
                        }
                    }
                } finally {
                    handle.finish();
                    startRequested.set(false);
                }
            }

        });
    }

    private void displayServerNotRunning() {
        waitOnUsersInput = true;
        Mutex.EVENT.postReadRequest(new Runnable() {
            @Override
            public void run() {
                JButton cancelButton = new JButton();
                Mnemonics.setLocalizedText(cancelButton, NbBundle.getMessage(StartManager.class, "StartManager.CancelButton")); // NOI18N
                cancelButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(StartManager.class, "StartManager.CancelButtonA11yDesc")); //NOI18N

                JButton keepWaitingButton = new JButton();
                Mnemonics.setLocalizedText(keepWaitingButton, NbBundle.getMessage(StartManager.class, "StartManager.KeepWaitingButton")); // NOI18N
                keepWaitingButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(StartManager.class, "StartManager.KeepWaitingButtonA11yDesc")); //NOI18N

                JButton propsButton = new JButton();
                Mnemonics.setLocalizedText(propsButton, NbBundle.getMessage(StartManager.class, "StartManager.PropsButton")); // NOI18N
                propsButton.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(StartManager.class, "StartManager.PropsButtonA11yDesc")); //NOI18N

                String message = NbBundle.getMessage(StartManager.class, "MSG_ServerDoesNotAppearToHaveStarted", errorMessage);
                final NotifyDescriptor ndesc = new NotifyDescriptor(message,
                        NbBundle.getMessage(StartManager.class, "StartManager.ServerNotRunningTitle"),
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
                    startRequested.set(false);
                    stopWaiting = true;
                } else if (keepWaitingButton.equals(ret)) {
                    stopWaiting = false;
                } else {
                    stopWaiting = ! displayAdminProperties(server, null);
                }
                waitOnUsersInput = false;
            }
        });
    }

    private boolean displayAdminProperties(final DatabaseServer server, Throwable th)  {
        PropertiesDialog pd = new PropertiesDialog(server);
        if (th != null) {
            while (th.getCause() != null) {
                th = th.getCause();
            }
            pd.setErrorMessage(th.getLocalizedMessage());
        }
        return pd.displayDialog();
    }

    @SuppressWarnings({"SleepWhileHoldingLock", "SleepWhileInLoop"})
    private boolean waitForStart() throws DatabaseException {
        int tries = 0;
        while (tries <= 5 && !stopWaiting) {
            tries++;

            try {
                // wait for perform start command first
                Thread.sleep(2000);
            } catch (InterruptedException ie) {
                LOGGER.log(Level.INFO, "Interrupted waiting for server to start", ie);
                Thread.currentThread().interrupt();
                return false;
            }

            try {
                server.reconnect();
                return true;
            } catch (DatabaseException dbe) {
                errorMessage = dbe.getMessage();
                LOGGER.log(Level.INFO, null, dbe);
                if (! DatabaseUtils.isCommunicationsException(dbe)) {
                    throw dbe;
                }
            } catch (TimeoutException te) {
                errorMessage = te.getMessage();
                LOGGER.log(Level.INFO, null, te);
            }

        }

        return false;
    }

    private class StartPropertyChangeListener implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            final DatabaseServer server = (DatabaseServer)evt.getSource();
            if ((MySQLOptions.PROP_START_ARGS.equals(evt.getPropertyName()) ||
                    MySQLOptions.PROP_START_PATH.equals(evt.getPropertyName())) && (startRequested.get())) {
                start(server);
            }
        }
    }
}
