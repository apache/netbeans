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

package org.netbeans.lib.ddl.adaptors;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.RowIdLifetime;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import org.openide.util.NbBundle;

public class DefaultAdaptor implements DatabaseMetaDataAdaptor, Serializable {
    
    private transient Connection con;
    private transient DatabaseMetaData dmd;
    protected Map properties;
    private transient PropertyChangeSupport propertySupport;

    private ResourceBundle bundle = NbBundle.getBundle("org.netbeans.lib.ddl.resources.Bundle"); // NOI18N
    
    public static final int NOT_SET = 0;
    public static final String NOT_SET_STRING = "";

    public static final int YES = 1;
    public static final int NO = 2;

    public static final int NULLSORT_HIGH = 1;
    public static final int NULLSORT_LOW = 2;
    public static final int NULLSORT_START = 3;
    public static final int NULLSORT_END = 4;

    public static final int STORE_LOWERCASE = 1;
    public static final int STORE_UPPERCASE = 2;
    public static final int STORE_MIXEDCASE = 3;

    // Boolean properties

    public static final String PROP_PROCEDURES_ARE_CALLABLE = "proceduresAreCallable"; // NOI18N
    public static final String PROP_TABLES_ARE_SELECTABLE = "tablesAreSelectable"; // NOI18N
    public static final String PROP_READONLY = "readOnly"; // NOI18N
    public static final String PROP_LOCAL_FILES = "localFiles"; // NOI18N
    public static final String PROP_FILE_PER_TABLE = "localFilePerTable"; // NOI18N
    public static final String PROP_MIXEDCASE_IDENTIFIERS = "mixedCaseIdentifiers"; // NOI18N
    public static final String PROP_MIXEDCASE_QUOTED_IDENTIFIERS = "mixedCaseQuotedIdentifiers"; // NOI18N
    public static final String PROP_ALTER_ADD = "alterTableWithAddColumn"; // NOI18N
    public static final String PROP_ALTER_DROP = "alterTableWithDropColumn"; // NOI18N
    public static final String PROP_COLUMN_ALIASING = "columnAliasing"; // NOI18N
    public static final String PROP_NULL_PLUS_NULL_IS_NULL = "nullPlusNonNullIsNull"; // NOI18N
    public static final String PROP_CONVERT = "convert"; // NOI18N
    public static final String PROP_TABLE_CORRELATION_NAMES = "tableCorrelationNames"; // NOI18N
    public static final String PROP_DIFF_TABLE_CORRELATION_NAMES = "differentTableCorrelationNames"; // NOI18N
    public static final String PROP_EXPRESSIONS_IN_ORDERBY = "expressionsInOrderBy"; // NOI18N
    public static final String PROP_ORDER_BY_UNRELATED = "orderByUnrelated"; // NOI18N
    public static final String PROP_GROUP_BY = "groupBy"; // NOI18N
    public static final String PROP_UNRELATED_GROUP_BY = "groupByUnrelated"; // NOI18N
    public static final String PROP_BEYOND_GROUP_BY = "groupByBeyondSelect"; // NOI18N
    public static final String PROP_ESCAPE_LIKE = "likeEscapeClause"; // NOI18N
    public static final String PROP_MULTIPLE_RS = "multipleResultSets"; // NOI18N
    public static final String PROP_MULTIPLE_TRANSACTIONS = "multipleTransactions"; // NOI18N
    public static final String PROP_NON_NULL_COLUMNSS = "nonNullableColumns"; // NOI18N
    public static final String PROP_MINUMUM_SQL_GRAMMAR = "minimumSQLGrammar"; // NOI18N
    public static final String PROP_CORE_SQL_GRAMMAR = "coreSQLGrammar"; // NOI18N
    public static final String PROP_EXTENDED_SQL_GRAMMAR = "extendedSQLGrammar"; // NOI18N
    public static final String PROP_ANSI_SQL_GRAMMAR = "ANSI92EntryLevelSQL"; // NOI18N
    public static final String PROP_INTERMEDIATE_SQL_GRAMMAR = "ANSI92IntermediateSQL"; // NOI18N
    public static final String PROP_FULL_SQL_GRAMMAR = "ANSI92FullSQL"; // NOI18N
    public static final String PROP_INTEGRITY_ENHANCEMENT = "IntegrityEnhancementFacility"; // NOI18N
    public static final String PROP_OUTER_JOINS = "outerJoins"; // NOI18N
    public static final String PROP_FULL_OUTER_JOINS = "fullOuterJoins"; // NOI18N
    public static final String PROP_LIMITED_OUTER_JOINS = "limitedOuterJoins"; // NOI18N
    public static final String PROP_CATALOG_AT_START = "catalogAtStart"; // NOI18N
    public static final String PROP_SCHEMAS_IN_DML = "schemasInDataManipulation"; // NOI18N
    public static final String PROP_SCHEMAS_IN_PROCEDURE_CALL = "schemasInProcedureCalls"; // NOI18N
    public static final String PROP_SCHEMAS_IN_TABLE_DEFINITION = "schemasInTableDefinitions"; // NOI18N
    public static final String PROP_SCHEMAS_IN_INDEX = "schemasInIndexDefinitions"; // NOI18N
    public static final String PROP_SCHEMAS_IN_PRIVILEGE_DEFINITION = "schemasInPrivilegeDefinitions"; // NOI18N
    public static final String PROP_CATALOGS_IN_DML = "catalogsInDataManipulation"; // NOI18N
    public static final String PROP_CATALOGS_IN_PROCEDURE_CALL = "catalogsInProcedureCalls"; // NOI18N
    public static final String PROP_CATALOGS_IN_TABLE_DEFINITION = "catalogsInTableDefinitions"; // NOI18N
    public static final String PROP_CATALOGS_IN_INDEX = "catalogsInIndexDefinitions"; // NOI18N
    public static final String PROP_CATALOGS_IN_PRIVILEGE_DEFINITION = "catalogsInPrivilegeDefinitions"; // NOI18N
    public static final String PROP_POSITIONED_DELETE = "positionedDelete"; // NOI18N
    public static final String PROP_POSITIONED_UPDATE = "positionedUpdate"; // NOI18N
    public static final String PROP_SELECT_FOR_UPDATE = "selectForUpdate"; // NOI18N
    public static final String PROP_STORED_PROCEDURES = "storedProcedures"; // NOI18N
    public static final String PROP_SUBQUERY_IN_COMPARSIONS = "subqueriesInComparisons"; // NOI18N
    public static final String PROP_SUBQUERY_IN_EXISTS = "subqueriesInExists"; // NOI18N
    public static final String PROP_SUBQUERY_IN_INS = "subqueriesInIns"; // NOI18N
    public static final String PROP_SUBQUERY_IN_QUANTIFIEDS = "subqueriesInQuantifieds"; // NOI18N
    public static final String PROP_CORRELATED_SUBQUERIES = "correlatedSubqueries"; // NOI18N
    public static final String PROP_UNION = "union"; // NOI18N
    public static final String PROP_UNION_ALL = "unionAll"; // NOI18N
    public static final String PROP_OPEN_CURSORS_ACROSS_COMMIT = "openCursorsAcrossCommit"; // NOI18N
    public static final String PROP_OPEN_CURSORS_ACROSS_ROLLBACK = "openCursorsAcrossRollback"; // NOI18N
    public static final String PROP_OPEN_STATEMENTS_ACROSS_COMMIT = "openStatementsAcrossCommit"; // NOI18N
    public static final String PROP_OPEN_STATEMENTS_ACROSS_ROLLBACK = "openStatementsAcrossRollback"; // NOI18N
    public static final String PROP_ROWSIZE_INCLUDING_BLOBS = "maxRowSizeIncludeBlobs"; // NOI18N
    public static final String PROP_TRANSACTIONS = "transactions"; // NOI18N
    public static final String PROP_DDL_AND_DML_TRANSACTIONS = "dataDefinitionAndDataManipulationTransactions"; // NOI18N
    public static final String PROP_DML_TRANSACTIONS_ONLY = "dataManipulationTransactionsOnly"; // NOI18N
    public static final String PROP_DDL_CAUSES_COMMIT = "dataDefinitionCausesTransactionCommit"; // NOI18N
    public static final String PROP_DDL_IGNORED_IN_TRANSACTIONS = "dataDefinitionIgnoredInTransactions"; // NOI18N
    public static final String PROP_BATCH_UPDATES = "batchUpdates"; // NOI18N

    // Integer properties

    public static final String PROP_NULL_SORT = "nullSort"; // NOI18N
    public static final String PROP_IDENTIFIER_STORE = "identifierStore"; // NOI18N
    public static final String PROP_QUOTED_IDENTS = "quotedIdentifierStore"; // NOI18N
    public static final String PROP_MAX_BINARY_LITERAL_LENGTH = "maxBinaryLiteralLength"; // NOI18N
    public static final String PROP_MAX_CHAR_LITERAL_LENGTH = "maxCharLiteralLength"; // NOI18N
    public static final String PROP_MAX_COLUMN_NAME_LENGTH = "maxColumnNameLength"; // NOI18N
    public static final String PROP_MAX_COLUMNS_IN_GROUPBY = "maxColumnsInGroupBy"; // NOI18N
    public static final String PROP_MAX_COLUMNS_IN_INDEX = "maxColumnsInIndex"; // NOI18N
    public static final String PROP_MAX_COLUMNS_IN_ORDERBY = "maxColumnsInOrderBy"; // NOI18N
    public static final String PROP_MAX_COLUMNS_IN_SELECT = "maxColumnsInSelect"; // NOI18N
    public static final String PROP_MAX_COLUMNS_IN_TABLE = "maxColumnsInTable"; // NOI18N
    public static final String PROP_MAX_CONNECTIONS = "maxConnections"; // NOI18N
    public static final String PROP_MAX_CURSORNAME_LENGTH = "maxCursorNameLength"; // NOI18N
    public static final String PROP_MAX_INDEX_LENGTH = "maxIndexLength"; // NOI18N
    public static final String PROP_MAX_SCHEMA_NAME = "maxSchemaNameLength"; // NOI18N
    public static final String PROP_MAX_PROCEDURE_NAME = "maxProcedureNameLength"; // NOI18N
    public static final String PROP_MAX_CATALOG_NAME = "maxCatalogNameLength"; // NOI18N
    public static final String PROP_MAX_ROW_SIZE = "maxRowSize"; // NOI18N
    public static final String PROP_MAX_STATEMENT_LENGTH = "maxStatementLength"; // NOI18N
    public static final String PROP_MAX_STATEMENTS = "maxStatements"; // NOI18N
    public static final String PROP_MAX_TABLENAME_LENGTH = "maxTableNameLength"; // NOI18N
    public static final String PROP_MAX_TABLES_IN_SELECT = "maxTablesInSelect"; // NOI18N
    public static final String PROP_MAX_USERNAME = "maxUserNameLength"; // NOI18N
    public static final String PROP_DEFAULT_ISOLATION = "defaultTransactionIsolation"; // NOI18N

    // String properties

    public static final String PROP_URL = "URL"; // NOI18N
    public static final String PROP_USERNAME = "userName"; // NOI18N
    public static final String PROP_PRODUCTNAME = "databaseProductName"; // NOI18N
    public static final String PROP_PRODUCTVERSION = "databaseProductVersion"; // NOI18N
    public static final String PROP_DRIVERNAME = "driverName"; // NOI18N
    public static final String PROP_DRIVER_VERSION = "driverVersion"; // NOI18N
    public static final String PROP_DRIVER_MAJOR_VERSION = "driverMajorVersion"; // NOI18N
    public static final String PROP_DRIVER_MINOR_VERSION = "driverMinorVersion"; // NOI18N
    public static final String PROP_IDENTIFIER_QUOTE = "identifierQuoteString"; // NOI18N
    public static final String PROP_SQL_KEYWORDS = "SQLKeywords"; // NOI18N
    public static final String PROP_NUMERIC_FUNCTIONS = "numericFunctions"; // NOI18N
    public static final String PROP_STRING_FUNCTIONS = "stringFunctions"; // NOI18N
    public static final String PROP_SYSTEM_FUNCTIONS = "systemFunctions"; // NOI18N
    public static final String PROP_TIME_FUNCTIONS = "timeDateFunctions"; // NOI18N
    public static final String PROP_STRING_ESCAPE = "searchStringEscape"; // NOI18N
    public static final String PROP_EXTRA_CHARACTERS = "extraNameCharacters"; // NOI18N
    public static final String PROP_SCHEMA_TERM = "schemaTerm"; // NOI18N
    public static final String PROP_PROCEDURE_TERM = "procedureTerm"; // NOI18N
    public static final String PROP_CATALOG_TERM = "catalogTerm"; // NOI18N
    public static final String PROP_CATALOGS_SEPARATOR = "catalogSeparator"; // NOI18N

    // Queries

    public static final String PROP_PROCEDURES_QUERY = "proceduresQuery"; // NOI18N
    public static final String PROP_PROCEDURE_COLUMNS_QUERY = "procedureColumnsQuery"; // NOI18N
    public static final String PROP_SCHEMAS_QUERY = "schemasQuery"; // NOI18N
    public static final String PROP_CATALOGS_QUERY = "catalogsQuery"; // NOI18N
    public static final String PROP_TABLES_QUERY = "tablesQuery"; // NOI18N
    public static final String PROP_TABLE_TYPES_QUERY = "tableTypesQuery"; // NOI18N
    public static final String PROP_COLUMNS_QUERY = "columnsQuery"; // NOI18N
    public static final String PROP_COLUMNS_PRIVILEGES_QUERY = "columnPrivilegesQuery"; // NOI18N
    public static final String PROP_TABLE_PRIVILEGES_QUERY = "tablePrivilegesQuery"; // NOI18N
    public static final String PROP_BEST_ROW_IDENTIFIER = "bestRowIdentifierQuery"; // NOI18N
    public static final String PROP_VERSION_COLUMNS = "versionColumnsQuery"; // NOI18N
    public static final String PROP_PK_QUERY = "primaryKeysQuery"; // NOI18N
    public static final String PROP_IK_QUERY = "importedKeysQuery"; // NOI18N
    public static final String PROP_EK_QUERY = "exportedKeysQuery"; // NOI18N
    public static final String PROP_CROSSREF_QUERY = "crossReferenceQuery"; // NOI18N
    public static final String PROP_TYPE_INFO_QUERY = "typeInfoQuery"; // NOI18N
    public static final String PROP_INDEX_INFO_QUERY = "indexInfoQuery"; // NOI18N
    public static final String PROP_UDT_QUERY = "UDTsQuery"; // NOI18N

    // Extended

    public static final String PROP_CAPITALIZE_USERNAME = "capitializeUsername"; // NOI18N

    static final long serialVersionUID =2490518619095829944L;
    public DefaultAdaptor()
    {
        propertySupport = new PropertyChangeSupport(this);
        properties = new HashMap();
    }

    public DefaultAdaptor(Connection conn) throws SQLException
    {
        propertySupport = new PropertyChangeSupport(this);
        properties = new HashMap();
        con = conn;
        if (con != null) dmd = con.getMetaData();
        else dmd = null;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener)
    {
        propertySupport.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener)
    {
        propertySupport.removePropertyChangeListener(listener);
    }

    public DatabaseMetaData getMetaData()
    {
        return this;
    }

    /**
    * Retrieves the connection that produced this metadata object.
    * @return the connection that produced this metadata object
    */
    public Connection getConnection() throws SQLException
    {
        return con;
    }

    /**
    * Sets the connection that produced this metadata object.
    * @return the connection that produced this metadata object
    */
    public void setConnection(Connection conn) throws SQLException
    {
        con = conn;
        if (con != null) dmd = con.getMetaData();
        else dmd = null;
    }

    private int getBoolean(String key)
    {
        Boolean val = (Boolean)properties.get(key);
        if (val != null) return (val.booleanValue() ? YES : NO);
        return NOT_SET;
    }

    private void setBoolean(String key, int value)
    {
        Boolean newValue, oldValue = (Boolean)properties.get(key);
        if (value != NOT_SET) {
            newValue = value == YES ? Boolean.TRUE : Boolean.FALSE;
            properties.put(key, newValue);
        } else {
            newValue = null;
            properties.remove(key);
        }

        propertySupport.firePropertyChange(key, oldValue, newValue);
    }

    private int getInt(String key)
    {
        Integer val = (Integer)properties.get(key);
        if (val == null) return NOT_SET;
        return val.intValue();
    }

    private void setInt(String key, int value)
    {
        Integer newValue, oldValue = (Integer)properties.get(key);
        if (value != NOT_SET) {
            newValue = Integer.valueOf(value);
            properties.put(key, newValue);
        } else {
            newValue = null;
            properties.remove(key);
        }

        propertySupport.firePropertyChange(key, oldValue, newValue);
    }

    private String getString(String key)
    {
        String val = (String)properties.get(key);
        if (val == null) return NOT_SET_STRING;
        return val;
    }

    private void setString(String key, String value)
    {
        String newValue, oldValue = (String)properties.get(key);
        if (value.length() > 0) {
            newValue = value;
            properties.put(key, newValue);
        } else {
            newValue = null;
            properties.remove(key);
        }

        propertySupport.firePropertyChange(key, oldValue, newValue);
    }

    // proceduresAreCallable

    public boolean allProceduresAreCallable() throws SQLException
    {
        Boolean proceduresAreCallable = (Boolean)properties.get(PROP_PROCEDURES_ARE_CALLABLE);
        if (proceduresAreCallable == null) {
            if (dmd != null) proceduresAreCallable = dmd.allProceduresAreCallable() ? Boolean.TRUE : Boolean.FALSE;
            else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
            properties.put(PROP_PROCEDURES_ARE_CALLABLE, proceduresAreCallable);
        }

        return proceduresAreCallable.booleanValue();
    }

