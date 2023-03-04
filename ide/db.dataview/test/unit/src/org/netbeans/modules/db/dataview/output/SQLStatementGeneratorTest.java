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
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.db.dataview.meta.DBColumn;
import org.netbeans.modules.db.dataview.meta.DBMetaDataFactory;
import org.netbeans.modules.db.dataview.meta.DBTable;
import org.netbeans.modules.db.dataview.util.DBReadWriteHelper;
import org.netbeans.modules.db.dataview.util.DBTestUtil;
import org.netbeans.modules.db.dataview.util.DbUtil;
import org.netbeans.modules.db.dataview.util.TestCaseContext;

/**
 *
 * @author jawed
 */
public class SQLStatementGeneratorTest extends NbTestCase {
    
    TestCaseContext context;
    DatabaseConnection dbconn;
    Connection conn;

    public SQLStatementGeneratorTest(String testName) {
        super(testName);
    }

    public static  org.netbeans.junit.NbTest suite() {
         org.netbeans.junit.NbTestSuite suite = new  org.netbeans.junit.NbTestSuite(SQLStatementGeneratorTest.class);
        return suite;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        DBTestUtil.suppressSuperfluousLogging();
        context = DbUtil.getContext();
        dbconn = DbUtil.getDBConnection();
        conn = DbUtil.getjdbcConnection();
        DbUtil.createTable();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        DbUtil.dropTable();
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }

    public void testGenerateWhereConditionWithPK() throws SQLException {
        DBMetaDataFactory dbMeta = new DBMetaDataFactory(conn);
        Statement s = conn.createStatement();
        String sql = "SELECT tinyintc, smallintc,varcharc FROM simpletable";

        ResultSet rs = s.executeQuery(sql);
        Collection<DBTable> tables = dbMeta.generateDBTables(rs, sql, true);
        dbMeta.postprocessTables(tables);

        DBTable table = tables.iterator().next();
        assertEquals(2, table.getPrimaryKey().getColumnCount());

        SQLStatementGenerator ssg = new SQLStatementGenerator();

        List<DBColumn> columns = table.getColumnList();

        DataViewTableUIModel model = new DataViewTableUIModel(
                columns.toArray(new DBColumn[0]));

        ResultSetMetaData rsmd = rs.getMetaData();

        while (rs.next()) {
            Object[] row = new Object[rsmd.getColumnCount()];
            for (int i = 0; i < rsmd.getColumnCount(); i++) {
                row[i] = DBReadWriteHelper.readResultSet(rs, columns.get(i),
                        i + 1);
            }
            model.addRow(row);
        }

        StringBuilder result = new StringBuilder();
        ssg.generateWhereCondition(table, result, 0, model);

        assertEquals("TINYINTC = -80 AND SMALLINTC = -32766", result.toString());
    }

    public void testGenerateWhereConditionWithoutPK() throws SQLException {
        DBMetaDataFactory dbMeta = new DBMetaDataFactory(conn);
        Statement s = conn.createStatement();
        String sql = "SELECT smallintc, varcharc FROM simpletable";

        ResultSet rs = s.executeQuery(sql);
        Collection<DBTable> tables = dbMeta.generateDBTables(rs, sql, true);
        dbMeta.postprocessTables(tables);

        DBTable table = tables.iterator().next();
        assertEquals(2, table.getPrimaryKey().getColumnCount());

        SQLStatementGenerator ssg = new SQLStatementGenerator();

        List<DBColumn> columns = table.getColumnList();

        DataViewTableUIModel model = new DataViewTableUIModel(
                columns.toArray(new DBColumn[0]));

        ResultSetMetaData rsmd = rs.getMetaData();

        while (rs.next()) {
            Object[] row = new Object[rsmd.getColumnCount()];
            for (int i = 0; i < rsmd.getColumnCount(); i++) {
                row[i] = DBReadWriteHelper.readResultSet(rs, columns.get(i), i + 1);
            }
            model.addRow(row);
        }

        StringBuilder result = new StringBuilder();
        ssg.generateWhereCondition(table, result, 0, model);

        assertEquals("SMALLINTC = -32766 AND VARCHARC = 'ala'", result.toString());
    }

    public void testGenerateWhereConditionWithPK2() throws SQLException {
        DBMetaDataFactory dbMeta = new DBMetaDataFactory(conn);
        Statement s = conn.createStatement();
        String sql = "SELECT tinyintc, smallintc,varcharc FROM simpletable";

        ResultSet rs = s.executeQuery(sql);
        Collection<DBTable> tables = dbMeta.generateDBTables(rs, sql, true);
        dbMeta.postprocessTables(tables);

        DBTable table = tables.iterator().next();
        assertEquals(2, table.getPrimaryKey().getColumnCount());

        SQLStatementGenerator ssg = new SQLStatementGenerator();

        List<DBColumn> columns = table.getColumnList();

        DataViewTableUIModel model = new DataViewTableUIModel(
                columns.toArray(new DBColumn[0]));

        ResultSetMetaData rsmd = rs.getMetaData();

        while (rs.next()) {
            Object[] row = new Object[rsmd.getColumnCount()];
            for (int i = 0; i < rsmd.getColumnCount(); i++) {
                row[i] = DBReadWriteHelper.readResultSet(rs, columns.get(i),
                        i + 1);
            }
            model.addRow(row);
        }

        StringBuilder resultSQL = new StringBuilder();
        List<Integer> resultTypes = new ArrayList<Integer>();
        List<Object> resultObject = new ArrayList<Object>();
        ssg.generateWhereCondition(table, resultSQL, resultTypes, resultObject, 0, model);

        assertEquals("\"TINYINTC\" = ?  AND \"SMALLINTC\" = ? ", resultSQL.toString());
        assertEquals(Arrays.asList(new Integer[]{Types.INTEGER, Types.INTEGER}), resultTypes);
        assertEquals(Arrays.asList(new Object[]{-80, -32766}), resultObject);
    }

