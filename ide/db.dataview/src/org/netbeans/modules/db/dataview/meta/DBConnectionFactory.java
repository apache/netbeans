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
package org.netbeans.modules.db.dataview.meta;

import java.sql.Connection;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.dataview.spi.DBConnectionProvider;

/**
 * DBConnectionFactory is used to serve out DB Session The actual physical
 * connection handling is implemented by classes implements DBConnectionProvider.
 *
 * If No DBConnectionProvider found, then it uses DBExplorer connection
 * 
 * @author Ahimanikya Satapathy
 */
public final class DBConnectionFactory {

    private static volatile DBConnectionFactory INSTANCE = null;
    private volatile Throwable ex = null;
    private static Logger mLogger = Logger.getLogger(DBConnectionFactory.class.getName());

    public static DBConnectionFactory getInstance() {
        synchronized (DBConnectionFactory.class) {
            if (INSTANCE == null) {
                if (INSTANCE == null) {
                    INSTANCE = new DBConnectionFactory();
                }
            }
        }
        return INSTANCE;
    }

    private DBConnectionFactory() {
    }

    public void closeConnection(Connection con) {
        DBConnectionProvider connectionProvider = findDBConnectionProvider();
        if (connectionProvider != null) {
            connectionProvider.closeConnection(con);
        }
    }

    public Connection getConnection(DatabaseConnection dbConn) {
        DBConnectionProvider connectionProvider = findDBConnectionProvider();
        this.ex = null;
        try {
            if (connectionProvider != null) {
                return connectionProvider.getConnection(dbConn);
            } else {
                return showConnectionDialog(dbConn);
            }
        } catch (Exception e) {
            mLogger.log(Level.WARNING, "Failed to set connection:" + e); // NOI18N
            this.ex = e;
            return null;
        }
    }

    public Throwable getLastException() {
        return ex;
    }

    private Connection showConnectionDialog(final DatabaseConnection dbConn) {
        if (dbConn == null) {
            return null;
        }
        Connection conn = dbConn.getJDBCConnection(!SwingUtilities.isEventDispatchThread());
        if (conn == null) {
            // this call is automatically redirected to AWT thread if needed
            ConnectionManager.getDefault().showConnectionDialog(dbConn);
            return dbConn.getJDBCConnection(!SwingUtilities.isEventDispatchThread());
        } else {
            return conn;
        }
    }

    private DBConnectionProvider findDBConnectionProvider() {
        Iterator<DBConnectionProvider> it = ServiceLoader.load(DBConnectionProvider.class).iterator();
        if (it.hasNext()) {
            return it.next();
        }

        /*
         * This gives the user/module/components that use etlengine DBConnection
         * factory an option to associate a required class loader with the
         * DBConnectionFactory class. Our requirement is to get the classLoader
         * whose getResources() should be able to point to
         * META-INF/services/org.netbeans.modules.db.model.spi.DBConnectionProvider
         */
        ClassLoader loader = DBConnectionFactory.class.getClassLoader();

        it = ServiceLoader.load(DBConnectionProvider.class, loader).iterator();
        if (it.hasNext()) {
            return it.next();
        }

        return null;
    }
}
