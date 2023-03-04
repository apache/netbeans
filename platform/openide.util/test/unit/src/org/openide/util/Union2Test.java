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
import org.openide.util.Union2;

/**
 * @author Jesse Glick
 */
public class Union2Test extends NbTestCase {

    public Union2Test(String name) {
        super(name);
    }

    public void testUnions() throws Exception {
        Union2<Integer,String> union = Union2.createFirst(3);
        assertEquals(3, union.first().intValue());
        try {
            union.second();
            fail();
        } catch (IllegalArgumentException e) {/*OK*/}
        assertTrue(union.hasFirst());
        assertFalse(union.hasSecond());
        assertEquals("3", union.toString());
        assertTrue(union.equals(Union2.createFirst(3)));
        assertFalse(union.equals(Union2.createFirst(4)));
        assertEquals(union.hashCode(), Union2.createFirst(3).hashCode());
        assertEquals(union, NbCollectionsTest.cloneBySerialization(union));
        assertEquals(union, union.clone());
        int i = union.clone().first();
        assertEquals(3, i);
        // Second type now.
        union = Union2.createSecond("hello");
        try {
            union.first();
            fail();
        } catch (IllegalArgumentException e) {/*OK*/}
        assertEquals("hello", union.second());
        assertFalse(union.hasFirst());
        assertTrue(union.hasSecond());
        assertEquals("hello", union.toString());
        assertTrue(union.equals(Union2.createSecond("hello")));
        assertFalse(union.equals(Union2.createSecond("there")));
        assertEquals(union.hashCode(), Union2.createSecond("hello").hashCode());
        assertEquals(union, NbCollectionsTest.cloneBySerialization(union));
        assertEquals(union, union.clone());
        String s = union.clone().second();
        assertEquals("hello", s);
    }

    public void testNulls() throws Exception {
        Union2<Void,Boolean> union1 = Union2.createFirst(null);
        assertEquals("null", union1.toString());
        assertEquals(0, union1.hashCode());
        assertTrue(union1.equals(null));
        assertFalse(union1.equals(Union2.createSecond(true)));
        Union2<Boolean,Void> union2 = Union2.createSecond(null);
        assertEquals("null", union2.toString());
        assertEquals(0, union2.hashCode());
        assertTrue(union2.equals(null));
        assertFalse(union2.equals(Union2.createFirst(true)));
    }

}
