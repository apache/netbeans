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

package org.netbeans.modules.db.explorer.action;

import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.modules.db.explorer.DatabaseConnection;

import org.netbeans.modules.db.explorer.node.ConnectionNode;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionState;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;


@ActionRegistration(
        displayName = "#Disconnect", 
        lazy = false,
        enabledOn = @ActionState(type = DatabaseConnection.class, useActionInstance = true)
)
@ActionID(category = "Database", id = "netbeans.db.explorer.action.Disconnect")
@ActionReference(path = "Databases/Explorer/Connection/Actions", position = 150)
public class DisconnectAction extends BaseAction {

    @Override
    public String getName() {
        return NbBundle.getMessage (DisconnectAction.class, "Disconnect"); // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(DisconnectAction.class);
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes.length == 0) { 
            return false;
        }
        
        for (int i = 0; i < activatedNodes.length; i++) {
            Lookup lookup = activatedNodes[i].getLookup();
            ConnectionNode node = lookup.lookup(ConnectionNode.class);
            if (node != null) {
                DatabaseConnection dbconn = lookup.lookup(DatabaseConnection.class);
                if (! dbconn.isConnected()) {
                    return false;
                }
            } else {
                return false;
            }
        }

        return true;
    }
    
    @Override
    public void performAction (final Node[] activatedNodes) {
        RequestProcessor.getDefault().post(
            new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < activatedNodes.length; i++) {
                        Lookup lookup = activatedNodes[i].getLookup();
                        ConnectionNode node = lookup.lookup(ConnectionNode.class);
                        if (node != null) {
                            DatabaseConnection connection = lookup.lookup(DatabaseConnection.class);
                            try {
                                connection.disconnect();
                            } catch (DatabaseException dbe) {
                                DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message(dbe.getLocalizedMessage()));
                            }
                        }
                    }
                }
            }
        );
    }
}
