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

package org.netbeans.modules.db.test;

import java.sql.Connection;
import java.sql.ResultSet;

/**
 *
 * @author David Van Couvering
 */
public class MySQLDBProvider extends DefaultDBProvider {

    @Override
    public void createTestTable(Connection conn, String schemaName, String tableName, String idName) throws Exception {
        conn.createStatement().executeUpdate("CREATE TABLE " + schemaName + "." + tableName + " (" +
                idName + " integer primary key) ENGINE=InnoDB");
    }

    @Override
    public void dropSchema(Connection conn, String schemaName) throws Exception {
        conn.createStatement().executeUpdate("DROP SCHEMA IF EXISTS " + schemaName);
    }

    @Override
    public void setSchema(Connection conn, String schemaName) throws Exception {
        conn.createStatement().executeUpdate("USE " + schemaName);
    }

    @Override
    public boolean columnInIndex(Connection conn, String schemaName, String tableName, String colname, String indexName) throws Exception {
        ResultSet rs = conn.createStatement().executeQuery(
                "show index from " + schemaName + "." + tableName +
                " where key_name = '" + indexName + "' and column_name = '" + colname + "'");

        return rs.next();
    }



}
