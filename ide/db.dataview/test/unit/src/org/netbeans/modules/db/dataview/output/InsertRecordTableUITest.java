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
import java.sql.Date;
import java.util.Collections;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.db.dataview.meta.DBTable;
import org.netbeans.modules.db.dataview.spi.DBConnectionProviderImpl;
import org.netbeans.modules.db.dataview.table.ResultSetTableModel;
import org.netbeans.modules.db.dataview.util.DBTestUtil;
import org.netbeans.modules.db.dataview.util.DbUtil;
import org.netbeans.modules.db.dataview.util.TestCaseContext;
import org.openide.util.Mutex;

/**
 * Test for InsertRecordTableUI
 */
public class InsertRecordTableUITest extends NbTestCase {

    Connection conn;
    DatabaseConnection dbconn;
    TestCaseContext context;

    public InsertRecordTableUITest(String testName) {
        super(testName);
    }

    @Override
    public boolean runInEQ() {
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
        final DataView dv = DataView.create(dbconn, sqlString, 5);

        Mutex.EVENT.writeAccess(new Mutex.Action<Object>() {
            @Override
            public Void run() {
                dv.createComponents();

                DataViewPageContext pageContext = dv.getPageContext(0);
                DBTable table = pageContext.getTableMetaData().getTable(0);

                InsertRecordDialog ird = new InsertRecordDialog(dv, pageContext, table);

                ResultSetTableModel model = ird.insertRecordTableUI.getModel();
                ird.insertRecordTableUI.appendEmptyRow();
                ird.insertRecordTableUI.appendEmptyRow();

                // Column 5 is the date column => Insert a "real" Date
                // => creates conflict with String inserted by "createNewRow"
                ird.insertRecordTableUI.setValueAt(
                        new Date(new java.util.Date().getTime()), 1, 5);
                try {
                    ird.insertRecordTableUI.getRowSorter().setSortKeys(Collections.singletonList(new RowSorter.SortKey(5, SortOrder.ASCENDING)));
                } catch (ClassCastException ex) {
                    assert false : "Bug 219011 - should not be reached!";
                }
                return null;
            }
        });
    }
}
