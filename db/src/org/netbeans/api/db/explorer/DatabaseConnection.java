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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2010 Sun
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

import java.sql.Connection;
import java.sql.Driver;
import java.util.Properties;
import javax.swing.SwingUtilities;
import org.netbeans.modules.db.explorer.ConnectionList;
import org.netbeans.modules.db.explorer.DatabaseConnectionAccessor;

/**
 * Encapsulates a database connection. Each DatabaseConnection instance
 * represents a connection to a database in the Database Explorer.
 *
 * <p>This class provides access to the properties of a database connection,
 * such as the connection name, database URL, user or default schema. New connections
 * can be created using the {@link #create} method (these connections can be
 * added to the Database Explorer using the 
 * {@link ConnectionManager#addConnection} method.</p>
 * 
 * <p>It is also possible to retrieve the JDBC {@link java.sql.Connection}
 * using the {@link #getJDBCConnection} method (the connection can be connected
 * or disconnected using the {@link ConnectionManager#showConnectionDialog} 
 * and {@link ConnectionManager#disconnect} methods.</p>
 * 
 * @author Andrei Badea
 *
 * @see ConnectionManager
 */
public final class DatabaseConnection {
    
    private org.netbeans.modules.db.explorer.DatabaseConnection delegate;

    /*
     * DatabaseConnection's methods delegate to 
     * org.netbeans.modules.db.explorer.DatabaseConnection. Each instance of
     * org.netbeans.modules.db.explorer.DatabaseConnection
     * creates and maintains an instance of
     * DatabaseConnection. Since the constructor of 
     * DatabaseConnection is package-protected, an accessor is needed
     * to create instances of DatabaseConnection from 
     * org.netbeans.modules.db.explorer.DatabaseConnection.
     *
     * See org.netbeans.modules.db.explorer.DatabaseConnectionAccessor
     */ 
    
    static {
        DatabaseConnectionAccessor.DEFAULT = new DatabaseConnectionAccessor() {
            @Override
            public DatabaseConnection createDatabaseConnection(org.netbeans.modules.db.explorer.DatabaseConnection conn) {
                return new DatabaseConnection(conn);
            }    
        };
    }
    
    /**
     * Package-protected constructor.
     */
    DatabaseConnection(org.netbeans.modules.db.explorer.DatabaseConnection delegate) {
        assert delegate != null;
        this.delegate = delegate;
    }

    /**
     * Returns the org.netbeans.modules.db.explorer.DatabaseConnection which this instance delegates to.
     */
    org.netbeans.modules.db.explorer.DatabaseConnection getDelegate() {
        return delegate;
    }
    
    /**
     * Creates a new DatabaseConnection instance w/ a default display name based
     * on connection URL and user
     * 
     * @param driver the JDBC driver the new connection uses; cannot be null.
     * @param databaseURL the URL of the database to connect to; cannot be null.
     * @param user the username.
     * @param schema the schema to use, or null for the default schema
     * @param password the password.
     * @param rememberPassword whether to remember the password for the current session.
     *
     * @return the new instance.
     *
     * @throws NullPointerException if driver or database are null.
     */
    public static DatabaseConnection create(JDBCDriver driver, String databaseURL, 
            String user, String schema, String password, boolean rememberPassword) {
            return create(driver, databaseURL, user, schema, password, rememberPassword, null);
    }

    /**
     * Creates a new DatabaseConnection instance.
     *
     * @param driver the JDBC driver the new connection uses; cannot be null.
     * @param databaseURL the URL of the database to connect to; cannot be null.
     * @param user the username.
     * @param schema the schema to use, or null for the default schema
     * @param password the password.
     * @param rememberPassword whether to remember the password for the current session.
     * @param displayName the display name of the connection as it shows under the Databases node
     *
     * @return the new instance.
     *
     * @since 1.42
     * @throws NullPointerException if driver or database are null.
     */
    public static DatabaseConnection create(JDBCDriver driver, String databaseURL, 
            String user, String schema, String password, boolean rememberPassword,
            String displayName) {
        return create(driver, databaseURL, user, schema, password,
                rememberPassword, displayName, null);
    }

    /**
     * Creates a new DatabaseConnection instance.
     *
     * @param driver the JDBC driver the new connection uses; cannot be null.
     * @param databaseURL the URL of the database to connect to; cannot be null.
     * @param user the username.
     * @param schema the schema to use, or null for the default schema
     * @param password the password.
     * @param rememberPassword whether to remember the password for the current
     * session.
     * @param displayName the display name of the connection as it shows under
     * the Databases node
     * @param connectionProperties Additional connection properties, see
     * {@link #getConnectionProperties()}.
     *
     * @return the new instance.
     *
     * @since 1.53
     * @throws NullPointerException if driver or database are null.
     */
    public static DatabaseConnection create(JDBCDriver driver, String databaseURL,
            String user, String schema, String password, boolean rememberPassword,
            String displayName, Properties connectionProperties) {
        if (driver == null || databaseURL == null) {
            throw new NullPointerException();
        }
        org.netbeans.modules.db.explorer.DatabaseConnection conn = new org.netbeans.modules.db.explorer.DatabaseConnection();
        conn.setDriverName(driver.getName());
        conn.setDriver(driver.getClassName());
        conn.setDatabase(databaseURL);
        conn.setUser(user);
        conn.setSchema(schema);
        conn.setPassword(password);
        conn.setRememberPassword(rememberPassword);
        conn.setDisplayName(displayName);
        conn.setConnectionProperties(connectionProperties);
        return conn.getDatabaseConnection();
    }
    
