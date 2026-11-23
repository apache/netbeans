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
package org.netbeans.modules.acp.api;

import org.netbeans.junit.NbTestCase;

public class AcpPlanEntryTest extends NbTestCase {

    public AcpPlanEntryTest(String name) {
        super(name);
    }

    public void testBasicCreation() {
        AcpPlanEntry entry = new AcpPlanEntry("Do something", AcpPlanEntry.STATUS_PENDING);
        assertEquals("Do something", entry.content());
        assertEquals(AcpPlanEntry.STATUS_PENDING, entry.status());
    }

    public void testAllStatusValues() {
        AcpPlanEntry pending = new AcpPlanEntry("step", AcpPlanEntry.STATUS_PENDING);
        assertEquals("pending", pending.status());

        AcpPlanEntry inProgress = new AcpPlanEntry("step", AcpPlanEntry.STATUS_IN_PROGRESS);
        assertEquals("in_progress", inProgress.status());

        AcpPlanEntry completed = new AcpPlanEntry("step", AcpPlanEntry.STATUS_COMPLETED);
        assertEquals("completed", completed.status());
    }

    public void testNullContentThrows() {
        try {
            new AcpPlanEntry(null, AcpPlanEntry.STATUS_PENDING);
            fail("Expected NullPointerException");
        } catch (NullPointerException ex) {
            // expected
        }
    }

    public void testNullStatusThrows() {
        try {
            new AcpPlanEntry("content", null);
            fail("Expected NullPointerException");
        } catch (NullPointerException ex) {
            // expected
        }
    }

    public void testRecordEquality() {
        AcpPlanEntry a = new AcpPlanEntry("step1", AcpPlanEntry.STATUS_PENDING);
        AcpPlanEntry b = new AcpPlanEntry("step1", AcpPlanEntry.STATUS_PENDING);
        AcpPlanEntry c = new AcpPlanEntry("step1", AcpPlanEntry.STATUS_COMPLETED);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
        assertFalse(a.equals(c));
    }

    public void testToStringContainsFields() {
        AcpPlanEntry entry = new AcpPlanEntry("do X", "pending");
        String str = entry.toString();
        assertTrue(str.contains("do X"));
        assertTrue(str.contains("pending"));
    }
}
