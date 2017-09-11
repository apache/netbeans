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

import java.sql.Connection;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author jawed
 */
public class DBReadWriteHelperTest extends NbTestCase {
    Connection conn;
    public DBReadWriteHelperTest(String testName) {
        super(testName);
    }

    public static  org.netbeans.junit.NbTest suite() {
         org.netbeans.junit.NbTestSuite suite = new  org.netbeans.junit.NbTestSuite(DBReadWriteHelperTest.class);
        return suite;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
//        conn = DbUtil.getjdbcConnection();
//        DbUtil.createTable();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
//        DbUtil.dropTable();
//        conn.close();
    }

    /**
     * Test of readResultSet method, of class DBReadWriteHelper.
     */
//    public void testReadResultSet() throws Exception {
//        String sql = "INSERT INTO simpletable (tinyintc,smallintc,varcharc,intc,bigintc,datec,charc,datetime," +
//                "floatc,doublec,doubleprecisionc) VALUES (-90,-32766,'ala',123456,123,'2005-10-10','c'," +
//                "'2005-04-01 22:12:32',2.65,-217.5,4.32);";
//        conn.createStatement().executeUpdate(sql);
//        String sqlstr = "select * from simpletable";
//        ResultSet rs = conn.createStatement().executeQuery(sqlstr);
//        int colType = 12;
//        int index = 2;
//        Object expResult = null;
//        Object result = DBReadWriteHelper.readResultSet(rs, colType, index);
//        assertEquals(expResult, result);
//    }

//    /**
//     * Test of setAttributeValue method, of class DBReadWriteHelper.
//     */
//    public void testSetAttributeValue() throws Exception {
//        System.out.println("setAttributeValue");
//        PreparedStatement ps = null;
//        int index = 0;
//        int jdbcType = 0;
//        Object valueObj = null;
//        DBReadWriteHelper.setAttributeValue(ps, index, jdbcType, valueObj);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of validate method, of class DBReadWriteHelper.
//     */
//    public void testValidate() throws Exception {
//        System.out.println("validate");
//        Object valueObj = null;
//        DBColumn col = null;
//        Object expResult = null;
//        Object result = DBReadWriteHelper.validate(valueObj, col);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of isNullString method, of class DBReadWriteHelper.
//     */
//    public void testIsNullString() {
//        System.out.println("isNullString");
//        String str = "";
//        boolean expResult = false;
//        boolean result = DBReadWriteHelper.isNullString(str);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
    public void testToDo(){
        assertTrue("To Do", true);
    }
}
