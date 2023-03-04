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

import java.util.logging.Logger;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.modules.db.mysql.DatabaseServer;
import org.netbeans.modules.db.mysql.ui.CreateDatabasePanel;
import org.netbeans.modules.db.mysql.util.Utils;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.CookieAction;

/**
 *
 * @author David Van Couvering
 */
public class CreateDatabaseAction extends CookieAction {
    private static Logger LOGGER = Logger.getLogger(CreateDatabaseAction.class.getName());

    private static final Class[] COOKIE_CLASSES = new Class[] {
        DatabaseServer.class
    };

    public CreateDatabaseAction() {
        putValue("noIconInMenu", Boolean.TRUE);
    }    
        
    protected boolean asynchronous() {
        return false;
    }

    public String getName() {
        return Utils.getBundle().getString("LBL_CreateDatabaseAction");
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(CreateDatabaseAction.class);
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if ( activatedNodes.length == 0 ) {
            return false;
        }
        
        Node node = activatedNodes[0];
        
        DatabaseServer server = node.getCookie(DatabaseServer.class);
        if ( server != null && server.isConnected() ) {
            return true;
        }
        
        return false;
    }

    @Override
    protected int mode() {
        return MODE_EXACTLY_ONE;
    }

    @Override
    protected Class<?>[] cookieClasses() {
        return COOKIE_CLASSES;
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
        Node node = activatedNodes[0];

        final DatabaseServer server = node.getCookie(DatabaseServer.class);
        try {
            CreateDatabasePanel panel = new CreateDatabasePanel(server);
            panel.showCreateDatabaseDialog();
        } catch (DatabaseException dbe) {
            Utils.displayErrorMessage(dbe.getMessage());
        }
    }
}