    public int getProceduresAreCallable()
    {
        return getBoolean(PROP_PROCEDURES_ARE_CALLABLE);
    }

    public void setProceduresAreCallable(int value) throws SQLException
    {
        setBoolean(PROP_PROCEDURES_ARE_CALLABLE, value);
    }

    // tablesAreSelectable

    public boolean allTablesAreSelectable() throws SQLException
    {
        Boolean tablesAreSelectable = (Boolean)properties.get(PROP_TABLES_ARE_SELECTABLE);
        if (tablesAreSelectable == null) {
            if (dmd != null) tablesAreSelectable = dmd.allTablesAreSelectable() ? Boolean.TRUE : Boolean.FALSE;
            else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
            properties.put(PROP_TABLES_ARE_SELECTABLE, tablesAreSelectable);
        }

        return tablesAreSelectable.booleanValue();
    }

    public int getTablesAreSelectable()
    {
        return getBoolean(PROP_TABLES_ARE_SELECTABLE);
    }

    public void setTablesAreSelectable(int value)
    {
        setBoolean(PROP_TABLES_ARE_SELECTABLE, value);
    }

    // url

    public String getURL() throws SQLException
    {
        String url = (String)properties.get(PROP_URL);
        if (url == null) {
            if (dmd != null) url = dmd.getURL();
            else return NOT_SET_STRING;
            properties.put(PROP_URL, url);
        }

        return url;
    }

    public void setURL(String value)
    {
        setString(PROP_URL, value);
    }

    // username

    public String getUserName() throws SQLException
    {
        String username = (String)properties.get(PROP_USERNAME);
        if (username == null) {
            if (dmd != null) username = dmd.getUserName();
            else return NOT_SET_STRING;
            properties.put(PROP_USERNAME, username);
        }

        return username;
    }

    public void setUserName(String value)
    {
        setString(PROP_USERNAME, value);
    }

    // readonly

    public boolean isReadOnly() throws SQLException {
        Boolean readonly = (Boolean)properties.get(PROP_READONLY);
        if (readonly == null) {
            if (dmd != null)
                readonly = dmd.isReadOnly() ? Boolean.TRUE : Boolean.FALSE;
            else
                throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
            properties.put(PROP_READONLY, readonly);
        }

        return readonly.booleanValue();
    }

    //	public int getReadOnly() {
    public int getreadOnly() {
        return getBoolean(PROP_READONLY);
    }

    //	public void setReadOnly(int flag) {
    public void setreadOnly(int flag) {
        setBoolean(PROP_READONLY, flag);
    }

    // nullSort

    public int getNullSort()
    {
        return getInt(PROP_NULL_SORT);
    }

    public void setNullSort(int value)
    {
        setInt(PROP_NULL_SORT, value);
    }

    public boolean nullsAreSortedHigh() throws SQLException
    {
        Integer nullSort = (Integer)properties.get(PROP_NULL_SORT);
        if (nullSort != null) return (nullSort.intValue() == NULLSORT_HIGH);
        if (dmd != null) return dmd.nullsAreSortedAtStart();
        throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
    }

    public boolean nullsAreSortedLow() throws SQLException
    {
        Integer nullSort = (Integer)properties.get(PROP_NULL_SORT);
        if (nullSort != null) return (nullSort.intValue() == NULLSORT_LOW);
        if (dmd != null) return dmd.nullsAreSortedLow();
        throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
    }

    public boolean nullsAreSortedAtStart() throws SQLException
    {
        Integer nullSort = (Integer)properties.get(PROP_NULL_SORT);
        if (nullSort != null) return (nullSort.intValue() == NULLSORT_START);
        if (dmd != null) return dmd.nullsAreSortedAtStart();
        throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
    }

    public boolean nullsAreSortedAtEnd() throws SQLException
    {
        Integer nullSort = (Integer)properties.get(PROP_NULL_SORT);
        if (nullSort != null) return (nullSort.intValue() == NULLSORT_END);
        if (dmd != null) return dmd.nullsAreSortedAtEnd();
        throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
    }

    // product

    public String getDatabaseProductName() throws SQLException
    {
        String product = (String)properties.get(PROP_PRODUCTNAME);
        if (product == null) {
            if (dmd != null && dmd.getDatabaseProductName() != null) {
                product = dmd.getDatabaseProductName().trim();
            } else {
                return NOT_SET_STRING;
            }
            properties.put(PROP_PRODUCTNAME, product);
        }

        return product;
    }

    public void setDatabaseProductName(String value)
    {
        setString(PROP_PRODUCTNAME, value);
    }

    // version

    public String getDatabaseProductVersion() throws SQLException
    {
        String version = (String)properties.get(PROP_PRODUCTVERSION);
        if (version == null) {
            if (dmd != null) version = dmd.getDatabaseProductVersion();
            else return NOT_SET_STRING;
            properties.put(PROP_PRODUCTVERSION, version);
        }

        return version;
    }

    public void setDatabaseProductVersion(String value)
    {
        setString(PROP_PRODUCTVERSION, value);
    }

    // driverName

    public String getDriverName() throws SQLException
    {
        String driverName = (String)properties.get(PROP_DRIVERNAME);
        if (driverName == null) {
            if (dmd != null) driverName = dmd.getDriverName();
            else return NOT_SET_STRING;
            properties.put(PROP_DRIVERNAME, driverName);
        }

        return driverName;
    }

    public void setDriverName(String value)
    {
        setString(PROP_DRIVERNAME, value);
    }

    /**
    * What's the version of this JDBC driver?
    * @return JDBC driver version
    * @exception SQLException if a database access error occurs
    */
    public String getDriverVersion() throws SQLException
    {
        String driverVersion = (String)properties.get(PROP_DRIVER_VERSION);
        if (driverVersion == null) {
            if (dmd != null) driverVersion = dmd.getDriverVersion();
            else return NOT_SET_STRING;
            properties.put(PROP_DRIVER_VERSION, driverVersion);
        }

        return driverVersion;
    }

    public void setDriverVersion(String value)
    {
        setString(PROP_DRIVER_VERSION, value);
    }

    /**
    * What's this JDBC driver's major version number?
    * @return JDBC driver major version
    */
    public int getDriverMajorVersion()
    {
        Integer driverMajorVersion = (Integer)properties.get(PROP_DRIVER_MAJOR_VERSION);
        if (driverMajorVersion == null) {
            if (dmd != null) driverMajorVersion = Integer.valueOf(dmd.getDriverMajorVersion());
            else driverMajorVersion = Integer.valueOf(NOT_SET);
            properties.put(PROP_DRIVER_MAJOR_VERSION, driverMajorVersion);
        }

        return driverMajorVersion.intValue();
    }

    public void setDriverMajorVersion(int value)
    {
        setInt(PROP_DRIVER_MAJOR_VERSION, value);
    }

    /**
    * What's this JDBC driver's minor version number?
    * @return JDBC driver minor version number
    */
    public int getDriverMinorVersion()
    {
        Integer driverMinorVersion = (Integer)properties.get(PROP_DRIVER_MINOR_VERSION);
        if (driverMinorVersion == null) {
            if (dmd != null) driverMinorVersion = Integer.valueOf(dmd.getDriverMinorVersion());
            else driverMinorVersion = Integer.valueOf(NOT_SET);
            properties.put(PROP_DRIVER_MINOR_VERSION, driverMinorVersion);
        }

        return driverMinorVersion.intValue();
    }

    public void setDriverMinorVersion(int value)
    {
        setInt(PROP_DRIVER_MINOR_VERSION, value);
    }

    /**
    * Does the database store tables in a local file?
    * @return <code>true</code> if so
    * @exception SQLException if a database access error occurs
    */
    public boolean usesLocalFiles() throws SQLException
    {
        Boolean localFiles = (Boolean)properties.get(PROP_LOCAL_FILES);
        if (localFiles == null) {
            if (dmd != null) localFiles = dmd.usesLocalFiles() ? Boolean.TRUE : Boolean.FALSE;
            else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
            properties.put(PROP_LOCAL_FILES, localFiles);
        }

        return localFiles.booleanValue();
    }

    public int getLocalFiles()
    {
        return getBoolean(PROP_LOCAL_FILES);
    }

    public void setLocalFiles(int value)
    {
        setBoolean(PROP_LOCAL_FILES, value);
    }

    /**
    * Does the database use a file for each table?
    * @return true if the database uses a local file for each table
    * @exception SQLException if a database access error occurs
    */
    public boolean usesLocalFilePerTable() throws SQLException
    {
        Boolean filePerTable = (Boolean)properties.get(PROP_FILE_PER_TABLE);
        if (filePerTable == null) {
            if (dmd != null) filePerTable = dmd.usesLocalFilePerTable() ? Boolean.TRUE : Boolean.FALSE;
            else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
            properties.put(PROP_FILE_PER_TABLE, filePerTable);
        }

        return filePerTable.booleanValue();
    }

    public int getLocalFilePerTable()
    {
        return getBoolean(PROP_FILE_PER_TABLE);
    }

    public void setLocalFilePerTable(int value)
    {
        setBoolean(PROP_FILE_PER_TABLE, value);
    }

    /**
    * Does the database treat mixed case unquoted SQL identifiers as
    * case sensitive and as a result store them in mixed case?
    * @return <code>true</code> if so
    */
    public boolean supportsMixedCaseIdentifiers() throws SQLException
    {
        Boolean mixedCaseIdentifiers = (Boolean)properties.get(PROP_MIXEDCASE_IDENTIFIERS);
        if (mixedCaseIdentifiers == null) {
            if (dmd != null) mixedCaseIdentifiers = dmd.supportsMixedCaseIdentifiers() ? Boolean.TRUE : Boolean.FALSE;
            else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
            properties.put(PROP_MIXEDCASE_IDENTIFIERS, mixedCaseIdentifiers);
        }

        return mixedCaseIdentifiers.booleanValue();
    }

    public int getMixedCaseIdentifiers()
    {
        return getBoolean(PROP_MIXEDCASE_IDENTIFIERS);
    }

    public void setMixedCaseIdentifiers(int value)
    {
        setBoolean(PROP_MIXEDCASE_IDENTIFIERS, value);
    }

    // identifier store

    public int getIdentifierStore()
    {
        return getInt(PROP_IDENTIFIER_STORE);
    }

    public void setIdentifierStore(int value)
    {
        setInt(PROP_IDENTIFIER_STORE, value);
    }

    /**
    * Does the database treat mixed case unquoted SQL identifiers as
    * case insensitive and store them in upper case?
    * @return <code>true</code> if so
    * @exception SQLException if a database access error occurs
    */
    public boolean storesUpperCaseIdentifiers() throws SQLException
    {
        Integer identStore = (Integer)properties.get(PROP_IDENTIFIER_STORE);
        if (identStore != null) return (identStore.intValue() == STORE_UPPERCASE);
        if (dmd != null) return dmd.storesUpperCaseIdentifiers();
        else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
    }

    /**
    * Does the database treat mixed case unquoted SQL identifiers as
    * case insensitive and store them in lower case?
    * @return <code>true</code> if so
    * @exception SQLException if a database access error occurs
    */
    public boolean storesLowerCaseIdentifiers() throws SQLException
    {
        Integer identStore = (Integer)properties.get(PROP_IDENTIFIER_STORE);
        if (identStore != null) return (identStore.intValue() == STORE_LOWERCASE);
        if (dmd != null) return dmd.storesLowerCaseIdentifiers();
        else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
    }

    /**
    * Does the database treat mixed case unquoted SQL identifiers as
    * case insensitive and store them in mixed case?
    * @return <code>true</code> if so
    * @exception SQLException if a database access error occurs
    */
    public boolean storesMixedCaseIdentifiers() throws SQLException
    {
        Integer identStore = (Integer)properties.get(PROP_IDENTIFIER_STORE);
        if (identStore != null) return (identStore.intValue() == STORE_MIXEDCASE);
        if (dmd != null) return dmd.storesLowerCaseIdentifiers();
        else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
    }

    /**
    * Does the database treat mixed case quoted SQL identifiers as
    * case sensitive and as a result store them in mixed case?
    * A JDBC Compliant<sup><font size=-2>TM</font></sup> driver will always return true.
    * @return <code>true</code> if so
    * @exception SQLException if a database access error occurs
    */
    public boolean supportsMixedCaseQuotedIdentifiers() throws SQLException
    {
        Boolean mixedCaseQuotedIdentifiers = (Boolean)properties.get(PROP_MIXEDCASE_QUOTED_IDENTIFIERS);
        if (mixedCaseQuotedIdentifiers == null) {
            if (dmd != null) mixedCaseQuotedIdentifiers = dmd.supportsMixedCaseQuotedIdentifiers() ? Boolean.TRUE : Boolean.FALSE;
            else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
            properties.put(PROP_MIXEDCASE_QUOTED_IDENTIFIERS, mixedCaseQuotedIdentifiers);
        }

        return mixedCaseQuotedIdentifiers.booleanValue();
    }

    public int getMixedCaseQuotedIdentifiers()
    {
        return getBoolean(PROP_MIXEDCASE_QUOTED_IDENTIFIERS);
    }

    public void setMixedCaseQuotedIdentifiers(int value)
    {
        setBoolean(PROP_MIXEDCASE_QUOTED_IDENTIFIERS, value);
    }

    // quoted store

    public int getQuotedIdentifierStore()
    {
        return getInt(PROP_QUOTED_IDENTS);
    }

    public void setQuotedIdentifierStore(int value)
    {
        setInt(PROP_QUOTED_IDENTS, value);
    }

    /**
    * Does the database treat mixed case quoted SQL identifiers as
    * case insensitive and store them in upper case?
    * @return <code>true</code> if so
    * @exception SQLException if a database access error occurs
    */
    public boolean storesUpperCaseQuotedIdentifiers() throws SQLException
    {
        Integer identQuotedStore = (Integer)properties.get(PROP_QUOTED_IDENTS);
        if (identQuotedStore != null) return (identQuotedStore.intValue() == STORE_UPPERCASE);
        if (dmd != null) return dmd.storesUpperCaseQuotedIdentifiers();
        throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
    }

    /**
    * Does the database treat mixed case quoted SQL identifiers as
    * case insensitive and store them in lower case?
    * @return <code>true</code> if so
    * @exception SQLException if a database access error occurs
    */
    public boolean storesLowerCaseQuotedIdentifiers() throws SQLException
    {
        Integer identQuotedStore = (Integer)properties.get(PROP_QUOTED_IDENTS);
        if (identQuotedStore != null) return (identQuotedStore.intValue() == STORE_LOWERCASE);
        if (dmd != null) return dmd.storesLowerCaseQuotedIdentifiers();
        throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
    }

    /**
    * Does the database treat mixed case quoted SQL identifiers as
    * case insensitive and store them in mixed case?
    * @return <code>true</code> if so
    */
    public boolean storesMixedCaseQuotedIdentifiers() throws SQLException
    {
        Integer identQuotedStore = (Integer)properties.get(PROP_QUOTED_IDENTS);
        if (identQuotedStore != null) return (identQuotedStore.intValue() == STORE_MIXEDCASE);
        if (dmd != null) return dmd.storesMixedCaseQuotedIdentifiers();
        throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
    }

    /**
    * What's the string used to quote SQL identifiers?
    * This returns a space " " if identifier quoting isn't supported.
    * @return the quoting string
    * @exception SQLException if a database access error occurs
    */
    public String getIdentifierQuoteString() throws SQLException
    {
        String identifierQuoteString = (String)properties.get(PROP_IDENTIFIER_QUOTE);
        if (identifierQuoteString == null) {
            if (dmd != null) identifierQuoteString = dmd.getIdentifierQuoteString();
            else return NOT_SET_STRING;
            properties.put(PROP_IDENTIFIER_QUOTE, identifierQuoteString);
        }

        return identifierQuoteString;
    }

    public void setIdentifierQuoteString(String value)
    {
        setString(PROP_IDENTIFIER_QUOTE, value);
    }

    /**
    * Gets a comma-separated list of all a database's SQL keywords
    * that are NOT also SQL92 keywords.
    * @return the list 
    * @exception SQLException if a database access error occurs
    */
    public String getSQLKeywords() throws SQLException
    {
        String sqlKeywords = (String)properties.get(PROP_SQL_KEYWORDS);
        if (sqlKeywords == null) {
            if (dmd != null) sqlKeywords = dmd.getSQLKeywords();
            else return NOT_SET_STRING;
            properties.put(PROP_SQL_KEYWORDS, sqlKeywords);
        }

        return sqlKeywords;
    }

    public void setSQLKeywords(String value)
    {
        setString(PROP_SQL_KEYWORDS, value);
    }

    /**
    * Gets a comma-separated list of math functions.  These are the 
    * X/Open CLI math function names used in the JDBC function escape 
    * clause.
    * @return the list
    * @exception SQLException if a database access error occurs
    */
    public String getNumericFunctions() throws SQLException
    {
        String numericFunctions = (String)properties.get(PROP_NUMERIC_FUNCTIONS);
        if (numericFunctions == null) {
            if (dmd != null) numericFunctions = dmd.getNumericFunctions();
            else return NOT_SET_STRING;
            properties.put(PROP_NUMERIC_FUNCTIONS, numericFunctions);
        }

        return numericFunctions;
    }

    public void setNumericFunctions(String value)
    {
        setString(PROP_NUMERIC_FUNCTIONS, value);
    }

    /**
    * Gets a comma-separated list of string functions.  These are the 
    * X/Open CLI string function names used in the JDBC function escape 
    * clause.
    * @return the list
    * @exception SQLException if a database access error occurs
    */
    public String getStringFunctions() throws SQLException
    {
        String stringFunctions = (String)properties.get(PROP_STRING_FUNCTIONS);
        if (stringFunctions == null) {
            if (dmd != null) stringFunctions = dmd.getStringFunctions();
            else return NOT_SET_STRING;
            properties.put(PROP_STRING_FUNCTIONS, stringFunctions);
        }

        return stringFunctions;
    }

