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

import java.util.List;
import org.netbeans.junit.NbTestCase;

public class AcpToolCallTest extends NbTestCase {

    public AcpToolCallTest(String name) {
        super(name);
    }

    public void testBuilderDefaults() {
        AcpToolCall tc = new AcpToolCall.Builder("tc-1").build();

        assertEquals("tc-1", tc.getToolCallId());
        assertEquals("", tc.getTitle());
        assertNull(tc.getName());
        assertEquals("other", tc.getKind());
        assertEquals(AcpToolCall.STATUS_PENDING, tc.getStatus());
        assertTrue(tc.getDiffs().isEmpty());
        assertTrue(tc.getLocations().isEmpty());
        assertNull(tc.getTextContent());
    }

    public void testBuilderAllFields() {
        AcpToolCall.DiffContent diff = new AcpToolCall.DiffContent("/a.txt", "old", "new");
        AcpToolCall.Location loc = new AcpToolCall.Location("/b.java", 42);

        AcpToolCall tc = new AcpToolCall.Builder("tc-2")
                .title("Write file")
                .name("write")
                .kind("file")
                .status(AcpToolCall.STATUS_COMPLETED)
                .diffs(List.of(diff))
                .locations(List.of(loc))
                .textContent("some output")
                .build();

        assertEquals("tc-2", tc.getToolCallId());
        assertEquals("Write file", tc.getTitle());
        assertEquals("write", tc.getName());
        assertEquals("file", tc.getKind());
        assertEquals(AcpToolCall.STATUS_COMPLETED, tc.getStatus());
        assertEquals(1, tc.getDiffs().size());
        assertEquals("/a.txt", tc.getDiffs().get(0).path());
        assertEquals("old", tc.getDiffs().get(0).oldText());
        assertEquals("new", tc.getDiffs().get(0).newText());
        assertEquals(1, tc.getLocations().size());
        assertEquals("/b.java", tc.getLocations().get(0).path());
        assertEquals(Integer.valueOf(42), tc.getLocations().get(0).line());
        assertEquals("some output", tc.getTextContent());
    }

    public void testNullToolCallIdThrows() {
        try {
            new AcpToolCall.Builder(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException ex) {
            // expected
        }
    }

    public void testNullTitleThrows() {
        try {
            new AcpToolCall.Builder("tc-3").title(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException ex) {
            // expected
        }
    }

    public void testNullKindThrows() {
        try {
            new AcpToolCall.Builder("tc-4").kind(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException ex) {
            // expected
        }
    }

    public void testNullStatusThrows() {
        try {
            new AcpToolCall.Builder("tc-5").status(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException ex) {
            // expected
        }
    }

    public void testNullDiffsThrows() {
        try {
            new AcpToolCall.Builder("tc-6").diffs(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException ex) {
            // expected
        }
    }

    public void testNullLocationsThrows() {
        try {
            new AcpToolCall.Builder("tc-7").locations(null);
            fail("Expected NullPointerException");
        } catch (NullPointerException ex) {
            // expected
        }
    }

    public void testDiffsAreImmutable() {
        AcpToolCall tc = new AcpToolCall.Builder("tc-8")
                .diffs(List.of(new AcpToolCall.DiffContent("p", "a", "b")))
                .build();
        try {
            tc.getDiffs().add(new AcpToolCall.DiffContent("q", "c", "d"));
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException ex) {
            // expected
        }
    }

    public void testLocationsAreImmutable() {
        AcpToolCall tc = new AcpToolCall.Builder("tc-9")
                .locations(List.of(new AcpToolCall.Location("/f", 1)))
                .build();
        try {
            tc.getLocations().add(new AcpToolCall.Location("/g", 2));
            fail("Expected UnsupportedOperationException");
        } catch (UnsupportedOperationException ex) {
            // expected
        }
    }

    public void testNameAllowsNull() {
        AcpToolCall tc = new AcpToolCall.Builder("tc-10")
                .name(null)
                .build();
        assertNull(tc.getName());
    }

    public void testTextContentAllowsNull() {
        AcpToolCall tc = new AcpToolCall.Builder("tc-11")
                .textContent(null)
                .build();
        assertNull(tc.getTextContent());
    }

    public void testToString() {
        AcpToolCall tc = new AcpToolCall.Builder("tc-12")
                .title("Run tests")
                .kind("command")
                .status(AcpToolCall.STATUS_IN_PROGRESS)
                .build();
        String str = tc.toString();
        assertTrue(str.contains("tc-12"));
        assertTrue(str.contains("Run tests"));
        assertTrue(str.contains("command"));
        assertTrue(str.contains("in_progress"));
    }

    public void testStatusConstants() {
        assertEquals("pending", AcpToolCall.STATUS_PENDING);
        assertEquals("in_progress", AcpToolCall.STATUS_IN_PROGRESS);
        assertEquals("completed", AcpToolCall.STATUS_COMPLETED);
        assertEquals("failed", AcpToolCall.STATUS_FAILED);
    }

    public void testDiffContentRecord() {
        AcpToolCall.DiffContent d1 = new AcpToolCall.DiffContent("/a", "old", "new");
        AcpToolCall.DiffContent d2 = new AcpToolCall.DiffContent("/a", "old", "new");
        AcpToolCall.DiffContent d3 = new AcpToolCall.DiffContent("/b", "old", "new");

        assertEquals(d1, d2);
        assertEquals(d1.hashCode(), d2.hashCode());
        assertFalse(d1.equals(d3));
    }

    public void testLocationRecord() {
        AcpToolCall.Location l1 = new AcpToolCall.Location("/f", 10);
        AcpToolCall.Location l2 = new AcpToolCall.Location("/f", 10);
        AcpToolCall.Location l3 = new AcpToolCall.Location("/f", 20);

        assertEquals(l1, l2);
        assertEquals(l1.hashCode(), l2.hashCode());
        assertFalse(l1.equals(l3));
    }

    public void testLocationNullLine() {
        AcpToolCall.Location loc = new AcpToolCall.Location("/f", null);
        assertNull(loc.line());
    }
}
