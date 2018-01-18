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
package org.netbeans.modules.java.hints.bugs;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;

/**
 *
 * @author sdedic
 */
public class BoxedIdentityComparisonTest extends NbTestCase {

    public BoxedIdentityComparisonTest(String name) {
        super(name);
    }

    public void testBoxedEqualsBothSidesPossiblyNull() throws Exception {
        HintTest.create().sourceLevel("1.7")
                .input(
                "package test;\n" + 
"public final class Test {\n" +
"    public static void test(Integer a, Integer b) {\n" +
"        if (a == b) {\n" +
"        }\n" +
"    }\n" +
"}"
                )
                .run(BoxedIdentityComparison.class).
                assertWarnings("3:12-3:18:verifier:Integer values compared using == or !=").
                findWarning("3:12-3:18:verifier:Integer values compared using == or !=").
                applyFix("Replace with null-safe equals()").
                assertOutput(
                "package test;\n" + 
                "import java.util.Objects;\n" +
"public final class Test {\n" +
"    public static void test(Integer a, Integer b) {\n" +
"        if (Objects.equals(a, b)) {\n" +
"        }\n" +
"    }\n" +
"}"
                );
    }

    public void testBoxedEqualsLhsNotNull() throws Exception {
        HintTest.create()
                .input(
                "package test;\n" +
"public final class Test {\n" +
"    public static void test(Integer a, Integer b) {\n" +
"        assert a != null;\n" +
"        if (a == b) {\n" +
"        }\n" +
"    }\n" +
"}"
                )
                .run(BoxedIdentityComparison.class).
                assertWarnings("4:12-4:18:verifier:Integer values compared using == or !=").
                findWarning("4:12-4:18:verifier:Integer values compared using == or !=").
                applyFix("Replace with 'a.equals(b)'").
                assertOutput(
                "package test;\n" +
"public final class Test {\n" +
"    public static void test(Integer a, Integer b) {\n" +
"        assert a != null;\n" +
"        if (a.equals(b)) {\n" +
"        }\n" +
"    }\n" +
"}"
                );
    }

    public void testBoxedEqualsNonPrimaryLhsNotNull() throws Exception {
        HintTest.create()
                .input(
                "package test;\n" +
"public final class Test {\n" +
"    private void test(Integer j) {\n" +
"        final Integer i = 3;\n" +
"        if ((Integer)(i + 1) == j) {\n" +
"        }\n" +
"    }\n" +
"}"
                )
                .run(BoxedIdentityComparison.class).
                assertWarnings("4:12-4:33:verifier:Integer values compared using == or !=").
                findWarning("4:12-4:33:verifier:Integer values compared using == or !=").
                applyFix("Replace with '((Integer)(i + 1)).equals(j)'").
                assertOutput(
                "package test;\n" +
"public final class Test {\n" +
"    private void test(Integer j) {\n" +
"        final Integer i = 3;\n" +
"        if (((Integer)(i + 1)).equals(j)) {\n" +
"        }\n" +
"    }\n" +
"}"
                ).
                assertCompilable();
    }

    public void testBoxedEqualsRhsNotNull() throws Exception {
        HintTest.create()
                .input(
                "package test;\n" +
"public final class Test {\n" +
"    public static void test(Integer a, Integer b) {\n" +
"        assert b != null;\n" +
"        if (a == b) {\n" +
"        }\n" +
"    }\n" +
"}"
                )
                .run(BoxedIdentityComparison.class).
                assertWarnings("4:12-4:18:verifier:Integer values compared using == or !=").
                findWarning("4:12-4:18:verifier:Integer values compared using == or !=").
                applyFix("Replace with 'b.equals(a)'").
                assertOutput(
                "package test;\n" +
"public final class Test {\n" +
"    public static void test(Integer a, Integer b) {\n" +
"        assert b != null;\n" +
"        if (b.equals(a)) {\n" +
"        }\n" +
"    }\n" +
"}"
                );
    }

    public void testBoxedEqualsRhsNotNull2() throws Exception {
        HintTest.create()
                .input(
                "package test;\n" +
"public final class Test {\n" +
"    public static void test(Boolean b) {\n" +
"        if (b == Boolean.TRUE) {\n" +
"        }\n" +
"    }\n" +
"}"
                )
                .run(BoxedIdentityComparison.class).
                assertWarnings("3:12-3:29:verifier:Boolean values compared using == or !=").
                findWarning("3:12-3:29:verifier:Boolean values compared using == or !=").
                applyFix("Replace with 'Boolean.TRUE.equals(b)'").
                assertOutput(
                "package test;\n" +
"public final class Test {\n" +
"    public static void test(Boolean b) {\n" +
"        if (Boolean.TRUE.equals(b)) {\n" +
"        }\n" +
"    }\n" +
"}"
                ).
                assertCompilable();
    }

    public void testBoxedEqualsRhsNotNull3() throws Exception {
        HintTest.create()
                .input(
                "package test;\n" +
"import static java.lang.Boolean.TRUE;\n" +
"public final class Test {\n" +
"    public static void test(Boolean b) {\n" +
"        if (b == TRUE) {\n" +
"        }\n" +
"    }\n" +
"}"
                )
                .run(BoxedIdentityComparison.class).
                assertWarnings("4:12-4:21:verifier:Boolean values compared using == or !=").
                findWarning("4:12-4:21:verifier:Boolean values compared using == or !=").
                applyFix("Replace with 'TRUE.equals(b)'").
                assertOutput(
                "package test;\n" +
"import static java.lang.Boolean.TRUE;\n" +
"public final class Test {\n" +
"    public static void test(Boolean b) {\n" +
"        if (TRUE.equals(b)) {\n" +
"        }\n" +
"    }\n" +
"}"
                ).
                assertCompilable();
    }

