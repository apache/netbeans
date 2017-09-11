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
