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

package org.netbeans.modules.db.explorer;

import java.lang.ref.WeakReference;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.modules.db.test.TestBase;
import org.netbeans.modules.db.test.Util;
import org.openide.loaders.DataObject;

/**
 *
 * @author Andrei Badea
 */
public class ConnectionListTest extends TestBase {

    public ConnectionListTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        Util.suppressSuperfluousLogging();
        super.setUp();
    }
    
    /**
     * Tests that ConnectionManager manages the same instance that was
     * added using the {@link ConnectionManager#addConnection} method.
     */
    public void testSameInstanceAfterAdd() throws Exception {
        Util.clearConnections();
        assertEquals(0, ConnectionList.getDefault().getConnections().length);

        DatabaseConnection dbconn = new DatabaseConnection("org.bar.BarDriver",
                "bar_driver", "jdbc:bar:localhost", "schema", "user", "password", true);
        // We are testing ConnectionList.addConnection(), but that doesn't return a DataObject.
        DataObject dbconnDO = DatabaseConnectionConvertor.create(dbconn);

        WeakReference<DataObject> dbconnDORef = new WeakReference<DataObject>(dbconnDO);
        dbconnDO = null;
        for (int i = 0; i < 50; i++) {
            System.gc();
            if (dbconnDORef.get() == null) {
                break;
            }
        }

        assertEquals(1, ConnectionList.getDefault().getConnections().length);

        // This used to fail as described in issue 75204.
        assertSame(dbconn, ConnectionList.getDefault().getConnections()[0]);

        Util.clearConnections();
        WeakReference<DatabaseConnection> dbconnRef = new WeakReference<DatabaseConnection>(dbconn);
        dbconn = null;
        assertGC("Should be able to GC dbconn", dbconnRef);
    }
}
