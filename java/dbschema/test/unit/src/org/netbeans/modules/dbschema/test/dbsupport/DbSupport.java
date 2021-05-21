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

package org.netbeans.modules.dbschema.test.dbsupport;

import java.sql.Connection;
import java.util.Collection;

/**
 *
 * @author David
 */
public abstract class DbSupport {
    public enum VENDOR { MYSQL, JAVADB, POSTGRES, UNKNOWN };

    public enum FEATURE { SEQUENCE, AUTOINCREMENT } ;

    public static VENDOR getVendor(String driverClass) {
        if ( "com.mysql.jdbc.Driver".equals(driverClass) 
                || "com.mysql.cj.jdbc.Driver".equals(driverClass)) {
            return VENDOR.MYSQL;
        } else if ("org.apache.derby.jdbc.ClientDriver".equals(driverClass)) {
            return VENDOR.JAVADB;
        } else if ("org.apache.derby.jdbc.EmbeddedDriver".equals(driverClass)) {
            return VENDOR.JAVADB;
        } else if ("org.postgresql.Driver".equals(driverClass)) {
            return VENDOR.POSTGRES;
        } else {
            return VENDOR.UNKNOWN;
        }
    }
    public static DbSupport getInstance(String driverClass) {
        if ( "com.mysql.jdbc.Driver".equals(driverClass) 
                || "com.mysql.cj.jdbc.Driver".equals(driverClass)) {
            return MySQLDBSupport.getInstance();
        } else if ("org.apache.derby.jdbc.ClientDriver".equals(driverClass)) {
            return JavaDbSupport.getInstance();
        } else if ("org.apache.derby.jdbc.EmbeddedDriver".equals(driverClass)) {
            return JavaDbSupport.getInstance();
        } else if ("org.postgresql.Driver".equals(driverClass)) {
            return PostgresDbSupport.getInstance();
        } else {
            throw new RuntimeException("No support for database with driver class" + driverClass);
        }
    }

    public boolean supportsFeature(FEATURE feature) {
        return getSupportedFeatures().contains(feature);
    }

    protected abstract Collection<FEATURE> getSupportedFeatures();

    public abstract void createAITable(Connection conn, String tableName, String columnName) throws Exception;

    public abstract void createSequenceTable(Connection conn, String tableName, String columnName) throws Exception;

}
