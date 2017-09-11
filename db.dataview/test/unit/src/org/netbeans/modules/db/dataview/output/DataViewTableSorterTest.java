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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
