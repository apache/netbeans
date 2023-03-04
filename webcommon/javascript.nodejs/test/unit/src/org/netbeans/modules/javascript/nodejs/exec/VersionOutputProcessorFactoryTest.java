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
package org.netbeans.modules.javascript.nodejs.exec;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.modules.javascript.nodejs.exec.NodeExecutable.VersionOutputProcessorFactory;

public class VersionOutputProcessorFactoryTest {

    private VersionOutputProcessorFactory versionFactory;

    @Before
    public void setUp() {
        versionFactory = new VersionOutputProcessorFactory();
    }

    @Test
    public void testValidVersion() {
        Assert.assertEquals("0.10.31", versionFactory.parseVersion("v0.10.31"));
        Assert.assertEquals("0.10.31.1258.2145", versionFactory.parseVersion("v0.10.31.1258.2145"));
    }

    @Test
    public void testInvalidVersion() {
        Assert.assertNull(versionFactory.parseVersion("v0.10.31a"));
        Assert.assertNull(versionFactory.parseVersion("v 0.10.31"));
    }

}
