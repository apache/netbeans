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

package org.netbeans.lib.editor.util.swing;

import org.netbeans.junit.NbTestCase;

public class BlockCompareTest extends NbTestCase {

    public BlockCompareTest(String testName) {
        super(testName);
    }

    public void testBlockCompare() throws Exception {
        BlockCompare bc;

        try {
            bc = BlockCompare.get(1,0,1,1);
            fail("AssertionError expected");
        } catch (AssertionError e) {
            // Expected
        }

        try {
            bc = BlockCompare.get(1,1,1,0);
            fail("AssertionError expected");
        } catch (AssertionError e) {
            // Expected
        }

        bc = BlockCompare.get(1,1,1,1);
        assertTrue(bc.before());
        assertTrue(bc.after());
        assertTrue(bc.inside());
        assertTrue(!bc.insideStrict());
        assertTrue(bc.contains());
        assertTrue(!bc.containsStrict());
        assertTrue(bc.equal());
        assertTrue(bc.equalStart());
        assertTrue(bc.equalEnd());
        assertTrue(!bc.lowerStart());
        assertTrue(!bc.lowerEnd());
        assertTrue(!bc.overlap());
        assertTrue(!bc.overlapStart());
        assertTrue(!bc.overlapEnd());
        assertTrue(bc.emptyX());
        assertTrue(bc.emptyY());
        assertTrue(!bc.invalidX());
        assertTrue(!bc.invalidY());

        bc = BlockCompare.get(1,2,3,4);
        assertTrue(bc.before());
        assertTrue(!bc.after());
        assertTrue(!bc.inside());
        assertTrue(!bc.insideStrict());
        assertTrue(!bc.contains());
        assertTrue(!bc.containsStrict());
        assertTrue(!bc.equal());
        assertTrue(!bc.equalStart());
        assertTrue(!bc.equalEnd());
        assertTrue(bc.lowerStart());
        assertTrue(bc.lowerEnd());
        assertTrue(!bc.overlap());
        assertTrue(!bc.overlapStart());
        assertTrue(!bc.overlapEnd());
        assertTrue(!bc.emptyX());
        assertTrue(!bc.emptyY());
        assertTrue(!bc.invalidX());
        assertTrue(!bc.invalidY());

        bc = BlockCompare.get(3,4,1,2);
        assertTrue(!bc.before());
        assertTrue(bc.after());
        assertTrue(!bc.inside());
        assertTrue(!bc.insideStrict());
        assertTrue(!bc.contains());
        assertTrue(!bc.containsStrict());
        assertTrue(!bc.equal());
        assertTrue(!bc.equalStart());
        assertTrue(!bc.equalEnd());
        assertTrue(!bc.lowerStart());
        assertTrue(!bc.lowerEnd());
        assertTrue(!bc.overlap());
        assertTrue(!bc.overlapStart());
        assertTrue(!bc.overlapEnd());
        assertTrue(!bc.emptyX());
        assertTrue(!bc.emptyY());
        assertTrue(!bc.invalidX());
        assertTrue(!bc.invalidY());

        bc = BlockCompare.get(1,3,2,4);
        assertTrue(!bc.before());
        assertTrue(!bc.after());
        assertTrue(!bc.inside());
        assertTrue(!bc.insideStrict());
        assertTrue(!bc.contains());
        assertTrue(!bc.containsStrict());
        assertTrue(!bc.equal());
        assertTrue(!bc.equalStart());
        assertTrue(!bc.equalEnd());
        assertTrue(bc.lowerStart());
        assertTrue(bc.lowerEnd());
        assertTrue(bc.overlap());
        assertTrue(bc.overlapStart());
        assertTrue(!bc.overlapEnd());
        assertTrue(!bc.emptyX());
        assertTrue(!bc.emptyY());
        assertTrue(!bc.invalidX());
        assertTrue(!bc.invalidY());

        bc = BlockCompare.get(2,4,1,3);
        assertTrue(!bc.before());
        assertTrue(!bc.after());
        assertTrue(!bc.inside());
        assertTrue(!bc.insideStrict());
        assertTrue(!bc.contains());
        assertTrue(!bc.containsStrict());
        assertTrue(!bc.equal());
        assertTrue(!bc.equalStart());
        assertTrue(!bc.equalEnd());
        assertTrue(!bc.lowerStart());
        assertTrue(!bc.lowerEnd());
        assertTrue(bc.overlap());
        assertTrue(!bc.overlapStart());
        assertTrue(bc.overlapEnd());
        assertTrue(!bc.emptyX());
        assertTrue(!bc.emptyY());
        assertTrue(!bc.invalidX());
        assertTrue(!bc.invalidY());

        bc = BlockCompare.get(1,4,2,3);
        assertTrue(!bc.before());
        assertTrue(!bc.after());
        assertTrue(!bc.inside());
        assertTrue(!bc.insideStrict());
        assertTrue(bc.contains());
        assertTrue(bc.containsStrict());
        assertTrue(!bc.equal());
        assertTrue(!bc.equalStart());
        assertTrue(!bc.equalEnd());
        assertTrue(bc.lowerStart());
        assertTrue(!bc.lowerEnd());
        assertTrue(!bc.overlap());
        assertTrue(!bc.overlapStart());
        assertTrue(!bc.overlapEnd());
        assertTrue(!bc.emptyX());
        assertTrue(!bc.emptyY());
        assertTrue(!bc.invalidX());
        assertTrue(!bc.invalidY());

        bc = BlockCompare.get(2,3,1,4);
        assertTrue(!bc.before());
        assertTrue(!bc.after());
        assertTrue(bc.inside());
        assertTrue(bc.insideStrict());
        assertTrue(!bc.contains());
        assertTrue(!bc.containsStrict());
        assertTrue(!bc.equal());
        assertTrue(!bc.equalStart());
        assertTrue(!bc.equalEnd());
        assertTrue(!bc.lowerStart());
        assertTrue(bc.lowerEnd());
        assertTrue(!bc.overlap());
        assertTrue(!bc.overlapStart());
        assertTrue(!bc.overlapEnd());
        assertTrue(!bc.emptyX());
        assertTrue(!bc.emptyY());
        assertTrue(!bc.invalidX());
        assertTrue(!bc.invalidY());

        bc = BlockCompare.get(2,3,2,4);
        assertTrue(!bc.before());
        assertTrue(!bc.after());
        assertTrue(bc.inside());
        assertTrue(bc.insideStrict());
        assertTrue(!bc.contains());
        assertTrue(!bc.containsStrict());
        assertTrue(!bc.equal());
        assertTrue(bc.equalStart());
        assertTrue(!bc.equalEnd());
        assertTrue(!bc.lowerStart());
        assertTrue(bc.lowerEnd());
        assertTrue(!bc.overlap());
        assertTrue(!bc.overlapStart());
        assertTrue(!bc.overlapEnd());
        assertTrue(!bc.emptyX());
        assertTrue(!bc.emptyY());
        assertTrue(!bc.invalidX());
        assertTrue(!bc.invalidY());

        bc = BlockCompare.get(2,4,2,3);
        assertTrue(!bc.before());
        assertTrue(!bc.after());
        assertTrue(!bc.inside());
        assertTrue(!bc.insideStrict());
        assertTrue(bc.contains());
        assertTrue(bc.containsStrict());
        assertTrue(!bc.equal());
        assertTrue(bc.equalStart());
        assertTrue(!bc.equalEnd());
        assertTrue(!bc.lowerStart());
        assertTrue(!bc.lowerEnd());
        assertTrue(!bc.overlap());
        assertTrue(!bc.overlapStart());
        assertTrue(!bc.overlapEnd());
        assertTrue(!bc.emptyX());
        assertTrue(!bc.emptyY());
        assertTrue(!bc.invalidX());
        assertTrue(!bc.invalidY());

        bc = BlockCompare.get(1,4,2,4);
        assertTrue(!bc.before());
        assertTrue(!bc.after());
        assertTrue(!bc.inside());
        assertTrue(!bc.insideStrict());
        assertTrue(bc.contains());
        assertTrue(bc.containsStrict());
        assertTrue(!bc.equal());
        assertTrue(!bc.equalStart());
        assertTrue(bc.equalEnd());
        assertTrue(bc.lowerStart());
        assertTrue(!bc.lowerEnd());
        assertTrue(!bc.overlap());
        assertTrue(!bc.overlapStart());
        assertTrue(!bc.overlapEnd());
        assertTrue(!bc.emptyX());
        assertTrue(!bc.emptyY());
        assertTrue(!bc.invalidX());
        assertTrue(!bc.invalidY());

        bc = BlockCompare.get(2,4,2,4);
        assertTrue(!bc.before());
        assertTrue(!bc.after());
        assertTrue(bc.inside());
        assertTrue(!bc.insideStrict());
        assertTrue(bc.contains());
        assertTrue(!bc.containsStrict());
        assertTrue(bc.equal());
        assertTrue(bc.equalStart());
        assertTrue(bc.equalEnd());
        assertTrue(!bc.lowerStart());
        assertTrue(!bc.lowerEnd());
        assertTrue(!bc.overlap());
        assertTrue(!bc.overlapStart());
        assertTrue(!bc.overlapEnd());
        assertTrue(!bc.emptyX());
        assertTrue(!bc.emptyY());
        assertTrue(!bc.invalidX());
        assertTrue(!bc.invalidY());

        bc = BlockCompare.get(2,2,4,4);
        assertTrue(bc.before());
        assertTrue(!bc.after());
        assertTrue(!bc.inside());
        assertTrue(!bc.insideStrict());
        assertTrue(!bc.contains());
        assertTrue(!bc.containsStrict());
        assertTrue(!bc.equal());
        assertTrue(!bc.equalStart());
        assertTrue(!bc.equalEnd());
        assertTrue(bc.lowerStart());
        assertTrue(bc.lowerEnd());
        assertTrue(!bc.overlap());
        assertTrue(!bc.overlapStart());
        assertTrue(!bc.overlapEnd());
        assertTrue(bc.emptyX());
        assertTrue(bc.emptyY());
        assertTrue(!bc.invalidX());
        assertTrue(!bc.invalidY());

        bc = BlockCompare.get(5,8,6,6);
        assertTrue(!bc.before());
        assertTrue(!bc.after());
        assertTrue(!bc.inside());
        assertTrue(!bc.insideStrict());
        assertTrue(bc.contains());
        assertTrue(bc.containsStrict());
        assertTrue(!bc.equal());
        assertTrue(!bc.equalStart());
        assertTrue(!bc.equalEnd());
        assertTrue(bc.lowerStart());
        assertTrue(!bc.lowerEnd());
        assertTrue(!bc.overlap());
        assertTrue(!bc.overlapStart());
        assertTrue(!bc.overlapEnd());
        assertTrue(!bc.emptyX());
        assertTrue(bc.emptyY());
        assertTrue(!bc.invalidX());
        assertTrue(!bc.invalidY());

        bc = BlockCompare.get(1,2,2,3);
        assertTrue(bc.before());
        assertTrue(!bc.after());
        assertTrue(!bc.inside());
        assertTrue(!bc.insideStrict());
        assertTrue(!bc.contains());
        assertTrue(!bc.containsStrict());
        assertTrue(!bc.equal());
        assertTrue(!bc.equalStart());
        assertTrue(!bc.equalEnd());
        assertTrue(bc.lowerStart());
        assertTrue(bc.lowerEnd());
        assertTrue(!bc.overlap());
        assertTrue(!bc.overlapStart());
        assertTrue(!bc.overlapEnd());
        assertTrue(!bc.emptyX());
        assertTrue(!bc.emptyY());
        assertTrue(!bc.invalidX());
        assertTrue(!bc.invalidY());

        bc = BlockCompare.get(2,3,1,2);
        assertTrue(!bc.before());
        assertTrue(bc.after());
        assertTrue(!bc.inside());
        assertTrue(!bc.insideStrict());
        assertTrue(!bc.contains());
        assertTrue(!bc.containsStrict());
        assertTrue(!bc.equal());
        assertTrue(!bc.equalStart());
        assertTrue(!bc.equalEnd());
        assertTrue(!bc.lowerStart());
        assertTrue(!bc.lowerEnd());
        assertTrue(!bc.overlap());
        assertTrue(!bc.overlapStart());
        assertTrue(!bc.overlapEnd());
        assertTrue(!bc.emptyX());
        assertTrue(!bc.emptyY());
        assertTrue(!bc.invalidX());
        assertTrue(!bc.invalidY());
        
        // Test invalid bounds
        bc = BlockCompare.get(2,1,4,4);
        assertTrue(bc.before());
        assertTrue(!bc.after());
        assertTrue(!bc.inside());
        assertTrue(!bc.insideStrict());
        assertTrue(!bc.contains());
        assertTrue(!bc.containsStrict());
        assertTrue(!bc.equal());
        assertTrue(!bc.equalStart());
        assertTrue(!bc.equalEnd());
        assertTrue(bc.lowerStart());
        assertTrue(bc.lowerEnd());
        assertTrue(!bc.overlap());
        assertTrue(!bc.overlapStart());
        assertTrue(!bc.overlapEnd());
        assertTrue(bc.emptyX());
        assertTrue(bc.emptyY());
        assertTrue(bc.invalidX());
        assertTrue(!bc.invalidY());

        bc = BlockCompare.get(2,2,4,3);
        assertTrue(bc.before());
        assertTrue(!bc.after());
        assertTrue(!bc.inside());
        assertTrue(!bc.insideStrict());
        assertTrue(!bc.contains());
        assertTrue(!bc.containsStrict());
        assertTrue(!bc.equal());
        assertTrue(!bc.equalStart());
        assertTrue(!bc.equalEnd());
        assertTrue(bc.lowerStart());
        assertTrue(bc.lowerEnd());
        assertTrue(!bc.overlap());
        assertTrue(!bc.overlapStart());
        assertTrue(!bc.overlapEnd());
        assertTrue(bc.emptyX());
        assertTrue(bc.emptyY());
        assertTrue(!bc.invalidX());
        assertTrue(bc.invalidY());

        bc = BlockCompare.get(2,1,4,3);
        assertTrue(bc.before());
        assertTrue(!bc.after());
        assertTrue(!bc.inside());
        assertTrue(!bc.insideStrict());
        assertTrue(!bc.contains());
        assertTrue(!bc.containsStrict());
        assertTrue(!bc.equal());
        assertTrue(!bc.equalStart());
        assertTrue(!bc.equalEnd());
        assertTrue(bc.lowerStart());
        assertTrue(bc.lowerEnd());
        assertTrue(!bc.overlap());
        assertTrue(!bc.overlapStart());
        assertTrue(!bc.overlapEnd());
        assertTrue(bc.emptyX());
        assertTrue(bc.emptyY());
        assertTrue(bc.invalidX());
        assertTrue(bc.invalidY());

        bc.toString(); // just test that it does not throw an exception
    }

}
