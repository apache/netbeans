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

import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.lib.ddl.DBConnection;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionState;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author sdedic
 */
@ActionRegistration(
        displayName = "#SetPreferred", 
        lazy = false,
        enabledOn = @ActionState(type = DatabaseConnection.class)
)
@ActionID(category = "Database", id = "netbeans.db.explorer.action.makepreferred")
@ActionReference(path = "Databases/Explorer/Connection/Actions", position = 160)
@NbBundle.Messages({
    "SetPreferred=Make Preferred Connection"
})
public class MakePreferred extends BaseAction {

    @Override
    protected void performAction(Node[] activatedNodes) {
        Node n = activatedNodes[0];
        DBConnection c = n.getLookup().lookup(DBConnection.class);
        ConnectionManager.getDefault().setPreferredConnection(((DatabaseConnection)c).getDatabaseConnection());
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes.length != 1) {
            return false;
        }
        Node n = activatedNodes[0];
        DBConnection c = n.getLookup().lookup(DBConnection.class);
        org.netbeans.api.db.explorer.DatabaseConnection prefC = ConnectionManager.getDefault().getPreferredConnection(true);
        return c != null && (prefC == null || !prefC.getName().equals(c.getName()));
    }

    @Override
    public String getName() {
        return Bundle.SetPreferred();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(MakePreferred.class);
    }
    
}
