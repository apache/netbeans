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

package org.netbeans.modules.derby;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.db.explorer.ConnectionListener;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.DatabaseException;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 * Connect to a database
 * 
 * @author Jiri Rechtacek
 */
public class ConnectDatabaseAction extends NodeAction {
    private static final Logger LOGGER = Logger.getLogger(ConnectDatabaseAction.class.getName());

    public ConnectDatabaseAction() {
        putValue("noIconInMenu", Boolean.TRUE);
    }    
        
    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    public String getName() {
        return NbBundle.getMessage(ConnectDatabaseAction.class, "ConnectDatabaseAction_ConnectAction");
    }

    @Override
    public boolean enable(Node[] activatedNodes) {
        return activatedNodes != null && activatedNodes.length == 1;
    }


    @Override
    protected void performAction(Node[] activatedNodes) {
        if ( activatedNodes == null || activatedNodes.length == 0 ) {
            return;
        }
        Node n = activatedNodes[0];

        final String dbname = n.getName();

        List<DatabaseConnection> conns = DerbyDatabasesImpl.getDefault().findDatabaseConnections(dbname);

        try {
            if ( conns.isEmpty() )
            {
                JDBCDriver drivers[] = JDBCDriverManager.getDefault().getDrivers(DerbyOptions.DRIVER_CLASS_NET);
                if (drivers.length == 0) {
                    showDriverNotFoundDialog();
                    return;
                }
                final DatabaseConnection dbconn = DatabaseConnection.create(drivers[0], "jdbc:derby://localhost:" + // NOI18N
                        RegisterDerby.getDefault().getPort() +
                        "/" + dbname, // NOI18N
                        DerbyDatabasesImpl.getDefault().getUser(dbname),
                        DerbyDatabasesImpl.getDefault().getSchema(dbname),
                        DerbyDatabasesImpl.getDefault().getPassword(dbname),
                        true);

                // Can't display the dialog until the connection has been succesfully added
                // to the database explorer.
                ConnectionManager.getDefault().addConnectionListener(new ConnectionListener() {
                    @Override
                    public void connectionsChanged() {
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
        } finally {
            // Refresh in case the state of the server changed... (e.g. the connection was lost)
        }
    }

    /**
     * If Derby driver cannot be found, show info message and ask user whether
     * they want to open the Add Driver dialog.
     *
     * See bug #225609.
     */
    private void showDriverNotFoundDialog() {
        String msg = NbBundle.getMessage(ConnectDatabaseAction.class,
                "ERR_DerbyDriverNotFoundConfigure", //NOI18N
                DerbyOptions.DRIVER_DISP_NAME_NET);
        NotifyDescriptor nd = new NotifyDescriptor.Confirmation(
                msg, NotifyDescriptor.YES_NO_OPTION);
        DialogDisplayer.getDefault().notify(nd);
        if (NotifyDescriptor.YES_OPTION.equals(nd.getValue())) {
            JDBCDriverManager.getDefault().showAddDriverDialog();
        }
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(ConnectDatabaseAction.class);
    }

}
