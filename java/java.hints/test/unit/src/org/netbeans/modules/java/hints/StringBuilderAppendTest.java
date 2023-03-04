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
 * @author lahvac
 */
public class StringBuilderAppendTest extends NbTestCase {

    public StringBuilderAppendTest(String name) {
        super(name);
    }

    public void testStringBuilder() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private void test(int a, int b) {\n" +
                       "        StringBuilder sb = new StringBuilder();\n" +
                       "        sb.append(\"a\" + \"b\" + a + \"c\" + b);\n" +
                       "    }\n" +
                       "}\n")
                .run(StringBuilderAppend.class)
                .findWarning("4:18-4:41:verifier:String concatenation in StringBuilder.append")
                .applyFix("FIX_StringBuilderAppend")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    private void test(int a, int b) {\n" +
                              "        StringBuilder sb = new StringBuilder();\n" +
                              "        sb.append(\"ab\").append(a).append(\"c\").append(b);\n" +
                              "    }\n" +
                              "}\n");
    }

    public void testStringBuffer() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private void test(int a, int b) {\n" +
                       "        StringBuffer sb = new StringBuffer();\n" +
                       "        sb.append(\"a\" + \"b\" + a + \"c\" + b);\n" +
                       "    }\n" +
                       "}\n")
                .run(StringBuilderAppend.class)
                .findWarning("4:18-4:41:verifier:String concatenation in StringBuffer.append")
                .applyFix("FIX_StringBuilderAppend")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    private void test(int a, int b) {\n" +
                              "        StringBuffer sb = new StringBuffer();\n" +
                              "        sb.append(\"ab\").append(a).append(\"c\").append(b);\n" +
                              "    }\n" +
                              "}\n");
    }

    public void testParenthesised() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private void test(int a, int b) {\n" +
                       "        StringBuffer sb = new StringBuffer();\n" +
                       "        sb.append((\"a\" + \"b\") + a + (\"c\" + CONST));\n" +
                       "    }\n" +
                       "    private static final String CONST = \"d\";\n" +
                       "}\n")
                .run(StringBuilderAppend.class)
                .findWarning("4:18-4:49:verifier:String concatenation in StringBuffer.append")
                .applyFix("FIX_StringBuilderAppend")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    private void test(int a, int b) {\n" +
                              "        StringBuffer sb = new StringBuffer();\n" +
                              "        sb.append(\"ab\").append(a).append(\"c\" + CONST);\n" +
                              "    }\n" +
                              "    private static final String CONST = \"d\";\n" +
                              "}\n");
    }

    public void testNoString() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private void test(int a, int b) {\n" +
                       "        StringBuilder sb = new StringBuilder();\n" +
                       "        sb.append(a + b);\n" +
                       "    }\n" +
                       "}\n")
                .run(StringBuilderAppend.class)
                .assertWarnings();
    }

    public void testMoreArgs() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private void test(int a, int b) {\n" +
                       "        StringBuilder sb = new StringBuilder();\n" +
                       "        sb.append(\"a\" + a + \"b\" + b, 1, 2);\n" +
                       "    }\n" +
                       "}\n")
                .run(StringBuilderAppend.class)
                .assertWarnings();
    }

    public void testOneCluster() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private void test(int a, int b) {\n" +
                       "        StringBuilder sb = new StringBuilder();\n" +
                       "        sb.append(\"a\" + \"b\");\n" +
                       "    }\n" +
                       "}\n")
                .run(StringBuilderAppend.class)
                .assertWarnings();
    }

    public void testTrailingCluster() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private void test(int a, int b) {\n" +
                       "        StringBuffer sb = new StringBuffer();\n" +
                       "        sb.append((\"a\" + \"b\") + a + \"c\");\n" +
                       "    }\n" +
                       "    private static final String CONST = \"d\";\n" +
                       "}\n")
                .run(StringBuilderAppend.class)
                .findWarning("4:18-4:39:verifier:String concatenation in StringBuffer.append")
                .applyFix("FIX_StringBuilderAppend")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    private void test(int a, int b) {\n" +
                              "        StringBuffer sb = new StringBuffer();\n" +
                              "        sb.append(\"ab\").append(a).append(\"c\");\n" +
                              "    }\n" +
                              "    private static final String CONST = \"d\";\n" +
                              "}\n");
    }

    public void testNoConst() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private void test(int a, int b) {\n" +
                       "        StringBuffer sb = new StringBuffer();\n" +
                       "        sb.append(\"a\" + CONST);\n" +
                       "    }\n" +
                       "    private static String CONST = \"d\";\n" +
                       "}\n")
                .run(StringBuilderAppend.class)
                .findWarning("4:18-4:29:verifier:String concatenation in StringBuffer.append")
                .applyFix("FIX_StringBuilderAppend")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "    private void test(int a, int b) {\n" +
                              "        StringBuffer sb = new StringBuffer();\n" +
                              "        sb.append(\"a\").append(CONST);\n" +
                              "    }\n" +
                              "    private static String CONST = \"d\";\n" +
                              "}\n");
    }
}