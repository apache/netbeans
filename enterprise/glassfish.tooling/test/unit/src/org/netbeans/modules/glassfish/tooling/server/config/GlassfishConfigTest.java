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
package org.netbeans.modules.glassfish.tooling.server.config;

import org.netbeans.modules.glassfish.tooling.CommonTest;
import org.netbeans.modules.glassfish.tooling.admin.CommandHttpTest;
import org.netbeans.modules.glassfish.tooling.admin.CommandRestTest;
import org.netbeans.modules.glassfish.tooling.data.GlassFishConfig;
import org.netbeans.modules.glassfish.tooling.data.GlassFishServer;
import org.netbeans.modules.glassfish.tooling.data.ToolsConfig;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author Peter Benedikovic, Tomas Kraus
 */
public class GlassfishConfigTest extends CommonTest {

    GlassFishServer serverHttp = CommandHttpTest.glassFishServer();
    GlassFishServer serverRest = CommandRestTest.glassFishServer();

    @Test
    public void testGlassfishConfigXMLTools() {
        // GlassFish configured for HTTP (v3)
        GlassFishConfig config = GlassFishConfigManager.getConfig(
                ConfigBuilderProvider.getBuilderConfig(
                serverHttp.getVersion()));
        ToolsConfig toolsConfig = config.getTools();
        assertNotNull(toolsConfig);
        assertNotNull(toolsConfig.getAsadmin());
        // GlassFish configured for REST (v4)
        config = GlassFishConfigManager.getConfig(
                ConfigBuilderProvider.getBuilderConfig(
                serverRest.getVersion()));
        toolsConfig = config.getTools();
        assertNotNull(toolsConfig);
        assertNotNull(toolsConfig.getAsadmin());
    }
    
}
