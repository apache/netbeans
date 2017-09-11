/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
