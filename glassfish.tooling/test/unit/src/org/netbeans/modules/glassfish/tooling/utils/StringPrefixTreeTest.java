/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015, 2016 Oracle and/or its affiliates. All rights reserved.
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
 */
package org.netbeans.modules.glassfish.tooling.utils;

import org.netbeans.modules.glassfish.tooling.CommonTest;
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
