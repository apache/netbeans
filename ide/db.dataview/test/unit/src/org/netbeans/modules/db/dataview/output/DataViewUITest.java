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
public class DataViewUITest extends NbTestCase {
    
    public DataViewUITest(String testName) {
        super(testName);
    }

    public static  org.netbeans.junit.NbTest suite() {
         org.netbeans.junit.NbTestSuite suite = new  org.netbeans.junit.NbTestSuite(DataViewUITest.class);
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
     * Test of getEditButtons method, of class DataViewUI.
     */
//    public void testGetEditButtons() {
//        System.out.println("getEditButtons");
//        DataViewUI instance = null;
//        JButton[] expResult = null;
//        JButton[] result = instance.getEditButtons();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setEditable method, of class DataViewUI.
//     */
//    public void testSetEditable() {
//        System.out.println("setEditable");
//        boolean editable = false;
//        DataViewUI instance = null;
//        instance.setEditable(editable);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of isEditable method, of class DataViewUI.
//     */
//    public void testIsEditable() {
//        System.out.println("isEditable");
//        DataViewUI instance = null;
//        boolean expResult = false;
//        boolean result = instance.isEditable();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setTotalCount method, of class DataViewUI.
//     */
//    public void testSetTotalCount() {
//        System.out.println("setTotalCount");
//        int count = 0;
//        DataViewUI instance = null;
//        instance.setTotalCount(count);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of isCommitEnabled method, of class DataViewUI.
//     */
//    public void testIsCommitEnabled() {
//        System.out.println("isCommitEnabled");
//        DataViewUI instance = null;
//        boolean expResult = false;
//        boolean result = instance.isCommitEnabled();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getDataViewTableUI method, of class DataViewUI.
//     */
//    public void testGetDataViewTableUI() {
//        System.out.println("getDataViewTableUI");
//        DataViewUI instance = null;
//        DataViewTableUI expResult = null;
//        DataViewTableUI result = instance.getDataViewTableUI();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getUpdatedRowContext method, of class DataViewUI.
//     */
//    public void testGetUpdatedRowContext() {
//        System.out.println("getUpdatedRowContext");
//        DataViewUI instance = null;
//        UpdatedRowContext expResult = null;
//        UpdatedRowContext result = instance.getUpdatedRowContext();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setCommitEnabled method, of class DataViewUI.
//     */
//    public void testSetCommitEnabled() {
//        System.out.println("setCommitEnabled");
//        boolean flag = false;
//        DataViewUI instance = null;
//        instance.setCommitEnabled(flag);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setCancelEnabled method, of class DataViewUI.
//     */
//    public void testSetCancelEnabled() {
//        System.out.println("setCancelEnabled");
//        boolean flag = false;
//        DataViewUI instance = null;
//        instance.setCancelEnabled(flag);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setDataRows method, of class DataViewUI.
//     */
//    public void testSetDataRows() {
//        System.out.println("setDataRows");
//        List<Object[]> rows = null;
//        DataViewUI instance = null;
//        instance.setDataRows(rows);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of syncPageWithTableModel method, of class DataViewUI.
//     */
//    public void testSyncPageWithTableModel() {
//        System.out.println("syncPageWithTableModel");
//        DataViewUI instance = null;
//        instance.syncPageWithTableModel();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of disableButtons method, of class DataViewUI.
//     */
//    public void testDisableButtons() {
//        System.out.println("disableButtons");
//        DataViewUI instance = null;
//        instance.disableButtons();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getPageSize method, of class DataViewUI.
//     */
//    public void testGetPageSize() {
//        System.out.println("getPageSize");
//        DataViewUI instance = null;
//        int expResult = 0;
//        int result = instance.getPageSize();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of isDirty method, of class DataViewUI.
//     */
//    public void testIsDirty() {
//        System.out.println("isDirty");
//        DataViewUI instance = null;
//        boolean expResult = false;
//        boolean result = instance.isDirty();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of resetToolbar method, of class DataViewUI.
//     */
//    public void testResetToolbar() {
//        System.out.println("resetToolbar");
//        boolean wasError = false;
//        DataViewUI instance = null;
//        instance.resetToolbar(wasError);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

}
