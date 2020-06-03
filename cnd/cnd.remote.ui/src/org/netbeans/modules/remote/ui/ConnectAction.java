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
package org.netbeans.modules.remote.ui;

import java.io.IOException;
import org.netbeans.modules.cnd.remote.utils.RemoteUtil;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager.CancellationException;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.StatusDisplayer;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 */
@ActionID(id = "org.netbeans.modules.remote.ui.ConnectAction", category = "NativeRemote")
@ActionRegistration(displayName = "#ConnectMenuItem", lazy = false)
@ActionReference(path = "Remote/Host/Actions", name = "ConnectAction", position = 100)
public class ConnectAction extends SingleHostAction {
    
    private static final RequestProcessor RP = new RequestProcessor("ConnectAction", 1); // NOI18N

    @Override
    public String getName() {
        return NbBundle.getMessage(HostListRootNode.class, "ConnectMenuItem");
    }

    @Override
    protected boolean enable(ExecutionEnvironment env) {
        return !ConnectionManager.getInstance().isConnectedTo(env)
                || !HostNode.isOnline(env);
    }

    @Override
    public boolean isVisible(Node node) {
        ExecutionEnvironment env = node.getLookup().lookup(ExecutionEnvironment.class);        
        return env != null && env.isRemote();
    }


    @Override
    protected void performAction(final ExecutionEnvironment env, Node node) {
        RP.post(new Runnable() {
            @Override
            public void run() {
                connect(env);
            }
        });
    }

    private void connect(ExecutionEnvironment env) {
        try {
            ConnectionManager.getInstance().connectTo(env);
            RemoteUtil.checkSetupAfterConnection(env);
        } catch (IOException ex) {
            conectionFailed(env, ex);
        } catch (CancellationException ex) {
            conectionFailed(env, ex);
        }
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    private void conectionFailed(ExecutionEnvironment env, Exception e) {
        StatusDisplayer.getDefault().setStatusText(
                NbBundle.getMessage(ConnectAction.class, "UnableToConnectMessage", RemoteUtil.getDisplayName(env), e.getMessage()));

    }
}
