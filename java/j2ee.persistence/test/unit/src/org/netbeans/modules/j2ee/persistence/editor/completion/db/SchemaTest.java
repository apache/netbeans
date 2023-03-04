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

package org.netbeans.modules.j2ee.persistence.editor.completion.db;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.HashSet;
import junit.framework.*;
import org.netbeans.modules.db.test.jdbcstub.ConnectionImpl;
import org.netbeans.modules.db.test.jdbcstub.DatabaseMetaDataImpl;
import org.netbeans.modules.db.test.jdbcstub.JDBCStubUtil;
import org.netbeans.modules.dbschema.ColumnPairElement;
import org.netbeans.test.stub.api.Stub;
import org.netbeans.modules.dbschema.TableElement;

/**
 *
 * @author Andrei Badea
 */
public class SchemaTest extends TestCase {
    
    private Connection conn;
    private DatabaseMetaData metaData;
    
    public SchemaTest(String testName) {
        super(testName);
    }
    
    private void createConnection(String[] catalogNames, String[] schemaNames, String[][] tableNamesBySchema) {
        metaData = (DatabaseMetaData)Stub.create(DatabaseMetaData.class, new MyDatabaseMetaDataImpl());
        conn = (Connection)Stub.create(Connection.class, new ConnectionImpl(metaData));
    }
    
    public void testGetTableNames() throws SQLException {
        createConnection(new String[0], new String[] { "schema1" }, new String[][] { new String[] { "schema1", "s1table2", "s1table1" } });
        DBMetaDataProvider provider = DBMetaDataProvider.get(conn, "");
        
        String[] tableNames = provider.getCatalog(null).getSchema("schema1").getTableNames();
        assertEquals("s1table1", tableNames[0]);
        assertEquals("s1table2", tableNames[1]);
    }
    
    public void testGetTableByName() throws SQLException {
        // to display the exceptions in dbschema
        System.setProperty("netbeans.debug.exceptions", "true");
        assertTrue(Boolean.getBoolean("netbeans.debug.exceptions"));
        
        createConnection(new String[0], new String[] { "schema1" }, new String[][] { new String[] { "schema1", "s1table2", "s1table1" } });
        DBMetaDataProvider provider = DBMetaDataProvider.get(conn, "");
        
        TableElement table = provider.getCatalog(null).getSchema("schema1").getTable("s1table1");
        assertEquals(3, table.getColumns().length);
        assertEquals("S1TABLE1_INTEGER_COL", table.getPrimaryKey().getColumns()[0].getName().getName());
        ColumnPairElement[] pairs = table.getForeignKeys()[0].getColumnPairs();
        assertEquals("S1TABLE1_FK_COL", pairs[0].getLocalColumn().getName().getName());
        assertEquals("S1TABLE2_INTEGER_COL", pairs[0].getReferencedColumn().getName().getName());
    }
    
    public static final class MyDatabaseMetaDataImpl extends DatabaseMetaDataImpl {
        
        public ResultSet getCatalogs() {
            return JDBCStubUtil.catalogsResultSet(new String[0]);
        }
        
        public ResultSet getSchemas() {
            return JDBCStubUtil.schemasResultSet(new String[] { "schema1" });
        }
        
        public ResultSet getTables(String catalog, String schemaPattern, String tableNamePattern, String[] types) {
            if (catalog == null && new HashSet(Arrays.asList(types)).contains("TABLE")) {
                return JDBCStubUtil.tablesResultSet(new String[][] { { "schema1", "s1table2", "s1table1" } });
            } 
            return JDBCStubUtil.emptyResultSet();
        }
        
        public ResultSet getColumns(String catalog, String schemaPattern, String tableNamePattern, String columnNamePattern) {
            if (catalog == null) {
                if ("s1table1".equals(tableNamePattern)) {
                    return JDBCStubUtil.columnsResultSet(
                            new String[] { "S1TABLE1_INTEGER_COL", "S1TABLE1_VARCHAR_COL", "S1TABLE1_FK_COL" },
                            new String[] { "INTEGER", "VARCAHR", "INTEGER" },
                            new int[] { Types.INTEGER, Types.VARCHAR, Types.INTEGER },
                            new int[] { 0, 20, 0 },
                            new int[] { 0, 0, 0 },
                            new int[] { DatabaseMetaData.columnNoNulls, DatabaseMetaData.columnNullable, DatabaseMetaData.columnNullable }
                    );
                    
                } else if ("s1table2".equals(tableNamePattern)) {
                    return JDBCStubUtil.columnsResultSet(
                            new String[] { "S1TABLE2_INTEGER_COL" },
                            new String[] { "INTEGER" },
                            new int[] { Types.INTEGER },
                            new int[] { 0 },
                            new int[] { 0 },
                            new int[] { DatabaseMetaData.columnNoNulls }
                    );
                }
            }
            return JDBCStubUtil.emptyResultSet();
        }
        
        public ResultSet getIndexInfo(String catalog, String schema, String table, boolean unique, boolean approximate) {
            if (catalog == null) {
                if ("s1table1".equals(table)) {
                    return JDBCStubUtil.indexesResultSet(
                            new String[] { "PK_S1TABLE1_INTEGER_COL" },
                            new String[] { "S1TABLE1_INTEGER_COL" },
                            new boolean[] { false }
                    );
                    
                } else if ("s1table2".equals(table)) {
                    return JDBCStubUtil.indexesResultSet(
                            new String[] { "PK_S1TABLE2_INTEGER_COL" },
                            new String[] { "S1TABLE2_INTEGER_COL" },
                            new boolean[] { false }
                    );
                    
                }
            }
            return JDBCStubUtil.emptyResultSet();
        }
        
        public ResultSet getPrimaryKeys(String catalog, String schema, String table) {
            if (catalog == null) {
                if ("s1table1".equals(table)) {
                    return JDBCStubUtil.primaryKeysResultSet(
                            new String[] { "PK_S1TABLE1_INTEGER_COL" },
                            new String[] { "S1TABLE1_INTEGER_COL" },
                            new short[] { 0 }
                    );
                    
                } else if ("s1table2".equals(table)) {
                    return JDBCStubUtil.primaryKeysResultSet(
                            new String[] { "PK_S1TABLE2_INTEGER_COL" },
                            new String[] { "S1TABLE2_INTEGER_COL" },
                            new short[] { 0 }
                    );
                }
            }
            return JDBCStubUtil.emptyResultSet();
        }
        
        public ResultSet getImportedKeys(String catalog, String schema, String table) {
            //return JDBCStubUtil.emptyResultSet();
            if (catalog == null) {
                if ("s1table1".equals(table)) {
                    return JDBCStubUtil.importedKeysResultSet(
                            new String[] { "FK_S1TABLE1_FK_COL" },
                            new String[] { null },
                            new String[] { null },
                            new String[] { "s1table2" },
                            new String[] { "S1TABLE2_INTEGER_COL" },
                            new String[] { null },
                            new String[] { null },
                            new String[] { "s1table1" },
                            new String[] { "S1TABLE1_FK_COL" }
                    );
                }
            }
            
            return JDBCStubUtil.emptyResultSet();
        }
    }
}
