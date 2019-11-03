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

package org.netbeans.modules.db.mysql.impl;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.ResourceBundle;
import org.netbeans.api.db.explorer.JDBCDriverManager;
import org.netbeans.modules.db.mysql.DatabaseServer;
import org.netbeans.modules.db.mysql.test.TestBase;
import org.netbeans.modules.db.mysql.util.Utils;
import org.openide.filesystems.FileUtil;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;
import org.openide.util.Utilities;

/**
 *
 * @author David
 */
public class MySQLDatabaseServerTest extends TestBase {

    private DatabaseServer server;

    public MySQLDatabaseServerTest(String testName) {
        super(testName);
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        Lookup.getDefault().lookup(ModuleInfo.class);

        // We need to set up netbeans.dirs so that the NBInst URLMapper correctly
        // finds the mysql jar file
        File jarFile = Utilities.toFile(JDBCDriverManager.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        File clusterDir = jarFile.getParentFile().getParentFile();
        System.setProperty("netbeans.dirs", clusterDir.getAbsolutePath());

        getProperties();

        server = MySQLDatabaseServer.getDefault();
        server.setUser(getUser());
        server.setPassword(getPassword());
        server.setHost(getHost());
        server.setPort(getPort());
    }

    /**
     * Test of getDefault method, of class MySQLDatabaseServer.
     */
    public void testGetDefault() {
        DatabaseServer expResult = MySQLDatabaseServer.getDefault();
        DatabaseServer result = MySQLDatabaseServer.getDefault();
        assertEquals(expResult, result);
    }

    /**
     * Test of getHost method, of class MySQLDatabaseServer.
     */
    public void testHost() throws Exception {
        testStringProperty("host", "localhost");
    }

    private void testStringProperty(String propName, String defaultValue) throws Exception {
        propName = propName.substring(0, 1).toUpperCase() + propName.substring(1);
        String setter = "set" + propName;
        String getter = "get" + propName;

        String value = "Testing " + propName;

        Method setMethod = server.getClass().getMethod(setter, String.class);
        Method getMethod = server.getClass().getMethod(getter);

        setMethod.invoke(server, value);
        String result = (String)getMethod.invoke(server);

        assertEquals(result, value);

        if ( defaultValue != null ) {
            setMethod.invoke(server, (Object)null);
            result = (String)getMethod.invoke(server);
            assertEquals(defaultValue, result);
        }

    }

    private void testStringProperty(String propName) throws Exception {
        testStringProperty(propName, null);
    }


    /**
     * Test of getPort method, of class MySQLDatabaseServer.
     */
    public void testPort() throws Exception {
        testStringProperty("port", "3306");
    }


    /**
     * Test of getUser method, of class MySQLDatabaseServer.
     */
    public void testUser() throws Exception {
        testStringProperty("user", "root");
    }

    /**
     * Test of getPassword method, of class MySQLDatabaseServer.
     */
    public void testPassword() throws Exception {
        testStringProperty("password", "");
    }

    /**
     * Test of isSavePassword method, of class MySQLDatabaseServer.
     */
    public void testSavePassword() {
        boolean value = ! server.isSavePassword();
        server.setSavePassword(value);
        assert(value == server.isSavePassword());
    }

    /**
     * Test of getAdminPath method, of class MySQLDatabaseServer.
     */
    public void testAdminPath() throws Exception {
        testStringProperty("adminPath");
    }

    /**
     * Test of getStartPath method, of class MySQLDatabaseServer.
     */
    public void testStartPath() throws Exception {
        testStringProperty("startPath");
    }

    /**
     * Test of getStopPath method, of class MySQLDatabaseServer.
     */
    public void testStopPath() throws Exception {
        testStringProperty("stopPath");
    }

    /**
     * Test of getStopArgs method, of class MySQLDatabaseServer.
     */
    public void testStopArgs() throws Exception {
        testStringProperty("stopArgs");
    }

    /**
     * Test of getStartArgs method, of class MySQLDatabaseServer.
     */
    public void testStartArgs() throws Exception {
        testStringProperty("startArgs");
    }

    /**
     * Test of getAdminArgs method, of class MySQLDatabaseServer.
     */
    public void testAdminArgs() throws Exception {
        testStringProperty("adminArgs");
    }


    /**
     * Test of isConnected method, of class MySQLDatabaseServer.
     */
    public void testIsConnected() throws Exception {
        System.out.println(Arrays.asList(FileUtil.getConfigFile("Databases/JDBCDrivers").getChildren()));
        assertFalse(server.isConnected());
        server.reconnect();
        assertTrue(server.isConnected());
        server.disconnectSync();
        assertFalse(server.isConnected());
        server.reconnect();
        assertTrue(server.isConnected());
    }

    /**
     * Test of getDisplayName method, of class MySQLDatabaseServer.
     */
    public void testGetDisplayName() throws Exception {
        String displayNameLabel = "LBL_ServerDisplayName";

        String hostPort = server.getPort();
        String user = server.getUser();
        String port = getPort();
        if ( Utils.isEmpty(port)) {
            port = "";
        } else {
            port = ":" + port;
        }
        hostPort = getHost() + port;

        server.disconnectSync();
        String stateLabel = server.getState().name();
        String disconnectedString = Utils.getMessage(displayNameLabel, hostPort, user, Utils.getMessage(stateLabel));
        assertEquals(disconnectedString, server.getDisplayName());

        server.reconnect();
        stateLabel = server.getState().name();
        String connectedString = Utils.getMessage(displayNameLabel, hostPort, user, Utils.getMessage(stateLabel));
        assertEquals(connectedString, server.getDisplayName());
    }

    /**
     * Test of getShortDescription method, of class MySQLDatabaseServer.
     */
    public void testGetShortDescription() {
        ResourceBundle bundle = Utils.getBundle();
        String description = bundle.getString("LBL_ServerShortDescription");
        description = description.replace("{0}", getHost() + ":" + getPort()).replace("{1}", getUser()).replace("{2}", Utils.getMessage(server.getState().name()));
        assertEquals(description, server.getShortDescription());
    }

    /**
     * Test of getURL method, of class MySQLDatabaseServer.
     */
    public void testGetURL_0args() {
    }

    /**
     * Test of getURL method, of class MySQLDatabaseServer.
     */
    public void testGetURL_String() {
    }

    /**
     * Test of refreshDatabaseList method, of class MySQLDatabaseServer.
     */
    public void testRefreshDatabaseList() throws Exception {
    }

    /**
     * Test of getDatabases method, of class MySQLDatabaseServer.
     */
    public void testGetDatabases() throws Exception {
    }

    /**
     * Test of databaseExists method, of class MySQLDatabaseServer.
     */
    public void testDatabaseExists() throws Exception {
    }

    /**
     * Test of createDatabase method, of class MySQLDatabaseServer.
     */
    public void testCreateDatabase() throws Exception {
    }

    /**
     * Test of dropDatabase method, of class MySQLDatabaseServer.
     */
    public void testDropDatabase() throws Exception {
    }

    /**
     * Test of getUsers method, of class MySQLDatabaseServer.
     */
    public void testGetUsers() throws Exception {
    }

    /**
     * Test of grantFullDatabaseRights method, of class MySQLDatabaseServer.
     */
    public void testGrantFullDatabaseRights() throws Exception {
    }

    /**
     * Test of start method, of class MySQLDatabaseServer.
     */
    public void testStart() throws Exception {
    }

    /**
     * Test of stop method, of class MySQLDatabaseServer.
     */
    public void testStop() throws Exception {
    }

    /**
     * Test of startAdmin method, of class MySQLDatabaseServer.
     */
    public void testStartAdmin() throws Exception {
    }

    /**
     * Test of addChangeListener method, of class MySQLDatabaseServer.
     */
    public void testAddChangeListener() {
    }

    /**
     * Test of removeChangeListener method, of class MySQLDatabaseServer.
     */
    public void testRemoveChangeListener() {
    }

}
