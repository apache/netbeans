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

package org.netbeans.api.db.explorer;

import java.sql.Connection;
import java.util.Properties;
import org.netbeans.modules.db.test.Util;
import org.netbeans.modules.db.test.DBTestBase;
import org.openide.util.Utilities;

/**
 *
 * @author Andrei Badea
 */
public class DatabaseConnectionTest extends DBTestBase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Util.clearConnections();
    }
    
    public DatabaseConnectionTest(String testName) {
        super(testName);
    }
    
    public void testConnectionsRemovedWhenFilesDeleted() throws Exception{
        Util.clearConnections();
        Util.deleteDriverFiles();

        JDBCDriver driver = Util.createDummyDriver();
        assertEquals(1, JDBCDriverManager.getDefault().getDrivers().length);

        DatabaseConnection dbconn = DatabaseConnection.create(driver, "database", "user", "schema", "password", true);
        ConnectionManager.getDefault().addConnection(dbconn);

        assertTrue(ConnectionManager.getDefault().getConnections().length > 0);

        Util.clearConnections();

        assertTrue(ConnectionManager.getDefault().getConnections().length == 0);
    }

    public void testGetJDDCDriver() throws Exception{
        Util.clearConnections();
        Util.deleteDriverFiles();

        JDBCDriver driver = Util.createDummyDriver();
        assertEquals(1, JDBCDriverManager.getDefault().getDrivers().length);

        DatabaseConnection dbconn = DatabaseConnection.create(driver, "database", "user", "schema", "password", true);
        assertEquals ("Returns the correct driver", driver, dbconn.getJDBCDriver ());
    }

    public void testGetJDDCDriverWhenAddOtherDriver() throws Exception{
        Util.clearConnections();
        Util.deleteDriverFiles();

        JDBCDriver driver1 = Util.createDummyDriver();
        assertEquals(1, JDBCDriverManager.getDefault().getDrivers().length);
        DatabaseConnection dbconn1 = DatabaseConnection.create(driver1, "database", "user", "schema", "password", true);
        assertEquals ("Returns the correct driver", driver1, dbconn1.getJDBCDriver ());

        JDBCDriver driver2 = Util.createDummyDriverWithOtherJar ();
        assertEquals(2, JDBCDriverManager.getDefault().getDrivers().length);
        DatabaseConnection dbconn2 = DatabaseConnection.create(driver2, "database", "user", "schema", "password", true);
        assertEquals ("Returns the correct driver", driver1, dbconn1.getJDBCDriver ());
        assertEquals ("Returns the correct driver", driver2, dbconn2.getJDBCDriver ());
    }

    public void testSameDatabaseConnectionReturned() throws Exception {
        Util.clearConnections();
        Util.deleteDriverFiles();
        assertEquals(0, ConnectionManager.getDefault().getConnections().length);
        
        JDBCDriver driver = Util.createDummyDriver();
        assertEquals(1, JDBCDriverManager.getDefault().getDrivers().length);

        DatabaseConnection dbconn = DatabaseConnection.create(driver, "database", "user", "schema", "password", true);
        ConnectionManager.getDefault().addConnection(dbconn);
        assertTrue(ConnectionManager.getDefault().getConnections().length == 1);
        
        assertEquals(dbconn, ConnectionManager.getDefault().getConnections()[0]);
    }

    public void testSyncConnection() throws Exception {
        DatabaseConnection dbconn = getDatabaseConnection(false);
        ConnectionManager.getDefault().disconnect(dbconn);
        assertNull(dbconn.getJDBCConnection());
        
        ConnectionManager.getDefault().connect(dbconn);
        Connection conn = dbconn.getJDBCConnection();
        assertTrue(connectionIsValid(conn));
        
        ConnectionManager.getDefault().connect(dbconn);
        assertSame(conn, dbconn.getJDBCConnection());
        assertTrue(connectionIsValid(dbconn.getJDBCConnection()));
    }

    public void testDeleteConnection() throws Exception {
        Util.clearConnections();
        Util.deleteDriverFiles();
        
        assertEquals(0, ConnectionManager.getDefault().getConnections().length);
        assertEquals(0, JDBCDriverManager.getDefault().getDrivers().length);
        
        JDBCDriver driver = Util.createDummyDriver();
        
        DatabaseConnection dbconn = DatabaseConnection.create(
                    driver, "jdbc:bar:localhost", 
                    "user", "schema", "password", true);
        ConnectionManager.getDefault().addConnection(dbconn);
        
        assertEquals(1, ConnectionManager.getDefault().getConnections().length);
        
        ConnectionManager.getDefault().removeConnection(dbconn);
        assertEquals(0, ConnectionManager.getDefault().getConnections().length);
    }

    public void testDisconnect() throws Exception {
        DatabaseConnection conn = getDatabaseConnection(true);
        assertTrue(connectionIsValid(conn.getJDBCConnection()));

        ConnectionManager.getDefault().disconnect(conn);
        assertFalse(connectionIsValid(conn.getJDBCConnection()));

        ConnectionManager.getDefault().connect(conn);
        assertTrue(connectionIsValid(conn.getJDBCConnection()));

    }

    public void testGetJDBCConnectionWithTest() throws Exception {
        DatabaseConnection dbconn = getDatabaseConnection(false);
        ConnectionManager.getDefault().disconnect(dbconn);
        assertNull(dbconn.getJDBCConnection(true));
        assertFalse(connectionIsValid(dbconn.getJDBCConnection()));

        ConnectionManager.getDefault().connect(dbconn);
        assertNotNull(dbconn.getJDBCConnection(true));
        assertTrue(connectionIsValid(dbconn.getJDBCConnection(true)));
        assertNotNull(dbconn.getJDBCConnection(false));

        dbconn.getJDBCConnection(true).close();
        assertNotNull(dbconn.getJDBCConnection(false));
        assertNull(dbconn.getJDBCConnection(true));
        assertNull(dbconn.getJDBCConnection(false));

        ConnectionManager.getDefault().connect(dbconn);
        assertNotNull(dbconn.getJDBCConnection(true));
        assertTrue(connectionIsValid(dbconn.getJDBCConnection(true)));
        assertNotNull(dbconn.getJDBCConnection(false));

        if (!Utilities.isWindows()) { // Causes OOME on Win - TODO investigate
            if (isDerby()) {
                shutdownDerby();
                assertNull(dbconn.getJDBCConnection(true));
            }
        } else {
            System.out.println("Shutdown derby:"
                    + " Skipping part of test that fails on Windows");
        }
    }

    /**
     * Verifies that the {@link DatabaseConnection#create(org.netbeans.api.db.explorer.JDBCDriver, java.lang.String, java.lang.String, java.lang.String, java.lang.String, boolean, java.lang.String)}
     * factory method creates a valid connection with the given display name
     *
     * @throws Exception
     */
    public void testDatabaseConnectionCreatedWithDisplayName() throws Exception {
        Util.clearConnections();
        Util.deleteDriverFiles();

        JDBCDriver driver = Util.createDummyDriver();
        DatabaseConnection dbconn = DatabaseConnection.create(driver, "database", "user", "schema", "password", true, "displayName");

        assertEquals("The connection was created with a display name different that the one provided", "displayName", dbconn.getDisplayName());

        Util.clearConnections();

    }

    /**
     * Verifies that the {@link DatabaseConnection#create(org.netbeans.api.db.explorer.JDBCDriver, java.lang.String, java.lang.String, java.lang.String, java.lang.String, boolean)}
     * creates some default display name that is not null
     * @throws Exception
     */
    public void testDatabaseConnectionCreatedWithDefaultDisplayName() throws Exception {
        Util.clearConnections();
        Util.deleteDriverFiles();

        JDBCDriver driver = Util.createDummyDriver();
        DatabaseConnection dbconn = DatabaseConnection.create(driver, "database", "user", "schema", "password", true);

        assertEquals("The connection was created with the default display name ", "database [user on schema]", dbconn.getDisplayName());

        Util.clearConnections();
    }

    /**
     * Test that additional connection properties are set and get correctly.
     */
    public void testGetConnectionProperties() throws Exception {
        DatabaseConnection nullPropertiesConn = DatabaseConnection.create(
                getJDBCDriver(), getDbUrl(), getUsername(), getSchema(),
                getPassword(), false, "Test", null);
        Properties p = nullPropertiesConn.getConnectionProperties();
        assertNotNull(p);
        assertTrue("Properties object should be empty", p.keySet().isEmpty());

        Properties testConnProps = new Properties();
        testConnProps.put("testKey", "testValue");
        DatabaseConnection somePopertiesConn = DatabaseConnection.create(
                getJDBCDriver(), getDbUrl(), getUsername(), getSchema(),
                getPassword(), false, "Test", testConnProps);
        Properties returnedProps = somePopertiesConn.getConnectionProperties();
        assertEquals(1, returnedProps.keySet().size());
        assertEquals("testValue", returnedProps.get("testKey"));

        returnedProps.put("addedKey", "addedValue");
        Properties returnedAgain = somePopertiesConn.getConnectionProperties();
        assertEquals("Internal properties should not be affected by changes",
                1, returnedAgain.keySet().size());
    }

    private static boolean connectionIsValid(Connection conn) throws Exception {
        return (conn != null) && (! conn.isClosed());
    }
}
