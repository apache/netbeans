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
package org.netbeans.modules.cnd.makefile.editor;

import java.util.List;
import java.util.Arrays;
import javax.swing.text.Position;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.netbeans.modules.cnd.makefile.editor.ShellEmbeddingHighlightContainer.*;

/**
 */
public class ShellEmbeddingHighlightContainerTest {

    @Test
    public void testGetChangeInterval1() {
        List<HighlightItem> highlights = Arrays.asList(hi(0, 1, "a"), hi(1, 2, "b"), hi(2, 3, "c"));
        assertNull(changedInterval(highlights, highlights));

        assertArrayEquals(new int[] {1, 2}, changedInterval(
                highlights,
                Arrays.asList(hi(0, 1, "a"), hi(1, 2, "c"), hi(2, 3, "c"))));

        assertArrayEquals(new int[] {2, Integer.MAX_VALUE}, changedInterval(
                highlights,
                Arrays.asList(hi(0, 1, "a"), hi(1, 2, "b"), hi(2, 3, "d"))));

        assertArrayEquals(new int[] {0, 1}, changedInterval(
                highlights,
                Arrays.asList(hi(0, 1, "z"), hi(1, 2, "b"), hi(2, 3, "c"))));
    }

    @Test
    public void testFirstOverlap() {
        List<HighlightItem> highlights = Arrays.asList(hi(0, 2, "a"), hi(2, 4, "b"), hi(4, 6, "c"));
        assertEquals(0, firstOverlap(highlights, -1));
        assertEquals(0, firstOverlap(highlights, 0));
        assertEquals(0, firstOverlap(highlights, 1));
        assertEquals(1, firstOverlap(highlights, 2));
        assertEquals(1, firstOverlap(highlights, 3));
        assertEquals(2, firstOverlap(highlights, 4));
        assertEquals(2, firstOverlap(highlights, 5));
        assertEquals(3, firstOverlap(highlights, 6));
        assertEquals(3, firstOverlap(highlights, 7));
    }

    @Test
    public void testLastOverlap() {
        List<HighlightItem> highlights = Arrays.asList(hi(0, 2, "a"), hi(2, 4, "b"), hi(4, 6, "c"));
        assertEquals(-1, lastOverlap(highlights, -1));
        assertEquals(-1, lastOverlap(highlights, 0));
        assertEquals(0, lastOverlap(highlights, 1));
        assertEquals(0, lastOverlap(highlights, 2));
        assertEquals(1, lastOverlap(highlights, 3));
        assertEquals(1, lastOverlap(highlights, 4));
        assertEquals(2, lastOverlap(highlights, 5));
        assertEquals(2, lastOverlap(highlights, 6));
        assertEquals(2, lastOverlap(highlights, 7));
    }

    private static HighlightItem hi(int start, int end, String category) {
        return new HighlightItem(new PosImpl(start), new PosImpl(end), category);
    }

    private static class PosImpl implements Position {

        private final int offset;

        public PosImpl(int offset) {
            this.offset = offset;
        }

        @Override
        public int getOffset() {
            return offset;
        }
    }
}
