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
 * @author navaneeth
 */
public class DataViewPageContextTest extends NbTestCase {
    
    public DataViewPageContextTest(String testName) {
        super(testName);
    }

    public static  org.netbeans.junit.NbTest suite() {
         org.netbeans.junit.NbTestSuite suite = new  org.netbeans.junit.NbTestSuite(DataViewPageContextTest.class);
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
     * Test of getPageSize method, of class DataViewPageContext.
     */
//    public void testGetPageSize() {
//        System.out.println("getPageSize");
//        DataViewPageContext instance = null;
//        int expResult = 0;
//        int result = instance.getPageSize();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getCurrentPos method, of class DataViewPageContext.
//     */
//    public void testGetCurrentPos() {
//        System.out.println("getCurrentPos");
//        DataViewPageContext instance = null;
//        int expResult = 0;
//        int result = instance.getCurrentPos();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getCurrentRows method, of class DataViewPageContext.
//     */
//    public void testGetCurrentRows() {
//        System.out.println("getCurrentRows");
//        DataViewPageContext instance = null;
//        List<Object[]> expResult = null;
//        List<Object[]> result = instance.getCurrentRows();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getTotalRows method, of class DataViewPageContext.
//     */
//    public void testGetTotalRows() {
//        System.out.println("getTotalRows");
//        DataViewPageContext instance = null;
//        int expResult = 0;
//        int result = instance.getTotalRows();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of hasRows method, of class DataViewPageContext.
//     */
//    public void testHasRows() {
//        System.out.println("hasRows");
//        DataViewPageContext instance = null;
//        boolean expResult = false;
//        boolean result = instance.hasRows();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of hasNext method, of class DataViewPageContext.
//     */
//    public void testHasNext() {
//        System.out.println("hasNext");
//        DataViewPageContext instance = null;
//        boolean expResult = false;
//        boolean result = instance.hasNext();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of hasOnePageOnly method, of class DataViewPageContext.
//     */
//    public void testHasOnePageOnly() {
//        System.out.println("hasOnePageOnly");
//        DataViewPageContext instance = null;
//        boolean expResult = false;
//        boolean result = instance.hasOnePageOnly();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of hasPrevious method, of class DataViewPageContext.
//     */
//    public void testHasPrevious() {
//        System.out.println("hasPrevious");
//        DataViewPageContext instance = null;
//        boolean expResult = false;
//        boolean result = instance.hasPrevious();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of first method, of class DataViewPageContext.
//     */
//    public void testFirst() {
//        System.out.println("first");
//        DataViewPageContext instance = null;
//        instance.first();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of previous method, of class DataViewPageContext.
//     */
//    public void testPrevious() {
//        System.out.println("previous");
//        DataViewPageContext instance = null;
//        instance.previous();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of next method, of class DataViewPageContext.
//     */
//    public void testNext() {
//        System.out.println("next");
//        DataViewPageContext instance = null;
//        instance.next();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of last method, of class DataViewPageContext.
//     */
//    public void testLast() {
//        System.out.println("last");
//        DataViewPageContext instance = null;
//        instance.last();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of isLastPage method, of class DataViewPageContext.
//     */
//    public void testIsLastPage() {
//        System.out.println("isLastPage");
//        DataViewPageContext instance = null;
//        boolean expResult = false;
//        boolean result = instance.isLastPage();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of refreshRequiredOnInsert method, of class DataViewPageContext.
//     */
//    public void testRefreshRequiredOnInsert() {
//        System.out.println("refreshRequiredOnInsert");
//        DataViewPageContext instance = null;
//        boolean expResult = false;
//        boolean result = instance.refreshRequiredOnInsert();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of hasDataRows method, of class DataViewPageContext.
//     */
//    public void testHasDataRows() {
//        System.out.println("hasDataRows");
//        DataViewPageContext instance = null;
//        boolean expResult = false;
//        boolean result = instance.hasDataRows();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of pageOf method, of class DataViewPageContext.
//     */
//    public void testPageOf() {
//        System.out.println("pageOf");
//        DataViewPageContext instance = null;
//        String expResult = "";
//        String result = instance.pageOf();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setPageSize method, of class DataViewPageContext.
//     */
//    public void testSetPageSize() {
//        System.out.println("setPageSize");
//        int pageSize = 0;
//        DataViewPageContext instance = null;
//        instance.setPageSize(pageSize);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setTotalRows method, of class DataViewPageContext.
//     */
//    public void testSetTotalRows() {
//        System.out.println("setTotalRows");
//        int totalCount = 0;
//        DataViewPageContext instance = null;
//        instance.setTotalRows(totalCount);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of decrementRowSize method, of class DataViewPageContext.
//     */
//    public void testDecrementRowSize() {
//        System.out.println("decrementRowSize");
//        int count = 0;
//        DataViewPageContext instance = null;
//        instance.decrementRowSize(count);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setCurrentRows method, of class DataViewPageContext.
//     */
//    public void testSetCurrentRows() {
//        System.out.println("setCurrentRows");
//        List<Object[]> rows = null;
//        DataViewPageContext instance = null;
//        instance.setCurrentRows(rows);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

}
