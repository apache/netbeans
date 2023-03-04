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
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.db.sql.support.SQLIdentifiers;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.db.dataview.util.DbUtil;
import org.netbeans.modules.db.dataview.util.TestCaseContext;
import org.openide.util.Exceptions;

/**
 *
 * @author jawed
 */
public class DBTableTest extends NbTestCase {
    
    private DBTable table;
    private TestCaseContext context;
    private DatabaseConnection dbconn;
    private Connection conn;
    
    public DBTableTest(String testName) {
        super(testName);
    }

    public static  org.netbeans.junit.NbTest suite() {
         org.netbeans.junit.NbTestSuite suite = new  org.netbeans.junit.NbTestSuite(DBTableTest.class);
        return suite;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        context = DbUtil.getContext();
        dbconn = DbUtil.getDBConnection();
        conn = DbUtil.getjdbcConnection();
        DbUtil.createTable();
        createTable();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        conn.createStatement().execute(context.getSqlDel());
        conn.close();
        dbconn=null;
    }

    protected void createTable(){
        try {
            //Quoter qt = SQLIdentifiers.createQuoter(dbmd);
            ResultSet rs = conn.createStatement().executeQuery(context.getSqlSelect());
            ResultSetMetaData rsMeta = rs.getMetaData();
            String aName = rsMeta.getTableName(1);
            String aSchema = rsMeta.getSchemaName(1);
            String aCatalog = rsMeta.getCatalogName(1);
            table = new DBTable(aName, aSchema, aCatalog);
            table.setQuoter(SQLIdentifiers.createQuoter(conn.getMetaData()));
            //table.setQuoter(quoter);
        } catch (SQLException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    //------------------- Test Case --------------------
    /**
     * Test of addColumn method, of class DBTable.
     */
    public void testAddColumn() throws Exception {
        DBColumn theColumn = new DBColumn(table, "colName",12, "varchar", 10, 5, true, false);
        DBTable instance = table;
        boolean expResult = true;
        boolean result = instance.addColumn(theColumn);
        assertEquals(expResult, result);
    }

    /**
     * Test of getCatalog method, of class DBTable.
     */
    public void testGetCatalog() {
        String expResult = "TESTDB";
        String result = table.getCatalog();
        assertEquals(expResult, result);
    }

    /**
     * Test of getDisplayName method, of class DBTable.
     */
    public void testGetDisplayName() {
        String expResult = "TESTDB.\"PUBLIC\".SIMPLETABLE";
        String result = table.getDisplayName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getForeignKeys method, of class DBTable.
     */
//    public void testGetForeignKeys() {
//        System.out.println("getForeignKeys");
//        DBTable instance = null;
//        List<DBForeignKey> expResult = null;
//        List<DBForeignKey> result = instance.getForeignKeys();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of getFullyQualifiedName method, of class DBTable.
     */
    public void testGetFullyQualifiedName() {
        DBTable instance = table;
        String expResult = "SIMPLETABLE";
        String result = instance.getQualifiedName(false);
        assertEquals(expResult, result);
    }

    /**
     * Test of getQualifiedName method, of class DBTable.
     */
    public void testGetQualifiedName() {
        DBTable instance = table;
        String expResult = "SIMPLETABLE";
        String result = instance.getQualifiedName(false);
        assertEquals(expResult, result);
    }

    /**
     * Test of getName method, of class DBTable.
     */
    public void testGetName() {
        DBTable instance = table;
        String expResult = "SIMPLETABLE";
        String result = instance.getName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getPrimaryKey method, of class DBTable.
     */
    public void testGetPrimaryKey() {
        try {
            DBTable instance = table;
            ResultSet rs = conn.createStatement().executeQuery(context.getSqlSelect());
            ResultSetMetaData rsMeta = rs.getMetaData();
            ResultSet rsP = conn.getMetaData().getPrimaryKeys(rsMeta.getCatalogName(1), rsMeta.getSchemaName(1),rsMeta.getTableName(1));
            DBPrimaryKey expResult = new DBPrimaryKey(rsP);
            expResult.setDisplayName("P_Key");
            instance.setPrimaryKey(expResult);
            expResult.setParentObject(instance);
            DBPrimaryKey result = instance.getPrimaryKey();
            assertEquals(expResult.getDisplayName(), result.getDisplayName());
        } catch (SQLException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
