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
package org.netbeans.modules.j2ee.deployment.plugins.api;

import java.util.Collections;
import org.netbeans.api.server.ServerInstance;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistryTestBase;
import org.openide.util.Lookup;

/**
 *
 * @author Petr Hejl
 */
public class CommonServerBridgeTest extends ServerRegistryTestBase {

    private static final String TEST_URL_PREFIX = "fooservice:";

    public CommonServerBridgeTest(String name) {
        super(name);
    }

    public void testCommonInstance() throws InstanceCreationException {
        String url = TEST_URL_PREFIX + "testCommonInstance";
        InstanceProperties.createInstanceProperties(url, "test", "test", "TestCommon");

        ServerInstance common = CommonServerBridge.getCommonInstance(url);
        assertNotNull(common);

        Lookup lookup = common.getLookup();
        assertNotNull(lookup.lookup(J2eePlatform.class));
        assertNotNull(lookup.lookup(InstanceProperties.class));
    }

    public void testNoCommonInstance() throws InstanceCreationException {
        String url = TEST_URL_PREFIX + "testNoCommonInstance";
        InstanceProperties.createInstancePropertiesWithoutUI(url, "test", "test", "TestNoCommon", Collections.<String, String>emptyMap());

        try {
            CommonServerBridge.getCommonInstance(url);
            fail("Common instance found for EE instance registered without UI");
        } catch (IllegalStateException ex) {
            // expected
        }
    }
}
