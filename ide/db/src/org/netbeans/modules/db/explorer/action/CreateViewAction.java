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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.db.explorer.node.BaseNode;
import org.netbeans.lib.ddl.impl.Specification;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.explorer.DbUtilities;
import org.netbeans.modules.db.explorer.dlg.AddViewDialog;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Rob Englander
 */
public class CreateViewAction extends BaseAction {

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes == null || activatedNodes.length != 1) {
            return false;
        }

        boolean enabled = false;
        DatabaseConnection dbconn = activatedNodes[0].getLookup().lookup(DatabaseConnection.class);

        if (dbconn != null) {
            enabled = dbconn.isVitalConnection();
        }

        return enabled;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(CreateViewAction.class);
    }

    @Override
    public void performAction (Node[] activatedNodes) {
        final BaseNode node = activatedNodes[0].getLookup().lookup(BaseNode.class);
        RequestProcessor.getDefault().post(
            new Runnable() {
            @Override
                public void run() {
                    perform(node);
                }
            }
        );
    }

    private void perform(final BaseNode node) {
        DatabaseConnection connection = node.getLookup().lookup(DatabaseConnection.class);

        String schemaName = findSchemaWorkingName(node.getLookup());

        try {
            boolean viewsSupported = connection.getConnector().getDriverSpecification(schemaName).areViewsSupported();
            if (!viewsSupported) {
                String message = NbBundle.getMessage (CreateViewAction.class, "MSG_ViewsAreNotSupported", // NOI18N
                        connection.getJDBCConnection().getMetaData().getDatabaseProductName().trim());
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.INFORMATION_MESSAGE));
                return;
            }

            Specification spec = connection.getConnector().getDatabaseSpecification();

            boolean viewAdded = AddViewDialog.showDialogAndCreate(spec, schemaName);
            if (viewAdded) {
                SystemAction.get(RefreshAction.class).performAction(new Node[]{node});
            }
        } catch(Exception exc) {
            Logger.getLogger(CreateViewAction.class.getName()).log(Level.INFO, exc.getLocalizedMessage(), exc);
            DbUtilities.reportError(NbBundle.getMessage (CreateViewAction.class, "ERR_UnableToCreateView"), exc.getMessage()); // NOI18N
        }
     }

    @Override
    public String getName() {
        return NbBundle.getMessage (CreateViewAction.class, "AddView"); // NOI18N
    }
}
