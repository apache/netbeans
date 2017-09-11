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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
