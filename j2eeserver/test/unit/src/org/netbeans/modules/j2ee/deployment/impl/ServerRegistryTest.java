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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
