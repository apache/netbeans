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
package org.netbeans.modules.db.dataview.meta;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.db.sql.support.SQLIdentifiers;
import org.netbeans.api.db.sql.support.SQLIdentifiers.Quoter;
import org.netbeans.modules.db.dataview.util.DataViewUtils;

/**
 * Extracts database metadata information (table names and constraints, their
 * associated columns, etc.)
 *
 * @author Ahimanikya Satapathy
 */
public final class DBMetaDataFactory {

    public static final int DB2 = 0;
    public static final int ORACLE = 1;
    public static final int SQLSERVER = 2;
    public static final int JDBC = 3;
    public static final int PostgreSQL = 4;
    public static final int MYSQL = 5;
    public static final int DERBY = 6;
    public static final int SYBASE = 7;
    public static final int AXION = 8;
    public static final int POINTBASE = 9;
    private final int dbType;
    private final DatabaseMetaData dbmeta;
    private final Quoter sqlquoter;
    private final String identifierQuoteString;

    public DBMetaDataFactory(Connection dbconn) throws SQLException {
        assert dbconn != null;
        dbmeta = dbconn.getMetaData();
        
        // get the database type based on the product name converted to lowercase
        if (dbmeta.getURL() != null) {
            dbType = getDBTypeFromURL(dbmeta.getURL());
        } else {
            dbType = JDBC;
        }
        
        sqlquoter = SQLIdentifiers.createQuoter(dbmeta);
        String buildIdentifierQuoteString = "\""; // NOI18N
        try {
            buildIdentifierQuoteString = dbmeta.getIdentifierQuoteString().trim();
        } catch (SQLException e) {
        }
        identifierQuoteString = buildIdentifierQuoteString;
    }

    public boolean supportsLimit() {
        switch (dbType) {
            case MYSQL:
            case PostgreSQL:
            case AXION:
                return true;
            default:
                return false;
        }
    }

    public int getDBType() throws SQLException {
        return dbType;
    }

    private static int getDBTypeFromURL(String url) {
        int dbtype;

        // get the database type based on the product name converted to lowercase
        url = url.toLowerCase();
        if (url.contains("sybase")) { // NOI18N
            dbtype = SYBASE;
        } else if (url.contains("sqlserver")) { // NOI18N
            dbtype = SQLSERVER;
        } else if (url.contains("db2")) { // NOI18N
            dbtype = DB2;
        } else if (url.contains("orac")) { // NOI18N
            dbtype = ORACLE;
        } else if (url.contains("axion")) { // NOI18N
            dbtype = AXION;
        } else if (url.contains("derby")) { // NOI18N
            dbtype = DERBY;
        } else if (url.contains("postgre")) { // NOI18N
            dbtype = PostgreSQL;
        } else if (url.contains("mysql")) { // NOI18N
            dbtype = MYSQL;
        } else if (url.contains("pointbase")) { // NOI18N
            dbtype = POINTBASE;
        } else {
            dbtype = JDBC;
        }

        return dbtype;
    }

    private DBPrimaryKey getPrimaryKeys(String tcatalog, String tschema, String tname) {
        ResultSet rs = null;
        try {
            rs = dbmeta.getPrimaryKeys(setToNullIfEmpty(tcatalog), setToNullIfEmpty(tschema), tname);
            return new DBPrimaryKey(rs);
        } catch (SQLException | NullPointerException e) {
            // NullPointerException is thrown by Microsoft SQL Server when
            // set showplan_* on is issued
            return null;
        } finally {
            DataViewUtils.closeResources(rs);
        }
    }

    private Map<String, DBForeignKey> getForeignKeys(DBTable table) {
        Map<String, DBForeignKey> fkList = Collections.emptyMap();
        ResultSet rs = null;
        try {
            rs = dbmeta.getImportedKeys(setToNullIfEmpty(table.getCatalog()), setToNullIfEmpty(table.getSchema()), table.getName());
            fkList = DBForeignKey.createForeignKeyColumnMap(table, rs);
        } catch (SQLException | NullPointerException e) {
            // NullPointerException is thrown by Microsoft SQL Server when
            // set showplan_* on is issued
            return null;
        } finally {
            DataViewUtils.closeResources(rs);
        }
        return fkList;
    }

