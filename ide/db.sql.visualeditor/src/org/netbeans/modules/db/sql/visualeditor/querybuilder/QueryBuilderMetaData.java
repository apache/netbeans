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
package org.netbeans.modules.db.sql.visualeditor.querybuilder;

import org.netbeans.api.db.explorer.DatabaseConnection;

import org.netbeans.modules.db.sql.visualeditor.querymodel.Column;
import org.netbeans.modules.db.sql.visualeditor.api.VisualSQLEditorMetaData;
import org.netbeans.modules.db.sql.visualeditor.Log;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.sql.SQLException;

public class QueryBuilderMetaData {

    // Metadata managed by the QueryEditor
    private Map<String, List<String>>   importKcTable = new Hashtable<>();
    private Map<String, String>         allColumnNames = null;

    // Metadata object
    // This will contain *either* a metadata object provided by the client,
    // *or* one that is generated internally
    private VisualSQLEditorMetaData metadata;
    private QueryBuilder 	queryBuilder;

    private boolean 		DEBUG = false;

    // Constructor, with external metaData
    // Used by most clients
    QueryBuilderMetaData(VisualSQLEditorMetaData vseMetaData, QueryBuilder queryBuilder) {
        this.metadata = vseMetaData;
        this.queryBuilder = queryBuilder;
    }

    // Constructor, without external metaData
    // Used by DB Explorer and other clients who don't have a dependency on VSE
    QueryBuilderMetaData(DatabaseConnection dbconn, QueryBuilder queryBuilder) {
        this.metadata = new InternalVSEMetaDataImpl(dbconn);
        this.queryBuilder = queryBuilder;
    }


    // Various schema methods
    boolean isSchemaName(String schemaName) {
        List<String> schemas = getSchemas();
        if (schemas.contains(schemaName)) {
            Log.getLogger().finest(" found schema name " + schemaName);
            return true;
        }
        return false;
    }

    boolean isTableName(String tableName) {
        try {
            String x = checkTableName(tableName);
            if (x != null) {
                return true;
            }
        } catch (SQLException se) {
            // exception handled elsewhere.
        }
        return false;
    }

    boolean isColumnName(String columnName) {

        try {
            if (allColumnNames == null) {
                getAllColumnNames();
            }
            return allColumnNames.containsKey(columnName);
        } catch (SQLException se) {
            // exception handled elsewhere.
        }
        return false;
    }

    // Return the list of columns that may appear in queries
    // Implemented by fetching all columns for all known tables
    // ToDo: Decide how to avoid re-fetching all the information
    void getAllColumnNames() throws SQLException {
        allColumnNames = new Hashtable<>(500);
        List<List<String>> tables = getTables();
        for (List<String> table : tables) {
            List<String> columns = getColumns(table.get(0), table.get(1));
            for (String column : columns) {
                allColumnNames.put(column, column);
            }
        }
    }

    /**
     *   Check if the table name exists
     *   Case is ignored while searching
     *   If table does not exist then return null
     *   else return the same name if it exactly matches
     *   else return the name from the database
     *
     *   tableName can be schema.table or just table - look for both.
     */
    String checkTableName(String tableName) throws SQLException {

        Log.getLogger().entering("QueryBuilderMetaData", "checkTableName", tableName); // NOI18N
        if (tableName == null || tableName.length() < 1) {
            return tableName;
        }

        String[] descrip = parseTableName(tableName);
        String paramSchemaName = descrip[0];
        String paramTableName = descrip[1];

        if (paramSchemaName != null) {
            return checkFullTableName(tableName);
        }

        String returnTable = null;

        List<String> tables = getAllTables();
        // now search for the tablename in the list.

        for (String fullNameDb : tables) {
            // first check if the table name exists as is
            String tableNameDb = parseTableName(fullNameDb)[1];
            if (tableNameDb.equalsIgnoreCase(paramTableName)) {
                returnTable = fullNameDb;
                break;
            }
        }

        if (returnTable == null) {
            String fullAliasTableName = queryBuilder.getQueryModel().getFullTableName(paramTableName);
            if (fullAliasTableName != null && tableName.equals(fullAliasTableName)) {
                return null;
            } else if (fullAliasTableName != null) {
                return checkTableName(fullAliasTableName);
            }
        }

        if (returnTable != null) {
            getColumnNames(returnTable);
        }
        // table name was not found
        return returnTable;
    }


