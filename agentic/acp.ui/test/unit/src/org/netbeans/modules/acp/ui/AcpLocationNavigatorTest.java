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
package org.netbeans.modules.acp.ui;

import junit.framework.TestCase;

public class AcpLocationNavigatorTest extends TestCase {

    public void testParsesOptionalOneBasedLine() {
        assertEquals(new AcpLocationNavigator.Location("/work/Some File.java", 12),
                AcpLocationNavigator.parseNavUrl("acp-nav:/work/Some File.java:12"));
        assertEquals(new AcpLocationNavigator.Location("src/C.java", null),
                AcpLocationNavigator.parseNavUrl("acp-nav:src/C.java"));
    }

    public void testPreservesWindowsDriveAndNonLineColons() {
        assertEquals(new AcpLocationNavigator.Location("C:\\src\\C.java", 4),
                AcpLocationNavigator.parseNavUrl("acp-nav:C:\\src\\C.java:4"));
        assertEquals(new AcpLocationNavigator.Location("/work/file:note", null),
                AcpLocationNavigator.parseNavUrl("acp-nav:/work/file:note"));
        assertEquals(new AcpLocationNavigator.Location("file:0", null),
                AcpLocationNavigator.parseNavUrl("acp-nav:file:0"));
    }

    public void testIgnoresOtherAndEmptyLinks() {
        assertNull(AcpLocationNavigator.parseNavUrl(null));
        assertNull(AcpLocationNavigator.parseNavUrl("acp-diff:diff-2"));
        assertNull(AcpLocationNavigator.parseNavUrl("acp-nav:"));
    }
}
