/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
/*
 * InternalVSEMetaDataImpl.java
 *
 * Fetches MetaData for VSE, from DatabaseConnection
 */

package org.netbeans.modules.db.sql.visualeditor.querybuilder;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;

import org.netbeans.api.db.explorer.DatabaseConnection;

import org.netbeans.modules.db.sql.visualeditor.Log;
import org.netbeans.modules.db.sql.visualeditor.api.VisualSQLEditorMetaData;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 * Database meta data cache for the QueryBuilder.
 *
 * @author jimdavidson
 */
public class InternalVSEMetaDataImpl implements VisualSQLEditorMetaData {

    private DatabaseMetaData 		databaseMetaData = null;
    private DatabaseConnection 		dbconn;
    private List<String> 		schemas;
    private List<List<String>> 		allTables = null;
    private int 			hashSizeForTables = 30;
    private Hashtable 			fkExportedTable = new Hashtable(30);
    private Hashtable 			fkImportedTable = new Hashtable(30);
    private Hashtable 			columnNameTable = new Hashtable(30);
    private Hashtable 			allColumnsTable = new Hashtable(400);
    private Hashtable 			pkTable = new Hashtable(30);
    private String 			identifierQuoteString = null;

    /** Constructor */

    public InternalVSEMetaDataImpl(DatabaseConnection dbconn) {
        this.dbconn = dbconn;
        try {
            // JDTODO:  add listeners for MetaData changes.
            // dataSourceInfo.addConnectionListener(listener) ;
            
            initMetaData();
        } catch (SQLException sqle) {
            // TODO: Better error handling
            Log.getLogger().warning("Could not create cache for " + sqle.getLocalizedMessage());
        }
    }


    /**
     * Returns the schemas that are included in this DataSource/DatabaseConnection
     * Used during Add Table and similar operations.
     *
     * @return the List of schema names
     */
    /* getSchemas() is different from the other metadata calls, in that it just asks the
     * dbconn or datasource for the list of schemas, not the database
     */
    public List<String> getSchemas() {
        if (schemas != null) {
            return schemas;
        }
        schemas = new ArrayList<String>();
       schemas.add(dbconn.getSchema());
        return schemas;
    }

    /**
     * Returns the tables (and views) in this DataSource/DatabaseConnection
     *
     * @return the List of tables/views, each in the form of a {@literal List<schema, table>}
     */
    // public List<List<String>> getTables(String schema) throws SQLException ;
    public List<List<String>> getTables() throws SQLException {
        if (databaseMetaData == null) {
            initMetaData();
        }
        if (allTables != null) {
            return allTables;
        }
        Log.getLogger().finest(" loading tables");

        allTables = new ArrayList<List<String>>();

        List<String> tmpSchemas = getSchemas();
        if (tmpSchemas != null && tmpSchemas.size() > 0) {
            for (String schema : tmpSchemas) {
                Log.getLogger().finest("  schema: " + schema);
                ResultSet rs = databaseMetaData.getTables(null, schema, "%", new String[]{"TABLE", "VIEW"}); // NOI18N
                while (rs.next()) {
                    ArrayList<String> table = new ArrayList<String>();
                    table.add(rs.getString("TABLE_SCHEM"));
                    table.add(rs.getString("TABLE_NAME"));
                    allTables.add(table);
                }
            }
        } else {
            Log.getLogger().finest(" all schemas");
            ResultSet rs = databaseMetaData.getTables(null, null, "%", new String[]{"TABLE", "VIEW"}); // NOI18N
            while (rs.next()) {
                ArrayList<String> table = new ArrayList<String>();
                table.add(rs.getString("TABLE_SCHEM"));
                table.add(rs.getString("TABLE_NAME"));
                allTables.add(table);
            }
        }
        Log.getLogger().finest(" tables loaded " + allTables.size());

        return allTables;
    }