    public void testGenerateWhereConditionWidthoutPK2() throws SQLException {
        DBMetaDataFactory dbMeta = new DBMetaDataFactory(conn);
        Statement s = conn.createStatement();
        String sql = "SELECT smallintc, varcharc FROM simpletable";

        ResultSet rs = s.executeQuery(sql);
        Collection<DBTable> tables = dbMeta.generateDBTables(rs, sql, true);
        dbMeta.postprocessTables(tables);

        DBTable table = tables.iterator().next();
        assertEquals(2, table.getPrimaryKey().getColumnCount());

        SQLStatementGenerator ssg = new SQLStatementGenerator();

        List<DBColumn> columns = table.getColumnList();

        DataViewTableUIModel model = new DataViewTableUIModel(
                columns.toArray(new DBColumn[0]));

        ResultSetMetaData rsmd = rs.getMetaData();

        while (rs.next()) {
            Object[] row = new Object[rsmd.getColumnCount()];
            for (int i = 0; i < rsmd.getColumnCount(); i++) {
                row[i] = DBReadWriteHelper.readResultSet(rs, columns.get(i), i + 1);
            }
            model.addRow(row);
        }

        StringBuilder resultSQL = new StringBuilder();
        List<Integer> resultTypes = new ArrayList<Integer>();
        List<Object> resultObject = new ArrayList<Object>();
        ssg.generateWhereCondition(table, resultSQL, resultTypes, resultObject, 0, model);

        assertEquals("\"SMALLINTC\" = ?  AND \"VARCHARC\" = ? ", resultSQL.toString());
        assertEquals(Arrays.asList(new Integer[]{Types.INTEGER, Types.VARCHAR}), resultTypes);
        assertEquals(Arrays.asList(new Object[]{-32766, "ala"}), resultObject);
    }

//        protected void createTable(){
//        try {
//            //Quoter qt = SQLIdentifiers.createQuoter(dbmd);
//            ResultSet rs = conn.createStatement().executeQuery(context.getSqlSelect());
//            ResultSetMetaData rsMeta = rs.getMetaData();
//            String aName = rsMeta.getTableName(1);
//            String aSchema = rsMeta.getSchemaName(1);
//            String aCatalog = rsMeta.getCatalogName(1);
//            table = new DBTable(aName, aSchema, aCatalog);
//            //table.setQuoter(quoter);
//        } catch (SQLException ex) {
//            Exceptions.printStackTrace(ex);
//        }
//    }
        
    /**
     * Test of generateInsertStatement method, of class SQLStatementGenerator.
     */
//    public void testGenerateInsertStatement() throws Exception {
//        Statement stmt = null;
//        String sqlSelect = context.getSqlSelect();
//        int pageSize = 5;
//        //ResultSet rs = conn.createStatement().executeQuery(sqlSelect);
//        DataView dataView = DataView.create(dbconn, sqlSelect, pageSize);
//        
//        String sql = dataView.getSQLString();
//        stmt = conn.createStatement();
//        ResultSet rs = stmt.executeQuery(sql);
//        DataViewDBTable tblMeta = dataView.getDataViewDBTable();
//        List<Object[]> rows = new ArrayList<Object[]>();
//        int colCnt = tblMeta.getColumnCount();
//        Object[] row = new Object[colCnt];
//        for (int i = 0; i < colCnt; i++) {
//            int type = tblMeta.getColumn(i).getJdbcType();
//            row[i] = DBReadWriteHelper.readResultSet(rs, type, i + 1);
//        }
//        rows.add(row);
//        
//        Object[] insertedRow = dataView.getDataViewPageContext().getCurrentRows().get(0);
//        SQLStatementGenerator instance = new SQLStatementGenerator(dataView);
//        String[] expResult = null;
//        String[] result = instance.generateInsertStatement(insertedRow);
//        assertEquals(expResult, result[0]);
//    }
//
//    /**
//     * Test of generateUpdateStatement method, of class SQLStatementGenerator.
//     */
//    public void testGenerateUpdateStatement() throws Exception {
//        System.out.println("generateUpdateStatement");
//        int row = 0;
//        int col = 0;
//        Object value = null;
//        List<Object> values = null;
//        List<Integer> types = null;
//        TableModel tblModel = null;
//        SQLStatementGenerator instance = null;
//        String[] expResult = null;
//        String[] result = instance.generateUpdateStatement(row, col, value, values, types, tblModel);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of generateDeleteStatement method, of class SQLStatementGenerator.
//     */
//    public void testGenerateDeleteStatement() {
//        System.out.println("generateDeleteStatement");
//        List<Integer> types = null;
//        List<Object> values = null;
//        int rowNum = 0;
//        TableModel tblModel = null;
//        SQLStatementGenerator instance = null;
//        String[] expResult = null;
//        String[] result = instance.generateDeleteStatement(types, values, rowNum, tblModel);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of generateCreateStatement method, of class SQLStatementGenerator.
//     */
//    public void testGenerateCreateStatement() throws Exception {
//        System.out.println("generateCreateStatement");
//        DBTable table = null;
//        SQLStatementGenerator instance = null;
//        String expResult = "";
//        String result = instance.generateCreateStatement(table);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getCountSQLQuery method, of class SQLStatementGenerator.
//     */
//    public void testGetCountSQLQuery() {
//        System.out.println("getCountSQLQuery");
//        String queryString = "";
//        String expResult = "";
//        String result = SQLStatementGenerator.getCountSQLQuery(queryString);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
}