    public void setStringFunctions(String value)
    {
        setString(PROP_STRING_FUNCTIONS, value);
    }

    /**
    * Gets a comma-separated list of system functions.  These are the 
    * X/Open CLI system function names used in the JDBC function escape 
    * clause.
    * @return the list
    * @exception SQLException if a database access error occurs
    */
    public String getSystemFunctions() throws SQLException
    {
        String systemFunctions = (String)properties.get(PROP_SYSTEM_FUNCTIONS);
        if (systemFunctions == null) {
            if (dmd != null) systemFunctions = dmd.getSystemFunctions();
            else return NOT_SET_STRING;
            properties.put(PROP_SYSTEM_FUNCTIONS, systemFunctions);
        }

        return systemFunctions;
    }

    public void setSystemFunctions(String value)
    {
        setString(PROP_SYSTEM_FUNCTIONS, value);
    }

    /**
    * Gets a comma-separated list of time and date functions.
    * @return the list
    * @exception SQLException if a database access error occurs
    */
    public String getTimeDateFunctions() throws SQLException
    {
        String timeFunctions = (String)properties.get(PROP_TIME_FUNCTIONS);
        if (timeFunctions == null) {
            if (dmd != null) timeFunctions = dmd.getTimeDateFunctions();
            else return NOT_SET_STRING;
            properties.put(PROP_TIME_FUNCTIONS, timeFunctions);
        }

        return timeFunctions;
    }

    public void setTimeDateFunctions(String value)
    {
        setString(PROP_TIME_FUNCTIONS, value);
    }

    /**
    * Gets the string that can be used to escape wildcard characters.
    * This is the string that can be used to escape '_' or '%' in
    * the string pattern style catalog search parameters.
    * <P>The '_' character represents any single character.
    * <P>The '%' character represents any sequence of zero or 
    * more characters.
    * @return the string used to escape wildcard characters
    * @exception SQLException if a database access error occurs
    */
    public String getSearchStringEscape() throws SQLException
    {
        String stringEscape = (String)properties.get(PROP_STRING_ESCAPE);
        if (stringEscape == null) {
            if (dmd != null) stringEscape = dmd.getSearchStringEscape();
            else return NOT_SET_STRING;
            properties.put(PROP_STRING_ESCAPE, stringEscape);
        }

        return stringEscape;
    }

    public void setSearchStringEscape(String value)
    {
        setString(PROP_STRING_ESCAPE, value);
    }

    /**
    * Gets all the "extra" characters that can be used in unquoted
    * identifier names (those beyond a-z, A-Z, 0-9 and _).
    * @return the string containing the extra characters 
    * @exception SQLException if a database access error occurs
    */
    public String getExtraNameCharacters() throws SQLException
    {
        String extraCharacters = (String)properties.get(PROP_EXTRA_CHARACTERS);
        if (extraCharacters == null) {
            if (dmd != null) extraCharacters = dmd.getExtraNameCharacters();
            else return NOT_SET_STRING;
            properties.put(PROP_EXTRA_CHARACTERS, extraCharacters);
        }

        return extraCharacters;
    }

    public void setExtraNameCharacters(String value)
    {
        setString(PROP_EXTRA_CHARACTERS, value);
    }

    /**
    * Is "ALTER TABLE" with add column supported?
    * @return <code>true</code> if so
    */
    public boolean supportsAlterTableWithAddColumn() throws SQLException
    {
        Boolean alterAdd = (Boolean)properties.get(PROP_ALTER_ADD);
        if (alterAdd == null) {
            if (dmd != null) alterAdd = dmd.supportsAlterTableWithAddColumn() ? Boolean.TRUE : Boolean.FALSE;
            else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
            properties.put(PROP_ALTER_ADD, alterAdd);
        }

        return alterAdd.booleanValue();
    }

    public int getAlterTableWithAddColumn()
    {
        return getBoolean(PROP_ALTER_ADD);
    }

    public void setAlterTableWithAddColumn(int value)
    {
        setBoolean(PROP_ALTER_ADD, value);
    }

    /**
    * Is "ALTER TABLE" with drop column supported?
    * @return <code>true</code> if so
    * @exception SQLException if a database access error occurs
    */
    public boolean supportsAlterTableWithDropColumn() throws SQLException
    {
        Boolean alterDrop = (Boolean)properties.get(PROP_ALTER_DROP);
        if (alterDrop == null) {
            if (dmd != null) alterDrop = dmd.supportsAlterTableWithDropColumn() ? Boolean.TRUE : Boolean.FALSE;
            else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
            properties.put(PROP_ALTER_DROP, alterDrop);
        }

        return alterDrop.booleanValue();
    }

    public int getAlterTableWithDropColumn()
    {
        return getBoolean(PROP_ALTER_DROP);
    }

    public void setAlterTableWithDropColumn(int value)
    {
        setBoolean(PROP_ALTER_DROP, value);
    }

    /**
    * Is column aliasing supported? 
    * <P>If so, the SQL AS clause can be used to provide names for
    * computed columns or to provide alias names for columns as
    * required.
    * @return <code>true</code> if so
    */
    public boolean supportsColumnAliasing() throws SQLException
    {
        Boolean columnAliasing = (Boolean)properties.get(PROP_COLUMN_ALIASING);
        if (columnAliasing == null) {
            if (dmd != null) columnAliasing = dmd.supportsColumnAliasing() ? Boolean.TRUE : Boolean.FALSE;
            else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
            properties.put(PROP_COLUMN_ALIASING, columnAliasing);
        }

        return columnAliasing.booleanValue();
    }

    public int getColumnAliasing()
    {
        return getBoolean(PROP_COLUMN_ALIASING);
    }

    public void setColumnAliasing(int value)
    {
        setBoolean(PROP_COLUMN_ALIASING, value);
    }

    /**
    * Are concatenations between NULL and non-NULL values NULL?
    * A JDBC Compliant<sup><font size=-2>TM</font></sup> driver always returns true.
    * @return <code>true</code> if so
    */
    public boolean nullPlusNonNullIsNull() throws SQLException
    {
        Boolean nullPlusNull = (Boolean)properties.get(PROP_NULL_PLUS_NULL_IS_NULL);
        if (nullPlusNull == null) {
            if (dmd != null) nullPlusNull = dmd.nullPlusNonNullIsNull() ? Boolean.TRUE : Boolean.FALSE;
            else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
            properties.put(PROP_NULL_PLUS_NULL_IS_NULL, nullPlusNull);
        }

        return nullPlusNull.booleanValue();
    }

    public int getNullPlusNonNullIsNull()
    {
        return getBoolean(PROP_NULL_PLUS_NULL_IS_NULL);
    }

    public void setNullPlusNonNullIsNull(int value)
    {
        setBoolean(PROP_NULL_PLUS_NULL_IS_NULL, value);
    }

    /**
    * Is the CONVERT function between SQL types supported?
    * @return <code>true</code> if so
    * @exception SQLException if a database access error occurs
    */
    public boolean supportsConvert() throws SQLException
    {
        Boolean supportsConvert = (Boolean)properties.get(PROP_CONVERT);
        if (supportsConvert == null) {
            if (dmd != null) supportsConvert = dmd.supportsConvert() ? Boolean.TRUE : Boolean.FALSE;
            else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
            properties.put(PROP_CONVERT, supportsConvert);
        }

        return supportsConvert.booleanValue();
    }

    public int getConvert()
    {
        return getBoolean(PROP_CONVERT);
    }

    public void setConvert(int value)
    {
        setBoolean(PROP_CONVERT, value);
    }

    /**
    * Is CONVERT between the given SQL types supported?
    * @param fromType the type to convert from
    * @param toType the type to convert to     
    * @return <code>true</code> if so
    */
    public boolean supportsConvert(int fromType, int toType) throws SQLException
    {
        if (dmd != null) return dmd.supportsConvert(fromType, toType);
        else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
    }

    /**
    * Are table correlation names supported?
    * A JDBC Compliant<sup><font size=-2>TM</font></sup> driver always returns true.
    * @return <code>true</code> if so
    * @exception SQLException if a database access error occurs
    */
    public boolean supportsTableCorrelationNames() throws SQLException
    {
        Boolean nameCorrelation = (Boolean)properties.get(PROP_TABLE_CORRELATION_NAMES);
        if (nameCorrelation == null) {
            if (dmd != null) nameCorrelation = dmd.supportsTableCorrelationNames() ? Boolean.TRUE : Boolean.FALSE;
            else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
            properties.put(PROP_TABLE_CORRELATION_NAMES, nameCorrelation);
        }

        return nameCorrelation.booleanValue();
    }

    public int getTableCorrelationNames()
    {
        return getBoolean(PROP_TABLE_CORRELATION_NAMES);
    }

    public void setTableCorrelationNames(int value)
    {
        setBoolean(PROP_TABLE_CORRELATION_NAMES, value);
    }

    /**
    * If table correlation names are supported, are they restricted
    * to be different from the names of the tables?
    * @exception SQLException if a database access error occurs
    */
    public boolean supportsDifferentTableCorrelationNames() throws SQLException
    {
        Boolean tableCorrelation = (Boolean)properties.get(PROP_DIFF_TABLE_CORRELATION_NAMES);
        if (tableCorrelation == null) {
            if (dmd != null) tableCorrelation = dmd.supportsDifferentTableCorrelationNames() ? Boolean.TRUE : Boolean.FALSE;
            else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
            properties.put(PROP_DIFF_TABLE_CORRELATION_NAMES, tableCorrelation);
        }

        return tableCorrelation.booleanValue();
    }

    public int getDifferentTableCorrelationNames()
    {
        return getBoolean(PROP_DIFF_TABLE_CORRELATION_NAMES);
    }

    public void setDifferentTableCorrelationNames(int value)
    {
        setBoolean(PROP_DIFF_TABLE_CORRELATION_NAMES, value);
    }

    /**
    * Are expressions in "ORDER BY" lists supported?
    * @return <code>true</code> if so
    * @exception SQLException if a database access error occurs
    */
    public boolean supportsExpressionsInOrderBy() throws SQLException
    {
        Boolean ordering = (Boolean)properties.get(PROP_EXPRESSIONS_IN_ORDERBY);
        if (ordering == null) {
            if (dmd != null) ordering = dmd.supportsExpressionsInOrderBy() ? Boolean.TRUE : Boolean.FALSE;
            else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
            properties.put(PROP_EXPRESSIONS_IN_ORDERBY, ordering);
        }

        return ordering.booleanValue();
    }

    public int getExpressionsInOrderBy()
    {
        return getBoolean(PROP_EXPRESSIONS_IN_ORDERBY);
    }

    public void setExpressionsInOrderBy(int value)
    {
        setBoolean(PROP_EXPRESSIONS_IN_ORDERBY, value);
    }

    /**
    * Can an "ORDER BY" clause use columns not in the SELECT statement?
    * @return <code>true</code> if so
    * @exception SQLException if a database access error occurs
    */
    public boolean supportsOrderByUnrelated() throws SQLException
    {
        Boolean unrelatedOrdering = (Boolean)properties.get(PROP_ORDER_BY_UNRELATED);
        if (unrelatedOrdering == null) {
            if (dmd != null) unrelatedOrdering = dmd.supportsOrderByUnrelated() ? Boolean.TRUE : Boolean.FALSE;
            else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
            properties.put(PROP_ORDER_BY_UNRELATED, unrelatedOrdering);
        }

        return unrelatedOrdering.booleanValue();
    }

    public int getOrderByUnrelated()
    {
        return getBoolean(PROP_ORDER_BY_UNRELATED);
    }

    public void setOrderByUnrelated(int value)
    {
        setBoolean(PROP_ORDER_BY_UNRELATED, value);
    }

    /**
    * Is some form of "GROUP BY" clause supported?
    * @return <code>true</code> if so
    */
    public boolean supportsGroupBy() throws SQLException
    {
        Boolean groupBy = (Boolean)properties.get(PROP_GROUP_BY);
        if (groupBy == null) {
            if (dmd != null) groupBy = dmd.supportsGroupBy() ? Boolean.TRUE : Boolean.FALSE;
            else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
            properties.put(PROP_GROUP_BY, groupBy);
        }

        return groupBy.booleanValue();
    }

    public int getGroupBy()
    {
        return getBoolean(PROP_GROUP_BY);
    }

    public void setGroupBy(int value)
    {
        setBoolean(PROP_GROUP_BY, value);
    }

    /**
    * Can a "GROUP BY" clause use columns not in the SELECT?
    * @return <code>true</code> if so
    * @exception SQLException if a database access error occurs
    */
    public boolean supportsGroupByUnrelated() throws SQLException
    {
        Boolean unrelatedGroupBy = (Boolean)properties.get(PROP_UNRELATED_GROUP_BY);
        if (unrelatedGroupBy == null) {
            if (dmd != null) unrelatedGroupBy = dmd.supportsGroupByUnrelated() ? Boolean.TRUE : Boolean.FALSE;
            else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
            properties.put(PROP_UNRELATED_GROUP_BY, unrelatedGroupBy);
        }

        return unrelatedGroupBy.booleanValue();
    }

    public int getGroupByUnrelated()
    {
        return getBoolean(PROP_UNRELATED_GROUP_BY);
    }

    public void setGroupByUnrelated(int value)
    {
        setBoolean(PROP_UNRELATED_GROUP_BY, value);
    }

    /**
    * Can a "GROUP BY" clause add columns not in the SELECT
    * provided it specifies all the columns in the SELECT?
    * @return <code>true</code> if so
    */
    public boolean supportsGroupByBeyondSelect() throws SQLException
    {
        Boolean beyondGroupBy = (Boolean)properties.get(PROP_BEYOND_GROUP_BY);
        if (beyondGroupBy == null) {
            if (dmd != null) beyondGroupBy = dmd.supportsGroupByBeyondSelect() ? Boolean.TRUE : Boolean.FALSE;
            else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
            properties.put(PROP_BEYOND_GROUP_BY, beyondGroupBy);
        }

        return beyondGroupBy.booleanValue();
    }

    public int getGroupByBeyondSelect()
    {
        return getBoolean(PROP_BEYOND_GROUP_BY);
    }

    public void setGroupByBeyondSelect(int value)
    {
        setBoolean(PROP_BEYOND_GROUP_BY, value);
    }

    /**
    * Is the escape character in "LIKE" clauses supported?
    * A JDBC Compliant<sup><font size=-2>TM</font></sup> driver always returns true.
    * @return <code>true</code> if so
    * @exception SQLException if a database access error occurs
    */
    public boolean supportsLikeEscapeClause() throws SQLException
    {
        Boolean escapeLike = (Boolean)properties.get(PROP_ESCAPE_LIKE);
        if (escapeLike == null) {
            if (dmd != null) escapeLike = dmd.supportsLikeEscapeClause() ? Boolean.TRUE : Boolean.FALSE;
            else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
            properties.put(PROP_ESCAPE_LIKE, escapeLike);
        }

        return escapeLike.booleanValue();
    }

    public int getLikeEscapeClause()
    {
        return getBoolean(PROP_ESCAPE_LIKE);
    }

    public void setLikeEscapeClause(int value)
    {
        setBoolean(PROP_ESCAPE_LIKE, value);
    }

    /**
    * Are multiple ResultSets from a single execute supported?
    * @return <code>true</code> if so
    * @exception SQLException if a database access error occurs
    */
    public boolean supportsMultipleResultSets() throws SQLException
    {
        Boolean multipleResultSets = (Boolean)properties.get(PROP_MULTIPLE_RS);
        if (multipleResultSets == null) {
            if (dmd != null) multipleResultSets = dmd.supportsMultipleResultSets() ? Boolean.TRUE : Boolean.FALSE;
            else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
            properties.put(PROP_MULTIPLE_RS, multipleResultSets);
        }

        return multipleResultSets.booleanValue();
    }

    public int getMultipleResultSets()
    {
        return getBoolean(PROP_MULTIPLE_RS);
    }

    public void setMultipleResultSets(int value)
    {
        setBoolean(PROP_MULTIPLE_RS, value);
    }

    /**
    * Can we have multiple transactions open at once (on different
    * connections)?
    * @return <code>true</code> if so
    * @exception SQLException if a database access error occurs
    */
    public boolean supportsMultipleTransactions() throws SQLException
    {
        Boolean multipleTransactions = (Boolean)properties.get(PROP_MULTIPLE_TRANSACTIONS);
        if (multipleTransactions == null) {
            if (dmd != null) multipleTransactions = dmd.supportsMultipleTransactions() ? Boolean.TRUE : Boolean.FALSE;
            else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
            properties.put(PROP_MULTIPLE_TRANSACTIONS, multipleTransactions);
        }

        return multipleTransactions.booleanValue();
    }

    public int getMultipleTransactions()
    {
        return getBoolean(PROP_MULTIPLE_TRANSACTIONS);
    }

    public void setMultipleTransactions(int value)
    {
        setBoolean(PROP_MULTIPLE_TRANSACTIONS, value);
    }

    /**
    * Can columns be defined as non-nullable?
    * A JDBC Compliant<sup><font size=-2>TM</font></sup> driver always returns true.
    * @exception SQLException if a database access error occurs
    */
    public boolean supportsNonNullableColumns() throws SQLException
    {
        Boolean nunNullableColumns = (Boolean)properties.get(PROP_NON_NULL_COLUMNSS);
        if (nunNullableColumns == null) {
            if (dmd != null) nunNullableColumns = dmd.supportsNonNullableColumns() ? Boolean.TRUE : Boolean.FALSE;
            else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
            properties.put(PROP_NON_NULL_COLUMNSS, nunNullableColumns);
        }

        return nunNullableColumns.booleanValue();
    }

