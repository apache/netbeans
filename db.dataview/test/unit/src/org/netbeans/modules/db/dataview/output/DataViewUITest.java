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
