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

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.netbeans.modules.db.explorer.dlg.ConnectionDialogMediator;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;

import org.netbeans.modules.db.explorer.DatabaseConnection;

import org.netbeans.modules.db.explorer.dlg.SchemaPanel;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.modules.db.explorer.dlg.AddConnectionWizard;
import org.netbeans.modules.db.explorer.node.DriverNode;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor.Task;

public class ConnectUsingDriverAction extends BaseAction {
    private static final Logger LOGGER = Logger.getLogger(ConnectUsingDriverAction.class.getName());

    @Override
    public String getName() {
        return NbBundle.getMessage (ConnectUsingDriverAction.class, "ConnectUsing"); // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(ConnectUsingDriverAction.class);
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        return (activatedNodes != null && activatedNodes.length == 1);
    }

    @Override
    public void performAction(Node[] activatedNodes) {
        Lookup lookup = activatedNodes[0].getLookup();
        DriverNode node = lookup.lookup(DriverNode.class);
        
        if (node != null) {
            JDBCDriver driver = node.getDatabaseDriver().getJDBCDriver();
            new NewConnectionDialogDisplayer().showDialog(driver, null, null, null);
        }
    }
    
    public static final class NewConnectionDialogDisplayer extends ConnectionDialogMediator {
        
        // the most recent task passed to the RequestProcessor
        Task activeTask = null;

        public DatabaseConnection showDialog(JDBCDriver driver, String databaseUrl, String user, String password) {
            return AddConnectionWizard.showWizard(driver, databaseUrl, user, password);
        }

        @Override
        protected Task retrieveSchemasAsync(final SchemaPanel schemaPanel, final DatabaseConnection dbcon, final String defaultSchema)
        {
            activeTask = super.retrieveSchemasAsync(schemaPanel, dbcon, defaultSchema);
            
            return activeTask;
        }
        
        @Override
        protected boolean retrieveSchemas(SchemaPanel schemaPanel, DatabaseConnection dbcon, String defaultSchema) {
            fireConnectionStep(NbBundle.getMessage (ConnectUsingDriverAction.class, "ConnectionProgress_Schemas")); // NOI18N
            List<String> schemas = new ArrayList<String>();
            try {
                DatabaseMetaData dbMetaData = dbcon.getJDBCConnection().getMetaData();
                if (dbMetaData.supportsSchemasInTableDefinitions()) {
                    ResultSet rs = dbMetaData.getSchemas();
                    if (rs != null) {
                        while (rs.next()) {
                            schemas.add(rs.getString(1).trim());
                        }
                    }
                }
            } catch (SQLException exc) {
                String message = NbBundle.getMessage(ConnectUsingDriverAction.class, "ERR_UnableObtainSchemas", exc.getMessage()); // NOI18N
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
            }
            return schemaPanel.setSchemas(schemas, defaultSchema);
        }
    }
}