    public synchronized Collection<DBTable> generateDBTables(ResultSet rs, String sql, boolean isSelect) throws SQLException {
        Map<String, DBTable> tables = new LinkedHashMap<>();
        String noTableName = "UNKNOWN"; // NOI18N

        // get table column information
        ResultSetMetaData rsMeta = rs.getMetaData();
        for (int i = 1; i <= rsMeta.getColumnCount(); i++) {
            // #153219 - workaround 
            String tableName = rsMeta.getTableName(i);
            if (tableName == null) {
                tableName = noTableName;
            }
            String schemaName = rsMeta.getSchemaName(i);
            // although Javadoc admit of returning null, SQLite returns null
            if (schemaName == null) {
                schemaName = "";
            }
            String catalogName = rsMeta.getCatalogName(i);
            // although Javadoc admit of returning null, SQLite returns null
            if (catalogName == null) {
                catalogName = "";
            }
            if (schemaName.trim().length() == 0 && catalogName.equals(tableName)) {
                // a workaround for SQLite
                // suppose the catalog shouldn't be same if schema is not supported
                catalogName = ""; // NOI18N
            }
            String key = catalogName + schemaName + tableName;
            if (key.equals("")) {
                key = noTableName;
            }
            DBTable table = tables.get(key);
            if (table == null) {
                table = new DBTable(tableName, schemaName, catalogName);
                tables.put(key, table);
            }

            int sqlTypeCode = rsMeta.getColumnType(i);
            String sqlTypeStr = rsMeta.getColumnTypeName(i);
            if (sqlTypeCode == java.sql.Types.OTHER && dbType == ORACLE) {
                if (sqlTypeStr.startsWith("TIMESTAMP")) { // NOI18N
                    sqlTypeCode = java.sql.Types.TIMESTAMP;
                } else if (sqlTypeStr.startsWith("FLOAT")) { // NOI18N
                    sqlTypeCode = java.sql.Types.FLOAT;
                } else if (sqlTypeStr.startsWith("REAL")) { // NOI18N
                    sqlTypeCode = java.sql.Types.REAL;
                } else if (sqlTypeStr.startsWith("BLOB")) { // NOI18N
                    sqlTypeCode = java.sql.Types.BLOB;
                } else if (sqlTypeStr.startsWith("CLOB")) { // NOI18N
                    sqlTypeCode = java.sql.Types.CLOB;
                }
            }

            String colName = rsMeta.getColumnName(i);
            int position = i;
            int scale = rsMeta.getScale(i);
            int precision;
            try {
                precision = rsMeta.getPrecision(i);
            } catch (NumberFormatException nfe) {
                // Oracle classes12.jar driver throws NumberFormatException while getting precision
                // let's ignore it and set Integer.MAX_VALUE as fallback and log it
                precision = Integer.MAX_VALUE;
                Logger.getLogger(DBMetaDataFactory.class.getName()).log(Level.FINE,
                        "Oracle classes12.jar driver throws NumberFormatException while getting precision, use Integer.MAX_VALUE as fallback.", // NOI18N
                        nfe);
            }

            boolean isNullable = (rsMeta.isNullable(i) == ResultSetMetaData.columnNullable);
            String displayName = rsMeta.getColumnLabel(i);
            int displaySize = rsMeta.getColumnDisplaySize(i);
            boolean autoIncrement = rsMeta.isAutoIncrement(i);

            //Oracle DATE type needs to be retrieved as full date and time
            if (sqlTypeCode == java.sql.Types.DATE && dbType == ORACLE) {
                sqlTypeCode = java.sql.Types.TIMESTAMP;
                displaySize = 22;
            }

            //Handle MySQL BIT(n) where n > 1
            if (sqlTypeCode == java.sql.Types.VARBINARY && dbType == MYSQL) {
                if (sqlTypeStr.startsWith("BIT")) { // NOI18N
                    sqlTypeCode = java.sql.Types.BIT;
                }
            }

            // The SQL Server timestamp type is a JDBC BINARY type with the fixed length of 8 bytes.
            // A Transact-SQL timestamp != an ANSI SQL-92 timestamp.
            // If its a SQL style timestamp you are after use a datetime data type.
            // A T-SQL timestamp are just auto generated binary numbers guarenteed to
            // be unique in the context of a database and are typically used for
            // versioning, not storing dates.

            if (sqlTypeCode == java.sql.Types.BINARY && dbType == SQLSERVER && precision == 8) {
                autoIncrement = true;
            }

            // create a table column and add it to the vector
            DBColumn col = new DBColumn(table, colName, sqlTypeCode, sqlTypeStr, scale, precision, isNullable, autoIncrement);
            col.setOrdinalPosition(position);
            col.setDisplayName(displayName);
            col.setDisplaySize(displaySize);
            table.addColumn(col);
            table.setQuoter(sqlquoter);
        }

        // Oracle does not return table name for resultsetmetadata.getTableName()
        DBTable table = tables.get(noTableName);
        if (tables.size() == 1 && table != null && isSelect) {
            adjustTableMetadata(sql, tables.get(noTableName));
            for (DBColumn col : table.getColumns().values()) {
                col.setEditable(!table.getName().equals("") && !col.isGenerated());
            }
        }

        return tables.values();
    }

    /**
     * Do post processing of the resultset metadata and add data provided by
     * database metadata.
     * 
     * This was decoupled from generateDBTables because accessing the database
     * metadata before the resultset is fully read risks either a corrupted resultset
     * (oracle, pointbase) or out of memory errors on large resultsets (mssql)
     * 
     * @param tables 
     */
    public void postprocessTables(Collection<DBTable> tables) {
        DBModel dbModel = new DBModel();
        dbModel.setDBType(dbType);
        for (DBTable tbl : tables) {
            if (DataViewUtils.isNullString(tbl.getName())) {
                continue;
            }
            checkPrimaryKeys(tbl);
            checkForeignKeys(tbl);
            dbModel.addTable(tbl);
            populateDefaults(tbl);
        }
    }

