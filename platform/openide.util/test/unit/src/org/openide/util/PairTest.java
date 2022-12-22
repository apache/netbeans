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
package org.openide.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;


/**
 *
 * @author Tomas Zezula, Lukasz Bownik
 */
public class PairTest {

    //-------------------------------------------------------------------------- 
    @Test
    public void pairEquality() {

        Pair<Integer, Integer> pair11a = Pair.of(1, 1);
        Pair<Integer, Integer> pair11b = Pair.of(1, 1);
        Pair<Integer, Integer> pair12 = Pair.of(1, 2);
        Pair<Integer, Integer> pairNul1 = Pair.of(null, 1);
        Pair<Integer, Integer> pair1Null = Pair.of(1, null);
        Pair<Integer, Integer> pairNullNull = Pair.of(null, null);

        assertTrue(pair11a.equals(pair11a));
        assertEquals(pair11a.hashCode(), pair11a.hashCode());
        assertEquals(new Integer(1), pair11a.first());
        assertEquals(new Integer(1), pair11a.second());

        assertTrue(pair12.equals(pair12));
        assertEquals(pair12.hashCode(), pair12.hashCode());
        assertEquals(new Integer(1), pair12.first());
        assertEquals(new Integer(2), pair12.second());

        assertTrue(pair11a.equals(pair11b));
        assertEquals(pair11a.hashCode(), pair11b.hashCode());
        assertEquals(new Integer(1), pair11b.first());
        assertEquals(new Integer(1), pair11b.second());

        assertTrue(pair1Null.equals(pair1Null));
        assertEquals(pair1Null.hashCode(), pair1Null.hashCode());
        assertEquals(new Integer(1), pair1Null.first());
        assertEquals(null, pair1Null.second());

        assertTrue(pairNul1.equals(pairNul1));
        assertEquals(pairNul1.hashCode(), pairNul1.hashCode());
        assertEquals(null, pairNul1.first());
        assertEquals(new Integer(1), pairNul1.second());

        assertTrue(pairNullNull.equals(pairNullNull));
        assertEquals(pairNullNull.hashCode(), pairNullNull.hashCode());
        assertEquals(null, pairNullNull.first());
        assertEquals(null, pairNullNull.second());
    }

    //-------------------------------------------------------------------------- 
    @Test
    public void test_pairInequality() {

        Pair<Integer, Integer> pair11a = Pair.of(1, 1);
        Pair<Integer, Integer> pair12 = Pair.of(1, 2);
        Pair<Integer, Integer> pair21 = Pair.of(2, 1);
        Pair<Integer, Integer> pairNul1 = Pair.of(null, 1);
        Pair<Integer, Integer> pair1Null = Pair.of(1, null);
        Pair<Integer, Integer> pairNullNull = Pair.of(null, null);
        
        assertFalse(pair11a.equals(pair12));
        assertFalse(pair11a.equals(pair21));
        assertFalse(pair11a.equals(pairNul1));
        assertFalse(pair11a.equals(pair1Null));
        assertFalse(pair11a.equals(pairNullNull));
        assertFalse(pairNul1.equals(pair11a));
        assertFalse(pair1Null.equals(pair11a));
        assertFalse(pairNullNull.equals(pair11a));

        assertFalse(pair11a.equals(null));
        assertFalse(pair11a.equals(""));
    }

    //--------------------------------------------------------------------------
    @Test
    public void toStringMethod() {
        
        assertEquals("Pair[1,1]", Pair.of(1, 1).toString());
        assertEquals("Pair[1,2]", Pair.of(1, 2).toString());
        assertEquals("Pair[1,null]", Pair.of(1, null).toString());
        assertEquals("Pair[null,1]", Pair.of(null, 1).toString());
        assertEquals("Pair[null,null]", Pair.of(null, null).toString());
    }
}
