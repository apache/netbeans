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
