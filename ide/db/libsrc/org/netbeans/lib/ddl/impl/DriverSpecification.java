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

package org.netbeans.lib.ddl.impl;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.lib.ddl.DriverSpecificationFactory;

public class DriverSpecification {

    /** Used DBConnection */
    private Map<Integer, String> desc;

    private String catalog, schema;

    private DatabaseMetaData dmd;

    private ResultSet rs;

    private String quoteString;

    /** Owned factory */
    SpecificationFactory factory;

    /** Constructor */
    public DriverSpecification(Map<Integer, String> description) {
        desc = description;
        quoteString = null;
    }

    public DriverSpecificationFactory getDriverSpecificationFactory() {
        return factory;
    }

    public void setDriverSpecificationFactory(DriverSpecificationFactory fac) {
        factory = (SpecificationFactory) fac;
    }

    public void setCatalog(String catalog) {
        if (catalog == null || dmd == null) {
            this.catalog = catalog;
            return;
        } else {
            catalog = catalog.trim();
        }

        ResultSet result = null;
        List<String> list = new LinkedList<String>();

        try {
            result = dmd.getCatalogs();
            while (result.next()) {
                String candidate = result.getString(1);
                if(candidate != null) {
                    list.add(candidate.trim());
                }
            }
            result.close();
        } catch (SQLException exc) {
            Logger.getLogger("global").log(Level.INFO, null, exc);
            this.catalog = null;  //hack for IBM ODBC driver
            return;
        } finally {
            try {
                if (result != null) {
                    result.close();
                }
            } catch (Exception ex) {
            }
        }

        if (list.contains(catalog)) {
            this.catalog = catalog;
        } else {
            this.catalog = null; //hack for Sybase ODBC driver
        }
    }

    public String getCatalog() {
        return catalog;
    }

    public void setSchema(String schema) {
        if (schema == null || dmd == null) {
            this.schema = schema;
            return;
        } else {
            schema = schema.trim();
        }
        ResultSet result = null;
        List<String> list = new LinkedList<String>();

        try {
            result = dmd.getSchemas();
            while (result.next()) {
                String candidate = result.getString(1);
                if (candidate != null) {
                    list.add(candidate.trim());
                }
            }
        } catch (SQLException exc) {
            Logger.getLogger("global").log(Level.INFO, null, exc);      //NOI18N
            this.schema = null;  //hack for Access ODBC driver
            return;
        } finally {
            try {
                if (result != null) {
                    result.close();
                }
            } catch (Exception ex) {
            }
        }
        if (list.contains(schema)) {
            this.schema = schema;
        } else {
            this.schema = null; //hack for Sybase ODBC driver
        }
    }

    public String getSchema() {
        return schema;
    }

    public void setMetaData(DatabaseMetaData dmd) {
        this.dmd = dmd;
    }
    
    public DatabaseMetaData getMetaData() {
        return dmd;
    }

    public void getTables(String tableNamePattern, String[] types) throws SQLException {
        try {
            rs = dmd.getTables(catalog, schema, tableNamePattern, types);
        } catch (SQLException exc) {
            rs = null;
            throw exc;
        }
    }

    public void getProcedures(String procedureNamePattern) throws SQLException {
        try {
            procedureNamePattern = quoteString(procedureNamePattern);
            rs = dmd.getProcedures(catalog, schema, procedureNamePattern);
        } catch (SQLException exc) {
            rs = null;
            throw exc;
        }
    }

    public void getPrimaryKeys(String table) throws SQLException {
        try {
            table = quoteString(table);
            rs = dmd.getPrimaryKeys(catalog, schema, table);
        } catch (SQLException exc) {
            rs = null;
            throw exc;
        }
    }

    public void getIndexInfo(String table, boolean unique, boolean approximate) throws SQLException {
        try {
            table = quoteString(table);
            rs = dmd.getIndexInfo(catalog, schema, table, unique, approximate);
        } catch (SQLException exc) {
            rs = null;
            throw exc;
        }
    }

