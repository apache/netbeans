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
package org.netbeans.modules.java.hints.bugs;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;

/**
 *
 * @author Jan Lahoda
 */
public class EqualsHintTest extends NbTestCase {

    public EqualsHintTest(String testName) {
        super(testName);
    }

    public void testSimpleAnalysis1() throws Exception {
        HintTest
                .create()
                .input("package test; public class Test{ public void test() {int[] a = null; boolean b = a.equals(a);}}")
                .run(EqualsHint.class)
                .assertWarnings("0:83-0:89:verifier:AE");
    }

    public void testSimpleAnalysis2() throws Exception {
        HintTest
                .create()
                .input("package test; public class Test{ public void test() {Class c = null; String s = null; boolean b = c.equals(s);}}")
                .run(EqualsHint.class)
                .assertWarnings("0:100-0:106:verifier:IE");
    }

    public void testSimpleAnalysis3() throws Exception {
        HintTest
                .create()
                .input("package test; public class Test{ public void test() {Class c = null; String s = null; boolean b = s.equals(c);}}")
                .run(EqualsHint.class)
                .assertWarnings("0:100-0:106:verifier:IE");
    }

    public void testSimpleAnalysis4() throws Exception {
        HintTest
                .create()
                .input("package test; public class Test{ public void test() {Class c = null; Object o = null; boolean b = o.equals(c);}}")
                .run(EqualsHint.class)
                .assertWarnings();
    }

    public void testSimpleAnalysis5() throws Exception {
        HintTest
                .create()
                .input("package test; public class Test{ public void test() {Class c = null; Object o = null; boolean b = c.equals(o);}}")
                .run(EqualsHint.class)
                .assertWarnings();
    }

    public void testFix1() throws Exception {
        HintTest
                .create()
                .input("package test; public class Test{ public void test() {int[] a = null; boolean b = a.equals(a);}}")
                .run(EqualsHint.class)
                .findWarning("0:83-0:89:verifier:AE")
                .applyFix("FIX_ReplaceWithArraysEquals")
                .assertCompilable()
                .assertOutput("package test; import java.util.Arrays; public class Test{ public void test() {int[] a = null; boolean b = Arrays.equals(a, a);}}");
    }

    public void testFix2() throws Exception {
        HintTest
                .create()
                .input("package test; public class Test{ public void test() {int[] a = null; boolean b = a.equals(a);}}")
                .run(EqualsHint.class)
                .findWarning("0:83-0:89:verifier:AE")
                .applyFix("FIX_ReplaceWithInstanceEquals")
                .assertCompilable()
                .assertOutput("package test; public class Test{ public void test() {int[] a = null; boolean b = a == a;}}");
    }

    public void testAnalysis132853() throws Exception {
        HintTest
                .create()
                .input("package test; public class Test{ public void test() {Class c = null; Object o = null; boolean b = this.equals(c, o);} private boolean equals(Object o1, Object o2) { return false; } }")
                .run(EqualsHint.class)
                .assertWarnings();
    }

    public void testUnresolved1() throws Exception {
        HintTest
                .create()
                .input("package test; public class Test{ public void test() {int[] a = null; boolean b = a.equals(aa);}}", false)
                .run(EqualsHint.class)
                .assertWarnings();
    }

    public void testNoThis() throws Exception {
        HintTest
                .create()
                .input("package test; public class Test { public boolean test(Integer o) {return equals(o);}}")
                .run(EqualsHint.class)
                .assertWarnings("0:73-0:79:verifier:IE");
    }
}