    /**
     *   Check if the full table name exists in the _tableColumns
     *   Case is ignored while searching
     *   If table does not exist then return null
     *
     *   return the same name if it exactly matches
     *   if case differs, return the full name from the database
     *   if it's an alias and there's a cases-insenstive match, return the full name from the database
     *
     *   This function is called only from checkFrom.
     *   Cases to be considered :
     *   case 1 : <schema_name>.<table_name>
     *   case 2 : <table_name> SHOULD NOT EXIST.
     *   case 3 : <alias_table_name>
     *
     *   return the
     *
     */
    String checkFullTableName(String fullTableName) throws SQLException {
        Log.getLogger().entering("QueryBuilderMetaData", "checkFullTableName", fullTableName); // NOI18N
        String returnTable = null;

        if (parseTableName(fullTableName)[0] == null) {
            // no schema name, so fullTableName is really just a tableName.
            return checkTableName(fullTableName);
        }

        List<String> tables = getAllTables();
        for (Iterator i = tables.iterator(); i.hasNext();) {
            // first check if the table name exists as is
            String fullNameDb = (String) i.next();
            if (fullNameDb.equalsIgnoreCase(fullTableName)) {
                returnTable = fullNameDb;
                break;
            }
        }

        // Load the column cache for this table.
        if (returnTable != null) {
            getColumnNames(returnTable);
        }
        return returnTable;
    }


    /**
     *   Check if the column name exists in the _tableColumns
     *   case is ignored while searching
     *   if column does not exist then return null
     *   else return the same name if it exactly matches
     *   else return the name from the database
     */
    String checkColumnName(String tableName, String columnName) throws SQLException {

        Log.getLogger().entering("QueryBuilderMetaData", "checkColumnName", new Object[]{tableName, columnName}); // NOI18N
        String tabName = checkTableName(tableName);

        List columns = getColumnNames(tabName);

        if (columns == null) {
            return null;
        }
        for (int k = 0; k < columns.size(); k++) {
            String columnDB = (String) columns.get(k);
            // first check if the column name exists "as is"
            if (columnName.equals(columnDB)) {
                return columnName;
            } else if (columnName.equalsIgnoreCase(columnDB)) {
                return columnDB;
            }
        }

        // column name was not found
        return null;
    }


    /**
     *  Given a column and table name checks if the table name stored in the
     *  column matches with the one in the database.
     *  Updates the column name with the one in the database and returns true
     *  false otherwise.
     */