    public int getNonNullableColumns()
    {
        return getBoolean(PROP_NON_NULL_COLUMNSS);
    }

    public void setNonNullableColumns(int value)
    {
        setBoolean(PROP_NON_NULL_COLUMNSS, value);
    }

    /**
    * Is the ODBC Minimum SQL grammar supported?
    * All JDBC Compliant<sup><font size=-2>TM</font></sup> drivers must return true.
    * @return <code>true</code> if so
    * @exception SQLException if a database access error occurs
    */
    public boolean supportsMinimumSQLGrammar() throws SQLException
    {
        Boolean minimumSQLGrammar = (Boolean)properties.get(PROP_MINUMUM_SQL_GRAMMAR);
        if (minimumSQLGrammar == null) {
            if (dmd != null) minimumSQLGrammar = dmd.supportsMinimumSQLGrammar() ? Boolean.TRUE : Boolean.FALSE;
            else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
            properties.put(PROP_MINUMUM_SQL_GRAMMAR, minimumSQLGrammar);
        }

        return minimumSQLGrammar.booleanValue();
    }

    public int getMinimumSQLGrammar()
    {
        return getBoolean(PROP_MINUMUM_SQL_GRAMMAR);
    }

    public void setMinimumSQLGrammar(int value)
    {
        setBoolean(PROP_MINUMUM_SQL_GRAMMAR, value);
    }

    /**
    * Is the ODBC Core SQL grammar supported?
    * @return <code>true</code> if so
    * @exception SQLException if a database access error occurs
    */
    public boolean supportsCoreSQLGrammar() throws SQLException
    {
        Boolean coreSQLGrammar = (Boolean)properties.get(PROP_CORE_SQL_GRAMMAR);
        if (coreSQLGrammar == null) {
            if (dmd != null) coreSQLGrammar = dmd.supportsCoreSQLGrammar() ? Boolean.TRUE : Boolean.FALSE;
            else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
            properties.put(PROP_CORE_SQL_GRAMMAR, coreSQLGrammar);
        }

        return coreSQLGrammar.booleanValue();
    }

    public int getCoreSQLGrammar()
    {
        return getBoolean(PROP_CORE_SQL_GRAMMAR);
    }

    public void setCoreSQLGrammar(int value)
    {
        setBoolean(PROP_CORE_SQL_GRAMMAR, value);
    }

    /**
    * Is the ODBC Extended SQL grammar supported?
    * @return <code>true</code> if so
    */
    public boolean supportsExtendedSQLGrammar() throws SQLException
    {
        Boolean extendedSQLGrammar = (Boolean)properties.get(PROP_EXTENDED_SQL_GRAMMAR);
        if (extendedSQLGrammar == null) {
            if (dmd != null) extendedSQLGrammar = dmd.supportsExtendedSQLGrammar() ? Boolean.TRUE : Boolean.FALSE;
            else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
            properties.put(PROP_EXTENDED_SQL_GRAMMAR, extendedSQLGrammar);
        }

        return extendedSQLGrammar.booleanValue();
    }

    public int getExtendedSQLGrammar()
    {
        return getBoolean(PROP_EXTENDED_SQL_GRAMMAR);
    }

    public void setExtendedSQLGrammar(int value)
    {
        setBoolean(PROP_EXTENDED_SQL_GRAMMAR, value);
    }

    /**
    * Is the ANSI92 entry level SQL grammar supported?
    * All JDBC Compliant<sup><font size=-2>TM</font></sup> drivers must return true.
    * @return <code>true</code> if so
    * @exception SQLException if a database access error occurs
    */
    public boolean supportsANSI92EntryLevelSQL() throws SQLException
    {
        Boolean ansiSQLGrammar = (Boolean)properties.get(PROP_ANSI_SQL_GRAMMAR);
        if (ansiSQLGrammar == null) {
            if (dmd != null) ansiSQLGrammar = dmd.supportsANSI92EntryLevelSQL() ? Boolean.TRUE : Boolean.FALSE;
            else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
            properties.put(PROP_ANSI_SQL_GRAMMAR, ansiSQLGrammar);
        }

        return ansiSQLGrammar.booleanValue();
    }

    public int getANSI92EntryLevelSQL()
    {
        return getBoolean(PROP_ANSI_SQL_GRAMMAR);
    }

    public void setANSI92EntryLevelSQL(int value)
    {
        setBoolean(PROP_ANSI_SQL_GRAMMAR, value);
    }

    /**
    * Is the ANSI92 intermediate SQL grammar supported?
    * @exception SQLException if a database access error occurs
    */
    public boolean supportsANSI92IntermediateSQL() throws SQLException
    {
        Boolean ansiInterSQLGrammar = (Boolean)properties.get(PROP_INTERMEDIATE_SQL_GRAMMAR);
        if (ansiInterSQLGrammar == null) {
            if (dmd != null) ansiInterSQLGrammar = dmd.supportsANSI92IntermediateSQL() ? Boolean.TRUE : Boolean.FALSE;
            else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
            properties.put(PROP_INTERMEDIATE_SQL_GRAMMAR, ansiInterSQLGrammar);
        }

        return ansiInterSQLGrammar.booleanValue();
    }

    public int getANSI92IntermediateSQL()
    {
        return getBoolean(PROP_INTERMEDIATE_SQL_GRAMMAR);
    }

    public void setANSI92IntermediateSQL(int value)
    {
        setBoolean(PROP_INTERMEDIATE_SQL_GRAMMAR, value);
    }

    /**
    * Is the ANSI92 full SQL grammar supported?
    * @return <code>true</code> if so
    */
    public boolean supportsANSI92FullSQL() throws SQLException
    {
        Boolean ansiFullSQLGrammar = (Boolean)properties.get(PROP_FULL_SQL_GRAMMAR);
        if (ansiFullSQLGrammar == null) {
            if (dmd != null) ansiFullSQLGrammar = dmd.supportsANSI92FullSQL() ? Boolean.TRUE : Boolean.FALSE;
            else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
            properties.put(PROP_FULL_SQL_GRAMMAR, ansiFullSQLGrammar);
        }

        return ansiFullSQLGrammar.booleanValue();
    }

    public int getANSI92FullSQL()
    {
        return getBoolean(PROP_FULL_SQL_GRAMMAR);
    }

    public void setANSI92FullSQL(int value)
    {
        setBoolean(PROP_FULL_SQL_GRAMMAR, value);
    }

    /**
    * Is the SQL Integrity Enhancement Facility supported?
    * @return <code>true</code> if so
    * @exception SQLException if a database access error occurs
    */
    public boolean supportsIntegrityEnhancementFacility() throws SQLException
    {
        Boolean integrityEnh = (Boolean)properties.get(PROP_INTEGRITY_ENHANCEMENT);
        if (integrityEnh == null) {
            if (dmd != null) integrityEnh = dmd.supportsIntegrityEnhancementFacility() ? Boolean.TRUE : Boolean.FALSE;
            else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
            properties.put(PROP_INTEGRITY_ENHANCEMENT, integrityEnh);
        }

        return integrityEnh.booleanValue();
    }

    public int getIntegrityEnhancementFacility()
    {
        return getBoolean(PROP_INTEGRITY_ENHANCEMENT);
    }

    public void setIntegrityEnhancementFacility(int value)
    {
        setBoolean(PROP_INTEGRITY_ENHANCEMENT, value);
    }

    /**
    * Is some form of outer join supported?
    * @return <code>true</code> if so
    * @exception SQLException if a database access error occurs
    */
    public boolean supportsOuterJoins() throws SQLException
    {
        Boolean outerJoins = (Boolean)properties.get(PROP_OUTER_JOINS);
        if (outerJoins == null) {
            if (dmd != null) outerJoins = dmd.supportsOuterJoins() ? Boolean.TRUE : Boolean.FALSE;
            else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
            properties.put(PROP_OUTER_JOINS, outerJoins);
        }

        return outerJoins.booleanValue();
    }

    public int getOuterJoins()
    {
        return getBoolean(PROP_OUTER_JOINS);
    }

    public void setOuterJoins(int value)
    {
        setBoolean(PROP_OUTER_JOINS, value);
    }

    /**
    * Are full nested outer joins supported?
    * @return <code>true</code> if so
    * @exception SQLException if a database access error occurs
       	*/
    public boolean supportsFullOuterJoins() throws SQLException
    {
        Boolean fullOuterJoins = (Boolean)properties.get(PROP_FULL_OUTER_JOINS);
        if (fullOuterJoins == null) {
            if (dmd != null) fullOuterJoins = dmd.supportsFullOuterJoins() ? Boolean.TRUE : Boolean.FALSE;
            else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
            properties.put(PROP_FULL_OUTER_JOINS, fullOuterJoins);
        }

        return fullOuterJoins.booleanValue();
    }

    public int getFullOuterJoins()
    {
        return getBoolean(PROP_FULL_OUTER_JOINS);
    }

    public void setFullOuterJoins(int value)
    {
        setBoolean(PROP_FULL_OUTER_JOINS, value);
    }

    /**
    * Is there limited support for outer joins?  (This will be true
    * if supportFullOuterJoins is true.)
    * @return <code>true</code> if so
    * @exception SQLException if a database access error occurs
    */
    public boolean supportsLimitedOuterJoins() throws SQLException
    {
        Boolean limiterOuterJoins = (Boolean)properties.get(PROP_LIMITED_OUTER_JOINS);
        if (limiterOuterJoins == null) {
            if (dmd != null) limiterOuterJoins = dmd.supportsLimitedOuterJoins() ? Boolean.TRUE : Boolean.FALSE;
            else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
            properties.put(PROP_LIMITED_OUTER_JOINS, limiterOuterJoins);
        }

        return limiterOuterJoins.booleanValue();
    }

    public int getLimitedOuterJoins()
    {
        return getBoolean(PROP_LIMITED_OUTER_JOINS);
    }

    public void setLimitedOuterJoins(int value)
    {
        setBoolean(PROP_LIMITED_OUTER_JOINS, value);
    }

    /**
    * What's the database vendor's preferred term for "schema"?
    	*
    	* @return the vendor term
    	* @exception SQLException if a database access error occurs
    	*/
    public String getSchemaTerm() throws SQLException
    {
        String schemaTerm = (String)properties.get(PROP_SCHEMA_TERM);
        if (schemaTerm == null) {
            if (dmd != null) schemaTerm = dmd.getSchemaTerm();
            else return NOT_SET_STRING;
            properties.put(PROP_SCHEMA_TERM, schemaTerm);
        }

        return schemaTerm;
    }

    public void setSchemaTerm(String value)
    {
        setString(PROP_SCHEMA_TERM, value);
    }

    /**
    	* What's the database vendor's preferred term for "procedure"?
    	*
    	* @return the vendor term
    	* @exception SQLException if a database access error occurs
    	*/
    public String getProcedureTerm() throws SQLException
    {
        String procedureTerm = (String)properties.get(PROP_PROCEDURE_TERM);
        if (procedureTerm == null) {
            if (dmd != null) procedureTerm = dmd.getProcedureTerm();
            else return NOT_SET_STRING;
            properties.put(PROP_PROCEDURE_TERM, procedureTerm);
        }

        return procedureTerm;
    }

    public void setProcedureTerm(String value)
    {
        setString(PROP_PROCEDURE_TERM, value);
    }

    /**
    	* What's the database vendor's preferred term for "catalog"?
    	*
    	* @return the vendor term
    	* @exception SQLException if a database access error occurs
    	*/
    public String getCatalogTerm() throws SQLException
    {
        String catalogTerm = (String)properties.get(PROP_CATALOG_TERM);
        if (catalogTerm == null) {
            if (dmd != null) catalogTerm = dmd.getCatalogTerm();
            else return NOT_SET_STRING;
            properties.put(PROP_CATALOG_TERM, catalogTerm);
        }

        return catalogTerm;
    }

    public void setCatalogTerm(String value)
    {
        setString(PROP_CATALOG_TERM, value);
    }

    /**
    * Does a catalog appear at the start of a qualified table name?
    * (Otherwise it appears at the end)
    * @return true if it appears at the start 
    * @exception SQLException if a database access error occurs
    */

    public boolean isCatalogAtStart() throws SQLException {
        Boolean catalogAtStart = (Boolean)properties.get(PROP_CATALOG_AT_START);
        if (catalogAtStart == null) {
            if (dmd != null)
                catalogAtStart = dmd.isCatalogAtStart() ? Boolean.TRUE : Boolean.FALSE;
            else
                throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
            properties.put(PROP_CATALOG_AT_START, catalogAtStart);
        }

        return catalogAtStart.booleanValue();
    }

    //	public int getCatalogAtStart() {
    public int getcatalogAtStart() {
        return getBoolean(PROP_CATALOG_AT_START);
    }

    //	public void setCatalogAtStart(int value) {
    public void setcatalogAtStart(int value) {
        setBoolean(PROP_CATALOG_AT_START, value);
    }

    /**
    * What's the separator between catalog and table name?
    * @return the separator string
    * @exception SQLException if a database access error occurs
    */
    public String getCatalogSeparator() throws SQLException
    {
        String catalogSeparator = (String)properties.get(PROP_CATALOGS_SEPARATOR);
        if (catalogSeparator == null) {
            if (dmd != null) catalogSeparator = dmd.getCatalogSeparator();
            else return NOT_SET_STRING;
            properties.put(PROP_CATALOGS_SEPARATOR, catalogSeparator);
        }

        return catalogSeparator;
    }

    public void setCatalogSeparator(String value)
    {
        setString(PROP_CATALOGS_SEPARATOR, value);
    }

    /**
    * Can a schema name be used in a data manipulation statement?
    * @return <code>true</code> if so
    * @exception SQLException if a database access error occurs
    */
    public boolean supportsSchemasInDataManipulation() throws SQLException
    {
        Boolean schemasInDM = (Boolean)properties.get(PROP_SCHEMAS_IN_DML);
        if (schemasInDM == null) {
            if (dmd != null) schemasInDM = dmd.supportsSchemasInDataManipulation() ? Boolean.TRUE : Boolean.FALSE;
            else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
            properties.put(PROP_SCHEMAS_IN_DML, schemasInDM);
        }

        return schemasInDM.booleanValue();
    }

    public int getSchemasInDataManipulation()
    {
        return getBoolean(PROP_SCHEMAS_IN_DML);
    }

    public void setSchemasInDataManipulation(int value)
    {
        setBoolean(PROP_SCHEMAS_IN_DML, value);
    }

    /**
    * Can a schema name be used in a procedure call statement?
    * @return <code>true</code> if so
    * @exception SQLException if a database access error occurs
    */
    public boolean supportsSchemasInProcedureCalls() throws SQLException
    {
        Boolean schemasInProcedureCalls = (Boolean)properties.get(PROP_SCHEMAS_IN_PROCEDURE_CALL);
        if (schemasInProcedureCalls == null) {
            if (dmd != null) schemasInProcedureCalls = dmd.supportsSchemasInProcedureCalls() ? Boolean.TRUE : Boolean.FALSE;
            else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
            properties.put(PROP_SCHEMAS_IN_PROCEDURE_CALL, schemasInProcedureCalls);
        }

        return schemasInProcedureCalls.booleanValue();
    }

    public int getSchemasInProcedureCalls()
    {
        return getBoolean(PROP_SCHEMAS_IN_PROCEDURE_CALL);
    }

    public void setSchemasInProcedureCalls(int value)
    {
        setBoolean(PROP_SCHEMAS_IN_PROCEDURE_CALL, value);
    }

    /**
    * Can a schema name be used in a table definition statement?
    * @return <code>true</code> if so
    * @exception SQLException if a database access error occurs
    */
    public boolean supportsSchemasInTableDefinitions() throws SQLException
    {
        Boolean schemasInTable = (Boolean)properties.get(PROP_SCHEMAS_IN_TABLE_DEFINITION);
        if (schemasInTable == null) {
            if (dmd != null) schemasInTable = dmd.supportsSchemasInTableDefinitions() ? Boolean.TRUE : Boolean.FALSE;
            else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
            properties.put(PROP_SCHEMAS_IN_TABLE_DEFINITION, schemasInTable);
        }

        return schemasInTable.booleanValue();
    }

    public int getSchemasInTableDefinitions()
    {
        return getBoolean(PROP_SCHEMAS_IN_TABLE_DEFINITION);
    }

    public void setSchemasInTableDefinitions(int value)
    {
        setBoolean(PROP_SCHEMAS_IN_TABLE_DEFINITION, value);
    }

    /**
    * Can a schema name be used in an index definition statement?
    * @return <code>true</code> if so
    * @exception SQLException if a database access error occurs
    */
    public boolean supportsSchemasInIndexDefinitions() throws SQLException
    {
        Boolean schemasInIndex = (Boolean)properties.get(PROP_SCHEMAS_IN_INDEX);
        if (schemasInIndex == null) {
            if (dmd != null) schemasInIndex = dmd.supportsSchemasInIndexDefinitions() ? Boolean.TRUE : Boolean.FALSE;
            else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
            properties.put(PROP_SCHEMAS_IN_INDEX, schemasInIndex);
        }

        return schemasInIndex.booleanValue();
    }

    public int getSchemasInIndexDefinitions()
    {
        return getBoolean(PROP_SCHEMAS_IN_INDEX);
    }

    public void setSchemasInIndexDefinitions(int value)
    {
        setBoolean(PROP_SCHEMAS_IN_INDEX, value);
    }