    /**
     * Returns the columns in the specified schema/table.
     * @return a List of column names
     */
    public List<String> getColumns(String schema, String table) throws SQLException
    {
        Log.getLogger().entering("InternalVSEMetaDataImpl", "getColumns", new Object[]{schema, table});

        if (databaseMetaData == null) {
            initMetaData();
        }
        String fullTableName = mergeTableName(schema, table);
        List<String> columnNames = (List<String>) columnNameTable.get(fullTableName);
        Log.getLogger().finest("    cache hit=" + (columnNames != null));
        if (columnNames != null) {
            return columnNames;
        }
        columnNames = new ArrayList<String>();
        ResultSet rs = databaseMetaData.getColumns(null, schema, table, "%"); // NOI18N
        if (rs != null) {
            while (rs.next()) {
                columnNames.add(rs.getString("COLUMN_NAME")); // NOI18N
            }
            rs.close();

            if (Log.getLogger().isLoggable(java.util.logging.Level.FINEST)) {
                for (int j = 0; j < columnNames.size(); j++) {
                    Log.getLogger().finest("     Column:" + (String) columnNames.get(j)); // NOI18N
                }
            }
        }
        Log.getLogger().finest("   getColumnNames loaded  " + columnNames.size());
        columnNameTable.put(fullTableName, columnNames);
//         for ( int i = 0 ; i < columnNames.size() ; i++) {
//             allColumnsTable.put(columnNames.get(i),fullTableName) ;
//         }
        return columnNames;
    }


    /**
     * Returns the primary key columns for the given schema/table combination.
     *
     * @return the List of columns
     */
    public List<String> getPrimaryKeys(String schema, String table) throws SQLException
    {
        Log.getLogger().entering("InternalVSEMetaDataImpl", "getPrimaryKeys", new Object[]{schema, table});

        if (databaseMetaData == null) {
            initMetaData();
        }
        String fullTableName = mergeTableName(schema, table);
        List primaryKeys = (List) pkTable.get(fullTableName);
        if (primaryKeys != null) {
            return primaryKeys;
        }
        primaryKeys = new ArrayList();

        String[] tableDesrip = parseTableName(fullTableName);

        ResultSet rs = databaseMetaData.getPrimaryKeys(null, schema, table);
        if (rs != null) {
            String name;
            while (rs.next()) {
                name = rs.getString("COLUMN_NAME"); // NOI18N
                primaryKeys.add(name);
            }
            rs.close();
        }
        pkTable.put(fullTableName, primaryKeys);
        return primaryKeys;
    }



    /***
     * Returns the imported keys for the given schema/table.
     * @return the List of imported Keys.  Each key is a List of the form
     * <br> {@literal <foreign schema, foreign table, foreign column, primary schema, primary table, primary column>}
     */
    public List<List<String>> getImportedKeys(String schema, String table) throws SQLException {
        return getForeignKeys(schema, table, false);
    }

    /***
     * Returns the exported keys for the given schema/table.
     * @return the List of exported keys.  Each key is a List of the form
     * <br> {@literal <foreign schema, foreign table, foreign column, primary schema, primary table, primary column>}
     */
    public List<List<String>> getExportedKeys(String schema, String table) throws SQLException {
        return getForeignKeys(schema, table, true);
    }

    private List<List<String>> getForeignKeys(String schema, String table, boolean exported) throws SQLException {
        Log.getLogger().entering("InternalVSEMetaDataImpl", "getForeignKeys", new Object[]{schema, table});

        if (databaseMetaData == null) {
            initMetaData();
        }
        Hashtable lookupTable;
        if (exported) {
            lookupTable = fkExportedTable;
        } else {
            lookupTable = fkImportedTable;
        }
        String fullTableName = mergeTableName(schema, table);
        List keys = (List) lookupTable.get(fullTableName);
        if (keys != null) {
            return keys;
        }
        keys = new ArrayList();
        ResultSet rs = exported ?
	    databaseMetaData.getExportedKeys(null, schema, table) :
	    databaseMetaData.getImportedKeys(null, schema, table);
        if (rs != null) {
            while (rs.next()) {
                String fschem = rs.getString("FKTABLE_SCHEM"); // NOI18N
                String pschem = rs.getString("PKTABLE_SCHEM"); // NOI18N
                List<String> key = Arrays.asList(((fschem != null) ? fschem + "." : "") + rs.getString("FKTABLE_NAME"),
						 rs.getString("FKCOLUMN_NAME"),
						 ((pschem != null) ? pschem + "." : "") + rs.getString("PKTABLE_NAME"),
						 rs.getString("PKCOLUMN_NAME")); // NOI18N
                keys.add(key);
            }
            rs.close();
        }
        lookupTable.put(fullTableName, keys);

        return keys;
    }


