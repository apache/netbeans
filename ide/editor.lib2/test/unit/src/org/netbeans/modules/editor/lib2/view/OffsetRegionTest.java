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

package org.netbeans.modules.editor.lib2.view;

import javax.swing.text.Document;
import javax.swing.text.PlainDocument;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Miloslav Metelka
 */
public class OffsetRegionTest extends NbTestCase {
    
    public OffsetRegionTest(String name) {
        super(name);
    }
    
    public void testUnionAndIntersection() throws Exception {
        Document doc = new PlainDocument();
        doc.insertString(0, "abcdefghij", null);
        OffsetRegion empty00 = OffsetRegion.create(doc, 0, 0);
        assertTrue(empty00.isEmpty());
        OffsetRegion empty55 = OffsetRegion.create(doc, 5, 5);
        assertTrue(empty55.isEmpty());
        OffsetRegion empty99 = OffsetRegion.create(doc, 9, 9);
        assertTrue(empty99.isEmpty());
        try {
            OffsetRegion i = OffsetRegion.create(doc, 5, 1);
            fail("Creation succeeded"); // NOI18N
        } catch (IllegalArgumentException ex) {
            // Expected
        }
        OffsetRegion r1 = OffsetRegion.create(doc, 5, 9);
        OffsetRegion r2 = OffsetRegion.create(doc, 1, 5);
        OffsetRegion r21 = OffsetRegion.create(doc, 1, 9);
        OffsetRegion r3 = OffsetRegion.create(doc, 3, 7);
        OffsetRegion r4 = OffsetRegion.create(doc, 5, 6);
        OffsetRegion r = r1.union(r2, false);
        assertRegion(r, 1, 9);
        assertEquals(r21, r1.union(r2, false));
        assertEquals(r21, r1.union(doc, 1, 5, false));
        assertEquals(r21, r2.union(r1, false));
        assertEquals(r21, r2.union(doc, 5, 9, false));
        assertSame(r3, r3.union(r3, false));
        assertSame(r3, r3.union(doc, 3, 7, false));
        assertSame(r3, r3.union(r4, false));
        assertSame(r3, r3.union(doc, 5, 6, false));
        // Union with empty
        assertSame(r2, r2.union(empty99, true));
        assertSame(r2, r2.union(doc, 9, 9, true));
        assertSame(r2, empty99.union(r2, true));
        assertEquals(r21, r2.union(empty99, false));
        assertEquals(r21, r2.union(doc, 9, 9, false));
        assertEquals(r21, empty99.union(r2, false));
                
        assertSame(null, r1.intersection(doc, 1, 4, true));
        assertEquals(empty55, r1.intersection(doc, 1, 4, false)); // implementation-dependent
        assertEquals(empty55, r1.intersection(doc, 5, 5, true)); // implementation-dependent
        assertEquals(empty55, r1.intersection(doc, 5, 5, false)); // implementation-dependent
        assertSame(null, r1.intersection(doc, 10, 12, true));
        assertEquals(empty99, r1.intersection(doc, 10, 12, false));
        assertSame(r1, r1.intersection(doc, 5, 9, true));
        assertSame(r1, r1.intersection(r1, true));
    }
    
    private static void assertRegion(OffsetRegion r, int startOffset, int endOffset) {
        assertEquals("Invalid startOffset", startOffset, r.startOffset());
        assertEquals("Invalid endOffset", endOffset, r.endOffset());
    }

}
