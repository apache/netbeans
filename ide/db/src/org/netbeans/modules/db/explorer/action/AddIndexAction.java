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

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.lib.ddl.impl.DriverSpecification;
import org.netbeans.lib.ddl.impl.Specification;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.explorer.DatabaseConnector;
import org.netbeans.modules.db.explorer.DbUtilities;
import org.netbeans.modules.db.explorer.dlg.AddIndexDialog;
import org.netbeans.modules.db.explorer.node.IndexListNode;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Rob Englander
 */
public class AddIndexAction extends BaseAction {
    private static final Logger LOGGER = Logger.getLogger(AddIndexAction.class.getName());

    @Override
    public String getName() {
        return NbBundle.getMessage (AddIndexAction.class, "AddIndex"); // NOI18N
    }


    @Override
    protected boolean enable(Node[] activatedNodes) {
        boolean enabled = false;

        if (activatedNodes.length == 1) {
            IndexListNode node = activatedNodes[0].getLookup().lookup(IndexListNode.class);
            enabled = node != null;
        }

        return enabled;
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
        final IndexListNode node = activatedNodes[0].getLookup().lookup(IndexListNode.class);
        RequestProcessor.getDefault().post(
            new Runnable() {
            @Override
                public void run() {
                    perform(node);
                }
            }
        );
    }

    private void perform(final IndexListNode node) {
        try {
            DatabaseConnection dbConn = node.getLookup().lookup(DatabaseConnection.class);
            DatabaseConnector connector = dbConn.getConnector();

            final String tablename = node.getTableName();
            String schemaName = node.getSchemaName();
            String catalogName = node.getCatalogName();

            if (schemaName == null) {
                schemaName = catalogName;
            }

            if (catalogName == null) {
                catalogName = schemaName;
            }

            Specification spec = connector.getDatabaseSpecification();

            final DriverSpecification drvSpec = connector.getDriverSpecification(catalogName);

            // List columns not present in current index
            List<String> cols = new ArrayList<String> (5);

            drvSpec.getColumns(tablename, "%");
            ResultSet rs = drvSpec.getResultSet();
            Map<Integer, String> rset = new HashMap<Integer, String>();
            while (rs.next()) {
                rset = drvSpec.getRow();
                cols.add(rset.get(Integer.valueOf(4)));
                rset.clear();
            }
            rs.close();

            if (cols.isEmpty())
                throw new Exception(NbBundle.getMessage (AddIndexAction.class, "EXC_NoUsableColumnInPlace")); // NOI18N

            // Create and execute command
            final AddIndexDialog dlg = new AddIndexDialog(cols, spec, tablename, schemaName);
            dlg.setIndexName(tablename + "_idx"); // NOI18N
            if (dlg.run()) {
                RequestProcessor.getDefault().post(new Runnable() {
                    @Override
                    public void run() {
                        Node refreshNode = node.getParentNode();
                        if (refreshNode == null) {
                            refreshNode = node;
                        }

                        SystemAction.get(RefreshAction.class).performAction(new Node[] { refreshNode } );
                    }
                });
            }
        } catch(Exception exc) {
            LOGGER.log(Level.INFO, exc.getLocalizedMessage(), exc);
            DbUtilities.reportError(NbBundle.getMessage (AddIndexAction.class, "ERR_UnableToAddIndex"), exc.getMessage()); // NOI18N
        }
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(AddIndexAction.class);
    }
}
