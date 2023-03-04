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

package org.netbeans.modules.db.dataview.output;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.db.dataview.meta.DBException;
import org.netbeans.modules.db.dataview.spi.DBConnectionProviderImpl;
import org.netbeans.modules.db.dataview.util.DbUtil;
import org.netbeans.modules.db.dataview.util.TestCaseContext;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author jawed
 */
public class SQLStatementExecutorTest extends NbTestCase {
    private TestCaseContext context;
    private DatabaseConnection dbconn;
    private Connection conn;
    
    public SQLStatementExecutorTest(String testName) {
        super(testName);
    }

    public static  org.netbeans.junit.NbTest suite() {
         org.netbeans.junit.NbTestSuite suite = new  org.netbeans.junit.NbTestSuite(SQLStatementExecutorTest.class);
        return suite;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockServices.setServices(new DBConnectionProviderImpl().getClass());
        context = DbUtil.getContext();
        dbconn = DbUtil.getDBConnection();
        conn = DbUtil.getjdbcConnection();
        DbUtil.createTable();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        DbUtil.dropTable();
        dbconn = null;
    }

    public void testSQLExecutionHelper(){
        final String sqlStr = context.getSqlUpdate();
        DataView dv = null;
        try {
            int pageSize = 5;
            
            dv = DataView.create(dbconn, sqlStr, pageSize);
            SQLStatementExecutor executor = new SQLStatementExecutor(dv, NbBundle.getMessage(SQLExecutionHelper.class, "LBL_sql_insert"), "", true) {

                @Override
                public void finished() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public void execute() throws SQLException, DBException {
                    Connection con = DbUtil.getjdbcConnection();
                    Statement stmt = con.createStatement();
                    stmt.executeUpdate(sqlStr);
                }
            };
            executor.execute();
        } catch (SQLException | DBException ex) {
            Exceptions.printStackTrace(ex);
        }
        assertEquals(1, dv.getUpdateCount());
        assertEquals(sqlStr, dv.getSQLString());
    }
}
