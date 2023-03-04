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

import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.explorer.DatabaseMetaDataTransfer.Column;
import org.netbeans.api.db.explorer.DatabaseMetaDataTransfer.Connection;
import org.netbeans.api.db.explorer.DatabaseMetaDataTransfer.Table;
import org.netbeans.api.db.explorer.DatabaseMetaDataTransfer.View;
import org.netbeans.api.db.explorer.JDBCDriver;

/**
 * 
 * @author Andrei Badea
 */
public abstract class DatabaseMetaDataTransferAccessor {
    
    public static DatabaseMetaDataTransferAccessor DEFAULT;
    
    static {
        Class c = DatabaseMetaDataTransferAccessor.class;
        try {
            Class.forName(c.getName(), true, c.getClassLoader());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Returns an object which encapsulates a database connection.
     */
    public abstract Connection createConnectionData(DatabaseConnection dbconn, JDBCDriver jdbcDriver);
    
    /**
     * Returns an object which encapsulates a database table.
     */
    public abstract Table createTableData(DatabaseConnection dbconn, JDBCDriver jdbcDriver, String tableName);
    
    /**
     * Returns an object which encapsulates a database view.
     */
    public abstract View createViewData(DatabaseConnection dbconn, JDBCDriver jdbcDriver, String viewName);
    
    /**
     * Returns an object which encapsulates a column of a database table.
     */
    public abstract Column createColumnData(DatabaseConnection dbconn, JDBCDriver jdbcDriver, String tableName, String columnName);
}
