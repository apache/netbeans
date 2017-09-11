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
import java.util.List;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.db.dataview.util.DbUtil;
import org.netbeans.modules.db.dataview.util.TestCaseContext;
import org.openide.util.Exceptions;

/**
 *
 * @author jawed
 */
public class DBPrimaryKeyTest extends NbTestCase {
    
    DBTable table;
    private TestCaseContext context;
    private DatabaseConnection dbconn;
    private Connection conn;
    
    public DBPrimaryKeyTest(String testName) {
        super(testName);
    }

    public static  org.netbeans.junit.NbTest suite() {
         org.netbeans.junit.NbTestSuite suite = new  org.netbeans.junit.NbTestSuite(DBPrimaryKeyTest.class);
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
        dbconn = null;
    }

    protected void createTable() {
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
    
    private DBPrimaryKey getDBPrimaryKey() {
        try {
            ResultSet rs = conn.createStatement().executeQuery(context.getSqlSelect());
            ResultSetMetaData rsMeta = rs.getMetaData();
            ResultSet rsP = conn.getMetaData().getPrimaryKeys(rsMeta.getCatalogName(1), rsMeta.getSchemaName(1), rsMeta.getTableName(1));
            DBPrimaryKey pk = new DBPrimaryKey(rsP);
            return pk;
        } catch (SQLException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }
 //---------------- Test Case ------------------
    /**
     * Test of equals method, of class DBPrimaryKey.
     */
    public void testEquals() {
        DBTable instance = table;
        DBPrimaryKey expResult = getDBPrimaryKey();
        expResult.setDisplayName("P_Key");
        instance.setPrimaryKey(expResult);
        expResult.setParentObject(instance);
        DBPrimaryKey result = instance.getPrimaryKey();
        boolean expected = true;
        boolean actual = result.equals(expResult);
        assertEquals(expected, actual);
    }

    /**
     * Test of getColumnCount method, of class DBPrimaryKey.
     */
    public void testGetColumnCount() {
        DBTable instanceTable = table;
        DBPrimaryKey expPK = getDBPrimaryKey();
        expPK.setDisplayName("P_Key");
        instanceTable.setPrimaryKey(expPK);
        int expResult = 2;
        int result = expPK.getColumnCount();
        assertEquals(expResult, result);
    }

    /**
     * Test of getColumnNames method, of class DBPrimaryKey.
     */
    public void testGetColumnNames() {
        DBPrimaryKey instance = getDBPrimaryKey();
        List<String> result = instance.getColumnNames();
        assertEquals("TINYINTC", result.get(0));
        assertEquals("SMALLINTC", result.get(1));
    }
    
    /**
     * Test of toString method, of class DBPrimaryKey.
     */
    public void testToString() {
        DBPrimaryKey instance = getDBPrimaryKey();
        String expResult = "TINYINTC,SMALLINTC";
        String result = instance.toString();
        assertEquals(expResult, result);
    }
}
