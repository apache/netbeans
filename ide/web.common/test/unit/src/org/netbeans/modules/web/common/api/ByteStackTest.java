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
package org.netbeans.modules.web.common.api;

import org.netbeans.junit.NbTestCase;


public class ByteStackTest extends NbTestCase {

    private ByteStack stack;


    public ByteStackTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        stack = new ByteStack();
    }

    public void testGeneral() {
        assertTrue(stack.isEmpty());
        try {
            stack.peek();
            fail("Should not get here");
        } catch (ArrayIndexOutOfBoundsException exc) {
            // expected
        }
        try {
            stack.pop();
            fail("Should not get here");
        } catch (ArrayIndexOutOfBoundsException exc) {
            // expected
        }
        stack.push(7);
        assertEquals(7, stack.peek());
        assertEquals(1, stack.size());
        assertFalse(stack.isEmpty());
        assertFalse(stack.contains(8));
        assertTrue(stack.contains(7));
        int first = stack.pop();
        assertEquals(7, first);
        assertTrue(stack.isEmpty());
        stack.push(5);
        stack.push(8);
        stack.push(15);
        assertEquals(15, stack.peek());
        assertEquals(3, stack.size());
        assertFalse(stack.isEmpty());
        assertFalse(stack.contains(7));
        assertTrue(stack.contains(8));
        stack.clear();
        assertEquals(0, stack.size());
        assertTrue(stack.isEmpty());
        assertFalse(stack.contains(7));
        assertFalse(stack.contains(8));
    }

    public void testCopying() {
        stack.push(2);
        ByteStack copy = stack.copyOf();
        assertFalse(copy.isEmpty());
        assertEquals(1, copy.size());
        assertEquals(2, copy.peek());
        stack.push(17);
        copy = stack.copyOf();
        assertEquals(2, copy.size());
        assertEquals(17, copy.peek());
        assertTrue(stack.equals(copy));
        stack.clear();
        assertFalse(copy.isEmpty());
        ByteStack other = new ByteStack();
        other.push(4);
        other.push(16);
        other.push(35);
        stack.copyFrom(other);
        assertEquals(3, stack.size());
        assertEquals(35, stack.peek());
        assertTrue(stack.equals(other));
        other.clear();
        assertFalse(stack.isEmpty());
    }

}
