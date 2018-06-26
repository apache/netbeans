/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved.
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
