/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
            assertEquals("jdbc:h2:mem:testDB", meta.getURL());
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
        int expResult = 3;
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