    public void getColumns(String tableNamePattern, String columnNamePattern) throws SQLException {
        try {
            tableNamePattern = quoteString(tableNamePattern);
            columnNamePattern = quoteString(columnNamePattern);
            rs = dmd.getColumns(catalog, schema, tableNamePattern, columnNamePattern);
        } catch (SQLException exc) {
            rs = null;
            throw exc;
        }
    }

    public void getProcedureColumns(String procedureNamePattern, String columnNamePattern) throws SQLException {
        try {
            procedureNamePattern = quoteString(procedureNamePattern);
            columnNamePattern = quoteString(columnNamePattern);
            rs = dmd.getProcedureColumns(catalog, schema, procedureNamePattern, columnNamePattern);
        } catch (SQLException exc) {
            rs = null;
            throw exc;
        }
    }

    public void getExportedKeys(String table) throws SQLException {
        try {
            table = quoteString(table);
            rs = dmd.getExportedKeys(catalog, schema, table);
        } catch (SQLException exc) {
            rs = null;
            throw exc;
        }
    }

    public void getImportedKeys(String table) throws SQLException {
        try {
            table = quoteString(table);
            rs = dmd.getImportedKeys(catalog, schema, table);
        } catch (SQLException exc) {
            rs = null;
            throw exc;
        }
    }

    public ResultSet getResultSet() {
        return rs;
    }

    public Map<Integer, String> getRow() throws SQLException {
        Map<Integer, String> rset = new HashMap<Integer, String>();
        String value;

        try {
            int count = rs.getMetaData().getColumnCount();

            for (int i = 1; i <= count; i++) {
                value = null;
                try {
                    value = rs.getString(i);
//                    value = rs.getObject(i); //cannot use getObject() because of problems with MSSQL ODBC driver
                }  catch (SQLException exc) {
                    rset = null;
                    // break;
                    throw exc;
                }
                rset.put(Integer.valueOf(i), value);
            }
        } catch (SQLException exc) {
            rset = null;
            throw exc;
        }

        return rset;
    }

    //another patches

    public boolean areViewsSupported() {
        try {
            String productName = dmd.getDatabaseProductName().trim();
            
            if ("PointBase".equals(productName)) { // NOI18N
                int driverMajorVersion = dmd.getDriverMajorVersion();
                int driverMinorVersion = dmd.getDriverMinorVersion();
                return ((driverMajorVersion == 4 && driverMinorVersion >= 1) || driverMajorVersion > 4);
            } else if ("MySQL".equals(productName)) { // NOI18N
                int databaseMajorVersion = dmd.getDatabaseMajorVersion();
                return (databaseMajorVersion >= 5);
            } else if ("HypersonicSQL".equals(productName)) { // NOI18N
                // XXX is this still true for HypersonicSQL?
                return false;
            }
        } catch(SQLException exc) {
            Logger.getLogger("global").log(Level.INFO, null, exc);
        }
        
        return true;
    }

    private String getQuoteString() {
        if (quoteString == null) {
            try {
                quoteString = dmd.getIdentifierQuoteString();
                if (quoteString == null || quoteString.equals(" ")) //NOI18N
                    quoteString = ""; //NOI18N
                else
                    quoteString.trim();
            } catch (SQLException exc) {
                quoteString = ""; //NOI18N
            }
        }
        
        return quoteString;
    }
    
    private String quoteString(String str) {
        try {
            if (dmd.getDatabaseProductName().trim().equals("PointBase")) { //NOI18N
                //hack for PointBase - DatabaseMetaData methods require quoted arguments for case sensitive identifiers
                String quoteStr = getQuoteString();
                if (str != null && !str.equals("%") && !quoteStr.isEmpty()) //NOI18N
                    str = quoteStr + str + quoteStr;
            }
        } catch (SQLException exc) {
            //PENDING
        }
        
        return str;
    }
    
    public String getDBName() {
        try {
            return dmd.getDatabaseProductName().trim();
        } catch (SQLException exc) {
            //PENDING
            return null;
        }
    }
}
