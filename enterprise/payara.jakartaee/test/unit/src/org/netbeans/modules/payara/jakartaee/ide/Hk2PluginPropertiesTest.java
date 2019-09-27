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

package org.netbeans.modules.payara.jakartaee.ide;

import java.io.IOException;
import java.net.ServerSocket;
import org.netbeans.modules.payara.tooling.utils.NetUtils;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author vbk
 */
public class Hk2PluginPropertiesTest {

    public Hk2PluginPropertiesTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of isRunning method, of class Hk2PluginProperties.
     */
    @Test
    public void testIsRunning() throws IOException {
        String host = "10.229.117.91";
        //localhost";
        if ("localhost".equals(host) || "127.0.0.1".equals(host)) {
            System.out.println("isRunning");
            ServerSocket ss = new ServerSocket(0);
            int port = ss.getLocalPort();
            boolean expResult = true;
            boolean result = NetUtils.isPortListeningRemote(
                    host, port, NetUtils.PORT_CHECK_TIMEOUT);
            assertEquals(expResult, result);
            ss.close();
            result = NetUtils.isPortListeningRemote(
                    host, port, NetUtils.PORT_CHECK_TIMEOUT);
            expResult = false;
            assertEquals(expResult, result);
            port = 4848;
            try {
                ss = new ServerSocket(port);
            // It looks like there is an app server running, let's pound on it.
            } catch (IOException ioe) {
                System.out.println("isRunning " + host + ":4848");
                poundOnIt(host, port, true);
            } finally {
                ss.close();
            }
        } else {
            System.out.println("isRunning "+host+":4848");
            poundOnIt(host, 4848, NetUtils.isPortListeningRemote(
                    host, 4848, NetUtils.PORT_CHECK_TIMEOUT));
        }
    }

    private void poundOnIt(String host, int port, boolean expResult) {
        boolean result = NetUtils.isPortListeningRemote(
                host, port, NetUtils.PORT_CHECK_TIMEOUT);
        assertEquals(expResult, result);
        for (int i = 0; result && i < 4000; i++) {
            NetUtils.isPortListeningRemote(
                    host, port, NetUtils.PORT_CHECK_TIMEOUT);
        }
    }


}