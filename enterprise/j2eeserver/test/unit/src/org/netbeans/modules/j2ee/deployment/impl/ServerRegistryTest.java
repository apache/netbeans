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

import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.exceptions.DeploymentManagerCreationException;
import javax.enterprise.deploy.spi.factories.DeploymentFactory;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.impl.ui.RegistryNodeProvider;
import org.netbeans.modules.j2ee.deployment.plugins.spi.IncrementalDeployment;
import org.netbeans.modules.j2ee.deployment.plugins.spi.OptionalDeploymentManagerFactory;
import org.netbeans.modules.j2ee.deployment.plugins.spi.StartServer;
import org.netbeans.tests.j2eeserver.plugin.ManagerWrapperFactory;
/**
 *
 * @author nn136682
 */
public class ServerRegistryTest extends ServerRegistryTestBase {
    
    public ServerRegistryTest(String testName) {
        super(testName);
    }
    
    /** 
     * Test plugin layer file which install 1 plugin instance.
     * @precondition: test plugin is installed
     * @postcondition: getServer("Test") to get testplugin 
     * @postcondition: getInstance("fooservice") to get testplugin instance
     */
    public void testPluginLayerFile() {
        ServerRegistry registry = ServerRegistry.getInstance();
        System.out.println ("registry:" + registry);
        Server testPlugin = registry.getServer("Test");
        if (testPlugin == null || ! testPlugin.getShortName().equals("Test"))
            fail("Could not get testPlugin: "+testPlugin);
        
        DeploymentFactory factory = testPlugin.getDeploymentFactory();
        assertNotNull ("No DeploymentFactory for test plugin", factory);
        
        RegistryNodeProvider nodeProvider = testPlugin.getNodeProvider();
        assertNotNull ("No RegistryNodeProvider for test plugin", nodeProvider);
        
        OptionalDeploymentManagerFactory optionalFactory = testPlugin.getOptionalFactory();
        assertNotNull ("No OptionalDeploymentManagerFactory for test plugin", optionalFactory);
        
        DeploymentManager manager = null;
        try {
            manager = testPlugin.getDisconnectedDeploymentManager();
            assertNotNull ("No DeploymentManager for test plugin", manager);
        } catch (DeploymentManagerCreationException dce) {
            fail(dce.getLocalizedMessage());
        }
        
        IncrementalDeployment incrementalDepl = optionalFactory.getIncrementalDeployment(manager);
        assertNotNull ("No IncrementalDeployment for test plugin", incrementalDepl);
        
        StartServer start = optionalFactory.getStartServer(manager);
        assertNotNull ("No StartServer for test plugin", start);
        
        String url = "fooservice";
        ServerInstance instance = registry.getServerInstance(url);
        if (instance == null || ! instance.getUrl().equals(url)) {
            fail("Failed: expected: " + url + " got: " + instance);
        }
    }
    
    public void testDeploymentFileNames() {
        ServerRegistry registry = ServerRegistry.getInstance();
        Server testPlugin = registry.getServer("Test");
        if (testPlugin == null || ! testPlugin.getShortName().equals("Test")) {
            fail("Could not get testPlugin: "+testPlugin);
        }
        
        String[] names = testPlugin.getDeploymentPlanFiles(J2eeModule.Type.WAR);
        assertEquals(1, names.length);
        assertEquals("WEB-INF/test-web.xml", names[0]);

        names = testPlugin.getDeploymentPlanFiles(J2eeModule.Type.EAR);
        assertEquals(1, names.length);
        assertEquals("META-INF/test-app.xml", names[0]);

        names = testPlugin.getDeploymentPlanFiles(J2eeModule.Type.CAR);
        assertEquals(1, names.length);
        assertEquals("META-INF/test-client.xml", names[0]);

        names = testPlugin.getDeploymentPlanFiles(J2eeModule.Type.EJB);
        assertEquals(1, names.length);
        assertEquals("META-INF/test-ejb.xml", names[0]);
    }

    public void testServerPluginInitialization() {
        ServerRegistry registry = ServerRegistry.getInstance();
        Server testPlugin = registry.getServer("Test");

        assertNotNull("Registry does not contain test plugin", testPlugin);

        ManagerWrapperFactory optionalFactory = (ManagerWrapperFactory) testPlugin.getOptionalFactory();
        assertTrue(optionalFactory.isInitialized());

        testPlugin = registry.getServer("TestFailingInitialization");
        assertNull("Registry contain plugin while its initialization failed", testPlugin);
    }
    
}
