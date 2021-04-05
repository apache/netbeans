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
package org.netbeans.modules.payara.tooling.server.config;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import org.netbeans.modules.payara.tooling.CommonTest;
import org.netbeans.modules.payara.tooling.admin.CommandHttpTest;
import org.netbeans.modules.payara.tooling.admin.CommandRestTest;
import org.netbeans.modules.payara.tooling.data.PayaraLibrary;
import org.netbeans.modules.payara.tooling.data.PayaraServer;
import org.netbeans.modules.payara.tooling.data.PayaraVersion;
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
            + "/src/main/java/org/netbeans/modules/payara/tooling/server/config/";

    /** Library builder configuration. */
    private static Config libraryConfig;

    // Initialize ConfigBuilder class
    static {
        try {
            URL configFileV3 = new URL(PATH + "PayaraV3.xml");
            URL configFileV4 = new URL(PATH + "PayaraV4.xml");
            libraryConfig = new Config(configFileV3,
                    new Config.Next(
                    PayaraVersion.PF_4_1_144, configFileV4));
        } catch (MalformedURLException mue) {
            fail("Cannot initialize library builder");
        }
    }

    /**
     * Test library builder with Payara.
     */
    @Test
    public void testLibraryBuilderPF() {
        PayaraServer server = CommandHttpTest.payaraServer();
        File home = new File(server.getServerHome());
        ConfigBuilder lb = new ConfigBuilder(libraryConfig, home, home, home);
        List<PayaraLibrary> lib = lb.getLibraries(
                PayaraVersion.PF_4_1_144);
        assertNotNull(lib);
        try {
            lb.getLibraries(PayaraVersion.PF_4_1_151);
            fail("Library builder could not work for more than one Payara"
                    + " version with a single server instance.");
        } catch (ServerConfigException sce) {}
    }

    /**
     * Test library builder with Payara v4.
     */
    @Test
    public void testLibraryBuilderGFv4() {
        PayaraServer server = CommandRestTest.payaraServer();
        File home = new File(server.getServerHome());
        ConfigBuilder lb = new ConfigBuilder(libraryConfig, home, home, home);
        List<PayaraLibrary> lib = lb.getLibraries(
                PayaraVersion.PF_4_1_144);
        assertNotNull(lib);
        try {
            lb.getLibraries(PayaraVersion.PF_4_1_151);
            fail("Library builder could not work for more than one Payara"
                    + " version with a single server instance.");
        } catch (ServerConfigException sce) {}
    }

}