    boolean checkColumnNameForTable(Column col, String tableName) {
        String columnName = col.getColumnName();

        Log.getLogger().entering("QueryBuilderMetaData", "checkColumnNameForTable", tableName); // NOI18N
        String fullTableNameFromAlias = queryBuilder.getQueryModel().getFullTableName(tableName);
        if (fullTableNameFromAlias != null) {
            tableName = fullTableNameFromAlias;
        }
        boolean retVal = false;

        // TODO JFB should not catch this.
        List<String> cols;
        String checkedTable;
        try {
            checkedTable = checkTableName(tableName);
            if (checkedTable == null) {
                return false;
            }
            cols = getColumnNames(checkedTable);
        } catch (SQLException sqle) {
            Log.getLogger().finest("  ** problems getting metadata " + sqle.getMessage());
            return false;
        }
        if ("*".equals(columnName)) {            // NOI18N
	    retVal = true;
            if (fullTableNameFromAlias == null && !(checkedTable.equals(col.getTableSpec()))) {
                col.setTableSpec(col.getTableSpec(), checkedTable);
                Log.getLogger().finest(" adjust table to " + checkedTable);
            }
        } else {
            for (int icnt = 0; icnt < cols.size(); icnt++) {
                if (columnName.equalsIgnoreCase(cols.get(icnt))) {
                    col.setColumnName(col.getColumnName(), cols.get(icnt));
                    Log.getLogger().finest(" adjust colname to " + cols.get(icnt));
                    if (col.getTableSpec() == null) {
                        col.setTableSpec(col.getTableSpec(), checkedTable);
                        Log.getLogger().finest(" adjust table to " + checkedTable);
                    }
                    retVal = true;
                    break;
                }
            }
        }

        Log.getLogger().finest("checkColumnNameForTable found=" + retVal); // NOI18N
        return retVal;

	/***
        for ( int i = 0; i < _tableColumns.size(); i++ ) {
	    TableColumns tableColumn = (TableColumns) _tableColumns.get(i);
	    String _tableName = tableColumn.getTableName();
	    // first check if the table name exists "as is"
	    // table name must already be valid using checkTableName
	    // reset the column's table spec.
	    if ( _tableName.equals( tableName ) ) {
		List columns = tableColumn.getColumns();
		for ( int k = 0; k < columns.size(); k++ ) {
		    String _columnName = (String) columns.get(k);
		    if ( ( _columnName.equals( columnName ) ) ||
			 ( _columnName.equalsIgnoreCase( columnName ) ) ) {
			// change the column's table name
			col.setTableSpec(col.getTableSpec(), tableName);
			// change the column's name to the correct one.
			col.setColumnName(col.getColumnName(), _columnName);
			return true;
		    }
		}
	    } else {
		// check if the tableName is actually an alias
		String fullTableNameFromAlias =
		    _queryModel.getFullTableName( tableName );
		if ( fullTableNameFromAlias != null ) {
		    // tableName is an alias
		    if ( _tableName.equals( fullTableNameFromAlias ) ) {
			if ( ! tableColumn.columnsLoaded() ) {
			    loadColumns( tableColumn );
			}
			List columns = tableColumn.getColumns();
			for ( int k = 0; k < columns.size(); k++ ) {
			    String _columnName = (String) columns.get(k);
			    if ( ( _columnName.equals( columnName ) ) ||
				 ( _columnName.equalsIgnoreCase( columnName ) ) ) {
				// change the column's table name
				col.setColumnTableName(fullTableNameFromAlias);
				// change the column's corr name
				col.setColumnCorrName(tableName);
				// change the column's name to the correct one.
				col.setColumnName(col.getColumnName(), _columnName);
				return true;
			    }
			}
		    }
		}
	    }
        }
        return false;
	***/
    }


