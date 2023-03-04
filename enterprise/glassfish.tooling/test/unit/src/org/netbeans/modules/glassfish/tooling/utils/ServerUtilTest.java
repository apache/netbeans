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
package org.netbeans.modules.glassfish.tooling.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.glassfish.tooling.CommonTest;
import org.netbeans.modules.glassfish.tooling.admin.CommandHttpTest;
import org.netbeans.modules.glassfish.tooling.admin.CommandRestTest;
import org.netbeans.modules.glassfish.tooling.data.GlassFishServer;
import org.netbeans.modules.glassfish.tooling.data.GlassFishVersion;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 * Test GlassFish server related utilities.
 * <p>
 * @author Tomas Kraus, Peter Benedikovic
 */
@Test(groups = {"unit-tests"})
public class ServerUtilTest extends CommonTest {

    /**
     * Helper method to test <code>ServerUtils.getServerVersion</code>
     * functionality.
     * <p/>
     * @param server Glassfish server instance to be tested.
     */
    public void doTestGetServerVersion(final GlassFishServer server) {
        GlassFishVersion version = ServerUtils.getServerVersion(
                server.getServerHome());
        assertNotNull(version);
    }

    /**
     * Test <code>ServerUtils.getServerVersion</code> functionality
     * on GlassFish v3.
     */
    @Test
    public void testGetServerVersionGFv3() {
        doTestGetServerVersion(CommandHttpTest.glassFishServer());
    }

    /**
     * Test <code>ServerUtils.getServerVersion</code> functionality
     * on GlassFish v3.
     */
    @Test
    public void testGetServerVersionGFv4() {
        doTestGetServerVersion(CommandRestTest.glassFishServer());
    }

    /**
     * Test <code>ServerUtils.addComponentToMap</code> functionality.
     */
    @Test
    public void testAddComponentToMap() {
        String[] components = {
            "application1 <ejb>",
            "library1 <appclient, connector, web, ejb>",
            "application2 <connector, ejb>"
        }; 
        Map<String, List<String>> map = new HashMap<>();
        for (String component : components) {
            ServerUtils.addComponentToMap(map, component);
        }
        List listEjb = map.get("ejb");
        List listWeb = map.get("web");
        assertTrue(listEjb.contains("application1"));
        assertTrue(listEjb.contains("application2"));
        assertTrue(listWeb.contains("library1"));
    }

    /**
     * Helper method to test Jersey version string retrieving method.
     * <p/>
     * @param server Glassfish server instance to be tested.
     */
    private void doTestGetJerseyVersion(final GlassFishServer server) {
        String version = ServerUtils.getJerseyVersion(server.getServerHome());
        assertNotNull(version);
        String[] items = version.split("\\.");
        assertTrue(items != null && items.length > 0);
        for (String item : items) {
            try {
                Integer.parseInt(item);
            } catch (NumberFormatException nfe) {
                fail("Version component is not a number.");
            }
        }
    }

    /**
     * Test Jersey version string retrieving method on GlassFish v3.
     */
    @Test
    public void testGetJerseyVersionGFv3() {
        doTestGetJerseyVersion(CommandHttpTest.glassFishServer());
    }

    /**
     * Test Jersey version string retrieving method on GlassFish v4.
     */
    @Test
    public void testGetJerseyVersionGFv4() {
        doTestGetJerseyVersion(CommandRestTest.glassFishServer());
    }

}