    /**
    * Can a schema name be used in a privilege definition statement?
    * @return <code>true</code> if so
    * @exception SQLException if a database access error occurs
    */
    public boolean supportsSchemasInPrivilegeDefinitions() throws SQLException
    {
        Boolean schemasInPriv = (Boolean)properties.get(PROP_SCHEMAS_IN_PRIVILEGE_DEFINITION);
        if (schemasInPriv == null) {
            if (dmd != null) schemasInPriv = dmd.supportsSchemasInPrivilegeDefinitions() ? Boolean.TRUE : Boolean.FALSE;
            else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
            properties.put(PROP_SCHEMAS_IN_PRIVILEGE_DEFINITION, schemasInPriv);
        }

        return schemasInPriv.booleanValue();
    }

    public int getSchemasInPrivilegeDefinitions()
    {
        return getBoolean(PROP_SCHEMAS_IN_PRIVILEGE_DEFINITION);
    }

    public void setSchemasInPrivilegeDefinitions(int value)
    {
        setBoolean(PROP_SCHEMAS_IN_PRIVILEGE_DEFINITION, value);
    }

    /**
    * Can a catalog name be used in a data manipulation statement?
    * @return <code>true</code> if so
    * @exception SQLException if a database access error occurs
    */
    public boolean supportsCatalogsInDataManipulation() throws SQLException
    {
        Boolean catalogInDM = (Boolean)properties.get(PROP_CATALOGS_IN_DML);
        if (catalogInDM == null) {
            if (dmd != null) catalogInDM = dmd.supportsCatalogsInDataManipulation() ? Boolean.TRUE : Boolean.FALSE;
            else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
            properties.put(PROP_CATALOGS_IN_DML, catalogInDM);
        }

        return catalogInDM.booleanValue();
    }

    public int getCatalogsInDataManipulation()
    {
        return getBoolean(PROP_CATALOGS_IN_DML);
    }

    public void setCatalogsInDataManipulation(int value)
    {
        setBoolean(PROP_CATALOGS_IN_DML, value);
    }

    /**
    * Can a catalog name be used in a procedure call statement?
    * @return <code>true</code> if so
    * @exception SQLException if a database access error occurs
    */
    public boolean supportsCatalogsInProcedureCalls() throws SQLException
    {
        Boolean catalogInProc = (Boolean)properties.get(PROP_CATALOGS_IN_PROCEDURE_CALL);
        if (catalogInProc == null) {
            if (dmd != null) catalogInProc = dmd.supportsCatalogsInProcedureCalls() ? Boolean.TRUE : Boolean.FALSE;
            else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
            properties.put(PROP_CATALOGS_IN_PROCEDURE_CALL, catalogInProc);
        }

        return catalogInProc.booleanValue();
    }

    public int getCatalogsInProcedureCalls()
    {
        return getBoolean(PROP_CATALOGS_IN_PROCEDURE_CALL);
    }

    public void setCatalogsInProcedureCalls(int value)
    {
        setBoolean(PROP_CATALOGS_IN_PROCEDURE_CALL, value);
    }

    /**
    * Can a catalog name be used in a table definition statement?
    * @return <code>true</code> if so
    * @exception SQLException if a database access error occurs
    */
    public boolean supportsCatalogsInTableDefinitions() throws SQLException
    {
        Boolean catalogInTable = (Boolean)properties.get(PROP_CATALOGS_IN_TABLE_DEFINITION);
        if (catalogInTable == null) {
            if (dmd != null) catalogInTable = dmd.supportsCatalogsInTableDefinitions() ? Boolean.TRUE : Boolean.FALSE;
            else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
            properties.put(PROP_CATALOGS_IN_TABLE_DEFINITION, catalogInTable);
        }

        return catalogInTable.booleanValue();
    }

    public int getCatalogsInTableDefinitions()
    {
        return getBoolean(PROP_CATALOGS_IN_TABLE_DEFINITION);
    }

    public void setCatalogsInTableDefinitions(int value)
    {
        setBoolean(PROP_CATALOGS_IN_TABLE_DEFINITION, value);
    }

    /**
    * Can a catalog name be used in an index definition statement?
    * @return <code>true</code> if so
    * @exception SQLException if a database access error occurs
    */
    public boolean supportsCatalogsInIndexDefinitions() throws SQLException
    {
        Boolean catalogInIndex = (Boolean)properties.get(PROP_CATALOGS_IN_INDEX);
        if (catalogInIndex == null) {
            if (dmd != null) catalogInIndex = dmd.supportsCatalogsInIndexDefinitions() ? Boolean.TRUE : Boolean.FALSE;
            else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
            properties.put(PROP_CATALOGS_IN_INDEX, catalogInIndex);
        }

        return catalogInIndex.booleanValue();
    }

    public int getCatalogsInIndexDefinitions()
    {
        return getBoolean(PROP_CATALOGS_IN_INDEX);
    }

    public void setCatalogsInIndexDefinitions(int value)
    {
        setBoolean(PROP_CATALOGS_IN_INDEX, value);
    }

    /**
    * Can a catalog name be used in a privilege definition statement?
    * @return <code>true</code> if so
    * @exception SQLException if a database access error occurs
    */
    public boolean supportsCatalogsInPrivilegeDefinitions() throws SQLException
    {
        Boolean catalogInPriv = (Boolean)properties.get(PROP_CATALOGS_IN_PRIVILEGE_DEFINITION);
        if (catalogInPriv == null) {
            if (dmd != null) catalogInPriv = dmd.supportsCatalogsInPrivilegeDefinitions() ? Boolean.TRUE : Boolean.FALSE;
            else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
            properties.put(PROP_CATALOGS_IN_PRIVILEGE_DEFINITION, catalogInPriv);
        }

        return catalogInPriv.booleanValue();
    }

    public int getCatalogsInPrivilegeDefinitions()
    {
        return getBoolean(PROP_CATALOGS_IN_PRIVILEGE_DEFINITION);
    }

    public void setCatalogsInPrivilegeDefinitions(int value)
    {
        setBoolean(PROP_CATALOGS_IN_PRIVILEGE_DEFINITION, value);
    }

    /**
    * Is positioned DELETE supported?
    * @return <code>true</code> if so
    * @exception SQLException if a database access error occurs
    */
    public boolean supportsPositionedDelete() throws SQLException
    {
        Boolean posDelete = (Boolean)properties.get(PROP_POSITIONED_DELETE);
        if (posDelete == null) {
            if (dmd != null) posDelete = dmd.supportsPositionedDelete() ? Boolean.TRUE : Boolean.FALSE;
            else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
            properties.put(PROP_POSITIONED_DELETE, posDelete);
        }

        return posDelete.booleanValue();
    }

    public int getPositionedDelete()
    {
        return getBoolean(PROP_POSITIONED_DELETE);
    }

    public void setPositionedDelete(int value)
    {
        setBoolean(PROP_POSITIONED_DELETE, value);
    }

    /**
    * Is positioned UPDATE supported?
    * @return <code>true</code> if so
    * @exception SQLException if a database access error occurs
    */
    public boolean supportsPositionedUpdate() throws SQLException
    {
        Boolean posUpdate = (Boolean)properties.get(PROP_POSITIONED_UPDATE);
        if (posUpdate == null) {
            if (dmd != null) posUpdate = dmd.supportsPositionedUpdate() ? Boolean.TRUE : Boolean.FALSE;
            else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
            properties.put(PROP_POSITIONED_UPDATE, posUpdate);
        }

        return posUpdate.booleanValue();
    }

    public int getPositionedUpdate()
    {
        return getBoolean(PROP_POSITIONED_UPDATE);
    }

    public void setPositionedUpdate(int value)
    {
        setBoolean(PROP_POSITIONED_UPDATE, value);
    }

    /**
    * Is SELECT for UPDATE supported?
    * @return <code>true</code> if so
    * @exception SQLException if a database access error occurs
    */
    public boolean supportsSelectForUpdate() throws SQLException
    {
        Boolean selectForUpdate = (Boolean)properties.get(PROP_SELECT_FOR_UPDATE);
        if (selectForUpdate == null) {
            if (dmd != null) selectForUpdate = dmd.supportsSelectForUpdate() ? Boolean.TRUE : Boolean.FALSE;
            else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
            properties.put(PROP_SELECT_FOR_UPDATE, selectForUpdate);
        }

        return selectForUpdate.booleanValue();
    }

    public int getSelectForUpdate()
    {
        return getBoolean(PROP_SELECT_FOR_UPDATE);
    }

    public void setSelectForUpdate(int value)
    {
        setBoolean(PROP_SELECT_FOR_UPDATE, value);
    }

    /**
    * Are stored procedure calls using the stored procedure escape
    * syntax supported?
    * @return <code>true</code> if so
    * @exception SQLException if a database access error occurs
    */
    public boolean supportsStoredProcedures() throws SQLException
    {
        Boolean storedProcedures = (Boolean)properties.get(PROP_STORED_PROCEDURES);
        if (storedProcedures == null) {
            if (dmd != null) storedProcedures = dmd.supportsStoredProcedures() ? Boolean.TRUE : Boolean.FALSE;
            else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
            properties.put(PROP_STORED_PROCEDURES, storedProcedures);
        }

        return storedProcedures.booleanValue();
    }

    public int getStoredProcedures()
    {
        return getBoolean(PROP_STORED_PROCEDURES);
    }

    public void setStoredProcedures(int value)
    {
        setBoolean(PROP_STORED_PROCEDURES, value);
    }

    /**
    * Are subqueries in comparison expressions supported?
    * A JDBC Compliant<sup><font size=-2>TM</font></sup> driver always returns true.
    * @return <code>true</code> if so
    * @exception SQLException if a database access error occurs
    */
    public boolean supportsSubqueriesInComparisons() throws SQLException
    {
        Boolean subqueryComp = (Boolean)properties.get(PROP_SUBQUERY_IN_COMPARSIONS);
        if (subqueryComp == null) {
            if (dmd != null) subqueryComp = dmd.supportsSubqueriesInComparisons() ? Boolean.TRUE : Boolean.FALSE;
            else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
            properties.put(PROP_SUBQUERY_IN_COMPARSIONS, subqueryComp);
        }

        return subqueryComp.booleanValue();
    }

    public int getSubqueriesInComparisons()
    {
        return getBoolean(PROP_SUBQUERY_IN_COMPARSIONS);
    }

    public void setSubqueriesInComparisons(int value)
    {
        setBoolean(PROP_SUBQUERY_IN_COMPARSIONS, value);
    }

    /**
    * Are subqueries in 'exists' expressions supported?
    * A JDBC Compliant<sup><font size=-2>TM</font></sup> driver always returns true.
    * @return <code>true</code> if so
    * @exception SQLException if a database access error occurs
    */
    public boolean supportsSubqueriesInExists() throws SQLException
    {
        Boolean subqueryExist = (Boolean)properties.get(PROP_SUBQUERY_IN_EXISTS);
        if (subqueryExist == null) {
            if (dmd != null) subqueryExist = dmd.supportsSubqueriesInExists() ? Boolean.TRUE : Boolean.FALSE;
            else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
            properties.put(PROP_SUBQUERY_IN_EXISTS, subqueryExist);
        }

        return subqueryExist.booleanValue();
    }

    public int getSubqueriesInExists()
    {
        return getBoolean(PROP_SUBQUERY_IN_EXISTS);
    }

    public void setSubqueriesInExists(int value)
    {
        setBoolean(PROP_SUBQUERY_IN_EXISTS, value);
    }

    /**
    * Are subqueries in 'in' statements supported?
    * A JDBC Compliant<sup><font size=-2>TM</font></sup> driver always returns true.
    * @return <code>true</code> if so
    */
    public boolean supportsSubqueriesInIns() throws SQLException
    {
        Boolean subqueryIns = (Boolean)properties.get(PROP_SUBQUERY_IN_INS);
        if (subqueryIns == null) {
            if (dmd != null) subqueryIns = dmd.supportsSubqueriesInIns() ? Boolean.TRUE : Boolean.FALSE;
            else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
            properties.put(PROP_SUBQUERY_IN_INS, subqueryIns);
        }

        return subqueryIns.booleanValue();
    }

    public int getSubqueriesInIns()
    {
        return getBoolean(PROP_SUBQUERY_IN_INS);
    }

    public void setSubqueriesInIns(int value)
    {
        setBoolean(PROP_SUBQUERY_IN_INS, value);
    }

    /**
    * Are subqueries in quantified expressions supported?
    * A JDBC Compliant<sup><font size=-2>TM</font></sup> driver always returns true.
    * @return <code>true</code> if so
    * @exception SQLException if a database access error occurs
    */
    public boolean supportsSubqueriesInQuantifieds() throws SQLException
    {
        Boolean subqueryQuant = (Boolean)properties.get(PROP_SUBQUERY_IN_QUANTIFIEDS);
        if (subqueryQuant == null) {
            if (dmd != null) subqueryQuant = dmd.supportsSubqueriesInQuantifieds() ? Boolean.TRUE : Boolean.FALSE;
            else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
            properties.put(PROP_SUBQUERY_IN_QUANTIFIEDS, subqueryQuant);
        }

        return subqueryQuant.booleanValue();
    }

    public int getSubqueriesInQuantifieds()
    {
        return getBoolean(PROP_SUBQUERY_IN_QUANTIFIEDS);
    }

    public void setSubqueriesInQuantifieds(int value)
    {
        setBoolean(PROP_SUBQUERY_IN_QUANTIFIEDS, value);
    }

    /**
    * Are correlated subqueries supported?
    * A JDBC Compliant<sup><font size=-2>TM</font></sup> driver always returns true.
    * @return <code>true</code> if so
    * @exception SQLException if a database access error occurs
    */
    public boolean supportsCorrelatedSubqueries() throws SQLException
    {
        Boolean subqueryCorr = (Boolean)properties.get(PROP_CORRELATED_SUBQUERIES);
        if (subqueryCorr == null) {
            if (dmd != null) subqueryCorr = dmd.supportsCorrelatedSubqueries() ? Boolean.TRUE : Boolean.FALSE;
            else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
            properties.put(PROP_CORRELATED_SUBQUERIES, subqueryCorr);
        }

        return subqueryCorr.booleanValue();
    }

    public int getCorrelatedSubqueries()
    {
        return getBoolean(PROP_CORRELATED_SUBQUERIES);
    }

    public void setCorrelatedSubqueries(int value)
    {
        setBoolean(PROP_CORRELATED_SUBQUERIES, value);
    }

    /**
    * Is SQL UNION supported?
    * @return <code>true</code> if so
    * @exception SQLException if a database access error occurs
    */
    public boolean supportsUnion() throws SQLException
    {
        Boolean union = (Boolean)properties.get(PROP_UNION);
        if (union == null) {
            if (dmd != null) union = dmd.supportsUnion() ? Boolean.TRUE : Boolean.FALSE;
            else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
            properties.put(PROP_UNION, union);
        }

        return union.booleanValue();
    }

    public int getUnion()
    {
        return getBoolean(PROP_UNION);
    }

    public void setUnion(int value)
    {
        setBoolean(PROP_UNION, value);
    }

    /**
    * Is SQL UNION ALL supported?
    * @return <code>true</code> if so
    * @exception SQLException if a database access error occurs
    */
    public boolean supportsUnionAll() throws SQLException
    {
        Boolean unionAll = (Boolean)properties.get(PROP_UNION_ALL);
        if (unionAll == null) {
            if (dmd != null) unionAll = dmd.supportsUnionAll() ? Boolean.TRUE : Boolean.FALSE;
            else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
            properties.put(PROP_UNION_ALL, unionAll);
        }

        return unionAll.booleanValue();
    }

    public int getUnionAll()
    {
        return getBoolean(PROP_UNION_ALL);
    }

    public void setUnionAll(int value)
    {
        setBoolean(PROP_UNION_ALL, value);
    }

    /**
    * Can cursors remain open across commits? 
    * @return <code>true</code> if cursors always remain open
    * @exception SQLException if a database access error occurs
    */
    public boolean supportsOpenCursorsAcrossCommit() throws SQLException
    {
        Boolean cursorInCommit = (Boolean)properties.get(PROP_OPEN_CURSORS_ACROSS_COMMIT);
        if (cursorInCommit == null) {
            if (dmd != null) cursorInCommit = dmd.supportsOpenCursorsAcrossCommit() ? Boolean.TRUE : Boolean.FALSE;
            else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
            properties.put(PROP_OPEN_CURSORS_ACROSS_COMMIT, cursorInCommit);
        }

        return cursorInCommit.booleanValue();
    }

    public int getOpenCursorsAcrossCommit()
    {
        return getBoolean(PROP_OPEN_CURSORS_ACROSS_COMMIT);
    }

    public void setOpenCursorsAcrossCommit(int value)
    {
        setBoolean(PROP_OPEN_CURSORS_ACROSS_COMMIT, value);
    }

    /**
    * Can cursors remain open across rollbacks?
    * @return <code>true</code> if cursors always remain open
    * @exception SQLException if a database access error occurs
    */
    public boolean supportsOpenCursorsAcrossRollback() throws SQLException
    {
        Boolean cursorInRollback = (Boolean)properties.get(PROP_OPEN_CURSORS_ACROSS_ROLLBACK);
        if (cursorInRollback == null) {
            if (dmd != null) cursorInRollback = dmd.supportsOpenCursorsAcrossRollback() ? Boolean.TRUE : Boolean.FALSE;
            else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
            properties.put(PROP_OPEN_CURSORS_ACROSS_ROLLBACK, cursorInRollback);
        }

        return cursorInRollback.booleanValue();
    }

    public int getOpenCursorsAcrossRollback()
    {
        return getBoolean(PROP_OPEN_CURSORS_ACROSS_ROLLBACK);
    }

    public void setOpenCursorsAcrossRollback(int value)
    {
        setBoolean(PROP_OPEN_CURSORS_ACROSS_ROLLBACK, value);
    }

