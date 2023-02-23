/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.db.test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author David Van Couvering
 */
public class DefaultDBProvider implements DBProvider {
    public void createSchema(Connection conn, String schemaName) throws Exception {
        dropSchema(conn, schemaName);
        conn.createStatement().executeUpdate("CREATE SCHEMA " + schemaName);
    }
    
    public void setSchema(Connection conn, String schemaName) throws Exception {
        conn.createStatement().executeUpdate("SET SCHEMA " + schemaName);
    }
    
    public void dropSchema(Connection conn, String schemaName) throws Exception {
        if (schemaExists(conn, schemaName)) {
            return;
        }
        conn.createStatement().executeUpdate("DROP SCHEMA " + schemaName);
    }

    public void createTestTable(Connection conn, String schemaName, String tableName, String idName) throws Exception {
        if (tableExists(conn, schemaName, tableName)) {
            return;
        }
        conn.createStatement().executeUpdate("CREATE TABLE " + schemaName + '.' + tableName + " (" +
                idName + " integer primary key)");
    }
    
    public void dropTable(Connection conn, String schemaName, String tableName) throws Exception {
        if (!tableExists(conn, schemaName, tableName)) {
            return;
        }
        try {
            conn.createStatement().executeUpdate("DROP TABLE " + schemaName + "." + tableName);
        } catch (SQLException sqle) {
            System.out.println("Exception when dropping table, probably because it doesn't exist: " + sqle.getMessage());
        }
    }

    public void dropView(Connection conn, String schemaName, String tableName) throws Exception {
        conn.createStatement().executeUpdate("DROP VIEW " + schemaName + "." + tableName);
    }

    public boolean tableExists(Connection conn, String schema, String tableName) throws Exception {
        DatabaseMetaData md = conn.getMetaData();
        ResultSet rs = md.getTables(null, schema, tableName, null);
        return rs.next();        
    }

    public boolean schemaExists(Connection conn, String schemaName) throws Exception {
        DatabaseMetaData md = conn.getMetaData();
        
        ResultSet rs  = md.getSchemas();
        
        while ( rs.next() ) {
            if ( schemaName.equalsIgnoreCase(rs.getString(1))) {
                return true;
            }
        }
    
        return false;
    }
    
    public boolean columnInIndex(Connection conn, String schemaName, String tableName, String colname, String indexName)
            throws Exception {
        DatabaseMetaData md = conn.getMetaData();
        ResultSet rs = md.getIndexInfo(null, schemaName, tableName, false, false);

        while ( rs.next() ) {
            String ixName = rs.getString(6);
            if ( ixName != null && ixName.equals(indexName)) {
                String ixColName = rs.getString(9);
                if ( ixColName.equals(colname) ) {
                    return true;
                }
            }
        }

        return false;
        
    }

}
