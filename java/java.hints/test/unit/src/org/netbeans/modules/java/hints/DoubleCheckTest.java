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
 * @author Jaroslav Tulach
 */
public class DoubleCheckTest extends NbTestCase {

    public DoubleCheckTest(String testName) {
        super(testName);
    }

    /**
     * The check will not report a code which is 
     * @throws Exception 
     */
    public void testDoNotReportVolatileButNoLocal() throws Exception {
        String code = "package test;\n" +
            "public class Test {\n" +
            "    private volatile String f;\n" +
            "    public void t() {\n" +
            "        if (f == null) {\n" +
            "            synchronized (this) {\n" +
            "                if (f == null) {\n" +
            "                    f = \"\";\n" +
            "                }\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "}";
        HintTest.
                create().
                input(code).
                run(DoubleCheck.class).
                findWarning("5:12-5:24:verifier:ERR_DoubleCheck").
                assertFixes("Use local variable for better performance");
    }
    
    public void testClassWithOnlyStaticMethods() throws Exception {
        String before = "package test; public class Test {" +
                        "  private static Test INST;" +
                        "public static Test factory() {" +
                        "  if (INST == null) {" +
                        "    synchro";
        String after = "nized (Test.class) {" +
                       "      if (INST == null) {" +
                       "        INST = new Test();" +
                       "      }" +
                       "    }" +
                       "  }" +
                       "  return INST;" +
                       "}" +
                       "}";

        HintTest
                .create()
                .input(before + after)
                .run(DoubleCheck.class)
                .assertWarnings("0:115-0:127:verifier:ERR_DoubleCheck");
    }

    public void testSomeCodeAfterTheOuterIf() throws Exception {
        String before = "package test; public class Test {" +
                        "  private static Test INST;" +
                        "  private static int cnt;" +
                        "public static Test factory() {" +
                        "  if (INST == null) {" +
                        "    synchro";
        String after = "nized (Test.class) {" +
                       "      if (INST == null) {" +
                       "        INST = new Test();" +
                       "      }" +
                       "    }" +
                       "    cnt++;" +
                       "  }" +
                       "  return INST;" +
                       "}" +
                       "}";
        // no hint, probably
        HintTest
                .create()
                .input(before + after)
                .run(DoubleCheck.class)
                .assertWarnings();
    }

    public void testDifferentVariable() throws Exception {
        String before = "package test; public class Test {" +
                        "  private static Test INST;" +
                        "  private static Object cnt;" +
                        "public static Test factory() {" +
                        "  if (cnt == null) {" +
                        "    synchro";
        String after = "nized (Test.class) {" +
                       "      if (INST == null) {" +
                       "        INST = new Test();" +
                       "      }" +
                       "    }" +
                       "  }" +
                       "  return INST;" +
                       "}" +
                       "}";
        // no hint, for sure
        HintTest
                .create()
                .input(before + after)
                .run(DoubleCheck.class)
                .assertWarnings();
    }

    public void testNoNPEWhenBrokenCondition() throws Exception {
        String before = "package test; public class Test {" +
                        "  private static Test INST;" +
                        "  private static Object cnt;" +
                        "public static Test factory() {" +
                        "  if (INST == nu) {" +
                        "    synchro";
        String after = "nized (Test.class) {" +
                       "      if (INST == nu) {" +
                       "        INST = new Test();" +
                       "      }" +
                       "    }" +
                       "  }" +
                       "  return INST;" +
                       "}" +
                       "}";
        // no hint, for sure
        HintTest
                .create()
                .input(before + after, false)
                .run(DoubleCheck.class)
                .assertWarnings();
    }

    public void testApplyClassWithOnlyStaticMethods() throws Exception {
        String before1 = "package test; public class Test {\n" +
                         "private static Test INST;\n" +
                         "public static Test factory() {\n";
        String before2 =
                "if (INST == null) {\n";
        String before3 =
                "synchro";
        String after1 = "nized (INST) {\n" +
                        "if (INST == null) {\n" +
                        "INST = new test.Test();\n" +
                        "}\n" +
                        "}\n";
        String after2 =
                "}\n";
        String after3 =
                "return INST;\n" +
                "}" +
                "}\n";
        String after4 = "\n";

        String before = before1 + before2 + before3;
        String after = after1 + after2 + after3 + after4;

        String golden = (before1 + before3 + after1 + after3)
                .replace("\n", " ");
        HintTest
                .create()
                .input(before + after)
                .run(DoubleCheck.class)
                .findWarning("4:0-4:12:verifier:ERR_DoubleCheck")
                .applyFix("FIX_DoubleCheck")
                .assertCompilable()
                .assertOutput(golden);
    }

    public void testVolatileJDK5IZ153334() throws Exception {
        String code = "package test; public class Test {\n" +
                      "private static volatile Test INST;\n" +
                      "public static Test factory() {\n" +
                      "if (INST == null) {\n" +
                      "synchronized (INST) {\n" +
                      "if (INST == null) {\n" +
                      "INST = new test.Test();\n" +
                      "}\n" +
                      "}\n" +
                      "}\n" +
                      "return INST;\n" +
                      "}" +
                      "}\n";

        HintTest
                .create()
                .input(code)
                .run(DoubleCheck.class)
                .findWarning("4:0-4:12:verifier:ERR_DoubleCheck").
                assertFixes("Use local variable for better performance");
    }

    public void testVolatileJDK4IZ153334() throws Exception {
        String code = "package test; public class Test {\n" +
                      "private static volatile Test INST;\n" +
                      "public static Test factory() {\n" +
                      "if (INST == null) {\n" +
                      "synchronized (INST) {\n" +
                      "if (INST == null) {\n" +
                      "INST = new test.Test();\n" +
                      "}\n" +
                      "}\n" +
                      "}\n" +
                      "return INST;\n" +
                      "}" +
                      "}\n";

        HintTest
                .create()
                .input(code)
                .sourceLevel("1.4")
                .run(DoubleCheck.class)
                .assertWarnings("4:0-4:12:verifier:ERR_DoubleCheck");
    }
}