    // checks the table name and column name given a col.
    // if possible corrects the column name and table name
    // otherwise returns false, the caller is supposed to give an error message
    boolean checkTableColumnName(Column col) throws SQLException {

        String tableSpec = col.getTableSpec();
        String tableName = col.getFullTableName();
        String colName = col.getColumnName();

        Log.getLogger().finest("checkTableColunName col=*, notable " + " tableSpec = " + tableSpec +
			       " tableName = " + tableName + " . " + colName); // NOI18N
        if ("*".equals(col.getColumnName()) && tableSpec == null) {
            // Column name was "*" with no tableSpec - assume it's OK.
            Log.getLogger().finest("checkTableColunName col=*, notable ");
            return true; //NOI18N
        }
        String checkedTableName = checkTableName(tableSpec);

        String fullTableNameFromAlias = null;
//        if ( checkedTableName == null && tableSpec != null ) {
        // why the above check ? This will not set fullTableNameFromAlias
        // regression
        // http://daning.sfbay/cvsweb/queryeditor/src/com/sun/rave/queryeditor/querybuilder/QueryBuilder.java.diff?r1=1.133&r2=1.134&cvsroot=/cvs/rave
        fullTableNameFromAlias = queryBuilder.getQueryModel().getFullTableName(tableSpec);
//        }
	Log.getLogger().finest("checkTableColumnName called. " + " checkedTableName = " + checkedTableName +
			       " fullTableNameFromAlias = " + fullTableNameFromAlias); // NOI18N
        if (checkedTableName == null) {
            // table not found
            return false; // let the caller display the error
        } else if ((fullTableNameFromAlias != null) && (!fullTableNameFromAlias.equalsIgnoreCase(tableSpec))) {
            if (DEBUG) {
                System.out.println("setColumnTableName called. " + " checkedTableName = " + checkedTableName +
				   " tableSpec = " + tableSpec + " fullTableNameFromAlias = " + fullTableNameFromAlias + "\n"); // NOI18N
            }
            col.setColumnTableName(checkedTableName);
            col.setColumnCorrName(tableSpec);
        } else if (!checkedTableName.equals(tableName)) {
            // table found but maybe in a wrong case, replace
            // it in the querymodel
            if (DEBUG) {
                System.out.println("setTableSpec called. " + " checkedTableName = " + checkedTableName + "\n"); // NOI18N
            }
            col.setTableSpec(tableName, checkedTableName);
        }

        String columnName = col.getColumnName();

        if (columnName.equals("*")) {
            return true;
        }
        String checkedColumnName = checkColumnName(checkedTableName, columnName);
        if (DEBUG) {
            System.out.println("column Name = " + columnName + "\n" + "checked column Name = " + checkedColumnName + "\n"); // NOI18N
        }
        if (checkedColumnName == null) {
            // column not found
            return false; // let the caller display the error
        } else if (!checkedColumnName.equals(columnName)) {
            if (DEBUG) 
                System.out.println("set column name called. oldColumnName = " + columnName +
				   " newColumnName = " + checkedColumnName + "\n"); // NOI18N
            // column found but maybe in a wrong case, replace
            // it in the querymodel
            col.setColumnName(columnName, checkedColumnName);
        }

        return true;
    }


    /**
     * Returns the list of tables and views
     */
    List<String> getAllTables() throws SQLException {
        /*
        List tables = getTablesInternal("TABLE");
        tables.addAll(getTablesInternal("VIEW"));
        return tables;
         */
        // return metaDataCache.getTables() ;
        List<List<String>> tables = getTables();

        // Convert from List<table, schema> to "table.schema", expected by query editor
        List<String> result = new ArrayList<>();
        for (List<String> fullTable : tables) {
            String schema = fullTable.get(0);
            String table  = fullTable.get(1);
            result.add(((schema == null) || (schema.equals(""))) ? table : schema + "." + table);
        }
        return result;
    }

    /* ===== JFB
    private List getTablesInternal(String type) {
	List tableNames = new ArrayList();
	if ( checkDatabaseConnection() == false ) {
	    return tableNames;
	}
	boolean firstTime = true;
	while ( true ) {
	    try {
		checkMetaData();
		TableMetaData[] tmd;
		if ( Log.isLoggable()) Log.log("start get"+type+"MetaData") ;
		tmd = (type.equals("TABLE")) ? _dbmdh.getTableMetaData() : _dbmdh.getViewMetaData();
		if ( Log.isLoggable()) Log.log("end get"+type+"MetaData") ;
		for (int i=0; i<tmd.length; i++)
		    tableNames.add(getFullTableName(tmd[i]));
		break;
	    } catch (SQLException sqle) {
		if ( firstTime ) {
		    refreshDataBaseMetaData();
		    firstTime = false;
		} else {
		    reportDatabaseError("DATABASE_ERROR", sqle); // NOI18N
		    break;
		}
	    }
	}
	return tableNames;
    }
    *****/


