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

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.db.dataview.meta.DBColumn;
import org.netbeans.modules.db.dataview.meta.DBTable;

/**
 *
 * @author jawed
 */
public class DataViewUtilsTest extends NbTestCase {
    
    private DBTable table;
    
    public DataViewUtilsTest(String testName) {
        super(testName);
    }

    public static  org.netbeans.junit.NbTest suite() {
         org.netbeans.junit.NbTestSuite suite = new  org.netbeans.junit.NbTestSuite(DataViewUtilsTest.class);
        return suite;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        createTable();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of isNumeric method, of class DataViewUtils.
     */
    public void testIsNumeric() {
        int jdbcType = 16;
        boolean expResult = true;
        boolean result = DataViewUtils.isNumeric(jdbcType);
        assertEquals(expResult, result);
    }

    /**
     * Test of isPrecisionRequired method, of class DataViewUtils.
     */
    public void testIsPrecisionRequired() {
        int jdbcType = 4;
        boolean isdb2 = false;
        boolean result = DataViewUtils.isPrecisionRequired(jdbcType, isdb2);
        assertFalse(result);
        jdbcType = 2005;
        isdb2 = true;
        result = DataViewUtils.isPrecisionRequired(jdbcType, isdb2);
        assertTrue(result);
    }

    /**
     * Test of isScaleRequired method, of class DataViewUtils.
     */
    public void testIsScaleRequired() {
        int type = 2;
        boolean result = DataViewUtils.isScaleRequired(type);
        assertTrue(result);
        type = 12;
        result = DataViewUtils.isScaleRequired(type);
        assertFalse(result);
    }

    /**
     * Test of isBinary method, of class DataViewUtils.
     */
    public void testIsBinary() {
        int jdbcType = -3;
        boolean result = DataViewUtils.isBinary(jdbcType);
        assertTrue(result);
        jdbcType = 12;
        result = DataViewUtils.isBinary(jdbcType);
        assertFalse(result);
    }

    /**
     * Test of getColumnToolTip method, of class DataViewUtils.
     */
/*    public void testGetColumnToolTip() {
        DBColumn column = new DBColumn(table, "colName", 12, "varchar", 10, 5, true, false);
        String expResult = "<html> <table border=0 cellspacing=0 cellpadding=0 ><tr> <td>&nbsp;Name</td> " +
                "<td> &nbsp; : &nbsp; <b>colName</b> </td> </tr><tr> <td>&nbsp;Type</td> " +
                "<td> &nbsp; : &nbsp; <b>VARCHAR</b> </td> </tr><tr> <td>&nbsp;Length</td> " +
                "<td> &nbsp; : &nbsp; <b>5</b> </td> </tr></table> </html>";
        String result = DataViewUtils.getColumnToolTip(column);
        assertEquals(expResult, result);
        expResult = "<html> <table border=0 cellspacing=0 cellpadding=0 ><tr> <td>&nbsp;Name</td> " +
                "<td> &nbsp; : &nbsp; <b>colNameF</b> </td> </tr><tr> <td>&nbsp;Type</td> <td> &nbsp; : &nbsp; " +
                "<b>FLOAT</b> </td> </tr><tr> <td>&nbsp;Precision</td> <td> &nbsp; : &nbsp; <b>5</b> </td> </tr>" +
                "<tr> <td>&nbsp;Scale</td> <td> &nbsp; : &nbsp; <b>10</b> </td> </tr></table> </html>";
        column = new DBColumn(table, "colNameF", 6, "float" ,10, 5, true, false);
        result = DataViewUtils.getColumnToolTip(column);
        assertEquals(expResult, result);
    }*/

    /**
     * Test of getForeignKeyString method, of class DataViewUtils.
     */
//    public void testGetForeignKeyString() {
//        System.out.println("getForeignKeyString");
//        DBColumn column = null;
//        String expResult = "";
//        String result = DataViewUtils.getForeignKeyString(column);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    public void createTable() {
        String aName = "aName";
        String aSchema = "aSchema";
        String aCatalog = "aCatalog";
        table = new DBTable(aName, aSchema, aCatalog);
    }
}
