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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.db.dataview.util.DbUtil;
import org.netbeans.modules.db.dataview.util.TestCaseContext;
import org.openide.util.Exceptions;

/**
 *
 * @author jawed
 */
public class DBMetaDataFactoryTest extends NbTestCase {
    
    private Connection conn = null;
    private DBMetaDataFactory dbMeta;
    private TestCaseContext context;
    
    public DBMetaDataFactoryTest(String testName) {
        super(testName);
    }

    public static  org.netbeans.junit.NbTest suite() {
         org.netbeans.junit.NbTestSuite suite = new org.netbeans.junit.NbTestSuite(DBMetaDataFactoryTest.class);
        return suite;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        context = DbUtil.getContext();
        conn = DbUtil.getjdbcConnection();
        DbUtil.createTable();
        dbMeta = new DBMetaDataFactory(conn);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        conn.createStatement().execute(context.getSqlDel());
        conn.close();
    }
    
    //-------------------Test Case ---------------------
    
    public void testConstructor(){
        try {
            DatabaseMetaData meta = conn.getMetaData();
            assertNotNull(dbMeta);
            assertEquals("jdbc:axiondb:db:build/testDB", meta.getURL());
        } catch (SQLException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * Test of getDBName method, of class DBMetaDataFactory.
     */
//    public void testGetDBName() throws Exception {
//        assertNotNull(dbMeta);
//        assertEquals("AxionDB", dbMeta.getDBName());
//    }

    /**
     * Test of getDBType method, of class DBMetaDataFactory.
     */
    public void testGetDBType() throws Exception {
        int expResult = 8;
        int result = dbMeta.getDBType();
        assertEquals(expResult, result);
    }

    /**
     * Test of getDBTypeFromURL method, of class DBMetaDataFactory.
     */
//    public void testGetDBTypeFromURL() {
//        String url = "jdbc:axiondb:db:build/testDB";
//        int expResult = 9;
//        int result = DBMetaDataFactory.getDBTypeFromURL(url);
//        assertEquals(expResult, result);
//    }

    /**
     * Test of getForeignKeys method, of class DBMetaDataFactory.
     */
//    public void testGetForeignKeys() throws Exception {
//        System.out.println("getForeignKeys");
//        DBTable table = null;
//        DBForeignKey fkey = new 
//        Map<String, DBForeignKey> expResult = null;
//        Map<String, DBForeignKey> result = instance.getForeignKeys(table);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of generateDBTables method, of class DBMetaDataFactory.
     */
//    public void testGenerateDBTables() throws Exception {
//        System.out.println("generateDBTables");
//        ResultSet rs = null;
//        DBMetaDataFactory instance = null;
//        Collection<DBTable> expResult = null;
//        Collection<DBTable> result = instance.generateDBTables(rs);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

}
