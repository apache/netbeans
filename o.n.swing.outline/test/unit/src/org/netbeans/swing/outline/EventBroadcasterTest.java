/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.swing.outline;

import java.util.Arrays;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author Martin Entlicher
 */
public class EventBroadcasterTest extends NbTestCase {
    
    public EventBroadcasterTest(String name) {
        super(name);
    }
    
    public void testIsDiscontiguous() throws Exception {
        assertFalse(EventBroadcaster.isDiscontiguous(null));
        assertFalse(EventBroadcaster.isDiscontiguous(new int[] {}));
        assertFalse(EventBroadcaster.isDiscontiguous(new int[] {2}));
        assertFalse(EventBroadcaster.isDiscontiguous(new int[] {3, 4}));
        assertFalse(EventBroadcaster.isDiscontiguous(new int[] {1001, 1002, 1003}));
        assertTrue(EventBroadcaster.isDiscontiguous(new int[] {5, 7}));
        assertTrue(EventBroadcaster.isDiscontiguous(new int[] {5, 6, 7, 6}));
    }
    
    public void testGetContiguousIndexBlocks() throws Exception {
        checkGetContiguousIndexBlocks(new int[] {}, true, new int[][] {{}});
        checkGetContiguousIndexBlocks(new int[] {10}, false, new int[][] {{10}});
        checkGetContiguousIndexBlocks(new int[] {10, 12}, false, new int[][] {{10}, {12}});
        checkGetContiguousIndexBlocks(new int[] {10, 12}, true, new int[][] {{12}, {10}});
        checkGetContiguousIndexBlocks(new int[] {10, 11, 12}, false, new int[][] {{10, 11, 12}});
        checkGetContiguousIndexBlocks(new int[] {10, 11, 12}, true, new int[][] {{12, 11, 10}});
        checkGetContiguousIndexBlocks(new int[] {1, 2, 5}, false, new int[][] {{1, 2}, {5}});
        checkGetContiguousIndexBlocks(new int[] {1, 2, 5, 6, 7, 8, 9, 10, 200, 201, 202, 205}, false,
                                      new int[][] {{1, 2}, {5, 6, 7, 8, 9, 10}, {200, 201, 202}, {205}});
        checkGetContiguousIndexBlocks(new int[] {1, 2, 5, 6, 7, 8, 9, 10, 200, 201, 202, 205}, true,
                                      new int[][] {{205}, {202, 201, 200}, {10, 9, 8, 7, 6, 5}, {2, 1}});
        checkGetContiguousIndexBlocks(new int[] {0, 1, 0, 2, 8, 5, 0, 6, 7, 8, 9, 0, 203, 10, 204, 0, 200, 201, 202, 205}, false,
                                      new int[][] {{0}, {0}, {0}, {0}, {0, 1, 2}, {5, 6, 7, 8}, {8, 9, 10}, {200, 201, 202, 203, 204, 205}});
    }

    private void checkGetContiguousIndexBlocks(int[] indices, boolean reverseOrder, int[][] blocks) {
        int[][] cblocks = EventBroadcaster.getContiguousIndexBlocks(indices, reverseOrder);
        boolean equals = Arrays.deepEquals(blocks, cblocks);
        if (!equals) {
            String msg = "Blocks "+Arrays.deepToString(blocks)+
                         " are not equal to continuous blocks "+Arrays.deepToString(cblocks);
            assertTrue(msg, equals);
        }
    }
    
}