    /**
    * Can statements remain open across commits?
    * @return <code>true</code> if statements always remain open
    * @exception SQLException if a database access error occurs
    */
    public boolean supportsOpenStatementsAcrossCommit() throws SQLException
    {
        Boolean statementInCommit = (Boolean)properties.get(PROP_OPEN_STATEMENTS_ACROSS_COMMIT);
        if (statementInCommit == null) {
            if (dmd != null) statementInCommit = dmd.supportsOpenStatementsAcrossCommit() ? Boolean.TRUE : Boolean.FALSE;
            else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
            properties.put(PROP_OPEN_STATEMENTS_ACROSS_COMMIT, statementInCommit);
        }

        return statementInCommit.booleanValue();
    }

    public int getOpenStatementsAcrossCommit()
    {
        return getBoolean(PROP_OPEN_STATEMENTS_ACROSS_COMMIT);
    }

    public void setOpenStatementsAcrossCommit(int value)
    {
        setBoolean(PROP_OPEN_STATEMENTS_ACROSS_COMMIT, value);
    }

    /**
    * Can statements remain open across rollbacks?
    * @return <code>true</code> if statements always remain open
    * @exception SQLException if a database access error occurs
    */
    public boolean supportsOpenStatementsAcrossRollback() throws SQLException
    {
        Boolean statementInRollback = (Boolean)properties.get(PROP_OPEN_STATEMENTS_ACROSS_ROLLBACK);
        if (statementInRollback == null) {
            if (dmd != null) statementInRollback = dmd.supportsOpenStatementsAcrossRollback() ? Boolean.TRUE : Boolean.FALSE;
            else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
            properties.put(PROP_OPEN_STATEMENTS_ACROSS_ROLLBACK, statementInRollback);
        }

        return statementInRollback.booleanValue();
    }

    public int getOpenStatementsAcrossRollback()
    {
        return getBoolean(PROP_OPEN_STATEMENTS_ACROSS_ROLLBACK);
    }

    public void setOpenStatementsAcrossRollback(int value)
    {
        setBoolean(PROP_OPEN_STATEMENTS_ACROSS_ROLLBACK, value);
    }

    /**
    * How many hex characters can you have in an inline binary literal?
    * @return max binary literal length in hex characters
    * @exception SQLException if a database access error occurs
    */
    public int getMaxBinaryLiteralLength() throws SQLException
    {
        Integer binaryLiteral = (Integer)properties.get(PROP_MAX_BINARY_LITERAL_LENGTH);
        if (binaryLiteral == null && dmd != null) {
            binaryLiteral = Integer.valueOf(dmd.getMaxBinaryLiteralLength());
            properties.put(PROP_MAX_BINARY_LITERAL_LENGTH, binaryLiteral);
        }

        if (binaryLiteral != null) return binaryLiteral.intValue();
        return 0;
    }

    public void setMaxBinaryLiteralLength(int value)
    {
        setInt(PROP_MAX_BINARY_LITERAL_LENGTH, value);
    }

    /**
    * What's the max length for a character literal?
    * @return max literal length
    * @exception SQLException if a database access error occurs
    */
    public int getMaxCharLiteralLength() throws SQLException
    {
        Integer maxCharLiteral = (Integer)properties.get(PROP_MAX_CHAR_LITERAL_LENGTH);
        if (maxCharLiteral == null && dmd != null) {
            maxCharLiteral = Integer.valueOf(dmd.getMaxCharLiteralLength());
            properties.put(PROP_MAX_CHAR_LITERAL_LENGTH, maxCharLiteral);
        }

        if (maxCharLiteral != null) return maxCharLiteral.intValue();
        return 0;
    }

    public void setMaxCharLiteralLength(int value)
    {
        setInt(PROP_MAX_CHAR_LITERAL_LENGTH, value);
    }

    /**
    * What's the limit on column name length?
    * @return max column name length
    * @exception SQLException if a database access error occurs
    */
    public int getMaxColumnNameLength() throws SQLException
    {
        Integer maxColumnName = (Integer)properties.get(PROP_MAX_COLUMN_NAME_LENGTH);
        if (maxColumnName == null && dmd != null) {
            maxColumnName = Integer.valueOf(dmd.getMaxColumnNameLength());
            properties.put(PROP_MAX_COLUMN_NAME_LENGTH, maxColumnName);
        }

        if (maxColumnName != null) return maxColumnName.intValue();
        return 0;
    }

    public void setMaxColumnNameLength(int value)
    {
        setInt(PROP_MAX_COLUMN_NAME_LENGTH, value);
    }

    /**
    * What's the maximum number of columns in a "GROUP BY" clause?
    * @return max number of columns
    * @exception SQLException if a database access error occurs
    */
    public int getMaxColumnsInGroupBy() throws SQLException
    {
        Integer maxColumnGroup = (Integer)properties.get(PROP_MAX_COLUMNS_IN_GROUPBY);
        if (maxColumnGroup == null && dmd != null) {
            maxColumnGroup = Integer.valueOf(dmd.getMaxColumnsInGroupBy());
            properties.put(PROP_MAX_COLUMNS_IN_GROUPBY, maxColumnGroup);
        }

        if (maxColumnGroup != null) return maxColumnGroup.intValue();
        return 0;
    }

    public void setMaxColumnsInGroupBy(int value)
    {
        setInt(PROP_MAX_COLUMNS_IN_GROUPBY, value);
    }

    /**
    * What's the maximum number of columns allowed in an index?
    * @return max number of columns
    * @exception SQLException if a database access error occurs
    */
    public int getMaxColumnsInIndex() throws SQLException
    {
        Integer maxColumnIndex = (Integer)properties.get(PROP_MAX_COLUMNS_IN_INDEX);
        if (maxColumnIndex == null && dmd != null) {
            maxColumnIndex = Integer.valueOf(dmd.getMaxColumnsInIndex());
            properties.put(PROP_MAX_COLUMNS_IN_INDEX, maxColumnIndex);
        }

        if (maxColumnIndex != null) return maxColumnIndex.intValue();
        return 0;
    }

    public void setMaxColumnsInIndex(int value)
    {
        setInt(PROP_MAX_COLUMNS_IN_INDEX, value);
    }

    /**
    * What's the maximum number of columns in an "ORDER BY" clause?
    * @return max number of columns
    * @exception SQLException if a database access error occurs
    */
    public int getMaxColumnsInOrderBy() throws SQLException
    {
        Integer maxColumnOrderBy = (Integer)properties.get(PROP_MAX_COLUMNS_IN_ORDERBY);
        if (maxColumnOrderBy == null && dmd != null) {
            maxColumnOrderBy = Integer.valueOf(dmd.getMaxColumnsInOrderBy());
            properties.put(PROP_MAX_COLUMNS_IN_ORDERBY, maxColumnOrderBy);
        }

        if (maxColumnOrderBy != null) return maxColumnOrderBy.intValue();
        return 0;
    }

    public void setMaxColumnsInOrderBy(int value)
    {
        setInt(PROP_MAX_COLUMNS_IN_ORDERBY, value);
    }

    /**
    * What's the maximum number of columns in a "SELECT" list?
    * @return max number of columns
    * @exception SQLException if a database access error occurs
    */
    public int getMaxColumnsInSelect() throws SQLException
    {
        Integer maxColumnSelect = (Integer)properties.get(PROP_MAX_COLUMNS_IN_SELECT);
        if (maxColumnSelect == null && dmd != null) {
            maxColumnSelect = Integer.valueOf(dmd.getMaxColumnsInSelect());
            properties.put(PROP_MAX_COLUMNS_IN_SELECT, maxColumnSelect);
        }

        if (maxColumnSelect != null) return maxColumnSelect.intValue();
        return 0;
    }

    public void setMaxColumnsInSelect(int value)
    {
        setInt(PROP_MAX_COLUMNS_IN_SELECT, value);
    }

    /**
    * What's the maximum number of columns in a table?
    * @return max number of columns
    * @exception SQLException if a database access error occurs
    */
    public int getMaxColumnsInTable() throws SQLException
    {
        Integer maxColumnTable = (Integer)properties.get(PROP_MAX_COLUMNS_IN_TABLE);
        if (maxColumnTable == null && dmd != null) {
            maxColumnTable = Integer.valueOf(dmd.getMaxColumnsInTable());
            properties.put(PROP_MAX_COLUMNS_IN_TABLE, maxColumnTable);
        }

        if (maxColumnTable != null) return maxColumnTable.intValue();
        return 0;
    }

    public void setMaxColumnsInTable(int value)
    {
        setInt(PROP_MAX_COLUMNS_IN_TABLE, value);
    }

    /**
    * How many active connections can we have at a time to this database?
    * @return max number of active connections
    * @exception SQLException if a database access error occurs
    */
    public int getMaxConnections() throws SQLException
    {
        Integer maxConnections = (Integer)properties.get(PROP_MAX_CONNECTIONS);
        if (maxConnections == null && dmd != null) {
            maxConnections = Integer.valueOf(dmd.getMaxConnections());
            properties.put(PROP_MAX_CONNECTIONS, maxConnections);
        }

        if (maxConnections != null) return maxConnections.intValue();
        return 0;
    }

    public void setMaxConnections(int value)
    {
        setInt(PROP_MAX_CONNECTIONS, value);
    }

    /**
    * What's the maximum cursor name length?
    * @return max cursor name length in bytes
    * @exception SQLException if a database access error occurs
    */
    public int getMaxCursorNameLength() throws SQLException
    {
        Integer maxCursorName = (Integer)properties.get(PROP_MAX_CURSORNAME_LENGTH);
        if (maxCursorName == null && dmd != null) {
            maxCursorName = Integer.valueOf(dmd.getMaxCursorNameLength());
            properties.put(PROP_MAX_CURSORNAME_LENGTH, maxCursorName);
        }

        if (maxCursorName != null) return maxCursorName.intValue();
        return 0;
    }

    public void setMaxCursorNameLength(int value)
    {
        setInt(PROP_MAX_CURSORNAME_LENGTH, value);
    }

    /**
    * What's the maximum length of an index (in bytes)?	
    * @return max index length in bytes
    * @exception SQLException if a database access error occurs
    */
    public int getMaxIndexLength() throws SQLException
    {
        Integer maxIndex = (Integer)properties.get(PROP_MAX_INDEX_LENGTH);
        if (maxIndex == null && dmd != null) {
            maxIndex = Integer.valueOf(dmd.getMaxIndexLength());
            properties.put(PROP_MAX_INDEX_LENGTH, maxIndex);
        }

        if (maxIndex != null) return maxIndex.intValue();
        return 0;
    }

    public void setMaxIndexLength(int value)
    {
        setInt(PROP_MAX_INDEX_LENGTH, value);
    }

    /**
    * What's the maximum length allowed for a schema name?
    * @return max name length in bytes
    * @exception SQLException if a database access error occurs
    */
    public int getMaxSchemaNameLength() throws SQLException
    {
        Integer maxSchemaName = (Integer)properties.get(PROP_MAX_SCHEMA_NAME);
        if (maxSchemaName == null && dmd != null) {
            maxSchemaName = Integer.valueOf(dmd.getMaxSchemaNameLength());
            properties.put(PROP_MAX_SCHEMA_NAME, maxSchemaName);
        }

        if (maxSchemaName != null) return maxSchemaName.intValue();
        return 0;
    }

    public void setMaxSchemaNameLength(int value)
    {
        setInt(PROP_MAX_SCHEMA_NAME, value);
    }

    /**
    * What's the maximum length of a procedure name?
    * @return max name length in bytes
    * @exception SQLException if a database access error occurs
    */
    public int getMaxProcedureNameLength() throws SQLException
    {
        Integer maxProcName = (Integer)properties.get(PROP_MAX_PROCEDURE_NAME);
        if (maxProcName == null && dmd != null) {
            maxProcName = Integer.valueOf(dmd.getMaxProcedureNameLength());
            properties.put(PROP_MAX_PROCEDURE_NAME, maxProcName);
        }

        if (maxProcName != null) return maxProcName.intValue();
        return 0;
    }

    public void setMaxProcedureNameLength(int value)
    {
        setInt(PROP_MAX_PROCEDURE_NAME, value);
    }

    /**
    * What's the maximum length of a catalog name?
    * @return max name length in bytes
    * @exception SQLException if a database access error occurs
    */
    public int getMaxCatalogNameLength() throws SQLException
    {
        Integer maxCatalogName = (Integer)properties.get(PROP_MAX_CATALOG_NAME);
        if (maxCatalogName == null && dmd != null) {
            maxCatalogName = Integer.valueOf(dmd.getMaxProcedureNameLength());
            properties.put(PROP_MAX_CATALOG_NAME, maxCatalogName);
        }

        if (maxCatalogName != null) return maxCatalogName.intValue();
        return 0;
    }

    public void setMaxCatalogNameLength(int value)
    {
        setInt(PROP_MAX_CATALOG_NAME, value);
    }

    /**
    * What's the maximum length of a single row?
    * @return max row size in bytes
    * @exception SQLException if a database access error occurs
    */
    public int getMaxRowSize() throws SQLException
    {
        Integer maxRowSize = (Integer)properties.get(PROP_MAX_ROW_SIZE);
        if (maxRowSize == null && dmd != null) {
            maxRowSize = Integer.valueOf(dmd.getMaxProcedureNameLength());
            properties.put(PROP_MAX_ROW_SIZE, maxRowSize);
        }

        if (maxRowSize != null) return maxRowSize.intValue();
        return 0;
    }

    public void setMaxRowSize(int value)
    {
        setInt(PROP_MAX_ROW_SIZE, value);
    }

    /**
    * Did getMaxRowSize() include LONGVARCHAR and LONGVARBINARY
    * blobs?
    * @return <code>true</code> if so
    * @exception SQLException if a database access error occurs
    */
    public boolean doesMaxRowSizeIncludeBlobs() throws SQLException
    {
        Boolean rowSizeBlobs = (Boolean)properties.get(PROP_ROWSIZE_INCLUDING_BLOBS);
        if (rowSizeBlobs == null) {
            if (dmd != null) rowSizeBlobs = dmd.doesMaxRowSizeIncludeBlobs() ? Boolean.TRUE : Boolean.FALSE;
            else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
            properties.put(PROP_ROWSIZE_INCLUDING_BLOBS, rowSizeBlobs);
        }

        return rowSizeBlobs.booleanValue();
    }

    public int getMaxRowSizeIncludeBlobs()
    {
        return getBoolean(PROP_ROWSIZE_INCLUDING_BLOBS);
    }

    public void setMaxRowSizeIncludeBlobs(int value)
    {
        setBoolean(PROP_ROWSIZE_INCLUDING_BLOBS, value);
    }

    /**
    * What's the maximum length of a SQL statement?
    * @return max length in bytes
    * @exception SQLException if a database access error occurs
    */
    public int getMaxStatementLength() throws SQLException
    {
        Integer maxStatement = (Integer)properties.get(PROP_MAX_STATEMENT_LENGTH);
        if (maxStatement == null && dmd != null) {
            maxStatement = Integer.valueOf(dmd.getMaxStatementLength());
            properties.put(PROP_MAX_STATEMENT_LENGTH, maxStatement);
        }

        if (maxStatement != null) return maxStatement.intValue();
        return 0;
    }

    public void setMaxStatementLength(int value)
    {
        setInt(PROP_MAX_STATEMENT_LENGTH, value);
    }

    /**
    * How many active statements can we have open at one time to this
    * database?
    * @return the maximum number of statements that can be open at one time
    * @exception SQLException if a database access error occurs
    */
    public int getMaxStatements() throws SQLException
    {
        Integer maxStatements = (Integer)properties.get(PROP_MAX_STATEMENTS);
        if (maxStatements == null && dmd != null) {
            maxStatements = Integer.valueOf(dmd.getMaxStatementLength());
            properties.put(PROP_MAX_STATEMENTS, maxStatements);
        }

        if (maxStatements != null) return maxStatements.intValue();
        return 0;
    }

    public void setMaxStatements(int value)
    {
        setInt(PROP_MAX_STATEMENTS, value);
    }

    /**
    * What's the maximum length of a table name?
    * @return max name length in bytes
    * @exception SQLException if a database access error occurs
    */
    public int getMaxTableNameLength() throws SQLException
    {
        Integer maxTable = (Integer)properties.get(PROP_MAX_TABLENAME_LENGTH);
        if (maxTable == null && dmd != null) {
            maxTable = Integer.valueOf(dmd.getMaxStatementLength());
            properties.put(PROP_MAX_TABLENAME_LENGTH, maxTable);
        }

        if (maxTable != null) return maxTable.intValue();
        return 0;
    }

    public void setMaxTableNameLength(int value)
    {
        setInt(PROP_MAX_TABLENAME_LENGTH, value);
    }

    /**
    * What's the maximum number of tables in a SELECT statement?
    * @return the maximum number of tables allowed in a SELECT statement
    * @exception SQLException if a database access error occurs
    */
    public int getMaxTablesInSelect() throws SQLException
    {
        Integer maxTable = (Integer)properties.get(PROP_MAX_TABLES_IN_SELECT);
        if (maxTable == null && dmd != null) {
            maxTable = Integer.valueOf(dmd.getMaxStatementLength());
            properties.put(PROP_MAX_TABLES_IN_SELECT, maxTable);
        }

        if (maxTable != null) return maxTable.intValue();
        return 0;
    }

    public void setMaxTablesInSelect(int value)
    {
        setInt(PROP_MAX_TABLES_IN_SELECT, value);
    }

    /**
    * What's the maximum length of a user name?
    * @return max user name length  in bytes
    * @exception SQLException if a database access error occurs
    */
    public int getMaxUserNameLength() throws SQLException
    {
        Integer maxUserName = (Integer)properties.get(PROP_MAX_USERNAME);
        if (maxUserName == null && dmd != null) {
            maxUserName = Integer.valueOf(dmd.getMaxUserNameLength());
            properties.put(PROP_MAX_USERNAME, maxUserName);
        }

        if (maxUserName != null) return maxUserName.intValue();
        return 0;
    }

