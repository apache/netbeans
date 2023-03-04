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

package org.netbeans.modules.j2ee.deployment.devmodules.api;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistryTestBase;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;
import org.netbeans.tests.j2eeserver.plugin.jsr88.TestDeploymentManager;

/**
 *
 * @author Petr Hejl
 */
public class J2eePlatformTest extends ServerRegistryTestBase {

    private static final String TEST_URL = "fooservice:j2eePlatformTest";

    private static final String TEST_USERNAME = "username";

    private static final String TEST_PASSWORD = "password";

    private static final String TEST_DISPLAY_NAME = "j2eePlatformTest";

    public J2eePlatformTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        Map<String, String> props = new HashMap<String, String>();
        props.put(TestDeploymentManager.PLATFORM_ROOT_PROPERTY, getWorkDirPath());

        InstanceProperties.createInstanceProperties(TEST_URL,
                TEST_USERNAME, TEST_PASSWORD, TEST_DISPLAY_NAME);
    }

    public void testLookup() {
        J2eePlatform platform = Deployment.getDefault().getJ2eePlatform(TEST_URL);
        assertNotNull(platform.getLookup());
    }
}
