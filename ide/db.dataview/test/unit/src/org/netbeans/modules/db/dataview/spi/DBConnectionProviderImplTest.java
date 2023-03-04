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

package org.netbeans.modules.db.dataview.spi;

import java.sql.Connection;
import java.sql.SQLException;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.db.dataview.util.DbUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author jawed
 */
public class DBConnectionProviderImplTest extends NbTestCase {
    
    DatabaseConnection dbconn;
    public DBConnectionProviderImplTest(String testName) {
        super(testName);
    }

    public static  org.netbeans.junit.NbTest suite() {
         org.netbeans.junit.NbTestSuite suite = new  org.netbeans.junit.NbTestSuite(DBConnectionProviderImplTest.class);
        return suite;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        dbconn = DbUtil.getDBConnection();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        dbconn = null;
    }
    
    /**
     * Test of getConnection method, of class DBConnectionProvider.
     */
    public void testGetConnection() {
        try {
            DBConnectionProviderImpl instance = new DBConnectionProviderImpl();
            Connection result = instance.getConnection(dbconn);
            assertNotNull(result);
            assertEquals("H2", result.getMetaData().getDatabaseProductName());
            result.close();
        } catch (SQLException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    /**
     * Test of closeConnection method, of class DBConnectionProvider.
     */
    public void testCloseConnection() {
        try {
            DBConnectionProviderImpl instance = new DBConnectionProviderImpl();
            Connection result = instance.getConnection(dbconn);
            instance.closeConnection(result);
            assertTrue(result.isClosed());
        } catch (SQLException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

}
