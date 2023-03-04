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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.MissingResourceException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.lib.ddl.CommandNotSupportedException;
import org.netbeans.lib.ddl.DDLException;
import org.netbeans.lib.ddl.impl.Specification;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.explorer.DatabaseConnector;
import org.netbeans.modules.db.explorer.node.CatalogNode;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Rob Englander
 */
public class MakeDefaultCatalogAction extends BaseAction {
    private static final Logger LOGGER = Logger.getLogger(MakeDefaultCatalogAction.class.getName());

    @Override
    public String getName() {
        return NbBundle.getMessage (MakeDefaultCatalogAction.class, "MakeDefaultCatalog"); // NOI18N
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        boolean result = false;

        if (activatedNodes.length == 1) {
            CatalogNode node = activatedNodes[0].getLookup().lookup(CatalogNode.class);
            if (node != null) {
                DatabaseConnector connector = node.getLookup().lookup(DatabaseConnection.class).getConnector();
                result = connector.supportsCommand(Specification.DEFAULT_DATABASE);
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
                    DatabaseConnection connection = activatedNodes[0].getLookup().lookup(DatabaseConnection.class);
                    String name = activatedNodes[0].getLookup().lookup(CatalogNode.class).getName();

                    try {
                        connection.setDefaultCatalog(name);
                    } catch (CommandNotSupportedException ex) {
                        Exceptions.printStackTrace(ex);
                    } catch (DDLException e) {
                        try {
                            handleDLLException(connection, e);
                        } catch (SQLException ex) {
                            Exceptions.printStackTrace(e);
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            }
        );
    }

    /**
     * If DDL exception was caused by a closed connection, log info and display
     * a simple error dialog. Otherwise let users report the exception.
     */
    private void handleDLLException(DatabaseConnection dbConn,
            DDLException e) throws SQLException, MissingResourceException {
        Connection conn = dbConn == null ? null : dbConn.getJDBCConnection();
        if (conn != null && !conn.isValid(1000)) {
            LOGGER.log(Level.INFO, e.getMessage(), e);
            NotifyDescriptor nd = new NotifyDescriptor.Message(
                    NbBundle.getMessage(
                    MakeDefaultCatalogAction.class,
                    "ERR_ConnectionToServerClosed"), //NOI18N
                    NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notifyLater(nd);
        } else {
            Exceptions.printStackTrace(e);
        }
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(MakeDefaultCatalogAction.class);
    }


}
