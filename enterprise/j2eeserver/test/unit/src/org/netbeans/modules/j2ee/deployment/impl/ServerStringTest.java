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

package org.netbeans.modules.j2ee.deployment.impl;

import javax.enterprise.deploy.spi.Target;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceCreationException;

/**
 *
 * @author Petr Hejl
 */
public class ServerStringTest extends ServerRegistryTestBase {

    private static final String TEST_PLUGIN = "fooplugin";

    private static final String TEST_URL = "fooservice:testServerString";

    private static final String TEST_TARGET = "target";

    public ServerStringTest(String name) {
        super(name);
    }

    public void testConstructors() throws InstanceCreationException {
        ServerRegistry registry = ServerRegistry.getInstance();
        registry.addInstance(TEST_URL, "user", "password", "TestInstance", true, false, null);
        ServerInstance instance = registry.getServerInstance(TEST_URL);

        ServerString serverString = new ServerString(TEST_PLUGIN, TEST_URL, null, null);
        assertProperties(serverString, TEST_PLUGIN, TEST_URL, new String[] {}, instance);

        serverString = new ServerString(TEST_PLUGIN, TEST_URL, new String[] {TEST_TARGET}, null);
        assertProperties(serverString, TEST_PLUGIN, TEST_URL, new String[] {TEST_TARGET}, instance);

        serverString = new ServerString(TEST_PLUGIN, TEST_URL, new String[] {TEST_TARGET}, instance);
        assertProperties(serverString, TEST_PLUGIN, TEST_URL, new String[] {TEST_TARGET}, instance);

        serverString = new ServerString(TEST_PLUGIN, TEST_URL, null);
        assertProperties(serverString, TEST_PLUGIN, TEST_URL, new String[] {}, instance);

        serverString = new ServerString(TEST_PLUGIN, TEST_URL, new String[] {TEST_TARGET});
        assertProperties(serverString, TEST_PLUGIN, TEST_URL, new String[] {TEST_TARGET}, instance);

        serverString = new ServerString(instance);
        assertProperties(serverString, instance.getServer().getShortName(), TEST_URL, new String[] {}, instance);

        serverString = new ServerString(new ServerTarget(instance, new TestTarget()));
        assertProperties(serverString, instance.getServer().getShortName(),
                TEST_URL, new String[] {TEST_TARGET}, instance);

        serverString = new ServerString(instance, TEST_TARGET);
        assertProperties(serverString, instance.getServer().getShortName(),
                TEST_URL, new String[] {TEST_TARGET}, instance);

        serverString = new ServerString(instance, null);
        assertProperties(serverString, instance.getServer().getShortName(), TEST_URL, new String[] {}, instance);
    }

    private static void assertProperties(ServerString serverString, String plugin,
            String url, String[] targets, ServerInstance serverInstance) {

        assertEquals(plugin, serverString.getPlugin());
        assertEquals(url, serverString.getUrl());
        assertEquals(ServerRegistry.getInstance().getServer(plugin), serverString.getServer());
        if (targets == null) {
            assertNull(serverString.getTargets());
        } else {
            assertEquals(targets.length, serverString.getTargets().length);
            for (int i = 0; i < targets.length; i++) {
                assertEquals(targets[i], serverString.getTargets()[i]);
            }
        }
        assertEquals(serverInstance, serverString.getServerInstance());
    }

    private static class TestTarget implements Target {

        public String getDescription() {
            return TEST_TARGET;
        }

        public String getName() {
            return TEST_TARGET;
        }

    }
}
