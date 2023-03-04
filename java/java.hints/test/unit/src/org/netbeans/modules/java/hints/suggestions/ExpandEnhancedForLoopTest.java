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
package org.netbeans.modules.java.hints.suggestions;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;

/**
 *
 * @author Jan Lahoda
 */
public class ExpandEnhancedForLoopTest extends NbTestCase {

    public ExpandEnhancedForLoopTest(String name) {
        super(name);
    }

    public void testSimple1() throws Exception {
        HintTest
                .create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private void test() {\n" +
                       "        fo|r (String s : java.util.Arrays.asList(\"a\")) {\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(ExpandEnhancedForLoop.class)
                .findWarning("3:10-3:10:verifier:Convert to long for loop")
                .applyFix("Convert to long for loop")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "import java.util.Iterator;\n" +
                              "public class Test {\n" +
                              "    private void test() {\n" +
                              "        for (Iterator<String> it = java.util.Arrays.asList(\"a\").iterator(); it.hasNext();) {" +
                              "            String s = it.next();\n" +
                              "        }\n" +
                              "    }\n" +
                              "}\n");
    }

    public void testSimple2() throws Exception {
        HintTest
                .create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private void test() {" +
                       "        java.util.List<? extends CharSequence> l = null;\n" +
                       "        fo|r (CharSequence c : l) {\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n")
                .run(ExpandEnhancedForLoop.class)
                .findWarning("3:10-3:10:verifier:Convert to long for loop")
                .applyFix("Convert to long for loop")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "import java.util.Iterator;\n" +
                              "public class Test {\n" +
                              "    private void test() {\n" +
                              "        java.util.List<? extends CharSequence> l = null;\n" +
                              "        for (Iterator<? extends CharSequence> it = l.iterator(); it.hasNext();) {" +
                              "            CharSequence c = it.next();\n" +
                              "        }\n" +
                              "    }\n" +
                              "}\n");
    }

    public void testNoBlock() throws Exception {
        HintTest
                .create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private void test() {" +
                       "        java.util.List<? extends CharSequence> l = null;\n" +
                       "        fo|r (CharSequence c : l)\n" +
                       "            System.err.println(c);\n" +
                       "    }\n" +
                       "}\n")
                .run(ExpandEnhancedForLoop.class)
                .findWarning("3:10-3:10:verifier:Convert to long for loop")
                .applyFix("Convert to long for loop")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "import java.util.Iterator;\n" +
                              "public class Test {\n" +
                              "    private void test() {\n" +
                              "        java.util.List<? extends CharSequence> l = null;\n" +
                              "        for (Iterator<? extends CharSequence> it = l.iterator(); it.hasNext();) {" +
                              "            CharSequence c = it.next();\n" +
                              "            System.err.println(c);\n" +
                              "        }\n" +
                              "    }\n" +
                              "}\n");
    }

    public void testEmptyStatement() throws Exception {
        HintTest
                .create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private void test() {" +
                       "        java.util.List<? extends CharSequence> l = null;\n" +
                       "        fo|r (CharSequence c : l);\n" +
                       "    }\n" +
                       "}\n")
                .run(ExpandEnhancedForLoop.class)
                .findWarning("3:10-3:10:verifier:Convert to long for loop")
                .applyFix("Convert to long for loop")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "import java.util.Iterator;\n" +
                              "public class Test {\n" +
                              "    private void test() {\n" +
                              "        java.util.List<? extends CharSequence> l = null;\n" +
                              "        for (Iterator<? extends CharSequence> it = l.iterator(); it.hasNext();) {" +
                              "            CharSequence c = it.next();\n" +
                              "        }\n" +
                              "    }\n" +
                              "}\n");
    }

    public void testNegative() throws Exception {
        HintTest
                .create()
                .setCaretMarker('|')
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private void test() {\n" +
                       "        fo|r (String s : new Object()) {\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n", false)
                .run(ExpandEnhancedForLoop.class)
                .assertWarnings();
    }
}