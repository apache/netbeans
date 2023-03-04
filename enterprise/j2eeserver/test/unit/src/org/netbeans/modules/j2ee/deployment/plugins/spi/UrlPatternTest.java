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

package org.netbeans.modules.j2ee.deployment.plugins.spi;

import org.netbeans.modules.j2ee.deployment.impl.ServerRegistry;
import org.netbeans.modules.j2ee.deployment.impl.ServerRegistryTestBase;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceCreationException;
import org.netbeans.modules.j2ee.deployment.plugins.api.InstanceProperties;

/**
 *
 * @author Petr Hejl
 */
public class UrlPatternTest extends ServerRegistryTestBase {

    private static final String TEST_URL_PREFIX_PERMITTED = "permittedfooservice:";

    private static final String TEST_URL_PREFIX_FORBIDDEN = "forbiddenfooservice:";

    public UrlPatternTest(String name) {
        super(name);
    }

    public void testPermittedPattern() throws InstanceCreationException {
        InstanceProperties.createInstanceProperties(
                TEST_URL_PREFIX_PERMITTED + "testPermitted", "test", "password", "Permitted instance");
    }

    public void testForbiddenPattern() throws InstanceCreationException {
        try {
        InstanceProperties.createInstanceProperties(
                TEST_URL_PREFIX_FORBIDDEN + "testForbidden", "test", "password", "Forbidden instance");
        fail("Forbidden instance registered");
        } catch (InstanceCreationException ex) {
            // expected
        }
    }
}
