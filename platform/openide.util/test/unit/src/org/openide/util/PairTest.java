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
package org.openide.util;

import org.netbeans.junit.NbTestCase;
import org.openide.util.Pair;

/**
 *
 * @author Tomas Zezula
 */
public class PairTest extends NbTestCase {

    public PairTest(String name) {
        super(name);
    }


    public void testPairs() {
        final Pair<Integer,Integer> p1a = Pair.of(1, 1);
        final Pair<Integer,Integer> p1b = Pair.of(1, 1);
        final Pair<Integer,Integer> p2 = Pair.of(1, 2);
        final Pair<Integer,Integer> p3 = Pair.of(2, 1);
        final Pair<Integer,Integer> p4 = Pair.of(null, 1);
        final Pair<Integer,Integer> p5 = Pair.of(1, null);
        final Pair<Integer,Integer> p6 = Pair.of(null, null);
        assertTrue(p1a.equals(p1a));
        assertTrue(p1a.equals(p1b));
        assertFalse(p1a.equals(p2));
        assertFalse(p1a.equals(p3));
        assertFalse(p1a.equals(p4));
        assertFalse(p1a.equals(p5));
        assertFalse(p1a.equals(p6));
        assertEquals(p1a.hashCode(), p1b.hashCode());
        assertEquals(p4.hashCode(), p4.hashCode());
        assertEquals(p5.hashCode(), p5.hashCode());
        assertEquals(p6.hashCode(), p6.hashCode());
        assertFalse(p4.hashCode() == p5.hashCode());
    }

}
