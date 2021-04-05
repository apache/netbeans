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
import org.openide.filesystems.FileUtil;

/**
 *
 * @author lahvac
 */
public class NPECheckTest extends NbTestCase {
    
    public NPECheckTest(String testName) {
        super(testName);
    }

    public void testSimpleNull1() throws Exception {
        performAnalysisTest("test/Test.java", "package test; class Test {private void test() {Object o; o = null; o.toString();}}", "0:69-0:77:verifier:DN");
    }
    
    public void testSimpleNull2() throws Exception {
        performAnalysisTest("test/Test.java", "package test; class Test {private void test() {Object o = null; o.toString();}}", "0:66-0:74:verifier:DN");
    }
    
    public void testIf1() throws Exception {
        performAnalysisTest("test/Test.java", "package test; class Test {private void test(Object o) {if (o == null) {o.toString();}}}", "0:73-0:81:verifier:DN");
    }
    
    public void testIf2() throws Exception {
        HintTest.create()
                .input("package test; class Test {private void test(Object o) {if (o == null) {o = \"\";} o.length();}}", false)
                .run(NPECheck.class)
                .assertWarnings();
    }
    
    public void testIf3() throws Exception {
        performAnalysisTest("test/Test.java", "package test; class Test {private void test() {String s = null; if (s == null) {s = \"\";} s.length();}}");
    }
    
    public void testIf4() throws Exception {
        performAnalysisTest("test/Test.java", "package test; class Test {private void test() {String s = null; if (s == null) {s = \"\";} else {s = \"\";} s.length();}}");
    }
    
    public void testIf5() throws Exception {
        performAnalysisTest("test/Test.java", "package test; class Test {private void test() {String s = null; if (s == null) {s = \"\";} else {s = null;} s.length();}}", "0:108-0:114:verifier:Possibly Dereferencing null");
    }
    
    public void testIf6() throws Exception {
        performAnalysisTest("test/Test.java", "package test; class Test {private void test(Object o) {if (null == o) {o.toString();}}}", "0:73-0:81:verifier:DN");
    }
    
    public void testIf7() throws Exception {
        performAnalysisTest("test/Test.java", "package test; class Test {private void test() {String o = null; if (o != null) {o.toString();}}}");
    }
    
    public void testIf8() throws Exception {
        performAnalysisTest("test/Test.java", "package test; class Test {private void test() {String o = null; if (null != o) {o.toString();}}}");
    }
    
    public void testIf9() throws Exception {
        HintTest.create()
                .input("package test; class Test {private void test(String s) {if (s == null) {} s.length();}}")
                .run(NPECheck.class)
                .assertWarnings("0:75-0:81:verifier:Possibly Dereferencing null");
    }
    
    public void testIfa() throws Exception {
        HintTest.create()
                .input("package test; class Test {private void test(String s1, String s2) {if (s1 == null) {s1 = s2;} s1.length();}}")
                .run(NPECheck.class)
                .assertWarnings();
    }
    
    public void testIfb() throws Exception {
        HintTest.create()
                .input("package test; class Test {private void test(@Null String s) {if (s == null) {throw new UnsupportedOperationException();} s.length();}} @interface Null {}")
                .run(NPECheck.class)
                .assertWarnings();
    }
    
