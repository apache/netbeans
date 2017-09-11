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
public class DataViewTablePanelTest extends NbTestCase {
    
    public DataViewTablePanelTest(String testName) {
        super(testName);
    }

    public static  org.netbeans.junit.NbTest suite() {
         org.netbeans.junit.NbTestSuite suite = new  org.netbeans.junit.NbTestSuite(DataViewTablePanelTest.class);
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
     * Test of fireTableModelChange method, of class DataViewTablePanel.
     */
//    public void testFireTableModelChange() {
//        System.out.println("fireTableModelChange");
//        DataViewTablePanel instance = null;
//        instance.fireTableModelChange();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setEditable method, of class DataViewTablePanel.
//     */
//    public void testSetEditable() {
//        System.out.println("setEditable");
//        boolean edit = false;
//        DataViewTablePanel instance = null;
//        instance.setEditable(edit);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of isEditable method, of class DataViewTablePanel.
//     */
//    public void testIsEditable() {
//        System.out.println("isEditable");
//        DataViewTablePanel instance = null;
//        boolean expResult = false;
//        boolean result = instance.isEditable();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of isDirty method, of class DataViewTablePanel.
//     */
//    public void testIsDirty() {
//        System.out.println("isDirty");
//        DataViewTablePanel instance = null;
//        boolean expResult = false;
//        boolean result = instance.isDirty();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setDirty method, of class DataViewTablePanel.
//     */
//    public void testSetDirty() {
//        System.out.println("setDirty");
//        boolean dirty = false;
//        DataViewTablePanel instance = null;
//        instance.setDirty(dirty);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getDataViewTableUI method, of class DataViewTablePanel.
//     */
//    public void testGetDataViewTableUI() {
//        System.out.println("getDataViewTableUI");
//        DataViewTablePanel instance = null;
//        DataViewTableUI expResult = null;
//        DataViewTableUI result = instance.getDataViewTableUI();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getUpdatedRowContext method, of class DataViewTablePanel.
//     */
//    public void testGetUpdatedRowContext() {
//        System.out.println("getUpdatedRowContext");
//        DataViewTablePanel instance = null;
//        UpdatedRowContext expResult = null;
//        UpdatedRowContext result = instance.getUpdatedRowContext();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getDataViewDBTable method, of class DataViewTablePanel.
//     */
//    public void testGetDataViewDBTable() {
//        System.out.println("getDataViewDBTable");
//        DataViewTablePanel instance = null;
//        DataViewDBTable expResult = null;
//        DataViewDBTable result = instance.getDataViewDBTable();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of isCommitEnabled method, of class DataViewTablePanel.
//     */
//    public void testIsCommitEnabled() {
//        System.out.println("isCommitEnabled");
//        DataViewTablePanel instance = null;
//        boolean expResult = false;
//        boolean result = instance.isCommitEnabled();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of createTableModel method, of class DataViewTablePanel.
//     */
//    public void testCreateTableModel() {
//        System.out.println("createTableModel");
//        List<Object[]> rows = null;
//        DataViewTablePanel instance = null;
//        instance.createTableModel(rows);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getPageDataFromTable method, of class DataViewTablePanel.
//     */
//    public void testGetPageDataFromTable() {
//        System.out.println("getPageDataFromTable");
//        DataViewTablePanel instance = null;
//        List<Object[]> expResult = null;
//        List<Object[]> result = instance.getPageDataFromTable();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

}
