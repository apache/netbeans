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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
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
