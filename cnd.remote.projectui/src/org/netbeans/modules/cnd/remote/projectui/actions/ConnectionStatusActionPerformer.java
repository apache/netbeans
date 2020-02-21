/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
