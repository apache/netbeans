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

package org.netbeans.modules.dbapi;

import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.api.explorer.MetaDataListener;
import org.netbeans.modules.db.explorer.DbMetaDataListener;
import org.netbeans.modules.db.test.DBTestBase;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Andrei Badea
 */
public class DbMetaDataListenerImplTest extends DBTestBase {

    public DbMetaDataListenerImplTest(String testName) {
        super(testName);
    }

    /**
     * Tests the registered listeners are invoked when the tableChanged and tablesChanged
     * methods of DbMetaDataListenerImpl are invoked.
     */
    public void testListenerFired() throws Exception {
        /*
        JDBCDriver driver = JDBCDriver.create("foo", "Foo", "org.example.Foo", new URL[0]);
        DatabaseConnection dbconn = DatabaseConnection.create(driver, "url", "user", "schema", "pwd", false);
        */
        DatabaseConnection dbconn = getDatabaseConnection(true);
        createTestTable();

        class TestListener implements MetaDataListener {

            DatabaseConnection dbconn;
            String tableName;

            public void tablesChanged(DatabaseConnection dbconn) {
                this.dbconn = dbconn;
            }

            public void tableChanged(DatabaseConnection dbconn, String tableName) {
                this.dbconn = dbconn;
                this.tableName = tableName;
            }
        }

        FileObject listenersFO = FileUtil.createFolder(FileUtil.getConfigRoot(), DbMetaDataListenerImpl.REFRESH_LISTENERS_PATH);
        FileObject listenerFO = listenersFO.createData("TestListener", "instance");
        TestListener listener = new TestListener();
        listenerFO.setAttribute("instanceCreate", listener);

        DbMetaDataListener dbListener = new DbMetaDataListenerImpl();

        assertNull(listener.dbconn);
        dbListener.tablesChanged(dbconn);
        assertSame(dbconn, listener.dbconn);

        listener.dbconn = null;
        assertNull(listener.dbconn);
        assertNull(listener.tableName);
        dbListener.tableChanged(dbconn, DBTestBase.getTestTableName());
        assertSame(dbconn, listener.dbconn);
        assertEquals(DBTestBase.getTestTableName(), listener.tableName);
    }
}