    /**
     * Returns the JDBC driver class that this connection uses.
     *
     * @return the JDBC driver class
     */
    public String getDriverClass() {
        return delegate.getDriver();
    }

    /**
     * Returns the JDBC driver instance that this connection uses.
     *
     * @since 1.32
     * @return the JDBC driver or null if no driver registered
     */
    public JDBCDriver getJDBCDriver() {
        return delegate.findJDBCDriver ();
    }

    /**
     * Returns this connection's database URL.
     *
     * @return the connection's database URL
     */
    public String getDatabaseURL() {
        return delegate.getDatabase();
    }
    
    /**
     * Returns this connection's default schema.
     *
     * @return the schema
     */
    public String getSchema() {
        return delegate.getSchema();
    }
    
    /**
     * Returns the user name used to connect to the database.
     *
     * @return the user name
     */
    public String getUser() {
        return delegate.getUser();
    }
    
    /**
     * Returns the password used to connect to the database.
     *
     * @return the password
     */
    public String getPassword() {
        return delegate.getPassword();
    }
    
    /**
     * Returns the programmatic name of this connection in the Database Explorer.
     *
     * @return the programmatic name
     */
    public String getName() {
        return delegate.getName();
    }
    
    /**
     * Returns the name used to display this connection in the UI.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return delegate.getDisplayName();
    }
    
    /**
     * Returns additional connection properties for the connection.
     * <p>
     * The properties can be set by the user and may be used e.g. in
     * {@link Driver#connect(String, Properties)}. Note that properties "user"
     * and "password" are not included in this object, use {@link #getUser()}
     * and {@link #getPassword()}. Additional properties are usually needed to
     * configure some database-specific connection options (e.g. charset).
     * </p>
     * <p>
     * Changes made to returned object will have no effect (copy of internal
     * properties is returned).
     * </p>
     * @return the connection properties (maybe null)
     * @since db/1.53
     */
    public Properties getConnectionProperties() {
        return delegate.getConnectionProperties();
    }

    /**
     * Check whether usage of scrollable cursors is recommended for this
     * connection.
     *
     * @return True if scrollable cursors can be used, false if scrollable
     * cursors are not supported by driver or database of this connection.
     *
     * @since db/1.57
     */
    public boolean isUseScrollableCursors() {
        return delegate.isUseScrollableCursors();
    }

    /**
     * Set whether usage of scrollable cursors is recommended for this
     * connection.
     *
     * @param useScrollableCursors True if this connection is allowed to use
     * scrollable cursors, false otherwise (older JDBC methods will be used
     * instead.).
     * @since db/1.57
     */
    public void setUseScrollableCursors(boolean useScrollableCursors) {
        delegate.setUseScrollableCursors(useScrollableCursors);
    }

    /**
     * Returns the {@link java.sql.Connection} instance which encapsulates 
     * the physical connection to the database if this database connection
     * is connected. Note that "connected" here means "connected using the
     * Database Explorer". There is no check if {@link java.sql.Connection#close}
     * has been called on the returned connection. However,
     * clients should not call <code>Connection.close()</code> on the returned
     * connection, therefore this method should always return a non-closed 
     * connection or <code>null</code>.
     *
     * <p><strong>Calling {@link java.sql.Connection#close} on the connection
     * returned by this method is illegal. Use 
     * {@link ConnectionManager#disconnect} 
     * to close the connection.</strong></p>
     *
     * @return the physical connection or null if not connected.
     *
     * @throws IllegalStateException if this connection is not added to the
     *         ConnectionManager.
     */
    public Connection getJDBCConnection() {
        if (!ConnectionList.getDefault().contains(delegate)) {
            throw new IllegalStateException("This connection is not added to the ConnectionManager."); // NOI18N
        }

        return delegate.getJDBCConnection();
    }
    
    /**
     * Returns the {@link java.sql.Connection} instance which encapsulates 
     * the physical connection to the database if this database connection
     * is connected. Note that "connected" here means "connected using the
     * Database Explorer". Unless <code>test</code is set to <code>true</code>,
     * there is no check if {@link java.sql.Connection#close}
     * has been called on the returned connection. However,
     * clients should not call <code>Connection.close()</code> on the returned
     * connection, therefore this method should always return a non-closed 
     * connection or <code>null</code>.
     *
     * <p><strong>Calling {@link java.sql.Connection#close} on the connection
     * returned by this method is illegal. Use 
     * {@link ConnectionManager#disconnect} 
     * to close the connection.</strong></p>
     *
     * @param test Set this to true if you want the Database Explorer to validate
     * the JDBC connection before returning it.  If the JDBC connection is invalid, the
     * DatabaseConnection is marked as disconnected and null is returned.
     * <p>
     * <strong>NOTE</strong>
     * that setting this to true can have a performance impact because it requires
     * sending a command to the database server.  Also, this method should not be
     * called on the AWT event thread if you set <code>test</code> to true.
     *
     * @return the physical connection or null if not connected.
     *
     * @throws IllegalStateException if this connection is not added to the
     * ConnectionManager, or if <code>test</code> is set to true and this method is called
     * on the AWT event thread.
     *
     * @since 1.30
     */
    public Connection getJDBCConnection(boolean test) {
        if (test && SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException("This method can not be called on the event dispatch thread with 'test' set to true."); // NOI18N
        }
        
        if (!ConnectionList.getDefault().contains(delegate)) {
            throw new IllegalStateException("This connection is not added to the ConnectionManager."); // NOI18N
        }
        
        return delegate.getJDBCConnection(test);        
    }

    /**
     * Returns a string representation of the database connection.
     */
    @Override
    public String toString() {
        return "DatabaseConnection[name='" + getName() + "']"; // NOI18N
    }
}
