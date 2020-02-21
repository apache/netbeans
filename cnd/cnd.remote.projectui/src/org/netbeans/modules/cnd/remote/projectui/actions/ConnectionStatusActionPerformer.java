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
package org.netbeans.modules.cnd.remote.projectui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.modules.cnd.api.remote.ServerList;
import org.netbeans.modules.cnd.api.remote.ServerRecord;
import org.netbeans.modules.cnd.remote.actions.base.RemoteOpenActionBase;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionListener;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.openide.awt.StatusDisplayer;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 */
@ServiceProvider(path="CND/Toobar/Services/ConnectionStatus", service=ActionListener.class)
public class ConnectionStatusActionPerformer implements ActionListener, PropertyChangeListener, ConnectionListener {
    private RequestProcessor RP = new RequestProcessor("Connection worker", 1); //NOI18N
    private static final Logger logger = Logger.getLogger("remote.toolbar"); //NOI18N
    private ConnectionStatusAction presenter;
    private ExecutionEnvironment prevEnv = null;

    public ConnectionStatusActionPerformer() {
    }

    private void init() {
        ServerList.addPropertyChangeListener(WeakListeners.propertyChange(this, this));
        ConnectionManager.getInstance().addConnectionListener(WeakListeners.create(ConnectionListener.class, this, ConnectionManager.getInstance()));
        // initial status        
        updateStatus();
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (RemoteOpenActionBase.ACTIVATED_PSEUDO_ACTION_COMAND.equals(e.getActionCommand())) { // NOI18N
            presenter = (ConnectionStatusAction) e.getSource();
            init();
            return;
        }
        ExecutionEnvironment executionEnvironment = ServerList.getDefaultRecord().getExecutionEnvironment();
        if (executionEnvironment.isLocal()) {
            return;
        }
        actionPerformed(executionEnvironment, ConnectionManager.getInstance().isConnectedTo(executionEnvironment));
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (ServerList.PROP_DEFAULT_RECORD.equals(evt.getPropertyName())) {
            if (logger.isLoggable(Level.FINE)) {
                ExecutionEnvironment executionEnvironment = ServerList.getDefaultRecord().getExecutionEnvironment();
                boolean connectedTo = ConnectionManager.getInstance().isConnectedTo(executionEnvironment);
                logger.log(Level.FINE, "change default host {0}, connected {1}", new Object[]{executionEnvironment, connectedTo}); // NOI18N
            }
            updateStatus();
        }
        if (ServerRecord.PROP_STATE_CHANGED.equals(evt.getPropertyName())) {
            Object source = evt.getSource();
            if (source instanceof ServerRecord) {
                ServerRecord r = (ServerRecord) source;
                if (r.getExecutionEnvironment().equals(ServerList.getDefaultRecord().getExecutionEnvironment())) {
                    updateStatus();
                }
            }
        }
    }
    @Override
    public void connected(ExecutionEnvironment env) {
        ExecutionEnvironment executionEnvironment = ServerList.getDefaultRecord().getExecutionEnvironment();
        if (env.equals(executionEnvironment)) {
            logger.log(Level.FINE, "change state host {0}, connected {1}", new Object[]{executionEnvironment, true}); // NOI18N
            updateStatus();
        }
    }

    @Override
    public void disconnected(ExecutionEnvironment env) {
        ExecutionEnvironment executionEnvironment = ServerList.getDefaultRecord().getExecutionEnvironment();
        if (env.equals(executionEnvironment)) {
            logger.log(Level.FINE, "change state host {0}, connected {1}", new Object[]{executionEnvironment, false}); // NOI18N
            updateStatus();
        }
    }

    private void updateStatus() {

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (presenter != null) {
                    ExecutionEnvironment executionEnvironment = ServerList.getDefaultRecord().getExecutionEnvironment();
                    ServerRecord record = ServerList.get(executionEnvironment);
                    // prevEnv does not need sync - accessed from EDT only
                    if (!executionEnvironment.equals(prevEnv)) {
                        record.addPropertyChangeListener(ConnectionStatusActionPerformer.this);
                        if (prevEnv != null) {
                            ServerRecord prevRecord = ServerList.get(prevEnv);
                            prevRecord.removePropertyChangeListener(ConnectionStatusActionPerformer.this);
                        }
                        prevEnv = executionEnvironment;
                    }
                    presenter.setEnabled(!executionEnvironment.isLocal());
                    boolean connectedTo = ConnectionManager.getInstance().isConnectedTo(executionEnvironment);
                    if (executionEnvironment.isLocal()) {
                        presenter.putValue("iconBase", "org/netbeans/modules/cnd/remote/projectui/resources/connected.png"); //NOI18N
                    } else {
                        if (connectedTo) {                            
                            if(record != null && record.isOnline()) {
                                presenter.putValue("iconBase", "org/netbeans/modules/cnd/remote/projectui/resources/connected.png"); //NOI18N
                            } else {
                                presenter.putValue("iconBase", "org/netbeans/modules/cnd/remote/projectui/resources/not_set_up.png"); //NOI18N
                            }
                        } else {
                            presenter.putValue("iconBase", "org/netbeans/modules/cnd/remote/projectui/resources/disconnected.png"); //NOI18N
                        }
                    }
                }
            }
        });
    }

    private void actionPerformed(final ExecutionEnvironment executionEnvironment, final boolean isConnected) {
        RP.post(new Runnable() {

            @Override
            public void run() {
                try {
                    if (!isConnected) {
                        ConnectionManager.getInstance().connectTo(executionEnvironment);
                        ServerRecord record = ServerList.get(executionEnvironment);
                        record.checkSetupAfterConnection(null);
                    } else {
                        ConnectionManager.getInstance().disconnect(executionEnvironment);
                    }
                } catch (Exception ex) {
                    String message = NbBundle.getMessage(ConnectionStatusActionPerformer.class, 
                            "ErrorConnectingHost", executionEnvironment.getDisplayName(), ex.getMessage()); // NOI18N
                    StatusDisplayer.getDefault().setStatusText(message);
                }
            }
        });
    }
}
