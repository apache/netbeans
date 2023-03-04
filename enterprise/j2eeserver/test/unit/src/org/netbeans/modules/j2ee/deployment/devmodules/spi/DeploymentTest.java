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

package org.netbeans.modules.j2ee.deployment.devmodules.spi;

import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistryTestBase;

/**
 *
 * @author Petr Hejl
 */
public class DeploymentTest extends ServerRegistryTestBase {

    private static final String URL = "fooservice:testInstance"; // NOI18N

    public DeploymentTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ServerRegistry registry = ServerRegistry.getInstance();
        registry.addInstance(URL, "user", "password", "TestInstance", true, false, null); // NOI18N
    }

    @Override
    protected void tearDown() throws Exception {
        ServerRegistry registry = ServerRegistry.getInstance();
        registry.removeServerInstance(URL);
        super.tearDown();
    }

    public void testGetDisplayName() {
        assertEquals("TestInstance", Deployment.getDefault().getServerInstanceDisplayName(URL));
        ServerRegistry.getInstance().removeServerInstance(URL);
        assertNull(Deployment.getDefault().getServerInstanceDisplayName(URL));
    }

    public void testGetServerID() {
        Deployment deployment = Deployment.getDefault();
        assertEquals("Test", deployment.getServerID(URL));
        ServerRegistry.getInstance().removeServerInstance(URL);
        assertNull(deployment.getServerID(URL));
    }

    public void testIsRunning() {
        // TODO missing test for running state
        assertFalse(Deployment.getDefault().isRunning(URL));
        ServerRegistry.getInstance().removeServerInstance(URL);
        assertFalse(Deployment.getDefault().isRunning(URL));
    }

    public void testGetJ2eePlatform() {
        assertNotNull(Deployment.getDefault().getJ2eePlatform(URL));
        ServerRegistry.getInstance().removeServerInstance(URL);
        assertNull(Deployment.getDefault().getJ2eePlatform(URL));
    }

    public void testGetServerDisplayName() {
        assertEquals("Sample JSR88 plugin", Deployment.getDefault().getServerDisplayName("Test"));
        ServerRegistry.getInstance().removeServerInstance(URL);
        assertEquals("Sample JSR88 plugin", Deployment.getDefault().getServerDisplayName("Test"));
    }

    public void testGetServerInstance() {
        assertNotNull(Deployment.getDefault().getServerInstance(URL));
        try {
            Deployment.getDefault().getServerInstance(null);
            fail("getServerInstance accepts null");
        } catch (NullPointerException ex) {
            // expected
        }
    }
}