    public void testBoxedNotEqualsBothSidesPossiblyNull() throws Exception {
        HintTest.create().sourceLevel("1.7")
                .input(
                "package test;\n" +
"public final class Test {\n" +
"    public static void test(Integer a, Integer b) {\n" +
"        if (a != b) {\n" +
"        }\n" +
"    }\n" +
"}"
                )
                .run(BoxedIdentityComparison.class).
                assertWarnings("3:12-3:18:verifier:Integer values compared using == or !=").
                findWarning("3:12-3:18:verifier:Integer values compared using == or !=").
                applyFix("Replace with null-safe equals()").
                assertOutput(
                "package test;\n" + 
                "import java.util.Objects;\n" +
"public final class Test {\n" +
"    public static void test(Integer a, Integer b) {\n" +
"        if (!Objects.equals(a, b)) {\n" +
"        }\n" +
"    }\n" +
"}"
                );
    }

    public void testBoxedNotEqualsLhsNotNull() throws Exception {
        HintTest.create()
                .input(
                "package test;\n" +
"public final class Test {\n" +
"    public static void test(Integer a, Integer b) {\n" +
"        assert a != null;\n" +
"        if (a != b) {\n" +
"        }\n" +
"    }\n" +
"}"
                )
                .run(BoxedIdentityComparison.class).
                assertWarnings("4:12-4:18:verifier:Integer values compared using == or !=").
                findWarning("4:12-4:18:verifier:Integer values compared using == or !=").
                applyFix("Replace with '!a.equals(b)'").
                assertOutput(
                "package test;\n" +
"public final class Test {\n" +
"    public static void test(Integer a, Integer b) {\n" +
"        assert a != null;\n" +
"        if (!a.equals(b)) {\n" +
"        }\n" +
"    }\n" +
"}"
                );
    }

    public void testBoxedNotEqualsRhsNotNull() throws Exception {
        HintTest.create()
                .input(
                "package test;\n" +
"public final class Test {\n" +
"    public static void test(Integer a, Integer b) {\n" +
"        assert b != null;\n" +
"        if (a != b) {\n" +
"        }\n" +
"    }\n" +
"}"
                )
                .run(BoxedIdentityComparison.class).
                assertWarnings("4:12-4:18:verifier:Integer values compared using == or !=").
                findWarning("4:12-4:18:verifier:Integer values compared using == or !=").
                applyFix("Replace with '!b.equals(a)'").
                assertOutput(
                "package test;\n" +
"public final class Test {\n" +
"    public static void test(Integer a, Integer b) {\n" +
"        assert b != null;\n" +
"        if (!b.equals(a)) {\n" +
"        }\n" +
"    }\n" +
"}"
                );
    }

    public void testBoxedNotEqualsRhsNotNull2() throws Exception {
        HintTest.create()
                .input(
                "package test;\n" +
"public final class Test {\n" +
"    public static void test(Boolean b) {\n" +
"        if (b != Boolean.FALSE) {\n" +
"        }\n" +
"    }\n" +
"}"
                )
                .run(BoxedIdentityComparison.class).
                assertWarnings("3:12-3:30:verifier:Boolean values compared using == or !=").
                findWarning("3:12-3:30:verifier:Boolean values compared using == or !=").
                applyFix("Replace with '!Boolean.FALSE.equals(b)'").
                assertOutput(
                "package test;\n" +
"public final class Test {\n" +
"    public static void test(Boolean b) {\n" +
"        if (!Boolean.FALSE.equals(b)) {\n" +
"        }\n" +
"    }\n" +
"}"
                );
    }

    public void testNoFixInSource6AndBothSidesPossiblyNull() throws Exception {
        HintTest.create().sourceLevel("1.6")
                .input(
                "package test;\n" +
"public final class Test {\n" +
"    public static void test(Integer a, Integer b) {\n" +
"        if (a != b) {\n" +
"        }\n" +
"    }\n" +
"}"
                )
                .run(BoxedIdentityComparison.class).
                assertWarnings("3:12-3:18:verifier:Integer values compared using == or !=").
                findWarning("3:12-3:18:verifier:Integer values compared using == or !=").assertFixes();
    }

    public void testBoxedEqualsWithPrimitiveRight() throws Exception {
        HintTest.create()
                .input(
                "package test;\n" +
"public final class Test {\n" +
"    public static void main(String[] args) {\n" +
"        Integer a = 1;\n" +
"        int b = 2;\n" +
"        if (a != b) {\n" +
"        }\n" +
"    }\n" +
"}"
                )
                .run(BoxedIdentityComparison.class).
                assertWarnings(
                );
    }

    public void testBoxedEqualsWithPrimitiveLeft() throws Exception {
        HintTest.create()
                .input(
                "package test;\n" +
"public final class Test {\n" +
"    public static void main(String[] args) {\n" +
"        int a = 1;\n" +
"        Integer b = 2;\n" +
"        if (a != b) {\n" +
"        }\n" +
"    }\n" +
"}"
                )
                .run(BoxedIdentityComparison.class).
                assertWarnings(
                );
    }
}
