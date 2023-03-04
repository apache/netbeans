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

package org.netbeans.modules.j2ee.deployment.devmodules.spi;

import org.netbeans.junit.NbTestCase;

/**
 *
 * @author sherold
 */
public class J2eeModuleProviderTest extends NbTestCase {
    
    /** Creates a new instance of J2eeModuleProviderTest */
    public J2eeModuleProviderTest(String testName) {
        super(testName);
    }
    
    public void testCreateJ2eeModule() {
        try {
            J2eeModuleProvider.class.getDeclaredMethod("resetConfigSupport");
        } catch (NoSuchMethodException e) {
            fail("J2eeModuleProvider.resetConfigSupport() method is missing! See the issue #109507. " +
                    "Do not remove the method unless the reporter of the issue agrees!");
        }
    }
}
