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
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.db.dataview.util.DbUtil;
import org.netbeans.modules.db.dataview.util.TestCaseContext;
import org.openide.util.Exceptions;

/**
 *
 * @author jawed
 */
public class DBTableTest extends NbTestCase {
    
    DBTable table;
    private TestCaseContext context;
    private DatabaseConnection dbconn;
    private Connection conn;
    
    public DBTableTest(String testName) {
        super(testName);
    }

    public static  org.netbeans.junit.NbTest suite() {
         org.netbeans.junit.NbTestSuite suite = new  org.netbeans.junit.NbTestSuite(DBTableTest.class);
        return suite;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        context = DbUtil.getContext();
        dbconn = DbUtil.getDBConnection();
        conn = DbUtil.getjdbcConnection();
        DbUtil.createTable();
        createTable();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        conn.createStatement().execute(context.getSqlDel());
        conn.close();
        dbconn=null;
    }

    protected void createTable(){
        try {
            //Quoter qt = SQLIdentifiers.createQuoter(dbmd);
            ResultSet rs = conn.createStatement().executeQuery(context.getSqlSelect());
            ResultSetMetaData rsMeta = rs.getMetaData();
            String aName = rsMeta.getTableName(1);
            String aSchema = rsMeta.getSchemaName(1);
            String aCatalog = rsMeta.getCatalogName(1);
            table = new DBTable(aName, aSchema, aCatalog);
            //table.setQuoter(quoter);
        } catch (SQLException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    //------------------- Test Case --------------------
    /**
     * Test of addColumn method, of class DBTable.
     */
    public void testAddColumn() throws Exception {
        DBColumn theColumn = new DBColumn(table, "colName",12, "varchar", 10, 5, true, false);
        DBTable instance = table;
        boolean expResult = true;
        boolean result = instance.addColumn(theColumn);
        assertEquals(expResult, result);
    }

    /**
     * Test of getCatalog method, of class DBTable.
     */
    public void testGetCatalog() {
        String expResult = "";
        String result = table.getCatalog();
        assertEquals(expResult, result);
    }

    /**
     * Test of getDisplayName method, of class DBTable.
     */
    public void testGetDisplayName() {
        String expResult = "SIMPLETABLE";
        String result = table.getDisplayName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getForeignKeys method, of class DBTable.
     */
//    public void testGetForeignKeys() {
//        System.out.println("getForeignKeys");
//        DBTable instance = null;
//        List<DBForeignKey> expResult = null;
//        List<DBForeignKey> result = instance.getForeignKeys();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }

    /**
     * Test of getFullyQualifiedName method, of class DBTable.
     */
    public void testGetFullyQualifiedName() {
        DBTable instance = table;
        String expResult = "SIMPLETABLE";
        String result = instance.getQualifiedName(false);
        assertEquals(expResult, result);
    }

    /**
     * Test of getQualifiedName method, of class DBTable.
     */
    public void testGetQualifiedName() {
        DBTable instance = table;
        String expResult = "SIMPLETABLE";
        String result = instance.getQualifiedName(false);
        assertEquals(expResult, result);
    }

    /**
     * Test of getName method, of class DBTable.
     */
    public void testGetName() {
        DBTable instance = table;
        String expResult = "SIMPLETABLE";
        String result = instance.getName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getPrimaryKey method, of class DBTable.
     */
    public void testGetPrimaryKey() {
        try {
            DBTable instance = table;
            ResultSet rs = conn.createStatement().executeQuery(context.getSqlSelect());
            ResultSetMetaData rsMeta = rs.getMetaData();
            ResultSet rsP = conn.getMetaData().getPrimaryKeys(rsMeta.getCatalogName(1), rsMeta.getSchemaName(1),rsMeta.getTableName(1));
            DBPrimaryKey expResult = new DBPrimaryKey(rsP);
            expResult.setDisplayName("P_Key");
            instance.setPrimaryKey(expResult);
            expResult.setParentObject(instance);
            DBPrimaryKey result = instance.getPrimaryKey();
            assertEquals(expResult.getDisplayName(), result.getDisplayName());
        } catch (SQLException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
