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

import org.netbeans.junit.NbTestCase;

/**
 *
 * @author jawed
 */
public class DBForeignKeyTest extends NbTestCase {
    
    public DBForeignKeyTest(String testName) {
        super(testName);
    }

    public static  org.netbeans.junit.NbTest suite() {
        org.netbeans.junit.NbTestSuite suite = new  org.netbeans.junit.NbTestSuite(DBForeignKeyTest.class);
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

    
    public void testToDo(){
        assertTrue("To Do", true);
    }
    /**
     * Test of createForeignKeyColumnMap method, of class DBForeignKey.
     */
//    public void testCreateForeignKeyColumnMap() throws Exception {
//        System.out.println("createForeignKeyColumnMap");
//        DBTable table = null;
//        ResultSet rs = null;
//        Map<String, DBForeignKey> expResult = null;
//        Map<String, DBForeignKey> result = DBForeignKey.createForeignKeyColumnMap(table, rs);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of contains method, of class DBForeignKey.
//     */
//    public void testContains() {
//        System.out.println("contains");
//        DBColumn fkCol = null;
//        DBForeignKey instance = null;
//        boolean expResult = false;
//        boolean result = instance.contains(fkCol);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of equals method, of class DBForeignKey.
//     */
//    public void testEquals() {
//        System.out.println("equals");
//        Object refObj = null;
//        DBForeignKey instance = null;
//        boolean expResult = false;
//        boolean result = instance.equals(refObj);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getColumnNames method, of class DBForeignKey.
//     */
//    public void testGetColumnNames() {
//        System.out.println("getColumnNames");
//        DBForeignKey instance = null;
//        List<String> expResult = null;
//        List<String> result = instance.getColumnNames();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getName method, of class DBForeignKey.
//     */
//    public void testGetName() {
//        System.out.println("getName");
//        DBForeignKey instance = null;
//        String expResult = "";
//        String result = instance.getName();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getPKCatalog method, of class DBForeignKey.
//     */
//    public void testGetPKCatalog() {
//        System.out.println("getPKCatalog");
//        DBForeignKey instance = null;
//        String expResult = "";
//        String result = instance.getPKCatalog();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getPKColumnNames method, of class DBForeignKey.
//     */
//    public void testGetPKColumnNames() {
//        System.out.println("getPKColumnNames");
//        DBForeignKey instance = null;
//        List<String> expResult = null;
//        List<String> result = instance.getPKColumnNames();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getPKName method, of class DBForeignKey.
//     */
//    public void testGetPKName() {
//        System.out.println("getPKName");
//        DBForeignKey instance = null;
//        String expResult = "";
//        String result = instance.getPKName();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getPKSchema method, of class DBForeignKey.
//     */
//    public void testGetPKSchema() {
//        System.out.println("getPKSchema");
//        DBForeignKey instance = null;
//        String expResult = "";
//        String result = instance.getPKSchema();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getPKTable method, of class DBForeignKey.
//     */
//    public void testGetPKTable() {
//        System.out.println("getPKTable");
//        DBForeignKey instance = null;
//        String expResult = "";
//        String result = instance.getPKTable();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of hashCode method, of class DBForeignKey.
//     */
//    public void testHashCode() {
//        System.out.println("hashCode");
//        DBForeignKey instance = null;
//        int expResult = 0;
//        int result = instance.hashCode();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of references method, of class DBForeignKey.
//     */
//    public void testReferences() {
//        System.out.println("references");
//        DBPrimaryKey pk = null;
//        DBForeignKey instance = null;
//        boolean expResult = false;
//        boolean result = instance.references(pk);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

}
