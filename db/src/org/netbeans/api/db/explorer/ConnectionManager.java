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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.api.db.explorer;

import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.lib.ddl.DBConnection;
import org.netbeans.modules.db.explorer.ConnectionList;
import org.netbeans.modules.db.explorer.action.ConnectUsingDriverAction;
import org.netbeans.modules.db.runtime.DatabaseRuntimeManager;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;

/**
 * Provides access to the list of connections in the Database Explorer.
 *
 * <p>The list of connections can be retrieved using the {@link #getConnections}
 * method. A connection can be also retrieved by name using the
 * {@link #getConnection} method.</p>
 *
 * <p>New connections can be added to the Connection Manager using the
 * {@link #addConnection} method (new connections can be created using the
 * {@link DatabaseConnection#create} method.
 * It is also possible to display the New Database Connection dialog to let the
 * user create a new database connection using the {@link #showAddConnectionDialog}.
 * Connections can be realized using the {@link #showConnectionDialog} method.</p>
 *
 * <p>Clients can be informed of changes to the ConnectionManager by registering
 * a {@link ConnectionListener} using the {@link #addConnectionListener} method.</p>
 *
 * @see DatabaseConnection
 *
 * @author Andrei Badea
 */
public final class ConnectionManager {

    private static final Logger LOGGER = Logger.getLogger((ConnectionManager.class.getName()));

    /**
     * The ConnectionManager singleton instance.
     */
    private static ConnectionManager DEFAULT;

    /**
     * Gets the ConnectionManager singleton instance.
     */
    public static synchronized ConnectionManager getDefault() {
        if (DEFAULT == null) {
            // init runtimes
            DatabaseRuntimeManager.getDefault().getRuntimes();
            DEFAULT = new ConnectionManager();
        }
        return DEFAULT;
    }

    /**
     * Returns the list of connections in the Database Explorer.
     *
     * @return a non-null array of connections.
     */
    public DatabaseConnection[] getConnections() {
        DBConnection[] conns = ConnectionList.getDefault().getConnections();
        DatabaseConnection[] dbconns = new DatabaseConnection[conns.length];
        for (int i = 0; i < conns.length; i++) {
            dbconns[i] = ((org.netbeans.modules.db.explorer.DatabaseConnection)conns[i]).getDatabaseConnection();
        }
        return dbconns;
    }

    /**
     * Returns the connection with the specified name.
     *
     * @param name the connection name
     *
     * @throws NullPointerException if the specified database name is null.
     */
    public DatabaseConnection getConnection(String name) {
        if (name == null) {
            throw new NullPointerException();
        }
        DBConnection[] conns = ConnectionList.getDefault().getConnections();
        for (int i = 0; i < conns.length; i++) {
            DatabaseConnection dbconn = ((org.netbeans.modules.db.explorer.DatabaseConnection)conns[i]).getDatabaseConnection();
            if (name.equals(dbconn.getName())) {
                return dbconn;
            }
        }
        return null;
    }

    /**
     * Adds a new connection to Database Explorer. This method does not display any UI and
     * does not try to connect to the respective database.
     *
     * @param dbconn the connection to be added; must not be null.
     *
     * @throws NullPointerException if dbconn is null.
     * @throws DatabaseException if an error occurs while adding the connection.
     */
    public void addConnection(DatabaseConnection dbconn) throws DatabaseException {
        if (dbconn == null) {
            throw new NullPointerException();
        }

        ConnectionList.getDefault().add(dbconn.getDelegate());
    }

    /**
     * Connects this connection to the database <b>without opening any
     * dialog</b>.  If not all the necessary parameters, such as the user name or password,
     * are set, the method will silently return <code>false</code>.
     *
     * <p>The connection is made synchronously in the calling thread, which must not
     * be the AWT event dispatching thread.</p>
     *
     * @param dbconn the database connection to be connected.
     * @return false if not all parameters necessary to connect are available.
     *
     * @throws NullPointerException if the dbconn parameter is null.
     * @throws DatabaseException if an error occurs while connecting.
     * @throws IllegalStateException if this connection is not added to the
     *         ConnectionManager or the calling thread is the AWT event dispatching thread.
     *
     * @since 1.26
     */
    public boolean connect(DatabaseConnection dbconn) throws DatabaseException {
        if (dbconn == null) {
            throw new NullPointerException();
        }

        if (!ConnectionList.getDefault().contains(dbconn.getDelegate())) {
            throw new IllegalStateException("This connection is not added to the ConnectionManager."); // NOI18N
        }

        // Password can be empty
        if (isEmpty(dbconn.getUser()) || isEmpty(dbconn.getDatabaseURL())) {
            return false;
        }

        if (SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("This method can not be called on the event dispatch thread."); // NOI18N
        }

        if (dbconn.getDelegate().isVitalConnection()) {
            return true;
        }

        dbconn.getDelegate().connectSync();
        return true;
    }

