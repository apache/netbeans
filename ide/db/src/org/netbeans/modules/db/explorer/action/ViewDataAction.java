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
import org.netbeans.modules.db.explorer.sql.editor.SQLEditorSupport;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Rob Englander
 */
public class ViewDataAction extends QueryAction {
    private static final RequestProcessor RP = new RequestProcessor(ViewDataAction.class.getName(), 1);

    @Override
    public String getName() {
        return NbBundle.getMessage (ViewDataAction.class, "ViewData"); // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(ViewDataAction.class);
    }

    @Override
    public void performAction (final Node[] activatedNodes) {
        final DatabaseConnection connection = activatedNodes[0].getLookup().lookup(DatabaseConnection.class);
        if (connection != null) {
            RP.post(
                new Runnable() {
                    @Override
                    public void run() {
                        String expression = null;
                        try {
                            expression = getDefaultQuery(activatedNodes);
                            SQLEditorSupport.openSQLEditor(connection.getDatabaseConnection(), expression + ";\n", true); //NOI18N
                        } catch(Exception exc) {
                            Logger.getLogger(ViewDataAction.class.getName()).log(Level.INFO, exc.getLocalizedMessage() + " while executing expression " + expression, exc); // NOI18N
                            String message = NbBundle.getMessage (ViewDataAction.class, "ShowDataError", exc.getMessage()); // NOI18N
                            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
                        }
                    }
                }
            );
        }
    }

}
