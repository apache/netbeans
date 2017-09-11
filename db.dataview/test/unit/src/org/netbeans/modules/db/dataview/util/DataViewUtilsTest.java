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