    private static boolean isEmpty(String value) {
        return (value == null || value.trim().length() == 0);
    }

    /**
     * Remove an existing connection from the Database Explorer.  This method
     * unregisters the connection from the the explorer so it will
     * no longer appear as a connection in the UI.  This method also closes
     * the underlying JDBC connection if it is open.
     *
     * @param dbconn the connection to be removed
     *
     * @since 1.25
     */
    public void removeConnection(DatabaseConnection dbconn) throws DatabaseException {
        if ( dbconn == null ) {
            throw new NullPointerException();
        }

        ConnectionList.getDefault().remove(dbconn.getDelegate());
    }

    /**
     * Shows the dialog for adding a new connection. The specified driver will be
     * selected by default in the New Database Connection dialog.
     *
     * @param driver the JDBC driver; can be null.
     */
    public void showAddConnectionDialog(JDBCDriver driver) {
        showAddConnectionDialog(driver, null, null, null);
    }

    /**
     * Shows the dialog for adding a new connection with the specified database URL.
     * The specified driver be filled as the single element of the
     * Driver combo box of the New Database Connection dialog box.
     * The database URL will be filled in the Database URL field in the
     * New Database Connection dialog box.
     *
     * @param driver the JDBC driver; can be null.
     * @param databaseUrl the database URL; can be null.
     */
    public void showAddConnectionDialog(JDBCDriver driver, final String databaseUrl) {
        showAddConnectionDialog(driver, databaseUrl, null, null);
    }

