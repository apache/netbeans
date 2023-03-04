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
 * @author sdedic
 */
public class BoxedIdentityComparisonTest extends NbTestCase {

    public BoxedIdentityComparisonTest(String name) {
        super(name);
    }
    
    public void testBoxedEquals() throws Exception {
        HintTest.create().sourceLevel("1.7")
                .input(
                "package test;\n" + 
"public final class Test {\n" +
"    public static void main(String[] args) {\n" +
"        Integer a = 1;\n" +
"        Integer b = 2;\n" +
"        if (a == b) {\n" +
"        }\n" +
"    }\n" +
"}"
                )
                .run(BoxedIdentityComparison.class).
                assertWarnings("5:12-5:18:verifier:Integer values compared using == or !=").
                findWarning("5:12-5:18:verifier:Integer values compared using == or !=").
                applyFix("Replace with null-safe equals()").
                assertOutput(
                "package test;\n" + 
                "import java.util.Objects;\n" +
"public final class Test {\n" +
"    public static void main(String[] args) {\n" +
"        Integer a = 1;\n" +
"        Integer b = 2;\n" +
"        if (Objects.equals(a, b)) {\n" +
"        }\n" +
"    }\n" +
"}"
                );
    }
    
    public void testBoxedNotEquals() throws Exception {
        HintTest.create().sourceLevel("1.7")
                .input(
                "package test;\n" +
"public final class Test {\n" +
"    public static void main(String[] args) {\n" +
"        Integer a = 1;\n" +
"        Integer b = 2;\n" +
"        if (a != b) {\n" +
"        }\n" +
"    }\n" +
"}"
                )
                .run(BoxedIdentityComparison.class).
                assertWarnings("5:12-5:18:verifier:Integer values compared using == or !=").
                findWarning("5:12-5:18:verifier:Integer values compared using == or !=").
                applyFix("Replace with null-safe equals()").
                assertOutput(
                "package test;\n" + 
                "import java.util.Objects;\n" +
"public final class Test {\n" +
"    public static void main(String[] args) {\n" +
"        Integer a = 1;\n" +
"        Integer b = 2;\n" +
"        if (!Objects.equals(a, b)) {\n" +
"        }\n" +
"    }\n" +
"}"
                );
    }
    
    public void testNoFixInSource6() throws Exception {
        HintTest.create().sourceLevel("1.6")
                .input(
                "package test;\n" +
"public final class Test {\n" +
"    public static void main(String[] args) {\n" +
"        Integer a = 1;\n" +
"        Integer b = 2;\n" +
"        if (a != b) {\n" +
"        }\n" +
"    }\n" +
"}"
                )
                .run(BoxedIdentityComparison.class).
                assertWarnings("5:12-5:18:verifier:Integer values compared using == or !=").
                findWarning("5:12-5:18:verifier:Integer values compared using == or !=").assertFixes();
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