    //     private String getFullTableName(TableMetaData tmd) throws SQLException {
//         if (DEBUG) {
//             System.out.println(" getFullTableName() called " + "\n" ); // NOI18N
//         }
//         String schema = tmd.getMetaInfo(TableMetaData.TABLE_SCHEM);
//         /*
//          * !JK always show schema
//          * if (schema == null || schema.trim().equals("") || isSchemaInPath(schema)) {
//          }
//          */
//         if (schema == null || schema.trim().equals("")) {
//             schema = "";
//         } else {
//             schema += ".";
//         }
//         String tableName = tmd.getMetaInfo(TableMetaData.TABLE_NAME);
//
//         // if table name does not contain spaces
//         if (tableName.indexOf(' ') == -1 ) {
//             return schema + tableName;
//         } else {
//             return schema + "\"" + tableName + "\"";
//         }
//     }
//     /**
//      * Returns the list of tables and views in all schemas that are accessible
//      * through the DataSource associated with this QE
//      */
//     List getAllTablesInDataSource() throws SQLException {
//         // Log.log(" getAllTablesInDataSource() called " + "\n" ); // NOI18N
//         return metaDataCache.getTables() ;
//         /*
//         try {
//             checkMetaData();
//         } catch (SQLException sqle) {
//             reportDatabaseError("DATABASE_ERROR", sqle); // NOI18N
//         }
//         // Get list of schemas in the datasource
//         String[] schemaNames =  sqlStatement.getSchemas();
//         if (schemaNames == null || schemaNames.length == 0)
//             return getAllTables();
//         else {
//             _schemaNames = new ArrayList();
//             List tables = new ArrayList();
//             for (int i=0; i<schemaNames.length; i++) {
//                 tables.addAll(getTablesInternal("TABLE", schemaNames[i]));
//                 tables.addAll(getTablesInternal("VIEW", schemaNames[i]));
//                 _schemaNames.add(schemaNames[i]);
//             }
//             return tables;
//         }
//         */
//     }

    /**
     * Returns the list of table names in the specified schema
     */
    /**** JFB
    private List getTablesInternal(String type, String schemaName) {
	if (Log.isLoggable() ) Log.log("enter tablesInternal "+type+","+schemaName) ;
	List tableNames = new ArrayList();
	if ( checkDatabaseConnection() == false )
	    return tableNames;
	boolean firstTime = true;
	while ( true ) {
	    try {
		checkMetaData();
		String[] tables =
		    (type.equals("TABLE")) ?_dbmdh.getTables(schemaName) : _dbmdh.getViews(schemaName);     // NOI18N
		// Convert to ArrayList, because caller expects it
		for (int i=0; i<tables.length; i++) {
		    tableNames.add(tables[i]);
		    if (DEBUG)
			System.out.println(" getAllTablesInternal() tables [ " + i + " ]  = " + tables[i]  + "\n" ); // NOI18N
		}
		break;
	    } catch (SQLException sqle) {
		if ( firstTime ) {
		    refreshDataBaseMetaData();
		    firstTime = false;
		} else {
		    reportDatabaseError("DATABASE_ERROR", sqle); // NOI18N
		    break;
		}
	    }
	}
	if (Log.isLoggable() ) Log.log("exit tablesInternal, cnt= " + tableNames.size() ) ;
	return tableNames;
    }
    ***/

    /**
     * Returns the set of columns in the specified table.
     * This is obtained from the DbMetaData.
     */
    // SCH: Modified to use schema if available
    /*  JFB
    public List getColumnNames(String tableName) throws SQLException {
	Log.err.log(ErrorManager.INFORMATIONAL,
		    "Entering QueryBuilder.getColumnNames, tableName: " + tableName); // NOI18N
	return metaDataCache.getColumnNames(tableName)) ;
    boolean firstTime = true;
    while ( true ) {
	try {
	    checkMetaData();
	    ResultSet rs = _dbmdh.getMetaData().getColumns(null, null, tableName, "%"); // NOI18N
	    if (rs != null) {
		while (rs.next()) {
		    columnNames.add(rs.getString("COLUMN_NAME")); // NOI18N
		}
		rs.close();
	    }
	    break;
	} catch (SQLException sqle) {
	    if ( firstTime ) {
		refreshDataBaseMetaData();
		firstTime = false;
	    } else {
		reportDatabaseError("DATABASE_ERROR", sqle); // NOI18N
		break;
	    }
	}
    }
    if (DEBUG)
	for (int j=0; j<columnNames.size(); j++)
	    System.out.println("Column ["+j+"] : " + (String) columnNames.get(j) + "\n" ); // NOI18N
    }
   ***/

