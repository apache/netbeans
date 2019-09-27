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
import java.util.Map;
import org.netbeans.modules.payara.tooling.server.parser.HttpData;
import org.netbeans.modules.payara.tooling.server.parser.HttpListenerReader;
import org.netbeans.modules.payara.tooling.server.parser.NetworkListenerReader;
import org.netbeans.modules.payara.tooling.server.parser.TargetConfigNameReader;
import org.netbeans.modules.payara.tooling.server.parser.TreeParser;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.Test;

/**
 *
 * @author Peter Benedikovic, Tomas Kraus
 */
@Test(groups = {"unit-tests"})
public class DomainConfigReadersTest {

    private static final String DOMAIN_CONFIG_FILE = System.getProperty("user.dir")
            + "/src/test/java/org/netbeans/modules/payara/tooling/server/config/domain.xml";

    @Test
    public void testReadAdminPort() {
        File domainXML = new File(DOMAIN_CONFIG_FILE);
        TargetConfigNameReader configNameReader = new TargetConfigNameReader();
        TreeParser.readXml(domainXML, configNameReader);
        String targetConfigName = configNameReader.getTargetConfigName();
        HttpListenerReader httpReader = new HttpListenerReader(targetConfigName);
        NetworkListenerReader networkReader = new NetworkListenerReader(targetConfigName);
        TreeParser.readXml(domainXML, httpReader, networkReader);
        Map<String, HttpData> result = httpReader.getResult();
        result.putAll(networkReader.getResult());
        HttpData adminData = result.get("admin-listener");
        assertTrue(adminData.getPort() == 4848);
    }

}
