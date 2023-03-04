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
package org.netbeans;

import java.security.Permission;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
public class TopSecurityManagerReplaceTest extends NbTestCase {

    public TopSecurityManagerReplaceTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        System.setProperty("netbeans.security.nocheck", "true");
        assertNull("No manager yet", System.getSecurityManager());
        TopSecurityManager.install();
        assertTrue("Installed OK", System.getSecurityManager() instanceof TopSecurityManager);
    }
    
    public void testReplaceSecurityManager() {
        MySM sm = new MySM();
        System.setSecurityManager(sm);
        assertEquals("Replaced OK", sm, System.getSecurityManager());
    }
    
    private static final class MySM extends SecurityManager {
        @Override
        public void checkPermission(Permission perm) {
        }
    }
}
