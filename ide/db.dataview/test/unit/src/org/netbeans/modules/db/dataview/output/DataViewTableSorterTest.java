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

import org.netbeans.junit.NbTestCase;

/**
 *
 * @author jawed
 */
public class DataViewTableSorterTest extends NbTestCase {
    
    public DataViewTableSorterTest(String testName) {
        super(testName);
    }

    public static  org.netbeans.junit.NbTest suite() {
         org.netbeans.junit.NbTestSuite suite = new  org.netbeans.junit.NbTestSuite(DataViewTableSorterTest.class);
        return suite;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testToDo() {
        assertTrue("To Do", true);
    }
    /**
     * Test of getTableModel method, of class DataViewTableSorter.
     */
//    public void testGetTableModel() {
//        System.out.println("getTableModel");
//        DataViewTableSorter instance = new DataViewTableSorter();
//        TableModel expResult = null;
//        TableModel result = instance.getTableModel();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setTableModel method, of class DataViewTableSorter.
//     */
//    public void testSetTableModel() {
//        System.out.println("setTableModel");
//        TableModel tableModel = null;
//        DataViewTableSorter instance = new DataViewTableSorter();
//        instance.setTableModel(tableModel);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getTableHeader method, of class DataViewTableSorter.
//     */
//    public void testGetTableHeader() {
//        System.out.println("getTableHeader");
//        DataViewTableSorter instance = new DataViewTableSorter();
//        JTableHeader expResult = null;
//        JTableHeader result = instance.getTableHeader();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setTableHeader method, of class DataViewTableSorter.
//     */
//    public void testSetTableHeader() {
//        System.out.println("setTableHeader");
//        JTableHeader tableHeader = null;
//        DataViewTableSorter instance = new DataViewTableSorter();
//        instance.setTableHeader(tableHeader);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of isSorting method, of class DataViewTableSorter.
//     */
//    public void testIsSorting() {
//        System.out.println("isSorting");
//        DataViewTableSorter instance = new DataViewTableSorter();
//        boolean expResult = false;
//        boolean result = instance.isSorting();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getSortingStatus method, of class DataViewTableSorter.
//     */
//    public void testGetSortingStatus() {
//        System.out.println("getSortingStatus");
//        int column = 0;
//        DataViewTableSorter instance = new DataViewTableSorter();
//        int expResult = 0;
//        int result = instance.getSortingStatus(column);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setSortingStatus method, of class DataViewTableSorter.
//     */
//    public void testSetSortingStatus() {
//        System.out.println("setSortingStatus");
//        int column = 0;
//        int status = 0;
//        DataViewTableSorter instance = new DataViewTableSorter();
//        instance.setSortingStatus(column, status);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getHeaderRendererIcon method, of class DataViewTableSorter.
//     */
//    public void testGetHeaderRendererIcon() {
//        System.out.println("getHeaderRendererIcon");
//        int column = 0;
//        int size = 0;
//        DataViewTableSorter instance = new DataViewTableSorter();
//        Icon expResult = null;
//        Icon result = instance.getHeaderRendererIcon(column, size);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setColumnComparator method, of class DataViewTableSorter.
//     */
//    public void testSetColumnComparator() {
//        System.out.println("setColumnComparator");
//        Class type = null;
//        Comparator<Object> comparator = null;
//        DataViewTableSorter instance = new DataViewTableSorter();
//        instance.setColumnComparator(type, comparator);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getComparator method, of class DataViewTableSorter.
//     */
//    public void testGetComparator() {
//        System.out.println("getComparator");
//        int column = 0;
//        DataViewTableSorter instance = new DataViewTableSorter();
//        Comparator expResult = null;
//        Comparator result = instance.getComparator(column);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of modelIndex method, of class DataViewTableSorter.
//     */
//    public void testModelIndex() {
//        System.out.println("modelIndex");
//        int viewIndex = 0;
//        DataViewTableSorter instance = new DataViewTableSorter();
//        int expResult = 0;
//        int result = instance.modelIndex(viewIndex);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getRowCount method, of class DataViewTableSorter.
//     */
//    public void testGetRowCount() {
//        System.out.println("getRowCount");
//        DataViewTableSorter instance = new DataViewTableSorter();
//        int expResult = 0;
//        int result = instance.getRowCount();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getColumnCount method, of class DataViewTableSorter.
//     */
//    public void testGetColumnCount() {
//        System.out.println("getColumnCount");
//        DataViewTableSorter instance = new DataViewTableSorter();
//        int expResult = 0;
//        int result = instance.getColumnCount();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getColumnName method, of class DataViewTableSorter.
//     */
//    public void testGetColumnName() {
//        System.out.println("getColumnName");
//        int column = 0;
//        DataViewTableSorter instance = new DataViewTableSorter();
//        String expResult = "";
//        String result = instance.getColumnName(column);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getColumnClass method, of class DataViewTableSorter.
//     */
//    public void testGetColumnClass() {
//        System.out.println("getColumnClass");
//        int column = 0;
//        DataViewTableSorter instance = new DataViewTableSorter();
//        Class expResult = null;
//        Class result = instance.getColumnClass(column);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of isCellEditable method, of class DataViewTableSorter.
//     */
//    public void testIsCellEditable() {
//        System.out.println("isCellEditable");
//        int row = 0;
//        int column = 0;
//        DataViewTableSorter instance = new DataViewTableSorter();
//        boolean expResult = false;
//        boolean result = instance.isCellEditable(row, column);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getValueAt method, of class DataViewTableSorter.
//     */
//    public void testGetValueAt() {
//        System.out.println("getValueAt");
//        int row = 0;
//        int column = 0;
//        DataViewTableSorter instance = new DataViewTableSorter();
//        Object expResult = null;
//        Object result = instance.getValueAt(row, column);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setValueAt method, of class DataViewTableSorter.
//     */
//    public void testSetValueAt() {
//        System.out.println("setValueAt");
//        Object aValue = null;
//        int row = 0;
//        int column = 0;
//        DataViewTableSorter instance = new DataViewTableSorter();
//        instance.setValueAt(aValue, row, column);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

}
