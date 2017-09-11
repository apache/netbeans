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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.db.dataview.output;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.db.dataview.meta.DBMetaDataFactory;
import org.netbeans.modules.db.dataview.meta.DBTable;
import org.netbeans.modules.db.dataview.spi.DBConnectionProviderImpl;
import org.netbeans.modules.db.dataview.util.DBTestUtil;
import org.netbeans.modules.db.dataview.util.DbUtil;
import org.netbeans.modules.db.dataview.util.TestCaseContext;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;

/**
 *
 * @author jawed
 */
public class DataViewTest extends NbTestCase {
    
    Connection conn;
    DatabaseConnection dbconn;
    TestCaseContext context;
    
    public DataViewTest(String testName) {
        super(testName);
    }

    public static  org.netbeans.junit.NbTest suite() {
         org.netbeans.junit.NbTestSuite suite = new  org.netbeans.junit.NbTestSuite(DataViewTest.class);
        return suite;
    }

    @Override
    public boolean runInEQ () {
        return false;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        DBTestUtil.suppressSuperfluousLogging();
        MockServices.setServices(new DBConnectionProviderImpl().getClass());
        context = DbUtil.getContext();
        dbconn = DbUtil.getDBConnection();
        conn = DbUtil.getjdbcConnection();
        DbUtil.createTable();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        conn.createStatement().execute(context.getSqlDel());
        conn.close();
        dbconn = null;
    }
    
    //--------------------- Test Case ---------------------

    public void testCreate() {
        String sqlString = context.getSqlSelect();
        int pageSize = 5;
        DataView result = DataView.create(dbconn, sqlString, pageSize);
        assertNotNull(result);
    }

    public void testHasExceptions() {
        String sqlString = context.getSqlSelect();
        int pageSize = 5;
        DataView instance = DataView.create(dbconn, sqlString, pageSize);
        boolean expResult = false;
        boolean result = instance.hasExceptions();
        assertEquals(expResult, result);
    }

    public void testHasResultSet() {
        String sqlString = context.getSqlSelect();
        int pageSize = 5;
        DataView instance = DataView.create(dbconn, sqlString, pageSize);
        boolean expResult = true;
        boolean result = instance.hasResultSet();
        assertEquals(expResult, result);
    }

    public void testGetUpdateCount() {
        String updateStr = "update simpletable set tinyintc='-40' where tinyintc='-80'";
        int pageSize = 5;
        DataView instance = DataView.create(dbconn, updateStr, pageSize);
        int expResult = 1;
        int result = instance.getUpdateCount();
        assertEquals(expResult, result);
    }

//    public void testGetExecutionTime() {
//        System.out.println("getExecutionTime");
//        String sqlStr = "select count(*) from simpletable";
//        int pageSize = 4;
//        DataView instance = DataView.create(dbconn, sqlStr, pageSize);
//        long expResult = (long)0.032;
//        long result = instance.getExecutionTime();
//        assertEquals(expResult, result);
//    }

    public void testGetDataViewDBTable() {
        try {
            String sqlStr = context.getSqlSelect();
            int pageSize = 4;
            DataView instance = DataView.create(dbconn, sqlStr, pageSize);
            java.sql.Statement stmt = conn.createStatement();
            DBMetaDataFactory dbMeta = new DBMetaDataFactory(conn);
            ResultSet rset = stmt.executeQuery(sqlStr);
            Collection<DBTable> tables = dbMeta.generateDBTables(rset, sqlStr, true); //generateDBTables(rset);
            DataViewDBTable expResult = new DataViewDBTable(tables);
            DataViewPageContext pageContext = instance.getPageContext(0);
            DataViewDBTable result = pageContext.getTableMetaData();
            assertEquals(expResult.getQualifiedName(0, false), result.getQualifiedName(0, false));
            assertEquals(expResult.getColumnCount(), result.getColumnCount());
            assertEquals(expResult.getColumnType(2), result.getColumnType(2));
            rset.close();
        } catch (SQLException ex) {
            Exceptions.printStackTrace(ex);
        }
        }

    public void testGetDataViewPageContext() {
        String sqlStr =context.getSqlSelect();
        int pageSize = 4;
        DataView instance = DataView.create(dbconn, sqlStr, pageSize);
        final DataViewPageContext result = instance.getPageContext(0);
        assertTrue(Mutex.EVENT.writeAccess(new Mutex.Action<Boolean>() {
            @Override
            public Boolean run() {
                return result.hasRows();
            }
        }));
    }

    public void testGetDatabaseConnection() {
        String sqlStr = context.getSqlSelect();
        int pagSize = 4;
        DataView instance = DataView.create(dbconn, sqlStr, pagSize);
        DatabaseConnection expResult = dbconn;
        DatabaseConnection result = instance.getDatabaseConnection();
        assertNotNull(result);
        assertEquals(expResult, result);
    }

    public void testGetSQLString() {
        String sqlStr = context.getSqlSelect();
        int pagSize = 4;
        DataView instance = DataView.create(dbconn, sqlStr, pagSize);
        String expResult = "select * from simpletable";
        String result = instance.getSQLString();
        assertEquals(expResult, result);
    }

    /**
     * Test of getSQLExecutionHelper method, of class DataView.
     */
    public void testGetSQLExecutionHelper() {
        String selectStr = "select * from simpletable";
        int pageSize = 5;
        DataView instance = DataView.create(dbconn, selectStr, pageSize);
        SQLExecutionHelper result = instance.getSQLExecutionHelper();
        assertFalse(instance.hasExceptions());
        assertNotNull(instance);
        assertNotNull(result);
    }
}