    /**
     * Returns the imported key columns for this table -- i.e., the columns
     * whose value is a foreign key for another table.  These columns are
     * displayed with a special icon in the Query Builder.
     * This was formerly included in SqlStatementMetaDataCache, now implemented
     * in the QueryEditor
     */
    List<String> getImportedKeyColumns(String fullTableName) throws SQLException {
        List<String> keys = importKcTable.get(fullTableName);
        if (keys != null) {
            return keys;
        }
        String[] tb = parseTableName(fullTableName);
        List<List<String>> importedKeys = getImportedKeys(tb[0], tb[1]);
        keys = new ArrayList<>();
        for (List<String> key : importedKeys) {
            keys.add(key.get(1));
        }
        importKcTable.put(fullTableName, keys);
        return keys;

        /*
        List keys = new ArrayList();
        String tableName, schemaName=null;
        String[] table = fullTableName.split("\\."); // NOI18N
        if (table.length>1) {
	    schemaName=table[0];
	    tableName = table[1];
        } else
	    tableName=table[0];
        boolean firstTime = true;
        while ( true ) {
	    try {
		checkMetaData();
		ResultSet rs = _databaseMetaData.getImportedKeys(null, schemaName, tableName);
		if (rs != null) {
		    while (rs.next()) {
			keys.add(rs.getString("FKCOLUMN_NAME")); // NOI18N
		    }
		    rs.close();
		}
		break;
	    } catch (SQLException sqle) {
		if ( firstTime ) {
		    refreshDataBaseMetaData();
		    firstTime = false;
		} else {
		    reportDatabaseError("DATABASE_ERROR", sqle); // NOI18N
		    break;
		}
	    }
        }
        Log.err.log(ErrorManager.INFORMATIONAL, "Imported key columns for table " + fullTableName); // NOI18N
        if (keys!= null)
	    for (int i=0; i<keys.size(); i++)
		Log.err.log(ErrorManager.INFORMATIONAL, "Keys("+i+"): " + keys.get(i)); // NOI18N
        return keys;
        */
    }

    /**
     * Returns the Foreign Key Constraints that apply to the specified table
     *
     * Result is an a-list of <foreignTable, foreignCol, primTable, primCol>.
     */
    List getForeignKeys(String fullTableName) throws SQLException {

        Log.getLogger().entering("QueryBuilderMetaData", "getForeignKeys", fullTableName); // NOI18N
        // keys.add(new String[] {"travel.trip", "personid", "travel.person", "personid"});
        // We get the exported keys (foreign tables that reference this one), then
        // imported keys (foreign tables that this one references).
        /*
        List keys = getForeignKeys1(fullTableName, true);
        keys.addAll(getForeignKeys1(fullTableName, false));
         */
        String[] tableSpec = parseTableName(fullTableName);
        List<List<String>> keys = getImportedKeys(tableSpec[0], tableSpec[1]);
        keys.addAll(getExportedKeys(tableSpec[0], tableSpec[1]));

        // Convert to a List(String[]), for compatibility with the rest of the QueryEditor
        List<Object> result = new ArrayList<>();
        for (List<String> key : keys) {
            result.add(key.toArray());
        }
        return result;
    }