    public void setMaxUserNameLength(int value)
    {
        setInt(PROP_MAX_USERNAME, value);
    }

    /**
    * What's the database's default transaction isolation level?  The
    * values are defined in <code>java.sql.Connection</code>.
    * @return the default isolation level 
    * @exception SQLException if a database access error occurs
    */
    public int getDefaultTransactionIsolation() throws SQLException
    {
        Integer maxTransaction = (Integer)properties.get(PROP_DEFAULT_ISOLATION);
        if (maxTransaction == null && dmd != null) {
            maxTransaction = Integer.valueOf(dmd.getDefaultTransactionIsolation());
            properties.put(PROP_DEFAULT_ISOLATION, maxTransaction);
        }

        if (maxTransaction != null) return maxTransaction.intValue();
        return 0;
    }

    public void setDefaultTransactionIsolation(int value)
    {
        setInt(PROP_DEFAULT_ISOLATION, value);
    }

    /**
    * Are transactions supported? If not, invoking the method
    * @return <code>true</code> if transactions are supported
    * @exception SQLException if a database access error occurs
    */
    public boolean supportsTransactions() throws SQLException
    {
        Boolean trans = (Boolean)properties.get(PROP_TRANSACTIONS);
        if (trans == null) {
            if (dmd != null) trans = dmd.doesMaxRowSizeIncludeBlobs() ? Boolean.TRUE : Boolean.FALSE;
            else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
            properties.put(PROP_TRANSACTIONS, trans);
        }

        return trans.booleanValue();
    }

    public int getTransactions()
    {
        return getBoolean(PROP_TRANSACTIONS);
    }

    public void setTransactions(int value)
    {
        setBoolean(PROP_TRANSACTIONS, value);
    }

    /**
    * Does this database support the given transaction isolation level?
    * @param level the values are defined in <code>java.sql.Connection</code>
    * @return <code>true</code> if so
    * @exception SQLException if a database access error occurs
    */
    public boolean supportsTransactionIsolationLevel(int level) throws SQLException
    {
        if (dmd != null) return dmd.supportsTransactionIsolationLevel(level);
        else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
    }

    /**
    * Are both data definition and data manipulation statements
    * within a transaction supported?
    * @return <code>true</code> if so
    * @exception SQLException if a database access error occurs
    */
    public boolean supportsDataDefinitionAndDataManipulationTransactions() throws SQLException
    {
        Boolean flag = (Boolean)properties.get(PROP_DDL_AND_DML_TRANSACTIONS);
        if (flag == null) {
            if (dmd != null) flag = dmd.doesMaxRowSizeIncludeBlobs() ? Boolean.TRUE : Boolean.FALSE;
            else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
            properties.put(PROP_DDL_AND_DML_TRANSACTIONS, flag);
        }

        return flag.booleanValue();
    }

    public int getDataDefinitionAndDataManipulationTransactions()
    {
        return getBoolean(PROP_DDL_AND_DML_TRANSACTIONS);
    }

    public void setDataDefinitionAndDataManipulationTransactions(int value)
    {
        setBoolean(PROP_DDL_AND_DML_TRANSACTIONS, value);
    }

    /**
    * Are only data manipulation statements within a transaction
    * supported?
    * @return <code>true</code> if so
    * @exception SQLException if a database access error occurs
    */
    public boolean supportsDataManipulationTransactionsOnly() throws SQLException
    {
        Boolean flag = (Boolean)properties.get(PROP_DML_TRANSACTIONS_ONLY);
        if (flag == null) {
            if (dmd != null) flag = dmd.supportsDataManipulationTransactionsOnly() ? Boolean.TRUE : Boolean.FALSE;
            else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
            properties.put(PROP_DML_TRANSACTIONS_ONLY, flag);
        }

        return flag.booleanValue();
    }

    public int getDataManipulationTransactionsOnly()
    {
        return getBoolean(PROP_DML_TRANSACTIONS_ONLY);
    }

    public void setDataManipulationTransactionsOnly(int value)
    {
        setBoolean(PROP_DML_TRANSACTIONS_ONLY, value);
    }

    /**
    * Does a data definition statement within a transaction force the
    * transaction to commit?
    * @return <code>true</code> if so
    * @exception SQLException if a database access error occurs
    */
    public boolean dataDefinitionCausesTransactionCommit() throws SQLException
    {
        Boolean flag = (Boolean)properties.get(PROP_DDL_CAUSES_COMMIT);
        if (flag == null) {
            if (dmd != null) flag = dmd.dataDefinitionCausesTransactionCommit() ? Boolean.TRUE : Boolean.FALSE;
            else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
            properties.put(PROP_DDL_CAUSES_COMMIT, flag);
        }

        return flag.booleanValue();
    }

    public int getDataDefinitionCausesTransactionCommit()
    {
        return getBoolean(PROP_DDL_CAUSES_COMMIT);
    }

    public void setDataDefinitionCausesTransactionCommit(int value)
    {
        setBoolean(PROP_DDL_CAUSES_COMMIT, value);
    }

    /**
     * Is a data definition statement within a transaction ignored?
     * @return <code>true</code> if so
     * @exception SQLException if a database access error occurs
     */
    public boolean dataDefinitionIgnoredInTransactions() throws SQLException
    {
        Boolean flag = (Boolean)properties.get(PROP_DDL_IGNORED_IN_TRANSACTIONS);
        if (flag == null) {
            if (dmd != null) flag = dmd.dataDefinitionIgnoredInTransactions() ? Boolean.TRUE : Boolean.FALSE;
            else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
            properties.put(PROP_DDL_IGNORED_IN_TRANSACTIONS, flag);
        }

        return flag.booleanValue();
    }

    public int getDataDefinitionIgnoredInTransactions()
    {
        return getBoolean(PROP_DDL_IGNORED_IN_TRANSACTIONS);
    }

    public void setDataDefinitionIgnoredInTransactions(int value)
    {
        setBoolean(PROP_DDL_IGNORED_IN_TRANSACTIONS, value);
    }

    /**
    * Gets a description of the stored procedures available in a
    * catalog.
    */
    public ResultSet getProcedures(String catalog, String schemaPattern, String procedureNamePattern) throws SQLException
    {
        if (getCapitializeUsername() && schemaPattern != null) schemaPattern = schemaPattern.toUpperCase();
        String query = (String)properties.get(PROP_PROCEDURES_QUERY);
        if (query != null) {
            if (con != null) {
                PreparedStatement stmt = con.prepareStatement(query);
                stmt.setString(1, catalog);
                stmt.setString(2, schemaPattern);
                stmt.setString(3, procedureNamePattern);
                return stmt.executeQuery();
            } else throw new SQLException(bundle.getString("EXC_NoConnection")); // NOI18N
        }

        if (dmd == null) throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
        return dmd.getProcedures(catalog, schemaPattern, procedureNamePattern);
    }

    public String getProceduresQuery()
    {
        return getString(PROP_PROCEDURES_QUERY);
    }

    public void setProceduresQuery(String value)
    {
        setString(PROP_PROCEDURES_QUERY, value);
    }

    /**
    * Gets a description of a catalog's stored procedure parameters
    * and result columns.
    */
    public ResultSet getProcedureColumns(String catalog, String schemaPattern, String procedureNamePattern, String columnNamePattern) throws SQLException
    {
        if (getCapitializeUsername() && schemaPattern != null) schemaPattern = schemaPattern.toUpperCase();
        String query = (String)properties.get(PROP_PROCEDURE_COLUMNS_QUERY);
        if (query != null) {
            if (con != null) {
                PreparedStatement stmt = con.prepareStatement(query);
                stmt.setString(1, catalog);
                stmt.setString(2, schemaPattern);
                stmt.setString(3, procedureNamePattern);
                stmt.setString(4, columnNamePattern);
                return stmt.executeQuery();
            } else throw new SQLException(bundle.getString("EXC_NoConnection")); // NOI18N
        }

        if (dmd == null) throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
        return dmd.getProcedureColumns(catalog, schemaPattern, procedureNamePattern, columnNamePattern);
    }

    public String getProcedureColumnsQuery()
    {
        return getString(PROP_PROCEDURE_COLUMNS_QUERY);
    }

    public void setProcedureColumnsQuery(String value)
    {
        setString(PROP_PROCEDURE_COLUMNS_QUERY, value);
    }

    /**
    * Gets the schema names available in this database.  The results
    * are ordered by schema name.
    */
    public ResultSet getSchemas() throws SQLException
    {
        String query = (String)properties.get(PROP_PROCEDURE_COLUMNS_QUERY);
        if (query != null) {
            if (con != null) return con.createStatement().executeQuery(query);
            else throw new SQLException(bundle.getString("EXC_NoConnection")); // NOI18N
        }

        if (dmd == null) throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
        return dmd.getSchemas();
    }

    public String getSchemasQuery()
    {
        return getString(PROP_SCHEMAS_QUERY);
    }

    public void setSchemasQuery(String value)
    {
        setString(PROP_SCHEMAS_QUERY, value);
    }

    /**
    * Gets the catalog names available in this database.  The results
    * are ordered by catalog name.
    * @exception SQLException if a database access error occurs
    */
    public ResultSet getCatalogs() throws SQLException
    {
        String query = (String)properties.get(PROP_CATALOGS_QUERY);
        if (query != null) {
            if (con != null) return con.createStatement().executeQuery(query);
            else throw new SQLException(bundle.getString("EXC_NoConnection")); // NOI18N
        }

        if (dmd == null) throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
        return dmd.getCatalogs();
    }

    public String getCatalogsQuery()
    {
        return getString(PROP_CATALOGS_QUERY);
    }

    public void setCatalogsQuery(String value)
    {
        setString(PROP_CATALOGS_QUERY, value);
    }

    /**
    * Gets the table types available in this database.  The results
    * are ordered by table type.
    */
    public ResultSet getTableTypes() throws SQLException
    {
        String query = (String)properties.get(PROP_TABLE_TYPES_QUERY);
        if (query != null) {
            if (con != null) return con.createStatement().executeQuery(query);
            else throw new SQLException(bundle.getString("EXC_NoConnection")); // NOI18N
        }

        if (dmd == null) throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
        return dmd.getCatalogs();
    }

    public String getTableTypesQuery()
    {
        return getString(PROP_TABLE_TYPES_QUERY);
    }

    public void setTableTypesQuery(String value)
    {
        setString(PROP_TABLE_TYPES_QUERY, value);
    }

    /**
    * Gets a description of table columns available in 
    * the specified catalog.
    */
    public ResultSet getColumns(String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern) throws SQLException
    {
        if (getCapitializeUsername() && schemaPattern != null) schemaPattern = schemaPattern.toUpperCase();
        String query = (String)properties.get(PROP_COLUMNS_QUERY);
        if (query != null) {
            if (con != null) {
                PreparedStatement stmt = con.prepareStatement(query);
                stmt.setString(1, catalog);
                stmt.setString(2, schemaPattern);
                stmt.setString(3, tableNamePattern);
                stmt.setString(4, columnNamePattern);
                return stmt.executeQuery();
            } else throw new SQLException(bundle.getString("EXC_NoConnection")); // NOI18N
        }

        if (dmd == null) throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
        return dmd.getColumns(catalog, schemaPattern, tableNamePattern, columnNamePattern);
    }

    public String getColumnsQuery()
    {
        return getString(PROP_COLUMNS_QUERY);
    }

    public void setColumnsQuery(String value)
    {
        setString(PROP_COLUMNS_QUERY, value);
    }

    /**
    * Gets a description of the access rights for a table's columns.
    */
    public ResultSet getColumnPrivileges(String catalog, String schema, String table, String columnNamePattern) throws SQLException
    {
        if (getCapitializeUsername() && schema != null) schema = schema.toUpperCase();
        String query = (String)properties.get(PROP_COLUMNS_PRIVILEGES_QUERY);
        if (query != null) {
            if (con != null) {
                PreparedStatement stmt = con.prepareStatement(query);
                stmt.setString(1, catalog);
                stmt.setString(2, schema);
                stmt.setString(3, table);
                stmt.setString(4, columnNamePattern);
                return stmt.executeQuery();
            } else throw new SQLException(bundle.getString("EXC_NoConnection")); // NOI18N
        }

        if (dmd == null) throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
        return dmd.getColumnPrivileges(catalog, schema, table, columnNamePattern);
    }

    public String getColumnPrivilegesQuery()
    {
        return getString(PROP_COLUMNS_PRIVILEGES_QUERY);
    }

    public void setColumnPrivilegesQuery(String value)
    {
        setString(PROP_COLUMNS_PRIVILEGES_QUERY, value);
    }

    /**
    * Gets a description of tables available in a catalog. 
    * Only table descriptions matching the catalog, schema, table 
    * name and type criteria are returned. They are ordered by TABLE_TYPE, 
    * TABLE_SCHEM and TABLE_NAME. 
    */
    public ResultSet getTables(String catalog, String schemaPattern, String tableNamePattern, String[] types) throws SQLException
    {
        if (getCapitializeUsername() && schemaPattern!= null) schemaPattern = schemaPattern.toUpperCase();
        String query = (String)properties.get(PROP_TABLES_QUERY);
        if (query != null) {
            if (con != null) {
                StringBuffer typebuff = new StringBuffer();
                PreparedStatement stmt = con.prepareStatement(query);
                stmt.setString(1, catalog);
                stmt.setString(2, schemaPattern);
                stmt.setString(3, tableNamePattern);

                for (int i=0; i<types.length; i++) {
                    if (i > 0) typebuff.append(", ");
                    typebuff.append("'");
                    typebuff.append(types[i]);
                    typebuff.append("'");
                }

                stmt.setString(4, typebuff.toString());
                return stmt.executeQuery();
            } else throw new SQLException(bundle.getString("EXC_NoConnection")); // NOI18N
        }

        if (dmd == null) throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
        return dmd.getTables(catalog, schemaPattern, tableNamePattern, types);
    }

    public String getTablesQuery()
    {
        return getString(PROP_TABLES_QUERY);
    }

    public void setTablesQuery(String value)
    {
        setString(PROP_TABLES_QUERY, value);
    }

    /**
    * Gets a description of the access rights for each table available
    * in a catalog. Note that a table privilege applies to one or
    * more columns in the table. It would be wrong to assume that
    * this priviledge applies to all columns (this may be true for
    * some systems but is not true for all.)
    */
    public ResultSet getTablePrivileges(String catalog, String schemaPattern, String tableNamePattern) throws SQLException
    {
        if (getCapitializeUsername() && schemaPattern!= null) schemaPattern = schemaPattern.toUpperCase();
        String query = (String)properties.get(PROP_TABLE_PRIVILEGES_QUERY);
        if (query != null) {
            if (con != null) {
                PreparedStatement stmt = con.prepareStatement(query);
                stmt.setString(1, catalog);
                stmt.setString(2, schemaPattern);
                stmt.setString(3, tableNamePattern);
                return stmt.executeQuery();
            } else throw new SQLException(bundle.getString("EXC_NoConnection")); // NOI18N
        }

        if (dmd == null) throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
        return dmd.getTablePrivileges(catalog, schemaPattern, tableNamePattern);
    }

    public String getTablePrivilegesQuery()
    {
        return getString(PROP_TABLE_PRIVILEGES_QUERY);
    }

    public void setTablePrivilegesQuery(String value)
    {
        setString(PROP_TABLE_PRIVILEGES_QUERY, value);
    }

    /**
    * Gets a description of a table's optimal set of columns that
    * uniquely identifies a row. They are ordered by SCOPE.
    */
    public ResultSet getBestRowIdentifier(String catalog, String schema, String table, int scope, boolean nullable) throws SQLException
    {
        if (getCapitializeUsername() && schema != null) schema = schema.toUpperCase();
        String query = (String)properties.get(PROP_BEST_ROW_IDENTIFIER);
        if (query != null) {
            if (con != null) {
                PreparedStatement stmt = con.prepareStatement(query);
                stmt.setString(1, catalog);
                stmt.setString(2, schema);
                stmt.setString(3, table);
                stmt.setInt(4, scope);
                stmt.setBoolean(5, nullable);
                return stmt.executeQuery();
            } else throw new SQLException(bundle.getString("EXC_NoConnection")); // NOI18N
        }

        if (dmd == null) throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
        return dmd.getBestRowIdentifier(catalog, schema, table, scope, nullable);
    }

    public String getBestRowIdentifierQuery()
    {
        return getString(PROP_BEST_ROW_IDENTIFIER);
    }

    public void setBestRowIdentifierQuery(String value)
    {
        setString(PROP_BEST_ROW_IDENTIFIER, value);
    }

    /**
    * Gets a description of a table's columns that are automatically
    * updated when any value in a row is updated.  They are
    * unordered.
    */
    public ResultSet getVersionColumns(String catalog, String schema, String table) throws SQLException
    {
        if (getCapitializeUsername() && schema != null) schema = schema.toUpperCase();
        String query = (String)properties.get(PROP_VERSION_COLUMNS);
        if (query != null) {
            if (con != null) {
                PreparedStatement stmt = con.prepareStatement(query);
                stmt.setString(1, catalog);
                stmt.setString(2, schema);
                stmt.setString(3, table);
                return stmt.executeQuery();
            } else throw new SQLException(bundle.getString("EXC_NoConnection")); // NOI18N
        }

        if (dmd == null) throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
        return dmd.getVersionColumns(catalog, schema, table);
    }

    public String getVersionColumnsQuery()
    {
        return getString(PROP_VERSION_COLUMNS);
    }

    public void setVersionColumnsQuery(String value)
    {
        setString(PROP_VERSION_COLUMNS, value);
    }

