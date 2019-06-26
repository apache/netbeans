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
package org.netbeans.modules.payara.tooling.utils;

import org.netbeans.modules.payara.tooling.CommonTest;
import org.testng.annotations.Test;
import static org.testng.Assert.assertTrue;

/**
 * Test String prefix tree.
 * <p>
 * @author Tomas Kraus, Peter Benedikovic
 */
@Test(groups = {"unit-tests"})
public class StringPrefixTreeTest extends CommonTest {

    /**
     * Test <code>StringPrefixTree</code> adding and prefix matching.
     */
    @Test
    public void testStringPrefixMatch() {
        StringPrefixTree<Integer> pt = new StringPrefixTree<Integer>(false);
        pt.add("ABC", new Integer(1));
        pt.add("ABCDE", new Integer(2));
        pt.add("ABE", new Integer(3));
        Integer v1 = pt.prefixMatch("abc");
        Integer v2 = pt.prefixMatch("abcde");
        Integer v3 = pt.prefixMatch("abe");
        Integer v4 = pt.prefixMatch("abcd");
        Integer v5 = pt.prefixMatch("abcdef");
        Integer v6 = pt.prefixMatch("bcdef");
        assertTrue(v1.compareTo(1) == 0);
        assertTrue(v2.compareTo(2) == 0);
        assertTrue(v3.compareTo(3) == 0);
        assertTrue(v4.compareTo(1) == 0);
        assertTrue(v5.compareTo(2) == 0);
        assertTrue(v6 == null);
    }

    /**
     * Test <code>StringPrefixTree</code> adding and full matching.
     */
    @Test
    public void testStringExactMatch() {
        StringPrefixTree<Integer> pt = new StringPrefixTree<Integer>(true);
        pt.add("ABC", new Integer(1));
        pt.add("ABCDE", new Integer(2));
        pt.add("ABE", new Integer(3));
        Integer v1 = pt.match("ABC");
        Integer v2 = pt.match("ABCDE");
        Integer v3 = pt.match("ABE");
        Integer v4 = pt.match("ABCD");
        Integer v5 = pt.match("ABCDEF");
        Integer v6 = pt.match("BCDEF");
        Integer v7 = pt.prefixMatch("abc");
        Integer v8 = pt.prefixMatch("abcde");
        Integer v9 = pt.prefixMatch("abe");
        assertTrue(v1.compareTo(1) == 0);
        assertTrue(v2.compareTo(2) == 0);
        assertTrue(v3.compareTo(3) == 0);
        assertTrue(v4 == null);
        assertTrue(v5 == null);
        assertTrue(v6 == null);
        assertTrue(v7 == null);
        assertTrue(v8 == null);
        assertTrue(v9 == null);
    }

    /**
     * Test <code>StringPrefixTree</code> removal.
     */
    @Test
    public void testStringRemoval() {
        StringPrefixTree<Integer> pt = new StringPrefixTree<Integer>(false);
        pt.add("HelloKitty", new Integer(1));
        pt.add("HelloPuppy", new Integer(2));
        pt.add("Hello", new Integer(3));
        pt.add("HelpWhales", new Integer(4));
        pt.add("HelpDolphins", new Integer(5));
        Integer r1 = pt.remove("HelpWhales");
        Integer r2 = pt.remove("HelpDolphins");
        Integer r3 = pt.remove("Hello");
        Integer r4 = pt.remove("HelloKitty");
        Integer r5 = pt.remove("HelloPuppy");
        Integer v1 = pt.match("Hello");
        assertTrue(r1.compareTo(4) == 0);
        assertTrue(r2.compareTo(5) == 0);
        assertTrue(r3.compareTo(3) == 0);
        assertTrue(r4.compareTo(1) == 0);
        assertTrue(r5.compareTo(2) == 0);
        assertTrue(v1 == null);
    }

    /**
     * Test <code>StringPrefixTree</code> removal.
     */
    @Test
    public void testClear() {
        StringPrefixTree<Integer> pt = new StringPrefixTree<Integer>(false);
        pt.add("HelloKitty", new Integer(1));
        pt.add("HelloPuppy", new Integer(2));
        pt.add("Hello", new Integer(3));
        pt.add("HelpWhales", new Integer(4));
        pt.add("HelpDolphins", new Integer(5));
        pt.clear();
        assertTrue(pt.size() == 0);
    }

}
