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
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.explorer.DbUtilities;
import org.netbeans.modules.db.explorer.dlg.AddTableColumnDialog;
import org.netbeans.modules.db.explorer.node.TableNode;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author Rob Englander
 */
public class AddColumnAction extends BaseAction {
    private static final Logger LOGGER = Logger.getLogger(AddColumnAction.class.getName());

    @Override
    public String getName() {
        return NbBundle.getMessage (AddColumnAction.class, "AddColumn"); // NOI18N
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        boolean result = false;

        if (activatedNodes.length == 1) {
            TableNode tn = activatedNodes[0].getLookup().lookup(TableNode.class);

            if (tn != null && (!tn.isSystem())) {
                result = true;
            }
        }
        return result;
    }

    @Override
    protected void performAction(final Node[] activatedNodes) {
        RequestProcessor.getDefault().post(
            new Runnable() {
            @Override
                public void run() {
                    final TableNode node = activatedNodes[0].getLookup().lookup(TableNode.class);
                    final DatabaseConnection connection = node.getLookup().lookup(DatabaseConnection.class);

                    try {
                        boolean columnAdded = AddTableColumnDialog.showDialogAndCreate(connection.getConnector().getDatabaseSpecification(), node);
                        if (columnAdded) {
                            SystemAction.get(RefreshAction.class).performAction(new Node[]{node});
                        }
                    } catch(Exception exc) {
                        LOGGER.log(Level.WARNING, exc.getLocalizedMessage(), exc);
                        DbUtilities.reportError(NbBundle.getMessage (AddColumnAction.class, "ERR_UnableToAddColumn"), exc.getMessage()); // NOI18N
                    }
                }
            }
        );
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(AddColumnAction.class);
    }

}
