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
