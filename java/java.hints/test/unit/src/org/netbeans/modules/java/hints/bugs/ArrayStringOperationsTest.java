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
public class ArrayStringOperationsTest extends NbTestCase {

    public ArrayStringOperationsTest(String name) {
        super(name);
    }
    
    /**
     * Checks that the exact warnings are printed
     * @throws Exception 
     */
    public void testDetectArrayStrings() throws Exception {
        HintTest.create()
                .input(
                "package test;\n" +
                "\n" +
                "import java.io.PrintStream;\n" +
                "import java.text.MessageFormat;\n" +
                "import java.util.Locale;\n" +
                "\n" +
                "public class Test {\n" +
                "    private int[] intArr;\n" +
                "    private Object[] objArr;\n" +
                "    private PrintStream stream;\n" +
                "    \n" +
                "    public void test() {\n" +
                "        String si = intArr.toString();\n" +
                "        String so = objArr.toString();\n" +
                "        // this is OK\n" +
                "        String s2 = String.format(\"ee\", objArr);\n" +
                "        String s1 = String.format(\"ee\", intArr, 2);\n" +
                "        String s3 = MessageFormat.format(\"eee\", intArr);\n" +
                "        stream.format(\"ee\", intArr);\n" +
                "        // not ok, not a last parameter\n" +
                "        stream.format(\"ee\", objArr, 2);\n" +
                "        System.err.format(Locale.getDefault(), \"ee\", objArr, 1);\n" +
                "        stream.print(intArr);\n" +
                "        stream.println(intArr);\n" +
                "        s1 = s2 + intArr;\n" +
                "        s1 = objArr + s2;\n" +
                "    }\n" +
                "}"
                )
                .run(ArrayStringConversions.class).
                assertWarnings(
                "12:20-12:37:verifier:toString() called on array instance",
                "13:20-13:37:verifier:toString() called on array instance",
                "16:40-16:46:verifier:Array instance passed as parameter to a formatter function", 
                "17:48-17:54:verifier:Array instance passed as parameter to a formatter function", 
                "18:28-18:34:verifier:Array instance passed as parameter to a formatter function", 
                "20:28-20:34:verifier:Array instance passed as parameter to a formatter function", 
                "21:53-21:59:verifier:Array instance passed as parameter to a formatter function", 
                "22:21-22:27:verifier:Array instance printed on PrintStream",
                "23:23-23:29:verifier:Array instance printed on PrintStream",
                "24:18-24:24:verifier:Array concatenated with String", 
                "25:13-25:19:verifier:Array concatenated with String"
                );
    }
    
    /**
     * .toString() is converted in a different way than Array passing, check the fix is OK
     * @throws Exception 
     */
    public void testExplicitArraysToString() throws Exception {
        HintTest.create()
                .input(
                "package test;\n" +
                "\n" +
                "import java.io.PrintStream;\n" +
                "import java.text.MessageFormat;\n" +
                "import java.util.Locale;\n" +
                "\n" +
                "public class Test {\n" +
                "    private int[] intArr;\n" +
                "    private Object[] objArr;\n" +
                "    private PrintStream stream;\n" +
                "    \n" +
                "    public void test() {\n" +
                "        String si = intArr.toString();\n" +
                "    }\n" +
                "}"
                )
                .run(ArrayStringConversions.class).
                findWarning(
                "12:20-12:37:verifier:toString() called on array instance"
                ).assertFixes("Wrap array using Arrays.toString").applyFix().
                assertOutput(
                "package test;\n" +
                "\n" +
                "import java.io.PrintStream;\n" +
                "import java.text.MessageFormat;\n" +
                "import java.util.Arrays;\n" +
                "import java.util.Locale;\n" +
                "\n" +
                "public class Test {\n" +
                "    private int[] intArr;\n" +
                "    private Object[] objArr;\n" +
                "    private PrintStream stream;\n" +
                "    \n" +
                "    public void test() {\n" +
                "        String si = Arrays.toString(intArr);\n" +
                "    }\n" +
                "}"
                );
    }

    public void testExplicitArraysToDeepString() throws Exception {
        HintTest.create()
                .input(
                "package test;\n" +
                "\n" +
                "import java.io.PrintStream;\n" +
                "import java.text.MessageFormat;\n" +
                "import java.util.Locale;\n" +
                "\n" +
                "public class Test {\n" +
                "    private int[] intArr;\n" +
                "    private Object[] objArr;\n" +
                "    private PrintStream stream;\n" +
                "    \n" +
                "    public void test() {\n" +
                "        String si = objArr.toString();\n" +
                "    }\n" +
                "}"
                )
                .run(ArrayStringConversions.class).
                findWarning(
                "12:20-12:37:verifier:toString() called on array instance"
                ).assertFixes(
                    "Wrap array using Arrays.toString",
                    "Wrap array using Arrays.deepToString").
                applyFix("Wrap array using Arrays.deepToString").
                assertOutput(
                "package test;\n" +
                "\n" +
                "import java.io.PrintStream;\n" +
                "import java.text.MessageFormat;\n" +
                "import java.util.Arrays;\n" +
                "import java.util.Locale;\n" +
                "\n" +
                "public class Test {\n" +
                "    private int[] intArr;\n" +
                "    private Object[] objArr;\n" +
                "    private PrintStream stream;\n" +
                "    \n" +
                "    public void test() {\n" +
                "        String si = Arrays.deepToString(objArr);\n" +
                "    }\n" +
                "}"
                );
    }
    