    public void testIfc() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test; class Test {\n"+
                            "    private void test(int i, @CheckForNull String o) {\n" +
                            "        if (i > 2 && o != null && o.length() > 2) {\n" +
                            "        }\n" +
                            "    }\n" +
                            "    @interface CheckForNull{}\n" +
                            "}\n");
    }
    
    public void testIfd() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test; class Test { private void test(int i, @CheckForNull String o) {\n" +
                            "        if (i > 2 || o == null || o.length() > 2) {\n" +
                            "        }\n" +
                            "    }\n" +
                            "    @interface CheckForNull{}\n" +
                            "}\n");
    }
    
    public void testTernary1() throws Exception {
        performAnalysisTest("test/Test.java", "package test; class Test {private void test(int i) {String s = i == 0 ? \"\" : \"a\"; s.length();}}");
    }
    
    public void testTernary2() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test; class Test {private void test(String s) {Object o = s == null ? \"\" : s; s = s.toString();}}",
                            "0:92-0:100:verifier:Possibly Dereferencing null");
    }
    
    public void testTernary3() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "class Test {\n" +
                            "    public void test() {" +
                            "        String e = null;" +
                            "        String f = e == null ? \"\" : e.trim();" +
                            "    }" +
                            "}");
    }
    
    public void testNewClass() throws Exception {
        performAnalysisTest("test/Test.java", "package test; class Test {private void test() {String s = null; if (s == null) {s = new String(\"\");} s.length();}}");
    }
    
    public void testCheckForNull1() throws Exception {
        performAnalysisTest("test/Test.java", "package test; class Test {private void test() {String s = get(); s.length();} @Nullable private String get() {return \"\";} @interface Nullable {}}", "0:67-0:73:verifier:Possibly Dereferencing null");
    }
    
    public void testCheckForNull2() throws Exception {
        HintTest.create()
                .preference(NPECheck.KEY_ENABLE_FOR_FIELDS, true)
                .input("package test; class Test {private void test() {s.length();} @Nullable private String s; @interface Nullable {}}")
                .run(NPECheck.class)
                .assertWarnings("0:49-0:55:verifier:Possibly Dereferencing null");
    }
    
    public void testAssignNullToNotNull() throws Exception {
        HintTest.create()
                .preference(NPECheck.KEY_ENABLE_FOR_FIELDS, true)
                .input("package test; class Test {private void test() {s = null;} @NotNull private String s; @interface NotNull {}}")
                .run(NPECheck.class)
                .assertWarnings("0:47-0:55:verifier:ANNNV");
    }
    
    public void testPossibleAssignNullToNotNull() throws Exception {
        HintTest.create()
                .preference(NPECheck.KEY_ENABLE_FOR_FIELDS, true)
                .input("package test; class Test {private void test(int i) {String s2 = null; if (i == 0) {s2 = \"\";} s = s2;} @NotNull private String s; @interface NotNull {}}")
                .run(NPECheck.class)
                .assertWarnings("0:93-0:99:verifier:PANNNV");
    }
    
    public void testAssignNullToNotNullVarInitializer1() throws Exception {
        performAnalysisTest("test/Test.java", "package test; class Test {private void test() {@NotNull String s = null;} @interface NotNull {}}", "0:47-0:72:verifier:ANNNV");
    }
    
    public void testNullCheckAnd1() throws Exception {
        performAnalysisTest("test/Test.java", "package test; class Test {private void test() {String s = null; if (s != null && s.length() > 0) {}}}");
    }
    
    public void testNullCheckAnd2() throws Exception {
        performAnalysisTest("test/Test.java", "package test; class Test {private void test() {String s = null; if (s == null && s.length() > 0) {}}}", "0:83-0:89:verifier:DN");
    }
    
    public void testNullCheckOr1() throws Exception {
        performAnalysisTest("test/Test.java", "package test; class Test {private void test() {String s = null; if (s != null || s.length() > 0) {}}}", "0:83-0:89:verifier:DN");
    }
    
    public void testNullCheckOr2() throws Exception {
        performAnalysisTest("test/Test.java", "package test; class Test {private void test() {String s = null; if (s == null || s.length() > 0) {}}}");
    }
    
    public void testContinue1() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test; class Test {private void test(String[] sa) {for (String s : sa) {if (s == null) continue; s.length();}}}");
    }
    
    public void testCondition1() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test; class Test {private void test(String sa) {boolean b = sa != null && (sa.length() == 0 || sa.length() == 1);}}");
    }
    
    public void testCondition2() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test; class Test {private void test(String sa) {boolean b = sa == null || (sa.length() == 0 && sa.length() == 1);}}");
    }
    
    public void testCondition3() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test; class Test {private void test(int i) {Object o2 = i < 1 ? null : \"\"; boolean b = true && o2 != null && o2.toString() != \"\";}}");
    }
    
    public void testWhileAndIf() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "class Test {\n" +
                            "    private void test(int i, boolean b, Object o2) {\n" +
                            "        Object o = null;\n" +
                            "        while (--i > 0) {\n" +
                            "            if (b) {\n" +
                            "                o = o2;\n" +
                            "            }\n" +
                            "        }\n" +
                            "        o.toString();\n" +
                            "    }" +
                            "}",
                            "9:10-9:18:verifier:Possibly Dereferencing null");
    }
    
    public void testWhile1() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "class Test {\n" +
                            "    private void test(String o) {\n" +
                            "        while (o != null && o.length() > 1) {\n" +
                            "            o = o.substring(1);\n" +
                            "        }\n" +
                            "        o.toString();\n" +
                            "    }" +
                            "}",
                            "6:10-6:18:verifier:Possibly Dereferencing null");
    }
    
    public void testWhile2() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "class Test {\n" +
                            "    private void test(@CheckForNull String o) {\n" +
                            "        while (o != null) {\n" +
                            "            o = o.substring(1);\n" +
                            "        }\n" +
                            "    }" +
                            "    @interface CheckForNull {}\n" +
                            "}");
    }
    
    public void testParameter1() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test; class Test {private void test(int i) {String s = null; ttt(s);} private void ttt(@NotNull String s){}} @interface NotNull {}",
                            "0:73-0:74:verifier:ERR_NULL_TO_NON_NULL_ARG");
    }
    
    public void testParameter2() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test; class Test {private void test(int i) {ttt(t());} private void ttt(@NotNull String s){} private @CheckForNull String t() {return \"\";} } @interface NotNull {} @interface CheckForNull {}",
                            "0:56-0:59:verifier:ERR_POSSIBLENULL_TO_NON_NULL_ARG");
    }
    
    public void testParameter3() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test; class Test {private void test(int i) {ttt(null, t());} private void ttt(@NullAllowed String na, @NotNull String s){} private @CheckForNull String t() {return \"\";} } @interface NotNull {} @interface CheckForNull {} @interface NullAllowed {}",
                            "0:62-0:65:verifier:ERR_POSSIBLENULL_TO_NON_NULL_ARG");
    }
    
    public void testParameter4() throws Exception {
        HintTest.create()
                .sourceLevel("8")
                .input("test/Test.java", 
                       "package test; \n" +
                       "import java.util.function.Function; \n" +
                       "class Test { \n" +
                       "       private void func(@NotNull Function<Object, Object> func){} \n" +
                       "       @CheckForNull private Object foo(){ return null;} \n" +
                       "       private void bar(){func((item)->foo());} \n" +
                       "       private @CheckForNull String t() { return null;}} \n" +
                       "       @interface NotNull {} \n" +
                       "       @interface CheckForNull {}")
                .run(NPECheck.class)
                .assertNotContainsWarnings("ERR_POSSIBLENULL_TO_NON_NULL_ARG");        
    }
    
    public void testNoMultipleReports1() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "class Test {\n" +
                            "    private void test(@CheckForNull String o) {\n" +
                            "        o.toString();\n" +
                            "        o.toString();\n" +
                            "        o.toString();\n" +
                            "    }" +
                            "    @interface CheckForNull {}\n" +
                            "}", "3:10-3:18:verifier:Possibly Dereferencing null");
    }
    
    public void testNoMultipleReports2() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "class Test {\n" +
                            "    private void test(@CheckForNull String o) {\n" +
                            "        if (o.length() > 1 && o.length() > 2);\n" +
                            "    }" +
                            "    @interface CheckForNull {}\n" +
                            "}", "3:14-3:20:verifier:Possibly Dereferencing null");
    }
    
    public void testNoMultipleReports3() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "class Test {\n" +
                            "    private void test(@CheckForNull String o) {\n" +
                            "        boolean b = o.length() > 1 && o.length() > 2;\n" +
                            "    }" +
                            "    @interface CheckForNull {}\n" +
                            "}", "3:22-3:28:verifier:Possibly Dereferencing null");
    }
    
    public void testNoMultipleReports4() throws Exception {
        performAnalysisTest("test/Test.java",
                            "package test;\n" +
                            "class Test {\n" +
                            "    private void test(@CheckForNull String o) {\n" +
                            "        boolean b = o.length() > 1 && o.length() > 2 ? false : true;\n" +
                            "    }" +
                            "    @interface CheckForNull {}\n" +
                            "}", "3:22-3:28:verifier:Possibly Dereferencing null");
    }
    
    public void testWouldAlreadyBeNPE() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    public void t(String str) {\n" +
                       "        int i = str.length();\n" +
                       "        if (str != null) {\n" +
                       "            System.err.println(\"A\");\n" +
                       "        }\n" +
                       "        boolean e = str.equals(\"\");\n" +
                       "    }\n" +
                       "}")
                .run(NPECheck.class)
                .assertWarnings("4:12-4:23:verifier:ERR_NotNullWouldBeNPE");
    }
    
    public void testCCE() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    private void test() {\n" +
                       "        c.s Object method = new Object();\n" +
                       "    }" +
                       "}", false)
                .run(NPECheck.class)
                .assertWarnings(/*"3:20-3:28:verifier:Possibly Dereferencing null"*/);
    }
    
    public void testVarArgs1() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    public void t(String str, @NonNull Object... obj) {\n" +
                       "        t(\"\");\n" +
                       "    }\n" +
                       "    @interface NonNull {}\n" +
                       "}")
                .run(NPECheck.class)
                .assertWarnings();
    }
    
    public void testInstanceOfIsNullCheck() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    public void t(@NullAllowed Object obj) {\n" +
                       "        if (obj instanceof String) {\n" +
                       "            System.err.println(obj.toString());\n" +
                       "        }\n" +
                       "    }\n" +
                       "    @interface NullAllowed {}\n" +
                       "}")
                .run(NPECheck.class)
                .assertWarnings();
    }
    
    public void test217589a() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    public static void processThrowable(@NullAllowed Object obj) {\n" +
                       "        if (obj != null) {\n" +
                       "            if (obj instanceof Integer) {\n" +
                       "            } else {\n" +
                       "                String s = obj.toString();\n" +
                       "            }\n" +
                       "        }\n" +
                       "    }\n" +
                       "@interface NullAllowed {}\n" +
                       "}")
                .run(NPECheck.class)
                .assertWarnings();
    }
    
    public void test217589b() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    public static void processThrowable(@NullAllowed Object obj) {\n" +
                       "        if (obj instanceof Integer) {\n" +
                       "        } else {\n" +
                       "            String s = obj.toString();\n" +
                       "        }\n" +
                       "    }\n" +
                       "@interface NullAllowed {}\n" +
                       "}")
                .run(NPECheck.class)
                .assertWarnings("5:27-5:35:verifier:Possibly Dereferencing null");
    }
    
    public void test217589c() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    public static void processThrowable(@NonNull Object obj) {\n" +
                       "            if (!(obj instanceof Integer)) {\n" +
                       "                String s = obj.toString();\n" +
                       "            }\n" +
                       "    }\n" +
                       "@interface NonNull {}\n" +
                       "}")
                .run(NPECheck.class)
                .assertWarnings();
    }
    
    public void testNeg220162a() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    public static void processThrowable(@NullAllowed Object obj) {\n" +
                       "            if (!(obj == null)) {\n" +
                       "                String s = obj.toString();\n" +
                       "            }\n" +
                       "    }\n" +
                       "@interface NullAllowed {}\n" +
                       "}")
                .run(NPECheck.class)
                .assertWarnings();
    }
    
    public void testNeg220162b() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    public static void processThrowable(@NullAllowed Object obj) {\n" +
                       "            if (!(obj != null)) {\n" +
                       "                String s = obj.toString();\n" +
                       "            }\n" +
                       "    }\n" +
                       "@interface NullAllowed {}\n" +
                       "}")
                .run(NPECheck.class)
                .assertWarnings("4:31-4:39:verifier:DN");
    }
    
    public void testOr217589() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    private void testMethod(String id) {\n" +
                       "        if (id != null ) {\n" +
                       "            boolean isFoo= true || id.equalsIgnoreCase(\"text\");\n" +
                       "        }\n" +
                       "    }\n" +
                       "}")
                .run(NPECheck.class)
                .assertWarnings();
    }
    
    public void testSimpleMethodBoundaryCheck219006() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    public Object obj = null;\n" +
                       "    public String toString() {\n" +
                       "        return obj.toString();\n" +
                       "    }\n" +
                       "    public void t() {\n" +
                       "        if (obj != null) {\n" +
                       "            System.err.println(obj.toString());\n" +
                       "        }\n" +
                       "    }\n" +
                       "}")
                .run(NPECheck.class)
                .assertWarnings();
    }
    
    public void testIfWithMultipartCondition() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    public void t(Object obj) {\n" +
                       "        if (obj == null || obj.hashCode() == 0) {\n" +
                       "            return ;\n" +
                       "        }\n" +
                       "        System.err.println(obj.toString());\n" +
                       "    }\n" +
                       "}")
                .run(NPECheck.class)
                .assertWarnings();
    }
    
    public void testForLoop() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "import javax.swing.tree.TreePath;\n" +
                       "class Test {\n" +
                       "    public void t(TreePath tp) {\n" +
                       "        tp.toString();\n" +
                       "        for (TreePath p = tp; p != null; p = p.getParentPath()) {\n" +
                       "            System.err.println(p.getLastPathComponent());\n" +
                       "        }\n" +
                       "    }\n" +
                       "}")
                .run(NPECheck.class)
                .assertWarnings();
    }
    
    public void testWhile() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "import javax.swing.tree.TreePath;\n" +
                       "class Test {\n" +
                       "    public void t(TreePath tp) {\n" +
                       "        tp.toString();\n" +
                       "        while (tp != null) {\n" +
                       "            tp = tp.getParentPath();\n" +
                       "        }\n" +
                       "    }\n" +
                       "}")
                .run(NPECheck.class)
                .assertWarnings();
    }
    
    public void testNull2NonNullReturnValue() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "import javax.swing.tree.TreePath;\n" +
                       "class Test {\n" +
                       "    public @NonNull String t() {\n" +
                       "        return null;\n" +
                       "    }\n" +
                       "    @interface NonNull {}\n" +
                       "}")
                .run(NPECheck.class)
                .assertWarnings("4:15-4:19:verifier:ERR_ReturningNullFromNonNull");
    }
    
    public void testPossibleNull2NonNullReturnValue() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "import javax.swing.tree.TreePath;\n" +
                       "class Test {\n" +
                       "    public @NonNull String t(@NullAllowed String str) {\n" +
                       "        return str;\n" +
                       "    }\n" +
                       "    @interface NonNull {}\n" +
                       "    @interface NullAllowed {}\n" +
                       "}")
                .run(NPECheck.class)
                .assertWarnings("4:15-4:18:verifier:ERR_ReturningPossibleNullFromNonNull");
    }
    
    public void testTernaryWithMultipartCondition() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    public String t(Object obj) {\n" +
                       "        return (obj == null || obj.hashCode() == 0) ? null : obj.toString();\n" +
                       "    }\n" +
                       "}")
                .run(NPECheck.class)
                .assertWarnings();
    }
    
    public void test222576a() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    public void foo(@NullAllowed Integer position, int id) {\n" +
                       "        position = id++;\n" +
                       "        if (position == null) position = 1;\n" +
                       "        if (position == null) position = 2;\n" +
                       "    }\n" +
                       "    @interface NullAllowed {}\n" +
                       "}")
                .run(NPECheck.class)
                .assertWarnings("4:12-4:28:verifier:ERR_NotNull",
                                "5:12-5:28:verifier:ERR_NotNull");
    }
    
    public void test222576b() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "import java.util.Set;\n" +
                       "class Test {\n" +
                       "    public int foo(Object provider, int id, Set<Integer> vendors) {\n" +
                       "        Integer position = (Integer) provider;\n" +
                       "        if (position == null || vendors.contains(position)) {\n" +
                       "            position = id++;\n" +
                       "        }\n" +
                       "        int value = position.intValue();\n" +
                       "        return value;\n" +
                       "    }\n" +
                       "}")
                .run(NPECheck.class)
                .assertWarnings();
    }
    
    public void test222580a() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    public boolean foo(String name, String path) {\n" +
                       "        if (path == null) {\n" +
                       "            path = name;\n" +
                       "        } else {\n" +
                       "            path += \".\" + name;\n" +
                       "        }\n" +
                       "        return path.equals(\"bin\");\n" +
                       "    }\n" +
                       "}")
                .run(NPECheck.class)
                .assertWarnings();
    }
    
    public void test222580b() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    public boolean foo(@NullAllowed String path) {\n" +
                       "        if (path == null) {\n" +
                       "            path = \"\";\n" +
                       "        } else {\n" +
                       "            path += \".\";\n" +
                       "        }\n" +
                       "        return path.equals(\"bin\");\n" +
                       "    }\n" +
                       "    @interface NullAllowed {}\n" +
                       "}")
                .run(NPECheck.class)
                .assertWarnings();
    }
    
    public void testAssert222795() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    public boolean foo(@NullAllowed String path) {\n" +
                       "        assert path != null;\n" +
                       "        return path.equals(\"bin\");\n" +
                       "    }\n" +
                       "    @interface NullAllowed {}\n" +
                       "}")
                .run(NPECheck.class)
                .assertWarnings();
    }
    
    public void testCleanup1() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    public boolean foo(@NullAllowed String path) {\n" +
                       "        assert path != null;\n" +
                       "        if (path == null) { }\n" +
                       "        return path.equals(\"bin\");\n" +
                       "    }\n" +
                       "    @interface NullAllowed {}\n" +
                       "}")
                .run(NPECheck.class)
                .assertWarnings("4:12-4:24:verifier:ERR_NotNull");
    }
    
    public void testArrayAccess() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    public void foo(String[] paths) {\n" +
                       "        assert paths != null;\n" +
                       "        if (paths[0] != null) { System.err.println(paths[0]); }\n" +
                       "    }\n" +
                       "}")
                .run(NPECheck.class)
                .assertWarnings();
    }
    
    public void testWhileInitialize() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    public void foo(int i) {\n" +
                       "        java.util.List<String> result = null;\n" +
                       "        while (i-- > 0) {\n" +
                       "             if (result == null) {\n" +
                       "                 result = new java.util.ArrayList<String>();\n" +
                       "             }\n" +
                       "             result.add(String.valueOf(i));\n" +
                       "        }\n" +
                       "        if (result == null) {\n" +
                       "            System.err.println(\"still null\");\n" +
                       "        }" +
                       "    }\n" +
                       "}")
                .run(NPECheck.class)
                .assertWarnings();
    }
    
    public void testForInitialize() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    public void foo(int i) {\n" +
                       "        java.util.List<String> result = null;\n" +
                       "        for ( ; i-- > 0 ; ) {\n" +
                       "             if (result == null) {\n" +
                       "                 result = new java.util.ArrayList<String>();\n" +
                       "             }\n" +
                       "             result.add(String.valueOf(i));\n" +
                       "        }\n" +
                       "        if (result == null) {\n" +
                       "            System.err.println(\"still null\");\n" +
                       "        }" +
                       "    }\n" +
                       "}")
                .run(NPECheck.class)
                .assertWarnings();
    }
    
    public void testAnd1() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    private void t(String str) {\n" +
                       "        boolean empty = str != null && str.isEmpty();\n" +
                       "        if (empty || str == null) {\n" +
                       "            throw new IllegalStateException();\n" +
                       "        }\n" +
                       "    }\n" +
                       "}")
                .run(NPECheck.class)
                .assertWarnings();
    }
    
    public void testAnd2() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    private void t(String str) {\n" +
                       "        boolean empty;\n" +
                       "        empty = str != null && str.isEmpty();\n" +
                       "        if (empty || str == null) {\n" +
                       "            throw new IllegalStateException();\n" +
                       "        }\n" +
                       "    }\n" +
                       "}")
                .run(NPECheck.class)
                .assertWarnings();
    }
    
    public void testAnd3() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    private void t(String str) {\n" +
                       "        boolean empty = true;\n" +
                       "        empty &= str != null && str.isEmpty();\n" +
                       "        if (empty || str == null) {\n" +
                       "            throw new IllegalStateException();\n" +
                       "        }\n" +
                       "    }\n" +
                       "}")
                .run(NPECheck.class)
                .assertWarnings();
    }
    
    public void test222871() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "    private void t(@CheckForNull String onSuccess, @CheckForNull Integer onError) {\n" +
                       "        checkState(onSuccess != null || onError != null);\n" +
                       "        checkState(onSuccess == null || onError == null);\n" +
                       "    }\n" +
                       "    private void checkState(boolean b) {}\n" +
                       "    @interface CheckForNull {}\n" +
                       "}")
                .run(NPECheck.class)
                .assertWarnings();
    }
    
    public void testWhileInitializeWithField() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    java.util.List<String> result;\n" +
                       "    public void foo(int i) {\n" +
                       "        while (i-- > 0) {\n" +
                       "             if (result == null) {\n" +
                       "                 result = new java.util.ArrayList<String>();\n" +
                       "             }\n" +
                       "             result.add(String.valueOf(i));\n" +
                       "        }\n" +
                       "        if (result == null) {\n" +
                       "            System.err.println(\"still null\");\n" +
                       "        }" +
                       "    }\n" +
                       "}")
                .run(NPECheck.class)
                .assertWarnings();
    }
    
    public void testSwitch1() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "import java.lang.annotation.RetentionPolicy;\n" +
                       "class Test {\n" +
                       "    private void t(RetentionPolicy pol, @CheckForNull String str) {\n" +
                       "        switch (pol) {\n" +
                       "            case CLASS: str = \"\"; break;\n" +
                       "            case RUNTIME:\n" +
                       "                if (str != null) {\n" +
                       "                    str = \"\";\n" +
                       "                    break;\n" +
                       "                }\n" +
                       "            case SOURCE:\n" +
                       "                str = \"a\";\n" +
                       "                break;\n" +
                       "            default:\n" +
                       "                str = \"b\";\n" +
                       "                break;\n" +
                       "        }\n" +
                       "        if (str == null) {\n" +
                       "            System.err.println(\"should not be null\");\n" +
                       "        }\n" +
                       "    }\n" +
                       "    @interface CheckForNull {}\n" +
                       "}")
                .run(NPECheck.class)
                .assertWarnings("18:12-18:23:verifier:ERR_NotNull");
    }
    
    public void testSwitch2() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "import java.lang.annotation.RetentionPolicy;\n" +
                       "class Test {\n" +
                       "    private void t(RetentionPolicy pol, @CheckForNull String str) {\n" +
                       "        switch (pol) {\n" +
                       "            case CLASS: str = \"\"; break;\n" +
                       "            case RUNTIME:\n" +
                       "                if (str != null) {\n" +
                       "                    str = \"\";\n" +
                       "                }\n" +
                       "                break;\n" +
                       "            case SOURCE:\n" +
                       "                str = \"a\";\n" +
                       "                break;\n" +
                       "            default:\n" +
                       "                str = \"b\";\n" +
                       "                break;\n" +
                       "        }\n" +
                       "        if (str == null) {\n" +
                       "            System.err.println(\"may be null\");\n" +
                       "        }\n" +
                       "    }\n" +
                       "    @interface CheckForNull {}\n" +
                       "}")
                .run(NPECheck.class)
                .assertWarnings();
    }
    
    public void testSwitch3() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "import java.lang.annotation.RetentionPolicy;\n" +
                       "class Test {\n" +
                       "    private void t(RetentionPolicy pol, @CheckForNull String str) {\n" +
                       "        switch (pol) {\n" +
                       "            case CLASS: str = \"\"; break;\n" +
                       "            case RUNTIME:\n" +
                       "                if (str != null) {\n" +
                       "                    str = \"\";\n" +
                       "                    break;\n" +
                       "                }\n" +
                       "            case SOURCE:\n" +
                       "                str = \"a\";\n" +
                       "                break;\n" +
                       "        }\n" +
                       "        if (str == null) {\n" +
                       "            System.err.println(\"should not be null\");\n" +
                       "        }\n" +
                       "    }\n" +
                       "    @interface CheckForNull {}\n" +
                       "}")
                .run(NPECheck.class)
                .assertWarnings();
    }
    
    public void testTry1() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "import java.lang.annotation.RetentionPolicy;\n" +
                       "class Test {\n" +
                       "    private void t(RetentionPolicy pol, @CheckForNull String str) {\n" +
                       "        switch (pol) {\n" +
                       "            case CLASS: str = \"\"; break;\n" +
                       "            case RUNTIME:\n" +
                       "                if (str != null) {\n" +
                       "                    str = \"\";\n" +
                       "                    break;\n" +
                       "                }\n" +
                       "            case SOURCE:\n" +
                       "                str = \"a\";\n" +
                       "                break;\n" +
                       "        }\n" +
                       "        if (str == null) {\n" +
                       "            System.err.println(\"should not be null\");\n" +
                       "        }\n" +
                       "    }\n" +
                       "    @interface CheckForNull {}\n" +
                       "}")
                .run(NPECheck.class)
                .assertWarnings();
    }
    
    public void testLoopExponentialExplosion() throws Exception {
        String sourceCode = "package test;\n" +
                            "import java.util.*;\n" +
                            "class Test {\n" +
                            "    private void t(List<String> args) {\n";
        
        for (int i = 0; i < 20; i++) {
            sourceCode += "for (Iterator<String> it" + i + " = args.iterator(); it" + i + ".hasNext(); )";
        }
        
        sourceCode += "if (args.size() == 0) System.err.println('a');\n" +
                      "    }\n" +
                      "}\n";
        HintTest.create()
                .input(sourceCode)
                .run(NPECheck.class)
                .assertWarnings();
    }
    
    public void test223297() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "import java.util.*;\n" +
                       "import java.util.concurrent.*;\n" +
                       "class Test {\n" +
                       "    public boolean foo() {\n" +
                       "        String name = \"a\";\n" +
                       "        String path = \"b\";\n" +
                       "        ConcurrentMap<String, List<String>> result = new ConcurrentHashMap<String, List<String>>();\n" +
                       "        List<String> list = result.get(name);\n" +
                       "        if (list == null) {\n" +
                       "            List<String> prev = result.putIfAbsent(name, list = new ArrayList<String>(1));\n" +
                       "            if (prev != null) {\n" +
                       "                list = prev;\n" +
                       "            }\n" +
                       "        }\n" +
                       "        return list.add(path);\n" +
                       "    }\n" +
                       "}")
                .run(NPECheck.class)
                .assertWarnings();
    }
    
    public void testTestedProduceWarning() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "import java.util.*;\n" +
                       "import java.util.concurrent.*;\n" +
                       "class Test {\n" +
                       "    public void foo(String param) {\n" +
                       "        boolean b = param != null;\n" +
                       "        System.err.println(param.toString());\n" +
                       "    }\n" +
                       "}")
                .run(NPECheck.class)
                .assertWarnings("6:33-6:41:verifier:Possibly Dereferencing null");
    }
    
    public void test224028() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "import java.io.*;\n" +
                       "import java.util.*;\n" +
                       "class Test {\n" +
                       "    private void doSomething(Properties props) {\n" +
                       "        if (props == null) {\n" +
                       "            props = new Properties();\n" +
                       "        }\n" +
                       "        props.clear();\n" +
                       "        try {\n" +
                       "            canThrow();\n" +
                       "        } catch (EmptyStackException | IOException ex) {\n" +
                       "        }\n" +
                       "        props.clear();\n" +
                       "    }\n" +
                       "    private void canThrow() throws EmptyStackException, IOException {\n" +
                       "    }\n" +
                       "}")
                .sourceLevel("1.7")
                .run(NPECheck.class)
                .assertWarnings();
    }
    
    public void testTry226184() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    private String str() throws Exception {\n" +
                       "        return \"\";\n" +
                       "    }\n" +
                       "    public int process() {\n" +
                       "        String builder = null;\n" +
                       "        try {\n" +
                       "            builder = str();\n" +
                       "        } catch (Exception ex) {\n" +
                       "            return 0;\n" +
                       "        }\n" +
                       "        return builder.length();\n" +
                       "    }\n" +
                       "}")
                .run(NPECheck.class)
                .assertWarnings();
    }
    
    public void testFields1() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "import java.io.*;\n" +
                       "import java.util.*;\n" +
                       "class Test {\n" +
                       "    private String str;\n" +
                       "    private void text() {\n" +
                       "        str = null;\n" +
                       "        System.err.println(str.length());\n" +
                       "    }\n" +
                       "}")
                .sourceLevel("1.7")
                .run(NPECheck.class)
                .assertWarnings();
    }
    
    public void testFields2() throws Exception {
        HintTest.create()
                .preference(NPECheck.KEY_ENABLE_FOR_FIELDS, true)
                .input("package test;\n" +
                       "import java.io.*;\n" +
                       "import java.util.*;\n" +
                       "class Test {\n" +
                       "    private String str;\n" +
                       "    private void text() {\n" +
                       "        str = null;\n" +
                       "        System.err.println(str.length());\n" +
                       "    }\n" +
                       "}")
                .sourceLevel("1.7")
                .run(NPECheck.class)
                .assertWarnings("7:31-7:37:verifier:DN");
    }
    
    public void testFields3() throws Exception {
        HintTest.create()
                .preference(NPECheck.KEY_ENABLE_FOR_FIELDS, false)
                .input("package test;\n" +
                       "class Test {\n" +
                       "    @Nullable private String str;\n" +
                       "    private void text() {\n" +
                       "        assert str != null;\n" +
                       "        System.err.println(str.length());\n" +
                       "    }\n" +
                       "    @interface Nullable {}\n" +
                       "}")
                .sourceLevel("1.7")
                .run(NPECheck.class)
                .assertWarnings();
    }
    
    public void test226421a() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    static String hashOfToString(Object p) {\n" +
                       "        try {\n" +
                       "            return p.toString();\n" +
                       "        } catch (RuntimeException e) {\n" +
                       "            if (p != null) {\n" +
                       "                System.err.println();\n" +
                       "            }\n" +
                       "            throw e;\n" +
                       "        }\n" +
                       "    }\n" +
                       "}")
                .sourceLevel("1.7")
                .run(NPECheck.class)
                .assertWarnings();
    }
    
    public void test226421b() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    static String hashOfToString(Object p) {\n" +
                       "        if (p == null) throw new IllegalStateException();\n" +
                       "        try {\n" +
                       "            return p.toString();\n" +
                       "        } catch (RuntimeException e) {\n" +
                       "            if (p != null) {\n" +
                       "                System.err.println();\n" +
                       "            }\n" +
                       "            throw e;\n" +
                       "        }\n" +
                       "    }\n" +
                       "}")
                .sourceLevel("1.7")
                .run(NPECheck.class)
                .assertWarnings("7:16-7:25:verifier:ERR_NotNull");
    }
    
    public void test226558() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "import java.util.*;\n" +
                       "class Test {\n" +
                       "    @NotNull\n" +
                       "    public List<String> n(@Nullable List<String> l) {\n" +
                       "        if (l == null) {\n" +
                       "            return Collections.emptyList();\n" +
                       "        }\n" +
                       "        return l;\n" +
                       "    }\n" +
                       "    @interface Nullable {}\n" +
                       "    @interface NotNull {}\n" +
                       "}")
                .sourceLevel("1.7")
                .run(NPECheck.class)
                .assertWarnings();
    }
    
    public void test226923() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "import java.util.*;\n" +
                       "class Test {\n" +
                       "    private static void test(String str) {\n" +
                       "        System.err.println(str != null);\n" +
                       "        if ((str != null) && (str.length() == 0)) {\n" +
                       "            System.err.println(0);\n" +
                       "        }\n" +
                       "    }\n" +
                       "}")
                .sourceLevel("1.7")
                .run(NPECheck.class)
                .assertWarnings();
    }
    
    public void test227954() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "import java.util.*;\n" +
                       "class Test {\n" +
                       "    private static void test(@NullAllowed String str) {\n" +
                       "        if (str != null && str.equals(\"${\") && str.equals(\"{\")) {\n" +
                       "            System.err.println(0);\n" +
                       "        }\n" +
                       "    }\n" +
                       "    @interface NullAllowed {}\n" +
                       "}")
                .sourceLevel("1.7")
                .run(NPECheck.class)
                .assertWarnings();
    }
    
    public void test227745() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "import java.util.*;\n" +
                       "class Test {\n" +
                       "    public static void test2(String a, Iterable<String> list) {\n" +
                       "        if (null == a) return;\n" +
                       "        for (String s : list) {\n" +
                       "            if (null == a) return;\n" +
                       "            if (\"\".equals(s)) a = null;\n" +
                       "        }\n" +
                       "    }\n" +
                       "}")
                .run(NPECheck.class)
                .assertWarnings();
    }
    
    public void test228239a() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    private static void t(@NonNull Data d) {\n" +
                       "        if (d.a == null) {\n" +
                       "            System.err.println(\"null\");\n" +
                       "        }\n" +
                       "        t(new Data(null));\n" +
                       "    }\n" +
                       "    @interface NonNull {}\n" +
                       "    public static class Data {\n" +
                       "        public final String a;\n" +
                       "        public Data(String a) {\n" +
                       "            this.a = a;\n" +
                       "        }\n" +
                       "    }\n" +
                       "}")
                .run(NPECheck.class)
                .assertWarnings();
    }
    
    public void test228239b() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    private static void t(@NonNull Data d) {\n" +
                       "        if (d.a == null) {\n" +
                       "            System.err.println(\"null\");\n" +
                       "        }\n" +
                       "    }\n" +
                       "    @interface NonNull {}\n" +
                       "    public static class Data {\n" +
                       "        @NonNull public final String a;\n" +
                       "        public Data(@NonNull String a) {\n" +
                       "            this.a = a;\n" +
                       "        }\n" +
                       "    }\n" +
                       "}")
                .run(NPECheck.class)
                .assertWarnings("3:12-3:23:verifier:ERR_NotNull");
    }
    
    public void testInstanceOf229540() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    private void test(@NullAllowed Object node) {\n" +
                       "        System.err.println(node instanceof String);\n" +
                       "        if (node != null);\n" +
                       "    }\n" +
                       "    @interface NullAllowed {}\n" +
                       "}")
                .run(NPECheck.class)
                .assertWarnings();
    }
    
    public void testAssertNullNotNullTestNG225030() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    private void test1(@NullAllowed Object node) {\n" +
                       "        org.testng.Assert.assertNotNull(node);\n" +
                       "        node.toString();\n" +
                       "    }\n" +
                       "    private void test2(@NullAllowed Object node) {\n" +
                       "        org.testng.Assert.assertNull(node);\n" +
                       "        node.toString();\n" +
                       "    }\n" +
                       "    private void test3(@NullAllowed Object node) {\n" +
                       "        org.testng.Assert.assertNotNull(node, \"message\");\n" +
                       "        node.toString();\n" +
                       "    }\n" +
                       "    private void test4(@NullAllowed Object node) {\n" +
                       "        org.testng.Assert.assertNull(node, \"message\");\n" +
                       "        node.toString();\n" +
                       "    }\n" +
                       "    @interface NullAllowed {}\n" +
                       "}")
                .classpath(FileUtil.getArchiveRoot(org.testng.Assert.class.getProtectionDomain().getCodeSource().getLocation()))
                .run(NPECheck.class)
                .assertWarnings("8:13-8:21:verifier:DN", "16:13-16:21:verifier:DN");
    }
    
    public void testAssertNullNotNullJUnit225030a() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    private void test1(@NullAllowed Object node) {\n" +
                       "        org.junit.Assert.assertNotNull(node);\n" +
                       "        node.toString();\n" +
                       "    }\n" +
                       "    private void test2(@NullAllowed Object node) {\n" +
                       "        org.junit.Assert.assertNull(node);\n" +
                       "        node.toString();\n" +
                       "    }\n" +
                       "    private void test3(@NullAllowed Object node) {\n" +
                       "        org.junit.Assert.assertNotNull(\"message\", node);\n" +
                       "        node.toString();\n" +
                       "    }\n" +
                       "    private void test4(@NullAllowed Object node) {\n" +
                       "        org.junit.Assert.assertNull(\"message\", node);\n" +
                       "        node.toString();\n" +
                       "    }\n" +
                       "    @interface NullAllowed {}\n" +
                       "}")
                .classpath(FileUtil.getArchiveRoot(org.junit.Assert.class.getProtectionDomain().getCodeSource().getLocation()))
                .run(NPECheck.class)
                .assertWarnings("8:13-8:21:verifier:DN", "16:13-16:21:verifier:DN");
    }
    
    public void testAssertNullNotNullJUnit225030b() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    private void test1(@NullAllowed Object node) {\n" +
                       "        junit.framework.Assert.assertNotNull(node);\n" +
                       "        node.toString();\n" +
                       "    }\n" +
                       "    private void test2(@NullAllowed Object node) {\n" +
                       "        junit.framework.Assert.assertNull(node);\n" +
                       "        node.toString();\n" +
                       "    }\n" +
                       "    private void test3(@NullAllowed Object node) {\n" +
                       "        junit.framework.Assert.assertNotNull(\"message\", node);\n" +
                       "        node.toString();\n" +
                       "    }\n" +
                       "    private void test4(@NullAllowed Object node) {\n" +
                       "        junit.framework.Assert.assertNull(\"message\", node);\n" +
                       "        node.toString();\n" +
                       "    }\n" +
                       "    @interface NullAllowed {}\n" +
                       "}")
                .classpath(FileUtil.getArchiveRoot(junit.framework.Assert.class.getProtectionDomain().getCodeSource().getLocation()))
                .run(NPECheck.class)
                .assertWarnings("8:13-8:21:verifier:DN", "16:13-16:21:verifier:DN");
    }
    
    public void testResumeOnExceptionHandleMemory230238() throws Exception {
        StringBuilder code = new StringBuilder();
        code.append("package test;\n" +
                    "class Test {\n" +
                    "    private void t(int i) throws java.io.IOException { }\n" +
                    "    private void c() throws Exception {\n");
        for (int v = 0; v < 1000; v++) {
            code.append("String str" + v + " = null;");
        }
        for (int c = 0; c < 20000; c++) {
            code.append("t(0);");
        }
        code.append("    }\n" +
                    "}\n");
        HintTest.create()
                .input(code.toString())
                .run(NPECheck.class)
                .assertWarnings();
    }
    
    public void testRecordExceptionsFromNoArgMethod() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    private void test() throws java.io.IOException {\n" +
                       "    }\n" +
                       "    private void test2() {\n" +
                       "        String str = \"\";\n" +
                       "        try {\n" +
                       "            str = null;\n" +
                       "            test();\n" +
                       "            str = \"\";\n" +
                       "        } catch (java.io.IOException ex) {\n" +
                       "        }\n" +
                       "        str.toString();\n" +
                       "    }\n" +
                       "}")
                .run(NPECheck.class)
                .assertWarnings("12:12-12:20:verifier:Possibly Dereferencing null");
    }
    
    public void testIgnorePreconditionTests225970a() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    public void test (@NonNull String str) {\n" +
                       "        assert str != null;\n" +
                       "    }\n" +
                       "    @interface NonNull { }\n" +
                       "}")
                .run(NPECheck.class)
                .assertWarnings();
    }
    
    public void testIgnorePreconditionTests225970b() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    public void test (@NonNull String str) {\n" +
                       "        if (str == null) throw new NullPointerException();\n" +
                       "    }\n" +
                       "    @interface NonNull { }\n" +
                       "}")
                .run(NPECheck.class)
                .assertWarnings();
    }
    
    public void testIgnorePreconditionTests225970c() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    public void test (@NonNull String str1, @NonNull String str2) {\n" +
                       "        assert str1 != null && str2 != null;\n" +
                       "    }\n" +
                       "    @interface NonNull { }\n" +
                       "}")
                .run(NPECheck.class)
                .assertWarnings();
    }
    
    public void testIgnorePreconditionTests225970d() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "class Test {\n" +
                       "    public void test (@NonNull String str1, @NonNull String str2) {\n" +
                       "        if (str1 == null || str2 == null) throw new NullPointerException();\n" +
                       "    }\n" +
                       "    @interface NonNull { }\n" +
                       "}")
                .run(NPECheck.class)
                .assertWarnings();
    }
    
    public void testDoWhile232484() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "import java.io.*;\n" +
                       "class Test {\n" +
                       "    public static void m(BufferedReader br) throws IOException {\n" +
                       "       String line;\n" +
                       "       do {\n" +
                       "          line = br.readLine();\n" +
                       "       } while(line != null);\n" +
                       "       if(line != null) {\n" +
                       "       }\n" +
                       "   }\n" +
                       "}")
                .run(NPECheck.class)
                .assertWarnings();
    }
    
    public void testDoWhileIssue240643() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "import java.io.*;\n" +
                       "class Test {\n" +
                       "public static void main(String[] args) {\n" +
                        "        Test var = null;\n" +
                        "        do {\n" +
                        "            var = createNullDereferenceHint();\n" +
                        "            if (var == null) {\n" +
                        "                System.out.println(\"NullDereferenceHint still null!\");\n" +
                        "            }\n" +
                        "        } while (var == null);\n" +
                        "\n" +
                        "        Class<?> clazz = var.getClass(); \n" +
                        "    }\n" +
                        "\n" +
                        "    public static Test createNullDereferenceHint() {\n" +
                        "        return new Test();\n" +
                        "    }" + 
                        "}")
                .run(NPECheck.class)
                .assertWarnings();
    }
    
    public void testUnboxingInTernaryOperator269269() throws Exception {
        HintTest.create()
                .sourceLevel("8")
                .input("package test;\n"
                        + "import java.util.ArrayList;\n"
                        + "import java.util.List;\n"
                        + "public class Test {\n"
                        + "    static int test() {\n"
                        + "        List<Integer> maxStepS = new ArrayList<>(); \n"
                        + "        maxStepS.add(null);\n"
                        + "        Integer maxStepId = maxStepS.isEmpty() ? 0 : maxStepS.get(0);\n"
                        + "        return maxStepId == null ? 0 : maxStepId;\n"
                        + "    }\n"
                        + "}")
                .run(NPECheck.class)
                .assertWarnings(
                    "7:53-7:68:verifier:ERR_UnboxingPotentialNullValue", 
                    "8:15-8:32:verifier:ERR_NotNull"
                );
    }

    /**
     * The fetched value is first dereferenced, so unboxing happens, but on known-not-null value.
     * @throws Exception 
     */
    public void testUnboxingOKI0nTernaryOperator() throws Exception {
        HintTest.create()
                .sourceLevel("8")
                .input("package test;\n"
                        + "import java.util.ArrayList;\n"
                        + "import java.util.List;\n"
                        + "public class Test {\n"
                        + "    static int test() {\n"
                        + "        List<Integer> maxStepS = new ArrayList<>(); \n"
                        + "        maxStepS.add(null);\n"
                        + "        Integer a = maxStepS.get(0);\n"
                        + "        a.intValue();\n"
                        + "        Integer maxStepId = maxStepS.isEmpty() ? 0 : a;\n"
                        + "        return maxStepId == null ? 0 : maxStepId;\n"
                        + "    }\n"
                        + "}")
                .run(NPECheck.class)
                .assertWarnings(
                    "10:15-10:32:verifier:ERR_NotNull"
                );
    }
    
    /**
     * Wrappes are unboxed in order to promote smaller type. The true branch is 
     * known to be non-nul, so just the false branch is reported.
     * @throws Exception 
     */
    public void testUnboxingInTernaryOperatorWrapperPromotionOne() throws Exception {
        HintTest.create()
                .sourceLevel("8")
                .input("package test;\n"
                        + "import java.util.ArrayList;\n"
                        + "import java.util.List;\n"
                        + "public class Test {\n"
                        + "    static int test() {\n"
                        + "        List<Integer> maxStepS = new ArrayList<>(); \n"
                        + "        maxStepS.add(null);\n"
                        + "        Integer maxStepId = maxStepS.isEmpty() ? Short.valueOf((short)0) : maxStepS.get(0);\n"
                        + "        return maxStepId == null ? 0 : maxStepId;\n"
                        + "    }\n"
                        + "}")
                .run(NPECheck.class)
                .assertWarnings(
                        "7:75-7:90:verifier:ERR_UnboxingPotentialNullValue", 
                        "8:15-8:32:verifier:ERR_NotNull"
                );
    }

    /**
     * Wrappes are unboxed in order to promote smaller type.
     * @throws Exception 
     */
    public void testUnboxingInTernaryOperatorWrapperPromotionTwo() throws Exception {
        HintTest.create()
                .sourceLevel("8")
                .input("package test;\n"
                        + "import java.util.ArrayList;\n"
                        + "import java.util.List;\n"
                        + "public class Test {\n"
                        + "    static int test(Short s) {\n"
                        + "        List<Integer> maxStepS = new ArrayList<>(); \n"
                        + "        maxStepS.add(null);\n"
                        + "        Integer maxStepId = maxStepS.isEmpty() ? s : maxStepS.get(0);\n"
                        + "        return maxStepId == null ? 0 : maxStepId;\n"
                        + "    }\n"
                        + "}")
                .run(NPECheck.class)
                .assertWarnings(
                        "7:49-7:50:verifier:ERR_UnboxingPotentialNullValue", 
                        "7:53-7:68:verifier:ERR_UnboxingPotentialNullValue", 
                        "8:15-8:32:verifier:ERR_NotNull"
                );
    }

    /**
     * Check that option disables the unboxing warnings
     */
    public void testUnboxingInTernaryOperatorWrapperDisabledByOption() throws Exception {
        HintTest.create()
                .sourceLevel("8")
                .preference(NPECheck.KEY_UNBOXING_UNKNOWN_VALUES, false)
                .input("package test;\n"
                        + "import java.util.ArrayList;\n"
                        + "import java.util.List;\n"
                        + "public class Test {\n"
                        + "    static int test(Short s) {\n"
                        + "        List<Integer> maxStepS = new ArrayList<>(); \n"
                        + "        maxStepS.add(null);\n"
                        + "        Integer maxStepId = maxStepS.isEmpty() ? s : maxStepS.get(0);\n"
                        + "        return maxStepId == null ? 0 : maxStepId;\n"
                        + "    }\n"
                        + "}")
                .run(NPECheck.class)
                .assertWarnings(
                        "8:15-8:32:verifier:ERR_NotNull"
                );
    }

    /**
     */
    public void testUnboxingInTernaryPossibleNullWorksWithOption() throws Exception {
        HintTest.create()
                .sourceLevel("8")
                .preference(NPECheck.KEY_UNBOXING_UNKNOWN_VALUES, false)
                .input("package test;\n"
                        + "import java.util.ArrayList;\n"
                        + "import java.util.List;\n"
                        + "public class Test {\n"
                        + "    static int test(Short s) {\n"
                        + "        if (s == null) {} \n"
                        + "        List<Integer> maxStepS = new ArrayList<>(); \n"
                        + "        maxStepS.add(null);\n"
                        + "        Integer maxStepId = maxStepS.isEmpty() ? s : maxStepS.get(0);\n"
                        + "        return maxStepId == null ? 0 : maxStepId;\n"
                        + "    }\n"
                        + "}")
                .run(NPECheck.class)
                .assertWarnings(
                        "8:49-8:50:verifier:ERR_UnboxingPotentialNullValue",
                        "9:15-9:32:verifier:ERR_NotNull"
                );
    }

    public void testLambdaExpressionShouldntReturnNull() throws Exception {
        HintTest.create()
                .sourceLevel("8")
                .input("package test;\n" +
                        "\n" +
                        "import java.util.function.Function;\n" +
                        "\n" +
                        "public class Test {\n" +
                        "\n" +
                        "    interface Custom {\n" +
                        "        @Nonnull\n" +
                        "        Object doStuff();\n" +
                        "    }\n" +
                        "\n" +
                        "    @Nonnull\n" +
                        "    public static Object test() {\n" +
                        "        Function<String, String> f = (input) -> {\n" +
                        "            return null;\n" + // shouldn't warn (#271823)
                        "        };\n" +
                        "        Object o = (Custom) (() -> {\n" +
                        "            return null;\n" +
                        "        });\n" +
                        "        return new Object();\n" +
                        "    }\n" +
                        "\n" +
                        "    @interface Nonnull { }\n" +
                        "}")
                .run(NPECheck.class)
                .assertWarnings("17:19-17:23:verifier:ERR_ReturningNullFromNonNull");
    }

    public void testNETBEANS407a() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "  public void test(Object o) {\n" +
                       "    boolean b1 = o instanceof Integer;\n" +
                       "    boolean b2 = o instanceof Integer && o != null;\n" +
                       "    System.out.println(o.toString());\n" +
                       "  }\n" +
                       "}")
                .run(NPECheck.class)
                .assertWarnings("4:41-4:50:verifier:ERR_NotNull");
    }
    
    public void testNETBEANS407b() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "  public void test(Object o) {\n" +
                       "    boolean b = !(o instanceof Integer) && o.toString() != null;\n" +
                       "  }\n" +
                       "}")
                .run(NPECheck.class)
                .assertWarnings("3:45-3:53:verifier:Possibly Dereferencing null");
    }
    
    public void testNETBEANS407c() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "  public void test(Object o) {\n" +
                       "    boolean b = o != null;\n" +
                       "    System.out.println(o.toString());\n" +
                       "  }\n" +
                       "}")
                .run(NPECheck.class)
                .assertWarnings("4:25-4:33:verifier:Possibly Dereferencing null");
    }
    
    public void testNETBEANS407d() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "  public void test(Object o) {\n" +
                       "    boolean b = (o == null || o == \"\") && o.toString() != null;\n" +
                       "  }\n" +
                       "}")
                .run(NPECheck.class)
                .assertWarnings("3:44-3:52:verifier:Possibly Dereferencing null");
    }
    
    public void testExceptionIsNonNullNETBEANS734a() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "  public void test(Object o) {\n" +
                       "    try {\n" +
                       "    } catch (Exception e) {\n" +
                       "        if (e instanceof RuntimeException) {}\n" +
                       "        System.err.println(e.toString());\n" +
                       "    }\n" +
                       "  }\n" +
                       "}")
                .run(NPECheck.class)
                .assertWarnings();
    }

    public void testExceptionIsNonNullNETBEANS734b() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "  public void test(Object o) {\n" +
                       "    try {\n" +
                       "    } catch (Exception e) {\n" +
                       "        if (e != null) {}\n" +
                       "    }\n" +
                       "  }\n" +
                       "}")
                .run(NPECheck.class)
                .assertWarnings("5:12-5:21:verifier:ERR_NotNull");
    }

    public void testExceptionIsNonNullNETBEANS734c() throws Exception {
        HintTest.create()
                .input("package test;\n" +
                       "public class Test {\n" +
                       "  public void test(Object o) {\n" +
                       "    try {\n" +
                       "    } catch (Exception e) {\n" +
                       "        e = null;\n" +
                       "        System.err.println(e.toString());\n" +
                       "    }\n" +
                       "  }\n" +
                       "}")
                .run(NPECheck.class)
                .assertWarnings("6:29-6:37:verifier:DN");
    }

    private void performAnalysisTest(String fileName, String code, String... golden) throws Exception {
        HintTest.create()
                .input(fileName, code)
                .run(NPECheck.class)
                .assertWarnings(golden);
    }
}
