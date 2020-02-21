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

import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.nativeexecution.api.util.ConnectionManager;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 */
@ActionID(id = "org.netbeans.modules.remote.ui.DisconnectAction", category = "NativeRemote")
@ActionRegistration(displayName = "#DisconnectMenuItem", lazy = false)
@ActionReference(path = "Remote/Host/Actions", name = "DisconnectAction", position = 200)
public class DisconnectAction extends SingleHostAction {
    
    private static final RequestProcessor RP = new RequestProcessor("DisconnectAction", 1); // NOI18N

    @Override
    public String getName() {
        return NbBundle.getMessage(HostListRootNode.class, "DisconnectMenuItem");
    }

    protected boolean enable(ExecutionEnvironment env) {
        return ConnectionManager.getInstance().isConnectedTo(env);
    }

    @Override
    public boolean isVisible(Node node) {
        return isRemote(node);
    }


    @Override
    protected void performAction(final ExecutionEnvironment env, Node node) {
        RP.post(new Runnable() {
            @Override
            public void run() {
                if (ConnectionManager.getInstance().isConnectedTo(env)) {
                    ConnectionManager.getInstance().disconnect(env);
                }
            }
        });
    }
}
