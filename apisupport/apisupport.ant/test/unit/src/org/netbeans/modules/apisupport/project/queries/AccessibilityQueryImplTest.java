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

package org.netbeans.modules.apisupport.project.queries;

import org.netbeans.api.java.queries.AccessibilityQuery;
import org.netbeans.modules.apisupport.project.TestBase;

/**
 * Test {@link AccessibilityQueryImpl}.
 * @author Jesse Glick
 */
public class AccessibilityQueryImplTest extends TestBase {

    public AccessibilityQueryImplTest(String name) {
        super(name);
    }

    public void testPublicPackages() throws Exception {
        assertEquals(Boolean.TRUE, AccessibilityQuery.isPubliclyAccessible(nbRoot().getFileObject("ide/project.ant/src/org/netbeans/spi/project/support/ant")));
        assertEquals(Boolean.FALSE, AccessibilityQuery.isPubliclyAccessible(nbRoot().getFileObject("ide/project.ant/src/org/netbeans/modules/project/ant")));
    }
    
    public void testFriendPackages() throws Exception {
        assertEquals(Boolean.TRUE, AccessibilityQuery.isPubliclyAccessible(nbRoot().getFileObject("java/ant.freeform/src/org/netbeans/modules/ant/freeform/spi")));
        assertEquals(Boolean.FALSE, AccessibilityQuery.isPubliclyAccessible(nbRoot().getFileObject("java/ant.freeform/src/org/netbeans/modules/ant/freeform")));
    }
    
    // XXX testSubpackages - would need to generate a new module to test

    public void testTestRoots() throws Exception {
        assertEquals(null, AccessibilityQuery.isPubliclyAccessible(nbRoot().getFileObject("ide/project.ant/test/unit/src/org/netbeans/api/project/ant")));
        assertEquals(null, AccessibilityQuery.isPubliclyAccessible(nbRoot().getFileObject("enterprise/j2ee.kit/test/qa-functional/src/org/netbeans/test/j2ee")));
    }
    
    public void testOtherSourceRoots() throws Exception {
        assertEquals(null, AccessibilityQuery.isPubliclyAccessible(nbRoot().getFileObject("extide/o.apache.tools.ant.module/src-bridge/org/apache/tools/ant/module/bridge/impl")));
    }
    
}
