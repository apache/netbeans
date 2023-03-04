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

package org.netbeans.api.db.explorer;

import java.awt.datatransfer.DataFlavor;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.JDBCDriver;
import org.netbeans.modules.db.explorer.DatabaseMetaDataTransferAccessor;
import org.netbeans.modules.db.explorer.DatabaseMetaDataTransferAccessor;

/**
 * This class contains data flavors and classes for transferring database metadata
 * from the Database Explorer. 
 *
 * @author Andrei Badea, Jan Stola
 *
 * @since 1.21
 */
public final class DatabaseMetaDataTransfer {

    /**
     * The {@link DataFlavor} representing a database connection.
     */
    public static DataFlavor CONNECTION_FLAVOR;

    /**
     * The {@link DataFlavor} representing a database table.
     */
    public static DataFlavor TABLE_FLAVOR;

    /**
     * The {@link DataFlavor} representing a database view.
     */
    public static DataFlavor VIEW_FLAVOR;

    /**
     * The {@link DataFlavor} representing a database column.
     */
    public static DataFlavor COLUMN_FLAVOR;

    static {
        DatabaseMetaDataTransferAccessor.DEFAULT = new DatabaseMetaDataTransferAccessor() {
            public Connection createConnectionData(DatabaseConnection dbconn, JDBCDriver jdbcDriver) {
                return new Connection(dbconn, jdbcDriver);
            }
            public Table createTableData(DatabaseConnection dbconn, JDBCDriver jdbcDriver, String tableName) {
                return new Table(dbconn, jdbcDriver, tableName);
            }
            public View createViewData(DatabaseConnection dbconn, JDBCDriver jdbcDriver, String viewName) {
                return new View(dbconn, jdbcDriver, viewName);
            }
            public Column createColumnData(DatabaseConnection dbconn, JDBCDriver jdbcDriver, String tableName, String columnName) {
                return new Column(dbconn, jdbcDriver, tableName, columnName);
            }
        };
        try {
            CONNECTION_FLAVOR = new DataFlavor("application/x-java-netbeans-dbexplorer-connection;class=org.netbeans.api.db.explorer.DatabaseMetaDataTransfer$Connection"); // NOI18N
            TABLE_FLAVOR = new DataFlavor("application/x-java-netbeans-dbexplorer-table;class=org.netbeans.api.db.explorer.DatabaseMetaDataTransfer$Table"); // NOI18N
            VIEW_FLAVOR = new DataFlavor("application/x-java-netbeans-dbexplorer-view;class=org.netbeans.api.db.explorer.DatabaseMetaDataTransfer$View"); // NOI18N
            COLUMN_FLAVOR = new DataFlavor("application/x-java-netbeans-dbexplorer-column;class=org.netbeans.api.db.explorer.DatabaseMetaDataTransfer$Column"); // NOI18N
        } catch (ClassNotFoundException e) {
            throw new AssertionError(e);
        }
    }
    
    private DatabaseMetaDataTransfer() {}

    /**
     * Represents a database connection during a drag and drop transfer.
     */
    public static final class Connection {

        private final DatabaseConnection dbconn;
        private final JDBCDriver jdbcDriver;

        private Connection(DatabaseConnection dbconn, JDBCDriver jdbcDriver) {
            this.dbconn = dbconn;
            this.jdbcDriver = jdbcDriver;
        }

        /**
         * Returns the {@link DatabaseConnection}.
         */
        public DatabaseConnection getDatabaseConnection() {
            return dbconn;
        }

        /**
         * Returns the {@link JDBCDriver} which was used to connect to {@link #getDatabaseConnection}.
         */
        public JDBCDriver getJDBCDriver() {
            return jdbcDriver;
        }

        public String toString() {
            return "Connection[databaseConnection=" + dbconn + ",jdbcDriver=" + jdbcDriver + "]"; // NOI18N
        }
    }

    /**
     * Represents a table during a drag and drop transfer.
     */
    public static final class Table {

        private final DatabaseConnection dbconn;
        private final JDBCDriver jdbcDriver;
        private final String tableName;

        private Table(DatabaseConnection dbconn, JDBCDriver jdbcDriver, String tableName) {
            this.dbconn = dbconn;
            this.jdbcDriver = jdbcDriver;
            this.tableName = tableName;
        }

        /**
         * Returns the {@link DatabaseConnection} this table comes from.
         */
        public DatabaseConnection getDatabaseConnection() {
            return dbconn;
        }

        /**
         * Returns the {@link JDBCDriver} which was used to connect to {@link #getDatabaseConnection}.
         */
        public JDBCDriver getJDBCDriver() {
            return jdbcDriver;
        }

        /**
         * Returns the name of this table.
         */
        public String getTableName() {
            return tableName;
        }

        public String toString() {
            return "Table[databaseConnection=" + dbconn + ",jdbcDriver=" + jdbcDriver + ",tableName=" + tableName + "]"; // NOI18N
        }
    }

    /**
     * Represents a view during a drag and drop transfer.
     */
    public static final class View {

        private final DatabaseConnection dbconn;
        private final JDBCDriver jdbcDriver;
        private final String viewName;

        private View(DatabaseConnection dbconn, JDBCDriver jdbcDriver, String viewName) {
            this.dbconn = dbconn;
            this.jdbcDriver = jdbcDriver;
            this.viewName = viewName;
        }

        /**
         * Returns the {@link DatabaseConnection} this view comes from.
         */
        public DatabaseConnection getDatabaseConnection() {
            return dbconn;
        }

        /**
         * Returns the {@link JDBCDriver} which was used to connect to {@link #getDatabaseConnection}.
         */
        public JDBCDriver getJDBCDriver() {
            return jdbcDriver;
        }

        /**
         * Returns the name of this view.
         */
        public String getViewName() {
            return viewName;
        }

        public String toString() {
            return "View[databaseConnection=" + dbconn + ",jdbcDriver=" + jdbcDriver + ",viewName=" + viewName + "]"; // NOI18N
        }
    }

    /**
     * Represents a column during a drag and drop transfer.
     */
    public static final class Column {

        private final DatabaseConnection dbconn;
        private final JDBCDriver jdbcDriver;
        private final String tableName;
        private final String columnName;

        private Column(DatabaseConnection dbconn, JDBCDriver jdbcDriver, String tableName, String columnName) {
            this.dbconn = dbconn;
            this.jdbcDriver = jdbcDriver;
            this.tableName = tableName;
            this.columnName = columnName;
        }

        /**
         * Returns the {@link DatabaseConnection} this column comes from.
         */
        public DatabaseConnection getDatabaseConnection() {
            return dbconn;
        }

        /**
         * Returns the {@link JDBCDriver} which was used to connect to {@link #getDatabaseConnection}.
         */
        public JDBCDriver getJDBCDriver() {
            return jdbcDriver;
        }

        /**
         * Returns the name of the table that contains this column.
         */
        public String getTableName() {
            return tableName;
        }

        /**
         * Returns the name of this column.
         */
        public String getColumnName() {
            return columnName;
        }

        public String toString() {
            return "Column[databaseConnection=" + dbconn + ",jdbcDriver=" + jdbcDriver + ",tableName=" + tableName + ",columnName=" + columnName + "]"; // NOI18N
        }
    }
}