    /**
     * Shows the dialog for adding a new connection with the specified database URL, user and password
     * The specified driver be filled as the single element of the
     * Driver combo box of the New Database Connection dialog box.
     * The database URL will be filled in the Database URL field in the
     * New Database Connection dialog box.
     * The user and password will be filled in the User Name and Password
     * fields in the New Database Connection dialog box.
     *
     * @param driver the JDBC driver; can be null.
     * @param databaseUrl the database URL; can be null.
     * @param user the database user; can be null.
     * @param password user's password; can be null.
     *
     * @since 1.19
     */
    public void showAddConnectionDialog(final JDBCDriver driver, final String databaseUrl, final String user, final String password) {
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run() {
                new ConnectUsingDriverAction.NewConnectionDialogDisplayer().showDialog(driver, databaseUrl, user, password);
            }
        });
    }

    /**
     * The counterpart of {@link #showAddConnectionDialog(JDBCDriver) } which returns
     * the newly created database connection, but must be called from the event dispatching
     * thread.
     *
     * @param driver the JDBC driver; can be null.
     *
     * @return the new database connection or null if no database connection
     *         was created (e.g. the user pressed Cancel).
     *
     * @throws IllegalStateException if the calling thread is not the event
     *         dispatching thread.
     *
     * @since 1.19
     */
    public DatabaseConnection showAddConnectionDialogFromEventThread(JDBCDriver driver) {
        return showAddConnectionDialogFromEventThread(driver, null, null, null);
    }

    /**
     * The counterpart of {@link #showAddConnectionDialog(JDBCDriver, String) } which returns
     * the newly created database connection, but must be called from the event dispatching
     * thread.
     *
     * @param driver the JDBC driver; can be null.
     * @param databaseUrl the database URL; can be null.
     *
     * @return the new database connection or null if no database connection
     *         was created (e.g. the user pressed Cancel).
     *
     * @throws IllegalStateException if the calling thread is not the event
     *         dispatching thread.
     *
     * @since 1.19
     */
    public DatabaseConnection showAddConnectionDialogFromEventThread(JDBCDriver driver, String databaseUrl) {
        return showAddConnectionDialogFromEventThread(driver, databaseUrl, null, null);
    }

    /**
     * The counterpart of {@link #showAddConnectionDialog(JDBCDriver, String, String, String) }
     * which returns the newly created database connection, but must be called
     * from the event dispatching thread.
     *
     * @param driver the JDBC driver; can be null.
     * @param databaseUrl the database URL; can be null.
     * @param user the database user; can be null.
     * @param password user's password; can be null.
     *
     * @return the new database connection or null if no database connection
     *         was created (e.g. the user pressed Cancel).
     *
     * @throws IllegalStateException if the calling thread is not the event
     *         dispatching thread.
     *
     * @since 1.19
     */
    public DatabaseConnection showAddConnectionDialogFromEventThread(JDBCDriver driver, String databaseUrl, String user, String password) {
        if (!SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("The current thread is not the event dispatching thread."); // NOI18N
        }
        org.netbeans.modules.db.explorer.DatabaseConnection internalDBConn = new ConnectUsingDriverAction.NewConnectionDialogDisplayer().showDialog(driver, databaseUrl, user, password);
        if (internalDBConn != null) {
            return internalDBConn.getDatabaseConnection();
        }
        return null;
    }

    /**
     * Shows the Connect dialog for the specified connection if not all data
     * needed to connect, such as the user name or password,
     * are known), or displays a modal progress dialog and attempts
     * to connect to the database immediately.
     *
     * @param dbconn the database connection to be connected
     *
     * @throws NullPointerException if the dbconn parameter is null
     * @throws IllegalStateException if this connection is not added to the
     *         ConnectionManager.
     */
    public void showConnectionDialog(DatabaseConnection dbconn) {
        if (dbconn == null) {
            throw new NullPointerException();
        }
        if (!ConnectionList.getDefault().contains(dbconn.getDelegate())) {
            throw new IllegalStateException("This connection is not added to the ConnectionManager."); // NOI18N
        }
        dbconn.getDelegate().showConnectionDialog();
    }

    /**
     * Disconnects this connection from the database. Does not do anything
     * if not connected.
     *
     * @param dbconn the database connection to be connected
     *
     * @throws NullPointerException if the dbconn parameter is null
     * @throws IllegalStateException if this connection is not added to the
     *         ConnectionManager.
     */
    public void disconnect(DatabaseConnection dbconn) {
        if (dbconn == null) {
            throw new NullPointerException();
        }
        if (!ConnectionList.getDefault().contains(dbconn.getDelegate())) {
            throw new IllegalStateException("This connection is not added to the ConnectionManager."); // NOI18N
        }
        try {
            dbconn.getDelegate().disconnect();
        } catch (DatabaseException e) {
            // XXX maybe shouldn't catch the exception
            Exceptions.printStackTrace(e);
        }
    }

    /**
     * Selects the node corresponding to the specified connection in the
     * Runtime tab.
     *
     * @param dbconn the connection to select
     *
     * @throws NullPointerException if the dbconn parameter is null
     * @throws IllegalStateException if this connection is not added to the
     *         ConnectionManager.
     */
    public void selectConnectionInExplorer(DatabaseConnection dbconn) {
        if (dbconn == null) {
            throw new NullPointerException();
        }
        if (!ConnectionList.getDefault().contains(dbconn.getDelegate())) {
            throw new IllegalStateException("This connection is not added to the ConnectionManager."); // NOI18N
        }
        dbconn.getDelegate().selectInExplorer();
    }

    /**
     * Refresh the node corresponding to the specified connection in the
     * Runtime tab.
     *
     * @param dbconn the connection to select
     *
     * @throws NullPointerException if the dbconn parameter is null
     * @throws IllegalStateException if this connection is not added to the
     *         ConnectionManager.
     * @since 1.40
     */
    public void refreshConnectionInExplorer(DatabaseConnection dbconn) {
        if (dbconn == null) {
            throw new NullPointerException();
        }
        if (!ConnectionList.getDefault().contains(dbconn.getDelegate())) {
            throw new IllegalStateException("This connection is not added to the ConnectionManager."); // NOI18N
        }
        try {
            dbconn.getDelegate().refreshInExplorer();
        } catch (DatabaseException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * Registers a ConnectionListener.
     */
    public void addConnectionListener(ConnectionListener listener) {
        ConnectionList.getDefault().addConnectionListener(listener);
    }

    /**
     * Unregisters the specified connection listener.
     */
    public void removeConnectionListener(ConnectionListener listener) {
        ConnectionList.getDefault().removeConnectionListener(listener);
    }
}
