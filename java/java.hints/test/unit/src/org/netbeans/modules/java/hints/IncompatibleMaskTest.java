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

import org.junit.Test;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;
import org.openide.util.NbBundle;

/**
 *
 * @author Jan Jancura
 */
public class IncompatibleMaskTest extends NbTestCase {

    public IncompatibleMaskTest(String name) {
        super(name);
    }

    @Test
    public void testWarning1() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    void test () {\n" +
                       "        int i = 10;\n" +
                       "        boolean b = (i & 6) == 1;\n" +
                       "    }\n" +
                       "}")
                .run(IncompatibleMask.class)
                .assertWarnings(
                "4:20-4:32:verifier:IncompatibleMask: (i & 6) == 1 is always false");
    }

    @Test
    public void testWarning2() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    void test () {\n" +
                       "        int i = 10;\n" +
                       "        boolean b = 1 == (i & 6);\n" +
                       "    }\n" +
                       "}")
                .run(IncompatibleMask.class)
                .assertWarnings(
                "4:20-4:32:verifier:IncompatibleMask: 1 == (i & 6) is always false");
    }

    @Test
    public void testWarning3() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    void test () {\n" +
                       "        int i = 10;\n" +
                       "        boolean b = (6 & i) == 1;\n" +
                       "    }\n" +
                       "}")
                .run(IncompatibleMask.class)
                .assertWarnings(
                "4:20-4:32:verifier:IncompatibleMask: (6 & i) == 1 is always false");
    }

    @Test
    public void testWarning4() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    void test () {\n" +
                       "        int i = 10;\n" +
                       "        boolean b = 1 == (6 & i);\n" +
                       "    }\n" +
                       "}")
                .run(IncompatibleMask.class)
                .assertWarnings(
                "4:20-4:32:verifier:IncompatibleMask: 1 == (6 & i) is always false");
    }

    @Test
    public void testWarning5() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    void test () {\n" +
                       "        int i = 10;\n" +
                       "        boolean b = (i | 6) == 2;\n" +
                       "    }\n" +
                       "}")
                .run(IncompatibleMask.class)
                .assertWarnings(
                "4:20-4:32:verifier:IncompatibleMask: (i | 6) == 2 is always false");
    }

    @Test
    public void testWarning6() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    void test () {\n" +
                       "        int i = 10;\n" +
                       "        boolean b = 2 == (i | 6);\n" +
                       "    }\n" +
                       "}")
                .run(IncompatibleMask.class)
                .assertWarnings(
                "4:20-4:32:verifier:IncompatibleMask: 2 == (i | 6) is always false");
    }

    @Test
    public void testWarning7() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    void test () {\n" +
                       "        int i = 10;\n" +
                       "        boolean b = (6 | i) == 2;\n" +
                       "    }\n" +
                       "}")
                .run(IncompatibleMask.class)
                .assertWarnings(
                "4:20-4:32:verifier:IncompatibleMask: (6 | i) == 2 is always false");
    }

    @Test
    public void testWarning8() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    void test () {\n" +
                       "        int i = 10;\n" +
                       "        boolean b = 2 == (6 | i);\n" +
                       "    }\n" +
                       "}")
                .run(IncompatibleMask.class)
                .assertWarnings(
                "4:20-4:32:verifier:IncompatibleMask: 2 == (6 | i) is always false");
    }

    @Test
    public void testWarning11() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    static final int a = 6;\n" +
                       "    static final int c = 1;\n" +
                       "    void test () {\n" +
                       "        int i = 10;\n" +
                       "        boolean b = (i & a) == c;\n" +
                       "    }\n" +
                       "}")
                .run(IncompatibleMask.class)
                .assertWarnings(
                "6:20-6:32:verifier:IncompatibleMask: (i & a) == c is always false");
    }

    @Test
    public void testWarning12() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    static final int a = 6;\n" +
                       "    static final int c = 1;\n" +
                       "    void test () {\n" +
                       "        int i = 10;\n" +
                       "        boolean b = c == (i & a);\n" +
                       "    }\n" +
                       "}")
                .run(IncompatibleMask.class)
                .assertWarnings(
                "6:20-6:32:verifier:IncompatibleMask: c == (i & a) is always false");
    }

    @Test
    public void testWarning13() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    static final int a = 6;\n" +
                       "    static final int c = 1;\n" +
                       "    void test () {\n" +
                       "        int i = 10;\n" +
                       "        boolean b = (a & i) == c;\n" +
                       "    }\n" +
                       "}")
                .run(IncompatibleMask.class)
                .assertWarnings(
                "6:20-6:32:verifier:IncompatibleMask: (a & i) == c is always false");
    }

    @Test
    public void testWarning14() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    static final int a = 6;\n" +
                       "    static final int c = 1;\n" +
                       "    void test () {\n" +
                       "        int i = 10;\n" +
                       "        boolean b = c == (a & i);\n" +
                       "    }\n" +
                       "}")
                .run(IncompatibleMask.class)
                .assertWarnings(
                "6:20-6:32:verifier:IncompatibleMask: c == (a & i) is always false");
    }

    @Test
    public void testWarning15() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    static final int a = 6;\n" +
                       "    static final int c = 2;\n" +
                       "    void test () {\n" +
                       "        int i = 10;\n" +
                       "        boolean b = (i | a) == c;\n" +
                       "    }\n" +
                       "}")
                .run(IncompatibleMask.class)
                .assertWarnings(
                "6:20-6:32:verifier:IncompatibleMask: (i | a) == c is always false");
    }

    @Test
    public void testWarning16() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    static final int a = 6;\n" +
                       "    static final int c = 2;\n" +
                       "    void test () {\n" +
                       "        int i = 10;\n" +
                       "        boolean b = c == (i | a);\n" +
                       "    }\n" +
                       "}")
                .run(IncompatibleMask.class)
                .assertWarnings(
                "6:20-6:32:verifier:IncompatibleMask: c == (i | a) is always false");
    }

    @Test
    public void testWarning17() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    static final int a = 6;\n" +
                       "    static final int c = 2;\n" +
                       "    void test () {\n" +
                       "        int i = 10;\n" +
                       "        boolean b = (a | i) == c;\n" +
                       "    }\n" +
                       "}")
                .run(IncompatibleMask.class)
                .assertWarnings(
                "6:20-6:32:verifier:IncompatibleMask: (a | i) == c is always false");
    }

    @Test
    public void testWarning18() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    static final int a = 6;\n" +
                       "    static final int c = 2;\n" +
                       "    void test () {\n" +
                       "        int i = 10;\n" +
                       "        boolean b = c == (a | i);\n" +
                       "    }\n" +
                       "}")
                .run(IncompatibleMask.class)
                .assertWarnings(
                "6:20-6:32:verifier:IncompatibleMask: c == (a | i) is always false");
    }

    static {
        NbBundle
                .setBranding("test");
    }
}