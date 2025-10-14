/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.api.db.explorer.support;

import javax.swing.JComboBox;
import org.netbeans.api.db.explorer.*;
import org.netbeans.modules.db.test.TestBase;
import org.netbeans.modules.db.test.Util;

/**
 *
 * @author Libor Kotouc, Andrei Badea
 */
public class DatabaseExplorerUIsTest extends TestBase {

    private DatabaseConnection dbconn1 = null;
    private DatabaseConnection dbconn2 = null;

    public DatabaseExplorerUIsTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        Util.suppressSuperfluousLogging();
        super.setUp();
    }
    
    private void clearConnections() throws Exception {
        DatabaseConnection[] connections = ConnectionManager.getDefault().getConnections();
        for (DatabaseConnection dc : connections) {
            ConnectionManager.getDefault().removeConnection(dc);
        }
        assertEquals(0, ConnectionManager.getDefault().getConnections().length);
    }
    
    private void initConnections() throws Exception {
        clearConnections();
        JDBCDriver driver = Util.createDummyDriver();
        dbconn1 = DatabaseConnection.create(driver, "db", "dbuser", "dbschema", "dbpassword", true);
        dbconn2 = DatabaseConnection.create(driver, "database", "user", "schema", "password", true);
        ConnectionManager.getDefault().addConnection(dbconn1);
        ConnectionManager.getDefault().addConnection(dbconn2);
        assertEquals(2, ConnectionManager.getDefault().getConnections().length);
    }

    private JComboBox connect() {
        JComboBox combo = new JComboBox();
        DatabaseExplorerUIs.connect(combo, ConnectionManager.getDefault());
        return combo;
    }

    public void testEmptyComboboxContent() throws Exception {
        clearConnections();
        
        JComboBox combo = connect();

        forceFlush();
        
        assertEquals("Wrong number of items in the empty combobox", 1, combo.getItemCount());
    }

    public void testComboboxWithConnections() throws Exception {
        initConnections();
        JComboBox combo = connect();

        forceFlush(); // The next assert would occasionally fail unless this delay is added.

        assertTrue("Wrong number of items in the combobox", combo.getItemCount() == 3);

        assertSame(dbconn2, combo.getItemAt(0));
        assertSame(dbconn1, combo.getItemAt(1));
    }

    public void testComboboxChangingConnections() throws Exception {
        initConnections();
        JComboBox combo = connect();

        forceFlush(); // The next assert would occasionally fail unless this delay is added.

        assertEquals("Wrong number of items in the combobox", 3, combo.getItemCount());

        assertSame(dbconn2, combo.getItemAt(0));
        assertSame(dbconn1, combo.getItemAt(1));

        DatabaseConnection dc = DatabaseConnection.create(Util.createDummyDriver(), "dc1", "user", "schema", "password", true);
        ConnectionManager.getDefault().addConnection(dc);

        forceFlush();

        assertEquals("Wrong number of items in the combobox", 4, combo.getItemCount());

        assertSame(dc, combo.getItemAt(2));

        ConnectionManager.getDefault().removeConnection(dc);

        forceFlush();

        assertEquals("Wrong number of items in the combobox", 3, combo.getItemCount());

        assertSame(dbconn2, combo.getItemAt(0));
        assertSame(dbconn1, combo.getItemAt(1));
    }
}
