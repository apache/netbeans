/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
        File jarFile = new File(JDBCDriverManager.class.getProtectionDomain().getCodeSource().getLocation().toURI());
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
        propName = propName.substring(0, 1).toUpperCase() +
                propName.substring(1);
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
