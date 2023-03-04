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

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ServerInstance;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistryTestBase;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;

/**
 *
 * @author Petr Hejl
 */
public class ServerInstanceTest extends ServerRegistryTestBase {

    private static final String URL = "fooservice:testInstance"; // NOI18N

    public ServerInstanceTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        ServerRegistry registry = ServerRegistry.getInstance();
        Map<String, String> props = new HashMap<String, String>();
        props.put(InstanceProperties.HTTP_PORT_NUMBER, "8080");
        registry.addInstance(URL, "user", "password", "TestInstance", true, false, props); // NOI18N
    }

    @Override
    protected void tearDown() throws Exception {
        ServerRegistry registry = ServerRegistry.getInstance();
        registry.removeServerInstance(URL);
        super.tearDown();
    }

    public void testServerInstanceGC() {
        ServerRegistry registry = ServerRegistry.getInstance();

        WeakReference<org.netbeans.modules.j2ee.deployment.impl.ServerInstance> instance =
                new WeakReference<org.netbeans.modules.j2ee.deployment.impl.ServerInstance>(registry.getServerInstance(URL));
        registry.removeServerInstance(URL);

        assertGC("The instance for " + URL + " has not been collected", instance);
    }

    public void testGetDisplayName() throws InstanceRemovedException {
        ServerInstance instance = Deployment.getDefault().getServerInstance(URL);
        assertEquals("TestInstance", instance.getDisplayName());
        ServerRegistry.getInstance().removeServerInstance(URL);
        try {
            instance.getDisplayName();
            fail("Does not throw InstanceRemovedException");
        } catch (InstanceRemovedException ex) {
            // expected
        }
    }

    public void testGetServerDisplayName() throws InstanceRemovedException {
        ServerInstance instance = Deployment.getDefault().getServerInstance(URL);
        assertEquals("Sample JSR88 plugin", instance.getServerDisplayName());
        ServerRegistry.getInstance().removeServerInstance(URL);
        try {
            instance.getServerDisplayName();
            fail("Does not throw InstanceRemovedException");
        } catch (InstanceRemovedException ex) {
            // expected
        }
    }

    public void testGetServerID() throws InstanceRemovedException {
        ServerInstance instance = Deployment.getDefault().getServerInstance(URL);
        assertEquals("Test", instance.getServerID());
        ServerRegistry.getInstance().removeServerInstance(URL);
        try {
            instance.getServerID();
            fail("Does not throw InstanceRemovedException");
        } catch (InstanceRemovedException ex) {
            // expected
        }
    }

    public void testIsRunning() throws InstanceRemovedException {
        ServerInstance instance = Deployment.getDefault().getServerInstance(URL);
        assertFalse(instance.isRunning());
        ServerRegistry.getInstance().removeServerInstance(URL);
        try {
            instance.isRunning();
            fail("Does not throw InstanceRemovedException");
        } catch (InstanceRemovedException ex) {
            // expected
        }
    }

    public void testGetJ2eePlatform() throws InstanceRemovedException {
        ServerInstance instance = Deployment.getDefault().getServerInstance(URL);
        assertNotNull(instance.getJ2eePlatform());
        ServerRegistry.getInstance().removeServerInstance(URL);
        try {
            instance.getJ2eePlatform();
            fail("Does not throw InstanceRemovedException");
        } catch (InstanceRemovedException ex) {
            // expected
        }
    }

    public void testDescriptor() throws InstanceRemovedException {
        ServerInstance instance = Deployment.getDefault().getServerInstance(URL);
        ServerInstance.Descriptor descriptor = instance.getDescriptor();
        assertNotNull(descriptor);
        assertEquals(8080, descriptor.getHttpPort());
        assertEquals("localhost", descriptor.getHostname());
        assertTrue(descriptor.isLocal());

        ServerRegistry.getInstance().removeServerInstance(URL);

        try {
            descriptor.getHttpPort();
            fail("Does not throw InstanceRemovedException");
        } catch (InstanceRemovedException ex) {
            // expected
        }

        try {
            descriptor.getHostname();
            fail("Does not throw InstanceRemovedException");
        } catch (InstanceRemovedException ex) {
            // expected
        }

        try {
            descriptor.isLocal();
            fail("Does not throw InstanceRemovedException");
        } catch (InstanceRemovedException ex) {
            // expected
        }
    }
}
