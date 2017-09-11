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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2010 Sun
 * Microsystems, Inc. All Rights Reserved.
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
