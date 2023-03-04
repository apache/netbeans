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

package org.netbeans.modules.db.metadata.model;

import java.lang.ref.WeakReference;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.db.metadata.model.api.Action;
import org.netbeans.modules.db.metadata.model.api.Metadata;
import org.netbeans.modules.db.metadata.model.api.MetadataException;
import org.netbeans.modules.db.metadata.model.api.MetadataModelException;
import org.netbeans.modules.db.metadata.model.jdbc.JDBCMetadata;
import org.netbeans.modules.db.metadata.model.jdbc.mssql.MSSQLMetadata;
import org.netbeans.modules.db.metadata.model.jdbc.mysql.MySQLMetadata;
import org.netbeans.modules.db.metadata.model.jdbc.oracle.OracleMetadata;

/**
 *
 * @author Andrei Badea
 */
public class JDBCConnMetadataModel implements MetadataModelImplementation {

    private static final Logger LOGGER = Logger.getLogger(JDBCConnMetadataModel.class.getName());

    private final ReentrantLock lock = new ReentrantLock();
    private final WeakReference<Connection> connRef;
    private final String defaultSchemaName;

    private JDBCMetadata jdbcMetadata;

    public JDBCConnMetadataModel(Connection conn, String defaultSchemaName) {
        this.connRef = new WeakReference<Connection>(conn);
        if (defaultSchemaName != null && defaultSchemaName.trim().length() == 0) {
            this.defaultSchemaName = null;
        } else {
            this.defaultSchemaName = defaultSchemaName;
        }
    }

    public void runReadAction(Action<Metadata> action) throws MetadataModelException {
        lock.lock();
        try {
            // Prevent conn from being GC'd while under read access
            // by holding it in a variable.
            Connection conn = connRef.get();
            if (conn == null) {
                return;
            }
            try {
                enterReadAccess(conn);
                if (jdbcMetadata != null) {
                    Metadata metadata = jdbcMetadata.getMetadata();
                    action.run(metadata);
                }
            } catch (SQLException e) {
                throw new MetadataModelException(e);
            } catch (MetadataException e) {
                throw new MetadataModelException(e);
            }
        } finally {
            lock.unlock();
        }
    }

    public void refresh() {
        LOGGER.fine("Refreshing model");
        lock.lock();
        try {
            jdbcMetadata = null;
        } finally {
            lock.unlock();
        }
    }

    private void enterReadAccess(final Connection conn) throws SQLException {
        if (conn == null) {
            throw new NullPointerException("Connection can not be null");
        }
        Connection oldConn = (jdbcMetadata != null) ? jdbcMetadata.getConnection() : null;
        if (oldConn != conn) {
            if (conn != null) {
                jdbcMetadata = createMetadata(conn, defaultSchemaName);
            } else {
                jdbcMetadata = null;
            }
        }
    }

    private static JDBCMetadata createMetadata(Connection conn, String defaultSchemaName) {
        try {
            DatabaseMetaData dmd = conn.getMetaData();
            if ("Oracle".equals(dmd.getDatabaseProductName())) { // NOI18N
                return new OracleMetadata(conn, defaultSchemaName);
            }

            if ("mysql".equalsIgnoreCase(dmd.getDatabaseProductName())) { // NOI18N
                return new MySQLMetadata(conn, defaultSchemaName);
            }
            
            String driverName = dmd.getDriverName();
            if (driverName != null) {
                if ((driverName.contains("Microsoft") && driverName.contains("SQL Server")) //NOI18N
                        || driverName.contains("jTDS")) { //NOI18N
                    return new MSSQLMetadata(conn, defaultSchemaName);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.INFO, null, e);
        }
        return new JDBCMetadata(conn, defaultSchemaName);
    }
}
