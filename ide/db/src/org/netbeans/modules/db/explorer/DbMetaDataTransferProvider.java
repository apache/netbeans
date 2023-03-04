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

package org.netbeans.modules.db.explorer;

import java.awt.datatransfer.DataFlavor;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.JDBCDriver;

/**
 * This interface is a means to communicate with the db/dbapi module, which
 * contains the actual data flavor and classes for transfer. The db/dbapi module
 * puts an implementation of this interface in the default lookup.
 *
 * @author Andrei Badea
 */
public interface DbMetaDataTransferProvider {

    /**
     * Returns the data flavor representing database connections.
     */
    DataFlavor getConnectionDataFlavor();

    /**
     * Returns the data flavor representing database tables.
     */
    DataFlavor getTableDataFlavor();
    
    /**
     * Returns the data flavor representing database views.
     */
    DataFlavor getViewDataFlavor();

    /**
     * Returns the data flavor representing columns of database tables.
     */
    DataFlavor getColumnDataFlavor();

    /**
     * Returns an object which encapsulates a database connection.
     */
    Object createConnectionData(DatabaseConnection dbconn, JDBCDriver jdbcDriver);
    
    /**
     * Returns an object which encapsulates a database table.
     */
    Object createTableData(DatabaseConnection dbconn, JDBCDriver jdbcDriver, String tableName);
    
    /**
     * Returns an object which encapsulates a database view.
     */
    Object createViewData(DatabaseConnection dbconn, JDBCDriver jdbcDriver, String viewName);
    
    /**
     * Returns an object which encapsulates a column of a database table.
     */
    Object createColumnData(DatabaseConnection dbconn, JDBCDriver jdbcDriver, String tableName, String columnName);
}
