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

import org.netbeans.modules.db.mysql.util.DatabaseUtils;
import org.netbeans.modules.db.mysql.*;
import org.netbeans.modules.db.mysql.DatabaseServer;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.db.explorer.ConnectionListener;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.modules.db.mysql.util.Utils;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;

/**
 * Connect to a database
 * 
 * @author David Van Couvering
 */
public class ConnectAction extends CookieAction {
    private static final Logger LOGGER = Logger.getLogger(ConnectAction.class.getName());
    private static final Class[] COOKIE_CLASSES = new Class[] {
        Database.class
    };

    public ConnectAction() {
        putValue("noIconInMenu", Boolean.TRUE);
    }    
        
    protected boolean asynchronous() {
        return false;
    }

    public String getName() {
        return Utils.getBundle().getString("LBL_ConnectAction");
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(ConnectAction.class);
    }
    
    @Override
    public boolean enable(Node[] activatedNodes) {
        return true;
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
        if ( activatedNodes == null || activatedNodes.length == 0 ) {
            return;
        }
        Database model = activatedNodes[0].getCookie(Database.class);
        if(model == null) {
            return;
        }
        DatabaseServer server = model.getServer();
        
        final String dbname = model.getDbName();

        List<DatabaseConnection> conns = DatabaseUtils.findDatabaseConnections(server.getURL(dbname));

        try {
            if ( conns.size() == 0 )
            {
                final DatabaseConnection dbconn = DatabaseConnection.create(DatabaseUtils.getJDBCDriver(),
                        server.getURL(dbname), server.getUser(), null,
                        server.isSavePassword() ? server.getPassword() : null,
                        server.isSavePassword());

                // Can't display the dialog until the connection has been succesfully added
                // to the database explorer.
                ConnectionManager.getDefault().addConnectionListener(new ConnectionListener() {
                    public void connectionsChanged() {
                        if (ConnectionManager.getDefault().getConnection(
                                dbconn.getName()) == null) {
                            return; // this is not the right event, see #217880
                        }
                        ConnectionManager.getDefault().showConnectionDialog(dbconn);
                        ConnectionManager.getDefault().removeConnectionListener(this);
                    }
                });

                ConnectionManager.getDefault().addConnection(dbconn);
            } else {
                ConnectionManager.getDefault().showConnectionDialog(conns.get(0));
            }

        } catch (DatabaseException dbe) {
            LOGGER.log(Level.INFO, dbe.getMessage(), dbe);
            Utils.displayErrorMessage(NbBundle.getMessage(ConnectAction.class,
                    "MSG_FailureConnecting", dbname, dbe.getMessage()));
        } finally {
            // Refresh in case the state of the server changed... (e.g. the connection was lost)
            server.refreshDatabaseList();
        }
    }
}
