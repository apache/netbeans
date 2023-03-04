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

package org.netbeans.modules.db.dataview.util;

import java.sql.Connection;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author jawed
 */
public class DBReadWriteHelperTest extends NbTestCase {
    Connection conn;
    public DBReadWriteHelperTest(String testName) {
        super(testName);
    }

    public static  org.netbeans.junit.NbTest suite() {
         org.netbeans.junit.NbTestSuite suite = new  org.netbeans.junit.NbTestSuite(DBReadWriteHelperTest.class);
        return suite;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
//        conn = DbUtil.getjdbcConnection();
//        DbUtil.createTable();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
//        DbUtil.dropTable();
//        conn.close();
    }

    /**
     * Test of readResultSet method, of class DBReadWriteHelper.
     */
//    public void testReadResultSet() throws Exception {
//        String sql = "INSERT INTO simpletable (tinyintc,smallintc,varcharc,intc,bigintc,datec,charc,datetime," +
//                "floatc,doublec,doubleprecisionc) VALUES (-90,-32766,'ala',123456,123,'2005-10-10','c'," +
//                "'2005-04-01 22:12:32',2.65,-217.5,4.32);";
//        conn.createStatement().executeUpdate(sql);
//        String sqlstr = "select * from simpletable";
//        ResultSet rs = conn.createStatement().executeQuery(sqlstr);
//        int colType = 12;
//        int index = 2;
//        Object expResult = null;
//        Object result = DBReadWriteHelper.readResultSet(rs, colType, index);
//        assertEquals(expResult, result);
//    }

//    /**
//     * Test of setAttributeValue method, of class DBReadWriteHelper.
//     */
//    public void testSetAttributeValue() throws Exception {
//        System.out.println("setAttributeValue");
//        PreparedStatement ps = null;
//        int index = 0;
//        int jdbcType = 0;
//        Object valueObj = null;
//        DBReadWriteHelper.setAttributeValue(ps, index, jdbcType, valueObj);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of validate method, of class DBReadWriteHelper.
//     */
//    public void testValidate() throws Exception {
//        System.out.println("validate");
//        Object valueObj = null;
//        DBColumn col = null;
//        Object expResult = null;
//        Object result = DBReadWriteHelper.validate(valueObj, col);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of isNullString method, of class DBReadWriteHelper.
//     */
//    public void testIsNullString() {
//        System.out.println("isNullString");
//        String str = "";
//        boolean expResult = false;
//        boolean result = DBReadWriteHelper.isNullString(str);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
    public void testToDo(){
        assertTrue("To Do", true);
    }
}