    private void populateDefaults(DBTable table) {
        ResultSet rs = null;
        try {
            rs = dbmeta.getColumns(setToNullIfEmpty(table.getCatalog()), setToNullIfEmpty(table.getSchema()), table.getName(), "%");
            while (rs.next()) {
                String defaultValue = rs.getString("COLUMN_DEF"); // NOI18N
                DBColumn col = table.getColumn(rs.getString("COLUMN_NAME")); // NOI18N

                if (col != null && defaultValue != null && defaultValue.trim().length() != 0) {
                    col.setDefaultValue(defaultValue.trim());
                }
            }
        } catch (SQLException | NullPointerException e) {
            // NullPointerException is thrown by Microsoft SQL Server when
            // set showplan_* on is issued
        } finally {
            DataViewUtils.closeResources(rs);
        }
    }

    private void adjustTableMetadata(String sql, DBTable table) {
        String tableName = "";
        if (sql.toUpperCase().contains("FROM")) { // NOI18N
            // User may type "FROM" in either lower, upper or mixed case
            String[] splitByFrom = sql.toUpperCase().split("FROM"); // NOI18N
            String fromsql = sql.substring(sql.length() - splitByFrom[1].length());
            if (fromsql.toUpperCase().contains("WHERE")) { // NOI18N
                splitByFrom = fromsql.toUpperCase().split("WHERE"); // NOI18N
                fromsql = fromsql.substring(0, splitByFrom[0].length());
            } else if (fromsql.toUpperCase().contains("ORDER BY")) { // NOI18N
                splitByFrom = fromsql.toUpperCase().split("ORDER BY"); // NOI18N
                fromsql = fromsql.substring(0, splitByFrom[0].length());
            }
            if (!sql.toUpperCase().contains("JOIN")) { // NOI18N
                StringTokenizer t = new StringTokenizer(fromsql, ","); // NOI18N

                if (t.hasMoreTokens()) {
                    tableName = t.nextToken().trim();
                }

                if (t.hasMoreTokens()) {
                    tableName = "";
                }
            }
        }
        String[] splitByDot = tableName.split("\\."); // NOI18N
        if (splitByDot.length == 3) {
            table.setCatalogName(unQuoteIfNeeded(splitByDot[0]));
            table.setSchemaName(unQuoteIfNeeded(splitByDot[1]));
            table.setName(unQuoteIfNeeded(splitByDot[2]));
        } else if (splitByDot.length == 2) {
            table.setSchemaName(unQuoteIfNeeded(splitByDot[0]));
            table.setName(unQuoteIfNeeded(splitByDot[1]));
        } else if (splitByDot.length == 1) {
            table.setName(unQuoteIfNeeded(splitByDot[0]));
        }
    }

    private String unQuoteIfNeeded(String id) {
        return id.replaceAll(identifierQuoteString, "");
    }

    private void checkPrimaryKeys(DBTable newTable) {
        DBPrimaryKey keys = getPrimaryKeys(newTable.getCatalog(), newTable.getSchema(), newTable.getName());
        if (keys != null && keys.getColumnCount() != 0) {
            newTable.setPrimaryKey(keys);

            // now loop through all the columns flagging the primary keys
            List<DBColumn> columns = newTable.getColumnList();
            if (columns != null) {
                for (int i = 0; i < columns.size(); i++) {
                    DBColumn col = columns.get(i);
                    if (keys.contains(col.getName())) {
                        col.setPrimaryKey(true);
                    }
                }
            }
        }
    }

    private void checkForeignKeys(DBTable newTable) {
        // get the foreing keys
        Map<String, DBForeignKey> foreignKeys = getForeignKeys(newTable);
        if (foreignKeys != null && !foreignKeys.isEmpty()) {
            newTable.setForeignKeyMap(foreignKeys);

            // create a hash set of the keys
            Set<String> foreignKeysSet = new HashSet<>();
            Iterator<DBForeignKey> it = foreignKeys.values().iterator();
            while (it.hasNext()) {
                DBForeignKey key = it.next();
                if (key != null) {
                    foreignKeysSet.addAll(key.getColumnNames());
                }
            }

            // now loop through all the columns flagging the foreign keys
            List<DBColumn> columns = newTable.getColumnList();
            if (columns != null) {
                for (int i = 0; i < columns.size(); i++) {
                    DBColumn col = columns.get(i);
                    if (foreignKeysSet.contains(col.getName())) {
                        col.setForeignKey(true);
                    }
                }
            }
        }
    }

    private String setToNullIfEmpty(String source) {
        if (source != null && source.equals("")) {
            source = null;
        }
        return source;
    }
}