    public void testReplaceArrayWithToString() throws Exception {
        HintTest.create()
                .input(
                "package test;\n" +
                "\n" +
                "import java.io.PrintStream;\n" +
                "import java.text.MessageFormat;\n" +
                "import java.util.Locale;\n" +
                "\n" +
                "public class Test {\n" +
                "    private int[] intArr;\n" +
                "    private Object[] objArr;\n" +
                "    private PrintStream stream;\n" +
                "    \n" +
                "    public void test() {\n" +
                "        String s1 = String.format(\"ee\", intArr, 2);\n" +
                "    }\n" +
                "}"
                )
                .run(ArrayStringConversions.class).
                findWarning(
                "12:40-12:46:verifier:Array instance passed as parameter to a formatter function"
                ).assertFixes("Wrap array using Arrays.toString").applyFix().
                assertOutput(
                "package test;\n" +
                "\n" +
                "import java.io.PrintStream;\n" +
                "import java.text.MessageFormat;\n" +
                "import java.util.Arrays;\n" +
                "import java.util.Locale;\n" +
                "\n" +
                "public class Test {\n" +
                "    private int[] intArr;\n" +
                "    private Object[] objArr;\n" +
                "    private PrintStream stream;\n" +
                "    \n" +
                "    public void test() {\n" +
                "        String s1 = String.format(\"ee\", Arrays.toString(intArr), 2);\n" +
                "    }\n" +
                "}"
                );
    }

    public void testReplaceArrayWithDeepToString() throws Exception {
        HintTest.create()
                .input(
                "package test;\n" +
                "\n" +
                "import java.io.PrintStream;\n" +
                "import java.text.MessageFormat;\n" +
                "import java.util.Locale;\n" +
                "\n" +
                "public class Test {\n" +
                "    private int[] intArr;\n" +
                "    private Object[] objArr;\n" +
                "    private PrintStream stream;\n" +
                "    \n" +
                "    public void test() {\n" +
                "        System.err.format(Locale.getDefault(), \"ee\", objArr, 1);\n" +
                "    }\n" +
                "}"
                )
                .run(ArrayStringConversions.class).
                findWarning(
                "12:53-12:59:verifier:Array instance passed as parameter to a formatter function"
                ).assertFixes(
                    "Wrap array using Arrays.toString",
                    "Wrap array using Arrays.deepToString").
                applyFix("Wrap array using Arrays.deepToString").
                assertOutput(
                "package test;\n" +
                "\n" +
                "import java.io.PrintStream;\n" +
                "import java.text.MessageFormat;\n" +
                "import java.util.Arrays;\n" +
                "import java.util.Locale;\n" +
                "\n" +
                "public class Test {\n" +
                "    private int[] intArr;\n" +
                "    private Object[] objArr;\n" +
                "    private PrintStream stream;\n" +
                "    \n" +
                "    public void test() {\n" +
                "        System.err.format(Locale.getDefault(), \"ee\", Arrays.deepToString(objArr), 1);\n" +
                "    }\n" +
                "}"
                );
    }
    
    public void testReplaceArrayConcatenation() throws Exception {
        HintTest.create()
                .input(
                "package test;\n" +
                "\n" +
                "import java.io.PrintStream;\n" +
                "import java.text.MessageFormat;\n" +
                "import java.util.Locale;\n" +
                "\n" +
                "public class Test {\n" +
                "    private int[] intArr;\n" +
                "    private Object[] objArr;\n" +
                "    private PrintStream stream;\n" +
                "    \n" +
                "    public void test() {\n" +
                "        String s1 = objArr + \"\";\n" +
                "    }\n" +
                "}"
                )
                .run(ArrayStringConversions.class).
                findWarning(
                "12:20-12:26:verifier:Array concatenated with String"
                ).assertFixes(
                    "Wrap array using Arrays.toString",
                    "Wrap array using Arrays.deepToString").
                applyFix("Wrap array using Arrays.deepToString").
                assertOutput(
                "package test;\n" +
                "\n" +
                "import java.io.PrintStream;\n" +
                "import java.text.MessageFormat;\n" +
                "import java.util.Arrays;\n" +
                "import java.util.Locale;\n" +
                "\n" +
                "public class Test {\n" +
                "    private int[] intArr;\n" +
                "    private Object[] objArr;\n" +
                "    private PrintStream stream;\n" +
                "    \n" +
                "    public void test() {\n" +
                "        String s1 = Arrays.deepToString(objArr) + \"\";\n" +
                "    }\n" +
                "}"
                );
    }


    /**
     * Checks that null concatenated with String does not produce any warning
     */
    public void testNullConcatenation() throws Exception {
        HintTest.create()
                .input(
                "package test;\n" +
                "\n" +
                "public class Test {\n" +
                "    \n" +
                "    public void test() {\n" +
                "        String s = \"ahoj\";\n" +
                "        System.err.println(s + null);\n" +
                "    }\n" +
                "}"
                )
                .run(ArrayStringConversions.class).
                assertWarnings();
    }
}
