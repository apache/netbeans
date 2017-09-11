/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