    /**
     * Returns either the exported or imported keys for this table, depending on the flag
     */
    /*
    List getForeignKeys1(String fullTableName, boolean exported) {
	String tableName, schemaName=null;
	String[] table = fullTableName.split("\\."); // NOI18N
	if (table.length>1) {
	    schemaName=table[0];
	    tableName = table[1];
	} else
	    tableName=table[0];
	Log.log(" getForeignKeys1 schemaName = " + schemaName + " tableName = " + tableName + "\n" ); // NOI18N
	List keys = new ArrayList();
	boolean firstTime = true;
	while ( true ) {
	    try {
		checkMetaData();
		ResultSet rs =
		    exported ?
		    _databaseMetaData.getExportedKeys(null, schemaName, tableName) :
		    _databaseMetaData.getImportedKeys(null, schemaName, tableName);
		if (rs != null) {
		    while (rs.next()) {
			String fschem = rs.getString("FKTABLE_SCHEM"); // NOI18N
			String pschem = rs.getString("PKTABLE_SCHEM"); // NOI18N
			String[] key = new String[] {
			    ((fschem!=null) ? fschem+"." : "") + rs.getString("FKTABLE_NAME"), // NOI18N
			    rs.getString("FKCOLUMN_NAME"), // NOI18N
			    ((pschem!=null) ? pschem+"." : "") + rs.getString("PKTABLE_NAME"), // NOI18N
			    rs.getString("PKCOLUMN_NAME") }; // NOI18N
			keys.add(key);
		    }
		    rs.close();
		}
		break;
	    } catch (SQLException sqle) {
		if ( firstTime ) {
		    refreshDataBaseMetaData();
		    firstTime = false;
		} else {
		    reportDatabaseError("DATABASE_ERROR", sqle); // NOI18N
		    break;
		}
	    }
	}
	return keys;
    }
    */

    /**
     * Returns a FK between this pair of tables if there is one, else null
     * Note that the set of FKs is passed in from the caller, to avoid having to make multiple
     * fetches from the dbmetedata when we're adding a new table
     */
    String[] findForeignKey(String oldFullTableName, String newFullTableName, List foreignKeys) {
        Log.getLogger().entering("QueryBuilderMetaData", "findForeignKey", new Object[]{oldFullTableName, newFullTableName}); // NOI18N
        if (foreignKeys != null) {
            for (int i = 0; i < foreignKeys.size(); i++) {
                String[] key = (String[]) foreignKeys.get(i);
                if ((key[0].equalsIgnoreCase(newFullTableName) &&
		     key[2].equalsIgnoreCase(oldFullTableName)) ||
		    (key[0].equalsIgnoreCase(oldFullTableName) &&
		     key[2].equalsIgnoreCase(newFullTableName)))
		{
                    return (String[]) foreignKeys.get(i);
                }
            }
        }
        Log.getLogger().finest("No key found"); // NOI18N
        return null;
    }


    /**
     * Returns a FK between this pair of tables and columnsif there is one, else null
     */
    String[] findForeignKey(String fullTableName1, String colName1, String fullTableName2, String colName2)
	throws SQLException
    {
        Log.getLogger().entering("QueryBuilderMetaData", "findForeignKey",
				 new Object[]{fullTableName1, colName1, fullTableName1, colName2});

        // Get the complete list of keys for one of the tables; we use table1
        List foreignKeys = getForeignKeys(fullTableName1);
        if (foreignKeys != null) {
            for (int i = 0; i < foreignKeys.size(); i++) {
                String[] key = (String[]) foreignKeys.get(i);
                if ((key[0].equalsIgnoreCase(fullTableName1) &&
		     key[1].equalsIgnoreCase(colName1) &&
		     key[2].equalsIgnoreCase(fullTableName2) &&
		     key[3].equalsIgnoreCase(colName2)) ||
		    (key[0].equalsIgnoreCase(fullTableName2) &&
		     key[1].equalsIgnoreCase(colName2) &&
		     key[2].equalsIgnoreCase(fullTableName1) &&
		     key[3].equalsIgnoreCase(colName1)))
		{
                    return (String[]) foreignKeys.get(i);
                }
            }
        }
        Log.getLogger().finest("No key found"); // NOI18N
        return null;
    }


    // Get the list of column names associated with the specified table name, with no Exception
    public void getColumnNames(String fullTableName, List<String> columnNames) {
        try {
            columnNames.addAll(getColumnNames(fullTableName));
        } catch (SQLException sqle) {
            // can't do anything.
        }
    }

