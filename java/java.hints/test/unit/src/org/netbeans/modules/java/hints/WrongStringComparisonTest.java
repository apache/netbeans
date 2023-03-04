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

package org.netbeans.modules.java.hints;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;

/**
 *
 * @author Jan Lahoda
 */
public class WrongStringComparisonTest extends NbTestCase {

    private WrongStringComparison wsc;

    public WrongStringComparisonTest(String name) {
        super(name);
        wsc = new WrongStringComparison();
    }

    public void testSimple() throws Exception {
        HintTest.create()
                .input("package test;" +
                       "public class Test {" +
                       "    private String s;" +
                       "    private void test() {" +
                       "        String t = null;" +
                       "        if (s == t);" +
                       "    }" +
                       "}")
                .run(WrongStringComparison.class)
                .assertWarnings("0:114-0:120:verifier:Comparing Strings using == or !=");
    }
    
    public void testDisableWhenCorrectlyCheckedAsIn111441() throws Exception {
        HintTest.create()
                .input("package test;" +
                       "public class Test {" +
                       "    private String s;" +
                       "    private void test() {" +
                       "        Test t = null;" +
                       "        boolean b = this.s != t.s && (this.s == null || !this.s.equals(t.s));" +
                       "    }" +
                       "}")
                .run(WrongStringComparison.class)
                .assertWarnings();
    }

    public void testFixWithTernaryNullCheck() throws Exception {
        HintTest.create()
                .input("package test;" +
                       "public class Test {" +
                       "    private String s;" +
                       "    private void test() {" +
                       "        String t = null;" +
                       "        if (s == t);" +
                       "    }" +
                       "}")
                .run(WrongStringComparison.class)
                .findWarning("0:114-0:120:verifier:Comparing Strings using == or !=")
                .applyFix("[WrongStringComparisonFix:Use equals() with null check (ternary)]")
                .assertCompilable()
                .assertOutput("package test;public class Test { private String s; private void test() { String t = null; if (s == null ? t == null : s.equals(t)); }}");
    }

    public void testFixWithoutNullCheck() throws Exception {
        HintTest.create()
                .input("package test;" +
                       "public class Test {" +
                       "    private String s;" +
                       "    private void test() {" +
                       "        String t = null;" +
                       "        if (s == t);" +
                       "    }" +
                       "}")
                .preference(WrongStringComparison.TERNARY_NULL_CHECK, false)
                .run(WrongStringComparison.class)
                .findWarning("0:114-0:120:verifier:Comparing Strings using == or !=")
                .applyFix("[WrongStringComparisonFix:Use equals()]")
                .assertCompilable()
                .assertOutput("package test;public class Test { private String s; private void test() { String t = null; if (s.equals(t)); }}");
    }

    public void testFixWithNullCheck() throws Exception {
        HintTest.create()
                .input("package test;" +
                       "public class Test {" +
                       "    private String s;" +
                       "    private void test() {" +
                       "        String t = null;" +
                       "        if (s == t);" +
                       "    }" +
                       "}")
                .preference(WrongStringComparison.TERNARY_NULL_CHECK, false)
                .run(WrongStringComparison.class)
                .findWarning("0:114-0:120:verifier:Comparing Strings using == or !=")
                .applyFix("[WrongStringComparisonFix:Use equals() with null check]")
                .assertCompilable()
                .assertOutput("package test;public class Test { private String s; private void test() { String t = null; if ((s == null && t == null) || (s != null && s.equals(t))); }}");
    }

    public void testFixWithStringLiteralFirst() throws Exception {
        HintTest.create()
                .input("package test;" +
                       "public class Test {" +
                       "    private String s;" +
                       "    private void test() {" +
                       "        if (\"\" == s);" +
                       "    }" +
                       "}")
                .run(WrongStringComparison.class)
                .findWarning("0:90-0:97:verifier:Comparing Strings using == or !=")
                .applyFix("[WrongStringComparisonFix:Use equals()]")
                .assertCompilable()
                .assertOutput("package test;public class Test { private String s; private void test() { if (\"\".equals(s)); }}");
    }

    public void testFixWithStringLiteralSecondReverseOperands() throws Exception {
        HintTest.create()
                .input("package test;" +
                       "public class Test {" +
                       "    private String s;" +
                       "    private void test() {" +
                       "        if (s == \"\");" +
                       "    }" +
                       "}")
                .run(WrongStringComparison.class)
                .findWarning("0:90-0:97:verifier:Comparing Strings using == or !=")
                .applyFix("[WrongStringComparisonFix:Use equals() and reverse operands]")
                .assertCompilable()
                .assertOutput("package test;public class Test { private String s; private void test() { if (\"\".equals(s)); }}");
    }

    public void testFixWithStringLiteralSecondNullCheck() throws Exception {
        HintTest.create()
                .input("package test;" +
                       "public class Test {" +
                       "    private String s;" +
                       "    private void test() {" +
                       "        if (s == \"\");" +
                       "    }" +
                       "}")
                .preference(WrongStringComparison.STRING_LITERALS_FIRST, false)
                .run(WrongStringComparison.class)
                .findWarning("0:90-0:97:verifier:Comparing Strings using == or !=")
                .applyFix("[WrongStringComparisonFix:Use equals() with null check]")
                .assertCompilable()
                .assertOutput("package test;public class Test { private String s; private void test() { if (s != null && s.equals(\"\")); }}");
    }

    public void testFixWithStringLiteralSecondNoNullCheck() throws Exception {
        HintTest.create()
                .input("package test;" +
                       "public class Test {" +
                       "    private String s;" +
                       "    private void test() {" +
                       "        if (s == \"\");" +
                       "    }" +
                       "}")
                .preference(WrongStringComparison.STRING_LITERALS_FIRST, false)
                .run(WrongStringComparison.class)
                .findWarning("0:90-0:97:verifier:Comparing Strings using == or !=")
                .applyFix("[WrongStringComparisonFix:Use equals()]")
                .assertCompilable()
                .assertOutput("package test;public class Test { private String s; private void test() { if (s.equals(\"\")); }}");
    }

    public void testFixWithTwoStringLiterals() throws Exception {
        HintTest.create()
                .input("test/Test.java",
                       "package test;" +
                       "public class Test {" +
                       "    private void test() {" +
                       "        if (\"\" == \"\");" +
                       "    }" +
                       "}")
                .run(WrongStringComparison.class)
                .findWarning("0:69-0:77:verifier:Comparing Strings using == or !=")
                .applyFix("[WrongStringComparisonFix:Use equals()]")
                .assertCompilable()
                .assertOutput("package test;public class Test { private void test() { if (\"\".equals(\"\")); }}");
    }

}
