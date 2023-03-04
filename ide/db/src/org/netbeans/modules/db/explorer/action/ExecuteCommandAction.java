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

import java.util.HashSet;
import java.util.Set;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.explorer.sql.editor.SQLEditorSupport;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 * Open an sql editor as an sql console.
 * 
 * @author Rob Englander
 */
@ActionID(id = "org.netbeans.modules.db.explorer.action.ExecuteCommandAction", category = "Database")
@ActionRegistration(displayName = "#ExecuteCommand", lazy = false)
@ActionReferences(value = {
    @ActionReference(path = "Databases/Explorer/Connection/Actions", position = 250),
    @ActionReference(path = "Databases/Explorer/TableList/Actions", position = 250),
    @ActionReference(path = "Databases/Explorer/SystemTableList/Actions", position = 250),
    @ActionReference(path = "Databases/Explorer/Table/Actions", position = 125),
    @ActionReference(path = "Databases/Explorer/ViewList/Actions", position = 250),
    @ActionReference(path = "Databases/Explorer/View/Actions", position = 200),
    @ActionReference(path = "Databases/Explorer/ProcedureList/Actions", position = 250),
    @ActionReference(path = "Databases/Explorer/Procedure/Actions", position = 100),
    @ActionReference(path = "Databases/Explorer/ProcedureParam/Actions", position = 100),
    @ActionReference(path = "Databases/Explorer/IndexList/Actions", position = 300),
    @ActionReference(path = "Databases/Explorer/Index/Actions", position = 250),
    @ActionReference(path = "Databases/Explorer/ForeignKeyList/Actions", position = 100),
    @ActionReference(path = "Databases/Explorer/ForeignKey/Actions", position = 100),
    @ActionReference(path = "Databases/Explorer/Column/Actions", position = 200),
    @ActionReference(path = "Databases/Explorer/IndexColumn/Actions", position = 100),
    @ActionReference(path = "Databases/Explorer/ForeignKeyColumn/Actions", position = 100)})
public class ExecuteCommandAction extends BaseAction {

    @Override
    protected boolean enable(Node[] activatedNodes) {
        return true;
    }

    @Override
    public void performAction(Node[] activatedNodes) {
        // If exactly one DatabaseConnection can be detected via the activated
        // nodes, that database connection is set as the default connection
        Set<DatabaseConnection> dbconnSet = new HashSet<>();
        if (activatedNodes != null) {
            for (Node node : activatedNodes) {
                DatabaseConnection dbconn = node.getLookup().lookup(DatabaseConnection.class);
                if (dbconn != null) {
                    dbconnSet.add(dbconn);
                }
            }
        }
        if (dbconnSet.size() == 1) {
            DatabaseConnection dbconn = dbconnSet.iterator().next();
            SQLEditorSupport.openSQLEditor(dbconn.getDatabaseConnection(), "", false); // NOI18N
        } else {
            SQLEditorSupport.openSQLEditor(null, "", false); // NOI18N
        }
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(ExecuteCommandAction.class, "ExecuteCommand"); // NOI18N
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(ExecuteCommandAction.class);
    }
}
