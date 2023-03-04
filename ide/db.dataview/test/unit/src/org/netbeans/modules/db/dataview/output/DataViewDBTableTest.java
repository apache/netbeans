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

package org.netbeans.modules.db.dataview.output;

import java.sql.Connection;
import java.util.Collection;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.db.dataview.meta.DBTable;
import org.netbeans.modules.db.dataview.spi.DBConnectionProviderImpl;
import org.netbeans.modules.db.dataview.util.DBTestUtil;
import org.netbeans.modules.db.dataview.util.DbUtil;
import org.netbeans.modules.db.dataview.util.TestCaseContext;

/**
 *
 * @author jawed
 */
public class DataViewDBTableTest extends NbTestCase {
    
    private Collection<DBTable> tables;
    private TestCaseContext context;
    private DatabaseConnection dbconn;
    private Connection conn;
    
    public DataViewDBTableTest(String testName) {
        super(testName);
    }

    public static  org.netbeans.junit.NbTest suite() {
         org.netbeans.junit.NbTestSuite suite = new  org.netbeans.junit.NbTestSuite(DataViewDBTableTest.class);
        return suite;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        DBTestUtil.suppressSuperfluousLogging();
        MockServices.setServices(new DBConnectionProviderImpl().getClass());
        context = DbUtil.getContext();
        dbconn = DbUtil.getDBConnection();
        conn = DbUtil.getjdbcConnection();
        DbUtil.createTable();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        conn.createStatement().execute(context.getSqlDel());
        conn.close();
        dbconn=null;
    }

    /**
     * Test of geTable method, of class DataViewDBTable.
     */
    public void testDataViewDBTableMethods() {
        int index = 5;
        String sqlStr = context.getSqlSelect();
        DataView dv = DataView.create(dbconn, sqlStr, index);
        DataViewDBTable instance = dv.getPageContext(0).getTableMetaData();
        DBTable result = instance.getTable(0);
        assertNotNull(result);
        assertEquals(11, instance.getColumnCount());
    }

    /**
     * Test of geTableCount method, of class DataViewDBTable.
     */
    public void testGeTableCount() {
        int index = 5;
        String sqlStr = context.getSqlSelect();
        DataView dv = DataView.create(dbconn, sqlStr, index);
        DataViewDBTable instance = dv.getPageContext(0).getTableMetaData();
        int expResult = 1;
        int result = instance.getTableCount();
        assertEquals(expResult, result);
    }

    /**
     * Test of hasOneTable method, of class DataViewDBTable.
     */
    public void testHasOneTable() {
        int index = 5;
        String sqlStr = context.getSqlSelect();
        DataView dv = DataView.create(dbconn, sqlStr, index);
        DataViewDBTable instance = dv.getPageContext(0).getTableMetaData();
        boolean expResult = true;
        boolean result = instance.hasOneTable();
        assertEquals(expResult, result);
    }

    /**
     * Test of getFullyQualifiedName method, of class DataViewDBTable.
     */
    public void testGetFullyQualifiedName() {
        int index = 5;
        String sqlStr = context.getSqlSelect();
        DataView dv = DataView.create(dbconn, sqlStr, index);
        DataViewDBTable instance = dv.getPageContext(0).getTableMetaData();
        String expResult = "TESTDB.\"PUBLIC\".SIMPLETABLE";
        String result = instance.getFullyQualifiedName(0, false);
        assertEquals(expResult, result);
    }

    /**
     * Test of getColumnType method, of class DataViewDBTable.
     */
    public void testGetColumnType() {
        int index = 5;
        String sqlStr = context.getSqlSelect();
        DataView dv = DataView.create(dbconn, sqlStr, index);
        DataViewDBTable instance = dv.getPageContext(0).getTableMetaData();
        int expResult = 12;
        int result = instance.getColumnType(2);
        assertEquals(expResult, result);
    }

    /**
     * Test of getColumnName method, of class DataViewDBTable.
     */
    public void testGetColumnName() {
        int index = 5;
        String sqlStr = context.getSqlSelect();
        DataView dv = DataView.create(dbconn, sqlStr, index);
        DataViewDBTable instance = dv.getPageContext(0).getTableMetaData();
        String expResult = "DATEC";
        String result = instance.getColumnName(index);
        assertEquals(expResult, result);
    }

    /**
     * Test of getQualifiedName method, of class DataViewDBTable.
     */
    public void testGetQualifiedName() {
        int index = 5;
        String sqlStr = context.getSqlSelect();
        DataView dv = DataView.create(dbconn, sqlStr, index);
        DataViewDBTable instance = dv.getPageContext(0).getTableMetaData();
        String expResult = "TINYINTC";
        String result = instance.getQualifiedName(0, false);
        assertEquals(expResult, result);
    }

    /**
     * Test of getColumnCount method, of class DataViewDBTable.
     */
    public void testGetColumnCount() {
        int index = 5;
        String sqlStr = context.getSqlSelect();
        DataView dv = DataView.create(dbconn, sqlStr, index);
        DataViewDBTable instance =dv.getPageContext(0).getTableMetaData();
        int expResult = 11;
        int result = instance.getColumnCount();
        assertEquals(expResult, result);
    }

    /**
     * Test of getColumnToolTips method, of class DataViewDBTable.
     */
/*    public void testGetColumnToolTips() {
        int index = 5;
        String sqlStr = context.getSqlSelect();
        DataView dv = DataView.create(dbconn, sqlStr, index);
        DataViewDBTable instance = dv.getDataViewDBTable();
        String expResult ="<html> <table border=0 cellspacing=0 cellpadding=0 ><tr> <td>&nbsp;Name</td> " +
                "<td> &nbsp; : &nbsp; <b>TINYINTC</b> </td> </tr><tr> <td>&nbsp;Type</td> <td> &nbsp; : &nbsp; " +
                "<b>INTEGER</b> </td> </tr><tr> <td>&nbsp;Precision</td> <td> &nbsp; : &nbsp; <b>10</b> </td>" +
                " </tr><tr> <td>&nbsp;PK</td> <td> &nbsp; : &nbsp; <b> Yes </b> </td> </tr></table> </html>";
        String[] result = instance.getColumnToolTips();
        assertEquals(expResult, result[0]);
    }*/

}
