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
package org.netbeans.modules.java.hints.perf;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.java.hints.test.api.HintTest;

/**
 *
 * @author lahvac
 */
public class TinyTest extends NbTestCase {

    public TinyTest(String name) {
        super(name);
    }

    public void testStringConstructor1() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private String test(String aa) {\n" +
                       "         return new String(aa);\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .findWarning("3:16-3:30:verifier:new String(...)")
                .applyFix("Remove new String(...)")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "     private String test(String aa) {\n" +
                              "         return aa;\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testStringConstructor2() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private String test(String aa) {\n" +
                       "         return new String(aa.substring(1));\n" +
                       "     }\n" +
                       "}\n")
                .preference(Tiny.SC_IGNORE_SUBSTRING, true)
                .run(Tiny.class)
                .assertWarnings();
    }

    public void testStringConstructor3() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private String test(String aa) {\n" +
                       "         return new String(aa.substring(1));\n" +
                       "     }\n" +
                       "}\n")
                .preference(Tiny.SC_IGNORE_SUBSTRING, false)
                .run(Tiny.class)
                .findWarning("3:16-3:43:verifier:new String(...)")
                .applyFix("Remove new String(...)")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "     private String test(String aa) {\n" +
                              "         return aa.substring(1);\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testStringConstructor4() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private String test(String aa) {\n" +
                       "         return new String(this.substring(1));\n" +
                       "     }\n" +
                       "     private String substring(int i) {return null;}\n" +
                       "}\n")
                .preference(Tiny.SC_IGNORE_SUBSTRING, true)
                .run(Tiny.class)
                .findWarning("3:16-3:45:verifier:new String(...)")
                .applyFix("Remove new String(...)")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "     private String test(String aa) {\n" +
                              "         return this.substring(1);\n" +
                              "     }\n" +
                              "     private String substring(int i) {return null;}\n" +
                              "}\n");
    }

    public void testStringEqualsEmpty1SL15() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private boolean test(String aa) {\n" +
                       "         return aa.equals(\"\");\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .findWarning("3:16-3:29:verifier:$string.equals(\"\")")
                .applyFix("$string.length() == 0")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "     private boolean test(String aa) {\n" +
                              "         return aa.length() == 0;\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testStringEqualsEmpty2SL15() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private boolean test(String aa) {\n" +
                       "         return !aa.equals(\"\");\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .findWarning("3:17-3:30:verifier:$string.equals(\"\")")
                .applyFix("$string.length() != 0")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "     private boolean test(String aa) {\n" +
                              "         return aa.length() != 0;\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testStringEqualsEmpty1_6() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private boolean test(String aa) {\n" +
                       "         return aa.equals(\"\");\n" +
                       "     }\n" +
                       "}\n")
                .sourceLevel("1.6")
                .run(Tiny.class)
                .findWarning("3:16-3:29:verifier:$string.equals(\"\")")
                .applyFix("$string.isEmpty()")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "     private boolean test(String aa) {\n" +
                              "         return aa.isEmpty();\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testStringEqualsIgnoreCaseEmpty1_6() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private boolean test(String aa) {\n" +
                       "         return aa.equalsIgnoreCase(\"\");\n" +
                       "     }\n" +
                       "}\n")
                .sourceLevel("1.6")
                .run(Tiny.class)
                .findWarning("3:16-3:39:verifier:$string.equals(\"\")")
                .applyFix("$string.isEmpty()")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "     private boolean test(String aa) {\n" +
                              "         return aa.isEmpty();\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testLengthOneStringIndexOf1() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private boolean test(String aa) {\n" +
                       "         return aa.indexOf(\"a\") != 0;\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .findWarning("3:27-3:30:verifier:indexOf(\"a\")")
                .applyFix("indexOf('.')")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "     private boolean test(String aa) {\n" +
                              "         return aa.indexOf(\'a\') != 0;\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testLengthOneStringIndexOf2() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private boolean test(String aa) {\n" +
                       "         return aa.indexOf(\"'\", 2) != 0;\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .findWarning("3:27-3:30:verifier:indexOf(\"'\")")
                .applyFix("indexOf('.')")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "     private boolean test(String aa) {\n" +
                              "         return aa.indexOf(\'\\'\', 2) != 0;\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testLengthOneStringIndexOf3() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private boolean test(String aa) {\n" +
                       "         return aa.indexOf(\"\\\"\", 2) != 0;\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .findWarning("3:27-3:31:verifier:indexOf(\"\\\"\")")
                .applyFix("indexOf('.')")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "     private boolean test(String aa) {\n" +
                              "         return aa.indexOf(\'\"\', 2) != 0;\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testLengthOneStringIndexOf206141a() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private boolean test(String aa) {\n" +
                       "         return aa.indexOf(\"\\\\\", 2) != 0;\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .findWarning("3:27-3:31:verifier:indexOf(\"\\\\\")")
                .applyFix("indexOf('.')")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "     private boolean test(String aa) {\n" +
                              "         return aa.indexOf(\'\\\\\', 2) != 0;\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testLengthOneStringIndexOf206141b() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private boolean test(String aa) {\n" +
                       "         return aa.indexOf(\"\\n\", 2) != 0;\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .findWarning("3:27-3:31:verifier:indexOf(\"\\n\")")
                .applyFix("indexOf('.')")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "     private boolean test(String aa) {\n" +
                              "         return aa.indexOf(\'\\n\', 2) != 0;\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testGetClassInsteadOfDotClass1() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private Class<?> test() {\n" +
                       "         return new String().getClass();\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .findWarning("3:16-3:39:verifier:ERR_GetClassInsteadOfDotClass")
                .applyFix("FIX_GetClassInsteadOfDotClass")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "     private Class<?> test() {\n" +
                              "         return String.class;\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testGetClassInsteadOfDotClass2() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "import java.util.LinkedList;\n" +
                       "public class Test {\n" +
                       "     private Class<?> test() {\n" +
                       "         return new LinkedList(java.util.Arrays.asList(1, 2)).getClass();\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .findWarning("4:16-4:72:verifier:ERR_GetClassInsteadOfDotClass")
                .applyFix("FIX_GetClassInsteadOfDotClass")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "import java.util.LinkedList;\n" +
                              "public class Test {\n" +
                              "     private Class<?> test() {\n" +
                              "         return LinkedList.class;\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testConstantIntern1() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private String test() {\n" +
                       "         return \"foo-bar\".intern();\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .findWarning("3:16-3:34:verifier:ERR_ConstantIntern")
                .applyFix("FIX_ConstantIntern")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "     private String test() {\n" +
                              "         return \"foo-bar\";\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testConstantIntern2() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private String test() {\n" +
                       "         return (\"foo\" + \"-\" + \"bar\").intern();\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .findWarning("3:16-3:46:verifier:ERR_ConstantIntern")
                .applyFix("FIX_ConstantIntern")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "     private String test() {\n" +
                              "         return \"foo\" + \"-\" + \"bar\";\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testConstantIntern3() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private int test() {\n" +
                       "         return (\"foo\" + \"-\" + \"bar\").intern().length();\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .findWarning("3:16-3:46:verifier:ERR_ConstantIntern")
                .applyFix("FIX_ConstantIntern")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "     private int test() {\n" +
                              "         return (\"foo\" + \"-\" + \"bar\").length();\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testEnumSet1() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private java.util.Set<java.lang.annotation.RetentionPolicy> test() {\n" +
                       "         return new java.util.HashSet<java.lang.annotation.RetentionPolicy>();\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .assertWarnings("3:16-3:77:verifier:ERR_Tiny_enumSet");
    }

    public void testEnumMap1() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private java.util.Map<java.lang.annotation.RetentionPolicy, Boolean> test() {\n" +
                       "         return new java.util.HashMap<java.lang.annotation.RetentionPolicy, Boolean>();\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .findWarning("3:16-3:86:verifier:ERR_Tiny_enumMap")
                .applyFix("FIX_Tiny_enumMap")
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "import java.util.EnumMap;\n" +
                              "public class Test {\n" +
                              "     private java.util.Map<java.lang.annotation.RetentionPolicy, Boolean> test() {\n" +
                              "         return new EnumMap<java.lang.annotation.RetentionPolicy, Boolean>(java.lang.annotation.RetentionPolicy.class);\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testEnumMap2() throws Exception {
        HintTest
                .create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private java.util.Map<java.lang.annotation.RetentionPolicy, Boolean> test() {\n" +
                       "         return new java.util.EnumMap<java.lang.annotation.RetentionPolicy, Boolean>(java.lang.annotation.RetentionPolicy.class);\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .assertWarnings();
    }
    
    public void testEnumMap218550() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private java.util.HashMap<java.lang.annotation.RetentionPolicy, Boolean> test() {\n" +
                       "         return new java.util.HashMap<java.lang.annotation.RetentionPolicy, Boolean>();\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .assertWarnings();
    }

    public void testCollectionsToArray() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private String[] test(java.util.Collection<String> col) {\n" +
                       "         return col.toArray(new String[col.size()]);\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .findWarning("3:20-3:27:verifier:ERR_Tiny_collectionsToArray")
                .applyFix()
                .assertCompilable()
                .assertOutput("package test;\n" +
                              "public class Test {\n" +
                              "     private String[] test(java.util.Collection<String> col) {\n" +
                              "         return col.toArray(new String[0]);\n" +
                              "     }\n" +
                              "}\n");
    }

    public void testCollectionsToArray2() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private java.util.Collection<String> col() { return null; }\n" +
                       "     private String[] test() {\n" +
                       "         return col().toArray(new String[0]);\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .findWarning("4:22-4:29:verifier:ERR_Tiny_collectionsToArray")
                .assertFixes();
    }

    public void testValueOfToParse1() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private void test() {\n" +
                       "         float prim = Float.parseFloat(\"5\");\n" +
                       "         prim = Float.valueOf(\"6\");\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .findWarning("4:16-4:34:verifier:Unnecessary temporary when converting from String")
                .applyFix()
                .assertCompilable()
                .assertOutput(
                       "package test;\n" +
                       "public class Test {\n" +
                       "     private void test() {\n" +
                       "         float prim = Float.parseFloat(\"5\");\n" +
                       "         prim = Float.parseFloat(\"6\");\n" +
                       "     }\n" +
                       "}\n");
    }

    public void testValueOfToParse2() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "import java.util.ArrayList;\n" +
                       "public class Test {\n" +
                       "     private void test() {\n" +
                       "         ArrayList arrayList = new ArrayList(Integer.valueOf(\"6\"));\n" +
                       "         arrayList.get(Integer.parseInt(\"1\"));\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .findWarning("4:45-4:65:verifier:Unnecessary temporary when converting from String")
                .applyFix()
                .assertCompilable()
                .assertOutput(
                       "package test;\n" +
                       "import java.util.ArrayList;\n" +
                       "public class Test {\n" +
                       "     private void test() {\n" +
                       "         ArrayList arrayList = new ArrayList(Integer.parseInt(\"6\"));\n" +
                       "         arrayList.get(Integer.parseInt(\"1\"));\n" +
                       "     }\n" +
                       "}\n")
                ;
    }

    public void testValueOfToParse3() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "import java.util.ArrayList;\n" +
                       "public class Test {\n" +
                       "     private void test() {\n" +
                       "         ArrayList arrayList = new ArrayList(Integer.parseInt(\"6\"));\n" +
                       "         arrayList.get(Integer.valueOf(\"1\"));\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .findWarning("5:23-5:43:verifier:Unnecessary temporary when converting from String")
                .applyFix()
                .assertCompilable()
                .assertOutput(
                       "package test;\n" +
                       "import java.util.ArrayList;\n" +
                       "public class Test {\n" +
                       "     private void test() {\n" +
                       "         ArrayList arrayList = new ArrayList(Integer.parseInt(\"6\"));\n" +
                       "         arrayList.get(Integer.parseInt(\"1\"));\n" +
                       "     }\n" +
                       "}\n");
    }

    public void testValueOfToParseInMethodReturn() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private int test() {\n" +
                       "         return Integer.valueOf(\"1\");\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .findWarning("3:16-3:36:verifier:Unnecessary temporary when converting from String")
                .applyFix()
                .assertCompilable()
                .assertOutput(
                       "package test;\n" +
                       "public class Test {\n" +
                       "     private int test() {\n" +
                       "         return Integer.parseInt(\"1\");\n" +
                       "     }\n" +
                       "}\n");
    }

    public void testParseToValueOfInLambdaReturn() throws Exception {
        HintTest.create()
                .sourceLevel("8")
                .input("package test;\n" +
                       "import java.util.concurrent.Callable;\n" +
                       "import java.util.concurrent.Executors;\n"  +
                       "public class Test {\n" +
                       "     private void test() {\n" +
                       "         Executors.newFixedThreadPool(1).submit(() -> {\n" +
                       "             return Integer.parseInt(\"1\");\n" +
                       "         });" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .findWarning("6:20-6:41:verifier:Unnecessary temporary when converting from String")
                .applyFix()
                .assertCompilable()
                .assertOutput(
                       "package test;\n" +
                       "import java.util.concurrent.Callable;\n" +
                       "import java.util.concurrent.Executors;\n"  +
                       "public class Test {\n" +
                       "     private void test() {\n" +
                       "         Executors.newFixedThreadPool(1).submit(() -> {\n" +
                       "             return Integer.valueOf(\"1\");\n" +
                       "         });" +
                       "     }\n" +
                       "}\n");
    }

    public void testParseToValueOf() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "import java.util.HashSet;\n" +
                       "public class Test {\n" +
                       "     private void test() {\n" +
                       "         new HashSet().add(Float.parseFloat(\"6\"));\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .findWarning("4:27-4:48:verifier:Unnecessary temporary when converting from String")
                .applyFix()
                .assertCompilable()
                .assertOutput(
                       "package test;\n" +
                       "import java.util.HashSet;\n" +
                       "public class Test {\n" +
                       "     private void test() {\n" +
                       "         new HashSet().add(Float.valueOf(\"6\"));\n" +
                       "     }\n" +
                       "}\n");
    }

    public void testNewObjectToValueOf_step1() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private void test() {\n" +
                       "         byte b = new Byte(\"5\");\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .findWarning("3:18-3:31:verifier:Replace usage of deprecated boxed primitive constructors with factory methods.")
                .applyFix()
                .assertCompilable()
                .assertOutput(
                       "package test;\n" +
                       "public class Test {\n" +
                       "     private void test() {\n" +
                       "         byte b = Byte.valueOf(\"5\");\n" +
                       "     }\n" +
                       "}\n");
    }

    public void testValueOfToParse_step2() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private void test() {\n" +
                       "         byte b = Byte.valueOf(\"5\");\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .findWarning("3:18-3:35:verifier:Unnecessary temporary when converting from String")
                .applyFix()
                .assertCompilable()
                .assertOutput(
                       "package test;\n" +
                       "public class Test {\n" +
                       "     private void test() {\n" +
                       "         byte b = Byte.parseByte(\"5\");\n" +
                       "     }\n" +
                       "}\n");
    }

    public void testValueOfToParseInBinaryOp() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "     private void test() {\n" +
                       "         if (0 < Byte.valueOf(\"5\")) {}\n" +
                       "     }\n" +
                       "}\n")
                .run(Tiny.class)
                .findWarning("3:17-3:34:verifier:Unnecessary temporary when converting from String")
                .applyFix()
                .assertCompilable()
                .assertOutput(
                       "package test;\n" +
                       "public class Test {\n" +
                       "     private void test() {\n" +
                       "         if (0 < Byte.parseByte(\"5\")) {}\n" +
                       "     }\n" +
                       "}\n");
    }
}