    public String getIdentifierQuoteString() throws SQLException {
        if (databaseMetaData == null) {
            initMetaData();
        }
        if (identifierQuoteString != null) {
            return identifierQuoteString;
        } else {
            identifierQuoteString = databaseMetaData.getIdentifierQuoteString();
            return identifierQuoteString;
        }
    }


    //     private DatasourceConnectionListener listener = new DatasourceConnectionListener() {
//         public void dataSourceConnectionModified() {
//             logInfo( dataSourceInfo.getName() + " connectionModified event." ) ;
//             refresh() ;
//         }
//     } ;
//     /****
//      * gets the tables that have cached colmumn names
//      */
//     public String[] getCachedColmnNameTables() throws SQLException {
//         List ret = new ArrayList() ;
//         java.util.Enumeration keys = columnNameTable.keys() ;
//         while ( keys.hasMoreElements() ) {
//             ret.add( (String)columnNameTable.get(keys.nextElement())) ;
//         }
//         return (String[])ret.toArray( new String[ret.size()]) ;
//     }
//     private void loadAllColumns() throws SQLException {
//         logInfo( dataSourceInfo.getName() + " loading all columns" ) ;
//         List<List<String>> tabs = getTables() ;
//         for ( int i = 0 ; i < tabs.size() ; i++ ) {
//          String schema = tabs.get(i).get(0);
//          String table = tabs.get(i).get(1);
//          String fullTableName=
//              ((schema==null) || schema.equals("")) ?
//              table :
//              schema + "." + table;
//             getColumnNames( fullTableName ) ;
// //            getColumnNames( (String)tabs.get(i)) ;
//         }
//         allColumnsTableLoaded = true ;
//         logInfo( dataSourceInfo.getName() + " finished loading all columns" ) ;
//     }

    private void initMetaData() throws SQLException {
        databaseMetaData = getMetaData();
        refreshCacheTables();
    }

    private void refresh() {
        Log.getLogger().entering("InternalVSEMetaDataImpl", "refresh");
        databaseMetaData = null;
        refreshCacheTables();
    }

    /**
     * clears the cache held in this instance.
     */
    private void refreshCacheTables() {
        schemas = null;
        columnNameTable.clear();
        fkExportedTable.clear();
        fkImportedTable.clear();
        allTables = null;
        allColumnsTable.clear();
        pkTable.clear();
        identifierQuoteString = null;
    }


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

    /*****
     * The opposite of parseTableName -- combine a schema and table into schema.tableName,
     * allowing for null or empty schema name
     */
    private static String mergeTableName(String schema, String table) {
        return ((schema == null) || (schema.equals(""))) ?
	    table :
	    schema + "." + table;
    }

    private DatabaseMetaData getMetaData() throws SQLException {
        if (databaseMetaData == null) {            
            Connection conn = dbconn.getJDBCConnection();
            if (conn == null) {                
                String msg = NbBundle.getMessage(QueryBuilder.class, "CANNOT_ESTABLISH_CONNECTION");     // NOI18N
                NotifyDescriptor d =
                    new NotifyDescriptor.Message(msg + "\n\n", NotifyDescriptor.ERROR_MESSAGE); // NOI18N
                DialogDisplayer.getDefault().notify(d);                
            } else {
                databaseMetaData = dbconn.getJDBCConnection().getMetaData();
            }
        }
        return databaseMetaData;
    }
}
