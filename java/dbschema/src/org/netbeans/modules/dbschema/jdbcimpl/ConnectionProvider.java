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

package org.netbeans.modules.dbschema.jdbcimpl;

import java.sql.*;
import java.util.ResourceBundle;

public class ConnectionProvider {

	final ResourceBundle bundle = ResourceBundle.getBundle("org.netbeans.modules.dbschema.resources.Bundle"); // NOI18N

    private Connection con;
    private DatabaseMetaData dmd;

    private String driver;
    private String url;
    private String username;
    private String password;
    private String schema;

    /** Creates new ConnectionProvider */
    public ConnectionProvider(Connection con, String driver) throws SQLException{
        this.con = con;
        this.driver = driver;
        dmd = con.getMetaData();
    }
    
    public ConnectionProvider(String driver, String url, String username, String password) throws ClassNotFoundException, SQLException {
        this.driver = driver;
        this.url = url;
        this.username = username;
        this.password = password;
    
        Class.forName(driver);
        con = DriverManager.getConnection(url, username, password);
        dmd = con.getMetaData();
    }
  
    public Connection getConnection() {
        return con;
    }
  
    public DatabaseMetaData getDatabaseMetaData() throws SQLException {
        return dmd;
    }

    public String getDriver() {
        return driver;
    }
    
    public void setSchema(String schema) {
        this.schema = schema;
    }
    
    public String getSchema() {
        return schema;
    }
    
    public void closeConnection() {
        if (con != null)
            try {
                con.close();
            } catch (SQLException exc) {
                if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                    System.out.println(bundle.getString("UnableToCloseConnection")); //NOI18N
                con = null;
            }
    }
}
