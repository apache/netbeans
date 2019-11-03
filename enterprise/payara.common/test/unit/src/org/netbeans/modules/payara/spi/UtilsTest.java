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

package org.netbeans.modules.payara.spi;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.netbeans.modules.payara.tooling.TaskState;
import org.netbeans.modules.payara.tooling.admin.CommandGetProperty;
import org.netbeans.modules.payara.tooling.admin.ResultMap;
import org.junit.*;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.payara.common.PayaraInstance;

/**
 *
 * @author vkraemer
 */
public class UtilsTest extends NbTestCase {

    public UtilsTest(String testName) {
        super(testName);
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    @Override
    public void setUp() {
    }

    @After
    @Override
    public void tearDown() {
    }


    /**
     * Test of getHttpListenerProtocol method, of class Utils.
     */
    @Test
    public void testGetHttpListenerProtocol() {
        System.out.println("getHttpListenerProtocol");
        String hostname = "glassfish.java.net";
        //int port = 443;
        //String expResult = "https";
        //String result = Utils.getHttpListenerProtocol(hostname, port);
        //assertEquals(expResult, result);
        int port = 80;
        String expResult = "http";
        String result = Utils.getHttpListenerProtocol(hostname, port);
        assertEquals(expResult, result);
    }

    @Test
    public void testGetFileFromPattern() throws Exception {
        System.out.println("getFileFromPattern");
        File f;
        try {
            f = Utils.getFileFromPattern(null, null);
            assertNull(f);
        } catch (AssertionError ae) {
            // I expect this
        }
        try {
            f = Utils.getFileFromPattern("", null);
            assertNull(f);
        } catch (AssertionError ae) {
            // I expect this
        }
        File dataDir = getDataDir();
        try {
            f = Utils.getFileFromPattern(null, dataDir);
            assertNull(f);
        } catch (AssertionError ae) {
            // I expect this
        }
        f = Utils.getFileFromPattern("", dataDir);
        assertNull(f);
        f = Utils.getFileFromPattern("", new File(dataDir, "nottaDir"));
        assertNull(f);
        f = Utils.getFileFromPattern("nottaDir", dataDir);
        assertNotNull(f);
        f = Utils.getFileFromPattern("nottaDir"+Utils.VERSIONED_JAR_SUFFIX_MATCHER, dataDir);
        assertNotNull(f);
        f = Utils.getFileFromPattern("nottaDir.jar", dataDir);
        assertNull(f);
        f = Utils.getFileFromPattern("subdir/nottaDir"+Utils.VERSIONED_JAR_SUFFIX_MATCHER, dataDir);
        assertNotNull(f);
        f = Utils.getFileFromPattern("subdir/nottaDir.jar", dataDir);
        assertNull(f);
        f = Utils.getFileFromPattern("nottasubdir/nottaDir"+Utils.VERSIONED_JAR_SUFFIX_MATCHER, dataDir);
        assertNull(f);
    }
    /**
     * Test of sanitizeName method, of class Commands.
     */
    @Test
    public void testSanitizeName() {
        System.out.println("sanitizeName");
        String name = "aa";
        String expResult = "aa";
        String result = Utils.sanitizeName(name);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        name = "1a";
        expResult = "1a";
        result = Utils.sanitizeName(name);
        assertEquals(expResult, result);
        name = "_a";
        expResult = "_a";
        result = Utils.sanitizeName(name);
        assertEquals(expResult, result);
        name = ".a";
        expResult = "_.a";
        result = Utils.sanitizeName(name);
        assertEquals(expResult, result);
        name = "foo(bar)";
        expResult = "_foo_bar_";
        result = Utils.sanitizeName(name);
        assertEquals(expResult, result);
        name = "foo((bar)";
        expResult = "_foo__bar_";
        result = Utils.sanitizeName(name);
        assertEquals(expResult, result);
        name = ".a()";
        expResult = "_.a__";
        result = Utils.sanitizeName(name);
        assertEquals(expResult, result);
        name = null;
        expResult = null;
        result = Utils.sanitizeName(name);
        assertEquals(expResult, result);
    }

    public static void main(String... args)
            throws InterruptedException, ExecutionException {
        for (int i = 0 ; i < 2000 ; i++) {
            String hostname =  //"127.0.0.1";
                 "10.229.117.91";
            int port = 4848;
            Map<String,String> ip = new HashMap<String,String>();
            ip.put(PayaraModule.HOSTNAME_ATTR, hostname);
            ip.put(PayaraModule.ADMINPORT_ATTR, port+"");
            PayaraInstance instance = PayaraInstance.create(ip, null);
            ResultMap<String, String> result
                    = CommandGetProperty.getProperties(
                instance, "*.server-config.*.http-listener-1.port");
            if (result.getState() == TaskState.COMPLETED) {
                System.out.println(result.getValue());
            } else {
                System.out.println(
                        "Could not retrieve properties from server.");
            }
        }
        System.exit(0);
    }

    @Test
    public void testIsLocalPortOccupied() throws IOException {
        System.out.println("isLocalPortOccupied");
        ServerSocket ss = new ServerSocket(0);
        int port = ss.getLocalPort();
        assert Utils.isLocalPortOccupied(port) : "the port is not occupied?";
        ss.close();
        assert !Utils.isLocalPortOccupied(port) : "the port is occupied?";
    }
}
