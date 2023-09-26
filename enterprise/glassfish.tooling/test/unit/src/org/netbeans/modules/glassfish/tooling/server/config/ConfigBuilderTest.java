/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
            lb.getLibraries(GlassFishVersion.GF_4);
            fail("Library builder could not work for more than one GlassFish"
                    + " version with a single server instance.");
        } catch (ServerConfigException sce) {}
    }

}