    /**
    * Gets a description of a table's primary key columns.  They
    * are ordered by COLUMN_NAME.
    */
    public ResultSet getPrimaryKeys(String catalog, String schema, String table) throws SQLException
    {
        if (getCapitializeUsername() && schema != null) schema = schema.toUpperCase();
        String query = (String)properties.get(PROP_PK_QUERY);
        if (query != null) {
            if (con != null) {
                PreparedStatement stmt = con.prepareStatement(query);
                stmt.setString(1, catalog);
                stmt.setString(2, schema);
                stmt.setString(3, table);
                return stmt.executeQuery();
            } else throw new SQLException(bundle.getString("EXC_NoConnection")); // NOI18N
        }

        if (dmd == null) throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
        return dmd.getPrimaryKeys(catalog, schema, table);
    }

    public String getPrimaryKeysQuery()
    {
        return getString(PROP_PK_QUERY);
    }

    public void setPrimaryKeysQuery(String value)
    {
        setString(PROP_PK_QUERY, value);
    }

    /**
    * Gets a description of the primary key columns that are
    * referenced by a table's foreign key columns (the primary keys
    * imported by a table).  They are ordered by PKTABLE_CAT,
    * PKTABLE_SCHEM, PKTABLE_NAME, and KEY_SEQ.
    * @see #getExportedKeys 
    */
    public ResultSet getImportedKeys(String catalog, String schema, String table) throws SQLException
    {
        if (getCapitializeUsername() && schema != null) schema = schema.toUpperCase();
        String query = (String)properties.get(PROP_IK_QUERY);
        if (query != null) {
            if (con != null) {
                PreparedStatement stmt = con.prepareStatement(query);
                stmt.setString(1, catalog);
                stmt.setString(2, schema);
                stmt.setString(3, table);
                return stmt.executeQuery();
            } else throw new SQLException(bundle.getString("EXC_NoConnection")); // NOI18N
        }

        if (dmd == null) throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
        return dmd.getImportedKeys(catalog, schema, table);
    }

    public String getImportedKeysQuery()
    {
        return getString(PROP_IK_QUERY);
    }

    public void setImportedKeysQuery(String value)
    {
        setString(PROP_IK_QUERY, value);
    }

    /**
    * Gets a description of the foreign key columns that reference a
    * table's primary key columns (the foreign keys exported by a
    * table).  They are ordered by FKTABLE_CAT, FKTABLE_SCHEM,
    * FKTABLE_NAME, and KEY_SEQ.
    * @see #getImportedKeys 
    */
    public ResultSet getExportedKeys(String catalog, String schema, String table) throws SQLException
    {
        if (getCapitializeUsername() && schema != null) schema = schema.toUpperCase();
        String query = (String)properties.get(PROP_EK_QUERY);
        if (query != null) {
            if (con != null) {
                PreparedStatement stmt = con.prepareStatement(query);
                stmt.setString(1, catalog);
                stmt.setString(2, schema);
                stmt.setString(3, table);
                return stmt.executeQuery();
            } else throw new SQLException(bundle.getString("EXC_NoConnection")); // NOI18N
        }

        if (dmd == null) throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
        return dmd.getExportedKeys(catalog, schema, table);
    }

    public String getExportedKeysQuery()
    {
        return getString(PROP_EK_QUERY);
    }

    public void setExportedKeysQuery(String value)
    {
        setString(PROP_EK_QUERY, value);
    }

    /**
    * Gets a description of the foreign key columns in the foreign key
    * table that reference the primary key columns of the primary key
    * table (describe how one table imports another's key.) This
    * should normally return a single foreign key/primary key pair
    * (most tables only import a foreign key from a table once.)  They
    * are ordered by FKTABLE_CAT, FKTABLE_SCHEM, FKTABLE_NAME, and
    * KEY_SEQ.
    */
    public ResultSet getCrossReference(String catalog, String schema, String table, String foreignCatalog, String foreignSchema, String foreignTable) throws SQLException
    {
        if (getCapitializeUsername() && schema != null) schema = schema.toUpperCase();
        String query = (String)properties.get(PROP_CROSSREF_QUERY);
        if (query != null) {
            if (con != null) {
                PreparedStatement stmt = con.prepareStatement(query);
                stmt.setString(1, catalog);
                stmt.setString(2, schema);
                stmt.setString(3, table);
                stmt.setString(4, foreignCatalog);
                stmt.setString(5, foreignSchema);
                stmt.setString(6, foreignTable);
                return stmt.executeQuery();
            } else throw new SQLException(bundle.getString("EXC_NoConnection")); // NOI18N
        }

        if (dmd == null) throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
        return dmd.getCrossReference(catalog, schema, table, foreignCatalog, foreignSchema, foreignTable);
    }

    public String getCrossReferenceQuery()
    {
        return getString(PROP_CROSSREF_QUERY);
    }

    public void setCrossReferenceQuery(String value)
    {
        setString(PROP_CROSSREF_QUERY, value);
    }

    /**
    * Gets a description of all the standard SQL types supported by
    * this database. They are ordered by DATA_TYPE and then by how
    * closely the data type maps to the corresponding JDBC SQL type.
    */
    public ResultSet getTypeInfo() throws SQLException
    {
        String query = (String)properties.get(PROP_TYPE_INFO_QUERY);
        if (query != null) {
            if (con != null) return con.createStatement().executeQuery(query);
            else throw new SQLException(bundle.getString("EXC_NoConnection")); // NOI18N
        }

        if (dmd == null) throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
        return dmd.getTypeInfo();
    }

    public String getTypeInfoQuery()
    {
        return getString(PROP_TYPE_INFO_QUERY);
    }

    public void setTypeInfoQuery(String value)
    {
        setString(PROP_TYPE_INFO_QUERY, value);
    }

    /**
    * Gets a description of a table's indices and statistics. They are
    * ordered by NON_UNIQUE, TYPE, INDEX_NAME, and ORDINAL_POSITION.
    *
    */
    public ResultSet getIndexInfo(String catalog, String schema, String table, boolean unique, boolean approximate) throws SQLException
    {
        if (getCapitializeUsername() && schema != null) schema = schema.toUpperCase();
        String query = (String)properties.get(PROP_INDEX_INFO_QUERY);
        if (query != null) {
            if (con != null) {
                PreparedStatement stmt = con.prepareStatement(query);
                stmt.setString(1, catalog);
                stmt.setString(2, schema);
                stmt.setString(3, table);
                stmt.setBoolean(4, unique);
                stmt.setBoolean(5, approximate);
                return stmt.executeQuery();
            } else throw new SQLException(bundle.getString("EXC_NoConnection")); // NOI18N
        }

        if (dmd == null) throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
        return dmd.getIndexInfo(catalog, schema, table, unique, approximate);
    }

    public String getIndexInfoQuery()
    {
        return getString(PROP_INDEX_INFO_QUERY);
    }

    public void setIndexInfoQuery(String value)
    {
        setString(PROP_INDEX_INFO_QUERY, value);
    }

    /**
    * Does the database support the given result set type?
    * @param type defined in <code>java.sql.ResultSet</code>
    * @return <code>true</code> if so
    * @exception SQLException if a database access error occurs
    * @see Connection
    */
    public boolean supportsResultSetType(int type) throws SQLException
    {
        if (dmd != null) return dmd.supportsResultSetType(type);
        else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
    }

    /**
    * Does the database support the concurrency type in combination
    * with the given result set type?
    * @param type defined in <code>java.sql.ResultSet</code>
    * @param concurrency type defined in <code>java.sql.ResultSet</code>
    * @return <code>true</code> if so
    * @exception SQLException if a database access error occurs
    * @see Connection
    */
    public boolean supportsResultSetConcurrency(int type, int concurrency) throws SQLException
    {
        if (dmd != null) return dmd.supportsResultSetConcurrency(type, concurrency);
        else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
    }

    /**
    * Indicates whether a result set's own updates are visible.
    * @param result set type, i.e. ResultSet.TYPE_XXX
    * @exception SQLException if a database access error occurs
    */
    public boolean ownUpdatesAreVisible(int type) throws SQLException
    {
        if (dmd != null) return dmd.ownUpdatesAreVisible(type);
        else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
    }

    /**
    * Indicates whether a result set's own deletes are visible.
    * @param result set type, i.e. ResultSet.TYPE_XXX
    * @return <code>true</code> if deletes are visible for the result set type
    * @exception SQLException if a database access error occurs
    */
    public boolean ownDeletesAreVisible(int type) throws SQLException
    {
        if (dmd != null) return dmd.ownDeletesAreVisible(type);
        else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
    }

    /**
    * Indicates whether a result set's own inserts are visible.
    * @param result set type, i.e. ResultSet.TYPE_XXX
    * @return <code>true</code> if inserts are visible for the result set type
    * @exception SQLException if a database access error occurs
    */
    public boolean ownInsertsAreVisible(int type) throws SQLException
    {
        if (dmd != null) return dmd.ownInsertsAreVisible(type);
        else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
    }

    /**
    * Indicates whether updates made by others are visible.
    * @param result set type, i.e. ResultSet.TYPE_XXX
    * @return <code>true</code> if updates made by others
    * are visible for the result set type
    * @exception SQLException if a database access error occurs
    */
    public boolean othersUpdatesAreVisible(int type) throws SQLException
    {
        if (dmd != null) return dmd.othersUpdatesAreVisible(type);
        else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
    }

    /**
    * Indicates whether deletes made by others are visible.
    * @param result set type, i.e. ResultSet.TYPE_XXX
    * @return <code>true</code> if deletes made by others
    * are visible for the result set type
    * @exception SQLException if a database access error occurs
    */
    public boolean othersDeletesAreVisible(int type) throws SQLException
    {
        if (dmd != null) return dmd.othersDeletesAreVisible(type);
        else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
    }

    /**
    * Indicates whether inserts made by others are visible.
    * @param result set type, i.e. ResultSet.TYPE_XXX
    * @return true if updates are visible for the result set type
    * @return <code>true</code> if inserts made by others
    * are visible for the result set type
    * @exception SQLException if a database access error occurs
    */
    public boolean othersInsertsAreVisible(int type) throws SQLException
    {
        if (dmd != null) return dmd.othersInsertsAreVisible(type);
        else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
    }

    /**
    * Indicates whether or not a visible row update can be detected by 
    * calling the method <code>ResultSet.rowUpdated</code>.
    * @param result set type, i.e. ResultSet.TYPE_XXX
    * @return <code>true</code> if changes are detected by the result set type
    * @exception SQLException if a database access error occurs
    */
    public boolean updatesAreDetected(int type) throws SQLException
    {
        if (dmd != null) return dmd.updatesAreDetected(type);
        else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
    }

    /**
    * Indicates whether or not a visible row delete can be detected by 
    * calling ResultSet.rowDeleted().  If deletesAreDetected()
    * returns false, then deleted rows are removed from the result set.
    * @param result set type, i.e. ResultSet.TYPE_XXX
    * @return true if changes are detected by the resultset type
    * @exception SQLException if a database access error occurs
    */
    public boolean deletesAreDetected(int type) throws SQLException
    {
        if (dmd != null) return dmd.deletesAreDetected(type);
        else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
    }

    /**
    * Indicates whether or not a visible row insert can be detected
    * by calling ResultSet.rowInserted().
    * @param result set type, i.e. ResultSet.TYPE_XXX
    * @return true if changes are detected by the resultset type
    * @exception SQLException if a database access error occurs
    */
    public boolean insertsAreDetected(int type) throws SQLException
    {
        if (dmd != null) return dmd.insertsAreDetected(type);
        else throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
    }

    /**
    * Indicates whether the driver supports batch updates.
    * @return true if the driver supports batch updates
    */
    public boolean supportsBatchUpdates() throws SQLException {
        Boolean flag = (Boolean)properties.get(PROP_BATCH_UPDATES);
        if (flag == null) {
            if (dmd != null)
                try {
                    flag = dmd.supportsBatchUpdates() ? Boolean.TRUE : Boolean.FALSE;
                } catch (AbstractMethodError exc) {
                    //PENDING - unknown problem with AbstractMethodError
                    //bug #27858 (http://db.netbeans.org/issues/show_bug.cgi?id=27858)
                }
            else
                throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
            properties.put(PROP_BATCH_UPDATES, flag);
        }

        return flag.booleanValue();
    }

    public int getBatchUpdates()
    {
        return getBoolean(PROP_BATCH_UPDATES);
    }

    public void setBatchUpdates(int value)
    {
        setBoolean(PROP_BATCH_UPDATES, value);
    }

    /**
    * Gets a description of the user-defined types defined in a particular
    * schema.  Schema-specific UDTs may have type JAVA_OBJECT, STRUCT, 
    * or DISTINCT.
    */
    public ResultSet getUDTs(String catalog, String schemaPattern, String typeNamePattern, int[] types) throws SQLException
    {
        if (getCapitializeUsername() && schemaPattern != null) schemaPattern = schemaPattern.toUpperCase();
        String query = (String)properties.get(PROP_UDT_QUERY);
        if (query != null) {
            if (con != null) {
                StringBuffer typebuff = new StringBuffer();
                PreparedStatement stmt = con.prepareStatement(query);
                stmt.setString(1, catalog);
                stmt.setString(2, schemaPattern);
                stmt.setString(3, typeNamePattern);

                for (int i=0; i<types.length; i++) {
                    if (i > 0) typebuff.append(", ");
                    typebuff.append(types[i]);
                }

                stmt.setString(4, typebuff.toString());
                return stmt.executeQuery();
            } else throw new SQLException(bundle.getString("EXC_NoConnection")); // NOI18N
        }

        if (dmd == null) throw new SQLException(bundle.getString("EXC_NoDBMetadata")); // NOI18N
        return dmd.getUDTs(catalog, schemaPattern, typeNamePattern, types);
    }

    public String getUDTsQuery()
    {
        return getString(PROP_UDT_QUERY);
    }

    public void setUDTsQuery(String value)
    {
        setString(PROP_UDT_QUERY, value);
    }

    // Extended properties

    public boolean getCapitializeUsername() {
        Boolean flag = (Boolean) properties.get("capitializeUsername");
        if(flag == null) {
            //flag = Boolean.TRUE;
            flag = Boolean.FALSE; 
            properties.put("capitializeUsername", flag);
        }
        return flag.booleanValue();
    }

    public void setCapitializeUsername(boolean value)
    {
        Boolean newValue, oldValue = (Boolean)properties.get(PROP_CAPITALIZE_USERNAME);
        newValue = value ? Boolean.TRUE : Boolean.FALSE;
        properties.put(PROP_CAPITALIZE_USERNAME, newValue);
        propertySupport.firePropertyChange(PROP_CAPITALIZE_USERNAME, oldValue, newValue);
    }

    
    //JDK 1.4 / JDBC 3.0
    // workaround to be compilable under JDK 1.4
    // the following methods don't return real values
    
    public ResultSet getAttributes(String catalog, String schemaPattern, String typeNamePattern, String attributeNamePattern) throws SQLException {
        return null;
    }
    
    public int getDatabaseMajorVersion() throws SQLException {
        return dmd.getDatabaseMajorVersion();
    }
    
    public int getDatabaseMinorVersion() throws SQLException {
        return -1;
    }
    
    public int getJDBCMajorVersion() throws SQLException {
        return -1;
    }
    
    public int getJDBCMinorVersion() throws SQLException {
        return -1;
    }
    
    public int getResultSetHoldability() throws SQLException {
        return -1;
    }
    
    public int getSQLStateType() throws SQLException {
        return -1;
    }
    
    public ResultSet getSuperTables(String catalog, String schemaPattern, String tableNamePattern) throws SQLException {
        return null;
    }
    
    public ResultSet getSuperTypes(String catalog, String schemaPattern, String typeNamePattern) throws SQLException {
        return null;
    }
    
    public boolean locatorsUpdateCopy() throws SQLException {
        return false;
    }
    
    public boolean supportsGetGeneratedKeys() throws SQLException {
        return false;
    }
    
    public boolean supportsMultipleOpenResults() throws SQLException {
        return false;
    }
    
    public boolean supportsNamedParameters() throws SQLException {
        return false;
    }
    
    public boolean supportsResultSetHoldability(int holdability) throws SQLException {
        return false;
    }
    
    public boolean supportsSavepoints() throws SQLException {
        return false;
    }
    
    public boolean supportsStatementPooling() throws SQLException {
        return false;
    }
    
    //JDK 1.6 / JDBC 4.0
    
    public boolean supportsStoredFunctionsUsingCallSyntax() throws SQLException {
        return false;
    }
    
    public boolean isWrapperFor (Class clazz) {
        return false;
    }

    public Object unwrap(java.lang.Class iface) throws java.sql.SQLException {
        return null;
    }
    
    public java.sql.RowIdLifetime getRowIdLifetime() throws SQLException {
        return null;
    }

    public ResultSet getSchemas(String catalog, String schemaPattern) throws SQLException {
        return null;
    }
    
    public ResultSet getClientInfoProperties() throws SQLException {
        return null;
    }
    
    public ResultSet getFunctionColumns(String catalog, String schemaPattern, String functionNamePattern, String columnNamePattern) throws SQLException {
        return null;
    }
    
    public ResultSet getFunctions(String catalog, String schemaPattern, String functionNamePattern) throws SQLException {
        return null;
    }
    
    public boolean providesQueryObjectGenerator() throws SQLException {
        return false;
    }
    
    public boolean autoCommitFailureClosesAllResultSets() throws SQLException {
        return false;
    }
    
    //
    // JDK7
    // 

    public ResultSet getPseudoColumns(String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern) throws SQLException {
        return null;
    }

    public boolean generatedKeyAlwaysReturned() throws SQLException {
        return false;
    }
}
