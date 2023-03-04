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
import java.util.List;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.db.dataview.util.DbUtil;
import org.netbeans.modules.db.dataview.util.TestCaseContext;
import org.openide.util.Exceptions;

/**
 *
 * @author jawed
 */
public class DBPrimaryKeyTest extends NbTestCase {
    
    DBTable table;
    private TestCaseContext context;
    private DatabaseConnection dbconn;
    private Connection conn;
    
    public DBPrimaryKeyTest(String testName) {
        super(testName);
    }

    public static  org.netbeans.junit.NbTest suite() {
         org.netbeans.junit.NbTestSuite suite = new  org.netbeans.junit.NbTestSuite(DBPrimaryKeyTest.class);
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
        dbconn = null;
    }

    protected void createTable() {
        try {
            //Quoter qt = SQLIdentifiers.createQuoter(dbmd);
            ResultSet rs = conn.createStatement().executeQuery(context.getSqlSelect());
            ResultSetMetaData rsMeta = rs.getMetaData();
            String aName = rsMeta.getTableName(1);
            String aSchema = rsMeta.getSchemaName(1);
            String aCatalog = rsMeta.getCatalogName(1);
            table = new DBTable(aName, aSchema, aCatalog);
        //table.setQuoter(quoter);
        } catch (SQLException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    private DBPrimaryKey getDBPrimaryKey() {
        try {
            ResultSet rs = conn.createStatement().executeQuery(context.getSqlSelect());
            ResultSetMetaData rsMeta = rs.getMetaData();
            ResultSet rsP = conn.getMetaData().getPrimaryKeys(rsMeta.getCatalogName(1), rsMeta.getSchemaName(1), rsMeta.getTableName(1));
            DBPrimaryKey pk = new DBPrimaryKey(rsP);
            return pk;
        } catch (SQLException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
 //---------------- Test Case ------------------
    /**
     * Test of equals method, of class DBPrimaryKey.
     */
    public void testEquals() {
        DBTable instance = table;
        DBPrimaryKey expResult = getDBPrimaryKey();
        expResult.setDisplayName("P_Key");
        instance.setPrimaryKey(expResult);
        expResult.setParentObject(instance);
        DBPrimaryKey result = instance.getPrimaryKey();
        boolean expected = true;
        boolean actual = result.equals(expResult);
        assertEquals(expected, actual);
    }

    /**
     * Test of getColumnCount method, of class DBPrimaryKey.
     */
    public void testGetColumnCount() {
        DBTable instanceTable = table;
        DBPrimaryKey expPK = getDBPrimaryKey();
        expPK.setDisplayName("P_Key");
        instanceTable.setPrimaryKey(expPK);
        int expResult = 2;
        int result = expPK.getColumnCount();
        assertEquals(expResult, result);
    }

    /**
     * Test of getColumnNames method, of class DBPrimaryKey.
     */
    public void testGetColumnNames() {
        DBPrimaryKey instance = getDBPrimaryKey();
        List<String> result = instance.getColumnNames();
        assertEquals("TINYINTC", result.get(0));
        assertEquals("SMALLINTC", result.get(1));
    }
    
    /**
     * Test of toString method, of class DBPrimaryKey.
     */
    public void testToString() {
        DBPrimaryKey instance = getDBPrimaryKey();
        String expResult = "TINYINTC,SMALLINTC";
        String result = instance.toString();
        assertEquals(expResult, result);
    }
}
