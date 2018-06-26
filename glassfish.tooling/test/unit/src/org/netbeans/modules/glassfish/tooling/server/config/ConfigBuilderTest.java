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
package org.netbeans.modules.glassfish.tooling.server.config;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import org.netbeans.modules.glassfish.tooling.CommonTest;
import org.netbeans.modules.glassfish.tooling.admin.CommandHttpTest;
import org.netbeans.modules.glassfish.tooling.admin.CommandRestTest;
import org.netbeans.modules.glassfish.tooling.data.GlassFishLibrary;
import org.netbeans.modules.glassfish.tooling.data.GlassFishServer;
import org.netbeans.modules.glassfish.tooling.data.GlassFishVersion;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;
import org.testng.annotations.Test;

/**
 *
 * @author Peter Benedikovic, Tomas Kraus
 */
@Test(groups = {"unit-tests"})
public class ConfigBuilderTest extends CommonTest {

    public static final String PATH = "file://"
            + System.getProperty("user.dir")
            + "/src/main/java/org/netbeans/modules/glassfish/tooling/server/config/";

    /** Library builder configuration. */
    private static Config libraryConfig;

    // Initialize ConfigBuilder class
    static {
        try {
            URL configFileV3 = new URL(PATH + "GlassFishV3.xml");
            URL configFileV4 = new URL(PATH + "GlassFishV4.xml");
            libraryConfig = new Config(configFileV3,
                    new Config.Next(
                    GlassFishVersion.GF_4, configFileV4));
        } catch (MalformedURLException mue) {
            fail("Cannot initialize library builder");
        }
    }

    /**
     * Test library builder with GlassFish v3.
     */
    @Test
    public void testLibraryBuilderGFv3() {
        GlassFishServer server = CommandHttpTest.glassFishServer();
        File home = new File(server.getServerHome());
        ConfigBuilder lb = new ConfigBuilder(libraryConfig, home, home, home);
        List<GlassFishLibrary> lib = lb.getLibraries(
                GlassFishVersion.GF_3_1_2_2);
        assertNotNull(lib);
        try {
            lb.getLibraries(GlassFishVersion.GF_3_1_2);
            fail("Library builder could not work for more than one GlassFish"
                    + " version with a single server instance.");
        } catch (ServerConfigException sce) {}
    }

    /**
     * Test library builder with GlassFish v4.
     */
    @Test
    public void testLibraryBuilderGFv4() {
        GlassFishServer server = CommandRestTest.glassFishServer();
        File home = new File(server.getServerHome());
        ConfigBuilder lb = new ConfigBuilder(libraryConfig, home, home, home);
        List<GlassFishLibrary> lib = lb.getLibraries(
                GlassFishVersion.GF_4);
        assertNotNull(lib);
        try {
            lb.getLibraries(GlassFishVersion.GF_3_1_2);
            fail("Library builder could not work for more than one GlassFish"
                    + " version with a single server instance.");
        } catch (ServerConfigException sce) {}
    }

}
