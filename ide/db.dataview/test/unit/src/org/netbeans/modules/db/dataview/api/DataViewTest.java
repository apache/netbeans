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

package org.netbeans.modules.db.dataview.api;

import java.sql.Connection;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.db.dataview.spi.DBConnectionProviderImpl;
import org.netbeans.modules.db.dataview.util.DBTestUtil;
import org.netbeans.modules.db.dataview.util.DbUtil;
import org.netbeans.modules.db.dataview.util.TestCaseContext;

/**
 *
 * @author jawed
 */
public class DataViewTest extends NbTestCase {
    private TestCaseContext context;
    private DatabaseConnection dbconn;
    private Connection conn;
    
    public DataViewTest(String testName) {
        super(testName);
    }

    public static org.netbeans.junit.NbTest suite() {
        org.netbeans.junit.NbTestSuite suite = new org.netbeans.junit.NbTestSuite(DataViewTest.class);
        return suite;
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
        String updateStr = context.getSqlUpdate();
        int pageSize = 5;
        DataView instance = DataView.create(dbconn, updateStr, pageSize);
        int expResult = 1;
        int result = instance.getUpdateCount();
        assertEquals(expResult, result);
    }
}
