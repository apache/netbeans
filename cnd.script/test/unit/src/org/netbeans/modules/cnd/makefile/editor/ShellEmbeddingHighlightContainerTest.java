/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
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
