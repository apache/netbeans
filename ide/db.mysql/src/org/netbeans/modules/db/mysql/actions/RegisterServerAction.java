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

package org.netbeans.modules.db.mysql.actions;

import org.netbeans.modules.db.mysql.DatabaseServer;
import org.netbeans.modules.db.mysql.DatabaseServerManager;
import org.netbeans.modules.db.mysql.impl.ServerNodeProvider;
import org.netbeans.modules.db.mysql.ui.PropertiesDialog;
import org.netbeans.modules.db.mysql.util.Utils;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;

/**
 * The ation to register the MySQL Server Provider.  
 * 
 * @author David Van Couvering
 */
public class RegisterServerAction extends NodeAction {
    
    @Override
    public String getName() {
        return Utils.getBundle().getString("LBL_RegisterServerAction");
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }
    
    @Override
    protected void performAction(Node[] activatedNodes) {
        DatabaseServer server = DatabaseServerManager.getDatabaseServer();
        PropertiesDialog dlg = new PropertiesDialog(server);
        dlg.displayDialog();
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        return !ServerNodeProvider.getDefault().isRegistered();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(RegisterServerAction.class);
    }
}
