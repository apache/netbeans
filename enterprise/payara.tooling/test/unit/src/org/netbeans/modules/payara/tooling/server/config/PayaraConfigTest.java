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

import org.netbeans.modules.payara.tooling.CommonTest;
import org.netbeans.modules.payara.tooling.admin.CommandHttpTest;
import org.netbeans.modules.payara.tooling.admin.CommandRestTest;
import org.netbeans.modules.payara.tooling.data.PayaraConfig;
import org.netbeans.modules.payara.tooling.data.PayaraServer;
import org.netbeans.modules.payara.tooling.data.ToolsConfig;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author Peter Benedikovic, Tomas Kraus
 */
public class PayaraConfigTest extends CommonTest {

    PayaraServer serverHttp = CommandHttpTest.payaraServer();
    PayaraServer serverRest = CommandRestTest.payaraServer();

    @Test
    public void testPayaraConfigXMLTools() {
        // Payara configured for HTTP (v3)
        PayaraConfig config = PayaraConfigManager.getConfig(
                ConfigBuilderProvider.getBuilderConfig(
                serverHttp.getVersion()));
        ToolsConfig toolsConfig = config.getTools();
        assertNotNull(toolsConfig);
        assertNotNull(toolsConfig.getAsadmin());
        // Payara configured for REST (v4)
        config = PayaraConfigManager.getConfig(
                ConfigBuilderProvider.getBuilderConfig(
                serverRest.getVersion()));
        toolsConfig = config.getTools();
        assertNotNull(toolsConfig);
        assertNotNull(toolsConfig.getAsadmin());
    }
    
}