    public List<String> getColumnNames(String fullTableName) throws SQLException {

        // Log.getLogger().entering("QueryBuilderMetaData", "getColumnNames", fullTableName ); // NOI18N
        String[] tb = parseTableName(fullTableName);
        return getColumns(tb[0], tb[1]);

        /*
        String[] table = fullTableName.split("\\.");
        if (table.length==1) // no schema -- use the old method
	    getColumnNames(fullTableName, columnNames);
        else {
	    String[] colNames=null;
	    boolean firstTime = true;
	    try {
		checkMetaData();
		// hack, getColumns throws an exception if table name has
		// spaces.
		colNames = _dbmdh.getColumns(fullTableName.replaceAll("\"", "") );
	    } catch (SQLException sqle) {
		// First time we catch an error, try resetting the RowSet
		refreshDataBaseMetaData();
		try {
		    checkMetaData();
		    colNames = _dbmdh.getColumns(fullTableName);
		} catch (SQLException sqle2) {
		    // We must have a real error.  Report it.
		    reportDatabaseError("DATABASE_ERROR", sqle2); // NOI18N
		}
	    }
	    // Convert to ArrayList because caller expects it
	    if (colNames!=null)
		for (int i=0; i<colNames.length; i++)
		    columnNames.add(colNames[i]);
        }
         */
    }


    /**
     * Returns the primary key columns of the specified table
     */
    List<String> getPrimaryKeys(String fullTableName) throws SQLException {

        Log.getLogger().entering("QueryBuilderMetaData", "getPrimaryKeys", fullTableName); // NOI18N
        String schemaName = null;
        String tableName;
        String[] table = parseTableName(fullTableName);
        if (table.length > 1) {
            schemaName = table[0];
            tableName = table[1];
        } else {
            tableName = table[0];
        }
        return getPrimaryKeys(schemaName, tableName);
    }

    //    private List<List<String>> allTables = null ;
//     // Formerly part of the metadata interface.  Now implemented locally.
//     List<List<String>> getTables() throws SQLException {
// 	if (allTables==null) {
// 	    allTables = new ArrayList<List<String>>() ;
// 	    List<String> schemas = getSchemas();
// 	    for (String schema : schemas) {
// 		List<List<String>> tables = getTables(schema);
// 		for (List<String> table : tables) {
// 		    allTables.add(table);
// 		}
// 	    }
// 	}
// 	return allTables;
//     }

    // Wrapper methods for accessing the actual metadata
    // These use an externally provided one (if available), otherwise the
    // internal one
//     List<List<String>> getTables(String schema) throws SQLException {
// 	return metadata.getTables(schema);
//     }

    List<String> getSchemas() {
        return metadata.getSchemas();
    }

    List<List<String>> getTables() throws SQLException {
        return metadata.getTables();
    }

    List<String> getPrimaryKeys(String schema, String table) throws SQLException {
        return metadata.getPrimaryKeys(schema, table);
    }

    List<List<String>> getImportedKeys(String schema, String table) throws SQLException {
        return metadata.getImportedKeys(schema, table);
    }

    List<List<String>> getExportedKeys(String schema, String table) throws SQLException {
        return metadata.getExportedKeys(schema, table);
    }

    List<String> getColumns(String schema, String table) throws SQLException {
        return metadata.getColumns(schema, table);
    }

    public String getIdentifierQuoteString() {
        try {
            return metadata.getIdentifierQuoteString();
        } catch (SQLException e) {
            return "";
        }
    }

    // JDTODO: figure out what to do here
    void checkDatabaseConnection() throws SQLException {
    }

    // Utility Methods
    /* ================================================================ */
    /*****
     * parse a full table name, e.g. Schema.Table or Table
     * and returns an array where
     * [0] = schema (or null if none found)
     * [1] = table name.
     */
    private static String[] parseTableName(String fullTableName) {
        String[] retVal = new String[2];

        String[] table = fullTableName.split("\\."); // NOI18N
        if (table.length > 1) {
            retVal[0] = table[0];
            retVal[1] = table[1];
        } else {
            retVal[0] = null;
            retVal[1] = table[0];
        }
        return retVal;
    }
}
